<template>
	<tera-model
		:asset-id="props.node.state.modelId"
		@new-model-configuration="refreshModelNode"
		@update-model-configuration="refreshModelNode"
	/>
</template>

<script setup lang="ts">
// Proxy to use tera-model via a workflow context

import { WorkflowNode } from '@/types/workflow';
import TeraModel from '@/components/model/tera-model.vue';
import { workflowEventBus } from '@/services/workflow';

const props = defineProps<{
	node: WorkflowNode;
}>();

// Send refresh event onto the eventBus
const refreshModelNode = () => {
	workflowEventBus.emitNodeRefresh({
		workflowId: props.node.workflowId,
		nodeId: props.node.id
	});
};
</script>
