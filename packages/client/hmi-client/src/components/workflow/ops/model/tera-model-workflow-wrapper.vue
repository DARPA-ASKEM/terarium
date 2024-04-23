<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<section>
			<tera-model
				:asset-id="props.node.state.modelId as string"
				@new-model-configuration="refreshModelNode"
				@update-model-configuration="refreshModelNode"
			/>
		</section>
	</tera-drilldown>
</template>

<script setup lang="ts">
// Proxy to use tera-model via a workflow context

import { WorkflowNode } from '@/types/workflow';
import TeraModel from '@/components/model/tera-model.vue';
// import { workflowEventBus } from '@/services/workflow';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';

import { ModelOperationState } from './model-operation';

const props = defineProps<{
	node: WorkflowNode<ModelOperationState>;
}>();

const emit = defineEmits(['close', 'update-state']);

// Send refresh event onto the eventBus
const refreshModelNode = () => {
	// workflowEventBus.emitNodeRefresh({
	// 	workflowId: props.node.workflowId,
	// 	nodeId: props.node.id
	// });
};
</script>
