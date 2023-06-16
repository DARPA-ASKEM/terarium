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
// import Button from 'primevue/button';
// import InputNumber from 'primevue/inputnumber';
import { ModelOperation } from '@/components/workflow/model-operation';
import { getModel } from '@/services/model';
import Dropdown from 'primevue/dropdown';
import { Model } from '@/types/Types';
import TeraModelDiagram from '@/components/models/tera-model-diagram.vue';
import {
	createModelConfiguration,
	getModelConfigurationById
} from '@/services/model-configurations';
import { WorkflowNode } from '@/types/workflow';

const props = defineProps<{
	node: WorkflowNode;
	modelId?: string | null;
	models: Model[];
	outputAmount: number;
}>();

const emit = defineEmits(['append-output-port']);

const model = ref<Model | null>();
const selectedModel = ref<Model>();

async function createDefaultModelConfig() {
	if (model.value) {
		const response = await createModelConfiguration(
			model.value?.id,
			'Config 1',
			'shawntest',
			model.value
		);

		if (ModelOperation.action) {
			// Create output
			emit('append-output-port', {
				type: ModelOperation.outputs[0].type,
				label: `Config ${props.outputAmount}`,
				value: response.id
			});
		}
	}
}

watch(
	() => selectedModel.value,
	async () => {
		if (selectedModel.value) {
			model.value = await getModel(selectedModel.value.id.toString());
			createDefaultModelConfig();
		}
	}
);

onMounted(async () => {
	const modelConfigId = props.node.outputs?.[0]?.value?.[0];

	if (modelConfigId) {
		const response = await getModelConfigurationById(modelConfigId);
		model.value = await getModel(response.configuration.id);
	} else if (props.modelId) {
		model.value = await getModel(props.modelId);
		createDefaultModelConfig();
	}
});
</script>

<style scoped>
ul {
	list-style-type: none;
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

li {
	display: flex;
	justify-content: space-between;
	align-items: center;
	width: 100%;
}

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
