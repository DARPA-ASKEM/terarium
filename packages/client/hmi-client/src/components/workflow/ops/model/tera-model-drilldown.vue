<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<tera-drilldown-section>
			<tera-model v-if="node.state.modelId" :asset-id="node.state.modelId" />
		</tera-drilldown-section>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { WorkflowNode } from '@/types/workflow';
import { ModelOperationState } from '@/components/workflow/ops/model/model-operation';
import TeraModel from '@/components/model/tera-model.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';

defineProps<{
	node: WorkflowNode<ModelOperationState>;
}>();

const emit = defineEmits(['close', 'update-state']);
</script>
