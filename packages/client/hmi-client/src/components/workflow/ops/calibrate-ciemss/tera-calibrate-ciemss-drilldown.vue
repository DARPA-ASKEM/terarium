<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
		@update:selection="onSelection"
	>
		<section :tabName="DrilldownTabs.Wizard" class="ml-4 mr-2 pt-3">
			<tera-drilldown-section>
				<template #header-controls-right>
					<tera-pyciemss-cancel-button class="mr-auto" :simulation-run-id="cancelRunId" />
					<Button label="Run" icon="pi pi-play" @click="runCalibrate" :disabled="disableRunButton" />
				</template>
				<div class="form-section">
					<h5>Mapping</h5>
					<DataTable class="mapping-table" :value="mapping">
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
								<Button class="p-button-sm p-button-text" label="Delete" @click="deleteMapRow(index)" />
							</template>
						</Column>
					</DataTable>
					<div class="flex justify-content-between">
						<div>
							<Button class="p-button-sm p-button-text" icon="pi pi-plus" label="Add mapping" @click="addMapping" />
							<Button
								class="p-button-sm p-button-text"
								icon="pi pi-sparkles"
								label="Auto map"
								@click="getAutoMapping"
							/>
						</div>
						<Button class="p-button-sm p-button-text" label="Delete all mapping" @click="deleteMapping" />
					</div>
				</div>

				<section class="form-section">
					<h5>
						Calibration settings
						<i v-tooltip="calibrationSettingsToolTip" class="pi pi-info-circle" />
					</h5>
					<div class="label-and-input">
						<label for="4">Preset (optional)</label>
						<Dropdown
							v-model="presetType"
							placeholder="Select an option"
							:options="[CiemssPresetTypes.Fast, CiemssPresetTypes.Normal]"
							@update:model-value="setPresetValues"
						/>
					</div>
					<h5>
						Number of Samples
						<i v-tooltip="numberOfSamplesTooltip" class="pi pi-info-circle" />
					</h5>
					<div class="input-row">
						<div class="label-and-input">
							<tera-input-number inputId="integeronly" v-model="numSamples" @update:model-value="updateState" />
						</div>
					</div>
					<h5>
						ODE solver options
						<i v-tooltip="odeSolverOptionsTooltip" class="pi pi-info-circle" />
					</h5>
					<div class="input-row">
						<div class="label-and-input">
							<label for="5">Method</label>
							<Dropdown
								id="5"
								v-model="method"
								:options="[CiemssMethodOptions.dopri5, CiemssMethodOptions.euler]"
								@update:model-value="updateState"
							/>
						</div>
						<div class="label-and-input">
							<label for="num-steps">Step size</label>
							<tera-input-number inputId="integeronly" v-model="knobs.stepSize" />
						</div>
					</div>
					<h5>
						Inference Options
						<i v-tooltip="inferenceOptionsTooltip" class="pi pi-info-circle" />
					</h5>
					<div class="input-row">
						<div class="label-and-input">
							<label for="num-iterations">Number of solver iterations</label>
							<tera-input-number inputId="integeronly" v-model="numIterations" @update:model-value="updateState" />
						</div>
						<div class="label-and-input">
							<label for="num-samples">End time for forecast</label>
							<tera-input-number inputId="integeronly" v-model="knobs.endTime" />
						</div>
						<div class="label-and-input">
							<label for="learning-rate">Learning rate</label>
							<tera-input-number inputId="numberonly" v-model="learningRate" @update:model-value="updateState" />
						</div>
						<div class="label-and-input">
							<label>Inference algorithm</label>
							<tera-input-text disabled model-value="SVI" />
						</div>
						<div class="label-and-input">
							<label>Loss function</label>
							<tera-input-text disabled model-value="ELBO" />
						</div>
						<div class="label-and-input">
							<label>Optimizer method</label>
							<tera-input-text disabled model-value="ADAM" />
						</div>
					</div>
				</section>
				<tera-pyciemss-output-settings
					:successDisplayChartsCheckbox="true"
					:summaryCheckbox="true"
					:interventionsDisplayChartsCheckbox="true"
					:simulationDisplayChartsCheckbox="true"
					:selectedSimulationVariables="knobs.selectedSimulationVariables"
					:selectedInterventionVariables="knobs.selectedInterventionVariables"
					:simulationChartOptions="simulationChartOptions"
					:interventionsOptions="interventionAppliedToOptions"
					@update-self="updateOutputSettingForm"
				/>
			</tera-drilldown-section>
		</section>
		<section :tabName="DrilldownTabs.Notebook">
			<h5>Notebook</h5>
		</section>

		<!-- Output section -->
		<template #preview>
			<tera-drilldown-preview>
				<tera-operator-output-summary v-if="node.state.summaryId && !showSpinner" :summary-id="node.state.summaryId" />

				<!-- Loss chart -->
				<h5>Loss</h5>
				<div ref="drilldownLossPlot" class="loss-chart" />

				<!-- Variable charts -->
				<div v-if="!showSpinner" class="form-section">
					<section v-if="modelConfig && csvAsset" ref="outputPanel">
						<h5>Parameters</h5>
						<tera-chart-control
							:chart-config="{ selectedRun: 'fixme', selectedVariable: selectedParameters }"
							:multi-select="true"
							:show-remove-button="false"
							:variables="Object.keys(pyciemssMap).filter((c) => modelPartTypesMap[c] === 'parameter')"
							@configuration-change="updateSelectedParameters"
						/>
						<template v-for="param of node.state.selectedParameters" :key="param">
							<vega-chart
								:are-embed-actions-visible="true"
								:visualization-spec="preparedDistributionCharts[param].histogram"
							>
								<template v-slot:footer>
									<table class="distribution-table">
										<thead>
											<tr>
												<th scope="col"></th>
												<th scope="col">{{ preparedDistributionCharts[param].stat.header[0] }}</th>
												<th scope="col">{{ preparedDistributionCharts[param].stat.header[1] }}</th>
											</tr>
										</thead>
										<tbody>
											<tr>
												<th scope="row">Mean</th>
												<td>{{ preparedDistributionCharts[param].stat.mean[0] }}</td>
												<td>{{ preparedDistributionCharts[param].stat.mean[1] }}</td>
											</tr>
											<tr>
												<th scope="row">Variance</th>
												<td>{{ preparedDistributionCharts[param].stat.variance[0] }}</td>
												<td>{{ preparedDistributionCharts[param].stat.variance[1] }}</td>
											</tr>
										</tbody>
									</table>
								</template>
							</vega-chart>
						</template>
						<template v-for="variable of node.state.selectedSimulationVariables" :key="variable">
							<vega-chart :are-embed-actions-visible="true" :visualization-spec="preparedCharts[variable]" />
						</template>
					</section>
					<section v-else-if="!modelConfig" class="emptyState">
						<img src="@assets/svg/seed.svg" alt="" draggable="false" />
						<p class="helpMessage">Connect a model configuration and dataset</p>
					</section>
				</div>

				<section v-else class="emptyState">
					<tera-progress-spinner :font-size="2" is-centered style="height: 12rem" />
					<p>Processing...{{ props.node.state.currentProgress }}%</p>
				</section>

				<tera-notebook-error v-if="!_.isEmpty(node.state?.errorMessage?.traceback)" v-bind="node.state.errorMessage" />
			</tera-drilldown-preview>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { csvParse, autoType, mean, variance } from 'd3';
import { computed, onMounted, ref, shallowRef, watch } from 'vue';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Dropdown from 'primevue/dropdown';
import Column from 'primevue/column';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import { CalibrateMap, renderLossGraph, setupDatasetInput, setupModelInput } from '@/services/calibrate-workflow';
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
import { getTimespan, drilldownChartSize, nodeMetadata } from '@/components/workflow/util';
import teraPyciemssOutputSettings, {
	OutputSettingKnobs
} from '@/components/pyciemss/tera-pyciemss-output-settings.vue';
import { useToastService } from '@/services/toast';
import { autoCalibrationMapping } from '@/services/concept';
import {
	getSimulation,
	getRunResultCSV,
	makeCalibrateJobCiemss,
	subscribeToUpdateMessages,
	unsubscribeToUpdateMessages,
	parsePyCiemssMap,
	DataArray,
	CiemssMethodOptions
} from '@/services/models/simulation-service';
import type { WorkflowNode } from '@/types/workflow';
import { createForecastChart, createHistogramChart } from '@/services/charts';
import { getInterventionPolicyById } from '@/services/intervention-policy';
import VegaChart from '@/components/widgets/VegaChart.vue';
import TeraChartControl from '@/components/workflow/tera-chart-control.vue';
import { CiemssPresetTypes, DrilldownTabs } from '@/types/common';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import { displayNumber } from '@/utils/number';
import type { CalibrationOperationStateCiemss } from './calibrate-operation';
import { renameFnGenerator, mergeResults } from './calibrate-utils';

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateCiemss>;
}>();
const emit = defineEmits(['close', 'select-output', 'update-state']);
const toast = useToastService();

const presetType = computed(() => {
	if (numSamples.value === speedValues.numSamples && method.value === speedValues.method) {
		return CiemssPresetTypes.Fast;
	}
	if (numSamples.value === qualityValues.numSamples && method.value === qualityValues.method) {
		return CiemssPresetTypes.Normal;
	}

	return '';
});

const speedValues = Object.freeze({
	numSamples: 1,
	method: CiemssMethodOptions.euler,
	numIterations: 10,
	learningRate: 0.1
});

const qualityValues = Object.freeze({
	numSamples: 100,
	method: CiemssMethodOptions.dopri5,
	numIterations: 1000,
	learningRate: 0.03
});

const numSamples = ref<number>(props.node.state.numSamples);
const method = ref<string>(props.node.state.method);
const numIterations = ref<number>(props.node.state.numIterations);
const learningRate = ref<number>(props.node.state.learningRate);

const calibrationSettingsToolTip: string = 'TODO';
const numberOfSamplesTooltip: string = 'TODO';
const inferenceOptionsTooltip: string = 'TODO';
const odeSolverOptionsTooltip: string = 'TODO';

// Model variables checked in the model configuration will be options in the mapping dropdown
const modelStateOptions = ref<any[] | undefined>();

const datasetColumns = ref<DatasetColumn[]>();
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

const modelConfig = ref<ModelConfiguration>();

const modelVarUnits = ref<{ [key: string]: string }>({});
const modelPartTypesMap = ref<{ [key: string]: string }>({});

const modelConfigId = computed<string | undefined>(() => props.node.inputs[0]?.value?.[0]);
const datasetId = computed<string | undefined>(() => props.node.inputs[1]?.value?.[0]);
const policyInterventionId = computed(() => props.node.inputs[2].value?.[0]);

let pyciemssMap: Record<string, string> = {};
const simulationChartOptions = ref<string[]>([]);

const interventionAppliedToOptions = ref<string[]>([]);

const cancelRunId = computed(
	() =>
		props.node.state.inProgressForecastId ||
		props.node.state.inProgressCalibrationId ||
		props.node.state.inProgressPreForecastId
);
const currentDatasetFileName = ref<string>();

const drilldownLossPlot = ref<HTMLElement>();
const runResult = ref<DataArray>([]);
const runResultPre = ref<DataArray>([]);
const runResultSummary = ref<DataArray>([]);
const runResultSummaryPre = ref<DataArray>([]);

const previewChartWidth = ref(120);

const showSpinner = ref(false);
let lossValues: { [key: string]: number }[] = [];

const mapping = ref<CalibrateMap[]>(props.node.state.mapping);

const mappingDropdownPlaceholder = computed(() => {
	if (!_.isEmpty(modelStateOptions.value) && !_.isEmpty(datasetColumns.value)) return 'Select variable';
	return 'Please wait...';
});

const updateState = () => {
	const state = _.cloneDeep(props.node.state);
	state.numSamples = numSamples.value;
	state.method = method.value;
	state.numIterations = numIterations.value;
	state.learningRate = learningRate.value;
	emit('update-state', state);
};

interface BasicKnobs {
	numIterations: number;
	numSamples: number;
	selectedInterventionVariables: string[];
	selectedSimulationVariables: string[];
	endTime: number;
	stepSize: number;
	learningRate: number;
	method: string;
}

const knobs = ref<BasicKnobs>({
	numIterations: props.node.state.numIterations ?? 1000,
	numSamples: props.node.state.numSamples ?? 100,
	selectedInterventionVariables: props.node.state.selectedInterventionVariables ?? [],
	selectedSimulationVariables: props.node.state.selectedSimulationVariables ?? [],
	endTime: props.node.state.endTime ?? 100,
	stepSize: props.node.state.stepSize ?? 1,
	learningRate: props.node.state.learningRate ?? 0.1,
	method: props.node.state.method ?? CiemssMethodOptions.dopri5
});

const setPresetValues = (data: CiemssPresetTypes) => {
	if (data === CiemssPresetTypes.Normal) {
		numSamples.value = qualityValues.numSamples;
		method.value = qualityValues.method;
		numIterations.value = qualityValues.numIterations;
		learningRate.value = qualityValues.learningRate;
	}
	if (data === CiemssPresetTypes.Fast) {
		numSamples.value = speedValues.numSamples;
		method.value = speedValues.method;
		numIterations.value = speedValues.numIterations;
		learningRate.value = speedValues.learningRate;
	}
};

const disableRunButton = computed(
	() => !currentDatasetFileName.value || !csvAsset.value || !modelConfigId.value || !datasetId.value
);

const selectedOutputId = ref<string>();
const outputPanel = ref(null);
const chartSize = computed(() => drilldownChartSize(outputPanel.value));

const selectedParameters = ref<string[]>(props.node.state.selectedParameters);

const preparedChartInputs = computed(() => {
	const state = props.node.state;

	if (!state.calibrationId) return null;

	// Merge before/after for chart
	const { result, resultSummary } = mergeResults(
		runResult.value,
		runResultPre.value,
		runResultSummary.value,
		runResultSummaryPre.value
	);

	// Build lookup map for calibration, include before/afer and dataset (observations)
	const reverseMap: Record<string, string> = {};
	Object.keys(pyciemssMap).forEach((key) => {
		reverseMap[`${pyciemssMap[key]}_mean`] = `${key} after calibration`;
		reverseMap[`${pyciemssMap[key]}_mean:pre`] = `${key} before calibration`;
	});
	state.mapping.forEach((mapObj) => {
		reverseMap[mapObj.datasetVariable] = 'Observations';
	});
	return {
		result,
		resultSummary,
		reverseMap
	};
});

const preparedCharts = computed(() => {
	if (!preparedChartInputs.value) return [];
	const { result, resultSummary, reverseMap } = preparedChartInputs.value;
	const state = props.node.state;

	// FIXME: Hacky re-parse CSV with correct data types
	let groundTruth: DataArray = [];
	if (csvAsset.value) {
		const csv = csvAsset.value.csv;
		const csvRaw = csv.map((d) => d.join(',')).join('\n');
		groundTruth = csvParse(csvRaw, autoType);
	}

	// Need to get the dataset's time field
	const datasetTimeField = state.mapping.find((d) => d.modelVariable === 'timestamp')?.datasetVariable;

	const charts = {};
	state.selectedSimulationVariables.forEach((variable) => {
		const datasetVariables: string[] = [];
		const mapObj = state.mapping.find((d) => d.modelVariable === variable);
		if (mapObj) {
			datasetVariables.push(mapObj.datasetVariable);
		}
		charts[variable] = createForecastChart(
			{
				dataset: result,
				variables: [`${pyciemssMap[variable]}:pre`, pyciemssMap[variable]],
				timeField: 'timepoint_id',
				groupField: 'sample_id'
			},
			{
				dataset: resultSummary,
				variables: [`${pyciemssMap[variable]}_mean:pre`, `${pyciemssMap[variable]}_mean`],
				timeField: 'timepoint_id'
			},
			{
				dataset: groundTruth,
				variables: datasetVariables,
				timeField: datasetTimeField as string,
				groupField: 'sample_id'
			},
			{
				title: variable,
				width: chartSize.value.width,
				height: chartSize.value.height,
				legend: true,
				translationMap: reverseMap,
				xAxisTitle: modelVarUnits.value._time || 'Time',
				yAxisTitle: modelVarUnits.value[variable] || '',
				colorscheme: ['#AAB3C6', '#1B8073']
			}
		);
	});
	return charts;
});

const preparedDistributionCharts = computed(() => {
	if (!preparedChartInputs.value) return [];
	const { result } = preparedChartInputs.value;
	const state = props.node.state;
	const labelBefore = 'Before calibration';
	const labelAfter = 'After calibration';
	const charts = {};
	state.selectedParameters.forEach((param) => {
		const fieldName = pyciemssMap[param];
		const beforeFieldName = `${fieldName}:pre`;
		const histogram = createHistogramChart(result, {
			title: `${param}`,
			width: chartSize.value.width,
			height: chartSize.value.height,
			xAxisTitle: `${param}`,
			yAxisTitle: 'Count',
			maxBins: 10,
			variables: [
				{ field: beforeFieldName, label: labelBefore, width: 54, color: '#AAB3C6' },
				{ field: fieldName, label: labelAfter, width: 24, color: '#1B8073' }
			]
		});
		const toDisplayNumber = (num?: number) => (num ? displayNumber(num.toString()) : '');
		const stat = {
			header: [labelBefore, labelAfter],
			mean: [mean(result, (d) => d[beforeFieldName]), mean(result, (d) => d[fieldName])].map(toDisplayNumber),
			variance: [variance(result, (d) => d[beforeFieldName]), variance(result, (d) => d[fieldName])].map(
				toDisplayNumber
			)
		};
		charts[param] = { histogram, stat };
	});
	return charts;
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

	const state = _.cloneDeep(props.node.state);

	// Create request
	const calibrationRequest: CalibrationRequestCiemss = {
		modelConfigId: modelConfigId.value,
		dataset: {
			id: datasetId.value,
			filename: currentDatasetFileName.value,
			mappings: formattedMap
		},
		extra: {
			solver_method: knobs.value.method,
			solver_step_size: knobs.value.stepSize,
			lr: knobs.value.learningRate,
			num_iterations: knobs.value.numIterations
		},
		timespan: getTimespan({ dataset: csvAsset.value, mapping: mapping.value }),
		engine: 'ciemss'
	};

	if (policyInterventionId.value) {
		calibrationRequest.policyInterventionId = policyInterventionId.value;
	}

	const response = await makeCalibrateJobCiemss(calibrationRequest, nodeMetadata(props.node));

	if (response?.simulationId) {
		state.inProgressCalibrationId = response?.simulationId;
		state.currentProgress = 0;
		state.inProgressForecastId = '';
		state.inProgressPreForecastId = '';

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

function updateSelectedParameters(event) {
	emit('update-state', { ...props.node.state, selectedParameters: event.selectedVariable });
}

const updateOutputSettingForm = (config: OutputSettingKnobs) => {
	knobs.value.selectedSimulationVariables = config.selectedSimulationVariables;
	knobs.value.selectedInterventionVariables = config.selectedInterventionVariables;
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
	mapping.value = (await autoCalibrationMapping(modelStateOptions.value, datasetColumns.value)) as CalibrateMap[];
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
	const { modelConfiguration, modelOptions, modelPartUnits, modelPartTypes } = await setupModelInput(
		modelConfigId.value
	);
	modelConfig.value = modelConfiguration;
	modelStateOptions.value = modelOptions;
	modelVarUnits.value = modelPartUnits ?? {};
	modelPartTypesMap.value = modelPartTypes ?? {};

	// dataset input
	const { filename, csv, datasetOptions } = await setupDatasetInput(datasetId.value);
	currentDatasetFileName.value = filename;
	csvAsset.value = csv;
	datasetColumns.value = datasetOptions;

	// Intervention input
	if (policyInterventionId.value) {
		const interventionPolicy = await getInterventionPolicyById(policyInterventionId.value);
		interventionAppliedToOptions.value = [...new Set(interventionPolicy.interventions.map((ele) => ele.appliedTo))];
	}
});

watch(
	() => knobs.value,
	async () => {
		const state = _.cloneDeep(props.node.state);
		state.numIterations = knobs.value.numIterations;
		state.numSamples = knobs.value.numSamples;
		state.endTime = knobs.value.endTime;
		state.selectedInterventionVariables = knobs.value.selectedInterventionVariables;
		state.selectedSimulationVariables = knobs.value.selectedSimulationVariables;
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
			const simulationObj = await getSimulation(props.node.state.calibrationId);
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
			runResult.value = await getRunResultCSV(state.forecastId, 'result.csv');
			runResultSummary.value = await getRunResultCSV(state.forecastId, 'result_summary.csv');

			runResultPre.value = await getRunResultCSV(state.preForecastId, 'result.csv', renameFnGenerator('pre'));
			runResultSummaryPre.value = await getRunResultCSV(
				state.preForecastId,
				'result_summary.csv',
				renameFnGenerator('pre')
			);

			pyciemssMap = parsePyCiemssMap(runResult.value[0]);
			simulationChartOptions.value = Object.keys(pyciemssMap);
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
.mapping-table:deep(td) {
	border: none !important;
	padding: 0 var(--gap-1) var(--gap-2) 0 !important;
}

.mapping-table:deep(th) {
	border: none !important;
	padding: 0 var(--gap-1) var(--gap-2) var(--gap-1) !important;
	width: 50%;
}

th {
	text-align: left;
}

.column-header {
	color: var(--text-color-primary);
	font-size: var(--font-body-small);
	font-weight: var(--font-weight-semibold);
	padding-top: var(--gap-2);
}

.emptyState {
	align-items: center;
	align-self: center;
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);
	margin-top: 15rem;
	text-align: center;
}

.helpMessage {
	color: var(--text-color-subdued);
	font-size: var(--font-body-small);
	margin-top: var(--gap-4);
	width: 90%;
}

img {
	width: 20%;
}

.form-section {
	background-color: var(--surface-50);
	border-radius: var(--border-radius-medium);
	box-shadow: 0px 0px 4px 0px rgba(0, 0, 0, 0.25) inset;
	display: flex;
	flex-direction: column;
	flex-grow: 1;
	gap: var(--gap-1);
	margin: 0 var(--gap) var(--gap) var(--gap);
	padding: var(--gap);
}

.label-and-input {
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);
}

.input-row {
	align-items: center;
	display: flex;
	flex-direction: row;
	flex-wrap: wrap;
	gap: var(--gap-2);
	width: 100%;

	& > * {
		flex: 1;
	}
}

.loss-chart {
	background: var(--surface-a);
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius-medium);
}

.distribution-table {
	width: 100%;
	border-collapse: collapse;
	thead {
		background-color: var(--surface-200);
	}
	tr {
		height: 1.75rem;
	}
	tbody tr {
		border-bottom: 1px solid var(--surface-border-light);
	}
	td,
	th {
		text-align: center;
	}
}
</style>
