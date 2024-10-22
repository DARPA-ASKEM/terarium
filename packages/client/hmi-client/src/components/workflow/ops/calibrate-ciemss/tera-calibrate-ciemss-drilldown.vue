<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
		@update:selection="onSelection"
	>
		<!-- Wizard -->
		<section :tabName="DrilldownTabs.Wizard" class="wizard">
			<tera-slider-panel
				class="input-config"
				v-model:is-open="isSidebarOpen"
				header="Calibration settings"
				content-width="420px"
			>
				<template #content>
					<div class="toolbar">
						<p>Set your mapping, calibration and visualization settings then click run.</p>
						<span class="flex gap-2">
							<Button
								label="Reset"
								outlined
								@click="resetState"
								severity="secondary"
								:disabled="_.isEmpty(node.outputs[0].value)"
							/>
							<tera-pyciemss-cancel-button class="mr-auto" :simulation-run-id="cancelRunId" />
							<Button label="Run" icon="pi pi-play" @click="runCalibrate" :disabled="disableRunButton" />
						</span>
					</div>

					<!-- Mapping section -->
					<div class="form-section">
						<h5 class="mb-1">Mapping</h5>
						<p class="mb-2">
							Select a subset of output variables of the model and individually associate them to columns in the
							dataset.
						</p>

						<!-- Mapping table: Time variables -->
						<div class="input-row mapping-input">
							<div class="label-and-input">
								<label class="column-header">Model: Timeline variable</label>
								<tera-input-text disabled model-value="timestamp" />
							</div>
							<div class="label-and-input">
								<label class="column-header">Dataset: Timeline variable</label>
								<Dropdown
									class="w-full"
									:placeholder="mappingDropdownPlaceholder"
									v-model="knobs.timestampColName"
									:options="datasetColumns?.map((ele) => ele.name)"
								/>
							</div>
						</div>

						<!-- Mapping table: Other variables -->
						<DataTable class="mapping-table" :value="mapping">
							<Column field="modelVariable">
								<template #header>
									<span class="column-header">Model: Other variables</span>
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
									<span class="column-header">Dataset: Other variables</span>
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
									<Button class="p-button-sm p-button-text" icon="pi pi-trash" @click="deleteMapRow(index)" />
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
							<Button class="p-button-sm p-button-text" label="Delete all mapping" @click="deleteAllMappings" />
						</div>
					</div>

					<!-- Mapping section -->
					<section class="form-section">
						<h5 class="mb-1">Calibration settings</h5>
						<div class="input-row">
							<div class="label-and-input">
								<label>Start time</label>
								<tera-input-text disabled model-value="0" />
							</div>
							<div class="label-and-input">
								<label for="num-samples">End time</label>
								<tera-input-number inputId="integeronly" v-model="knobs.endTime" />
							</div>
						</div>
						<div class="spacer m-2" />
						<p class="mb-1">Preset (optional)</p>
						<div class="label-and-input">
							<Dropdown
								v-model="presetType"
								placeholder="Select an option"
								:options="[CiemssPresetTypes.Fast, CiemssPresetTypes.Normal]"
								@update:model-value="setPresetValues"
							/>
						</div>
						<label class="mb-1 p-text-secondary text-sm">
							<i class="pi pi-info-circle" />
							This impacts solver method, iterations and learning rate.
						</label>
						<div class="mt-1 additional-settings">
							<div class="label-and-input">
								<label>Number of Samples</label>
								<tera-input-number inputId="integeronly" v-model="knobs.numSamples" @update:model-value="updateState" />
							</div>
							<div class="spacer m-3" />

							<h6 class="mb-2">ODE solver options</h6>

							<div class="input-row">
								<div class="label-and-input">
									<label for="5">Solver method</label>
									<Dropdown
										id="5"
										v-model="knobs.method"
										:options="[CiemssMethodOptions.dopri5, CiemssMethodOptions.euler]"
										@update:model-value="updateState"
									/>
								</div>
								<div class="label-and-input">
									<label for="num-steps">Solver step size</label>
									<tera-input-number inputId="integeronly" v-model="knobs.stepSize" />
								</div>
							</div>
							<div class="spacer m-3" />
							<h6 class="mb-2">Inference Options</h6>
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
									<label for="learning-rate">Learning rate</label>
									<tera-input-number
										inputId="numberonly"
										v-model="knobs.learningRate"
										@update:model-value="updateState"
									/>
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
						</div>
					</section>

					<section v-if="interventionPolicy" class="form-section">
						<h5>Intervention Policies</h5>
						<tera-intervention-summary-card
							v-for="(intervention, index) in interventionPolicy.interventions"
							:intervention="intervention"
							:key="index"
						/>
					</section>
					<div class="spacer m-7" />
				</template>
			</tera-slider-panel>
		</section>

		<!-- Notebook section -->
		<section :tabName="DrilldownTabs.Notebook" class="notebook-section">
			<p class="m-3">The notebook is under construction.</p>
		</section>

		<!-- Output section -->
		<template #preview>
			<tera-drilldown-section v-if="showOutputSection">
				<template #header-controls-left v-if="configuredModelConfig?.name">
					<h5 class="ml-3">{{ configuredModelConfig.name }}</h5>
				</template>
				<template #header-controls-right>
					<Button
						label="Save for re-use"
						severity="secondary"
						class="mr-3"
						outlined
						:disabled="!configuredModelConfig"
						@click="showSaveModal = true"
					/>
				</template>
				<tera-operator-output-summary
					v-if="node.state.summaryId && !isLoading"
					class="p-3"
					:summary-id="node.state.summaryId"
				/>
				<Accordion :active-index="0" class="px-2" v-if="!isLoading">
					<AccordionTab header="Loss">
						<!-- Loss chart -->
						<div ref="lossChartContainer">
							<vega-chart
								expandable
								v-if="lossValues.length > 0 || isLoading"
								ref="lossChartRef"
								:are-embed-actions-visible="true"
								:visualization-spec="lossChartSpec"
							/>
						</div>
					</AccordionTab>
				</Accordion>
				<div v-if="!isLoading">
					<section ref="outputPanel" v-if="modelConfig && csvAsset">
						<Accordion multiple :active-index="[0, 1, 2]" class="px-2">
							<AccordionTab header="Parameters">
								<template v-for="setting of selectedParameterSettings" :key="setting.id">
									<vega-chart
										v-if="preparedDistributionCharts[setting.selectedVariables[0]]"
										expandable
										:are-embed-actions-visible="true"
										:visualization-spec="preparedDistributionCharts[setting.selectedVariables[0]].histogram"
									>
										<template v-slot:footer>
											<table class="distribution-table">
												<thead>
													<tr>
														<th scope="col"></th>
														<th scope="col">
															{{ preparedDistributionCharts[setting.selectedVariables[0]].header[0] }}
														</th>
														<th scope="col">
															{{ preparedDistributionCharts[setting.selectedVariables[0]].header[1] }}
														</th>
													</tr>
												</thead>
												<tbody>
													<tr>
														<th scope="row">Mean</th>
														<td>{{ preparedDistributionCharts[setting.selectedVariables[0]].mean[0] }}</td>
														<td>{{ preparedDistributionCharts[setting.selectedVariables[0]].mean[1] }}</td>
													</tr>
													<tr>
														<th scope="row">Variance</th>
														<td>{{ preparedDistributionCharts[setting.selectedVariables[0]].variance[0] }}</td>
														<td>{{ preparedDistributionCharts[setting.selectedVariables[0]].variance[1] }}</td>
													</tr>
												</tbody>
											</table>
										</template>
									</vega-chart>
								</template>
							</AccordionTab>
							<AccordionTab header="Variables">
								<template v-for="setting of selectedVariableSettings" :key="setting.id">
									<vega-chart
										expandable
										:are-embed-actions-visible="true"
										:visualization-spec="preparedCharts[setting.selectedVariables[0]]"
									/>
								</template>
							</AccordionTab>
							<AccordionTab header="Errors" v-if="errorData.length > 0 && selectedErrorVariableSettings.length > 0">
								<vega-chart
									:expandable="onExpandErrorChart"
									:are-embed-actions-visible="true"
									:visualization-spec="errorChart"
								/>
							</AccordionTab>
						</Accordion>
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
			<!-- Empty state if calibrate hasn't been run yet -->
			<section v-else class="output-section-empty-state">
				<Vue3Lottie :animationData="EmptySeed" :height="150" loop autoplay />
				<p class="helpMessage">Click 'Run' to begin calibrating</p>
			</section>
		</template>

		<template #sidebar-right>
			<tera-slider-panel
				v-model:is-open="isOutputSettingsPanelOpen"
				direction="right"
				class="input-config"
				header="Output Settings"
				content-width="360px"
			>
				<template #overlay>
					<tera-chart-settings-panel
						:annotations="activeChartSettings?.type === ChartSettingType.VARIABLE ? chartAnnotations : undefined"
						:active-settings="activeChartSettings"
						:generate-annotation="generateAnnotation"
						@delete-annotation="deleteAnnotation"
						@close="activeChartSettings = null"
					/>
				</template>
				<template #content>
					<div class="output-settings-panel">
						<tera-chart-settings
							:title="'Parameter distributions'"
							:settings="chartSettings"
							:type="ChartSettingType.DISTRIBUTION_COMPARISON"
							:select-options="Object.keys(pyciemssMap).filter((c) => modelPartTypesMap[c] === 'parameter')"
							:selected-options="selectedParameterSettings.map((s) => s.selectedVariables[0])"
							@open="activeChartSettings = $event"
							@remove="removeChartSetting"
							@selection-change="updateChartSettings"
						/>
						<hr />
						<tera-chart-settings
							:title="'Variables over time'"
							:settings="chartSettings"
							:type="ChartSettingType.VARIABLE"
							:select-options="
								Object.keys(pyciemssMap).filter((c) => ['state', 'observable'].includes(modelPartTypesMap[c]))
							"
							:selected-options="selectedVariableSettings.map((s) => s.selectedVariables[0])"
							@open="activeChartSettings = $event"
							@remove="removeChartSetting"
							@selection-change="updateChartSettings"
						/>
						<hr />
						<tera-chart-settings
							:title="'Error'"
							:settings="chartSettings"
							:type="ChartSettingType.ERROR_DISTRIBUTION"
							:select-options="Object.keys(pyciemssMap).filter((c) => mapping.find((d) => d.modelVariable === c))"
							:selected-options="selectedErrorVariableSettings.map((s) => s.selectedVariables[0])"
							@open="activeChartSettings = $event"
							@remove="removeChartSetting"
							@selection-change="updateChartSettings"
						/>
						<hr />
					</div>
				</template>
			</tera-slider-panel>
		</template>
	</tera-drilldown>
	<tera-save-simulation-modal
		:initial-name="configuredModelConfig?.name"
		:is-visible="showSaveModal"
		:assets="[
			{ id: configuredModelConfig?.id ?? '', type: AssetType.ModelConfiguration },
			{ id: outputDatasetId, type: AssetType.Dataset }
		]"
		@close-modal="showSaveModal = false"
		@on-save="onSaveAsModelConfiguration"
		:simulation-id="node.state.forecastId"
	/>
</template>

<script setup lang="ts">
import _ from 'lodash';
import * as vega from 'vega';
import { csvParse, autoType, mean, variance } from 'd3';
import { computed, onMounted, ref, shallowRef, watch } from 'vue';
import { useConfirm } from 'primevue/useconfirm';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Dropdown from 'primevue/dropdown';
import Column from 'primevue/column';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import { CalibrateMap, setupDatasetInput, setupModelInput } from '@/services/calibrate-workflow';
import {
	deleteAnnotation,
	fetchAnnotations,
	generateForecastChartAnnotation,
	removeChartSettingById,
	saveAnnotation,
	updateChartSettingsBySelectedVariables
} from '@/services/chart-settings';
import { Vue3Lottie } from 'vue3-lottie';
import EmptySeed from '@/assets/images/lottie-empty-seed.json';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraNotebookError from '@/components/drilldown/tera-notebook-error.vue';
import TeraOperatorOutputSummary from '@/components/operator/tera-operator-output-summary.vue';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraChartSettings from '@/components/widgets/tera-chart-settings.vue';
import TeraChartSettingsPanel from '@/components/widgets/tera-chart-settings-panel.vue';
import {
	CalibrationRequestCiemss,
	ClientEvent,
	ClientEventType,
	CsvAsset,
	DatasetColumn,
	ModelConfiguration,
	ChartAnnotation,
	InterventionPolicy,
	ModelParameter,
	AssetType
} from '@/types/Types';
import { CiemssPresetTypes, DrilldownTabs, ChartSetting, ChartSettingType } from '@/types/common';
import { getTimespan, nodeMetadata } from '@/components/workflow/util';
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
import {
	createForecastChart,
	createHistogramChart,
	createErrorChart,
	applyForecastChartAnnotations,
	createInterventionChartMarkers
} from '@/services/charts';
import VegaChart from '@/components/widgets/VegaChart.vue';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import { displayNumber } from '@/utils/number';
import TeraPyciemssCancelButton from '@/components/pyciemss/tera-pyciemss-cancel-button.vue';
import TeraSaveSimulationModal from '@/components/project/tera-save-simulation-modal.vue';
import { useClientEvent } from '@/composables/useClientEvent';
import { useDrilldownChartSize } from '@/composables/useDrilldownChartSize';
import { flattenInterventionData, getInterventionPolicyById } from '@/services/intervention-policy';
import TeraInterventionSummaryCard from '@/components/intervention-policy/tera-intervention-summary-card.vue';
import { getParameters } from '@/model-representation/service';
import type { CalibrationOperationStateCiemss } from './calibrate-operation';
import { renameFnGenerator, mergeResults, getErrorData } from './calibrate-utils';

const isSidebarOpen = ref(true);

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateCiemss>;
}>();
const emit = defineEmits(['close', 'select-output', 'update-state']);
const toast = useToastService();
const confirm = useConfirm();

interface BasicKnobs {
	numIterations: number;
	numSamples: number;
	endTime: number;
	stepSize: number;
	learningRate: number;
	method: string;
	timestampColName: string;
}

const knobs = ref<BasicKnobs>({
	numIterations: props.node.state.numIterations ?? 1000,
	numSamples: props.node.state.numSamples ?? 100,
	endTime: props.node.state.endTime ?? 100,
	stepSize: props.node.state.stepSize ?? 1,
	learningRate: props.node.state.learningRate ?? 0.1,
	method: props.node.state.method ?? CiemssMethodOptions.dopri5,
	timestampColName: props.node.state.timestampColName ?? ''
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

const outputDatasetId = computed(() => {
	if (!selectedOutputId.value) return '';
	const output = props.node.outputs.find((o) => o.id === selectedOutputId.value);
	return output?.value?.[0]?.datasetId ?? '';
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

// Model variables checked in the model configuration will be options in the mapping dropdown
const modelStateOptions = ref<any[] | undefined>();

const modelParameters = ref<ModelParameter[]>([]);

const isOutputSettingsPanelOpen = ref(false);
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
const policyInterventionId = computed(() => props.node.inputs[2].value?.[0]);
const interventionPolicy = ref<InterventionPolicy | null>(null);

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

const isLoading = ref(false);

const mapping = ref<CalibrateMap[]>(props.node.state.mapping);

const mappingDropdownPlaceholder = computed(() => {
	if (!_.isEmpty(modelStateOptions.value) && !_.isEmpty(datasetColumns.value)) return 'Select variable';
	return 'Please wait...';
});

const showOutputSection = computed(
	() =>
		lossValues.value.length > 0 ||
		selectedErrorVariableSettings.value.length > 0 ||
		selectedVariableSettings.value.length > 0 ||
		selectedParameterSettings.value.length > 0 ||
		isLoading.value ||
		!_.isEmpty(props.node.state?.errorMessage?.traceback)
);

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

// reset drilldown state
const resetState = () => {
	confirm.require({
		header: 'Reset to original calibration state',
		message: 'Are you sure you want to reset the state?',
		accept: () => {
			// Restore to the original output port state
			const outputPort = props.node.outputs.find((output) => output.id === selectedOutputId.value);
			if (outputPort?.state) {
				knobs.value = _.cloneDeep(outputPort.state as CalibrationOperationStateCiemss);
				mapping.value = outputPort.state.mapping as CalibrateMap[];
			}
		}
	});
};

const disableRunButton = computed(
	() =>
		!currentDatasetFileName.value ||
		!csvAsset.value ||
		!modelConfigId.value ||
		!datasetId.value ||
		knobs.value.timestampColName === ''
);

const selectedOutputId = ref<string>();
const lossChartContainer = ref(null);
const lossChartSize = useDrilldownChartSize(lossChartContainer);
const outputPanel = ref(null);
const chartSize = useDrilldownChartSize(outputPanel);

const chartSettings = computed(() => props.node.state.chartSettings ?? []);
const selectedParameterSettings = computed(() =>
	chartSettings.value.filter((setting) => setting.type === ChartSettingType.DISTRIBUTION_COMPARISON)
);
const selectedVariableSettings = computed(() =>
	chartSettings.value.filter((setting) => setting.type === ChartSettingType.VARIABLE)
);

const selectedErrorVariableSettings = computed(() =>
	chartSettings.value.filter((setting) => setting.type === ChartSettingType.ERROR_DISTRIBUTION)
);

// --- Handle chart annotations
const chartAnnotations = ref<ChartAnnotation[]>([]);
const updateChartAnnotations = async () => {
	chartAnnotations.value = await fetchAnnotations(props.node.id);
};
onMounted(() => updateChartAnnotations());
useClientEvent([ClientEventType.ChartAnnotationCreate, ClientEventType.ChartAnnotationDelete], updateChartAnnotations);

const generateAnnotation = async (setting: ChartSetting, query: string) => {
	// Note: Currently llm generated chart annotations are supported for the forecast chart only
	if (!preparedChartInputs.value) return null;
	const { reverseMap } = preparedChartInputs.value;
	const variable = setting.selectedVariables[0];
	const annotationLayerSpec = await generateForecastChartAnnotation(
		query,
		'timepoint_id',
		[`${pyciemssMap.value[variable]}_mean:pre`, `${pyciemssMap.value[variable]}_mean`],
		{
			translationMap: reverseMap,
			xAxisTitle: modelVarUnits.value._time || 'Time',
			yAxisTitle: modelVarUnits.value[variable] || ''
		}
	);
	const saved = await saveAnnotation(annotationLayerSpec, props.node.id, setting.id);
	return saved;
};
// ---

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

const groupedInterventionOutputs = computed(() =>
	_.groupBy(flattenInterventionData(interventionPolicy.value?.interventions ?? []), 'appliedTo')
);

const preparedCharts = computed(() => {
	if (!preparedChartInputs.value) return {};
	const { result, resultSummary, reverseMap } = preparedChartInputs.value;
	const state = props.node.state;

	// Need to get the dataset's time field
	const datasetTimeField = knobs.value.timestampColName;

	const charts = {};
	selectedVariableSettings.value.forEach((settings) => {
		const variable = settings.selectedVariables[0];
		const annotations = chartAnnotations.value.filter((annotation) => annotation.chartId === settings.id);
		const datasetVariables: string[] = [];
		const mapObj = state.mapping.find((d) => d.modelVariable === variable);
		if (mapObj) {
			datasetVariables.push(mapObj.datasetVariable);
		}
		charts[variable] = applyForecastChartAnnotations(
			createForecastChart(
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
			),
			annotations
		);

		charts[variable].layer.push(...createInterventionChartMarkers(groupedInterventionOutputs.value[variable]));
	});
	return charts;
});

const preparedDistributionCharts = computed(() => {
	if (!preparedChartInputs.value || _.isEmpty(pyciemssMap.value)) return {};
	const { result } = preparedChartInputs.value;
	// Note that we want to show the parameter distribution at the first timepoint only
	const data = result.filter((d) => d.timepoint_id === 0);
	const labelBefore = 'Before calibration';
	const labelAfter = 'After calibration';
	const charts = {};
	selectedParameterSettings.value.forEach((setting) => {
		const param = setting.selectedVariables[0];
		const fieldName = pyciemssMap.value[param];
		const beforeFieldName = `${fieldName}:pre`;
		const histogram = createHistogramChart(data, {
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
			mean: [mean(data, (d) => d[beforeFieldName]), mean(data, (d) => d[fieldName])].map(toDisplayNumber),
			variance: [variance(data, (d) => d[beforeFieldName]), variance(data, (d) => d[fieldName])].map(toDisplayNumber)
		};
		charts[param] = { histogram, ...stat };
	});
	return charts;
});

const errorChartVariables = computed(() => {
	if (!selectedErrorVariableSettings.value.length) return [];
	const getDatasetVariable = (modelVariable: string) =>
		mapping.value.find((d) => d.modelVariable === modelVariable)?.datasetVariable;
	const variables = selectedErrorVariableSettings.value
		.map((s) => s.selectedVariables[0])
		.map((variable) => ({
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
const updateLossChartSpec = (data: string | Record<string, any>[], size: { width: number; height: number }) => {
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
			width: size.width,
			height: 100,
			xAxisTitle: 'Solver iterations',
			yAxisTitle: 'Loss'
		}
	);
};

const initDefaultChartSettings = (state: CalibrationOperationStateCiemss) => {
	// Initialize default selected chart settings when chart settings are not set yet. Return if chart settings are already set.
	if (Array.isArray(state.chartSettings)) return;
	const defaultSelectedParam = modelParameters.value.filter((p) => !!p.distribution).map((p) => p.id);
	const mappedModelVariables = mapping.value
		.filter((c) => ['state', 'observable'].includes(modelPartTypesMap.value[c.modelVariable]))
		.map((c) => c.modelVariable);
	state.chartSettings = updateChartSettingsBySelectedVariables([], ChartSettingType.VARIABLE, mappedModelVariables);
	state.chartSettings = updateChartSettingsBySelectedVariables(
		state.chartSettings,
		ChartSettingType.ERROR_DISTRIBUTION,
		mappedModelVariables
	);
	state.chartSettings = updateChartSettingsBySelectedVariables(
		state.chartSettings,
		ChartSettingType.DISTRIBUTION_COMPARISON,
		defaultSelectedParam
	);
};

const runCalibrate = async () => {
	if (!modelConfigId.value || !datasetId.value || !currentDatasetFileName.value) return;

	const formattedMap: { [index: string]: string } = {};
	formattedMap[knobs.value.timestampColName] = 'timestamp';
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

	if (policyInterventionId.value) {
		calibrationRequest.policyInterventionId = policyInterventionId.value;
	}

	const response = await makeCalibrateJobCiemss(calibrationRequest, nodeMetadata(props.node));

	if (response?.simulationId) {
		state.inProgressCalibrationId = response?.simulationId;
		state.currentProgress = 0;
		state.inProgressForecastId = '';
		state.inProgressPreForecastId = '';
		initDefaultChartSettings(state);
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

function updateChartSettings(selectedVariables: string[], type: ChartSettingType) {
	emit('update-state', {
		...props.node.state,
		chartSettings: updateChartSettingsBySelectedVariables(chartSettings.value, type, selectedVariables)
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

function deleteAllMappings() {
	mapping.value = [];

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
	const { model, modelConfiguration, modelOptions, modelPartUnits, modelPartTypes } = await setupModelInput(
		modelConfigId.value
	);
	modelConfig.value = modelConfiguration;
	modelStateOptions.value = modelOptions;
	modelParameters.value = model ? getParameters(model) : [];
	modelVarUnits.value = modelPartUnits ?? {};
	modelPartTypesMap.value = modelPartTypes ?? {};

	// dataset input
	const { filename, csv, datasetOptions } = await setupDatasetInput(datasetId.value);
	currentDatasetFileName.value = filename;
	csvAsset.value = csv;
	datasetColumns.value = datasetOptions;

	getConfiguredModelConfig();

	// look for timestamp col in dataset if its not yet filled in.
	if (knobs.value.timestampColName === '') {
		const timeCol = datasetColumns.value?.find((ele) => ele.name.toLocaleLowerCase().startsWith('time'));
		if (timeCol) {
			knobs.value.timestampColName = timeCol.name;
		}
	}
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
		state.timestampColName = knobs.value.timestampColName;
		emit('update-state', state);
	},
	{ deep: true }
);

watch(
	[() => props.node.state.inProgressCalibrationId, lossChartSize],
	([id, size]) => {
		if (id === '') {
			isLoading.value = false;
			updateLossChartSpec(lossValues.value, size);
			unsubscribeToUpdateMessages([id], ClientEventType.SimulationPyciemss, messageHandler);
		} else {
			isLoading.value = true;
			updateLossChartSpec(LOSS_CHART_DATA_SOURCE, size);
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
				updateLossChartSpec(lossValues.value, lossChartSize.value);
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

			errorData.value = getErrorData(
				groundTruthData.value,
				runResult.value,
				mapping.value,
				knobs.value.timestampColName
			);
		}
	},
	{ immediate: true }
);

watch(
	() => policyInterventionId.value,
	() => {
		if (policyInterventionId.value) {
			getInterventionPolicyById(policyInterventionId.value).then((policy) => {
				interventionPolicy.value = policy;
			});
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
.wizard .toolbar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: var(--gap-1) var(--gap);
	gap: var(--gap-2);
}

/* In Wizard mode, override grid template so output expands when sidebar is closed */
.overlay-container:deep(section.scale main) {
	grid-template-columns: auto 1fr;
}

/* Notebook */
.notebook-section {
	width: calc(50vw - 4rem);
	background: var(--surface-100);
	border-right: 1px solid var(--surface-border-light);
}

/* Mapping table */
.mapping-table:deep(td) {
	border: none !important;
	padding: 0 var(--gap-2) var(--gap-2) 0 !important;
	background: var(--surface-100);
}

.mapping-table:deep(th) {
	border: none !important;
	padding: 0 var(--gap-1) var(--gap-2) 0 !important;
	width: 50%;
	background: var(--surface-100);
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
	display: flex;
	flex-direction: column;
	flex-grow: 1;
	gap: var(--gap-1);
	padding: var(--gap);
}

.label-and-input {
	display: flex;
	flex-direction: column;
	gap: var(--gap-1);

	:deep(input) {
		text-align: left;
	}
}
.info-circle {
	color: var(--text-color-secondary);
	font-size: var(--font-caption);
	margin-left: var(--gap-1);
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

/** Make inputs align with mapping table */
.mapping-input {
	width: calc(100% - 40px);
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

.output-section-empty-state {
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: var(--gap);
	text-align: center;
	pointer-events: none;
}

.additional-settings {
	background: var(--surface-200);
	padding: var(--gap-3);
	border-radius: var(--border-radius-medium);
}

input {
	text-align: left;
}
</style>
