<template>
	<main>
		<template v-if="model">
			<SelectButton
				class="p-button-xsm"
				:model-value="view"
				@change="if ($event.value) view = $event.value;"
				:options="viewOptions"
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
const model = ref(null as Model | null);

onMounted(async () => {
	if (props.node?.active?.state?.modelId) {
		model.value = await getModel(props.node.active.state.modelId);
	}
});

onUpdated(async () => {
	if (props.node?.active?.state?.modelId) {
		model.value = await getModel(props.node.active.state.modelId);
	}
});
</script>

<style scoped>
.p-selectbutton {
	width: 100%;
}

.p-selectbutton:deep(.p-button) {
	flex-grow: 1;
}
</style>
