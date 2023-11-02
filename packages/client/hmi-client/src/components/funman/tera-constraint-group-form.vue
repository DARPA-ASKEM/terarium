<template>
	<div class="constraint-group" :style="`border-left: 9px solid ${props.config.borderColour}`">
		<div class="first-row">
			<label for="constraint-name">Name of constraint</label>
			<InputText
				id="constraint-name"
				v-model="constraintName"
				placeholder="Add constraint name"
				@focusout="emit('update-self', { index: props.index, updatedConfig: updatedConfig })"
			/>
			<label for="target">Target</label>
			<MultiSelect
				id="variables-select"
				v-model="variables"
				:options="props.modelNodeOptions"
				placeholder="Model states"
				display="chip"
				@update:model-value="
					emit('update-self', { index: props.index, updatedConfig: updatedConfig })
				"
			></MultiSelect>
		</div>
		<div class="second-row">
			<label for="start">Start time</label>
			<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="startTime" />
			<label for="end">End time</label>
			<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="endTime" />
			<label for="lower">Lower bound</label>
			<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="lowerBound" />
			<label for="upper">Upper bound</label>
			<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="upperBound" />
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import MultiSelect from 'primevue/multiselect';
// import InputSwitch from 'primevue/inputswitch';
import { ConstraintGroup } from '@/workflow/ops/funman/funman-operation';

const props = defineProps<{
	modelNodeOptions: string[];
	config: ConstraintGroup;
	index: number;
}>();

const emit = defineEmits(['delete-self', 'update-self']);

const constraintName = ref(props.config.name);
const upperBound = ref(props.config.interval?.ub);
const lowerBound = ref(props.config.interval?.lb);
const startTime = ref(props.config.timepoints?.lb);
const endTime = ref(props.config.timepoints?.ub);
const variables = ref(props.config.variables);

const updatedConfig = computed<ConstraintGroup>(
	() =>
		({
			borderColour: props.config.borderColour,
			name: constraintName.value,
			variables: variables.value,
			currentTimespan: { start: startTime.value, end: endTime.value },
			interval: { lb: lowerBound.value, ub: upperBound.value }
		} as ConstraintGroup)
);
</script>

<style>
.constraint-group {
	margin: 1rem;
	display: flex;
	padding: 1rem 1rem 1rem 1.5rem;
	flex-direction: column;
	justify-content: center;
	align-items: flex-start;
	border-radius: 0.375rem;
	background: #fff;
	/* Shadow/medium */
	box-shadow: 0px 2px 4px -1px rgba(0, 0, 0, 0.06), 0px 4px 6px -1px rgba(0, 0, 0, 0.08);
}

.sub-header {
	display: flex;
	padding-bottom: 0px;
	justify-content: flex-end;
	align-items: center;
	gap: 1rem;
	align-self: stretch;
}

.first-row {
	display: flex;
}
.age-group {
	display: flex;
	flex-direction: column;
	padding-right: 2rem;
	padding-bottom: 1rem;
}

.select-variables {
	display: flex;
	flex-direction: column;
}

.subdued-text {
	color: var(--text-color-subdued);
}

.trash-button {
	cursor: pointer;
}
</style>
