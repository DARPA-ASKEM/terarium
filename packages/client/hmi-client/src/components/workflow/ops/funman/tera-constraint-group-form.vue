<template>
	<div class="constraint-group">
		<header class="flex w-full gap-3 mb-2">
			<tera-toggleable-input
				v-model="constraintName"
				tag="h3"
				@update:model-value="emit('update-self', updatedConfig)"
			/>
			<div class="ml-auto flex align-items-center">
				<label class="mr-2">Active</label>
				<InputSwitch class="mr-3" />
				<Button icon="pi pi-trash" text rounded @click="emit('delete-self')" />
			</div>
		</header>
		<!-- <div class="button-row">
			<label>Name of constraint</label>
			<tera-input-text
				v-model="constraintName"
				placeholder="Add constraint name"

			/>
		</div> -->
		<p>
			The
			<Dropdown
				:model-value="constraintType"
				:options="constraintTypes"
				option-value="id"
				option-label="name"
				placeholder="Select constraint type"
				@update:model-value="changeConstraintType($event)"
			/>
			<MultiSelect
				v-model="variables"
				:options="props.modelStates"
				placeholder="Model states"
				display="chip"
				@update:model-value="updateChanges()"
			/>
			should be
		</p>
		<div class="section-row">
			<div class="button-row">
				<label>Constraint type</label>
				<Dropdown
					:model-value="constraintType"
					:options="constraintTypes"
					option-value="id"
					option-label="name"
					placeholder="Select constraint type"
					@update:model-value="changeConstraintType($event)"
				/>
			</div>

			<div class="button-row">
				<label>Target</label>
				<MultiSelect
					v-if="constraintType !== 'parameterConstraint'"
					v-model="variables"
					:options="props.modelStates"
					placeholder="Model states"
					display="chip"
					@update:model-value="updateChanges()"
				/>
				<MultiSelect
					v-else
					v-model="variables"
					:options="props.modelParameters"
					placeholder="Model states"
					display="chip"
					@update:model-value="updateChanges()"
				/>
			</div>
		</div>

		<!-- Weights -->
		<div v-for="(variable, index) of variables" :key="index">
			<div class="button-row">
				<label v-if="weights">
					{{ variable + ' Weight' }}
				</label>
				<tera-input-number
					v-if="weights"
					:key="index"
					:placeholder="variable"
					v-model="weights[index]"
					@update:model-value="updateChanges()"
				/>
			</div>
		</div>

		<!-- These are the radio buttons -->
		<section class="radio-buttons" v-if="constraintType === 'monotonicityConstraint'">
			<RadioButton v-model="derivativeType" @update:model-value="updateChanges()" value="increasing" />
			<label class="monoton-label">Increasing</label>
			&nbsp;
			<RadioButton v-model="derivativeType" @update:model-value="updateChanges()" value="decreasing" />
			<label class="monoton-label">Decreasing</label>
		</section>

		<!-- These are the start, end times and upper, lower bounts inputs -->
		<!-- I have cleaned this up a bit to make the fields spaced out better, fitting at full width. Also set the inputtext to md so that they match other fields on the page. -NG -->

		<div v-if="constraintType !== 'monotonicityConstraint'" class="flex-container">
			<div class="input-container">
				<label for="input1" class="label label-padding">Start time</label>
				<tera-input-number
					id="input1"
					class="p-inputtext-md"
					v-model="startTime"
					@update:model-value="updateChanges()"
				/>
			</div>

			<div class="input-container">
				<label for="input2" class="label label-padding">End time</label>
				<tera-input-number id="input2" class="p-inputtext-md" v-model="endTime" @update:model-value="updateChanges()" />
			</div>

			<div class="input-container">
				<label for="input3" class="label label-padding">Lower bound</label>
				<tera-input-number v-model="lowerBound" @update:model-value="updateChanges()" />
			</div>

			<div class="input-container">
				<label for="input4" class="label label-padding">Upper bound</label>
				<tera-input-number v-model="upperBound" @update:model-value="updateChanges()" />
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { watch, ref, computed } from 'vue';
import TeraToggleableInput from '@/components/widgets/tera-toggleable-input.vue';
import MultiSelect from 'primevue/multiselect';
import Dropdown from 'primevue/dropdown';
import RadioButton from 'primevue/radiobutton';
import InputSwitch from 'primevue/inputswitch';
import Button from 'primevue/button';
import { ConstraintGroup } from '@/components/workflow/ops/funman/funman-operation';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';

const props = defineProps<{
	modelStates: string[];
	modelParameters: string[];
	config: ConstraintGroup;
}>();

const emit = defineEmits(['delete-self', 'update-self']);

const constraintName = ref(props.config.name);
const constraintType = ref(props.config.constraintType);
const upperBound = ref(props.config.interval?.ub);
const lowerBound = ref(props.config.interval?.lb);
const startTime = ref(props.config.timepoints?.lb);
const endTime = ref(props.config.timepoints?.ub);
const variables = ref(props.config.variables);
const weights = ref(props.config.weights);
const derivativeType = ref(props.config.derivativeType);

const constraintTypes = [
	{ id: 'stateConstraint', name: 'state variables' },
	{ id: 'parameterConstraint', name: 'parameter' },
	{ id: 'observableConstraint', name: 'observable' }
];

const updatedConfig = computed<ConstraintGroup>(
	() =>
		({
			borderColour: props.config.borderColour,
			name: constraintName.value,
			variables: variables.value,
			weights: weights.value,
			timepoints: { lb: startTime.value, ub: endTime.value },
			interval: { lb: lowerBound.value, ub: upperBound.value },
			constraintType: constraintType.value,
			derivativeType: derivativeType.value
		}) as ConstraintGroup
);

// Changing type should wipe out current settings to avoid weird things from happening
const changeConstraintType = (value: any) => {
	constraintType.value = value;
	weights.value = [];
	variables.value = [];
	startTime.value = 0;
	endTime.value = 100;
	lowerBound.value = 0;
	upperBound.value = 1;

	updateChanges();
};

const updateChanges = () => {
	const wLen = updatedConfig.value.weights?.length ?? 0;
	const vLen = updatedConfig.value.variables.length;
	if (wLen !== vLen) {
		updatedConfig.value.weights = Array<number>(vLen).fill(1.0);
	}

	emit('update-self', updatedConfig.value);
};

watch(
	() => variables.value,
	() => {
		if (!weights.value || weights.value.length === 0) {
			weights.value = Array<number>(props.config.variables.length).fill(1);
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
.constraint-group {
	width: 100%;
	display: flex;
	padding: var(--gap-4);
	flex-direction: column;
	justify-content: center;
	align-items: flex-start;
	background: var(--gray-50);
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	overflow: hidden;
}

.sub-header {
	display: flex;
	padding-bottom: 0px;
	justify-content: flex-end;
	align-items: center;
	gap: 1rem;
	align-self: stretch;
}

.section-row {
	display: flex;
	flex-direction: row;
	padding: var(--gap-small) 0;
	align-items: center;
	gap: 0.5rem;
	width: 100%;
}
.button-row {
	display: flex;
	flex-direction: column;
	padding: var(--gap-small) 0 var(--gap-small) 0;
	width: 100%;
	gap: var(--gap-xsmall);
}

.label-padding {
	margin-bottom: 0.25rem;
}

.age-group {
	display: flex;
	flex-direction: column;
	padding-right: var(--gap-medium);
	padding-bottom: var(--gap-medium);
}

.select-variables {
	display: flex;
	flex-direction: column;
}

.subdued-text {
	color: var(--text-color-subdued);
}

.sub-header {
	display: flex;
	padding-bottom: 0px;
	justify-content: flex-end;
	align-items: center;
	gap: 1rem;
	align-self: stretch;
}

.monoton-label {
	margin-left: 0.25rem;
}

.radio-buttons {
	margin-top: 0.5rem;
}
.flex-container {
	width: 100%;
	display: flex;
	gap: 8px;
	flex-wrap: wrap;
	overflow: invisible;
}

.input-container {
	display: flex;
	flex-direction: column;
}

.label {
	overflow: hidden;
	white-space: nowrap;
	text-overflow: ellipsis;
}
</style>
