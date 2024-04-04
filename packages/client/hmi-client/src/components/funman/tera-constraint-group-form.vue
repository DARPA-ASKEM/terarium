<template>
	<div class="constraint-group" :style="`border-left: 9px solid ${props.config.borderColour}`">
		<div class="trash-button-align">
			<i class="trash-button pi pi-trash" @click="emit('delete-self', { index: props.index })" />
		</div>

		<div class="button-row">
			<label class="label-padding">Name of constraint</label>
			<InputText
				v-model="constraintName"
				placeholder="Add constraint name"
				@focusout="emit('update-self', { index: props.index, updatedConfig: updatedConfig })"
			/>
		</div>

		<div class="section-row">
			<div class="button-row">
				<label class="label-padding">Constraint type</label>
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
				<label class="label-padding">Target</label>
				<MultiSelect
					v-if="constraintType !== 'parameterConstraint'"
					v-model="variables"
					:options="props.modelStates"
					placeholder="Model states"
					display="chip"
					@update:model-value="updateChanges()"
				></MultiSelect>
				<MultiSelect
					v-else
					v-model="variables"
					:options="props.modelParameters"
					placeholder="Model states"
					display="chip"
					@update:model-value="updateChanges()"
				></MultiSelect>
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
					:min-fraction-digits="3"
					:max-fraction-digits="3"
					v-model="weights[index]"
					@update:model-value="updateChanges()"
				/>
			</div>
		</div>

		<!-- These are the radio buttons -->
		<section class="radio-buttons" v-if="constraintType === 'monotonicityConstraint'">
			<RadioButton
				v-model="derivativeType"
				@update:model-value="updateChanges()"
				value="increasing"
			/>
			<label class="monoton-label">Increasing</label>
			&nbsp;
			<RadioButton
				v-model="derivativeType"
				@update:model-value="updateChanges()"
				value="decreasing"
			/>
			<label class="monoton-label">Decreasing</label>
		</section>

		<!-- These are the start, end times and upper, lower bounts inputs -->
		<div v-if="constraintType !== 'monotonicityConstraint'" class="w-full">
			<div class="flex flex-row w-full pt-3 pb-1">
				<div class="col-3 p-0 flex flex-column pr-2">
					<label class="label-padding">Start time</label>
					<InputNumber
						class="p-inputtext-sm"
						v-model="startTime"
						@update:model-value="updateChanges()"
					/>
				</div>

				<div class="col-3 p-0 flex flex-column pr-2">
					<label class="label-padding">End time</label>
					<InputNumber
						class="p-inputtext-sm"
						v-model="endTime"
						@update:model-value="updateChanges()"
					/>
				</div>

				<div class="col-3 p-0 flex flex-column pr-2">
					<label class="label-padding">Lower bound</label>
					<tera-input-number
						class="p-inputtext-sm"
						v-model="lowerBound"
						:min-fraction-digits="3"
						:max-fraction-digits="12"
						@update:model-value="updateChanges()"
					/>
				</div>

				<div class="col-3 p-0 flex flex-column">
					<label class="label-padding">Upper bound</label>
					<tera-input-number
						class="p-inputtext-sm"
						v-model="upperBound"
						:min-fraction-digits="3"
						:max-fraction-digits="12"
						@update:model-value="updateChanges()"
					/>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { watch, ref, computed } from 'vue';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import MultiSelect from 'primevue/multiselect';
import Dropdown from 'primevue/dropdown';
import RadioButton from 'primevue/radiobutton';
import { ConstraintGroup } from '@/workflow/ops/funman/funman-operation';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';

const props = defineProps<{
	modelStates: string[];
	modelParameters: string[];
	config: ConstraintGroup;
	index: number;
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
	{ id: 'stateConstraint', name: 'State constraint' },
	{ id: 'monotonicityConstraint', name: 'Monotonicity constraint' },
	{ id: 'parameterConstraint', name: 'Parameter constraint' }
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
	emit('update-self', { index: props.index, updatedConfig: updatedConfig.value });
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
	margin-top: 1rem;
	display: flex;
	padding: 1rem 1rem 1rem 1.5rem;
	flex-direction: column;
	justify-content: center;
	align-items: flex-start;
	border-radius: 0.375rem;
	background: #fff;
	border: 1px solid rgba(0, 0, 0, 0.08);
	/* Shadow/medium */
	box-shadow:
		0px 2px 4px -1px rgba(0, 0, 0, 0.06),
		0px 4px 6px -1px rgba(0, 0, 0, 0.08);
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
	padding: 0.5rem 0rem;
	align-items: center;
	gap: 0.5rem;
	width: 100%;
}
.button-row {
	display: flex;
	flex-direction: column;
	padding: 1rem 0rem 0.5rem 0rem;
	width: 100%;
}
.age-group {
	display: flex;
	flex-direction: column;
	padding-right: 2rem;
	padding-bottom: rem;
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

.trash-button {
	cursor: pointer;
}
.label-padding {
	margin-bottom: 0.25rem;
}

.monoton-label {
	margin-left: 0.25rem;
}

.radio-buttons {
	margin-top: 0.5rem;
}
.trash-button-align {
	display: flex;
	padding-bottom: 0px;
	justify-content: flex-end;
	align-items: center;
	gap: 1rem;
	align-self: stretch;
}
</style>
