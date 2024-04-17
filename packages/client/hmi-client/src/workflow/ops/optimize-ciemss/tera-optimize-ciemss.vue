<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<section :tabName="OptimizeTabs.Wizard" class="ml-4 mr-2 pt-3">
			<tera-drilldown-section>
				<div class="form-section">
					<h5>Settings</h5>
					<div class="input-row">
						<div class="label-and-input">
							<label>Start time</label>
							<InputText disabled class="p-inputtext-sm" inputId="integeronly" value="0" />
						</div>
						<div class="label-and-input">
							<label>End time</label>
							<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="knobs.endTime" />
						</div>
					</div>
					<div>
						<Button
							v-if="!showAdditionalOptions"
							class="p-button-sm p-button-text"
							label="Show additional options"
							@click="toggleAdditonalOptions"
						/>
					</div>
					<div v-if="showAdditionalOptions" class="input-row">
						<div class="label-and-input">
							<label>Number of samples</label>
							<div>
								<InputNumber
									class="p-inputtext-sm"
									inputId="integeronly"
									v-model="knobs.numSamples"
								/>
							</div>
						</div>
						<div class="label-and-input">
							<label>Solver method</label>
							<Dropdown
								class="p-inputtext-sm"
								disabled
								:options="['dopri5', 'euler']"
								v-model="knobs.solverMethod"
								placeholder="Select"
							/>
						</div>
						<div class="label-and-input">
							<!-- TODO: This could likely be better explained to user -->
							<label> Minimized </label>
							<Dropdown
								class="toolbar-button"
								v-model="knobs.isMinimized"
								optionLabel="label"
								optionValue="value"
								:options="[
									{ label: 'true', value: true },
									{ label: 'false', value: false }
								]"
							/>
						</div>
					</div>
					<Button
						v-if="showAdditionalOptions"
						class="p-button-sm p-button-text w-3"
						label="Hide additional options"
						@click="toggleAdditonalOptions"
					/>
				</div>
				<div class="form-section">
					<h5>Intervention policy</h5>
					<div>
						<label>Intervention Type</label>
						<Dropdown
							class="p-inputtext-sm"
							:options="[
								{ label: 'parameter value', value: InterventionTypes.paramValue },
								{ label: 'start time', value: InterventionTypes.startTime }
							]"
							option-label="label"
							option-value="value"
							v-model="knobs.interventionType"
							placeholder="Select"
						/>
					</div>
					<tera-intervention-policy-group-form
						v-for="(cfg, idx) in props.node.state.interventionPolicyGroups"
						:key="idx"
						:config="cfg"
						:intervention-type="props.node.state.interventionType"
						:parameter-options="modelParameterOptions.map((ele) => ele.id)"
						@update-self="(config) => updateInterventionPolicyGroupForm(idx, config)"
						@delete-self="() => deleteInterverntionPolicyGroupForm(idx)"
					/>
					<div>
						<Button
							icon="pi pi-plus"
							class="p-button-sm p-button-text"
							label="Add more interventions"
							@click="addInterventionPolicyGroupForm"
						/>
					</div>
				</div>
				<div class="form-section">
					<h5>Constraint</h5>
					<div class="constraint-row">
						<div class="label-and-input">
							<label>Target-variable(s)</label>
							<MultiSelect
								class="p-inputtext-sm"
								:options="modelStateAndObsOptions"
								v-model="knobs.targetVariables"
								placeholder="Select"
								filter
							/>
						</div>
						<div class="label-and-input">
							<label>Qoi Method</label>
							<Dropdown
								class="p-inputtext-sm"
								:options="[
									{ label: 'Max', value: ContextMethods.max },
									{ label: 'Day average', value: ContextMethods.day_average }
								]"
								option-label="label"
								option-value="value"
								v-model="knobs.qoiMethod"
							/>
						</div>
					</div>
					<div class="constraint-row">
						<div class="label-and-input">
							<label>Acceptable risk of failure</label>
							<div>
								<InputNumber
									class="p-inputtext-sm"
									inputId="integeronly"
									v-model="knobs.riskTolerance"
								/>
							</div>
						</div>
						<div class="label-and-input">
							<label>Threshold</label>
							<InputNumber
								class="p-inputtext-sm"
								v-model="knobs.threshold"
								:min-fraction-digits="1"
								:max-fraction-digits="10"
							/>
						</div>
						<div class="label-and-input">
							<label>Maxiter</label>
							<InputNumber class="p-inputtext-sm" v-model="knobs.maxiter" inputId="integeronly" />
						</div>
						<div class="label-and-input">
							<label>Maxfeval</label>
							<InputNumber class="p-inputtext-sm" v-model="knobs.maxfeval" inputId="integeronly" />
						</div>
					</div>
				</div>
			</tera-drilldown-section>
		</section>
		<section :tabName="OptimizeTabs.Notebook" class="ml-4 mr-2 pt-3">
			<p>Under construction. Use the wizard for now.</p>
		</section>
		<template #preview>
			<tera-drilldown-preview
				title="Simulation output"
				:options="outputs"
				v-model:output="selectedOutputId"
				@update:selection="onSelection"
				:is-loading="showSpinner"
				is-selectable
				class="mr-4 ml-2 mt-3 mb-3"
				:class="{ 'failed-run': optimizationResult.success === 'False' }"
			>
				<!-- Optimize result.json display: -->
				<div
					v-if="optimizationResult && displayOptimizationResultMessage"
					class="result-message-grid"
				>
					<span class="flex flex-row">
						<h6>Response</h6>
						<Button
							icon="pi pi-times"
							text
							rounded
							size="small"
							class="ml-auto p-button-text"
							@click="displayOptimizationResultMessage = !displayOptimizationResultMessage"
						/>
					</span>
					<div v-for="(value, key) in optimizationResult" :key="key" class="result-message-row">
						<div class="label">{{ key }}:</div>
						<div class="value">{{ formatJsonValue(value) }}</div>
					</div>
				</div>
				<SelectButton
					:model-value="outputViewSelection"
					@change="if ($event.value) outputViewSelection = $event.value;"
					:options="outputViewOptions"
					option-value="value"
				>
					<template #option="{ option }">
						<i :class="`${option.icon} p-button-icon-left`" />
						<span class="p-button-label">{{ option.value }}</span>
					</template>
				</SelectButton>
				<tera-notebook-error v-bind="node.state.optimizeErrorMessage" />
				<tera-notebook-error v-bind="node.state.simulateErrorMessage" />
				<template v-if="simulationRunResults[knobs.forecastRunId]">
					<section v-if="outputViewSelection === OutputView.Charts" ref="outputPanel">
						<tera-simulate-chart
							v-for="(cfg, idx) in node.state.chartConfigs"
							:key="idx"
							:run-results="simulationRunResults[knobs.forecastRunId]"
							:chartConfig="{ selectedRun: knobs.forecastRunId, selectedVariable: cfg }"
							has-mean-line
							:size="chartSize"
							@configuration-change="chartProxy.configurationChange(idx, $event)"
							@remove="chartProxy.removeChart(idx)"
							show-remove-button
						/>
						<Button
							class="p-button-sm p-button-text"
							@click="chartProxy.addChart()"
							label="Add chart"
							icon="pi pi-plus"
						/>
						<tera-optimize-chart
							:risk-results="riskResults[knobs.forecastRunId]"
							:chartConfig="{
								selectedRun: knobs.forecastRunId,
								selectedVariable: knobs.targetVariables
							}"
							:target-variable="knobs.targetVariables[0]"
							:size="chartSize"
							:threshold="knobs.threshold"
						/>
					</section>
					<div v-else-if="outputViewSelection === OutputView.Data">
						<tera-dataset-datatable
							v-if="simulationRawContent[knobs.forecastRunId]"
							:rows="10"
							:raw-content="simulationRawContent[knobs.forecastRunId]"
						/>
					</div>
				</template>
			</tera-drilldown-preview>
		</template>
		<template #footer>
			<Button
				:disabled="isRunDisabled"
				outlined
				severity="secondary"
				:style="{ marginRight: 'auto' }"
				label="Run"
				icon="pi pi-play"
				@click="runOptimize"
			/>
			<tera-pyciemss-cancel-button
				:disabled="cancelRunId === ''"
				:simulation-run-id="cancelRunId"
			/>
			<Button
				outlined
				severity="secondary"
				label="Save as a new model configuration"
				@click="showModelModal = true"
			/>
			<tera-save-dataset-from-simulation :simulation-run-id="knobs.forecastRunId" />
			<Button label="Close" @click="emit('close')" />
		</template>
	</tera-drilldown>
	<Dialog
		v-model:visible="showModelModal"
		modal
		header="Save as new model configuration"
		class="save-dialog w-4"
	>
		<div class="label-and-input">
			<label> Model config name</label>
			<InputText v-model="knobs.modelConfigName" />
		</div>
		<div class="label-and-input">
			<label> Model config description</label>
			<InputText v-model="knobs.modelConfigDesc" />
		</div>
		<Button
			:disabled="knobs.modelConfigName === ''"
			outlined
			label="Save as a new model configuration"
			@click="saveModelConfiguration"
		/>
	</Dialog>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ref, onMounted, watch } from 'vue';
// components:
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import MultiSelect from 'primevue/multiselect';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import SelectButton from 'primevue/selectbutton';
import Dialog from 'primevue/dialog';
import TeraOptimizeChart from '@/workflow/tera-optimize-chart.vue';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraInterventionPolicyGroupForm from '@/workflow/ops/optimize-ciemss/tera-intervention-policy-group-form.vue';
import TeraSaveDatasetFromSimulation from '@/components/dataset/tera-save-dataset-from-simulation.vue';
import TeraPyciemssCancelButton from '@/components/pyciemss/tera-pyciemss-cancel-button.vue';
// Services:
import {
	getModelConfigurationById,
	createModelConfiguration
} from '@/services/model-configurations';
import {
	makeOptimizeJobCiemss,
	getRunResultCiemss,
	getRunResult
} from '@/services/models/simulation-service';
import { createCsvAssetFromRunResults } from '@/services/dataset';
// Types:
import {
	ModelConfiguration,
	Model,
	State,
	ModelParameter,
	OptimizeRequestCiemss,
	CsvAsset,
	OptimizedIntervention
} from '@/types/Types';
import { logger } from '@/utils/logger';
import { chartActionsProxy, drilldownChartSize } from '@/workflow/util';
import { RunResults as SimulationRunResults } from '@/types/SimulateConfig';
import { WorkflowNode } from '@/types/workflow';

import TeraNotebookError from '@/components/drilldown/tera-notebook-error.vue';
import {
	OptimizeCiemssOperationState,
	InterventionTypes,
	ContextMethods,
	InterventionPolicyGroup,
	blankInterventionPolicyGroup
} from './optimize-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<OptimizeCiemssOperationState>;
}>();

const emit = defineEmits(['update-state', 'close', 'select-output']);

enum OptimizeTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

enum OutputView {
	Charts = 'Charts',
	Data = 'Data'
}

interface BasicKnobs {
	endTime: number;
	numSamples: number;
	solverMethod: string;
	maxiter: number;
	maxfeval: number;
	qoiMethod: ContextMethods;
	targetVariables: string[];
	riskTolerance: number;
	threshold: number;
	isMinimized: boolean;
	forecastRunId: string;
	optimizationRunId: string;
	modelConfigName: string;
	modelConfigDesc: string;
	interventionType: InterventionTypes;
}

const knobs = ref<BasicKnobs>({
	endTime: props.node.state.endTime ?? 1,
	numSamples: props.node.state.numSamples ?? 0,
	solverMethod: props.node.state.solverMethod ?? '', // Currently not used.
	maxiter: props.node.state.maxiter ?? 5,
	maxfeval: props.node.state.maxfeval ?? 25,
	qoiMethod: props.node.state.qoiMethod ?? ContextMethods.max,
	targetVariables: props.node.state.targetVariables ?? [],
	riskTolerance: props.node.state.riskTolerance ?? 0,
	threshold: props.node.state.threshold ?? 0, // currently not used.
	isMinimized: props.node.state.isMinimized ?? true,
	forecastRunId: props.node.state.forecastRunId ?? '',
	optimizationRunId: props.node.state.optimizationRunId ?? '',
	modelConfigName: props.node.state.modelConfigName ?? '',
	modelConfigDesc: props.node.state.modelConfigDesc ?? '',
	interventionType: props.node.state.interventionType ?? ''
});

const outputPanel = ref(null);
const chartSize = computed(() => drilldownChartSize(outputPanel.value));
const inferredParameters = computed(() => props.node.inputs[1].value);
const cancelRunId = computed(
	() => props.node.state.inProgressForecastId || props.node.state.inProgressOptimizeId
);

const chartProxy = chartActionsProxy(props.node, (state: OptimizeCiemssOperationState) => {
	emit('update-state', state);
});

const showSpinner = computed<boolean>(
	() => props.node.state.inProgressOptimizeId !== '' || props.node.state.inProgressForecastId !== ''
);

const showModelModal = ref(false);
const displayOptimizationResultMessage = ref(true);

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

const isRunDisabled = computed(() => {
	if (
		knobs.value.targetVariables.length === 0 ||
		props.node.state.interventionPolicyGroups.length === 0
	)
		return true;
	return false;
});
const selectedOutputId = ref<string>();

const outputViewSelection = ref(OutputView.Charts);
const outputViewOptions = ref([
	{ value: OutputView.Charts, icon: 'pi pi-image' },
	{ value: OutputView.Data, icon: 'pi pi-list' }
]);
const simulationRunResults = ref<{ [runId: string]: SimulationRunResults }>({});
const riskResults = ref<{ [runId: string]: any }>({});
const simulationRawContent = ref<{ [runId: string]: CsvAsset | null }>({});
const optimizationResult = ref<any>('');

const modelParameterOptions = ref<ModelParameter[]>([]);
const modelStateAndObsOptions = ref<string[]>([]);
const modelConfiguration = ref<ModelConfiguration>();

const showAdditionalOptions = ref(true);

const onSelection = (id: string) => {
	emit('select-output', id);
};

const updateInterventionPolicyGroupForm = (index: number, config: InterventionPolicyGroup) => {
	const state = _.cloneDeep(props.node.state);
	if (!state.interventionPolicyGroups) return;

	state.interventionPolicyGroups[index] = config;
	emit('update-state', state);
};

const deleteInterverntionPolicyGroupForm = (index: number) => {
	const state = _.cloneDeep(props.node.state);
	if (!state.interventionPolicyGroups) return;

	state.interventionPolicyGroups.splice(index, 1);
	emit('update-state', state);
};

const addInterventionPolicyGroupForm = () => {
	const state = _.cloneDeep(props.node.state);
	if (!state.interventionPolicyGroups) return;

	state.interventionPolicyGroups.push(blankInterventionPolicyGroup);
	emit('update-state', state);
};

const toggleAdditonalOptions = () => {
	showAdditionalOptions.value = !showAdditionalOptions.value;
};

const formatJsonValue = (value) => {
	if (typeof value === 'object' && value !== null) {
		return JSON.stringify(value);
	}
	return value;
};

const initialize = async () => {
	const modelConfigurationId = props.node.inputs[0].value?.[0];
	if (!modelConfigurationId) return;
	modelConfiguration.value = await getModelConfigurationById(modelConfigurationId);
	const model = modelConfiguration.value.configuration as Model;

	modelParameterOptions.value = model.semantics?.ode.parameters ?? ([] as ModelParameter[]);
	modelStateAndObsOptions.value = model.model.states.map((ele) => ele.id) ?? ([] as State[]);
	model.semantics?.ode.observables
		?.map((ele) => ele.id)
		.forEach((obs) => modelStateAndObsOptions.value.push(obs));
};

const runOptimize = async () => {
	if (!modelConfiguration.value?.id) {
		logger.error('no model config id provided');
		return;
	}

	const paramNames: string[] = [];
	const paramValues: number[] = [];
	const startTime: number[] = [];
	const listInitialGuessInterventions: number[] = [];
	const listBoundsInterventions: number[][] = [];
	props.node.state.interventionPolicyGroups.forEach((ele) => {
		paramNames.push(ele.parameter);
		paramValues.push(ele.paramValue);
		startTime.push(ele.startTime);
		listInitialGuessInterventions.push(ele.initialGuess);
		listBoundsInterventions.push([ele.lowerBound]);
		listBoundsInterventions.push([ele.upperBound]);
	});

	const optimizeInterventions: OptimizedIntervention = {
		selection: knobs.value.interventionType,
		paramNames,
		startTime,
		paramValues
	};

	const optimizePayload: OptimizeRequestCiemss = {
		userId: 'no_user_provided',
		engine: 'ciemss',
		modelConfigId: modelConfiguration.value.id,
		timespan: {
			start: 0,
			end: knobs.value.endTime
		},
		interventions: optimizeInterventions,
		qoi: {
			contexts: knobs.value.targetVariables,
			method: knobs.value.qoiMethod
		},
		riskBound: knobs.value.threshold,
		initialGuessInterventions: listInitialGuessInterventions,
		boundsInterventions: listBoundsInterventions,
		extra: {
			isMinimized: knobs.value.isMinimized,
			numSamples: knobs.value.numSamples,
			maxiter: knobs.value.maxiter,
			maxfeval: knobs.value.maxfeval,
			alpha: (100 - knobs.value.riskTolerance) / 100,
			solverMethod: knobs.value.solverMethod
		}
	};

	if (inferredParameters.value) {
		optimizePayload.extra.inferredParameters = inferredParameters.value[0];
	}

	const optResult = await makeOptimizeJobCiemss(optimizePayload);
	const state = _.cloneDeep(props.node.state);
	state.inProgressOptimizeId = optResult.simulationId;
	state.optimizationRunId = '';
	state.inProgressForecastId = '';
	emit('update-state', state);
};

const saveModelConfiguration = async () => {
	if (!modelConfiguration.value) return;

	// TODO: This should be taking some values from our output result but its TBD
	const data = await createModelConfiguration(
		modelConfiguration.value.model_id,
		knobs.value.modelConfigName,
		knobs.value.modelConfigDesc,
		modelConfiguration.value.configuration as Model
	);

	if (!data) {
		logger.error('Failed to create model configuration');
		return;
	}

	logger.success('Created model configuration');
	showModelModal.value = false;
};

const setOutputValues = async () => {
	const output = await getRunResultCiemss(knobs.value.forecastRunId);
	simulationRunResults.value[knobs.value.forecastRunId] = output.runResults;
	riskResults.value[knobs.value.forecastRunId] = await getRunResult(
		knobs.value.forecastRunId,
		'risk.json'
	);

	simulationRawContent.value[knobs.value.forecastRunId] = createCsvAssetFromRunResults(
		simulationRunResults.value[knobs.value.forecastRunId]
	);

	const optimzationResult = await getRunResult(
		knobs.value.optimizationRunId,
		'optimize_results.json'
	);
	optimizationResult.value = optimzationResult;
};

onMounted(async () => {
	initialize();
});

watch(
	() => knobs.value,
	async () => {
		const state = _.cloneDeep(props.node.state);
		state.endTime = knobs.value.endTime;
		state.numSamples = knobs.value.numSamples;
		state.solverMethod = knobs.value.solverMethod;
		state.maxiter = knobs.value.maxiter;
		state.maxfeval = knobs.value.maxfeval;
		state.targetVariables = knobs.value.targetVariables;
		state.riskTolerance = knobs.value.riskTolerance;
		state.threshold = knobs.value.threshold;
		state.forecastRunId = knobs.value.forecastRunId;
		state.optimizationRunId = knobs.value.optimizationRunId;
		state.modelConfigName = knobs.value.modelConfigName;
		state.modelConfigDesc = knobs.value.modelConfigDesc;
		state.interventionType = knobs.value.interventionType;
		state.isMinimized = knobs.value.isMinimized;
		state.qoiMethod = knobs.value.qoiMethod;
		emit('update-state', state);
	},
	{ deep: true }
);

watch(
	() => props.node.active,
	() => {
		// Update selected output
		if (props.node.active) {
			selectedOutputId.value = props.node.active;
		}

		// Update knobs with current selected output state
		// timespan.value = props.node.state.currentTimespan;
		// numSamples.value = props.node.state.numSamples;
		// method.value = props.node.state.method;
	},
	{ immediate: true }
);

watch(
	() => knobs.value.forecastRunId,
	async () => {
		if (knobs.value.forecastRunId !== '') {
			setOutputValues();
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
.result-message-grid {
	display: flex;
	flex-direction: column;
	gap: 2px; /* Adjust the gap between rows as needed */
	font-size: var(--font-caption);
	background-color: var(--surface-glass);
	border: solid 1px var(--surface-border-light);
	border-radius: var(--border-radius);
	padding: var(--gap-small);
}

.result-message-row {
	display: flex;
	flex-direction: row;
	gap: var(--gap-small);
}

.label {
	font-weight: bold;
	width: 150px; /* Adjust the width of the label column as needed */
}
.value {
	flex-grow: 1;
}

.failed-run {
	border: 2px solid var(--error-color);
	border-radius: var(--border-radius-big);
	color: var(--error-color-text);
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

.constraint-row {
	display: flex;
	flex-direction: row;
	flex-wrap: wrap;
	align-items: center;
	gap: 0.5rem;

	& > *:first-child {
		flex: 2;
	}

	& > *:not(:first-child) {
		flex: 1;
	}
}
</style>
