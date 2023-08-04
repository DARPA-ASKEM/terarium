<template>
	<div v-if="!disableRunButton">
		<Button
			size="small"
			label="Run"
			@click="runEnsemble"
			:disabled="disableRunButton"
			icon="pi pi-play"
		></Button>
	</div>
	<section v-if="!showSpinner" class="result-container">
		<section v-if="simulationIds">
			<tera-simulate-chart
				v-for="(cfg, index) of node.state.chartConfigs"
				:key="index"
				:run-results="runResults"
				:chartConfig="cfg"
				has-mean-line
				@configuration-change="chartConfigurationChange(index, $event)"
			/>
			<Button
				class="add-chart"
				text
				:outlined="true"
				@click="addChart"
				label="Add Chart"
				icon="pi pi-plus"
			/>
		</section>
		<section v-else class="result-container">
			<div class="invalid-block" v-if="node.statusCode === WorkflowStatus.INVALID">
				<img class="image" src="@assets/svg/plants.svg" alt="" />
				<p class="helpMessage">Configure in side panel</p>
			</div>
		</section>
	</section>
	<section v-else>
		<tera-progress-bar :value="progress.value" :status="progress.status"></tera-progress-bar>
	</section>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, watch, computed, ComputedRef, onMounted } from 'vue';
// import { csvParse } from 'd3';
// import { ModelConfiguration } from '@/types/Types';
// import { getRunResult } from '@/services/models/simulation-service';
import {
	ProgressState,
	SimulationStateOperation,
	WorkflowNode,
	WorkflowStatus
} from '@/types/workflow';
// import { getModelConfigurationById } from '@/services/model-configurations';
import { workflowEventBus } from '@/services/workflow';
import { EnsembleSimulationCiemssRequest, TimeSpan, EnsembleModelConfigs } from '@/types/Types';
import {
	getSimulation,
	makeEnsembleCiemssSimulation,
	getRunResultCiemss,
	handleSimulationsInProgress
} from '@/services/models/simulation-service';
import Button from 'primevue/button';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { Poller, PollerState } from '@/api/api';
import {
	SimulateEnsembleCiemssOperationState,
	SimulateEnsembleCiemssOperation
} from './simulate-ensemble-ciemss-operation';
import TeraSimulateChart from './tera-simulate-chart.vue';
import TeraProgressBar from '../widgets/tera-progress-bar.vue';

const props = defineProps<{
	node: WorkflowNode;
}>();
const emit = defineEmits(['append-output-port', 'update-state']);

const showSpinner = ref(false);
const modelConfigIds = computed<string[]>(() => props.node.inputs[0].value as string[]);
const completedRunId = ref<string>();
const disableRunButton = computed(() => !ensembleConfigs?.value[0]?.weight);
const ensembleConfigs = computed<EnsembleModelConfigs[]>(() => props.node.state.mapping);
const timeSpan = computed<TimeSpan>(() => props.node.state.timeSpan);
const numSamples = ref<number>(props.node.state.numSamples);
const runResults = ref<RunResults>({});
const simulationIds: ComputedRef<any | undefined> = computed(
	<any | undefined>(() => props.node.outputs[0]?.value)
);
const progress = ref({ status: ProgressState.QUEUED, value: 0 });

onMounted(() => {
	const runIds = handleSimulationsInProgress(SimulationStateOperation.QUERY, props.node);
	if (runIds.length > 0) {
		getStatus(runIds[0]);
	}
});

const runEnsemble = async () => {
	const params: EnsembleSimulationCiemssRequest = {
		modelConfigs: ensembleConfigs.value,
		timespan: timeSpan.value,
		engine: 'ciemss',
		extra: { num_samples: numSamples.value }
	};
	const response = await makeEnsembleCiemssSimulation(params);
	if (response.simulationId) {
		getStatus(response.simulationId);
	}
};

// Tom TODO: Make this generic, its copy paste from drilldown
const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state: SimulateEnsembleCiemssOperationState = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

// TODO: This is repeated every single node that uses a chart. Hope to refactor if the state manip allows for it easily
const addChart = () => {
	const state: SimulateEnsembleCiemssOperationState = _.cloneDeep(props.node.state);
	state.chartConfigs.push({ selectedRun: '', selectedVariable: [] } as ChartConfig);

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const getStatus = async (simulationId: string) => {
	showSpinner.value = true;
	if (!simulationId) return;

	const poller = new Poller<object>()
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => {
			const response = await getSimulation(simulationId);
			if (response?.status === ProgressState.COMPLETE) {
				const newState = handleSimulationsInProgress(
					SimulationStateOperation.DELETE,
					props.node,
					simulationId
				);
				if (newState) {
					emit('update-state', newState);
				}
				return {
					data: response,
					progress: null,
					error: null
				};
			}
			if (response?.status === ProgressState.RUNNING) {
				const newState = handleSimulationsInProgress(
					SimulationStateOperation.ADD,
					props.node,
					simulationId
				);
				if (newState) {
					emit('update-state', newState);
				}
				progress.value = {
					status: ProgressState.RUNNING,
					value: progress.value.value + 5
				};
			}

			if (response?.status === ProgressState.QUEUED) {
				const newState = handleSimulationsInProgress(
					SimulationStateOperation.ADD,
					props.node,
					simulationId
				);
				if (newState) {
					emit('update-state', newState);
				}
				progress.value = {
					status: ProgressState.QUEUED,
					value: 0
				};
			}

			return {
				data: null,
				progress: null,
				error: null
			};
		});
	const pollerResults = await poller.start();

	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		console.error('Failed', simulationId);
		showSpinner.value = false;
		throw Error('Failed Runs');
	}
	completedRunId.value = simulationId;
	updateOutputPorts(completedRunId);
	addChart();
	showSpinner.value = false;
};

const updateOutputPorts = async (runId) => {
	const portLabel = props.node.inputs[0].label;
	emit('append-output-port', {
		type: SimulateEnsembleCiemssOperation.outputs[0].type,
		label: `${portLabel} Result`,
		value: { runId }
	});
};

watch(
	() => modelConfigIds.value,
	async () => {
		if (modelConfigIds.value) {
			const mapping: EnsembleModelConfigs[] = [];
			// Init ensemble Configs:
			for (let i = 0; i < modelConfigIds.value.length; i++) {
				mapping[i] = {
					id: modelConfigIds.value[i],
					solutionMappings: {},
					weight: 0
				};
			}

			const state: SimulateEnsembleCiemssOperationState = _.cloneDeep(props.node.state);
			state.modelConfigIds = modelConfigIds.value;
			state.mapping = mapping;
			workflowEventBus.emitNodeStateChange({
				workflowId: props.node.workflowId,
				nodeId: props.node.id,
				state
			});
		}
	},
	{ immediate: true }
);

watch(
	() => simulationIds.value,
	async () => {
		if (!simulationIds.value) return;

		const output = await getRunResultCiemss(simulationIds.value[0].runId, 'simulation.csv');
		runResults.value = output.runResults;
	},
	{ immediate: true }
);
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	width: 100%;
	padding: 10px;
	background: var(--surface-overlay);
}

.helpMessage {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
}
.result-container {
	align-items: center;
}

.image {
	height: 8.75rem;
	margin-bottom: 0.5rem;
	background-color: var(--surface-ground);
	border-radius: 1rem;
	background-color: rgb(0, 0, 0, 0);
}

.invalid-block {
	display: contents;
}

.simulate-chart {
	margin: 1em 0em;
}

.add-chart {
	width: 9em;
}
</style>
