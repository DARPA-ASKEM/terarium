<template>
	<main>
		<tera-operator-model-preview v-if="model" :model="model" />
		<tera-operator-placeholder v-else :node="node" />
		<Button @click="emit('open-drilldown')" label="Edit" severity="secondary" outlined />
	</main>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import { WorkflowNode } from '@/types/workflow';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { onMounted, onUpdated, ref } from 'vue';
import { ModelOperationState } from '@/components/workflow/ops/model/model-operation';
import { getModel } from '@/services/model';
import { Model } from '@/types/Types';
import TeraOperatorModelPreview from '@/components/operator/tera-operator-model-preview.vue';
import operator from '@/services/operator';

const props = defineProps<{
	node: WorkflowNode<ModelOperationState>;
}>();

const emit = defineEmits(['open-drilldown']);

const model = ref(null as Model | null);
const updateModel = async () => {
	const modelId = operator.getActiveOutput(props.node)?.value?.[0];
	if (modelId && modelId !== model?.value?.id) {
		model.value = await getModel(modelId);
	}
};
onMounted(updateModel);
onUpdated(updateModel);
</script>
