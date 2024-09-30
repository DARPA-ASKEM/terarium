<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<tera-drilldown-section>
			<tera-model v-if="node.state.modelId" :asset-id="node.state.modelId" :is-workflow="true" @on-save="onSaveEvent" />
		</tera-drilldown-section>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { cloneDeep } from 'lodash';
import { WorkflowNode } from '@/types/workflow';
import { ModelOperationState } from '@/components/workflow/ops/model/model-operation';
import TeraModel from '@/components/model/tera-model.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import operator from '@/services/operator';

const props = defineProps<{
	node: WorkflowNode<ModelOperationState>;
}>();

const emit = defineEmits(['close', 'update-state', 'update-output']);

const onSaveEvent = (event: any) => {
	const state = cloneDeep(props.node.state);
	state.modelId = event.id;
	emit('update-state', state);

	// Update the output with the new model
	const output = operator.getActiveOutput(props.node);
	if (!output) return;
	output.state = state;
	emit('update-output', output);
};
</script>
