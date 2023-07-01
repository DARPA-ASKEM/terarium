<template>
	<tera-model :project="props.project" :asset-id="props.node.state.modelId"> </tera-model>
</template>

<script setup lang="ts">
// Proxy to use tera-model via a workflow context

import { onMounted } from 'vue';
import { IProject } from '@/types/Project';
import { WorkflowNode } from '@/types/workflow';
import TeraModel from '@/components/models/tera-model.vue';

const props = defineProps<{
	node: WorkflowNode;
	project: IProject;
}>();

onMounted(() => {
	console.log('tera-model-workflow-wrapper');
	console.log(props.node);
	console.log(props.project);
});
</script>

<style scoped></style>
