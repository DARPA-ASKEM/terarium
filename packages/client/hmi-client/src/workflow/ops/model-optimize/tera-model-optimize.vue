<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section :tabname="OptimizeTabs.Wizard">
			<div>
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
				<div class="additional-options">
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
			<div>
				<h4>Intervention policy</h4>
				<tera-intervention-policy-group-form
					v-for="(cfg, idx) in props.node.state.interventionPolicyGroups"
					:key="idx"
					:config="cfg"
					:index="idx"
					:model-node-options="modelNodeOptions"
					@update-self="updateInterventionPolicyGroupForm"
					@delete-self="deleteInterverntionPolicyGroupForm"
				/>
			</div>
			<div>
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
		</section>
		<section :tabName="OptimizeTabs.Notebook">
			<h4>Notebook</h4>
		</section>
		<template #preview>
			<tera-drilldown-preview title="Output">
				<img src="@assets/svg/plants.svg" alt="" draggable="false" />
				<h4>No Output</h4>
			</tera-drilldown-preview>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ref } from 'vue';
import Dropdown from 'primevue/dropdown';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import Slider from 'primevue/slider';
import { WorkflowNode } from '@/types/workflow';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraInterventionPolicyGroupForm from '@/components/optimize/tera-intervention-policy-group-form.vue';
import { ModelOptimizeOperationState } from './model-optimize-operation';

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

const updateInterventionPolicyGroupForm = (data) => {
	const state = _.cloneDeep(props.node.state);
	if (!state.interventionPolicyGroups) return;

	state.interventionPolicyGroups[data.index] = data.updatedConfig;
	emit('update-state', state);
};

const deleteInterverntionPolicyGroupForm = (index: number) => {
	const state = _.cloneDeep(props.node.state);
	if (!state.interventionPolicyGroups) return;

	state.interventionPolicyGroups.splice(index, 1);
	emit('update-state', state);
};

const updateState = (updatedField) => {
	let state = _.cloneDeep(props.node.state);
	if (!state.interventionPolicyGroups) return;

	state = { ...state, ...updatedField };
	emit('update-state', state);
};

const debouncedUpdateState = _.debounce(updateState, 500);
</script>

<style scoped>
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
}

.input-row > * {
	flex: 1;
}

.constraint-row {
	display: flex;
	flex-direction: row;
	flex-wrap: wrap;
	align-items: center;
	gap: 0.5rem;
}

.constraint-row > *:first-child {
	flex-basis: 40%;
}

.constraint-row > *:nth-child(2),
.constraint-row > *:nth-child(3) {
	flex-basis: 30%;
}

.label-and-input {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

.additional-options {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: 0.5rem;
}

.additional-options > *:first-child {
	flex-basis: 70%;
}

.additional-options > *:nth-child(2) {
	flex-basis: 30%;
}

.input-and-slider {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: 1rem;
}

.input-and-slider > *:first-child {
	/* TODO: this doesn't work properly because InputNumber seems to have a min fixed width */
	flex-basis: 10%;
}

.input-and-slider > *:nth-child(2) {
	/* TODO: this isn't actually taking up 90% of the space right now */
	flex-basis: 90%;
}
</style>
