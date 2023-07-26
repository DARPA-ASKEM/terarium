<template>
	<div>
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
				<p>Configure in side panel</p>
			</div>
		</section>
	</section>
	<section v-else>
		<div>loading...</div>
	</section>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, watch, computed, ComputedRef } from 'vue';
// import { csvParse } from 'd3';
// import { ModelConfiguration } from '@/types/Types';
// import { getRunResult } from '@/services/models/simulation-service';
import { WorkflowNode, WorkflowStatus } from '@/types/workflow';
// import { getModelConfigurationById } from '@/services/model-configurations';
import { workflowEventBus } from '@/services/workflow';
import {
	EnsembleCalibrationCiemssRequest,
	Simulation,
	TimeSpan,
	EnsembleModelConfigs
} from '@/types/Types';
import {
	getSimulation,
	makeEnsembleCiemssCalibration,
	getRunResultCiemss
} from '@/services/models/simulation-service';
import Button from 'primevue/button';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { setupDatasetInput } from '@/services/calibrate-workflow';
import {
	CalibrateEnsembleCiemssOperationState,
	CalibrateEnsembleCiemssOperation,
	EnsembleCalibrateExtraCiemss
} from './calibrate-ensemble-ciemss-operation';
import TeraSimulateChart from './tera-simulate-chart.vue';

const props = defineProps<{
	node: WorkflowNode;
}>();
const emit = defineEmits(['append-output-port']);

const showSpinner = ref(false);
const modelConfigIds = computed<string[]>(() => props.node.inputs[0].value as string[]);
const datasetId = computed(() => props.node.inputs[1].value?.[0] as string | undefined);
const currentDatasetFileName = ref<string>();

const startedRunId = ref<string>();
const completedRunId = ref<string>();
const disableRunButton = computed(
	() => !ensembleConfigs?.value[0]?.weight || !datasetId.value || !currentDatasetFileName.value
);
const ensembleConfigs = computed<EnsembleModelConfigs[]>(() => props.node.state.mapping);
const timeSpan = computed<TimeSpan>(() => props.node.state.timeSpan);
const extra = ref<EnsembleCalibrateExtraCiemss>(props.node.state.extra);

const runResults = ref<RunResults>({});
const simulationIds: ComputedRef<any | undefined> = computed(
	<any | undefined>(() => props.node.outputs[0]?.value)
);
const datasetColumnNames = ref<string[]>();

const runEnsemble = async () => {
	if (!datasetId.value || !currentDatasetFileName.value) return;

	const params: EnsembleCalibrationCiemssRequest = {
		modelConfigs: ensembleConfigs.value,
		timespan: timeSpan.value,
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
	startedRunId.value = response.simulationId;

	showSpinner.value = true;
	getStatus();
};

const getStatus = async () => {
	if (!startedRunId.value) return;

	const currentSimulation: Simulation | null = await getSimulation(startedRunId.value); // get TDS's simulation object
	const ongoingStatusList = ['running', 'queued'];

	if (currentSimulation && currentSimulation.status === 'complete') {
		completedRunId.value = startedRunId.value;
		updateOutputPorts(completedRunId);
		addChart();
		showSpinner.value = false;
	} else if (currentSimulation && ongoingStatusList.includes(currentSimulation.status)) {
		// recursively call until all runs retrieved
		setTimeout(getStatus, 3000);
	} else {
		// throw if there are any failed runs for now
		console.error('Failed', startedRunId.value);
		throw Error('Failed Runs');
	}
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
	const state: CalibrateEnsembleCiemssOperationState = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

// TODO: This is repeated every single node that uses a chart. Hope to refactor if the state manip allows for it easily
const addChart = () => {
	const state: CalibrateEnsembleCiemssOperationState = _.cloneDeep(props.node.state);
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

			const state: CalibrateEnsembleCiemssOperationState = _.cloneDeep(props.node.state);
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
