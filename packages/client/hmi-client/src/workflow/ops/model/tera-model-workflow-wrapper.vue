<template>
	<tera-model
		:asset-id="props.node.state.modelId as string"
		@new-model-configuration="refreshModelNode"
		@update-model-configuration="refreshModelNode"
	/>
</template>

<script setup lang="ts">
// Proxy to use tera-model via a workflow context

import { WorkflowNode } from '@/types/workflow';
import TeraModel from '@/components/model/tera-model.vue';
import { workflowEventBus } from '@/services/workflow';
import { ModelOperationState } from './model-operation';

const props = defineProps<{
	node: WorkflowNode<ModelOperationState>;
}>();

// Send refresh event onto the eventBus
const refreshModelNode = () => {
	workflowEventBus.emitNodeRefresh({
		workflowId: props.node.workflowId,
		nodeId: props.node.id
	});
};
</script>
