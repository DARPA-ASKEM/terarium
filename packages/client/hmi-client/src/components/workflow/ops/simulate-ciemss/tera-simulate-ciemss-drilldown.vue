<template>
	<tera-drilldown
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
							<tera-pyciemss-cancel-button :simulation-run-id="cancelRunId" />
							<Button label="Run" icon="pi pi-play" @click="run" :disabled="showSpinner" />
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
							<div class="label-and-input">
								<label for="start-time">Start time</label>
								<tera-input-number
									id="start-time"
									v-model="timespan.start"
									inputId="integeronly"
									@update:model-value="updateState"
								/>
							</div>
							<div class="label-and-input">
								<label for="timespan">End time</label>
								<tera-input-number
									id="timespan"
									v-model="timespan.end"
									inputId="integeronly"
									@update:model-value="updateState"
								/>
							</div>
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
						<template v-if="interventionPolicy">
							<h4>Intervention Policies</h4>
							<tera-intervention-summary-card
								v-for="(intervention, index) in interventionPolicy.interventions"
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
			<tera-drilldown-section v-if="selectedOutputId" :is-loading="showSpinner">
				<template #header-controls-right>
					<Button label="Save for re-use" severity="secondary" outlined @click="showSaveDataset = true" />
				</template>
				<tera-operator-output-summary
					v-if="node.state.summaryId && runResults[selectedRunId]"
					:summary-id="node.state.summaryId"
					class="pt-3"
				/>
				<div class="flex flex-row align-items-center gap-2">
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
						<template v-for="(cfg, index) of node.state.chartConfigs" :key="index">
							<tera-chart-control
								:chart-config="{ selectedRun: selectedRunId, selectedVariable: cfg }"
								multi-select
								show-remove-button
								:variables="Object.keys(pyciemssMap)"
								@configuration-change="chartProxy.configurationChange(index, $event)"
								@remove="chartProxy.removeChart(index)"
							/>
							<vega-chart
								v-if="preparedCharts[index].layer.length > 0"
								expandable
								are-embed-actions-visible
								:visualization-spec="preparedCharts[index]"
							/>
							<!-- If no variables are selected, show empty state -->
							<section class="empty-chart" v-else>
								<img src="@assets/svg/seed.svg" class="empty-image" alt="" draggable="false" />
								<p>Select one or more variables for this chart</p>
							</section>

							<!-- Spacer between charts -->
							<div style="height: var(--gap-1)"></div>
						</template>
						<Button size="small" text @click="chartProxy.addChart()" label="Add chart" icon="pi pi-plus" />

						<!-- Spacer at bottom of page -->
						<div style="height: 2rem"></div>
					</div>
					<div v-else-if="view === OutputView.Data">
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
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import type { CsvAsset, InterventionPolicy, SimulationRequest, TimeSpan } from '@/types/Types';
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
import { getModelByModelConfigurationId, getUnitsFromModelParts } from '@/services/model';
import { chartActionsProxy, drilldownChartSize, nodeMetadata } from '@/components/workflow/util';

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
import { createForecastChart, createInterventionChartMarkers } from '@/services/charts';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { CiemssPresetTypes, DrilldownTabs } from '@/types/common';
import { getModelConfigurationById } from '@/services/model-configurations';
import { flattenInterventionData, getInterventionPolicyById } from '@/services/intervention-policy';
import TeraInterventionSummaryCard from '@/components/workflow/ops/simulate-ciemss/tera-intervention-summary-card.vue';
import TeraSaveSimulationModal from '@/components/project/tera-save-simulation-modal.vue';
import { SimulateCiemssOperationState } from './simulate-ciemss-operation';
import TeraChartControl from '../../tera-chart-control.vue';

const isSidebarOpen = ref(true);
const props = defineProps<{
	node: WorkflowNode<SimulateCiemssOperationState>;
}>();
const emit = defineEmits(['update-state', 'select-output', 'close']);

const modelVarUnits = ref<{ [key: string]: string }>({});
let editor: VAceEditorInstance['_editor'] | null;
const codeText = ref('');

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

const showSpinner = ref(false);
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

const cancelRunId = computed(() => props.node.state.inProgressForecastId);
const outputPanel = ref(null);
const chartSize = computed(() => drilldownChartSize(outputPanel.value));

const showSaveDataset = ref(false);

const chartProxy = chartActionsProxy(props.node, (state: SimulateCiemssOperationState) => {
	emit('update-state', state);
});

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

const preparedCharts = computed(() => {
	if (!selectedRunId.value) return [];

	const result = runResults.value[selectedRunId.value];
	const resultSummary = runResultsSummary.value[selectedRunId.value];
	const reverseMap: Record<string, string> = {};
	Object.keys(pyciemssMap).forEach((key) => {
		reverseMap[`${pyciemssMap[key]}_mean`] = key;
	});

	return props.node.state.chartConfigs.map((config) => {
		const chart = createForecastChart(
			{
				data: result,
				variables: config.map((d) => pyciemssMap[d]),
				timeField: 'timepoint_id',
				groupField: 'sample_id'
			},
			{
				data: resultSummary,
				variables: config.map((d) => `${pyciemssMap[d]}_mean`),
				timeField: 'timepoint_id'
			},
			null,
			// options
			{
				title: '',
				width: chartSize.value.width,
				height: chartSize.value.height,
				legend: true,
				translationMap: reverseMap,
				xAxisTitle: modelVarUnits.value._time || 'Time',
				yAxisTitle: _.uniq(config.map((v) => modelVarUnits.value[v]).filter((v) => !!v)).join(',') || ''
			}
		);
		if (interventionPolicy.value) {
			_.keys(groupedInterventionOutputs.value).forEach((key) => {
				if (config.includes(key)) {
					chart.layer.push(...createInterventionChartMarkers(groupedInterventionOutputs.value[key]));
				}
			});
		}
		return chart;
	});
});

const updateState = () => {
	const state = _.cloneDeep(props.node.state);
	state.currentTimespan = timespan.value;
	state.numSamples = numSamples.value;
	state.method = method.value;
	emit('update-state', state);
};

const run = async () => {
	const simulationId = await makeForecastRequest();

	const state = _.cloneDeep(props.node.state);
	state.inProgressForecastId = simulationId;
	emit('update-state', state);
};

const makeForecastRequest = async () => {
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

	if (policyInterventionId.value) {
		payload.policyInterventionId = policyInterventionId.value;
	}

	const response = await makeForecastJob(payload, nodeMetadata(props.node));
	return response.id;
};

const lazyLoadSimulationData = async (outputRunId: string) => {
	if (runResults.value[outputRunId] && rawContent.value[outputRunId]) return;

	const forecastId = props.node.state.forecastId;
	if (!forecastId || forecastId === '') return;

	const result = await getRunResultCSV(forecastId, 'result.csv');
	pyciemssMap = parsePyCiemssMap(result[0]);
	runResults.value[outputRunId] = result;
	rawContent.value[outputRunId] = convertToCsvAsset(result, Object.values(pyciemssMap));

	const resultSummary = await getRunResultCSV(forecastId, 'result_summary.csv');
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
		const model = await getModelByModelConfigurationId(id);
		if (model) modelVarUnits.value = getUnitsFromModelParts(model);
	},
	{ immediate: true }
);

watch(
	() => props.node.state.inProgressForecastId,
	(id) => {
		if (id === '') showSpinner.value = false;
		else showSpinner.value = true;
	}
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
	position: absolute;
	width: calc(100% - 240px);
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
</style>
