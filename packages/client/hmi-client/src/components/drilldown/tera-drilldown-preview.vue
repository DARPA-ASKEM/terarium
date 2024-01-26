<template>
	<tera-drilldown-section>
		<div class="content-container">
			<header v-if="!hideHeader">
				<h5>{{ title ?? 'Preview' }}</h5>
				<tera-output-dropdown
					v-if="options && output"
					:options="options"
					:is-selectable="isSelectable ?? false"
					:is-loading="isLoading"
					:output="output"
					@update:output="(e) => emit('update:output', e)"
					@update:selection="(e) => emit('update:selection', e)"
				/>
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
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { useSlots } from 'vue';
import { WorkflowOutput } from '@/types/workflow';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraOutputDropdown from '@/components/drilldown/tera-output-dropdown.vue';

defineProps<{
	title?: string;
	options?: WorkflowOutput<any>[] | { label: string; items: WorkflowOutput<any>[] }[];
	output?: WorkflowOutput<any>['id'];
	canSaveAsset?: boolean;
	isLoading?: boolean;
	isSelectable?: boolean;
	hideHeader?: boolean;
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

main {
	display: flex;
	flex-direction: column;
	flex-grow: 1;
	overflow-y: auto;
	gap: 1.5rem;
	padding: 1.5rem 1.5rem 1.5rem 1rem;
}
</style>
