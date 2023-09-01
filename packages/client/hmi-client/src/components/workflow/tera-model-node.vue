<template>
	<template v-if="model">
		<h5>{{ model.header.name }}</h5>
		<div class="container">
			<tera-model-diagram :model="model" :is-editable="false" nodePreview />
		</div>
	</template>
	<Dropdown
		v-else
		class="w-full p-button-sm p-button-outlined"
		v-model="selectedModel"
		:options="models"
		option-label="name"
		placeholder="Select a model"
	/>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { getModel } from '@/services/model';
import Dropdown from 'primevue/dropdown';
import { Model } from '@/types/Types';
import TeraModelDiagram from '@/components/models/tera-model-diagram.vue';
import { WorkflowNode } from '@/types/workflow';

const props = defineProps<{
	node: WorkflowNode;
	models: Model[];
	droppedModelId: null | string;
}>();

const emit = defineEmits(['select-model']);

const model = ref<Model | null>();
const selectedModel = ref<Model>();

async function getModelById(modelId: string) {
	model.value = await getModel(modelId);
	emit('select-model', { id: model.value?.id });
}

watch(
	() => selectedModel.value,
	async () => {
		if (selectedModel.value) {
			await getModelById(selectedModel.value.id.toString());
		}
	}
);

onMounted(async () => {
	const state = props.node.state;
	if (state.modelId) {
		model.value = await getModel(state.modelId);
	}

	// If model is drag and dropped from resource panel
	else if (props.droppedModelId) await getModelById(props.droppedModelId);

	// Force refresh of configs in the workflow node - August 2023
	emit('select-model', { id: model.value?.id });
});
</script>

<style scoped>
.node-title {
	padding-left: 0.5rem;
	padding-right: 0.5rem;
	padding-bottom: 0.5rem;
}

.container {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	overflow: hidden;
	margin-top: 0.5rem;
}

.p-button-sm.p-button-outlined {
	border: 1px solid var(--surface-border);
}

.p-button-sm.p-button-outlined:hover {
	border: 1px solid var(--surface-border-hover);
}

.p-inputtext.p-inputtext-sm {
	padding: 0.25rem 0.5rem;
	font-size: 1rem;
	margin-left: 1rem;
}

.p-button-sm.p-button-outlined:deep(.p-dropdown-label) {
	padding: 0.5rem;
}
</style>
