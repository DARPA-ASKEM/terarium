<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div :tabName="Tabs.Wizard">
			<tera-drilldown-section> </tera-drilldown-section>
		</div>
		<div :tabName="Tabs.Notebook"></div>
		<template #preview>
			<tera-drilldown-preview>sdsd</tera-drilldown-preview>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { WorkflowNode } from '@/types/workflow';
import { ModelComparisonOperationState } from './model-comparison-operation';

defineProps<{
	node: WorkflowNode<ModelComparisonOperationState>;
}>();
const emit = defineEmits(['append-output-port', 'update-state', 'close']);

enum Tabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}
</script>
