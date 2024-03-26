<template>
	<main>
		<div v-if="!showSpinner">
			<tera-simulate-chart
				v-for="(cfg, idx) in node.state.chartConfigs"
				:key="idx"
				:run-results="simulationRunResults[node.state.forecastRunId]"
				:chartConfig="{ selectedRun: node.state.forecastRunId, selectedVariable: cfg }"
				has-mean-line
				:size="{ width: 180, height: 120 }"
				@configuration-change="configurationChange(idx, $event)"
			/>
			<Button
				class="p-button-sm p-button-text"
				@click="addChart"
				label="Add chart"
				icon="pi pi-plus"
			/>
			<tera-optimize-chart
				:risk-results="riskResults[node.state.forecastRunId]"
				:chartConfig="{
					selectedRun: node.state.forecastRunId,
					selectedVariable: node.state.targetVariables
				}"
				:size="{ width: 180, height: 120 }"
				:target-variable="node.state.targetVariables[0]"
				:threshold="node.state.threshold"
			/>
		</div>
		<tera-progress-spinner v-if="showSpinner" :font-size="2" is-centered style="height: 100%" />
		<Button
			v-if="areInputsFilled"
			label="Edit"
			@click="emit('open-drilldown')"
			severity="secondary"
			outlined
		/>
		<tera-operator-placeholder v-else :operation-type="node.operationType">
			Connect a model configuration and dataset
		</tera-operator-placeholder>
	</main>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, watch, ref } from 'vue';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import TeraOptimizeChart from '@/workflow/tera-optimize-chart.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import {
	getRunResult,
	getRunResultCiemss,
	getSimulation,
	pollAction
} from '@/services/models/simulation-service';
import { WorkflowNode } from '@/types/workflow';
import { Poller, PollerState } from '@/api/api';
import { chartActionsProxy } from '@/workflow/util';
import { logger } from '@/utils/logger';
import { ChartConfig, RunResults as SimulationRunResults } from '@/types/SimulateConfig';
import { OptimizeCiemssOperationState } from './optimize-ciemss-operation';

const emit = defineEmits(['open-drilldown', 'update-state', 'append-output']);

const props = defineProps<{
	node: WorkflowNode<OptimizeCiemssOperationState>;
}>();

const isFetchingRunResults = ref(false);

const chartProxy = chartActionsProxy(props.node, (state: OptimizeCiemssOperationState) => {
	emit('update-state', state);
});

const areInputsFilled = computed(() => props.node.inputs[0].value);
const showSpinner = computed(
	() =>
		props.node.state.inProgressOptimizeId !== '' ||
		props.node.state.inProgressForecastId !== '' ||
		isFetchingRunResults.value ||
		!simulationRunResults.value[props.node.state.forecastRunId] ||
		!riskResults.value[props.node.state.forecastRunId]
);

const simulationRunResults = ref<{ [runId: string]: SimulationRunResults }>({});
const riskResults = ref<{ [runId: string]: any }>({});

const poller = new Poller();
const pollOptimizeResult = async (runId: string) => {
	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => pollAction(runId));
	const pollerResults = await poller.start();
	let state = _.cloneDeep(props.node.state);
	state.optimizeErrorMessage = { name: '', value: '', traceback: '' };
	emit('update-state', state);

	if (pollerResults.state === PollerState.Cancelled) {
		return pollerResults;
	}
	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		logger.error(`Optimzation: ${runId} has failed`, {
			toastTitle: 'Error - Pyciemss'
		});
		const simulation = await getSimulation(runId);
		if (simulation?.status && simulation?.statusMessage) {
			state = _.cloneDeep(props.node.state);
			state.optimizeErrorMessage = {
				name: runId,
				value: simulation.status,
				traceback: simulation.statusMessage
			};
			emit('update-state', state);
		}
		throw Error('Failed Runs');
	}
	return pollerResults;
};

const pollForecastResult = async (runId: string) => {
	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => pollAction(runId));
	const pollerResults = await poller.start();
	let state = _.cloneDeep(props.node.state);
	state.simulateErrorMessage = { name: '', value: '', traceback: '' };
	emit('update-state', state);

	if (pollerResults.state === PollerState.Cancelled) {
		return pollerResults;
	}
	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		logger.error(`Simulate: ${runId} has failed`, {
			toastTitle: 'Error - Ciemss'
		});
		const simulation = await getSimulation(runId);
		if (simulation?.status && simulation?.statusMessage) {
			state = _.cloneDeep(props.node.state);
			state.simulateErrorMessage = {
				name: runId,
				value: simulation.status,
				traceback: simulation.statusMessage
			};
			emit('update-state', state);
		}
		throw Error('Failed Runs');
	}
	if (state.chartConfigs.length === 0) {
		chartProxy.addChart();
	}
	return pollerResults;
};

const setOutputValues = async () => {
	if (!props.node.state.forecastRunId) return;
	isFetchingRunResults.value = true;
	const output = await getRunResultCiemss(props.node.state.forecastRunId);
	simulationRunResults.value[props.node.state.forecastRunId] = output.runResults;
	riskResults.value[props.node.state.forecastRunId] = await getRunResult(
		props.node.state.forecastRunId,
		'risk.json'
	);
	isFetchingRunResults.value = false;
};

const addChart = () => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs.push([]);

	emit('update-state', state);
};

const configurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config.selectedVariable;

	emit('update-state', state);
};

function processResult(id: string) {
	console.log(id);
}

async function handlePolling(id: string, pollResult: Function, inProgressId: string) {
	if (!id || id === '') return;

	const response = await pollResult(id);
	if (response.state === PollerState.Done) {
		processResult(id);
	}

	const state = _.cloneDeep(props.node.state);
	state[inProgressId] = '';
	emit('update-state', state);
}

watch(
	() => props.node.state.inProgressForecastId,
	(id) => handlePolling(id, pollForecastResult, 'inProgressForecastId'),
	{ immediate: true }
);

watch(
	() => props.node.state.inProgressOptimizeId,
	(id) => handlePolling(id, pollOptimizeResult, 'inProgressOptimizeId'),
	{ immediate: true }
);

watch(
	() => props.node.active,
	async () => {
		const active = props.node.active;
		if (!active) return;
		setOutputValues();
	},
	{ immediate: true }
);
</script>

<style scoped></style>
