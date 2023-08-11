<template>
	<section v-if="!showSpinner" class="result-container">
		<Button @click="runSimulate">Run</Button>
		<div class="chart-container" v-if="runResults">
			<tera-simulate-chart
				v-for="(cfg, index) of node.state.chartConfigs"
				:key="index"
				:run-results="runResults"
				:chartConfig="cfg"
				@configuration-change="configurationChange(index, $event)"
				:colorByRun="true"
			/>
		</div>
		<Button class="add-chart" text @click="addChart" label="Add Chart" icon="pi pi-plus"></Button>
	</section>
	<section v-else>
		<tera-progress-bar :value="progress.value" :status="progress.status" />
	</section>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, watch, computed, onMounted, onUnmounted } from 'vue';
import Button from 'primevue/button';
import { csvParse } from 'd3';
import { ModelConfiguration } from '@/types/Types';

import {
	makeForecastJob,
	getRunResult,
	simulationPollAction,
	querySimulationInProgress
} from '@/services/models/simulation-service';
import { ProgressState, WorkflowNode } from '@/types/workflow';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';

import { getModelConfigurationById } from '@/services/model-configurations';
import { workflowEventBus } from '@/services/workflow';
import { Poller, PollerState } from '@/api/api';
import TeraSimulateChart from './tera-simulate-chart.vue';
import { SimulateJuliaOperation, SimulateJuliaOperationState } from './simulate-julia-operation';
import TeraProgressBar from './tera-progress-bar.vue';

const props = defineProps<{
	node: WorkflowNode;
}>();
const emit = defineEmits(['append-output-port', 'update-state']);

const showSpinner = ref(false);
const completedRunIdList = ref<string[]>([]);
const runResults = ref<RunResults>({});

const modelConfiguration = ref<ModelConfiguration | null>(null);
const modelConfigId = computed<string | undefined>(() => props.node.inputs[0].value?.[0]);
const progress = ref({ status: ProgressState.QUEUED, value: 0 });

const poller = new Poller();

onMounted(() => {
	const runIds = querySimulationInProgress(props.node);
	if (runIds.length > 0) {
		getStatus(runIds);
	}
});

onUnmounted(() => {
	poller.stop();
});

const runSimulate = async () => {
	const modelConfigurationList = props.node.inputs[0].value;
	if (!modelConfigurationList?.length) return;

	const state = props.node.state as SimulateJuliaOperationState;

	const simulationRequests = modelConfigurationList.map(async (configId: string) => {
		const payload = {
			modelConfigId: configId,
			timespan: {
				start: state.currentTimespan.start,
				end: state.currentTimespan.end
			},
			extra: {},
			engine: 'sciml'
		};
		const response = await makeForecastJob(payload);
		return response.id;
	});

	const response = await Promise.all(simulationRequests);
	getStatus(response);
	showSpinner.value = true;
};

const getStatus = async (runIds: string[]) => {
	showSpinner.value = true;
	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => simulationPollAction(runIds, props.node, progress, emit));
	const pollerResults = await poller.start();

	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		console.error('Failed', runIds);
		showSpinner.value = false;
		throw Error('Failed Runs');
	}
	completedRunIdList.value = runIds;
	showSpinner.value = false;
};

const watchCompletedRunList = async (runIdList: string[]) => {
	if (runIdList.length === 0) return;

	const newRunResults = {};
	await Promise.all(
		runIdList.map(async (runId) => {
			if (runResults.value[runId]) {
				newRunResults[runId] = runResults.value[runId];
			} else {
				const resultCsv = await getRunResult(runId, 'result.csv');
				const csvData = csvParse(resultCsv);
				newRunResults[runId] = csvData;
			}
		})
	);
	runResults.value = newRunResults;

	const port = props.node.inputs[0];
	emit('append-output-port', {
		type: SimulateJuliaOperation.outputs[0].type,
		label: `${port.label} Results`,
		value: runIdList
	});
};

const configurationChange = (index: number, config: ChartConfig) => {
	const state: SimulateJuliaOperationState = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

watch(
	() => modelConfigId.value,
	async () => {
		if (modelConfigId.value) {
			modelConfiguration.value = await getModelConfigurationById(modelConfigId.value);
		}
	},
	{ immediate: true }
);

watch(() => completedRunIdList.value, watchCompletedRunList, { immediate: true });

onMounted(async () => {
	const node = props.node;
	if (!node) return;

	const port = node.outputs[0];
	if (!port) return;

	const runIdList = port.value as string[];
	await Promise.all(
		runIdList.map(async (runId) => {
			const resultCsv = await getRunResult(runId, 'result.csv');
			const csvData = csvParse(resultCsv);

			const configId = props.node.inputs[0].value?.[0];
			if (configId) {
				const modelConfig = await getModelConfigurationById(configId);
				const parameters = modelConfig.configuration.semantics.ode.parameters;
				csvData.forEach((row) =>
					parameters.forEach((parameter) => {
						row[parameter.id] = parameter.value;
					})
				);
			}
			runResults.value[runId] = csvData as any;
		})
	);
});

const addChart = () => {
	const state: SimulateJuliaOperationState = _.cloneDeep(props.node.state);
	state.chartConfigs.push(_.last(state.chartConfigs) as ChartConfig);

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	width: 100%;
	padding: 10px;
	background: var(--surface-overlay);
}

.simulate-chart {
	margin: 1em 0em;
}

.add-chart {
	width: 5rem;
}
</style>
