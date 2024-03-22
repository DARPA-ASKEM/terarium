<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<template #header-actions>
			<tera-operator-annotation
				:state="node.state"
				@update-state="(state: any) => emit('update-state', state)"
			/>
		</template>
		<section :tabName="OptimizeTabs.Wizard">
			<tera-drilldown-section>
				<div class="form-section">
					<h5>Settings</h5>
					<div class="input-row">
						<div class="label-and-input">
							<label>Start time</label>
							<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="knobs.startTime" />
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
							<label>Number of stochastic samples</label>
							<div class="input-and-slider">
								<InputNumber
									class="p-inputtext-sm"
									inputId="integeronly"
									v-model="knobs.numStochasticSamples"
								/>
							</div>
						</div>
						<div class="label-and-input">
							<label>Solver method</label>
							<Dropdown
								class="p-inputtext-sm"
								:options="['dopri5', 'euler']"
								v-model="knobs.solverMethod"
								placeholder="Select"
							/>
						</div>
						<div class="label-and-input">
							<!-- TODO: This could likely be better explained to user -->
							<label> Minimized</label>
							<Dropdown
								class="toolbar-button"
								v-model="knobs.isMinimized"
								:options="[true, false]"
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
					<tera-intervention-policy-group-form
						v-for="(cfg, idx) in props.node.state.interventionPolicyGroups"
						:key="idx"
						:config="cfg"
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
								:options="modelStateOptions.map((ele) => ele.id)"
								v-model="knobs.targetVariables"
								placeholder="Select"
								filter
							/>
						</div>
					</div>
					<div class="constraint-row">
						<div class="label-and-input">
							<label>Acceptable risk of failure</label>
							<div class="input-and-slider">
								<InputNumber
									class="p-inputtext-sm"
									inputId="integeronly"
									v-model="knobs.riskTolerance"
								/>
								<Slider v-model="knobs.riskTolerance" :min="0" :max="100" :step="1" />
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
					</div>
				</div>
			</tera-drilldown-section>
		</section>
		<section :tabName="OptimizeTabs.Notebook">
			<h5>Notebook</h5>
		</section>
		<template #preview>
			<tera-drilldown-preview
				title="Simulation output"
				:options="outputs"
				v-model:output="selectedOutputId"
				@update:selection="onSelection"
				:is-loading="showSpinner"
				is-selectable
				:class="optimizationResult.success === 'False' ? 'failed-run' : ''"
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
				<template v-if="simulationRunResults[knobs.forecastRunId]">
					<div v-if="outputViewSelection === OutputView.Charts">
						<tera-simulate-chart
							v-for="(cfg, idx) in node.state.chartConfigs"
							:key="idx"
							:run-results="simulationRunResults[knobs.forecastRunId]"
							:chartConfig="{ selectedRun: knobs.forecastRunId, selectedVariable: cfg }"
							has-mean-line
							:size="chartSize"
							@configuration-change="chartProxy.configurationChange(idx, $event)"
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
					</div>
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
			<label> Model Config Name</label>
			<InputText v-model="knobs.modelConfigName" />
		</div>
		<div class="label-and-input">
			<label> Model Config Description</label>
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
import Slider from 'primevue/slider';
import SelectButton from 'primevue/selectbutton';
import Dialog from 'primevue/dialog';
import TeraOptimizeChart from '@/workflow/tera-optimize-chart.vue';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraInterventionPolicyGroupForm from '@/components/optimize/tera-intervention-policy-group-form.vue';
import teraSaveDatasetFromSimulation from '@/components/dataset/tera-save-dataset-from-simulation.vue';
// Services:
import {
	getModelConfigurationById,
	createModelConfiguration
} from '@/services/model-configurations';
import {
	makeOptimizeJobCiemss,
	makeForecastJobCiemss,
	pollAction,
	getRunResultCiemss,
	getRunResult
} from '@/services/models/simulation-service';
import { createCsvAssetFromRunResults } from '@/services/dataset';
import { Poller, PollerState } from '@/api/api';
// Types:
import {
	ModelConfiguration,
	Model,
	State,
	ModelParameter,
	OptimizeRequestCiemss,
	SimulationRequest,
	CsvAsset,
	OptimizedIntervention,
	Intervention as SimulationIntervention
} from '@/types/Types';
import { logger } from '@/utils/logger';
import { chartActionsProxy, drilldownChartSize } from '@/workflow/util';
import { RunResults as SimulationRunResults } from '@/types/SimulateConfig';
import { WorkflowNode } from '@/types/workflow';
import TeraOperatorAnnotation from '@/components/operator/tera-operator-annotation.vue';
import {
	OptimizeCiemssOperation,
	OptimizeCiemssOperationState,
	InterventionPolicyGroup,
	blankInterventionPolicyGroup
} from './optimize-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<OptimizeCiemssOperationState>;
}>();

const emit = defineEmits(['append-output', 'update-state', 'close', 'select-output']);

enum OptimizeTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

enum OutputView {
	Charts = 'Charts',
	Data = 'Data'
}

interface BasicKnobs {
	startTime: number;
	endTime: number;
	numStochasticSamples: number;
	solverMethod: string;
	targetVariables: string[];
	riskTolerance: number;
	threshold: number;
	isMinimized: boolean;
	forecastRunId: string;
	optimzationRunId: string;
	modelConfigName: string;
	modelConfigDesc: string;
}

const knobs = ref<BasicKnobs>({
	startTime: props.node.state.startTime ?? 0,
	endTime: props.node.state.endTime ?? 1,
	numStochasticSamples: props.node.state.numStochasticSamples ?? 0,
	solverMethod: props.node.state.solverMethod ?? '', // Currently not used.
	targetVariables: props.node.state.targetVariables ?? [],
	riskTolerance: props.node.state.riskTolerance ?? 0,
	threshold: props.node.state.threshold ?? 0, // currently not used.
	isMinimized: props.node.state.isMinimized ?? true,
	forecastRunId: props.node.state.forecastRunId ?? '',
	optimzationRunId: props.node.state.optimzationRunId ?? '',
	modelConfigName: props.node.state.modelConfigName ?? '',
	modelConfigDesc: props.node.state.modelConfigDesc ?? ''
});

const outputPanel = ref(null);
const chartSize = computed(() => drilldownChartSize(outputPanel.value));
const chartProxy = chartActionsProxy(props.node, (state: OptimizeCiemssOperationState) => {
	emit('update-state', state);
});

const showSpinner = ref(false);
const poller = new Poller();
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
const policyResult = ref<number[]>();

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
const modelStateOptions = ref<State[]>([]);
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
	modelStateOptions.value = model.model.states ?? ([] as State[]);
};

const runOptimize = async () => {
	if (!modelConfiguration.value?.id) {
		logger.error('no model config id provided');
		return;
	}

	const paramNames: string[] = [];
	const startTime: number[] = [];
	const listInitialGuessInterventions: number[] = [];
	const listBoundsInterventions: number[][] = [];
	props.node.state.interventionPolicyGroups.forEach((ele) => {
		paramNames.push(ele.parameter);
		startTime.push(ele.startTime);
		listInitialGuessInterventions.push(ele.initialGuess);
		listBoundsInterventions.push([ele.lowerBound]);
		listBoundsInterventions.push([ele.upperBound]);
	});

	const optimizeInterventions: OptimizedIntervention = {
		selection: 'param_value',
		paramNames,
		startTime
	};

	const optimizePayload: OptimizeRequestCiemss = {
		userId: 'no_user_provided',
		engine: 'ciemss',
		modelConfigId: modelConfiguration.value.id,
		timespan: {
			start: knobs.value.startTime,
			end: knobs.value.endTime
		},
		interventions: optimizeInterventions,
		qoi: knobs.value.targetVariables,
		riskBound: knobs.value.threshold,
		initialGuessInterventions: listInitialGuessInterventions,
		boundsInterventions: listBoundsInterventions,
		extra: {
			isMinimized: knobs.value.isMinimized,
			numSamples: knobs.value.numStochasticSamples,
			maxiter: 5,
			maxfeval: 5,
			alpha: (100 - knobs.value.riskTolerance) / 100,
			solverMethod: knobs.value.solverMethod
		}
	};

	const optResult = await makeOptimizeJobCiemss(optimizePayload);
	await getOptimizeStatus(optResult.simulationId);
	policyResult.value = await getRunResult(optResult.simulationId, 'policy.json');
	const simulationIntervetions: SimulationIntervention[] = [];

	// This is all index matching for optimizeInterventions.paramNames, optimizeInterventions.startTimes, and policyResult
	for (let i = 0; i < optimizeInterventions.paramNames.length; i++) {
		if (policyResult.value?.at(i) && optimizeInterventions.startTime?.[i]) {
			simulationIntervetions.push({
				name: optimizeInterventions.paramNames[i],
				timestep: optimizeInterventions.startTime[i],
				value: policyResult.value[i]
			});
		}
	}

	const simulationPayload: SimulationRequest = {
		projectId: '',
		modelConfigId: modelConfiguration.value.id,
		timespan: {
			start: knobs.value.startTime,
			end: knobs.value.endTime
		},
		interventions: simulationIntervetions,
		extra: {
			num_samples: knobs.value.numStochasticSamples,
			method: knobs.value.solverMethod
		},
		engine: 'ciemss'
	};

	const simulationResponse = await makeForecastJobCiemss(simulationPayload);
	getStatus(simulationResponse.id);
	const state = _.cloneDeep(props.node.state);
	emit('append-output', {
		type: OptimizeCiemssOperation.outputs[0].type,
		label: `Simulation output - ${props.node.outputs.length + 1}`,
		value: [simulationResponse.id],
		isSelected: false,
		state
	});
};

const getStatus = async (runId: string) => {
	showSpinner.value = true;
	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => pollAction(runId));
	const pollerResults = await poller.start();

	if (pollerResults.state === PollerState.Cancelled) {
		showSpinner.value = false;
		return;
	}
	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		showSpinner.value = false;
		logger.error(`Simulate: ${runId} has failed`, {
			toastTitle: 'Error - Ciemss'
		});
		throw Error('Failed Runs');
	}

	const state = _.cloneDeep(props.node.state);
	if (state.chartConfigs.length === 0) {
		chartProxy.addChart();
	}

	knobs.value.forecastRunId = runId;
	showSpinner.value = false;
};

const getOptimizeStatus = async (runId: string) => {
	showSpinner.value = true;
	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => pollAction(runId));
	const pollerResults = await poller.start();

	if (pollerResults.state === PollerState.Cancelled) {
		showSpinner.value = false;
		return;
	}
	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		showSpinner.value = false;
		logger.error(`Optimize Run: ${runId} has failed`, {
			toastTitle: 'Error - Ciemss'
		});
		throw Error('Failed Runs');
	}

	knobs.value.optimzationRunId = runId;
	showSpinner.value = false;
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
		knobs.value.optimzationRunId,
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
		state.startTime = knobs.value.startTime;
		state.endTime = knobs.value.endTime;
		state.numStochasticSamples = knobs.value.numStochasticSamples;
		state.solverMethod = knobs.value.solverMethod;
		state.targetVariables = knobs.value.targetVariables;
		state.riskTolerance = knobs.value.riskTolerance;
		state.threshold = knobs.value.threshold;
		state.forecastRunId = knobs.value.forecastRunId;
		state.optimzationRunId = knobs.value.optimzationRunId;
		state.modelConfigName = knobs.value.modelConfigName;
		state.modelConfigDesc = knobs.value.modelConfigDesc;
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

.input-and-slider {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: 1rem;

	& > *:first-child {
		/* TODO: this doesn't work properly because InputNumber seems to have a min fixed width */
		flex: 1;
	}

	& > *:nth-child(2) {
		/* TODO: this isn't actually taking up 90% of the space right now */
		flex: 9;
		margin-right: 0.5rem;
	}
}
</style>
