<template>
	<section>
		<tera-operator-model-preview v-if="model" :model="model" />
		<tera-operator-placeholder v-else :operation-type="node.operationType">
			<template v-if="!node.inputs[0].value">Attach a model</template>
		</tera-operator-placeholder>
		<template v-if="node.inputs[0].value">
			<Button @click="emit('open-drilldown')" label="Edit" severity="secondary" outlined />
		</template>
	</section>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { WorkflowNode } from '@/types/workflow';
import { ModelEditOperationState } from '@/workflow/ops/model-edit/model-edit-operation';
import { onMounted, onUpdated, ref } from 'vue';
import { Model } from '@/types/Types';
import operator from '@/services/operator';
import { getModel } from '@/services/model';
import TeraOperatorModelPreview from '@/components/operator/tera-operator-model-preview.vue';

const emit = defineEmits(['open-drilldown']);

const props = defineProps<{
	node: WorkflowNode<ModelEditOperationState>;
}>();

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
