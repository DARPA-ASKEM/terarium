<template>
	<div class="container">
		<p>The average value of</p>
		<Dropdown
			class="p-inputtext-sm"
			:options="modelStateAndObsOptions"
			v-model="constraint.targetVariable"
			placeholder="Select"
		/>
		<p>at</p>
		<Dropdown
			class="p-inputtext-sm"
			:options="[
				{ label: 'Max', value: ContextMethods.max },
				{ label: 'Day average', value: ContextMethods.day_average }
			]"
			option-label="label"
			option-value="value"
			v-model="constraint.qoiMethod"
		/>
		<p>over the worst</p>
		<InputNumber class="p-inputtext-sm" inputId="integeronly" v-model="constraint.riskTolerance" />
		<p>% of simulated outcomes</p>
		<Dropdown
			class="toolbar-button"
			v-model="constraint.isMinimized"
			optionLabel="label"
			optionValue="value"
			:options="[
				{ label: 'less than', value: true },
				{ label: 'greater than', value: false }
			]"
		/>
		<p>a threshold of</p>
		<InputNumber
			class="p-inputtext-sm"
			v-model="constraint.threshold"
			:min-fraction-digits="1"
			:max-fraction-digits="10"
		/>
	</div>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref } from 'vue';
import Dropdown from 'primevue/dropdown';
import InputNumber from 'primevue/inputnumber';
import { ConstraintGroup, ContextMethods } from './optimize-ciemss-operation';

const props = defineProps<{
	constraint: ConstraintGroup;
	modelStateAndObsOptions: string[];
}>();

// const emit = defineEmits(['update-self', 'delete-self']);

const constraint = ref<ConstraintGroup>(_.cloneDeep(props.constraint));
</script>

<style scoped>
.container {
	border: 1px solid var(--surface-border);
	width: 100%;
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
</style>
