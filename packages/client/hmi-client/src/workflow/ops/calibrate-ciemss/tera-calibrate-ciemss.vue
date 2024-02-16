<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section :tabName="CalibrateTabs.Wizard">
			<tera-drilldown-section>
				<div class="form-section">
					<h4>Mapping</h4>
					<DataTable class="mapping-table" :value="mapping">
						<Button
							class="p-button-sm p-button-text"
							label="Delete All Mapping"
							@click="deleteMapping"
						/>
						<Column field="modelVariable">
							<template #header>
								<span class="column-header">Model variable</span>
							</template>
							<template #body="{ data, field }">
								<Dropdown
									class="w-full"
									placeholder="Select a variable"
									v-model="data[field]"
									:options="modelStateOptions?.map((ele) => ele.id)"
								/>
							</template>
						</Column>
						<Column field="datasetVariable">
							<template #header>
								<span class="column-header">Dataset variable</span>
							</template>
							<template #body="{ data, field }">
								<Dropdown
									class="w-full"
									placeholder="Select a variable"
									v-model="data[field]"
									:options="datasetColumns?.map((ele) => ele.name)"
								/>
							</template>
						</Column>
						<Column field="deleteRow">
							<template #header>
								<span class="column-header"></span>
							</template>
							<template #body="{ index }">
								<Button
									class="p-button-sm p-button-text"
									label="Delete"
									@click="deleteMapRow(index)"
								/>
							</template>
						</Column>
					</DataTable>
					<div>
						<Button
							class="p-button-sm p-button-text"
							icon="pi pi-plus"
							label="Add mapping"
							@click="addMapping"
						/>
						<Button
							class="p-button-sm p-button-text"
							icon="pi pi-plus"
							label="Auto map"
							@click="getAutoMapping"
						/>
					</div>
				</div>
				<!--
				<div class="form-section">
					<h4>Calibration settings</h4>
					<div class="input-row">
						<div class="label-and-input">
							<label for="num-samples">Number of samples</label>
							<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="numSamples" />
						</div>
						<div class="label-and-input">
							<label for="num-iterations">Number of solver iterations</label>
							<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="numIterations" />
						</div>
						<div class="label-and-input">
							<label for="method">Solver method</label>
							<Dropdown
								class="p-inputtext-sm"
								:options="ciemssMethodOptions"
								v-model="method"
								placeholder="Select"
							/>
						</div>
					</div>
				</div>
				-->
			</tera-drilldown-section>
		</section>
		<section :tabName="CalibrateTabs.Notebook">
			<h4>Notebook</h4>
		</section>
		<template #preview>
			<tera-drilldown-preview
				title="Preview"
				:options="outputs"
				v-model:output="selectedOutputId"
				@update:output="onUpdateOutput"
				@update:selection="onUpdateSelection"
				is-selectable
			>
				<h4>Loss</h4>
				<div ref="drilldownLossPlot"></div>
				<div v-if="!showSpinner" class="form-section">
					<h4>Variables</h4>
					<section v-if="modelConfig && node.state.chartConfigs.length && csvAsset">
						<tera-simulate-chart
							v-for="(cfg, index) of node.state.chartConfigs"
							:key="cfg.selectedRun"
							:run-results="runResults"
							:chartConfig="cfg"
							:initial-data="csvAsset"
							:mapping="mapping"
							has-mean-line
							@configuration-change="chartConfigurationChange(index, $event)"
							:size="{ width: previewChartWidth, height: 140 }"
						/>
						<Button
							class="add-chart"
							text
							:outlined="true"
							@click="addChart"
							label="Add chart"
							icon="pi pi-plus"
						></Button>
					</section>
					<section v-else-if="!modelConfig" class="emptyState">
						<img src="@assets/svg/seed.svg" alt="" draggable="false" />
						<p class="helpMessage">Connect a model configuration and dataset</p>
					</section>
				</div>
				<section v-else>
					<tera-progress-bar :value="progress.value" :status="progress.status" />
				</section>
			</tera-drilldown-preview>
		</template>
		<template #footer>
			<Button
				outlined
				:style="{ marginRight: 'auto' }"
				label="Run"
				icon="pi pi-play"
				@click="runCalibrate"
				:disabled="disableRunButton"
			/>
			<Button label="Close" @click="emit('close')" />
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, onMounted, onUnmounted, ref, shallowRef, watch } from 'vue';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Dropdown from 'primevue/dropdown';
import Column from 'primevue/column';
import {
	getRunResultCiemss,
	makeCalibrateJobCiemss,
	simulationPollAction,
	querySimulationInProgress,
	getCalibrateBlobURL,
	makeForecastJobCiemss,
	subscribeToUpdateMessages,
	unsubscribeToUpdateMessages
} from '@/services/models/simulation-service';
import {
	CalibrationRequestCiemss,
	ClientEvent,
	ClientEventType,
	CsvAsset,
	DatasetColumn,
	ModelConfiguration,
	ProgressState,
	State
} from '@/types/Types';
// import InputNumber from 'primevue/inputnumber';
import {
	CalibrateMap,
	autoCalibrationMapping,
	renderLossGraph,
	setupDatasetInput,
	setupModelInput
} from '@/services/calibrate-workflow';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { WorkflowNode } from '@/types/workflow';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import TeraProgressBar from '@/workflow/tera-progress-bar.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import { Poller, PollerState } from '@/api/api';
import { getTimespan } from '@/workflow/util';
import { logger } from '@/utils/logger';
import { useToastService } from '@/services/toast';
import { CalibrationOperationStateCiemss } from './calibrate-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateCiemss>;
}>();
const emit = defineEmits([
	'append-output',
	'close',
	'select-output',
	'update-output-port',
	'update-state'
]);
const toast = useToastService();

enum CalibrateTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

// Model variables checked in the model configuration will be options in the mapping dropdown
const modelStateOptions = ref<State[] | undefined>();

const datasetColumns = ref<DatasetColumn[]>();
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

const modelConfig = ref<ModelConfiguration>();

const modelConfigId = computed<string | undefined>(() => props.node.inputs[0]?.value?.[0]);
const datasetId = computed<string | undefined>(() => props.node.inputs[1]?.value?.[0]);
const currentDatasetFileName = ref<string>();

const drilldownLossPlot = ref<HTMLElement>();
const runResults = ref<RunResults>({});

const previewChartWidth = ref(120);

const showSpinner = ref(false);
const progress = ref({ status: ProgressState.Retrieving, value: 0 });
let lossValues: { [key: string]: number }[] = [];

const mapping = ref<CalibrateMap[]>(props.node.state.mapping);

// EXTRA section: Unused, comment out for now Feb 2023
/*
const numSamples = ref(100);
const numIterations = ref(100);
const method = ref('dopri5');
const ciemssMethodOptions = ref(['dopri5', 'euler']);
*/

const poller = new Poller();

const disableRunButton = computed(
	() =>
		!currentDatasetFileName.value ||
		!modelConfig.value ||
		!csvAsset.value ||
		!modelConfigId.value ||
		!datasetId.value
);

const selectedOutputId = ref<string>();
const outputs = computed(() => {
	if (!_.isEmpty(props.node.outputs)) {
		return [
			{
				label: 'Select outputs to display in operator',
				items: props.node.outputs
			}
		];
	}
	return [];
});

const runCalibrate = async () => {
	if (
		!modelConfigId.value ||
		!datasetId.value ||
		!currentDatasetFileName.value ||
		!modelConfig.value
	)
		return;

	const formattedMap: { [index: string]: string } = {};
	// If the user has done any mapping populate formattedMap
	if (mapping.value[0].datasetVariable !== '') {
		mapping.value.forEach((ele) => {
			formattedMap[ele.datasetVariable] = ele.modelVariable;
		});
	}

	const calibrationRequest: CalibrationRequestCiemss = {
		modelConfigId: modelConfigId.value,
		dataset: {
			id: datasetId.value,
			filename: currentDatasetFileName.value,
			mappings: formattedMap
		},
		extra: {
			num_samples: 100,
			num_iterations: 200
			/*
			num_samples: numSamples.value,
			num_iterations: numIterations.value,
			method: method.value
			*/
		},
		timespan: getTimespan(csvAsset.value, mapping.value),
		engine: 'ciemss'
	};
	const response = await makeCalibrateJobCiemss(calibrationRequest);

	if (response?.simulationId) {
		getCalibrateStatus(response.simulationId);
	}
};

const getMessageHandler = (event: ClientEvent<any>) => {
	console.log('msg', event.data);
	lossValues.push({ iter: lossValues.length, loss: event.data.loss });

	if (drilldownLossPlot.value) {
		renderLossGraph(drilldownLossPlot.value, lossValues, {
			width: previewChartWidth.value,
			height: 120
		});
	}
};

/**
 * This is a two step process
 * - Polling loop for calibration to finish, plot the loss values
 * - Polling loop for a sampmle simulation, plot the result against input dataset
 * */
const getCalibrateStatus = async (simulationId: string) => {
	showSpinner.value = true;
	if (!simulationId) {
		console.log('No sim id');
		return;
	}
	const runIds = [simulationId];
	lossValues = [];

	// open a connection for each run id and handle the messages
	await subscribeToUpdateMessages(
		[simulationId],
		ClientEventType.SimulationPyciemss,
		getMessageHandler
	);

	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => simulationPollAction(runIds, props.node, progress, emit));

	const pollerResults = await poller.start();

	// closing event source connections
	await unsubscribeToUpdateMessages(
		[simulationId],
		ClientEventType.SimulationPyciemss,
		getMessageHandler
	);

	if (pollerResults.state === PollerState.Cancelled) {
		return;
	}
	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		showSpinner.value = false;
		logger.error(`Calibrate: ${simulationId} has failed`, {
			toastTitle: 'Error - Pyciemss'
		});
		throw Error('Failed Runs');
	}

	// Start 2nd simulation to get sample simulation from dill
	const dillURL = await getCalibrateBlobURL(simulationId);
	console.log('dill URL is', dillURL);

	const resp = await makeForecastJobCiemss({
		projectId: '',
		modelConfigId: modelConfigId.value as string,
		timespan: {
			start: 0,
			end: 100
			// start: state.currentTimespan.start,
			// end: state.currentTimespan.end
		},
		extra: {
			num_samples: 5,
			method: 'dopri5',
			inferred_parameters: simulationId
		},
		engine: 'ciemss'
	});

	const sampleSimulateId = resp.id;

	poller.stop();
	poller
		.setInterval(3000)
		.setThreshold(500)
		.setPollAction(async () =>
			simulationPollAction([sampleSimulateId], props.node, progress, emit)
		);
	const sampleSimulateResults = await poller.start();

	if (sampleSimulateResults.state === PollerState.Cancelled) {
		showSpinner.value = false;
		return;
	}
	if (sampleSimulateResults.state !== PollerState.Done || !sampleSimulateResults.data) {
		// throw if there are any failed runs for now
		showSpinner.value = false;
		logger.error(`Simulation: ${sampleSimulateId} has failed`, {
			toastTitle: 'Error - Pyciemss'
		});
		throw Error('Failed Runs');
	}

	const output = await getRunResultCiemss(sampleSimulateId, 'result.csv');
	runResults.value = output.runResults;

	updateOutputPorts(simulationId, sampleSimulateId);
	showSpinner.value = false;
};

const updateOutputPorts = async (calibrationId: string, simulationId: string) => {
	const portLabel = props.node.inputs[0].label;
	const state = _.cloneDeep(props.node.state);

	state.chartConfigs = [
		{
			selectedRun: simulationId,
			selectedVariable: []
		}
	];

	state.calibrationId = calibrationId;
	state.simulationId = simulationId;

	emit('update-state', state);

	emit('append-output', {
		type: 'calibrateSimulationId',
		label: `${portLabel} Result`,
		value: [calibrationId]
	});
};

const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	emit('update-state', state);
};

const addChart = () => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs.push(_.last(state.chartConfigs) as ChartConfig);

	emit('update-state', state);
};

const onUpdateOutput = (id: string) => {
	emit('select-output', id);
};

const onUpdateSelection = (id) => {
	const outputPort = _.cloneDeep(props.node.outputs?.find((port) => port.id === id));
	if (!outputPort) return;
	outputPort.isSelected = !outputPort?.isSelected;
	emit('update-output-port', outputPort);
};

// Used from button to add new entry to the mapping object
function addMapping() {
	mapping.value.push({
		modelVariable: '',
		datasetVariable: ''
	});

	const state = _.cloneDeep(props.node.state);
	state.mapping = mapping.value;

	emit('update-state', state);
}

function deleteMapping() {
	mapping.value = [{ modelVariable: '', datasetVariable: '' }];

	const state = _.cloneDeep(props.node.state);
	state.mapping = mapping.value;

	emit('update-state', state);
}

function deleteMapRow(index: number) {
	mapping.value.splice(index, 1);
	const state = _.cloneDeep(props.node.state);
	state.mapping = mapping.value;

	emit('update-state', state);
}

async function getAutoMapping() {
	if (!modelStateOptions.value) {
		toast.error('', 'No model states to map with');
		return;
	}
	if (!datasetColumns.value) {
		toast.error('', 'No dataset columns to map with');
		return;
	}
	mapping.value = (await autoCalibrationMapping(
		modelStateOptions.value,
		datasetColumns.value
	)) as CalibrateMap[];
	const state = _.cloneDeep(props.node.state);
	state.mapping = mapping.value;
	emit('update-state', state);
}

onMounted(async () => {
	const runIds = querySimulationInProgress(props.node);

	// Get sizing
	if (drilldownLossPlot.value) {
		previewChartWidth.value = drilldownLossPlot.value.offsetWidth;
	}

	// Model configuration input
	const { modelConfiguration, modelOptions } = await setupModelInput(modelConfigId.value);
	modelConfig.value = modelConfiguration;
	modelStateOptions.value = modelOptions;

	// dataset input
	const { filename, csv, datasetOptions } = await setupDatasetInput(datasetId.value);
	currentDatasetFileName.value = filename;
	csvAsset.value = csv;
	datasetColumns.value = datasetOptions;

	// currently in progress
	if (runIds.length > 0) {
		getCalibrateStatus(runIds[0]);
	}
});

onUnmounted(() => {
	poller.stop();
});

watch(
	() => props.node.active,
	async () => {
		// Update selected output
		if (props.node.active) {
			selectedOutputId.value = props.node.active;

			// FIXME: could still be running
			const state = props.node.state;
			const output = await getRunResultCiemss(state.simulationId, 'result.csv');
			runResults.value = output.runResults;
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
.mapping-table:deep(td) {
	padding: 0rem 0.25rem 0.5rem 0rem !important;
	border: none !important;
}

.mapping-table:deep(th) {
	padding: 0rem 0.25rem 0.5rem 0.25rem !important;
	border: none !important;
	width: 50%;
}

th {
	text-align: left;
}

.column-header {
	color: var(--text-color-primary);
	font-size: var(--font-body-small);
	font-weight: var(--font-weight-semibold);
}

.emptyState {
	align-self: center;
	display: flex;
	flex-direction: column;
	align-items: center;
	text-align: center;
	margin-top: 15rem;
	gap: 0.5rem;
}

.helpMessage {
	color: var(--text-color-subdued);
	font-size: var(--font-body-small);
	width: 90%;
	margin-top: 1rem;
}

img {
	width: 20%;
}

.form-section {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

.label-and-input {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

.input-row {
	width: 100%;
	display: flex;
	flex-direction: row;
	flex-wrap: wrap;
	align-items: center;
	gap: 0.5rem;

	& > * {
		flex: 1;
	}
}
</style>
