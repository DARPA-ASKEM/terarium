<template>
	<template v-if="model">
		<div class="container">
			<tera-model-diagram :model="model" :is-editable="false" nodePreview />
		</div>
	</template>
</template>

<script setup lang="ts">
import { getModel } from '@/services/model';
import { Model } from '@/types/Types';
import { WorkflowNode } from '@/types/workflow';
import { onMounted, ref, watch } from 'vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import { ModelTransformerState } from './model-transformer-operation';

const props = defineProps<{
	node: WorkflowNode<ModelTransformerState>;
}>();

const model = ref<Model | null>(null);

const fetchModel = async () => {
	if (!props.node?.state?.modelId) return;
	model.value = await getModel(props.node?.state?.modelId);
};

onMounted(async () => {
	await fetchModel();
});
watch(
	() => props.node?.state?.modelId,
	async () => {
		await fetchModel();
	}
);
</script>

<style scoped></style>
