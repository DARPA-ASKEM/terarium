<template>
	<tera-dataset :project="props.project" :asset-id="props.node.state.datasetId" />
</template>

<script setup lang="ts">
// Proxy to use tera-dataset via a workflow context

import { IProject } from '@/types/Project';
import { WorkflowNode } from '@/types/workflow';
import TeraDataset from '@/components/dataset/tera-dataset.vue';

const props = defineProps<{
	node: WorkflowNode;
	project: IProject;
}>();
</script>
