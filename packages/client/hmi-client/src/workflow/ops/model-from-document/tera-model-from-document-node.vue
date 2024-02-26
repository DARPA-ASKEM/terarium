<template>
	<main>
		<template v-if="model">
			<SelectButton
				class="p-button-xsm"
				:model-value="view"
				:options="viewOptions"
				@change="handleChange"
			/>
			<div class="container">
				<tera-model-diagram
					v-if="view === View.Diagram"
					:model="model"
					:is-editable="false"
					is-preview
				/>
				<tera-model-equation
					v-else-if="view === View.Equation"
					:model="model"
					:is-editable="false"
				/>
			</div>
		</template>
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
import TeraModelEquation from '@/components/model/petrinet/tera-model-equation.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import SelectButton from 'primevue/selectbutton';
import { ref, onUpdated, onMounted } from 'vue';
import { ModelOperationState } from '@/workflow/ops/model/model-operation';
import { getModel } from '@/services/model';
import { Model } from '@/types/Types';

const props = defineProps<{
	node: WorkflowNode<ModelOperationState>;
}>();

const emit = defineEmits(['open-drilldown']);

enum View {
	Diagram = 'Diagram',
	Equation = 'Equation'
}
const view = ref(View.Diagram);
const viewOptions = ref([View.Diagram, View.Equation]);
const handleChange = (event) => {
	if (event.value) {
		view.value = event.value;
	}
};

const model = ref(null as Model | null);

// Update the model preview when the active output changes
const activeOutputId = ref(props.node?.active ?? props.node?.outputs?.[0].id);
const activeModelId = ref(
	props.node?.outputs?.find((output) => output.id === activeOutputId.value)?.state?.modelId
);
const updateModel = async () => {
	if (activeModelId.value) {
		model.value = await getModel(activeModelId.value);
	}
};
onMounted(updateModel);
onUpdated(updateModel);
</script>

<style scoped>
.p-selectbutton {
	width: 100%;
}

.p-selectbutton:deep(.p-button) {
	flex-grow: 1;
}
</style>
