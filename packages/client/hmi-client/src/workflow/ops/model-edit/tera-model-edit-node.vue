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
		<template v-else>
			<Dropdown
				class="w-full p-dropdown-sm"
				v-model="selectedModel"
				:options="models"
				option-label="header.name"
				placeholder="Select a model"
			/>
		</template>
	</main>
</template>

<script setup lang="ts">
// import _ from 'lodash';
import { ref, computed } from 'vue';
import Dropdown from 'primevue/dropdown';
import { Model } from '@/types/Types';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraModelEquation from '@/components/model/petrinet/tera-model-equation.vue';
// import { WorkflowNode } from '@/types/workflow';
import SelectButton from 'primevue/selectbutton';
import TeraOperatorTitle from '@/workflow/operator/tera-operator-title.vue';
import { useProjects } from '@/composables/project';
// import { ModelEditState } from './model-edit';

// const props = defineProps<{
// 	node: WorkflowNode<ModelEditState>;
// }>();

// const emit = defineEmits(['update-state', 'append-output-port']);
const models = computed<Model[]>(() => useProjects().activeProject.value?.assets?.models ?? []);

enum ModelNodeView {
	Diagram = 'Diagram',
	Equation = 'Equation'
}

const model = ref<Model | null>();
const selectedModel = ref<Model>();
const view = ref(ModelNodeView.Diagram);
const viewOptions = ref([ModelNodeView.Diagram, ModelNodeView.Equation]);
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
</style>
