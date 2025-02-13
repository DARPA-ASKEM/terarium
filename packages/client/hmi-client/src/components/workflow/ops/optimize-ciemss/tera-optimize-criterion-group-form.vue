<template>
	<div class="container">
		<div class="form-header">
			<div>
				<tera-input-text
					v-if="isEditing"
					v-model="config.name"
					placeholder="Criterion"
					@update:model-value="emit('update-self', config)"
				/>
				<h6 v-else>{{ props.criterion.name }}</h6>
				<i
					:class="{ 'pi pi-check i': isEditing, 'pi pi-pencil i text-xs text-color-secondary': !isEditing }"
					:style="'cursor: pointer'"
					@click="onEdit"
				/>
			</div>
			<label for="active">Active</label>
			<InputSwitch v-model="config.isActive" @change="emit('update-self', config)" />
			<i class="trash-button pi pi-trash" @click="emit('delete-self')" />
		</div>
		<div v-if="config.isActive" class="section-row">
			Ensure
			<Dropdown
				class="p-inputtext-sm"
				:options="modelStateAndObsOptions"
				option-label="label"
				option-value="value"
				v-model="config.targetVariable"
				placeholder="Select"
				@update:model-value="emit('update-self', config)"
			/>
			<i v-if="_.isEmpty(config.targetVariable)" class="pi pi-exclamation-circle" v-tooltip="requiredTooltip" />
			is
			<Dropdown
				class="toolbar-button"
				v-model="config.isMinimized"
				optionLabel="label"
				optionValue="value"
				:options="[
					{ label: 'below', value: true },
					{ label: 'above', value: false }
				]"
				@update:model-value="emit('update-self', config)"
			/>
			a threshold of
			<tera-input-number v-model="config.threshold" @update:model-value="emit('update-self', config)" />
			<i v-if="!config.threshold" class="pi pi-exclamation-circle" v-tooltip="requiredTooltip" />
			at
			<Dropdown
				:options="[
					{ label: 'all timepoints', value: ContextMethods.max },
					{ label: 'last timepoint', value: ContextMethods.day_average }
				]"
				option-label="label"
				option-value="value"
				v-model="config.qoiMethod"
				@update:model-value="emit('update-self', config)"
			/>
			in
			<tera-input-number v-model="config.riskTolerance" @update:model-value="emit('update-self', config)" /><i
				v-if="!config.riskTolerance"
				class="pi pi-exclamation-circle"
				v-tooltip="requiredTooltip"
			/>% of simulated outcomes
		</div>
		<div v-else class="section-row">
			Ensure <b>{{ config.targetVariable }}</b> is <b>{{ config.isMinimized ? 'below' : 'above' }}</b> a threshold of
			<b>{{ config.threshold }}</b> at <b>{{ config.qoiMethod }}</b> in <b>{{ config.riskTolerance }}%</b> of simulate
			outcomes
		</div>
	</div>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, watch } from 'vue';
import Dropdown from 'primevue/dropdown';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import InputSwitch from 'primevue/inputswitch';
import { Criterion, ContextMethods } from './optimize-ciemss-operation';

const props = defineProps<{
	criterion: Criterion;
	modelStateAndObsOptions: { label: string; value: string }[];
}>();

const emit = defineEmits(['update-self', 'delete-self']);

const config = ref<Criterion>(_.cloneDeep(props.criterion));
const requiredTooltip = 'Required';
const isEditing = ref<boolean>(false);

const onEdit = () => {
	isEditing.value = !isEditing.value;
};

watch(
	() => props.criterion,
	() => {
		config.value = _.cloneDeep(props.criterion);
	},
	{ immediate: true }
);
</script>

<style scoped>
.container {
	width: 100%;
	display: flex;
	padding: var(--gap-4);
	flex-direction: column;
	justify-content: center;
	align-items: flex-start;
	border-radius: var(--border-radius);
	margin: var(--gap-1) 0;
	background: #fff;
	border: 1px solid rgba(0, 0, 0, 0.08);
	/* Shadow/medium */
	box-shadow:
		0 2px 4px -1px rgba(0, 0, 0, 0.06),
		0 4px 6px -1px rgba(0, 0, 0, 0.08);
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
	padding: var(--gap-2) 0;
	align-items: center;
	gap: 0.5rem;
	width: 100%;
}
</style>
