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
				:initial-data="csvAsset"
				:chartConfig="cfg"
				has-mean-line
				@configuration-change="chartConfigurationChange(index, $event)"
			/>
			<Button
				class="add-chart"
				text
				:outlined="true"
				@click="addChart"
				label="Add chart"
				icon="pi pi-plus"
			/>
		</section>
		<section v-else class="result-container">
			<div class="invalid-block" v-if="node.status === OperatorStatus.INVALID">
				<img class="image" src="@assets/svg/plants.svg" alt="" />
				<p class="helpMessage">Configure in side panel</p>
			</div>
		</section>
	</section>
	<section v-else>
		<tera-progress-bar :value="progress.value" :status="progress.status" />
	</section>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, shallowRef, watch, computed, ComputedRef, onMounted, onUnmounted } from 'vue';
// import { csvParse } from 'd3';
// import { ModelConfiguration } from '@/types/Types';
// import { getRunResult } from '@/services/models/simulation-service';
import { ProgressState, WorkflowNode, OperatorStatus } from '@/types/workflow';
// import { getModelConfigurationById } from '@/services/model-configurations';
import { workflowEventBus } from '@/services/workflow';
import { CsvAsset, EnsembleCalibrationCiemssRequest, EnsembleModelConfigs } from '@/types/Types';
import {
	makeEnsembleCiemssCalibration,
	getRunResultCiemss,
	simulationPollAction,
	querySimulationInProgress
} from '@/services/models/simulation-service';
import Button from 'primevue/button';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { setupDatasetInput } from '@/services/calibrate-workflow';
import { Poller, PollerState } from '@/api/api';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import TeraProgressBar from '@/workflow/tera-progress-bar.vue';
import { getTimespan } from '@/workflow/util';
import { logger } from '@/utils/logger';
import {
	CalibrateEnsembleCiemssOperationState,
	CalibrateEnsembleCiemssOperation,
	EnsembleCalibrateExtraCiemss
} from './calibrate-ensemble-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrateEnsembleCiemssOperationState>;
}>();
const emit = defineEmits(['append-output-port', 'update-state']);

const showSpinner = ref(false);
const modelConfigIds = computed<string[]>(() => props.node.inputs[0].value as string[]);
const datasetId = computed(() => props.node.inputs[1].value?.[0] as string | undefined);
const currentDatasetFileName = ref<string>();

const completedRunId = ref<string>();
const disableRunButton = computed(
	() => !ensembleConfigs?.value[0]?.weight || !datasetId.value || !currentDatasetFileName.value
);
const ensembleConfigs = computed<EnsembleModelConfigs[]>(() => props.node.state.mapping);
const extra = ref<EnsembleCalibrateExtraCiemss>(props.node.state.extra);

const runResults = ref<RunResults>({});
const simulationIds: ComputedRef<any | undefined> = computed(
	<any | undefined>(() => props.node.outputs[0]?.value)
);
const datasetColumnNames = ref<string[]>();
const progress = ref({ status: ProgressState.RETRIEVING, value: 0 });

const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

const poller = new Poller();

onMounted(() => {
	const runIds = querySimulationInProgress(props.node);
	if (runIds.length > 0) {
		getStatus(runIds[0]);
	}
});

onUnmounted(() => {
	poller.stop();
});

const runEnsemble = async () => {
	if (!datasetId.value || !currentDatasetFileName.value) return;

	const params: EnsembleCalibrationCiemssRequest = {
		modelConfigs: ensembleConfigs.value,
		timespan: getTimespan(csvAsset.value),
		dataset: {
			id: datasetId.value,
			filename: currentDatasetFileName.value
		},
		engine: 'ciemss',
		extra: {
			num_samples: extra.value.numSamples,
			num_iterations: extra.value.numIterations,
			total_population: extra.value.totalPopulation
		}
	};
	const response = await makeEnsembleCiemssCalibration(params);
	if (response?.simulationId) {
		getStatus(response.simulationId);
	}
};

const getStatus = async (simulationId: string) => {
	showSpinner.value = true;
	if (!simulationId) return;

	const runIds = [simulationId];
	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => simulationPollAction(runIds, props.node, progress, emit));
	const pollerResults = await poller.start();

	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		showSpinner.value = false;
		logger.error(`Calibrate Ensemble: ${simulationId} has failed`, {
			toastTitle: 'Error - Pyciemss'
		});
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
		type: CalibrateEnsembleCiemssOperation.outputs[0].type,
		label: `${portLabel} Result`,
		value: { runId }
	});
};

// Tom TODO: Make this generic, its copy paste from drilldown
const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

// TODO: This is repeated every single node that uses a chart. Hope to refactor if the state manip allows for it easily
const addChart = () => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs.push({ selectedRun: '', selectedVariable: [] } as ChartConfig);

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

// Set up csv + dropdown names
// Note: Same as calibrate side panel
watch(
	() => datasetId.value,
	async () => {
		const { filename, csv } = await setupDatasetInput(datasetId.value);
		currentDatasetFileName.value = filename;
		csvAsset.value = csv;
		datasetColumnNames.value = csv?.headers;
	},
	{ immediate: true }
);

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

			const state = _.cloneDeep(props.node.state);
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

		const output = await getRunResultCiemss(simulationIds.value[0].runId, 'result.csv');
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
