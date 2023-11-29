<template>
	<main>
		<template v-if="model">
			<tera-operator-title>{{ model.header.name }}</tera-operator-title>
			<SelectButton
				class="p-button-sm"
				:model-value="view"
				@change="if ($event.value) view = $event.value;"
				:options="viewOptions"
			/>
			<div class="container">
				<tera-model-diagram
					v-if="view === ModelNodeView.Diagram"
					:model="model"
					:is-editable="false"
					nodePreview
				/>
				<tera-model-equation
					v-else-if="view === ModelNodeView.Equation"
					:model="model"
					:is-editable="false"
				/>
			</div>
		</template>
		<Dropdown
			v-else
			class="w-full p-button-sm p-button-outlined"
			v-model="selectedModel"
			:options="models"
			option-label="header.name"
			placeholder="Select a model"
		/>
	</main>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, watch, onMounted, computed } from 'vue';
import { getModel } from '@/services/model';
import Dropdown from 'primevue/dropdown';
import { Model } from '@/types/Types';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraModelEquation from '@/components/model/petrinet/tera-model-equation.vue';
import { WorkflowNode } from '@/types/workflow';
import SelectButton from 'primevue/selectbutton';
import TeraOperatorTitle from '@/workflow/operator/tera-operator-title.vue';
import { useProjects } from '@/composables/project';
import { ModelOperationState } from './model-operation';

const props = defineProps<{
	node: WorkflowNode<ModelOperationState>;
}>();

const emit = defineEmits(['update-state', 'append-output-port']);
const models = computed<Model[]>(() => useProjects().activeProject.value?.assets?.models ?? []);

enum ModelNodeView {
	Diagram = 'Diagram',
	Equation = 'Equation'
}

const model = ref<Model | null>();
const selectedModel = ref<Model>();
const view = ref(ModelNodeView.Diagram);
const viewOptions = ref([ModelNodeView.Diagram, ModelNodeView.Equation]);

async function getModelById(modelId: string) {
	model.value = await getModel(modelId);

	if (model.value) {
		const state = _.cloneDeep(props.node.state);
		state.modelId = model.value?.id;
		emit('update-state', state);
		emit('append-output-port', {
			type: 'modelId',
			label: model.value.header.name,
			value: [model.value.id]
		});
	}
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
});
</script>

<style scoped>
main {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

.container {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	overflow: hidden;
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
