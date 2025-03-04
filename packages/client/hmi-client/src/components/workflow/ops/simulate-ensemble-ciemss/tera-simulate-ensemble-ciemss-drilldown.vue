<template>
	<tera-drilldown
		v-bind="$attrs"
		:node="node"
		@update:selection="onSelection"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
		class="drilldown"
	>
		<tera-drilldown-section :tabName="DrilldownTabs.Wizard" class="input-config">
			<tera-slider-panel
				class="input-config"
				v-model:is-open="isSidebarOpen"
				header="Simulation ensemble settings"
				content-width="420px"
			>
				<template #content>
					<div class="toolbar">
						<p>Click Run to start the simulation.</p>
						<span class="flex gap-2">
							<tera-pyciemss-cancel-button class="mr-auto" :simulation-run-id="cancelRunId" />
							<Button label="Run" icon="pi pi-play" @click="runEnsemble" :disabled="false" />
						</span>
					</div>

					<Accordion :multiple="true" :active-index="activeAccordionIndicies">
						<!-- Mapping -->
						<AccordionTab header="Mapping">
							<p class="subheader">All variables mapped should be normalized (could use observables)</p>
							<template v-if="knobs.mapping.length > 0">
								<table class="w-full mb-2">
									<tbody>
										<tr>
											<th>Ensemble variables</th>
											<th v-for="(element, i) in listModelLabels" :key="i">
												{{ element }}
											</th>
										</tr>
										<tr v-for="(ele, indx) in knobs.mapping" :key="indx">
											<td>
												<tera-input-text v-model="ele.newName" auto-focus class="w-full" placeholder="Add a name" />
											</td>
											<td v-for="key in modelConfigurationIds" :key="key">
												<Dropdown
													class="w-full"
													:options="allModelOptions[key]"
													v-model="ele.modelConfigurationMappings[key]"
													placeholder="Select"
													@change="updateMapping()"
												/>
											</td>
											<td>
												<Button class="p-button-sm" icon="pi pi-times" rounded text @click="deleteMappingRow(ele.id)" />
											</td>
										</tr>
									</tbody>
								</table>
							</template>
							<section>
								<Button
									v-if="!showAddMappingInput"
									outlined
									:style="{ marginRight: 'auto' }"
									label="Add mapping"
									size="small"
									severity="secondary"
									icon="pi pi-plus"
									@click="
										newSolutionMappingKey = '';
										showAddMappingInput = true;
									"
								/>
								<div v-if="showAddMappingInput" class="flex">
									<tera-input-text
										v-model="newSolutionMappingKey"
										auto-focus
										class="w-full"
										placeholder="Add a name"
										@keydown.enter.stop.prevent="
											addMapping();
											showAddMappingInput = false;
										"
									/>
									<span class="flex gap-2">
										<Button
											class="p-button-sm p-button-outlined ml-2"
											severity="secondary"
											icon="pi pi-times"
											label="Cancel"
											@click="
												newSolutionMappingKey = '';
												showAddMappingInput = false;
											"
										/>
										<Button
											:disabled="!newSolutionMappingKey"
											class="p-button-sm p-button-outlined ml-2"
											icon="pi pi-check"
											label="Add"
											@click="
												addMapping();
												showAddMappingInput = false;
											"
										/>
									</span>
								</div>
							</section>
						</AccordionTab>

						<!-- Model weights -->
						<AccordionTab header="Model weights">
							<p class="subheader">
								This encodes your relative confidence for each model. These are the alpha parameters of a Dirichlet
								distribution.
							</p>
							<div v-if="!_.isEmpty(modelConfigIdToNameMap) && !_.isEmpty(knobs.weights)" class="model-weights">
								<table class="p-datatable-table">
									<tbody class="p-datatable-tbody">
										<tr v-for="key in modelConfigurationIds" :key="key">
											<td>
												{{ modelConfigIdToNameMap[key] }}
											</td>
											<td>
												<tera-signal-bars
													label="Relative certainty"
													:min-option="1"
													v-model="knobs.weights[key]"
													@change="updateWeights()"
												/>
											</td>
										</tr>
									</tbody>
								</table>
							</div>
						</AccordionTab>

						<!-- Other Settings -->
						<AccordionTab header="Other Settings">
							<div class="form-section" v-if="isSidebarOpen">
								<div class="input-row">
									<div class="label-and-input">
										<label>Start time</label>
										<tera-input-number class="w-12" disabled :model-value="0" />
									</div>
									<div class="label-and-input">
										<label>End time</label>
										<tera-input-number v-model="knobs.endTime" />
									</div>
								</div>
								<!-- Presets -->
								<div class="input-row">
									<div class="label-and-input">
										<label>Preset (optional)</label>
										<Dropdown
											v-model="presetType"
											placeholder="Select an option"
											:options="[CiemssPresetTypes.Fast, CiemssPresetTypes.Normal]"
											@update:model-value="setPresetValues"
										/>
									</div>
									<div class="label-and-input">
										<tera-checkbox
											label="Number of timepoints"
											:model-value="knobs.isNumberOfTimepointsManual"
											@update:model-value="toggleIsNumberOfTimepointsManual"
										/>
										<tera-input-number
											:disabled="!knobs.isNumberOfTimepointsManual"
											v-model="knobs.numberOfTimepoints"
											inputId="integeronly"
											:min="1"
										/>
									</div>
								</div>
								<!-- Number of Samples & Method -->
								<div class="input-row">
									<div class="label-and-input">
										<label>Number of samples</label>
										<tera-input-number v-model="knobs.numSamples" inputId="integeronly" :min="1" />
									</div>
									<div class="label-and-input">
										<label>Solver method</label>
										<Dropdown
											v-model="knobs.method"
											:options="[CiemssMethodOptions.dopri5, CiemssMethodOptions.rk4, CiemssMethodOptions.euler]"
										/>
									</div>
									<div class="label-and-input">
										<label>Solver step size</label>
										<tera-input-number
											:disabled="![CiemssMethodOptions.rk4, CiemssMethodOptions.euler].includes(knobs.method)"
											:min="0"
											v-model="knobs.stepSize"
										/>
									</div>
								</div>
							</div>
						</AccordionTab>
					</Accordion>
				</template>
			</tera-slider-panel>
		</tera-drilldown-section>

		<tera-drilldown-section :tabName="DrilldownTabs.Notebook" class="input-config">
			<tera-slider-panel
				class="input-config"
				v-model:is-open="isSidebarOpen"
				header="Simulation ensemble settings"
				content-width="420px"
			>
				<template #content>
					<div class="mt-3 ml-4 mr-2">Under construction.</div>
				</template>
			</tera-slider-panel>
		</tera-drilldown-section>
		<!-- Output Section -->
		<template #preview>
			<tera-drilldown-section
				title="Simulation output"
				:options="outputs"
				v-model:output="selectedOutputId"
				is-selectable
				:is-loading="isLoading"
				@update:selection="onSelection"
			>
				<template #header-controls-right>
					<Button class="mr-3" label="Save for re-use" severity="secondary" outlined @click="showSaveDataset = true" />
				</template>
				<tera-notebook-error v-if="!_.isEmpty(node.state?.errorMessage?.traceback)" v-bind="node.state.errorMessage" />
				<section ref="outputPanel">
					<div class="mx-2" ref="chartWidthDiv"></div>
					<Accordion :active-index="0">
						<template v-if="!isLoading">
							<AccordionTab header="Ensemble variables over time">
								<div class="flex flex-row" v-for="setting of selectedEnsembleVariableSettings" :key="setting.id">
									<vega-chart
										v-for="(spec, index) of ensembleVariableCharts[setting.id]"
										:key="index"
										expandable
										:are-embed-actions-visible="true"
										:visualization-spec="spec"
									/>
								</div>
							</AccordionTab>
						</template>
					</Accordion>
				</section>
			</tera-drilldown-section>
		</template>
		<!-- Output Settings Section -->
		<template #sidebar-right>
			<tera-slider-panel
				v-model:is-open="isOutputSettingsPanelOpen"
				direction="right"
				class="input-config"
				header="Output settings"
				content-width="360px"
			>
				<template #overlay>
					<tera-chart-settings-panel
						:annotations="
							[ChartSettingType.VARIABLE_ENSEMBLE].includes(activeChartSettings?.type as ChartSettingType)
								? getChartAnnotationsByChartId(activeChartSettings?.id ?? '')
								: undefined
						"
						:active-settings="activeChartSettings"
						:generate-annotation="generateAnnotation"
						:get-chart-labels="getChartLabels"
						@update-settings="updateActiveChartSettings"
						@delete-annotation="deleteAnnotation"
						@close="setActiveChartSettings(null)"
					/>
				</template>
				<template #content>
					<div class="output-settings-panel">
						<tera-chart-settings
							:title="'Ensemble variables over time'"
							:settings="chartSettings"
							:type="ChartSettingType.VARIABLE_ENSEMBLE"
							:select-options="ensembleVariables"
							:selected-options="selectedEnsembleVariableSettings.map((s) => s.selectedVariables[0])"
							@open="setActiveChartSettings($event)"
							@remove="removeChartSettings"
							@selection-change="updateChartSettings"
							@toggle-ensemble-variable-setting-option="updateEnsembleVariableSettingOption"
						/>
					</div>
				</template>
			</tera-slider-panel>
		</template>
	</tera-drilldown>
	<tera-save-simulation-modal
		:is-visible="showSaveDataset"
		@close-modal="showSaveDataset = false"
		:simulation-id="node.state.forecastId"
		:assets="[{ id: selectedRunId, type: AssetType.Dataset }]"
	/>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, computed, watch, onMounted } from 'vue';
import Button from 'primevue/button';
import AccordionTab from 'primevue/accordiontab';
import Accordion from 'primevue/accordion';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import Dropdown from 'primevue/dropdown';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraPyciemssCancelButton from '@/components/pyciemss/tera-pyciemss-cancel-button.vue';
import teraCheckbox from '@/components/widgets/tera-checkbox.vue';
import {
	makeEnsembleCiemssSimulation,
	CiemssMethodOptions,
	getRunResultCSV,
	parseEnsemblePyciemssMap,
	getEnsembleResultModelConfigMap
} from '@/services/models/simulation-service';
import { updateChartSettingsBySelectedVariables } from '@/services/chart-settings';
import { deleteAnnotation } from '@/services/chart-annotation';
import { getModelConfigurationById, getObservables, getInitials } from '@/services/model-configurations';
import { nodeMetadata } from '@/components/workflow/util';
import type { WorkflowNode } from '@/types/workflow';
import { AssetType, ModelConfiguration, type EnsembleSimulationCiemssRequest } from '@/types/Types';
import { DrilldownTabs, CiemssPresetTypes, ChartSettingType } from '@/types/common';
import TeraNotebookError from '@/components/drilldown/tera-notebook-error.vue';
import TeraSignalBars from '@/components/widgets/tera-signal-bars.vue';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraSaveSimulationModal from '@/components/project/tera-save-simulation-modal.vue';
import { v4 as uuidv4 } from 'uuid';
import { useChartSettings } from '@/composables/useChartSettings';
import { useCharts } from '@/composables/useCharts';
import { useDrilldownChartSize } from '@/composables/useDrilldownChartSize';
import { DataArray } from '@/utils/stats';
import VegaChart from '@/components/widgets/VegaChart.vue';
import teraChartSettings from '@/components/widgets/tera-chart-settings.vue';
import TeraChartSettingsPanel from '@/components/widgets/tera-chart-settings-panel.vue';
import {
	formatSimulateModelConfigurations,
	getChartEnsembleMapping,
	usePreparedChartInputs
} from './simulate-ensemble-util';
import {
	SimulateEnsembleCiemssOperationState,
	SimulateEnsembleMappingRow,
	SimulateEnsembleWeights,
	speedValues,
	normalValues
} from './simulate-ensemble-ciemss-operation';
import { setStateToModelConfigMap } from '../calibrate-ensemble-ciemss/calibrate-ensemble-util';

const props = defineProps<{
	node: WorkflowNode<SimulateEnsembleCiemssOperationState>;
}>();

const emit = defineEmits(['select-output', 'update-state', 'close']);

interface BasicKnobs {
	mapping: SimulateEnsembleMappingRow[];
	weights: SimulateEnsembleWeights;
	numSamples: number;
	method: CiemssMethodOptions;
	stepSize: number;
	endTime: number;
	isNumberOfTimepointsManual: boolean;
	numberOfTimepoints: number;
}

const knobs = ref<BasicKnobs>({
	mapping: props.node.state.mapping,
	weights: props.node.state.weights,
	numSamples: props.node.state.numSamples,
	method: props.node.state.method,
	stepSize: props.node.state.stepSize,
	endTime: props.node.state.endTime,
	isNumberOfTimepointsManual: props.node.state.isNumberOfTimepointsManual,
	numberOfTimepoints: props.node.state.numberOfTimepoints
});

const activeAccordionIndicies = ref([0, 1, 2]);
const isSidebarOpen = ref(true);
const isOutputSettingsPanelOpen = ref(false);
const isLoading = ref(false);
const showSaveDataset = ref(false);
const showAddMappingInput = ref(false);
const listModelLabels = ref<string[]>([]);

const presetType = computed(() => {
	if (
		knobs.value.numSamples === speedValues.numSamples &&
		knobs.value.method === speedValues.method &&
		knobs.value.stepSize === speedValues.stepSize
	) {
		return CiemssPresetTypes.Fast;
	}
	if (knobs.value.numSamples === normalValues.numSamples && knobs.value.method === normalValues.method) {
		return CiemssPresetTypes.Normal;
	}
	return '';
});

// List of each observible + state for each model.
const allModelOptions = ref<{ [key: string]: string[] }>({});
const modelConfigurationIds: string[] = props.node.inputs.map((ele) => ele.value?.[0]).filter(Boolean);
const modelConfigIdToNameMap = ref<Record<string, string>>({});
const allModelConfigurations = ref<ModelConfiguration[]>([]);

const newSolutionMappingKey = ref<string>('');
const runResults = ref<{ [runId: string]: DataArray }>({});
const runResultsSummary = ref<{ [runId: string]: DataArray }>({});
const cancelRunId = computed(() => props.node.state.inProgressForecastId);
// -------------- Charts && chart settings ----------------
const chartWidthDiv = ref(null);
const pyciemssMap = ref<Record<string, string>>({});
const chartSize = useDrilldownChartSize(chartWidthDiv);
const stateToModelConfigMap = ref<{ [key: string]: string[] }>({});
const selectedOutputMapping = computed(() => getChartEnsembleMapping(props.node, stateToModelConfigMap.value));
const preparedChartInputs = usePreparedChartInputs(props, runResults, runResultsSummary, pyciemssMap);
const ensembleVariables = computed(() =>
	getChartEnsembleMapping(props.node, stateToModelConfigMap.value).map((d) => d.newName)
);

const {
	activeChartSettings,
	chartSettings,
	removeChartSettings,
	updateChartSettings,
	selectedEnsembleVariableSettings,
	updateEnsembleVariableSettingOption,
	updateActiveChartSettings,
	setActiveChartSettings
} = useChartSettings(props, emit);

const { generateAnnotation, getChartLabels, getChartAnnotationsByChartId, useEnsembleVariableCharts } = useCharts(
	props.node.id,
	null,
	allModelConfigurations,
	preparedChartInputs,
	chartSize,
	null,
	selectedOutputMapping
);

const ensembleVariableCharts = useEnsembleVariableCharts(selectedEnsembleVariableSettings, null);

// Preview selection
const outputs = computed(() => {
	if (!_.isEmpty(props.node.outputs)) {
		return [
			{
				label: 'Select outputs',
				items: props.node.outputs
			}
		];
	}
	return [];
});
const selectedOutputId = ref<string>();
const selectedRunId = ref<string>('');
const outputPanel = ref(null);

const onSelection = (id: string) => {
	emit('select-output', id);
};

const setPresetValues = (data: CiemssPresetTypes) => {
	if (data === CiemssPresetTypes.Normal) {
		knobs.value.numSamples = normalValues.numSamples;
		knobs.value.method = normalValues.method;
	}
	if (data === CiemssPresetTypes.Fast) {
		knobs.value.numSamples = speedValues.numSamples;
		knobs.value.method = speedValues.method;
		knobs.value.stepSize = speedValues.stepSize;
	}
};

const toggleIsNumberOfTimepointsManual = () => {
	knobs.value.isNumberOfTimepointsManual = !knobs.value.isNumberOfTimepointsManual;
};

const addMapping = () => {
	// create empty configuration mappings
	const configMappings = {};
	modelConfigurationIds.forEach((id) => {
		configMappings[id as string] = '';
	});

	knobs.value.mapping.push({
		id: uuidv4(),
		newName: newSolutionMappingKey.value,
		modelConfigurationMappings: configMappings
	});

	const state = _.cloneDeep(props.node.state);
	state.mapping = knobs.value.mapping;
	emit('update-state', state);
};

const deleteMappingRow = (id: string) => {
	knobs.value.mapping = knobs.value.mapping.filter((ele) => ele.id !== id);
	const state = _.cloneDeep(props.node.state);
	state.mapping = knobs.value.mapping;
	emit('update-state', state);
};

const updateMapping = () => {
	const state = _.cloneDeep(props.node.state);
	state.mapping = knobs.value.mapping;
	emit('update-state', state);
};

const updateWeights = () => {
	const state = _.cloneDeep(props.node.state);
	state.weights = knobs.value.weights;
	emit('update-state', state);
};

function initDefaultChartSettings(state: SimulateEnsembleCiemssOperationState) {
	const mappedEnsembleVariables = state.mapping.map((ele) => ele.newName);
	if (_.isEmpty(state.chartSettings)) {
		state.chartSettings = updateChartSettingsBySelectedVariables(
			state.chartSettings ?? [],
			ChartSettingType.VARIABLE_ENSEMBLE,
			mappedEnsembleVariables
		);
	}
}

const runEnsemble = async () => {
	const modelConfigs = formatSimulateModelConfigurations(knobs.value.mapping, knobs.value.weights);
	const params: EnsembleSimulationCiemssRequest = {
		modelConfigs,
		timespan: {
			start: 0,
			end: knobs.value.endTime
		},
		loggingStepSize: knobs.value.endTime / knobs.value.numberOfTimepoints,
		engine: 'ciemss',
		extra: {
			num_samples: knobs.value.numSamples,
			solver_method: knobs.value.method,
			solver_step_size: knobs.value.stepSize
		}
	};
	const response = await makeEnsembleCiemssSimulation(params, nodeMetadata(props.node));

	const state = _.cloneDeep(props.node.state);
	initDefaultChartSettings(state);
	state.inProgressForecastId = response.simulationId;

	emit('update-state', state);
};

onMounted(async () => {
	if (!modelConfigurationIds) return;
	allModelConfigurations.value = await Promise.all(modelConfigurationIds.map((id) => getModelConfigurationById(id)));

	modelConfigIdToNameMap.value = {};
	allModelConfigurations.value.forEach((config) => {
		modelConfigIdToNameMap.value[config.id as string] = config.name as string;
	});

	allModelOptions.value = {};
	for (let i = 0; i < allModelConfigurations.value.length; i++) {
		const tempList: string[] = [];
		getInitials(allModelConfigurations.value[i]).forEach((element) => tempList.push(element.target));
		getObservables(allModelConfigurations.value[i]).forEach((element) => tempList.push(element.referenceId));
		allModelOptions.value[allModelConfigurations.value[i].id as string] = tempList;
	}
	listModelLabels.value = allModelConfigurations.value.map((ele) => ele.name ?? '');

	// initialze weights
	if (_.isEmpty(knobs.value.weights)) {
		allModelConfigurations.value.forEach((config) => {
			knobs.value.weights[config.id as string] = 1;
		});
	}
});

watch(
	() => props.node.state.inProgressForecastId,
	(id) => {
		if (id === '') isLoading.value = false;
		else isLoading.value = true;
	}
);

watch(
	() => props.node.active,
	async () => {
		const output = props.node.outputs.find((d) => d.id === props.node.active);
		if (!output || !output.value) return;

		selectedOutputId.value = output.id;
		selectedRunId.value = output.value[0];
		const forecastId = props.node.state.forecastId;
		if (!forecastId) return;

		const [result, resultSummary] = await Promise.all([
			getRunResultCSV(forecastId, 'result.csv'),
			getRunResultCSV(forecastId, 'result_summary.csv')
		]);
		const ensembleVarModelConfigMap = (await getEnsembleResultModelConfigMap(forecastId)) ?? {};
		pyciemssMap.value = parseEnsemblePyciemssMap(result[0], ensembleVarModelConfigMap);
		runResults.value[selectedRunId.value] = result;
		runResultsSummary.value[selectedRunId.value] = resultSummary;
		stateToModelConfigMap.value = await setStateToModelConfigMap(modelConfigurationIds);
	},
	{ immediate: true }
);

watch(
	() => knobs.value,
	() => {
		const state = _.cloneDeep(props.node.state);
		state.mapping = knobs.value.mapping;
		state.weights = knobs.value.weights;
		state.endTime = knobs.value.endTime;
		state.numSamples = knobs.value.numSamples;
		state.method = knobs.value.method;
		state.stepSize = knobs.value.stepSize;
		state.isNumberOfTimepointsManual = knobs.value.isNumberOfTimepointsManual;
		if (!knobs.value.isNumberOfTimepointsManual) {
			knobs.value.numberOfTimepoints = knobs.value.endTime;
		}
		state.numberOfTimepoints = knobs.value.numberOfTimepoints;
		emit('update-state', state);
	},
	{ deep: true }
);
</script>

<style scoped>
.output-settings-panel {
	padding: var(--gap-4);
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);
}

.toolbar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: var(--gap-1) var(--gap-4);
	gap: var(--gap-2);
}

.form-section {
	display: flex;
	flex-direction: column;
	flex-grow: 1;
	gap: var(--gap-4);
	padding: var(--gap-4);
}

.label-and-input {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	margin-bottom: var(--gap-4);
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

.input-config:deep(.content-wrapper) {
	padding-bottom: 0;
}

/* Override grid template so output expands when sidebar is closed */
.overlay-container:deep(section.drilldown main) {
	grid-template-columns: auto 1fr;
}

/* Override top and bottom padding of content-container */
.overlay-container:deep(section.drilldown main .content-container) {
	padding: 0 var(--gap-4);
}

.subheader {
	color: var(--text-color-subdued);
	margin-bottom: var(--gap-4);
}

.model-weights {
	display: flex;
	align-items: start;
}

th {
	text-align: left;
}
</style>
