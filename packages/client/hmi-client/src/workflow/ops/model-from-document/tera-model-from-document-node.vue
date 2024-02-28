<template>
	<main>
		<tera-operator-model-preview v-if="model" :model="model" />
		<tera-operator-placeholder
			v-else
			:operation-type="WorkflowOperationTypes.MODEL_FROM_DOCUMENT"
		/>
		<Button @click="emit('open-drilldown')" label="Edit" severity="secondary" outlined />
	</main>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import { WorkflowNode, WorkflowOperationTypes } from '@/types/workflow';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { ref, onUpdated, onMounted } from 'vue';
import { ModelOperationState } from '@/workflow/ops/model/model-operation';
import { getModel } from '@/services/model';
import { Model } from '@/types/Types';
import TeraOperatorModelPreview from '@/components/operator/tera-operator-model-preview.vue';

const props = defineProps<{
	node: WorkflowNode<ModelOperationState>;
}>();

const emit = defineEmits(['open-drilldown']);

const model = ref(null as Model | null);
const updateModel = async () => {
	const modelId = props.node.active;
	if (modelId) model.value = await getModel(modelId);
};
onMounted(updateModel);
onUpdated(updateModel);
</script>
