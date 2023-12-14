<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section>
			<tera-dataset :asset-id="props.node.state.datasetId as string" />
		</section>
	</tera-drilldown>
</template>

<script setup lang="ts">
// Proxy to use tera-dataset via a workflow context

import { WorkflowNode } from '@/types/workflow';
import TeraDataset from '@/components/dataset/tera-dataset.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import { DatasetOperationState } from './dataset-operation';

const props = defineProps<{
	node: WorkflowNode<DatasetOperationState>;
}>();
const emit = defineEmits(['close']);
</script>
