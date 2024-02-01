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
			</tera-drilldown-section>
		</section>
		<section :tabName="CalibrateTabs.Notebook">
			<h4>Notebook</h4>
		</section>
		<template #preview>
			<tera-drilldown-preview title="Preview">
				<div v-if="!showSpinner" class="form-section">
					<h4>Variables</h4>
					<section v-if="modelConfig && node.state.chartConfigs.length">
						<tera-simulate-chart
							v-for="(cfg, index) of node.state.chartConfigs"
							:key="index"
							:run-results="runResults"
							:chartConfig="cfg"
							:initial-data="csvAsset"
							:mapping="mapping"
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
	/* getRunResultCiemss, */
	makeCalibrateJobCiemss,
	simulationPollAction,
	querySimulationInProgress,
	getCalibrateBlobURL
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
import InputNumber from 'primevue/inputnumber';
import {
	setupModelInput,
	setupDatasetInput,
	CalibrateMap,
	autoCalibrationMapping
} from '@/services/calibrate-workflow';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { WorkflowNode } from '@/types/workflow';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import TeraProgressBar from '@/workflow/tera-progress-bar.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import { subscribe, unsubscribe } from '@/services/ClientEventService';
import { Poller, PollerState } from '@/api/api';
import { getTimespan } from '@/workflow/util';
import { logger } from '@/utils/logger';
import { useToastService } from '@/services/toast';
import { CalibrationOperationCiemss, CalibrationOperationStateCiemss } from './calibrate-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateCiemss>;
}>();
const emit = defineEmits(['append-output-port', 'update-state', 'close']);
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
const simulationIds = computed<any | undefined>(() => props.node.outputs[0]?.value);

const runResults = ref<RunResults>({});
const completedRunId = ref<string>();

const showSpinner = ref(false);
const progress = ref({ status: ProgressState.Retrieving, value: 0 });

const mapping = ref<CalibrateMap[]>(props.node.state.mapping);

// EXTRA section
const numSamples = ref(100);
const numIterations = ref(100);
const method = ref('dopri5');
const ciemssMethodOptions = ref(['dopri5', 'euler']);

const poller = new Poller();

const disableRunButton = computed(
	() =>
		!currentDatasetFileName.value ||
		!modelConfig.value ||
		!csvAsset.value ||
		!modelConfigId.value ||
		!datasetId.value
);

onMounted(() => {
	const runIds = querySimulationInProgress(props.node);
	if (runIds.length > 0) {
		getStatus(runIds[0]);
	}
});

onUnmounted(() => {
	poller.stop();
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
			num_samples: numSamples.value,
			num_iterations: numIterations.value,
			method: method.value
		},
		timespan: getTimespan(csvAsset.value, mapping.value),
		engine: 'ciemss'
	};
	const response = await makeCalibrateJobCiemss(calibrationRequest);

	if (response?.simulationId) {
		getStatus(response.simulationId);
	}
};

function getMessageHandler(event: ClientEvent<any>) {
	const runIds: string[] = querySimulationInProgress(props.node);
	if (runIds.length === 0) return;

	if (runIds.includes(event.data.id)) {
		// perform some action here
		console.log(`Event received for: ${event.data.id}`);
	}
}

const getStatus = async (simulationId: string) => {
	showSpinner.value = true;
	if (!simulationId) {
		console.log('No sim id');
		return;
	}
	const runIds = [simulationId];

	// open a connection for each run id and handle the messages
	await subscribe(ClientEventType.SimulationPyciemss, getMessageHandler);

	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => simulationPollAction(runIds, props.node, progress, emit));

	const pollerResults = await poller.start();

	// closing event source connections
	await unsubscribe(ClientEventType.SimulationPyciemss, getMessageHandler);

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

	console.log('hihi');

	completedRunId.value = simulationId;
	updateOutputPorts(completedRunId);
	showSpinner.value = false;
};

const updateOutputPorts = async (runId) => {
	const portLabel = props.node.inputs[0].label;
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs.push({
		selectedRun: runId,
		selectedVariable: []
	});
	emit('update-state', state);

	emit('append-output-port', {
		type: CalibrationOperationCiemss.outputs[0].type,
		label: `${portLabel} Result`,
		value: {
			runId
		}
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

// Set up model config + dropdown names
// Note: Same as calibrate-node
watch(
	() => modelConfigId.value,
	async () => {
		const { modelConfiguration, modelOptions } = await setupModelInput(modelConfigId.value);
		modelConfig.value = modelConfiguration;
		modelStateOptions.value = modelOptions;
	},
	{ immediate: true }
);

// Set up csv + dropdown names
// Note: Same as calibrate-node
watch(
	() => datasetId.value,
	async () => {
		const { filename, csv, datasetOptions } = await setupDatasetInput(datasetId.value);
		currentDatasetFileName.value = filename;
		csvAsset.value = csv;
		datasetColumns.value = datasetOptions;
	},
	{ immediate: true }
);

// Fetch simulation run results whenever output changes
watch(
	() => simulationIds.value,
	async () => {
		if (!simulationIds.value) return;

		const dillURL = await getCalibrateBlobURL(simulationIds.value[0].runId);
		console.log('dill URL is', dillURL);

		/*
		const output = await getRunResultCiemss(simulationIds.value[0].runId, result.csv');
		runResults.value = output.runResults;
		*/
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
