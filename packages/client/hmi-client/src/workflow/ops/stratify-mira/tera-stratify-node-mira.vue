<template>
	<section>
		<tera-operator-placeholder :operation-type="node.operationType">
			Attach a model
		</tera-operator-placeholder>
	</section>
</template>

<script setup lang="ts">
import { WorkflowNode } from '@/types/workflow';
import TeraOperatorPlaceholder from '@/workflow/operator/tera-operator-placeholder.vue';
import { StratifyOperationStateMira } from './stratify-mira-operation';

// const emit = defineEmits(['open-drilldown']);

defineProps<{
	node: WorkflowNode<StratifyOperationStateMira>;
}>();
</script>

<style scoped></style>
