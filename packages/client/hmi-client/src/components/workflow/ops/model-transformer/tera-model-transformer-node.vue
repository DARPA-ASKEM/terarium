<template>
	<section>
		<!--FIXME: See fetchModel()-->
		<template v-if="node.inputs[0].value && model">
			<tera-model-diagram :model="model" :feature-config="{ isPreview: true }" />
			<Button @click="emit('open-drilldown')" label="Edit" severity="secondary" outlined />
		</template>
		<template v-else>
			<tera-operator-placeholder :node="node"> Attach a model </tera-operator-placeholder>
		</template>
	</section>
</template>

<script setup lang="ts">
import { getModel } from '@/services/model';
import type { Model } from '@/types/Types';
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
	if (!props.node.inputs[0].value?.[0]) return;
	model.value = await getModel(props.node.inputs[0].value[0]);
};

onMounted(async () => {
	await fetchModel();
});
watch(
	() => props.node.inputs,
	async () => {
		await fetchModel();
	}
);
</script>

<style scoped></style>
