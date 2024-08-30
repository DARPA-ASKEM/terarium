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
			<div class="dropdown-option">
				<span>{{ getLabelById(slotProps.value) }}</span>
				<span class="timestamp"> {{ getCreateTimeById(slotProps.value) }} </span>
			</div>
		</template>
		<template #optiongroup="slotProps">
			<span class="timestamp">{{ slotProps.option?.label }}</span>
		</template>
		<template #option="slotProps">
			<div class="dropdown-option">
				<span>{{ slotProps.option?.label }}</span>
				<span class="timestamp">
					{{ getElapsedTimeText(slotProps.option?.timestamp) }}
				</span>
				<span v-if="slotProps.option?.status === WorkflowPortStatus.CONNECTED" class="connection-indicator"
					><i class="pi pi-link" />Connected</span
				>
			</div>
		</template>
	</Dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { WorkflowOutput, WorkflowPortStatus } from '@/types/workflow';
import Dropdown from 'primevue/dropdown';
import { getElapsedTimeText } from '@/utils/date';

const props = defineProps<{
	options: WorkflowOutput<any>[] | { label: string; items: WorkflowOutput<any>[] }[];
	output: WorkflowOutput<any>['id'];
	isLoading?: boolean;
}>();

const emit = defineEmits(['update:selection']);
const items = computed(() => props.options.flatMap((option) => option.items));

const getOptionById = (id: string) => items.value.find((option) => option.id === id);

const getCreateTimeById = (id: string) => {
	const option = getOptionById(id);
	if (!option?.timestamp) return '';
	return getElapsedTimeText(option.timestamp);
};

const getLabelById = (id: string) => {
	const option = getOptionById(id);
	return option?.label;
};
</script>

<style scoped>
.p-dropdown {
	/*FIXME: We may want to truncate the text with ellipsis or something (up to designers) */
	max-width: 25rem;
	align-self: end;
}

.output-dropdown:deep(.p-inputtext) {
	padding-left: var(--gap-3);
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
	gap: var(--gap-2);
	width: 100%;
	justify-content: space-between;
}

.timestamp {
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-size: var(--font-caption);
	font-weight: 500;
	color: var(--text-color-subdued);
}
.connection-indicator {
	font-size: var(--font-caption);
	color: var(--primary-color);
	display: flex;
	gap: var(--gap-small);
	align-items: center;
	margin-left: auto;
}
</style>
