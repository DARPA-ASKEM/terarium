<template>
	<div class="container">
		<div class="form-header">
			<div>
				<tera-input
					v-if="isEditing"
					v-model="config.name"
					placeholder="Constraint name"
					@focusout="emit('update-self', config)"
				/>
				<h6 v-else>{{ props.constraint.name }}</h6>
				<i
					:class="{ 'pi pi-check i': isEditing, 'pi pi-pencil i': !isEditing }"
					:style="'cursor: pointer'"
					@click="onEdit"
				/>
			</div>
			<label for="active">Active</label>
			<InputSwitch v-model="config.isActive" @change="emit('update-self', config)" />
			<i class="trash-button pi pi-trash" @click="emit('delete-self')" />
		</div>
		<div class="section-row">
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
			<tera-input
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
			<tera-input
				class="p-inputtext-sm"
				v-model="config.threshold"
				:min-fraction-digits="1"
				:max-fraction-digits="10"
				@focusout="emit('update-self', config)"
			/>
		</div>
	</div>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref } from 'vue';
import Dropdown from 'primevue/dropdown';
import teraInput from '@/components/widgets/tera-input.vue';
import InputSwitch from 'primevue/inputswitch';
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
	width: 100%;
	display: flex;
	margin-top: var(--gap);
	padding: var(--gap) var(--gap) var(--gap) var(--gap-medium);
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
}

.form-header {
	width: 100%;
	display: flex;
	flex-direction: row;
	justify-content: space-between;
	align-items: center;
	gap: 1rem;
	padding-bottom: 0.5rem;

	& > *:first-child {
		margin-right: auto;
	}

	& > * {
		display: flex;
		flex-direction: row;
		justify-content: space-between;
		align-items: center;
		gap: 0.5rem;
	}
}

.section-row {
	display: flex;
	flex-direction: row;
	flex-wrap: wrap;
	padding: var(--gap-small) 0;
	align-items: center;
	gap: 0.5rem;
	width: 100%;
}
</style>
