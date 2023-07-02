<template>
	<tera-model :project="props.project" :asset-id="props.node.state.modelId" :is-editable="true">
	</tera-model>
</template>

<script setup lang="ts">
// Proxy to use tera-model via a workflow context

import { IProject } from '@/types/Project';
import { WorkflowNode } from '@/types/workflow';
import TeraModel from '@/components/models/tera-model.vue';

const props = defineProps<{
	node: WorkflowNode;
	project: IProject;
}>();
</script>
