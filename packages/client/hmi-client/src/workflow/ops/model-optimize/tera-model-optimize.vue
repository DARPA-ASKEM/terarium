<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section>
			<div>
				<div class="header p-buttonset">
					<Button
						label="Wizard"
						severity="secondary"
						icon="pi pi-sign-in"
						size="small"
						:active="activeTab === OptimizeTabs.wizard"
						@click="activeTab = OptimizeTabs.wizard"
					/>
					<Button
						label="Notebook"
						severity="secondary"
						icon="pi pi-sign-out"
						size="small"
						:active="activeTab === OptimizeTabs.notebook"
						@click="activeTab = OptimizeTabs.notebook"
					/>
				</div>
				<div class="container">
					<div class="left-side" v-if="activeTab === OptimizeTabs.wizard">
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
					</div>
					<div class="left-side" v-else-if="activeTab === OptimizeTabs.notebook"></div>

					<div class="right-side">
						<h4>Output</h4>
						<img src="@assets/svg/plants.svg" alt="" draggable="false" />
						<h4>No Output</h4>
					</div>
				</div>
			</div>
		</section>
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
import TeraInterventionPolicyGroupForm from '@/components/optimize/tera-intervention-policy-group-form.vue';
import { ModelOptimizeOperationState, InterventionPolicyGroup } from './model-optimize-operation';

const props = defineProps<{
	node: WorkflowNode<ModelOptimizeOperationState>;
}>();

const emit = defineEmits(['append-output-port', 'update-state', 'close']);

enum OptimizeTabs {
	wizard = 'wizard',
	notebook = 'notebook'
}

const activeTab = ref<OptimizeTabs>(OptimizeTabs.wizard);

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

const startTime = ref<number>(props.node.state.startTime);
const endTime = ref<number>(props.node.state.endTime);
const numTimePoints = ref<number>(props.node.state.numTimePoints);
const timeUnit = ref<string>(props.node.state.timeUnit);
const timeSamples = computed<string>(() => {
	const samples = [];
	const timeStep = (endTime.value - startTime.value) / (numTimePoints.value - 1);
	for (let i = 0; i < numTimePoints.value; i++) {
		samples.push(Math.round(startTime.value + i * timeStep));
	}
	return samples.join(', ');
});
const numStochasticSamples = ref<number>(props.node.state.numStochasticSamples);
const solverMethod = ref<string>(props.node.state.solverMethod);

const updateInterventionPolicyGroupForm = (data: InterventionPolicyGroup) => {
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
.container {
	display: flex;
	flex-direction: row;
	margin-top: 1rem;
}

.left-side {
	width: 45%;
	padding-right: 2.5%;
}

.right-side {
	width: 45%;
	padding-left: 2.5%;
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
	justify-content: space-between;
	align-items: center;
	gap: 0.5rem;
}

.input-row > * {
	flex: 1;
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
	/* TODO: this doesn't work properly because InputNumber seems to have a min fixed width*/
	flex-basis: 10%;
}

.input-and-slider > *:nth-child(2) {
	flex-basis: 90%;
}
</style>
