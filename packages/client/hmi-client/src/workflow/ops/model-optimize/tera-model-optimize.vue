<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section :tabName="OptimizeTabs.Wizard">
			<tera-drilldown-section>
				<div class="form-section">
					<h4>Settings</h4>
					<div class="input-row">
						<div class="label-and-input">
							<label for="start-time">Start time</label>
							<InputNumber
								class="p-inputtext-sm"
								inputId="integeronly"
								v-model="startTime"
								@update:model-value="() => updateState({ startTime })"
							/>
						</div>
						<div class="label-and-input">
							<label for="end-time">End time</label>
							<InputNumber
								class="p-inputtext-sm"
								inputId="integeronly"
								v-model="endTime"
								@update:model-value="() => updateState({ endTime })"
							/>
						</div>
						<div class="label-and-input">
							<label for="num-points">Number of time points</label>
							<InputNumber
								class="p-inputtext-sm"
								inputId="integeronly"
								v-model="numTimePoints"
								@update:model-value="() => updateState({ numTimePoints })"
							/>
						</div>
						<div class="label-and-input">
							<label for="time-unit">Unit</label>
							<Dropdown
								class="p-inputtext-sm"
								:options="['Days', 'Hours', 'Minutes', 'Seconds']"
								v-model="timeUnit"
								placeholder="Select"
								@update:model-value="() => updateState({ timeUnit })"
							/>
						</div>
					</div>
					<InputText
						:style="{ width: '100%' }"
						v-model="timeSamples"
						:readonly="true"
						:value="timeSamples"
					/>
					<p v-if="showAdditionalOptions" class="text-button" @click="toggleAdditonalOptions">
						Hide additional options
					</p>
					<p v-if="!showAdditionalOptions" class="text-button" @click="toggleAdditonalOptions">
						Show additional options
					</p>
					<div v-if="showAdditionalOptions" class="input-row">
						<div class="label-and-input">
							<label for="num-samples">Number of stochastic samples</label>
							<div class="input-and-slider">
								<InputNumber
									class="p-inputtext-sm"
									inputId="integeronly"
									v-model="numStochasticSamples"
									@update:model-value="() => updateState({ numStochasticSamples })"
								/>
								<Slider
									v-model="numStochasticSamples"
									:min="1"
									:max="100"
									:step="1"
									@change="() => debouncedUpdateState({ numStochasticSamples })"
								/>
							</div>
						</div>
						<div class="label-and-input">
							<label for="solver-method">Solver method</label>
							<Dropdown
								class="p-inputtext-sm"
								:options="['dopri5', 'euler']"
								v-model="solverMethod"
								placeholder="Select"
								@update:model-value="() => updateState({ solverMethod })"
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
						:model-node-options="modelNodeOptions"
						@update-self="(config) => updateInterventionPolicyGroupForm(idx, config)"
						@delete-self="() => deleteInterverntionPolicyGroupForm(idx)"
					/>
					<p class="text-button" @click="addInterventionPolicyGroupForm">
						+ Add more interventions
					</p>
				</div>
				<div class="form-section">
					<h4>Constraint</h4>
					<div class="constraint-row">
						<div class="label-and-input">
							<label for="target-variable">Target-variables</label>
							<Dropdown
								class="p-inputtext-sm"
								:options="['S', 'I', 'R']"
								v-model="targetVariable"
								placeholder="Select"
								@update:model-value="() => updateState({ targetVariable })"
							/>
						</div>
						<div class="label-and-input">
							<label for="statistic">Statistic</label>
							<Dropdown
								class="p-inputtext-sm"
								:options="['Mean', 'Median']"
								v-model="statistic"
								placeholder="Select"
								@update:model-value="() => updateState({ statistic })"
							/>
						</div>
						<div class="label-and-input">
							<label for="num-days">Over number of days</label>
							<InputNumber
								class="p-inputtext-sm"
								inputId="integeronly"
								v-model="numDays"
								@update:model-value="() => updateState({ numDays })"
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
									v-model="riskTolerance"
									@update:model-value="() => updateState({ riskTolerance })"
								/>
								<Slider
									v-model="riskTolerance"
									:min="0"
									:max="100"
									:step="1"
									@change="() => debouncedUpdateState({ riskTolerance })"
								/>
							</div>
						</div>
						<div class="label-and-input">
							<label for="above-or-below">Above or below?</label>
							<Dropdown
								class="p-inputtext-sm"
								:options="['Above', 'Below']"
								v-model="aboveOrBelow"
								placeholder="Select"
								@update:model-value="() => updateState({ aboveOrBelow })"
							/>
						</div>
						<div class="label-and-input">
							<label for="threshold">Threshold</label>
							<InputNumber
								class="p-inputtext-sm"
								inputId="integeronly"
								v-model="threshold"
								@update:model-value="() => updateState({ threshold })"
							/>
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
				<tera-operator-placeholder-graphic :operation-type="node.operationType" />
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
import { computed, ref } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import Slider from 'primevue/slider';
import { WorkflowNode } from '@/types/workflow';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraInterventionPolicyGroupForm from '@/components/optimize/tera-intervention-policy-group-form.vue';
import TeraOperatorPlaceholderGraphic from '@/workflow/operator/tera-operator-placeholder-graphic.vue';
import {
	ModelOptimizeOperationState,
	InterventionPolicyGroup,
	blankInterventionPolicyGroup
} from './model-optimize-operation';

const props = defineProps<{
	node: WorkflowNode<ModelOptimizeOperationState>;
}>();

const emit = defineEmits(['append-output-port', 'update-state', 'close']);

enum OptimizeTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

interface PolicyDropdowns {
	parameters: string[];
	goals: string[];
	costBenefitFns: string[];
}

const modelNodeOptions = ref<PolicyDropdowns>({
	parameters: ['beta', 'gamma'],
	goals: ['Minimize', 'Maximize'],
	costBenefitFns: ['L1 Norm', 'L2 Norm']
});

const showAdditionalOptions = ref(true);

// Settings
const startTime = ref<number>(props.node.state.startTime);
const endTime = ref<number>(props.node.state.endTime);
const numTimePoints = ref<number>(props.node.state.numTimePoints);
const timeUnit = ref<string>(props.node.state.timeUnit);
const timeSamples = computed<string>(() => {
	const samples: number[] = [];
	const timeStep = (endTime.value - startTime.value) / (numTimePoints.value - 1);
	for (let i = 0; i < numTimePoints.value; i++) {
		samples.push(Math.round(startTime.value + i * timeStep));
	}
	return samples.join(', ');
});
const numStochasticSamples = ref<number>(props.node.state.numStochasticSamples);
const solverMethod = ref<string>(props.node.state.solverMethod);

// Constraints
const targetVariable = ref<string>(props.node.state.targetVariable);
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

const updateState = (updatedField) => {
	let state = _.cloneDeep(props.node.state);
	if (!state.interventionPolicyGroups) return;

	state = { ...state, ...updatedField };
	emit('update-state', state);
};

const debouncedUpdateState = _.debounce(updateState, 500);

const runOptimize = () => {
	console.log('run optimize');
};

const saveModel = () => {
	console.log('save model');
};
</script>

<style scoped>
.text-button {
	color: var(--Primary, #1b8073);
}

.text-button:hover {
	cursor: pointer;
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
