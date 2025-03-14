<template>
	<main>
		<tera-operator-model-preview v-if="model" :model="model" />
		<tera-operator-placeholder v-else :node="node" />
		<Button @click="emit('open-drilldown')" label="Open" severity="secondary" outlined />
	</main>
</template>

<script setup lang="ts">
import { watch, ref } from 'vue';
import Button from 'primevue/button';
import { WorkflowNode } from '@/types/workflow';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { getModel } from '@/services/model';
import { ClientEvent, ClientEventType, Model, TaskResponse, TaskStatus } from '@/types/Types';
import TeraOperatorModelPreview from '@/components/operator/tera-operator-model-preview.vue';
import { getActiveOutput } from '@/components/workflow/util';
import { useClientEvent } from '@/composables/useClientEvent';
import _ from 'lodash';
import { ModelFromEquationsState } from '@/components/workflow/ops/model-from-equations/model-from-equations-operation';
import { createEnrichmentCards } from './model-from-equations-utils';

const props = defineProps<{
	node: WorkflowNode<ModelFromEquationsState>;
}>();

const emit = defineEmits(['open-drilldown', 'update-state']);

const model = ref(null as Model | null);
const updateModel = async () => {
	const modelId = getActiveOutput(props.node)?.value?.[0];
	if (modelId && modelId !== model?.value?.id) {
		model.value = await getModel(modelId);
	}
};

useClientEvent([ClientEventType.TaskGollmEnrichModel], async (event: ClientEvent<TaskResponse>) => {
	const { modelId } = event.data.additionalProperties;
	if (props.node.state.modelId !== modelId) return;
	if ([TaskStatus.Success, TaskStatus.Cancelled, TaskStatus.Failed].includes(event.data.status)) {
		const { response } = JSON.parse(atob(event.data.output));
		const clonedState = _.cloneDeep(props.node.state);
		clonedState.enrichments = createEnrichmentCards(response);
		emit('update-state', clonedState);
	}
});

watch(
	() => props.node.active,
	() => {
		updateModel();
	},
	{ immediate: true }
);
</script>
