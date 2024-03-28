<template>
	<main>
		<template v-if="simulationRunResults[node.state.forecastRunId]">
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
		</template>
		<tera-optimize-chart
			v-if="riskResults[node.state.forecastRunId]"
			:risk-results="riskResults[node.state.forecastRunId]"
			:chartConfig="{
				selectedRun: node.state.forecastRunId,
				selectedVariable: node.state.targetVariables
			}"
			:size="{ width: 180, height: 120 }"
			:target-variable="node.state.targetVariables[0]"
			:threshold="node.state.threshold"
		/>
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
	pollAction,
	makeForecastJobCiemss
} from '@/services/models/simulation-service';
import { WorkflowNode } from '@/types/workflow';
import { Poller, PollerState } from '@/api/api';
// import { chartActionsProxy } from '@/workflow/util';
import { logger } from '@/utils/logger';
import { ChartConfig, RunResults as SimulationRunResults } from '@/types/SimulateConfig';
import { OptimizeCiemssOperationState } from './optimize-ciemss-operation';

const emit = defineEmits(['open-drilldown', 'update-state', 'append-output']);

const props = defineProps<{
	node: WorkflowNode<OptimizeCiemssOperationState>;
}>();

const isFetchingRunResults = ref(false);

// const chartProxy = chartActionsProxy(props.node, (state: OptimizeCiemssOperationState) => {
// 	emit('update-state', state);
// });

const areInputsFilled = computed(() => props.node.inputs[0].value);
const showSpinner = computed(
	() =>
		isFetchingRunResults.value &&
		(props.node.state.inProgressOptimizeId !== '' ||
			props.node.state.inProgressForecastId !== '' ||
			!simulationRunResults.value[props.node.state.forecastRunId] ||
			!riskResults.value[props.node.state.forecastRunId])
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

// Had to comment this since it's not used yet
// const pollForecastResult = async (runId: string) => {
// 	poller
// 		.setInterval(3000)
// 		.setThreshold(300)
// 		.setPollAction(async () => pollAction(runId));
// 	const pollerResults = await poller.start();
// 	let state = _.cloneDeep(props.node.state);
// 	state.simulateErrorMessage = { name: '', value: '', traceback: '' };
// 	emit('update-state', state);

// 	if (pollerResults.state === PollerState.Cancelled) {
// 		return pollerResults;
// 	}
// 	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
// 		logger.error(`Simulate: ${runId} has failed`, {
// 			toastTitle: 'Error - Ciemss'
// 		});
// 		const simulation = await getSimulation(runId);
// 		if (simulation?.status && simulation?.statusMessage) {
// 			state = _.cloneDeep(props.node.state);
// 			state.simulateErrorMessage = {
// 				name: runId,
// 				value: simulation.status,
// 				traceback: simulation.statusMessage
// 			};
// 			emit('update-state', state);
// 		}
// 		throw Error('Failed Runs');
// 	}
// 	if (state.chartConfigs.length === 0) {
// 		chartProxy.addChart();
// 	}
// 	return pollerResults;
// };

const setOutputValues = async () => {
	if (!props.node.state.forecastRunId) return;

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

	isFetchingRunResults.value = true;
	const response = await pollResult(id);
	if (response.state === PollerState.Done) {
		processResult(id);
	}

	const state = _.cloneDeep(props.node.state);
	state[inProgressId] = '';
	emit('update-state', state);
}

// watch(
// 	() => props.node.state.inProgressForecastId,
// 	(id) => handlePolling(id, pollForecastResult, 'inProgressForecastId'),
// 	{ immediate: true }
// );

watch(
	() => props.node.state.inProgressOptimizeId,
	async (id) => {
		const modelConfigurationId = props.node.inputs[0].value?.[0];
		if (!modelConfigurationId) return;

		await handlePolling(id, pollOptimizeResult, 'inProgressOptimizeId');

		// ATTEMPT to move a chunk of runOptimize here, the last line here is where I am bit stuck

		const paramNames: string[] = [];
		const startTime: number[] = [];

		props.node.state.interventionPolicyGroups.forEach((ele) => {
			paramNames.push(ele.parameter);
			startTime.push(ele.startTime);
		});

		const optimizeInterventions: OptimizedIntervention = {
			selection: 'param_value',
			paramNames,
			startTime
		};

		const policyResult = await getRunResult(optResult.simulationId, 'policy.json');
		const simulationInterventions: SimulationIntervention[] = [];

		// This is all index matching for optimizeInterventions.paramNames, optimizeInterventions.startTimes, and policyResult
		for (let i = 0; i < optimizeInterventions.paramNames.length; i++) {
			if (policyResult?.at(i) && optimizeInterventions.startTime?.[i]) {
				simulationInterventions.push({
					name: optimizeInterventions.paramNames[i],
					timestep: optimizeInterventions.startTime[i],
					value: policyResult[i]
				});
			}
		}

		const simulationPayload: SimulationRequest = {
			projectId: '',
			modelConfigId: modelConfigurationId,
			timespan: {
				start: 0,
				end: props.state.endTime
			},
			interventions: simulationInterventions,
			extra: {
				num_samples: props.state.numSamples,
				method: props.state.solverMethod
			},
			engine: 'ciemss'
		};

		const simulationResponse = await makeForecastJobCiemss(simulationPayload);
		console.log(simulationResponse.id);
		// knobs.value.forecastRunId = simulationResponse.id;
	},
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
