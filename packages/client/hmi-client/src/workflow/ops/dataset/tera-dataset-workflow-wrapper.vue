<template>
	<tera-dataset :asset-id="props.node.state.datasetId" />
</template>

<script setup lang="ts">
// Proxy to use tera-dataset via a workflow context

import { WorkflowNode } from '@/types/workflow';
import TeraDataset from '@/components/dataset/tera-dataset.vue';

const props = defineProps<{
	node: WorkflowNode;
}>();
</script>
