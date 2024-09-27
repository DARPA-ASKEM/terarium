<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<tera-drilldown-section>
			<tera-model v-if="node.state.modelId" :asset-id="node.state.modelId" @on-save="onSaveEvent" />
		</tera-drilldown-section>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { WorkflowNode } from '@/types/workflow';
import { ModelOperationState } from '@/components/workflow/ops/model/model-operation';
import TeraModel from '@/components/model/tera-model.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';

const props = defineProps<{
	node: WorkflowNode<ModelOperationState>;
}>();

const emit = defineEmits(['close', 'update-state']);

const onSaveEvent = (event: any) => {
	console.log('save finished', event);
	const state = _.cloneDeep(props.node.state);
	state.modelId = event.id;
	emit('update-state', state);
};
</script>
