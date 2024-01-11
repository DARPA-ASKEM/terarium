<template>
	<section>
		<!--FIXME: See fetchModel()-->
		<template v-if="model">
			<tera-model-diagram :model="model" :is-editable="false" is-preview />
			<Button @click="emit('open-drilldown')" label="Configure" severity="secondary" outlined />
		</template>
		<template v-else>
			<tera-operator-placeholder :operation-type="node.operationType">
				Attach a model
			</tera-operator-placeholder>
		</template>
	</section>
</template>

<script setup lang="ts">
import { getModel } from '@/services/model';
import { Model } from '@/types/Types';
import { WorkflowNode } from '@/types/workflow';
import { onMounted, ref, watch } from 'vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import Button from 'primevue/button';
import { ModelTransformerState } from './model-transformer-operation';

const emit = defineEmits(['open-drilldown']);

const props = defineProps<{
	node: WorkflowNode<ModelTransformerState>;
}>();

const model = ref<Model | null>(null);

const fetchModel = async () => {
	// FIXME: The state now holds a modelConfigIds - so this may have to be updated to support that
	if (!props.node?.state?.modelId) return;
	model.value = await getModel(props.node?.state?.modelId);
};

onMounted(async () => {
	await fetchModel();
});
watch(
	() => props.node?.inputs,
	async () => {
		await fetchModel();
	}
);
</script>

<style scoped></style>
