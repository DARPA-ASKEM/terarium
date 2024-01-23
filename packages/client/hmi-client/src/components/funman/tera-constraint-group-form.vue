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
			<label>Target</label>
			<MultiSelect
				v-model="variables"
				:options="props.modelNodeOptions"
				placeholder="Model states"
				display="chip"
				@update:model-value="
					emit('update-self', { index: props.index, updatedConfig: updatedConfig })
				"
			></MultiSelect>
			<!--
			<label for="weights">Weights</label>
			-->
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
						@update:model-value="
							emit('update-self', { index: props.index, updatedConfig: updatedConfig })
						"
					/>
				</div>
			</div>
		</div>
		<div class="section-row">
			<div class="button-row">
				<label>Start time</label>
				<InputNumber
					class="p-inputtext-sm"
					v-model="startTime"
					@update:model-value="
						emit('update-self', { index: props.index, updatedConfig: updatedConfig })
					"
				/>
			</div>
			<div class="button-row">
				<label>End time</label>
				<InputNumber
					class="p-inputtext-sm"
					v-model="endTime"
					@update:model-value="
						emit('update-self', { index: props.index, updatedConfig: updatedConfig })
					"
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
					@update:model-value="
						emit('update-self', { index: props.index, updatedConfig: updatedConfig })
					"
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
					@update:model-value="
						emit('update-self', { index: props.index, updatedConfig: updatedConfig })
					"
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
const weights = ref(props.config.weights);

const updatedConfig = computed<ConstraintGroup>(
	() =>
		({
			borderColour: props.config.borderColour,
			name: constraintName.value,
			variables: variables.value,
			weights: weights.value,
			timepoints: { lb: startTime.value, ub: endTime.value },
			interval: { lb: lowerBound.value, ub: upperBound.value }
		}) as ConstraintGroup
);

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
</style>
