<template>
	<div class="container">
		<div>
			<InputText
				v-if="isEditing"
				v-model="config.name"
				placeholder="Policy bounds"
				@focusout="emit('update-self', config)"
			/>
			<h6 v-else>{{ props.constraint.name }}</h6>
			<i
				:class="{ 'pi pi-check i': isEditing, 'pi pi-pencil i': !isEditing }"
				:style="'cursor: pointer'"
				@click="onEdit"
			/>
		</div>
		<div class="trash-button-align">
			<i class="trash-button pi pi-trash" @click="emit('delete-self')" />
		</div>
		<p>The average value of</p>
		<Dropdown
			class="p-inputtext-sm"
			:options="modelStateAndObsOptions"
			v-model="config.targetVariable"
			placeholder="Select"
			@update:model-value="emit('update-self', config)"
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
			v-model="config.qoiMethod"
			@update:model-value="emit('update-self', config)"
		/>
		<p>over the worst</p>
		<InputNumber
			class="p-inputtext-sm"
			inputId="integeronly"
			v-model="config.riskTolerance"
			@focusout="emit('update-self', config)"
		/>
		<p>% of simulated outcomes</p>
		<Dropdown
			class="toolbar-button"
			v-model="config.isMinimized"
			optionLabel="label"
			optionValue="value"
			:options="[
				{ label: 'less than', value: true },
				{ label: 'greater than', value: false }
			]"
			@update:model-value="emit('update-self', config)"
		/>
		<p>a threshold of</p>
		<InputNumber
			class="p-inputtext-sm"
			v-model="config.threshold"
			:min-fraction-digits="1"
			:max-fraction-digits="10"
			@focusout="emit('update-self', config)"
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

const emit = defineEmits(['update-self', 'delete-self']);

const config = ref<ConstraintGroup>(_.cloneDeep(props.constraint));

const isEditing = ref<boolean>(false);

const onEdit = () => {
	isEditing.value = !isEditing.value;
};
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
