<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section :tabName="OptimizeTabs.Wizard">
			<tera-drilldown-section>
				<div class="form-section">
					<h4>Settings</h4>
					<div class="input-row">
						<div class="label-and-input">
							<label for="start-time">Start time</label>
							<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="knobs.startTime" />
						</div>
						<div class="label-and-input">
							<label for="end-time">End time</label>
							<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="knobs.endTime" />
						</div>
						<div class="label-and-input">
							<label for="num-points">Number of time points</label>
							<InputNumber
								class="p-inputtext-sm"
								inputId="integeronly"
								v-model="knobs.numTimePoints"
							/>
						</div>
						<div class="label-and-input">
							<label for="time-unit">Unit</label>
							<Dropdown
								class="p-inputtext-sm"
								:options="['Days', 'Hours', 'Minutes', 'Seconds']"
								v-model="knobs.timeUnit"
								placeholder="Select"
							/>
						</div>
					</div>
					<InputText
						:disabled="true"
						:style="{ width: '100%' }"
						v-model="timespan"
						:readonly="true"
						:value="timespan"
					/>
					<div>
						<Button
							v-if="showAdditionalOptions"
							class="p-button-sm p-button-text"
							label="Hide additional options"
							@click="toggleAdditonalOptions"
						/>
						<Button
							v-if="!showAdditionalOptions"
							class="p-button-sm p-button-text"
							label="Show additional options"
							@click="toggleAdditonalOptions"
						/>
					</div>
					<div v-if="showAdditionalOptions" class="input-row">
						<div class="label-and-input">
							<label for="num-samples">Number of stochastic samples</label>
							<!-- TOM TODO Add isMinimized dropdown for this -->
							<div class="input-and-slider">
								<InputNumber
									class="p-inputtext-sm"
									inputId="integeronly"
									v-model="knobs.numStochasticSamples"
								/>
								<Slider v-model="knobs.numStochasticSamples" :min="1" :max="100" :step="1" />
							</div>
						</div>
						<div class="label-and-input">
							<label for="solver-method">Solver method</label>
							<Dropdown
								class="p-inputtext-sm"
								:options="['dopri5', 'euler']"
								v-model="knobs.solverMethod"
								placeholder="Select"
							/>
						</div>
						<div class="label-and-input">
							<label> Minimized</label>
							<Dropdown
								class="toolbar-button"
								v-model="knobs.isMinimized"
								:options="[true, false]"
							/>
						</div>
					</div>
				</div>
				<div class="form-section">
					<h4>Intervention policy</h4>
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
					<h4>Constraint</h4>
					<div class="constraint-row">
						<div class="label-and-input">
							<label for="target-variable">Target-variable(s)</label>
							<MultiSelect
								class="p-inputtext-sm"
								:options="modelStateOptions.map((ele) => ele.id)"
								v-model="knobs.targetVariables"
								placeholder="Select"
							/>
						</div>
						<div class="label-and-input">
							<label for="statistic">Statistic</label>
							<Dropdown
								class="p-inputtext-sm"
								:options="['Mean', 'Median']"
								v-model="knobs.statistic"
								placeholder="Select"
							/>
						</div>
						<div class="label-and-input">
							<label for="num-days">Over number of days</label>
							<InputNumber
								class="p-inputtext-sm"
								inputId="integeronly"
								v-model="knobs.numSamples"
							/>
						</div>
					</div>
					<div class="constraint-row">
						<div class="label-and-input">
							<label for="risk-tolerance">Risk tolerance</label>
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
							<label for="above-or-below">Above or below?</label>
							<Dropdown
								class="p-inputtext-sm"
								:options="['Above', 'Below']"
								v-model="knobs.aboveOrBelow"
								placeholder="Select"
							/>
						</div>
						<div class="label-and-input">
							<label for="threshold">Threshold</label>
							<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="knobs.threshold" />
						</div>
					</div>
				</div>
			</tera-drilldown-section>
		</section>
		<section :tabName="OptimizeTabs.Notebook">
			<h4>Notebook</h4>
		</section>
		<template #preview>
			<tera-drilldown-preview title="Output">
				<tera-operator-placeholder :operation-type="node.operationType" />
			</tera-drilldown-preview>
		</template>
		<template #footer>
			<Button
				outlined
				:style="{ marginRight: 'auto' }"
				label="Run"
				icon="pi pi-play"
				@click="runOptimize"
			/>
			<Button outlined label="Save as a new model configuration" @click="saveModelConfiguration" />
			<Button label="Close" @click="emit('close')" />
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ref, onMounted, watch } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import MultiSelect from 'primevue/multiselect';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import Slider from 'primevue/slider';
import { WorkflowNode } from '@/types/workflow';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraInterventionPolicyGroupForm from '@/components/optimize/tera-intervention-policy-group-form.vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { getModelConfigurationById } from '@/services/model-configurations';
import {
	makeOptimizeJobCiemss,
	makeForecastJobCiemss,
	pollAction,
	getSimulation
} from '@/services/models/simulation-service';
import {
	ModelConfiguration,
	Model,
	State,
	ModelParameter,
	OptimizeRequestCiemss,
	SimulationRequest
} from '@/types/Types';
import { Poller, PollerState } from '@/api/api';
import { logger } from '@/utils/logger';
import {
	OptimizeCiemssOperationState,
	InterventionPolicyGroup,
	blankInterventionPolicyGroup
} from './optimize-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<OptimizeCiemssOperationState>;
}>();

const emit = defineEmits(['append-output', 'update-state', 'close']);

enum OptimizeTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

interface BasicKnobs {
	startTime: number;
	endTime: number;
	numTimePoints: number;
	timeUnit: string;
	numStochasticSamples: number;
	solverMethod: string;
	targetVariables: string[];
	statistic: string;
	numSamples: number;
	riskTolerance: number;
	aboveOrBelow: string;
	threshold: number;
	isMinimized: boolean;
}

const knobs = ref<BasicKnobs>({
	startTime: props.node.state.startTime ?? 0,
	endTime: props.node.state.endTime ?? 1,
	numTimePoints: props.node.state.numTimePoints ?? 2,
	timeUnit: props.node.state.timeUnit ?? '',
	numStochasticSamples: props.node.state.numStochasticSamples ?? 0,
	solverMethod: props.node.state.solverMethod ?? '',
	targetVariables: props.node.state.targetVariables ?? [],
	statistic: props.node.state.statistic ?? '',
	numSamples: props.node.state.numSamples ?? 1,
	riskTolerance: props.node.state.riskTolerance ?? 0,
	aboveOrBelow: props.node.state.aboveOrBelow ?? '',
	threshold: props.node.state.threshold ?? 0,
	isMinimized: props.node.state.isMinimized ?? true
});

const showSpinner = ref(false);
const poller = new Poller();
// const progress = ref({ status: ProgressState.Retrieving, value: 0 });
// const completedRunId = ref<string>('');

const modelParameterOptions = ref<ModelParameter[]>([]);
const modelStateOptions = ref<State[]>([]);
const modelConfiguration = ref<ModelConfiguration>();

const showAdditionalOptions = ref(true);

const timespan = computed<string>(() => {
	const samples: number[] = [];
	const timeStep = (knobs.value.endTime - knobs.value.startTime) / (knobs.value.numTimePoints - 1);
	for (let i = 0; i < knobs.value.numTimePoints; i++) {
		samples.push(Math.round(knobs.value.startTime + i * timeStep));
	}
	return samples.join(', ');
});

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

const initialize = async () => {
	const modelConfigurationId = props.node.inputs[0].value?.[0];
	if (!modelConfigurationId) return;
	modelConfiguration.value = await getModelConfigurationById(modelConfigurationId);
	const model = modelConfiguration.value.configuration as Model;

	modelParameterOptions.value = model.semantics?.ode.parameters ?? ([] as ModelParameter[]);
	modelStateOptions.value = model.model.states ?? ([] as State[]);
};

const runOptimize = async () => {
	console.log('run optimize');
	if (!modelConfiguration.value?.id) {
		console.log('no model config id:');
		return;
	}

	const optimizePayload: OptimizeRequestCiemss = {
		userId: 'no_user_provided',
		engine: 'ciemss',
		modelConfigId: modelConfiguration.value.id,
		timespan: {
			start: knobs.value.startTime,
			end: knobs.value.endTime
		},
		interventions: [{ name: 'beta', timestep: 1 }],
		stepSize: 1,
		qoi: knobs.value.targetVariables,
		riskBound: knobs.value.riskTolerance,
		initialGuessInterventions: [0],
		boundsInterventions: [[0]], // TOM TODO
		extra: {
			isMinimized: knobs.value.isMinimized,
			numSamples: knobs.value.numSamples,
			maxiter: 5,
			maxfeval: 5
		}
	};

	// const sampleTest: OptimizeRequestCiemss = {
	// 	engine: 'ciemss',
	// 	userId: 'not_provided',
	// 	modelConfigId: '3c35c95c-c44c-41e7-a30a-8af7fd444d6f',
	// 	interventions: [
	// 		{
	// 			timestep: 1.0,
	// 			name: 'beta'
	// 		}
	// 	],
	// 	timespan: {
	// 		start: 0,
	// 		end: 90
	// 	},
	// 	qoi: ['Infected'],
	// 	riskBound: 10.0,
	// 	initialGuessInterventions: [1.0],
	// 	boundsInterventions: [[0.0], [3.0]],
	// 	extra: {
	// 		numSamples: 4,
	// 		isMinimized: true
	// 	}
	// };

	const optResult = await makeOptimizeJobCiemss(optimizePayload);
	// await getStatus(optResult.id);
	// TOM TODO: Use getStatus and get run results. Will need them.
	// policy.json, optimize_results.dill

	console.log(optResult);
	const policyResult = [1.0]; // TOM TODO, read policy.json for value

	const simulationPayload: SimulationRequest = {
		projectId: '',
		modelConfigId: modelConfiguration.value.id,
		timespan: {
			start: knobs.value.startTime,
			end: knobs.value.endTime
		},
		extra: {
			num_samples: knobs.value.numSamples,
			method: knobs.value.solverMethod,
			inferredParameters: policyResult
		},
		engine: 'ciemss'
	};

	const simulationResponse = await makeForecastJobCiemss(simulationPayload);
	console.log('Simulation Response:');
	console.log(simulationResponse);
	getStatus(simulationResponse.id);
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
		console.log(pollerResults.state);
		// throw if there are any failed runs for now
		showSpinner.value = false;
		logger.error(`Simulate: ${runId} has failed`, {
			toastTitle: 'Error - Julia'
		});
		throw Error('Failed Runs');
	}

	// const state = _.cloneDeep(props.node.state);
	// if (state.chartConfigs.length === 0) {
	// 	addChart();
	// }

	const sim = await getSimulation(runId);
	console.log(sim);
	// emit('append-output', {
	// 	type: OptimizeCiemssOperation.outputs[0].type,
	// 	label: `Output - ${props.node.outputs.length + 1}`,
	// 	value: runId,
	// 	state: {
	// 		currentTimespan: sim?.executionPayload.timespan ?? timespan.value,
	// 		simulationsInProgress: state.simulationsInProgress
	// 	},
	// 	isSelected: false
	// });

	showSpinner.value = false;
};

const saveModelConfiguration = () => {
	console.log('save model Configuration');
	// This will be a model configuration
	// that is essentially a duplicate of the inpiut model config
	// But there will be changes to the model configuration parameter values.
	// These changes will come from the intervention policy (aka policy.json)
	// follow this thread: https://askemgroup.slack.com/archives/C03U5FBRSQG/p1709139052459319

	// const state = _.cloneDeep(props.node.state);
	// if (state.chartConfigs.length === 0) {
	// 	addChart();
	// }

	// const sim = await getSimulation(runId);

	// emit('append-output', {
	// 	type: OptimizeCiemssOperation.outputs[0].type,
	// 	label: `Output - ${props.node.outputs.length + 1}`,
	// 	value: runId,
	// 	state: {
	// 		currentTimespan: sim?.executionPayload.timespan ?? timespan.value,
	// 		numSamples: sim?.executionPayload.extra.num_samples ?? knobs.value.numSamples,
	// 		method: sim?.executionPayload.extra.method ?? knobs.value.solverMethod,
	// 		simulationsInProgress: state.simulationsInProgress
	// 	},
	// 	isSelected: false
	// });
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
		state.numTimePoints = knobs.value.numTimePoints;
		state.timeUnit = knobs.value.timeUnit;
		state.numStochasticSamples = knobs.value.numStochasticSamples;
		state.solverMethod = knobs.value.solverMethod;
		state.targetVariables = knobs.value.targetVariables;
		state.statistic = knobs.value.statistic;
		state.numSamples = knobs.value.numSamples;
		state.riskTolerance = knobs.value.riskTolerance;
		state.aboveOrBelow = knobs.value.aboveOrBelow;
		state.threshold = knobs.value.threshold;
		emit('update-state', state);
	},
	{ deep: true }
);
</script>

<style scoped>
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
