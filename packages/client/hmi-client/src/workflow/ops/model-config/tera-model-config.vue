<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section>TODO</section>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { WorkflowNode } from '@/types/workflow';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import { ModelConfigOperationState } from './model-config-operation';

defineProps<{
	node: WorkflowNode<ModelConfigOperationState>;
}>();
const emit = defineEmits(['close']);
</script>

<style scoped></style>
