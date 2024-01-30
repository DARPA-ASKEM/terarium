<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<tera-drilldown-section> sds </tera-drilldown-section>
		<template #preview>
			<tera-drilldown-preview>sdsd</tera-drilldown-preview>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { SubsetDataState } from './subset-data-operation';

// const props =
defineProps<{
	node: WorkflowNode<SubsetDataState>;
}>();

const emit = defineEmits(['append-output-port', 'update-state', 'close']);
</script>
