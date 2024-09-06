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
							<tera-input-number inputId="integeronly" v-model="knobs.numSamples" @update:model-value="updateState" />
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
								v-model="knobs.method"
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
							<tera-input-number
								inputId="integeronly"
								v-model="knobs.numIterations"
								@update:model-value="updateState"
							/>
						</div>
						<div class="label-and-input">
							<label for="num-samples">End time for forecast</label>
							<tera-input-number inputId="integeronly" v-model="knobs.endTime" />
						</div>
						<div class="label-and-input">
							<label for="learning-rate">Learning rate</label>
							<tera-input-number inputId="numberonly" v-model="knobs.learningRate" @update:model-value="updateState" />
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
			</tera-drilldown-section>
		</section>
		<section :tabName="DrilldownTabs.Notebook">
			<h5>Notebook</h5>
		</section>

		<!-- Output section -->
		<template #preview>
			<tera-drilldown-section class="ml-4 mr-2 pt-3">
				<template #header-controls-left v-if="configuredModelConfig?.name">
					<h5>{{ configuredModelConfig.name }}</h5>
				</template>
				<template #header-controls-right>
					<Button
						label="Save for re-use"
						severity="secondary"
						outlined
						:disabled="!configuredModelConfig"
						@click="showSaveModal = true"
					/>
				</template>
				<tera-operator-output-summary v-if="node.state.summaryId && !showSpinner" :summary-id="node.state.summaryId" />

				<!-- Loss chart -->
				<h5>Loss</h5>
				<div ref="lossChartContainer">
					<vega-chart
						expandable
						v-if="lossValues.length > 0 || showSpinner"
						ref="lossChartRef"
						:are-embed-actions-visible="true"
						:visualization-spec="lossChartSpec"
					/>
				</div>

				<!-- Variable charts -->
				<div v-if="!showSpinner" class="form-section">
					<section ref="outputPanel" v-if="modelConfig && csvAsset">
						<h5>Parameters</h5>
						<br />
						<template v-for="param of selectedParameters" :key="param">
							<vega-chart
								expandable
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
						<h5>Variables</h5>
						<br />
						<template v-for="variable of selectedVariables" :key="variable">
							<vega-chart expandable :are-embed-actions-visible="true" :visualization-spec="preparedCharts[variable]" />
						</template>
						<h5>Errors</h5>
						<vega-chart
							v-if="errorData.length > 0 && selectedErrorVariables.length > 0"
							:expandable="onExpandErrorChart"
							:are-embed-actions-visible="true"
							:visualization-spec="errorChart"
						/>
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
			</tera-drilldown-section>
		</template>

		<template #sidebar-right>
			<tera-slider-panel
				v-model:is-open="isOutputSettingsPanelOpen"
				direction="right"
				header="Output Settings"
				content-width="360px"
			>
				<template #overlay>
					<tera-chart-settings-panel
						:annotations="[]"
						:active-settings="activeChartSettings"
						@close="activeChartSettings = null"
					/>
				</template>
				<template #content>
					<div class="output-settings-panel">
						<h5>Parameters</h5>
						<tera-chart-settings-item
							v-for="settings of chartSettings.filter(
								(setting) => setting.type === ChartSettingType.DISTRIBUTION_COMPARISON
							)"
							:key="settings.id"
							:settings="settings"
							@open="activeChartSettings = settings"
							@remove="removeChartSetting"
						/>
						<tera-chart-control
							:chart-config="{ selectedRun: 'fixme', selectedVariable: selectedParameters }"
							:multi-select="true"
							:show-remove-button="false"
							:variables="Object.keys(pyciemssMap).filter((c) => modelPartTypesMap[c] === 'parameter')"
							@configuration-change="updateSelectedParameters"
						/>
						<hr />
						<h5>Model Variables</h5>
						<tera-chart-settings-item
							v-for="settings of chartSettings.filter(
								(setting) => setting.type === ChartSettingType.VARIABLE_COMPARISON
							)"
							:key="settings.id"
							:settings="settings"
							@open="activeChartSettings = settings"
							@remove="removeChartSetting"
						/>
						<tera-chart-control
							:chart-config="{ selectedRun: 'fixme', selectedVariable: selectedVariables }"
							:multi-select="true"
							:show-remove-button="false"
							:variables="
								Object.keys(pyciemssMap).filter((c) => ['state', 'observable'].includes(modelPartTypesMap[c]))
							"
							@configuration-change="updateSelectedVariables"
						/>
						<hr />
						<h5>Error</h5>
						<tera-chart-settings-item
							v-for="settings of chartSettings.filter(
								(setting) => setting.type === ChartSettingType.ERROR_DISTRIBUTION
							)"
							:key="settings.id"
							:settings="settings"
							@open="activeChartSettings = settings"
							@remove="removeChartSetting"
						/>
						<tera-chart-control
							:chart-config="{ selectedRun: 'fixme', selectedVariable: selectedErrorVariables }"
							:multi-select="true"
							:show-remove-button="false"
							:variables="Object.keys(pyciemssMap).filter((c) => mapping.find((d) => d.modelVariable === c))"
							@configuration-change="updateSelectedErrorVariables"
						/>
					</div>
				</template>
			</tera-slider-panel>
		</template>
	</tera-drilldown>
	<tera-save-asset-modal
		:initial-name="configuredModelConfig?.name"
		:is-visible="showSaveModal"
		:asset="configuredModelConfig"
		:asset-type="AssetType.ModelConfiguration"
		@close-modal="showSaveModal = false"
		@on-save="onSaveAsModelConfiguration"
		is-updating-asset
	/>
</template>

<script setup lang="ts">
import _ from 'lodash';
import * as vega from 'vega';
import { csvParse, autoType, mean, variance } from 'd3';
import { computed, onMounted, ref, shallowRef, watch } from 'vue';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Dropdown from 'primevue/dropdown';
import Column from 'primevue/column';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import { CalibrateMap, setupDatasetInput, setupModelInput } from '@/services/calibrate-workflow';
import { removeChartSettingById, updateChartSettingsBySelectedVariables } from '@/services/chart-settings';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraNotebookError from '@/components/drilldown/tera-notebook-error.vue';
import TeraOperatorOutputSummary from '@/components/operator/tera-operator-output-summary.vue';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraChartSettingsPanel from '@/components/widgets/tera-chart-settings-panel.vue';
import TeraChartSettingsItem from '@/components/widgets/tera-chart-settings-item.vue';
import {
	CalibrationRequestCiemss,
	ClientEvent,
	ClientEventType,
	CsvAsset,
	DatasetColumn,
	ModelConfiguration,
	AssetType
} from '@/types/Types';
import { CiemssPresetTypes, DrilldownTabs, ChartSetting, ChartSettingType } from '@/types/common';
import { getTimespan, drilldownChartSize, nodeMetadata } from '@/components/workflow/util';
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
import { getModelConfigurationById } from '@/services/model-configurations';

import { WorkflowNode } from '@/types/workflow';
import { createForecastChart, createHistogramChart, createErrorChart } from '@/services/charts';
import VegaChart from '@/components/widgets/VegaChart.vue';
import TeraChartControl from '@/components/workflow/tera-chart-control.vue';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import { displayNumber } from '@/utils/number';
import TeraPyciemssCancelButton from '@/components/pyciemss/tera-pyciemss-cancel-button.vue';
import TeraSaveAssetModal from '@/components/project/tera-save-asset-modal.vue';
import type { CalibrationOperationStateCiemss } from './calibrate-operation';
import { renameFnGenerator, mergeResults, getErrorData } from './calibrate-utils';

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateCiemss>;
}>();
const emit = defineEmits(['close', 'select-output', 'update-state']);
const toast = useToastService();

interface BasicKnobs {
	numIterations: number;
	numSamples: number;
	endTime: number;
	stepSize: number;
	learningRate: number;
	method: string;
}

const knobs = ref<BasicKnobs>({
	numIterations: props.node.state.numIterations ?? 1000,
	numSamples: props.node.state.numSamples ?? 100,
	endTime: props.node.state.endTime ?? 100,
	stepSize: props.node.state.stepSize ?? 1,
	learningRate: props.node.state.learningRate ?? 0.1,
	method: props.node.state.method ?? CiemssMethodOptions.dopri5
});

const presetType = computed(() => {
	if (knobs.value.numSamples === speedPreset.numSamples && knobs.value.method === speedPreset.method) {
		return CiemssPresetTypes.Fast;
	}
	if (knobs.value.numSamples === qualityPreset.numSamples && knobs.value.method === qualityPreset.method) {
		return CiemssPresetTypes.Normal;
	}

	return '';
});

const speedPreset = Object.freeze({
	numSamples: 1,
	method: CiemssMethodOptions.euler,
	numIterations: 10,
	learningRate: 0.1
});

const qualityPreset = Object.freeze({
	numSamples: 100,
	method: CiemssMethodOptions.dopri5,
	numIterations: 1000,
	learningRate: 0.03
});

const calibrationSettingsToolTip: string = 'TODO';
const numberOfSamplesTooltip: string = 'TODO';
const inferenceOptionsTooltip: string = 'TODO';
const odeSolverOptionsTooltip: string = 'TODO';

// Model variables checked in the model configuration will be options in the mapping dropdown
const modelStateOptions = ref<any[] | undefined>();

const isOutputSettingsPanelOpen = ref(true);
const activeChartSettings = ref<ChartSetting | null>(null);

const datasetColumns = ref<DatasetColumn[]>();
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);
const groundTruthData = computed<DataArray>(() => {
	if (!csvAsset.value) return [];
	const csv = (csvAsset.value as CsvAsset).csv;
	const csvRaw = csv.map((d) => d.join(',')).join('\n');
	return csvParse(csvRaw, autoType);
});

const modelConfig = ref<ModelConfiguration>();

const modelVarUnits = ref<{ [key: string]: string }>({});
const modelPartTypesMap = ref<{ [key: string]: string }>({});

const modelConfigId = computed<string | undefined>(() => props.node.inputs[0]?.value?.[0]);
const datasetId = computed<string | undefined>(() => props.node.inputs[1]?.value?.[0]);
const policyInterventionId = computed(() => props.node.inputs[2].value);

const cancelRunId = computed(
	() =>
		props.node.state.inProgressForecastId ||
		props.node.state.inProgressCalibrationId ||
		props.node.state.inProgressPreForecastId
);
const currentDatasetFileName = ref<string>();

const runResult = ref<DataArray>([]);
const runResultPre = ref<DataArray>([]);
const runResultSummary = ref<DataArray>([]);
const runResultSummaryPre = ref<DataArray>([]);
const errorData = ref<Record<string, any>[]>([]);
const showSaveModal = ref(false);
const configuredModelConfig = ref<ModelConfiguration | null>(null);

const showSpinner = ref(false);

const mapping = ref<CalibrateMap[]>(props.node.state.mapping);

const mappingDropdownPlaceholder = computed(() => {
	if (!_.isEmpty(modelStateOptions.value) && !_.isEmpty(datasetColumns.value)) return 'Select variable';
	return 'Please wait...';
});

const updateState = () => {
	const state = _.cloneDeep(props.node.state);
	state.numSamples = knobs.value.numSamples;
	state.method = knobs.value.method;
	state.numIterations = knobs.value.numIterations;
	state.learningRate = knobs.value.learningRate;
	emit('update-state', state);
};

const setPresetValues = (data: CiemssPresetTypes) => {
	if (data === CiemssPresetTypes.Normal) {
		knobs.value.numSamples = qualityPreset.numSamples;
		knobs.value.method = qualityPreset.method;
		knobs.value.numIterations = qualityPreset.numIterations;
		knobs.value.learningRate = qualityPreset.learningRate;
	}
	if (data === CiemssPresetTypes.Fast) {
		knobs.value.numSamples = speedPreset.numSamples;
		knobs.value.method = speedPreset.method;
		knobs.value.numIterations = speedPreset.numIterations;
		knobs.value.learningRate = speedPreset.learningRate;
	}
};

const disableRunButton = computed(() => {
	const timestampMapping = mapping.value.find((d) => d.modelVariable === 'timestamp');
	return (
		!currentDatasetFileName.value ||
		!csvAsset.value ||
		!modelConfigId.value ||
		!datasetId.value ||
		!timestampMapping ||
		timestampMapping.datasetVariable === ''
	);
});

const selectedOutputId = ref<string>();
const lossChartContainer = ref(null);
const lossChartSize = computed(() => drilldownChartSize(lossChartContainer.value));
const outputPanel = ref(null);
const chartSize = computed(() => drilldownChartSize(outputPanel.value));

const chartSettings = computed(() => props.node.state.chartSettings ?? []);
const selectedParameters = computed(() =>
	chartSettings.value
		.filter((setting) => setting.type === ChartSettingType.DISTRIBUTION_COMPARISON)
		.map((setting) => setting.selectedVariables[0])
);
const selectedVariables = computed(() =>
	chartSettings.value
		.filter((setting) => setting.type === ChartSettingType.VARIABLE_COMPARISON)
		.map((setting) => setting.selectedVariables[0])
);
const selectedErrorVariables = computed(() =>
	chartSettings.value
		.filter((setting) => setting.type === ChartSettingType.ERROR_DISTRIBUTION)
		.map((setting) => setting.selectedVariables[0])
);

const pyciemssMap = ref<Record<string, string>>({});
const preparedChartInputs = computed(() => {
	const state = props.node.state;

	if (!state.calibrationId) return null;

	// Merge before/after for chart
	const { result, resultSummary } = mergeResults(
		runResultPre.value,
		runResult.value,
		runResultSummaryPre.value,
		runResultSummary.value
	);

	// Build lookup map for calibration, include before/afer and dataset (observations)
	const reverseMap: Record<string, string> = {};
	Object.keys(pyciemssMap.value).forEach((key) => {
		reverseMap[`${pyciemssMap.value[key]}_mean`] = `${key} after calibration`;
		reverseMap[`${pyciemssMap.value[key]}_mean:pre`] = `${key} before calibration`;
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
	if (!preparedChartInputs.value) return {};
	const { result, resultSummary, reverseMap } = preparedChartInputs.value;
	const state = props.node.state;

	// Need to get the dataset's time field
	const datasetTimeField = state.mapping.find((d) => d.modelVariable === 'timestamp')?.datasetVariable;

	const charts = {};
	selectedVariables.value.forEach((variable) => {
		const datasetVariables: string[] = [];
		const mapObj = state.mapping.find((d) => d.modelVariable === variable);
		if (mapObj) {
			datasetVariables.push(mapObj.datasetVariable);
		}
		charts[variable] = createForecastChart(
			{
				data: result,
				variables: [`${pyciemssMap.value[variable]}:pre`, pyciemssMap.value[variable]],
				timeField: 'timepoint_id',
				groupField: 'sample_id'
			},
			{
				data: resultSummary,
				variables: [`${pyciemssMap.value[variable]}_mean:pre`, `${pyciemssMap.value[variable]}_mean`],
				timeField: 'timepoint_id'
			},
			{
				data: groundTruthData.value,
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
	const labelBefore = 'Before calibration';
	const labelAfter = 'After calibration';
	const charts = {};
	selectedParameters.value.forEach((param) => {
		const fieldName = pyciemssMap.value[param];
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

const errorChartVariables = computed(() => {
	if (!selectedErrorVariables.value.length) return [];
	const getDatasetVariable = (modelVariable: string) =>
		mapping.value.find((d) => d.modelVariable === modelVariable)?.datasetVariable;
	const variables = selectedErrorVariables.value.map((variable) => ({
		field: getDatasetVariable(variable) as string,
		label: variable
	}));
	return variables;
});

const errorChart = computed(() => {
	if (errorData.value.length === 0) return {};
	const spec = createErrorChart(errorData.value, {
		title: '',
		width: chartSize.value.width,
		variables: errorChartVariables.value,

		xAxisTitle: 'Mean absolute (MAE)'
	});
	return spec;
});

const onExpandErrorChart = () => {
	if (errorData.value.length === 0) return {};
	// Customize the chart size by modifying the spec before expanding the chart
	const spec = createErrorChart(errorData.value, {
		title: '',
		width: window.innerWidth / 1.5,
		height: 230,
		boxPlotHeight: 50,
		areaChartHeight: 150,
		variables: errorChartVariables.value,
		xAxisTitle: 'Mean absolute (MAE)'
	});
	return spec as any;
};

const LOSS_CHART_DATA_SOURCE = 'lossData'; // Name of the streaming data source
const lossChartRef = ref<InstanceType<typeof VegaChart>>();
const lossChartSpec = ref();
const lossValues = ref<{ [key: string]: number }[]>([]);
const updateLossChartSpec = (data: string | Record<string, any>[]) => {
	lossChartSpec.value = createForecastChart(
		null,
		{
			data: Array.isArray(data) ? data : { name: data },
			variables: ['loss'],
			timeField: 'iter'
		},
		null,
		{
			title: '',
			width: lossChartSize.value.width,
			height: 100,
			xAxisTitle: 'Solver iterations',
			yAxisTitle: 'Loss'
		}
	);
};

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
	lossValues.value = [];

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

	if (policyInterventionId.value?.[0]) {
		calibrationRequest.policyInterventionId = policyInterventionId.value[0];
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
	if (!lossChartRef.value?.view) return;
	const data = { iter: lossValues.value.length, loss: event.data.loss };
	lossChartRef.value.view.change(LOSS_CHART_DATA_SOURCE, vega.changeset().insert(data)).resize().run();
	lossValues.value.push(data);
};

const onSelection = (id: string) => {
	emit('select-output', id);
};

function removeChartSetting(chartId) {
	emit('update-state', {
		...props.node.state,
		chartSettings: removeChartSettingById(chartSettings.value, chartId)
	});
}

function updateSelectedParameters(event) {
	emit('update-state', {
		...props.node.state,
		chartSettings: updateChartSettingsBySelectedVariables(
			chartSettings.value,
			ChartSettingType.DISTRIBUTION_COMPARISON,
			event.selectedVariable
		)
	});
}

function updateSelectedVariables(event) {
	emit('update-state', {
		...props.node.state,
		chartSettings: updateChartSettingsBySelectedVariables(
			chartSettings.value,
			ChartSettingType.VARIABLE_COMPARISON,
			event.selectedVariable
		)
	});
}

function updateSelectedErrorVariables(event) {
	emit('update-state', {
		...props.node.state,
		chartSettings: updateChartSettingsBySelectedVariables(
			chartSettings.value,
			ChartSettingType.ERROR_DISTRIBUTION,
			event.selectedVariable
		)
	});
}

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

const initialize = async () => {
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

	getConfiguredModelConfig();
};

const onSaveAsModelConfiguration = async () => {
	getConfiguredModelConfig();
};

const getConfiguredModelConfig = async () => {
	const selectedOutput = props.node.outputs.find((output) => output.id === selectedOutputId.value);
	const configuredModelId = selectedOutput?.value?.[0]?.modelConfigId;
	if (configuredModelId) {
		configuredModelConfig.value = await getModelConfigurationById(configuredModelId);
	}
};

onMounted(async () => {
	initialize();
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
			updateLossChartSpec(lossValues.value);
			unsubscribeToUpdateMessages([id], ClientEventType.SimulationPyciemss, messageHandler);
		} else {
			showSpinner.value = true;
			updateLossChartSpec(LOSS_CHART_DATA_SOURCE);
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
			await initialize();
			// Fetch saved intermediate state
			const simulationObj = await getSimulation(props.node.state.calibrationId);
			if (simulationObj?.updates) {
				lossValues.value = simulationObj?.updates
					.sort((a, b) => a.data.progress - b.data.progress)
					.map((d, i) => ({
						iter: i,
						loss: d.data.loss
					}));
				updateLossChartSpec(lossValues.value);
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

			if (!runResult.value.length) return;
			pyciemssMap.value = parsePyCiemssMap(runResult.value[0]);

			errorData.value = getErrorData(groundTruthData.value, runResult.value, mapping.value);
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
.chart-settings-item-container {
	gap: var(--gap-2);
}

.output-settings-panel {
	padding: var(--gap-4);
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);
	hr {
		border: 0;
		border-top: 1px solid var(--surface-border-alt);
		width: 100%;
	}
}
</style>
