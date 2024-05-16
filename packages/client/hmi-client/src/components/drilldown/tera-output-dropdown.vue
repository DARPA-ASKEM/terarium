<template>
	<Dropdown
		v-if="options"
		class="output-dropdown"
		placeholder="Select an output"
		:model-value="output"
		:options="options"
		option-value="id"
		option-group-children="items"
		option-group-label="label"
		@update:model-value="emit('update:selection', $event)"
		:loading="isLoading"
	>
		<template #value="slotProps">
			{{ getLabelById(slotProps.value) }}
			<span class="ml-2 dropdown-option-group">
				{{ getCreateTimeById(slotProps.value) }}
			</span>
		</template>
		<template #optiongroup="slotProps">
			<span class="dropdown-option-group">{{ slotProps.option?.label }}</span>
		</template>
		<template #option="slotProps">
			<div class="dropdown-option">
				<span>{{ slotProps.option?.label }}</span>
				<span class="dropdown-option-group">
					{{ getElapsedTimeText(slotProps.option?.timestamp) }}
				</span>
				<span
					v-if="slotProps.option?.status === WorkflowPortStatus.CONNECTED"
					class="connection-indicator"
					><i class="pi pi-link" />Connected</span
				>
			</div>
		</template>
	</Dropdown>
</template>

<script setup lang="ts">
import { WorkflowOutput, WorkflowPortStatus } from '@/types/workflow';
import Dropdown from 'primevue/dropdown';
import { getElapsedTimeText } from '@/utils/date';

const props = defineProps<{
	options: WorkflowOutput<any>[] | { label: string; items: WorkflowOutput<any>[] }[];
	output: WorkflowOutput<any>['id'];
	isLoading?: boolean;
}>();

const emit = defineEmits(['update:selection']);

const getOptionById = (id, items) => items.find((option) => option.id === id);

const getCreateTimeById = (id: string) => {
	const items = props.options.flatMap((option) => option.items);
	const option = getOptionById(id, items);
	return getElapsedTimeText(option.timestamp);
};

const getLabelById = (id: string) => {
	const items = props.options.flatMap((option) => option.items);
	const option = getOptionById(id, items);
	return option.label;
};
</script>

<style scoped>
.p-dropdown {
	/*FIXME: We may want to truncate the text with ellipsis or something (up to designers) */
	max-width: 20rem;
	align-self: end;
}

.output-dropdown:deep(.p-inputtext) {
	padding: 0.75rem 1rem;
	font-size: var(--font-body-small);
}

.output-dropdown:deep(.pi) {
	font-size: var(--font-body-small);
}

:deep(.p-checkbox .p-checkbox-box) {
	border-radius: var(--border-radius-medium);
}

.dropdown-option {
	display: flex;
	gap: var(--gap-small);
	font-size: var(--font-body-small);
}

.dropdown-option-group {
	display: flex;
	gap: 0.2rem;
	align-items: center;
	font-size: var(--font-caption);
	color: var(--gray-600);
}
.connection-indicator {
	font-size: var(--font-caption);
	color: var(--primary-color);
	display: flex;
	gap: 0.2rem;
	align-items: center;
	margin-left: auto;
}
</style>
