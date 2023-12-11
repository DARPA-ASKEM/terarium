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
					placeholder="Select an output"
					@update:model-value="emit('update:output', $event)"
					:loading="isLoading"
				></Dropdown>
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
import { WorkflowOutput } from '@/types/workflow';
import { useSlots } from 'vue';

defineProps<{
	title?: string;
	options?: WorkflowOutput<any>[]; // subject to change based on how we want to pass in output data
	output?: WorkflowOutput<any>['id'];
	canSaveAsset?: boolean;
	isLoading?: boolean;
}>();

const slots = useSlots();

const emit = defineEmits(['update:output']);
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
</style>
