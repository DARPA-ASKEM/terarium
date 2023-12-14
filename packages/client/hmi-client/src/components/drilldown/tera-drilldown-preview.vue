<template>
	<tera-drilldown-section>
		<div class="content-container">
			<header>
				<h5>{{ title ?? 'Preview' }}</h5>
				<Dropdown
					v-if="options"
					class="output-dropdown"
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
			</header>
			<main>
				<slot v-if="!isLoading" />
				<tera-progress-spinner v-else :font-size="2" is-centered style="height: 100%" />
			</main>
		</div>
		<template #footer v-if="slots.footer">
			<slot name="footer" />
		</template>
	</tera-drilldown-section>
</template>

<script setup lang="ts">
import Dropdown from 'primevue/dropdown';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { useSlots } from 'vue';
import { WorkflowOutput, WorkflowPortStatus } from '@/types/workflow';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import Checkbox from 'primevue/checkbox';

defineProps<{
	title?: string;
	options?: WorkflowOutput<any>[] | { label: string; items: WorkflowOutput<any>[] }[];
	output?: WorkflowOutput<any>['id'];
	canSaveAsset?: boolean;
	isLoading?: boolean;
	isSelectable?: boolean;
}>();

const slots = useSlots();

const emit = defineEmits(['update:output', 'update:selection']);
</script>

<style scoped>
.content-container {
	display: flex;
	flex-direction: column;
	background-color: var(--surface-50);
	flex-grow: 1;
	padding: 1rem;
	border-radius: var(--border-radius-medium);
	box-shadow: 0px 0px 4px 0px rgba(0, 0, 0, 0.25) inset;
	overflow: hidden;
}

header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-bottom: 0.5rem;
}

.content-container > main {
	overflow-y: auto;
	flex-grow: 1;
}

.output-dropdown:deep(.p-inputtext) {
	padding: 0.75rem 1rem;
	font-size: var(--font-body-small);
}

.output-dropdown:deep(.pi) {
	font-size: var(--font-body-small);
}

main {
	display: flex;
	flex-direction: column;
	flex-grow: 1;
	overflow-y: auto;
	gap: 1.5rem;
	padding: 1.5rem 1.5rem 1.5rem 1rem;
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
