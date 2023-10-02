<template>
	<tera-dataset :asset-id="props.node.state.datasetId as string" />
</template>

<script setup lang="ts">
// Proxy to use tera-dataset via a workflow context

import { WorkflowNode } from '@/types/workflow';
import TeraDataset from '@/components/dataset/tera-dataset.vue';
import { DatasetOperationState } from './dataset-operation';

const props = defineProps<{
	node: WorkflowNode<DatasetOperationState>;
}>();
</script>
