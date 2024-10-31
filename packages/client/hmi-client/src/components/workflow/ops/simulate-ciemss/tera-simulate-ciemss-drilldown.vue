<template>
	<tera-drilldown
		v-bind="$attrs"
		:node="node"
		@update:selection="onSelection"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
		class="drilldown"
	>
		<!-- Wizard -->
		<tera-drilldown-section :tabName="DrilldownTabs.Wizard" class="wizard">
			<tera-slider-panel
				class="input-config"
				v-model:is-open="isSidebarOpen"
				header="Simulation settings"
				content-width="420px"
			>
				<template #content>
					<div class="toolbar">
						<p>Click Run to start the simulation.</p>
						<span class="flex gap-2">
							<tera-pyciemss-cancel-button :simulation-run-id="cancelRunIds" />
							<Button label="Run" icon="pi pi-play" @click="run" :loading="inProgressForecastRun" />
						</span>
					</div>
					<div class="form-section" v-if="isSidebarOpen">
						<!-- Presets -->
						<div class="label-and-input">
							<label>Preset (optional)</label>
							<Dropdown
								v-model="presetType"
								placeholder="Select an option"
								:options="[CiemssPresetTypes.Fast, CiemssPresetTypes.Normal]"
								@update:model-value="setPresetValues"
							/>
						</div>

						<!-- Start & End -->
						<div class="input-row">
							<tera-timestep-calendar
								disabled
								v-if="model && modelConfiguration"
								label="Start time"
								:start-date="modelConfiguration.temporalContext"
								:calendar-settings="getCalendarSettingsFromModel(model)"
								v-model="timespan.start"
							/>
							<tera-timestep-calendar
								v-if="model && modelConfiguration"
								label="End time"
								:start-date="modelConfiguration.temporalContext"
								:calendar-settings="getCalendarSettingsFromModel(model)"
								v-model="timespan.end"
							/>
						</div>

						<!-- Number of Samples & Method -->
						<div class="input-row">
							<div class="label-and-input">
								<label for="num-samples">Number of samples</label>
								<tera-input-number
									id="num-samples"
									v-model="numSamples"
									inputId="integeronly"
									:min="1"
									@update:model-value="updateState"
								/>
							</div>
							<div class="label-and-input">
								<label for="solver-method">Method</label>
								<Dropdown
									id="solver-method"
									v-model="method"
									:options="[CiemssMethodOptions.dopri5, CiemssMethodOptions.euler]"
									@update:model-value="updateState"
								/>
							</div>
						</div>
						<template v-if="interventionPolicy && model">
							<h4>Intervention Policies</h4>
							<tera-intervention-summary-card
								v-for="(intervention, index) in interventionPolicy.interventions"
								:start-date="modelConfiguration?.temporalContext"
								:calendar-settings="getCalendarSettingsFromModel(model)"
								:intervention="intervention"
								:key="index"
							/>
						</template>
					</div>
				</template>
			</tera-slider-panel>
		</tera-drilldown-section>

		<!-- Notebook -->
		<tera-drilldown-section :tabName="DrilldownTabs.Notebook" class="notebook-section">
			<div class="toolbar">
				<tera-notebook-jupyter-input
					:kernel-manager="kernelManager"
					:context-language="'python3'"
					@llm-output="(data: any) => processLLMOutput(data)"
					@llm-thought-output="(data: any) => llmThoughts.push(data)"
					@question-asked="updateLlmQuery"
				>
					<template #toolbar-right-side>
						<Button label="Run" size="small" icon="pi pi-play" @click="runCode" />
					</template>
				</tera-notebook-jupyter-input>
			</div>
			<v-ace-editor
				v-model:value="codeText"
				@init="initializeAceEditor"
				lang="python"
				theme="chrome"
				style="flex-grow: 1; width: 100%"
				class="ace-editor"
			/>
		</tera-drilldown-section>

		<!-- Preview -->
		<template #preview>
			<tera-drilldown-section v-if="selectedOutputId" :is-loading="inProgressForecastRun">
				<template #header-controls-right>
					<Button class="mr-3" label="Save for re-use" severity="secondary" outlined @click="showSaveDataset = true" />
				</template>
				<tera-operator-output-summary
					v-if="node.state.summaryId && runResults[selectedRunId]"
					:summary-id="node.state.summaryId"
					class="p-3"
				/>
				<div class="pl-3 pr-3 flex flex-row align-items-center gap-2">
					<SelectButton
						class=""
						:model-value="view"
						@change="if ($event.value) view = $event.value;"
						:options="viewOptions"
						option-value="value"
					>
						<template #option="{ option }">
							<i :class="`${option.icon} p-button-icon-left`" />
							<span class="p-button-label">{{ option.value }}</span>
						</template>
					</SelectButton>
				</div>
				<tera-notebook-error v-bind="node.state.errorMessage" />
				<template v-if="runResults[selectedRunId]">
					<div v-if="view === OutputView.Charts" ref="outputPanel">
						<template v-for="setting of chartSettings" :key="setting.id">
							<vega-chart
								v-if="preparedCharts[setting.id]"
								expandable
								are-embed-actions-visible
								:visualization-spec="preparedCharts[setting.id]"
							/>
							<!-- Spacer between charts -->
							<div style="height: var(--gap-1)"></div>
						</template>
						<!-- Spacer at bottom of page -->
						<div style="height: 2rem"></div>
					</div>
					<div v-else-if="view === OutputView.Data" class="p-3">
						<tera-dataset-datatable
							v-if="rawContent[selectedRunId]"
							:rows="10"
							:raw-content="rawContent[selectedRunId]"
						/>
					</div>
				</template>
			</tera-drilldown-section>

			<!-- Empty state -->
			<section v-else class="empty-state">
				<Vue3Lottie :animationData="EmptySeed" :height="150" loop autoplay />
				<p class="helpMessage">Click 'Run' to start the simulation</p>
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
						:annotations="
							activeChartSettings?.type === ChartSettingType.VARIABLE_COMPARISON
								? getChartAnnotationsByChartId(activeChartSettings.id)
								: undefined
						"
						:active-settings="activeChartSettings"
						:generate-annotation="generateAnnotation"
						@delete-annotation="deleteAnnotation"
						@close="activeChartSettings = null"
					/>
				</template>
				<template #content>
					<div class="output-settings-panel">
						<tera-chart-settings
							:title="'Comparison charts'"
							:settings="chartSettings"
							:type="ChartSettingType.VARIABLE_COMPARISON"
							:select-options="Object.keys(pyciemssMap)"
							:selected-options="comparisonChartsSettingsSelection"
							@open="activeChartSettings = $event"
							@remove="removeChartSetting"
							@selection-change="comparisonChartsSettingsSelection = $event"
						/>
						<div>
							<Button
								:disabled="!comparisonChartsSettingsSelection.length"
								size="small"
								text
								@click="addComparisonChartSettings"
								label="Add comparison chart"
								icon="pi pi-plus"
							/>
						</div>
						<hr />
					</div>
				</template>
			</tera-slider-panel>
		</template>
	</tera-drilldown>
	<tera-save-simulation-modal
		:is-visible="showSaveDataset"
		@close-modal="showSaveDataset = false"
		:simulation-id="node.state.forecastId"
		:assets="[{ id: datasetId, type: AssetType.Dataset }]"
	/>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import { Vue3Lottie } from 'vue3-lottie';
import EmptySeed from '@/assets/images/lottie-empty-seed.json';
import { useDrilldownChartSize } from '@/composables/useDrilldownChartSize';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import type {
	CsvAsset,
	InterventionPolicy,
	Model,
	ModelConfiguration,
	SimulationRequest,
	TimeSpan
} from '@/types/Types';
import { AssetType } from '@/types/Types';
import type { WorkflowNode } from '@/types/workflow';
import {
	getRunResultCSV,
	parsePyCiemssMap,
	makeForecastJobCiemss as makeForecastJob,
	convertToCsvAsset,
	DataArray,
	CiemssMethodOptions
} from '@/services/models/simulation-service';
import {
	getModelByModelConfigurationId,
	getUnitsFromModelParts,
	getCalendarSettingsFromModel,
	getVegaDateOptions
} from '@/services/model';
import { nodeMetadata } from '@/components/workflow/util';

import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import SelectButton from 'primevue/selectbutton';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraPyciemssCancelButton from '@/components/pyciemss/tera-pyciemss-cancel-button.vue';
import TeraNotebookError from '@/components/drilldown/tera-notebook-error.vue';
import TeraOperatorOutputSummary from '@/components/operator/tera-operator-output-summary.vue';
import { useProjects } from '@/composables/project';
import TeraNotebookJupyterInput from '@/components/llm/tera-notebook-jupyter-input.vue';
import { KernelSessionManager } from '@/services/jupyter';
import { logger } from '@/utils/logger';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import {
	applyForecastChartAnnotations,
	createForecastChart,
	createInterventionChartMarkers,
	ForecastChartOptions
} from '@/services/charts';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { ChartSetting, ChartSettingType, CiemssPresetTypes, DrilldownTabs } from '@/types/common';
import { getModelConfigurationById } from '@/services/model-configurations';
import { flattenInterventionData, getInterventionPolicyById } from '@/services/intervention-policy';
import TeraInterventionSummaryCard from '@/components/intervention-policy/tera-intervention-summary-card.vue';
import TeraSaveSimulationModal from '@/components/project/tera-save-simulation-modal.vue';
import TeraChartSettings from '@/components/widgets/tera-chart-settings.vue';
import TeraChartSettingsPanel from '@/components/widgets/tera-chart-settings-panel.vue';
import TeraTimestepCalendar from '@/components/widgets/tera-timestep-calendar.vue';
import {
	addMultiVariableChartSetting,
	deleteAnnotation,
	generateForecastChartAnnotation,
	saveAnnotation,
	removeChartSettingById
} from '@/services/chart-settings';
import { useChartAnnotations } from '@/composables/useChartAnnotations';
import { SimulateCiemssOperationState } from './simulate-ciemss-operation';
import { mergeResults, renameFnGenerator } from '../calibrate-ciemss/calibrate-utils';

const props = defineProps<{
	node: WorkflowNode<SimulateCiemssOperationState>;
}>();
const emit = defineEmits(['update-state', 'select-output', 'close']);

const isSidebarOpen = ref(true);
const isOutputSettingsPanelOpen = ref(false);
const chartSettings = computed(() => props.node.state.chartSettings ?? []);
const activeChartSettings = ref<ChartSetting | null>(null);
const comparisonChartsSettingsSelection = ref<string[]>([]);

const modelVarUnits = ref<{ [key: string]: string }>({});
let editor: VAceEditorInstance['_editor'] | null;
const codeText = ref('');

const modelConfiguration = ref<ModelConfiguration | null>(null);
const model = ref<Model | null>(null);

const policyInterventionId = computed(() => props.node.inputs[1].value?.[0]);
const interventionPolicy = ref<InterventionPolicy | null>(null);
const datasetId = computed(() => {
	if (!selectedOutputId.value) return '';
	const output = props.node.outputs.find((o) => o.id === selectedOutputId.value);
	return output?.value?.[0] ?? '';
});

const llmThoughts = ref<any[]>([]);
const llmQuery = ref('');

// input params
const timespan = ref<TimeSpan>(props.node.state.currentTimespan);
const numSamples = ref<number>(props.node.state.numSamples);
const method = ref(props.node.state.method);

enum OutputView {
	Charts = 'Charts',
	Data = 'Data'
}

const speedPreset = Object.freeze({
	numSamples: 10,
	method: CiemssMethodOptions.euler
});

const qualityPreset = Object.freeze({
	numSamples: 100,
	method: CiemssMethodOptions.dopri5
});

const updateLlmQuery = (query: string) => {
	llmThoughts.value = [];
	llmQuery.value = query;
};

const processLLMOutput = (data: any) => {
	codeText.value = data.content.code;
};

const view = ref(OutputView.Charts);
const viewOptions = ref([
	{ value: OutputView.Charts, icon: 'pi pi-image' },
	{ value: OutputView.Data, icon: 'pi pi-list' }
]);

const inProgressForecastRun = computed(() =>
	Boolean(props.node.state.inProgressForecastId || props.node.state.inProgressBaseForecastId)
);
const runResults = ref<{ [runId: string]: DataArray }>({});
const runResultsSummary = ref<{ [runId: string]: DataArray }>({});
const rawContent = ref<{ [runId: string]: CsvAsset }>({});

let pyciemssMap: Record<string, string> = {};

const kernelManager = new KernelSessionManager();

const presetType = computed(() => {
	if (numSamples.value === speedPreset.numSamples && method.value === speedPreset.method) {
		return CiemssPresetTypes.Fast;
	}
	if (numSamples.value === qualityPreset.numSamples && method.value === qualityPreset.method) {
		return CiemssPresetTypes.Normal;
	}

	return '';
});

const selectedOutputId = ref<string>();
const selectedRunId = computed(() => props.node.outputs.find((o) => o.id === selectedOutputId.value)?.value?.[0]);

const cancelRunIds = computed(() =>
	[props.node.state.inProgressForecastId, props.node.state.inProgressBaseForecastId].filter((id) => Boolean(id))
);
const outputPanel = ref(null);
const chartSize = useDrilldownChartSize(outputPanel);

const showSaveDataset = ref(false);

const setPresetValues = (data: CiemssPresetTypes) => {
	if (data === CiemssPresetTypes.Normal) {
		numSamples.value = qualityPreset.numSamples;
		method.value = qualityPreset.method;
	}
	if (data === CiemssPresetTypes.Fast) {
		numSamples.value = speedPreset.numSamples;
		method.value = speedPreset.method;
	}
	updateState();
};

const groupedInterventionOutputs = computed(() =>
	_.groupBy(flattenInterventionData(interventionPolicy.value?.interventions ?? []), 'appliedTo')
);

const preparedChartInputs = computed(() => {
	if (!selectedRunId.value) return null;
	const result = runResults.value[selectedRunId.value];
	const resultSummary = runResultsSummary.value[selectedRunId.value];
	const reverseMap: Record<string, string> = {};
	Object.keys(pyciemssMap).forEach((key) => {
		reverseMap[`${pyciemssMap[key]}_mean`] = key;
	});
	return { result, resultSummary, reverseMap };
});

const getForecastChartOptions = (variables: string[], reverseMap: Record<string, string>) => {
	// If only one variable is selected, show the baseline forecast
	const showBaseLine = variables.length === 1 && Boolean(props.node.state.baseForecastId);
	const dateOptions = getVegaDateOptions(model.value, modelConfiguration.value);

	const options: ForecastChartOptions = {
		title: '',
		width: chartSize.value.width,
		height: chartSize.value.height,
		legend: true,
		translationMap: reverseMap,
		xAxisTitle: modelVarUnits.value._time || 'Time',
		yAxisTitle: _.uniq(variables.map((v) => modelVarUnits.value[v]).filter((v) => !!v)).join(',') || ''
	};
	let statLayerVariables = variables.map((d) => `${pyciemssMap[d]}_mean`);
	let sampleLayerVariables = variables.map((d) => pyciemssMap[d]);

	if (showBaseLine) {
		sampleLayerVariables = [`${pyciemssMap[variables[0]]}:base`, `${pyciemssMap[variables[0]]}`];
		statLayerVariables = [`${pyciemssMap[variables[0]]}_mean:base`, `${pyciemssMap[variables[0]]}_mean`];
		options.translationMap = {
			...options.translationMap,
			[`${pyciemssMap[variables[0]]}_mean:base`]: `${variables[0]} (baseline)`
		};
		options.colorscheme = ['#AAB3C6', '#1B8073'];
	}
	if (dateOptions) {
		options.dateOptions = dateOptions;
	}
	return { statLayerVariables, sampleLayerVariables, options };
};

const preparedCharts = computed(() => {
	const charts: Record<string, any> = {};
	if (!preparedChartInputs.value) return charts;
	const { result, resultSummary, reverseMap } = preparedChartInputs.value;

	chartSettings.value.forEach((setting) => {
		const selectedVars = setting.selectedVariables;
		const { statLayerVariables, sampleLayerVariables, options } = getForecastChartOptions(selectedVars, reverseMap);
		const annotations = getChartAnnotationsByChartId(setting.id);

		const chart = applyForecastChartAnnotations(
			createForecastChart(
				{
					data: result,
					variables: sampleLayerVariables,
					timeField: 'timepoint_id',
					groupField: 'sample_id'
				},
				{
					data: resultSummary,
					variables: statLayerVariables,
					timeField: 'timepoint_id'
				},
				null,
				options
			),
			annotations
		);
		if (interventionPolicy.value) {
			_.keys(groupedInterventionOutputs.value).forEach((key) => {
				if (selectedVars.includes(key)) {
					chart.layer.push(...createInterventionChartMarkers(groupedInterventionOutputs.value[key]));
				}
			});
		}
		charts[setting.id] = chart;
	});
	return charts;
});

// --- Handle chart annotations
const { getChartAnnotationsByChartId } = useChartAnnotations(props.node.id);
const generateAnnotation = async (setting: ChartSetting, query: string) => {
	// Note: Currently llm generated chart annotations are supported for the forecast chart only
	if (!preparedChartInputs.value) return null;
	const { statLayerVariables, options } = getForecastChartOptions(
		setting.selectedVariables,
		preparedChartInputs.value.reverseMap
	);
	const annotationLayerSpec = await generateForecastChartAnnotation(query, 'timepoint_id', statLayerVariables, options);
	const saved = await saveAnnotation(annotationLayerSpec, props.node.id, setting.id);
	return saved;
};
// ---

const removeChartSetting = (chartId) => {
	emit('update-state', {
		...props.node.state,
		chartSettings: removeChartSettingById(chartSettings.value, chartId)
	});
};

const addComparisonChartSettings = () => {
	emit('update-state', {
		...props.node.state,
		chartSettings: addMultiVariableChartSetting(
			chartSettings.value,
			ChartSettingType.VARIABLE_COMPARISON,
			comparisonChartsSettingsSelection.value
		)
	});
	comparisonChartsSettingsSelection.value = [];
};

const updateState = () => {
	const state = _.cloneDeep(props.node.state);
	state.currentTimespan = timespan.value;
	state.numSamples = numSamples.value;
	state.method = method.value;
	emit('update-state', state);
};

const run = async () => {
	const [baseSimulationId, simulationId] = await Promise.all([
		// If intervention id is available, request the base forecast run, otherwise resolve with empty string
		policyInterventionId.value ? makeForecastRequest(false) : Promise.resolve(''),
		makeForecastRequest()
	]);

	const state = _.cloneDeep(props.node.state);
	state.inProgressBaseForecastId = baseSimulationId;
	state.inProgressForecastId = simulationId;
	emit('update-state', state);
};

const makeForecastRequest = async (applyInterventions = true) => {
	const modelConfigId = props.node.inputs[0].value?.[0];
	const payload: SimulationRequest = {
		modelConfigId,
		timespan: {
			start: timespan.value.start,
			end: timespan.value.end
		},
		extra: {
			solver_method: method.value,
			solver_step_size: 1,
			num_samples: numSamples.value
		},
		engine: 'ciemss'
	};

	const modelConfig = await getModelConfigurationById(modelConfigId);
	if (modelConfig.simulationId) {
		payload.extra.inferred_parameters = modelConfig.simulationId;
	}

	if (applyInterventions && policyInterventionId.value) {
		payload.policyInterventionId = policyInterventionId.value;
	}
	const response = await makeForecastJob(payload, {
		...nodeMetadata(props.node),
		isBaseForecast: !applyInterventions
	});
	return response.id;
};

const lazyLoadSimulationData = async (outputRunId: string) => {
	if (runResults.value[outputRunId] && rawContent.value[outputRunId]) return;

	const forecastId = props.node.state.forecastId;
	if (!forecastId || inProgressForecastRun.value) return;

	let [result, resultSummary] = await Promise.all([
		getRunResultCSV(forecastId, 'result.csv'),
		getRunResultCSV(forecastId, 'result_summary.csv')
	]);
	pyciemssMap = parsePyCiemssMap(result[0]);
	rawContent.value[outputRunId] = convertToCsvAsset(result, Object.values(pyciemssMap));

	// Forecast results without the interventions
	const baseForecastId = props.node.state.baseForecastId;
	if (baseForecastId) {
		const [baseResult, baseResultSummary] = await Promise.all([
			getRunResultCSV(baseForecastId, 'result.csv', renameFnGenerator('base')),
			getRunResultCSV(baseForecastId, 'result_summary.csv', renameFnGenerator('base'))
		]);
		const merged = mergeResults(baseResult, result, baseResultSummary, resultSummary);
		result = merged.result;
		resultSummary = merged.resultSummary;
	}
	runResults.value[outputRunId] = result;
	runResultsSummary.value[outputRunId] = resultSummary;
};

const onSelection = (id: string) => {
	emit('select-output', id);
};

const buildJupyterContext = async () => {
	const modelConfigId = props.node.inputs[0].value?.[0];
	if (!modelConfigId) return;
	try {
		const jupyterContext = {
			context: 'pyciemss',
			language: 'python3',
			context_info: {
				model_config_id: modelConfigId
			}
		};
		if (jupyterContext) {
			if (kernelManager.jupyterSession !== null) {
				kernelManager.shutdown();
			}
			await kernelManager.init('beaker_kernel', 'Beaker Kernel', jupyterContext);
			kernelManager.sendMessage('get_simulate_request', {}).register('any_get_simulate_reply', (data) => {
				codeText.value = data.msg.content.return;
			});
		}
	} catch (error) {
		logger.error(`Error initializing Jupyter session: ${error}`);
	}
};

const runCode = () => {
	const code = editor?.getValue();
	if (!code) return;

	const messageContent = {
		silent: false,
		store_history: false,
		user_expressions: {},
		allow_stdin: true,
		stop_on_error: false,
		code
	};

	// TODO: Utilize the output of this request.
	kernelManager
		.sendMessage('execute_request', { messageContent })
		.register('execute_input', (data) => {
			console.log(data.content.code);
		})
		.register('stream', (data) => {
			console.log('stream', data);
		})
		.register('any_execute_reply', (data) => {
			console.log(data);
			// FIXME: save isnt working...but the idea is to save the simulation results to the HMI with this action
			kernelManager
				.sendMessage('save_results_to_hmi_request', { project_id: useProjects().activeProjectId })
				.register('code_cell', (d) => {
					console.log(d);
				});
		});
};
const initializeAceEditor = (editorInstance: any) => {
	editor = editorInstance;
};

watch(
	() => props.node.inputs[0].value,
	async () => {
		const input = props.node.inputs[0];
		if (!input.value) return;

		const id = input.value[0];
		modelConfiguration.value = await getModelConfigurationById(id);
		model.value = await getModelByModelConfigurationId(id);
		if (model.value) modelVarUnits.value = getUnitsFromModelParts(model.value);
	},
	{ immediate: true }
);

watch(
	() => props.node.active,
	async (newValue, oldValue) => {
		if (!props.node.active || newValue === oldValue) return;
		selectedOutputId.value = props.node.active;

		// Update Wizard form fields with current selected output state
		timespan.value = props.node.state.currentTimespan;
		numSamples.value = props.node.state.numSamples;
		method.value = props.node.state.method;

		lazyLoadSimulationData(selectedRunId.value);
	},
	{ immediate: true }
);

// fetch intervention policy
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

onMounted(() => {
	buildJupyterContext();
});

onUnmounted(() => kernelManager.shutdown());
</script>

<style scoped>
.wizard .toolbar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: var(--gap-1) var(--gap);
	gap: var(--gap-2);
}

/* Make tera-inputs the same height as the dropdowns */
.tera-input:deep(main) {
	padding: 6px;
}

/* Override grid template so output expands when sidebar is closed */
.overlay-container:deep(section.drilldown main) {
	grid-template-columns: auto 1fr;
}

/* Override top and bottom padding of content-container */
.overlay-container:deep(section.drilldown main .content-container) {
	padding: 0 var(--gap);
}

.empty-chart {
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	height: 10rem;
	gap: var(--gap);
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	margin-bottom: var(--gap);
	color: var(--text-color-secondary);
	background: var(--surface-50);
}
.empty-image {
	width: 5rem;
	height: 6rem;
}

/* Notebook */
.notebook-section {
	width: calc(50vw - 4rem);
}

.notebook-section:deep(main) {
	gap: var(--gap-small);
	position: relative;
}

.form-section {
	display: flex;
	flex-direction: column;
	flex-grow: 1;
	gap: var(--gap);
	padding: var(--gap);
}

.label-and-input {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	margin-bottom: var(--gap);
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

.empty-state {
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

.p-button-icon-left {
	color: var(--text-color-primary);
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
