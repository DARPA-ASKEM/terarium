<template>
	<template v-if="model">
		<h5>{{ model.name }}</h5>
		<tera-model-diagram :model="model" :is-editable="false" nodePreview />
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
}>();

const emit = defineEmits(['append-output-port', 'select-model']);

const model = ref<Model | null>();
const selectedModel = ref<Model>();

watch(
	() => selectedModel.value,
	async () => {
		if (selectedModel.value) {
			model.value = await getModel(selectedModel.value.id.toString());
			emit('select-model', { id: model.value?.id });
		}
	}
);

onMounted(async () => {
	const state = props.node.state;
	if (state.modelId) {
		model.value = await getModel(state.modelId);
	}
});
</script>

<style scoped>
.node-title {
	padding-left: 0.5rem;
	padding-right: 0.5rem;
	padding-bottom: 0.5rem;
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
