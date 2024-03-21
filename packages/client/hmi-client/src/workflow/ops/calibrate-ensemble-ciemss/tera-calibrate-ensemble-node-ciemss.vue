<template>
	<section>
		<template v-if="!showSpinner">
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
			<template v-if="modelConfigIds && datasetId">
				<div class="flex gap-2">
					<Button
						label="Edit"
						@click="emit('open-drilldown')"
						severity="secondary"
						outlined
						class="w-full"
					/>
					<Button
						label="Run"
						@click="runEnsemble"
						severity="secondary"
						outlined
						:disabled="disableRunButton"
						icon="pi pi-play"
						class="w-full"
					/>
				</div>
			</template>
			<tera-operator-placeholder
				v-else-if="node.status === OperatorStatus.INVALID"
				:operation-type="node.operationType"
			>
				Connect a model configuration and dataset
			</tera-operator-placeholder>
		</template>
		<template v-else>
			<tera-progress-bar :value="progress.value" :status="progress.status" />
		</template>
	</section>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ComputedRef, onMounted, onUnmounted, ref, shallowRef, watch } from 'vue';
import { OperatorStatus, WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import { ProgressState } from '@/types/Types';
// import { csvParse } from 'd3';
// import type { ModelConfiguration } from '@/types/Types';
// import { getRunResult } from '@/services/models/simulation-service';
// import { getModelConfigurationById } from '@/services/model-configurations';
import type {
	CsvAsset,
	EnsembleCalibrationCiemssRequest,
	EnsembleModelConfigs
} from '@/types/Types';
import {
	getRunResultCiemss,
	makeEnsembleCiemssCalibration,
	querySimulationInProgress,
	simulationPollAction
} from '@/services/models/simulation-service';
import Button from 'primevue/button';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { setupDatasetInput } from '@/services/calibrate-workflow';
import { Poller, PollerState } from '@/api/api';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import TeraProgressBar from '@/workflow/tera-progress-bar.vue';
import { getTimespan } from '@/workflow/util';
import { logger } from '@/utils/logger';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import {
	CalibrateEnsembleCiemssOperation,
	CalibrateEnsembleCiemssOperationState,
	EnsembleCalibrateExtraCiemss
} from './calibrate-ensemble-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrateEnsembleCiemssOperationState>;
}>();
const emit = defineEmits(['append-output', 'update-state', 'open-drilldown', 'append-input-port']);

const showSpinner = ref(false);
const modelConfigIds = computed(() => getModelConfigIds);

const getModelConfigIds = () => {
	const aList: string[] = [];
	props.node.inputs.forEach((ele) => {
		if (ele.value && ele.type === 'modelConfigId') {
			aList.push(ele.value[0]);
		}
	});
	return aList;
};
const datasetId = computed(() => props.node.inputs[0].value?.[0] as string | undefined);
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
const progress = ref({ status: ProgressState.Retrieving, value: 0 });

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
			num_particles: extra.value.numParticles,
			num_iterations: extra.value.numIterations,
			solver_method: extra.value.solverMethod
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
	const portLabel = props.node.inputs[1].label;
	emit('append-output', {
		type: CalibrateEnsembleCiemssOperation.outputs[0].type,
		label: `${portLabel} Result`,
		value: { runId }
	});
};

const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	emit('update-state', state);
};

const addChart = () => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs.push({ selectedRun: '', selectedVariable: [] } as ChartConfig);

	emit('update-state', state);
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
	() => simulationIds.value,
	async () => {
		if (!simulationIds.value) return;

		const output = await getRunResultCiemss(simulationIds.value[0].runId, 'result.csv');
		runResults.value = output.runResults;
	},
	{ immediate: true }
);

watch(
	() => props.node.inputs,
	() => {
		if (
			props.node.inputs.every(
				(input) => input.status === WorkflowPortStatus.CONNECTED || input.type !== 'modelConfigId'
			)
		) {
			emit('append-input-port', { type: 'modelConfigId', label: 'Model configuration' });
		}
	},
	{ deep: true }
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
