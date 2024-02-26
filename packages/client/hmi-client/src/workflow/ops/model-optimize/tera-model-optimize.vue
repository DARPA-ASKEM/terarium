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
						:style="{ width: '100%' }"
						v-model="timeSamples"
						:readonly="true"
						:value="timeSamples"
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
								v-model="statistic"
								placeholder="Select"
							/>
						</div>
						<div class="label-and-input">
							<label for="num-days">Over number of days</label>
							<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="numDays" />
						</div>
					</div>
					<div class="constraint-row">
						<div class="label-and-input">
							<label for="risk-tolerance">Risk tolerance</label>
							<div class="input-and-slider">
								<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="riskTolerance" />
								<Slider v-model="riskTolerance" :min="0" :max="100" :step="1" />
							</div>
						</div>
						<div class="label-and-input">
							<label for="above-or-below">Above or below?</label>
							<Dropdown
								class="p-inputtext-sm"
								:options="['Above', 'Below']"
								v-model="aboveOrBelow"
								placeholder="Select"
							/>
						</div>
						<div class="label-and-input">
							<label for="threshold">Threshold</label>
							<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="threshold" />
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
			<Button outlined label="Save as a new model" @click="saveModel" />
			<Button label="Close" @click="emit('close')" />
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ref, onMounted } from 'vue';
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
import { Model, State, ModelParameter } from '@/types/Types';
import {
	ModelOptimizeOperationState,
	InterventionPolicyGroup,
	blankInterventionPolicyGroup
} from './model-optimize-operation';

const props = defineProps<{
	node: WorkflowNode<ModelOptimizeOperationState>;
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
}

const knobs = ref<BasicKnobs>({
	startTime: props.node.state.startTime ?? 0,
	endTime: props.node.state.endTime ?? 0,
	numTimePoints: props.node.state.numTimePoints ?? 0,
	timeUnit: props.node.state.timeUnit ?? '',
	numStochasticSamples: props.node.state.numStochasticSamples ?? 0,
	solverMethod: props.node.state.solverMethod ?? '',
	targetVariables: props.node.state.targetVariables ?? []
});

const modelParameterOptions = ref<ModelParameter[]>([]);
const modelStateOptions = ref<State[]>([]);

const showAdditionalOptions = ref(true);

const timeSamples = computed<string>(() => {
	const samples: number[] = [];
	const timeStep = (knobs.value.endTime - knobs.value.startTime) / (knobs.value.numTimePoints - 1);
	for (let i = 0; i < knobs.value.numTimePoints; i++) {
		samples.push(Math.round(knobs.value.startTime + i * timeStep));
	}
	return samples.join(', ');
});

// Constraints
const statistic = ref<string>(props.node.state.statistic);
const numDays = ref<number>(props.node.state.numDays);
const riskTolerance = ref<number>(props.node.state.riskTolerance);
const aboveOrBelow = ref<string>(props.node.state.aboveOrBelow);
const threshold = ref<number>(props.node.state.threshold);

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
	const modelConfiguration = await getModelConfigurationById(modelConfigurationId);
	const model = modelConfiguration.configuration as Model;

	modelParameterOptions.value = model.semantics?.ode.parameters ?? [];
	modelStateOptions.value = model.model.states as State[];
	knobs.value.startTime = props.node.state.startTime;
	knobs.value.endTime = props.node.state.endTime;
};

const runOptimize = () => {
	console.log('run optimize');
};

const saveModel = () => {
	console.log('save model');
};

onMounted(async () => {
	initialize();
});
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
