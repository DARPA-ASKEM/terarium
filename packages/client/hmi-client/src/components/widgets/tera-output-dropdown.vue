<template>
	<Dropdown
		v-if="options"
		class="output-dropdown"
		placeholder="Select an output"
		:model-value="output"
		:options="options"
		option-value="id"
		option-label="label"
		option-group-children="items"
		option-group-label="label"
		@update:model-value="emit('update:output', $event)"
		:loading="isLoading"
	>
		<template #optiongroup="slotProps">
			<span class="dropdown-option-group">{{ slotProps.option?.label }}</span>
		</template>
		<template #option="slotProps">
			<div class="dropdown-option">
				<Checkbox
					v-if="isSelectable"
					@click.stop
					:model-value="slotProps.option?.isSelected"
					@update:model-value="emit('update:selection', slotProps.option?.id)"
					binary
				/>
				<span>{{ slotProps.option?.label }}</span>
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
import Checkbox from 'primevue/checkbox';

defineProps<{
	options?: WorkflowOutput<any>[] | { label: string; items: WorkflowOutput<any>[] }[];
	output?: WorkflowOutput<any>['id'];
	isSelectable?: boolean;
	isLoading?: boolean;
}>();

const emit = defineEmits(['update:selection', 'update:output']);
</script>

<style scoped>
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
	gap: 0.5rem;
	font-size: var(--font-body-small);
}

.dropdown-option-group {
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
