<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
		@update:selection="onSelection"
	>
		<section :tabName="CalibrateTabs.Wizard" class="ml-4 mr-2 pt-3">
			<tera-drilldown-section>
				<template #header-controls-right>
					<tera-pyciemss-cancel-button class="mr-auto" :simulation-run-id="cancelRunId" />
					<Button
						label="Run"
						icon="pi pi-play"
						@click="runCalibrate"
						:disabled="disableRunButton"
					/>
				</template>
				<div class="form-section">
					<h5>Mapping</h5>
					<DataTable class="mapping-table" :value="mapping">
						<Button
							class="p-button-sm p-button-text"
							label="Delete all mapping"
							@click="deleteMapping"
						/>
						<Column field="modelVariable">
							<template #header>
								<span class="column-header">Model variable</span>
							</template>
							<template #body="{ data, field }">
								<Dropdown
									class="w-full"
									:placeholder="mappingDropdownPlaceholder"
									v-model="data[field]"
									:options="modelStateOptions?.map((ele) => ele.referenceId ?? ele.id)"
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
									:placeholder="mappingDropdownPlaceholder"
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
							<InputNumber
								class="p-inputtext-sm"
								inputId="integeronly"
								v-model="knobs.numSamples"
							/>
						</div>
						<div class="label-and-input">
							<label for="num-iterations">Number of solver iterations</label>
							<InputNumber
								class="p-inputtext-sm"
								inputId="integeronly"
								v-model="knobs.numIterations"
							/>
						</div>
						<div class="label-and-input">
							<label for="num-samples">End time for forecast</label>
							<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="knobs.endTime" />
						</div>
					</div>
				</div>
			</tera-drilldown-section>
		</section>
		<section :tabName="CalibrateTabs.Notebook">
			<h5>Notebook</h5>
		</section>
		<template #preview>
			<tera-drilldown-preview>
				<tera-operator-output-summary
					v-if="node.state.summaryId && !showSpinner"
					:summary-id="node.state.summaryId"
				/>

				<h5>Loss</h5>
				<div ref="drilldownLossPlot"></div>
				<div v-if="!showSpinner" class="form-section">
					<h5>Variables</h5>
					<section
						v-if="modelConfig && node.state.chartConfigs.length && csvAsset"
						ref="outputPanel"
					>
						<template v-for="(cfg, index) of node.state.chartConfigs" :key="index">
							<tera-chart-control
								:variables="Object.keys(pyciemssMap)"
								:chartConfig="{ selectedRun: selectedRunId, selectedVariable: cfg }"
								:show-remove-button="true"
								@configuration-change="chartProxy.configurationChange(index, $event)"
								@remove="chartProxy.removeChart(index)"
							/>
							<vega-chart
								:are-embed-actions-visible="true"
								:visualization-spec="preparedCharts[index]"
							/>
						</template>
						<Button
							class="add-chart"
							text
							:outlined="true"
							@click="chartProxy.addChart()"
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
					<tera-progress-spinner :font-size="2" is-centered style="height: 100%" />
				</section>
				<tera-notebook-error
					v-if="!_.isEmpty(node.state?.errorMessage?.traceback)"
					v-bind="node.state.errorMessage"
				/>
			</tera-drilldown-preview>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { csvParse, autoType } from 'd3';
import { computed, onMounted, ref, shallowRef, watch } from 'vue';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Dropdown from 'primevue/dropdown';
import Column from 'primevue/column';
import InputNumber from 'primevue/inputnumber';
import {
	CalibrateMap,
	renderLossGraph,
	setupDatasetInput,
	setupModelInput
} from '@/services/calibrate-workflow';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraPyciemssCancelButton from '@/components/pyciemss/tera-pyciemss-cancel-button.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraNotebookError from '@/components/drilldown/tera-notebook-error.vue';
import TeraOperatorOutputSummary from '@/components/operator/tera-operator-output-summary.vue';
import {
	CalibrationRequestCiemss,
	ClientEvent,
	ClientEventType,
	CsvAsset,
	DatasetColumn,
	ModelConfiguration
} from '@/types/Types';
import {
	getTimespan,
	chartActionsProxy,
	drilldownChartSize,
	nodeMetadata
} from '@/components/workflow/util';
import { useToastService } from '@/services/toast';
import { autoCalibrationMapping } from '@/services/concept';
import {
	getSimulation,
	getRunResultCSV,
	makeCalibrateJobCiemss,
	subscribeToUpdateMessages,
	unsubscribeToUpdateMessages,
	parsePyCiemssMap
} from '@/services/models/simulation-service';

import type { WorkflowNode } from '@/types/workflow';
import { createForecastChart } from '@/services/charts';
import VegaChart from '@/components/widgets/VegaChart.vue';
import TeraChartControl from '@/components/workflow/tera-chart-control.vue';
import type { CalibrationOperationStateCiemss } from './calibrate-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateCiemss>;
}>();
const emit = defineEmits(['close', 'select-output', 'update-state']);
const toast = useToastService();

enum CalibrateTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

// Model variables checked in the model configuration will be options in the mapping dropdown
const modelStateOptions = ref<any[] | undefined>();

const datasetColumns = ref<DatasetColumn[]>();
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

const modelConfig = ref<ModelConfiguration>();

const modelConfigId = computed<string | undefined>(() => props.node.inputs[0]?.value?.[0]);
const datasetId = computed<string | undefined>(() => props.node.inputs[1]?.value?.[0]);
const policyInterventionId = computed(() => props.node.inputs[2].value);

const cancelRunId = computed(
	() => props.node.state.inProgressForecastId || props.node.state.inProgressCalibrationId
);
const currentDatasetFileName = ref<string>();

const drilldownLossPlot = ref<HTMLElement>();
const runResult = ref<any>(null);

const previewChartWidth = ref(120);

const showSpinner = ref(false);
let lossValues: { [key: string]: number }[] = [];

const mapping = ref<CalibrateMap[]>(props.node.state.mapping);

const mappingDropdownPlaceholder = computed(() => {
	if (!_.isEmpty(modelStateOptions.value) && !_.isEmpty(datasetColumns.value))
		return 'Select variable';
	return 'Please wait...';
});

interface BasicKnobs {
	numIterations: number;
	numSamples: number;
	endTime: number;
}

const knobs = ref<BasicKnobs>({
	numIterations: props.node.state.numIterations ?? 1000,
	numSamples: props.node.state.numSamples ?? 100,
	endTime: props.node.state.endTime ?? 100
});

const disableRunButton = computed(
	() => !currentDatasetFileName.value || !csvAsset.value || !modelConfigId.value || !datasetId.value
);

const selectedOutputId = ref<string>();
const selectedRunId = computed(
	() => props.node.outputs.find((o) => o.id === selectedOutputId.value)?.value?.[0]
);

let pyciemssMap: Record<string, string> = {};
const preparedCharts = computed(() => {
	if (!selectedRunId.value) return [];

	const state = props.node.state;
	const result = runResult.value;

	const reverseMap: Record<string, string> = {};
	Object.keys(pyciemssMap).forEach((key) => {
		reverseMap[`${pyciemssMap[key]}`] = key;
	});

	// Add dataset mappings to lookup as well
	state.mapping.forEach((mapObj) => {
		reverseMap[mapObj.datasetVariable] = mapObj.modelVariable;
	});

	// FIXME: Hacky re-parse CSV with correct data types
	let groundTruth: Record<string, any>[] = [];
	if (csvAsset.value) {
		const csv = csvAsset.value.csv;
		const csvRaw = csv.map((d) => d.join(',')).join('\n');
		groundTruth = csvParse(csvRaw, autoType);
	}

	// Need to get the dataset's time field
	const datasetTimeField = state.mapping.find(
		(d) => d.modelVariable === 'timestamp'
	)?.datasetVariable;

	return state.chartConfigs.map((config) => {
		const datasetVariables: string[] = [];
		config.forEach((variableName) => {
			const mapObj = state.mapping.find((d) => d.modelVariable === variableName);
			if (mapObj) {
				datasetVariables.push(mapObj.datasetVariable);
			}
		});

		return createForecastChart(
			{
				dataset: result,
				variables: config.map((d) => pyciemssMap[d]),
				timeField: 'timepoint_id',
				groupField: 'sample_id'
			},
			null,
			{
				dataset: groundTruth,
				variables: datasetVariables,
				timeField: datasetTimeField as string,
				groupField: 'sample_id'
			},
			{
				width: chartSize.value.width,
				height: chartSize.value.height,
				legend: true,
				translationMap: reverseMap,
				xAxisTitle: 'Time',
				yAxisTitle: ''
			}
		);
	});
});

const outputPanel = ref(null);
const chartSize = computed(() => drilldownChartSize(outputPanel.value));

const chartProxy = chartActionsProxy(props.node, (state: CalibrationOperationStateCiemss) => {
	emit('update-state', state);
});

const runCalibrate = async () => {
	if (!modelConfigId.value || !datasetId.value || !currentDatasetFileName.value) return;

	const formattedMap: { [index: string]: string } = {};
	// If the user has done any mapping populate formattedMap
	if (mapping.value[0].datasetVariable !== '') {
		mapping.value.forEach((ele) => {
			formattedMap[ele.datasetVariable] = ele.modelVariable;
		});
	}

	// Reset loss buffer
	lossValues = [];

	// Create request
	const calibrationRequest: CalibrationRequestCiemss = {
		modelConfigId: modelConfigId.value,
		dataset: {
			id: datasetId.value,
			filename: currentDatasetFileName.value,
			mappings: formattedMap
		},
		extra: {
			num_iterations: knobs.value.numIterations
		},
		timespan: getTimespan({ dataset: csvAsset.value, mapping: mapping.value }),
		engine: 'ciemss'
	};

	if (policyInterventionId.value?.[0]) {
		calibrationRequest.policyInterventionId = policyInterventionId.value[0];
	}

	const response = await makeCalibrateJobCiemss(calibrationRequest, nodeMetadata(props.node));

	if (response?.simulationId) {
		const state = _.cloneDeep(props.node.state);
		state.inProgressCalibrationId = response?.simulationId;
		state.inProgressForecastId = '';

		emit('update-state', state);
	}
};

const messageHandler = (event: ClientEvent<any>) => {
	lossValues.push({ iter: lossValues.length, loss: event.data.loss });

	if (drilldownLossPlot.value) {
		renderLossGraph(drilldownLossPlot.value, lossValues, {
			width: previewChartWidth.value,
			height: 120
		});
	}
};

const onSelection = (id: string) => {
	emit('select-output', id);
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
});

watch(
	() => knobs.value,
	async () => {
		const state = _.cloneDeep(props.node.state);
		state.numIterations = knobs.value.numIterations;
		state.numSamples = knobs.value.numSamples;
		state.endTime = knobs.value.endTime;
		emit('update-state', state);
	},
	{ deep: true }
);

watch(
	() => props.node.state.inProgressCalibrationId,
	(id) => {
		if (id === '') {
			showSpinner.value = false;
			unsubscribeToUpdateMessages([id], ClientEventType.SimulationPyciemss, messageHandler);
		} else {
			showSpinner.value = true;
			subscribeToUpdateMessages([id], ClientEventType.SimulationPyciemss, messageHandler);
		}
	},
	{ immediate: true }
);

watch(
	() => props.node.active,
	async () => {
		// Update selected output
		if (props.node.active) {
			selectedOutputId.value = props.node.active;

			// Fetch saved intermediate state
			const simulationObj = await getSimulation(selectedRunId.value);
			if (simulationObj?.updates) {
				lossValues = simulationObj?.updates.map((d, i) => ({
					iter: i,
					loss: d.data.loss
				}));
				if (drilldownLossPlot.value) {
					renderLossGraph(drilldownLossPlot.value, lossValues, {
						width: previewChartWidth.value,
						height: 120
					});
				}
			}

			const state = props.node.state;
			const result = await getRunResultCSV(state.forecastId, 'result.csv');
			pyciemssMap = parsePyCiemssMap(result[0]);

			runResult.value = result;

			// const output = await getRunResultCiemss(state.forecastId, 'result.csv');
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
.mapping-table:deep(td) {
	padding: 0 0.25rem 0.5rem 0 !important;
	border: none !important;
}

.mapping-table:deep(th) {
	padding: 0 0.25rem 0.5rem 0.25rem !important;
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
