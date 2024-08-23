<template>
	<tera-drilldown
		:node="node"
		:menu-items="menuItems"
		@update:selection="onSelection"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<section :tabName="DrilldownTabs.Wizard" class="ml-4 mr-2 pt-3">
			<tera-drilldown-section>
				<template #header-controls-right>
					<Button label="Run" icon="pi pi-play" @click="run" :disabled="showSpinner" />
					<tera-pyciemss-cancel-button class="mr-auto" :simulation-run-id="cancelRunId" />
				</template>
				<div class="form-section">
					<h5>
						Simulation settings
						<i v-tooltip="simulationSettingsToolTip" class="pi pi-info-circle" />
					</h5>

					<!-- Start & End -->
					<div class="input-row">
						<div class="label-and-input">
							<label for="2">Start time</label>
							<tera-input-number
								id="2"
								v-model="timespan.start"
								inputId="integeronly"
								@update:model-value="updateState"
							/>
						</div>
						<div class="label-and-input">
							<label for="3">End time</label>
							<tera-input-number
								id="3"
								v-model="timespan.end"
								inputId="integeronly"
								@update:model-value="updateState"
							/>
						</div>
					</div>

					<!-- Presets -->
					<div class="label-and-input">
						<label for="4">Preset (optional)</label>
						<Dropdown
							v-model="presetType"
							placeholder="Select an option"
							:options="[CiemssPresetTypes.Fast, CiemssPresetTypes.Normal]"
							@update:model-value="setPresetValues"
						/>
					</div>

					<!-- Number of Samples & Method -->
					<div class="input-row mt-3">
						<div class="label-and-input">
							<label for="4">Number of samples</label>
							<tera-input-number
								id="4"
								v-model="numSamples"
								inputId="integeronly"
								:min="1"
								@update:model-value="updateState"
							/>
						</div>
						<div class="label-and-input">
							<label for="5">Method</label>
							<Dropdown
								v-model="method"
								:options="[CiemssMethodOptions.dopri5, CiemssMethodOptions.euler]"
								@update:model-value="updateState"
							/>
						</div>
					</div>
					<!-- FIXME: show sampled values ???
					<div v-if="inferredParameters">Using inferred parameters from calibration: {{ inferredParameters[0] }}</div>
					-->
				</div>
			</tera-drilldown-section>
		</section>
		<tera-drilldown-section :tabName="DrilldownTabs.Notebook" class="notebook-section">
			<div class="toolbar">
				<tera-notebook-jupyter-input
					:kernel-manager="kernelManager"
					:context-language="'python3'"
					@llm-output="(data: any) => processLLMOutput(data)"
					@llm-thought-output="(data: any) => llmThoughts.push(data)"
					@question-asked="updateLlmQuery"
				>
					<template #toolbar-right-side
						>t
						<Button label="Run" size="small" icon="pi pi-play" @click="runCode" />
					</template>
				</tera-notebook-jupyter-input>
				<tera-notebook-jupyter-thought-output :llm-thoughts="llmThoughts" />
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
		<template #preview>
			<tera-drilldown-preview
				title="Simulation output"
				:options="outputs"
				v-model:output="selectedOutputId"
				@update:selection="onSelection"
				:is-loading="showSpinner"
				is-selectable
			>
				<tera-operator-output-summary
					v-if="node.state.summaryId && runResults[selectedRunId]"
					:summary-id="node.state.summaryId"
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
								:multi-select="true"
								:show-remove-button="true"
								:variables="Object.keys(pyciemssMap)"
								@configuration-change="chartProxy.configurationChange(index, $event)"
								@remove="chartProxy.removeChart(index)"
							/>
							<vega-chart expandable :are-embed-actions-visible="true" :visualization-spec="preparedCharts[index]" />
						</template>
						<Button size="small" text @click="chartProxy.addChart()" label="Add chart" icon="pi pi-plus" />
					</div>
					<div v-else-if="view === OutputView.Data">
						<tera-dataset-datatable
							v-if="rawContent[selectedRunId]"
							:rows="10"
							:raw-content="rawContent[selectedRunId]"
						/>
					</div>
				</template>
			</tera-drilldown-preview>
		</template>
		<template #footer>
			<tera-save-dataset-from-simulation
				:simulation-run-id="node.state.forecastId"
				:showDialog="showSaveDataDialog"
				@hide-dialog="showSaveDataDialog = false"
			/>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import type { CsvAsset, SimulationRequest, TimeSpan } from '@/types/Types';
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
import teraNotebookJupyterThoughtOutput from '@/components/llm/tera-notebook-jupyter-thought-output.vue';
import SelectButton from 'primevue/selectbutton';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraSaveDatasetFromSimulation from '@/components/dataset/tera-save-dataset-from-simulation.vue';
import TeraPyciemssCancelButton from '@/components/pyciemss/tera-pyciemss-cancel-button.vue';
import TeraNotebookError from '@/components/drilldown/tera-notebook-error.vue';
import TeraOperatorOutputSummary from '@/components/operator/tera-operator-output-summary.vue';
import { useProjects } from '@/composables/project';
import { isSaveDatasetDisabled } from '@/components/dataset/utils';
import TeraNotebookJupyterInput from '@/components/llm/tera-notebook-jupyter-input.vue';
import { KernelSessionManager } from '@/services/jupyter';
import { logger } from '@/utils/logger';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import { createForecastChart } from '@/services/charts';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { CiemssPresetTypes, DrilldownTabs } from '@/types/common';
import { getModelConfigurationById } from '@/services/model-configurations';
import { SimulateCiemssOperationState } from './simulate-ciemss-operation';
import TeraChartControl from '../../tera-chart-control.vue';

const props = defineProps<{
	node: WorkflowNode<SimulateCiemssOperationState>;
}>();
const emit = defineEmits(['update-state', 'select-output', 'close']);

const modelVarUnits = ref<{ [key: string]: string }>({});
let editor: VAceEditorInstance['_editor'] | null;
const codeText = ref('');

const policyInterventionId = computed(() => props.node.inputs[1].value);

const timespan = ref<TimeSpan>(props.node.state.currentTimespan);
const llmThoughts = ref<any[]>([]);
const llmQuery = ref('');

// extras
const numSamples = ref<number>(props.node.state.numSamples);
const method = ref(props.node.state.method);

const simulationSettingsToolTip: string = 'TODO';

enum OutputView {
	Charts = 'Charts',
	Data = 'Data'
}

const speedValues = Object.freeze({
	numSamples: 10,
	method: CiemssMethodOptions.euler
});

const qualityValues = Object.freeze({
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

const showSaveDataDialog = ref<boolean>(false);
const view = ref(OutputView.Charts);
const viewOptions = ref([
	{ value: OutputView.Charts, icon: 'pi pi-image' },
	{ value: OutputView.Data, icon: 'pi pi-list' }
]);

const isSaveDisabled = computed<boolean>(() =>
	isSaveDatasetDisabled(selectedRunId.value, useProjects().activeProject.value?.id)
);

const menuItems = computed(() => [
	{
		label: 'Save as new dataset',
		icon: 'pi pi-pencil',
		disabled: isSaveDisabled.value,
		command: () => {
			showSaveDataDialog.value = true;
		}
	}
]);

const showSpinner = ref(false);
const runResults = ref<{ [runId: string]: DataArray }>({});
const runResultsSummary = ref<{ [runId: string]: DataArray }>({});
const rawContent = ref<{ [runId: string]: CsvAsset }>({});

let pyciemssMap: Record<string, string> = {};

const kernelManager = new KernelSessionManager();

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

const presetType = computed(() => {
	if (numSamples.value === speedValues.numSamples && method.value === speedValues.method) {
		return CiemssPresetTypes.Fast;
	}
	if (numSamples.value === qualityValues.numSamples && method.value === qualityValues.method) {
		return CiemssPresetTypes.Normal;
	}

	return '';
});

const selectedOutputId = ref<string>();
const selectedRunId = computed(() => props.node.outputs.find((o) => o.id === selectedOutputId.value)?.value?.[0]);

const cancelRunId = computed(() => props.node.state.inProgressForecastId);
const outputPanel = ref(null);
const chartSize = computed(() => drilldownChartSize(outputPanel.value));

const chartProxy = chartActionsProxy(props.node, (state: SimulateCiemssOperationState) => {
	emit('update-state', state);
});

const setPresetValues = (data: CiemssPresetTypes) => {
	if (data === CiemssPresetTypes.Normal) {
		numSamples.value = qualityValues.numSamples;
		method.value = qualityValues.method;
	}
	if (data === CiemssPresetTypes.Fast) {
		numSamples.value = speedValues.numSamples;
		method.value = speedValues.method;
	}
};

const preparedCharts = computed(() => {
	if (!selectedRunId.value) return [];

	const result = runResults.value[selectedRunId.value];
	const resultSummary = runResultsSummary.value[selectedRunId.value];

	const reverseMap: Record<string, string> = {};
	Object.keys(pyciemssMap).forEach((key) => {
		reverseMap[`${pyciemssMap[key]}_mean`] = key;
	});
	return props.node.state.chartConfigs.map((config) =>
		createForecastChart(
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
		)
	);
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
	const state = props.node.state;

	const payload: SimulationRequest = {
		modelConfigId,
		timespan: {
			start: state.currentTimespan.start,
			end: state.currentTimespan.end
		},
		extra: {
			solver_method: state.method,
			solver_step_size: 1,
			num_samples: state.numSamples
		},
		engine: 'ciemss'
	};

	const modelConfig = await getModelConfigurationById(modelConfigId);
	if (modelConfig.simulationId) {
		payload.extra.inferred_parameters = modelConfig.simulationId;
	}

	if (policyInterventionId.value?.[0]) {
		payload.policyInterventionId = policyInterventionId.value[0];
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

onMounted(() => {
	buildJupyterContext();
});

onUnmounted(() => kernelManager.shutdown());
</script>

<style scoped>
.notebook-section:deep(main .toolbar) {
	padding-left: var(--gap-medium);
}

.notebook-section:deep(main) {
	gap: var(--gap-small);
	position: relative;
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
