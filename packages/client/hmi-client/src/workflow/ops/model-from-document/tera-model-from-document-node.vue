<template>
	<main>
		<template v-if="model">
			<tera-operator-title>{{ model.header.name }}</tera-operator-title>
			<SelectButton
				class="p-button-xsm"
				:model-value="view"
				@change="if ($event.value) view = $event.value;"
				:options="viewOptions"
			/>
			<div class="container">
				<tera-model-diagram
					v-if="view === ModelNodeView.Diagram"
					:model="model"
					:is-editable="false"
					is-preview
				/>
				<tera-model-equation
					v-else-if="view === ModelNodeView.Equation"
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
import TeraOperatorTitle from '@/components/operator/tera-operator-title.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import SelectButton from 'primevue/selectbutton';
import { ref, onUpdated, onMounted } from 'vue';
import { ModelOperationState } from '@/workflow/ops/model/model-operation';
import { getModel } from '@/services/model';
import { Model } from '@/types/Types';

const props = defineProps<{
	node: WorkflowNode<ModelOperationState>;
}>();

enum ModelNodeView {
	Diagram = 'Diagram',
	Equation = 'Equation'
}
const view = ref(ModelNodeView.Diagram);
const viewOptions = ref([ModelNodeView.Diagram, ModelNodeView.Equation]);

const model = ref(null as Model | null);

onMounted(
	async () => {
		if (props.node?.outputs[0]?.state?.modelId) {
			model.value = await getModel(props.node.outputs[0].state.modelId);
		}
		console.log('model', model.value);
		console.log('props.node.state.modelId', props.node);
	},
	{ immediate: true }
);

onUpdated(async () => {
	if (props.node?.outputs[0]?.state?.modelId) {
		model.value = await getModel(props.node.outputs[0].state.modelId);
	}
});

const emit = defineEmits(['open-drilldown']);
</script>

<style scoped>
.p-selectbutton {
	width: 100%;
}

.p-selectbutton:deep(.p-button) {
	flex-grow: 1;
}
</style>
