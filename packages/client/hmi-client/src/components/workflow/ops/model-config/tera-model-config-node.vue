<template>
	<section>
		<tera-operator-placeholder :operation-type="node.operationType" />
		<Button
			:label="isModelInputConnected ? 'Open' : 'Attach a model'"
			@click="emit('open-drilldown')"
			severity="secondary"
			outlined
			:disabled="!isModelInputConnected"
		/>
	</section>
</template>

<script setup lang="ts">
import { cloneDeep, isEmpty } from 'lodash';
import { computed, watch } from 'vue';
import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { getModel, getModelConfigurations } from '@/services/model';
import { postAsConfiguredModel } from '@/services/model-configurations';
import { ModelConfigOperationState } from './model-config-operation';

const props = defineProps<{
	node: WorkflowNode<ModelConfigOperationState>;
}>();
const emit = defineEmits(['open-drilldown', 'append-input-port', 'update-state']);

const modelInput = props.node.inputs.find((input) => input.type === 'modelId');
const isModelInputConnected = computed(() => modelInput?.status === WorkflowPortStatus.CONNECTED);

// Update the node with the new input ports
watch(
	() => props.node.inputs,
	async () => {
		const inputs = props.node.inputs;
		const documentInputs = inputs.filter((input) => input.type === 'documentId');
		const datasetInputs = inputs.filter((input) => input.type === 'datasetId');

		const modelInputs = inputs.filter((input) => input.type === 'modelId');
		if (!modelInputs[0].value) {
			// Reset previous model cache
			const state = cloneDeep(props.node.state);
			state.transientModelConfig = {
				id: '',
				modelId: '',
				observableSemanticList: [],
				parameterSemanticList: [],
				initialSemanticList: []
			};
			emit('update-state', state);
		} else {
			// Check if it has a model configuration
			const modelId = modelInputs[0].value[0];
			const modelConfigurations = await getModelConfigurations(modelId);
			if (isEmpty(modelConfigurations)) {
				// Create a model configuration if it does not exist
				const model = await getModel(modelId);
				if (model) {
					postAsConfiguredModel(model);
				}
			}
		}

		// If all document inputs are connected, add a new document input port
		if (documentInputs.every((input) => input.status === WorkflowPortStatus.CONNECTED)) {
			emit('append-input-port', { type: 'documentId', label: 'Document', isOptional: true });
		}

		// If all dataset inputs are connected, add a new dataset input port
		if (datasetInputs.every((input) => input.status === WorkflowPortStatus.CONNECTED)) {
			emit('append-input-port', { type: 'datasetId', label: 'Dataset', isOptional: true });
		}
	},
	{ deep: true }
);
</script>
