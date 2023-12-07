<template>
	<main>
		<tera-operator-placeholder :operation-type="node.operationType" />
	</main>
</template>

<script setup lang="ts">
import TeraOperatorPlaceholder from '@/workflow/operator/tera-operator-placeholder.vue';
import { WorkflowNode } from '@/types/workflow';
import { ModelOptimizeOperationState } from './model-optimize-operation';

defineProps<{
	node: WorkflowNode<ModelOptimizeOperationState>;
}>();
</script>

<style scoped></style>
