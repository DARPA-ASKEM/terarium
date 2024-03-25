<template>
	<div class="constraint-group" :style="`border-left: 9px solid ${props.config.borderColour}`">
		<div>
			<i class="trash-button pi pi-trash" @click="emit('delete-self', { index: props.index })" />
		</div>
		<div class="button-row">
			<label>Name of constraint</label>
			<InputText
				v-model="constraintName"
				placeholder="Add constraint name"
				@focusout="emit('update-self', { index: props.index, updatedConfig: updatedConfig })"
			/>

			<div style="display: flex; flex-direction: row; gap: 0.5rem">
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
						v-model="variables"
						:options="props.modelNodeOptions"
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
					<InputNumber
						v-if="weights"
						:key="index"
						:placeholder="variable"
						mode="decimal"
						:min-fraction-digits="3"
						:max-fraction-digits="3"
						v-model="weights[index]"
						@update:model-value="updateChanges()"
					/>
				</div>
			</div>

			<section v-if="constraintType === 'monotonicityConstraint'">
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
		</div>
		<div class="section-row" v-if="constraintType !== 'monotonicityConstraint'">
			<div class="button-row">
				<label>Start time</label>
				<InputNumber
					class="p-inputtext-sm"
					v-model="startTime"
					@update:model-value="updateChanges()"
				/>
			</div>
			<div class="button-row">
				<label>End time</label>
				<InputNumber
					class="p-inputtext-sm"
					v-model="endTime"
					@update:model-value="updateChanges()"
				/>
			</div>
			<div class="button-row">
				<label>Lower bound</label>
				<InputNumber
					class="p-inputtext-sm"
					mode="decimal"
					:min-fraction-digits="3"
					:max-fraction-digits="3"
					v-model="lowerBound"
					@update:model-value="updateChanges()"
				/>
			</div>
			<div class="button-row">
				<label>Upper bound</label>
				<InputNumber
					class="p-inputtext-sm"
					mode="decimal"
					:min-fraction-digits="3"
					:max-fraction-digits="3"
					v-model="upperBound"
					@update:model-value="updateChanges()"
				/>
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

const props = defineProps<{
	modelNodeOptions: string[];
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

.button-row {
	display: flex;
	flex-direction: column;
	padding: 1rem 0rem 0.5rem 0rem;
	align-items: flex-start;
	align-self: stretch;
}

.section-row {
	display: flex;
	/* flex-direction: column; */
	padding: 0.5rem 0rem;
	align-items: center;
	gap: 0.5rem;
	align-self: stretch;
	flex-wrap: wrap;
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

.monoton-label {
	margin-left: 0.25rem;
}
</style>
