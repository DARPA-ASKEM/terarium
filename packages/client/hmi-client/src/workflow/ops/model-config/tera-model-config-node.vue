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
import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { computed, watch } from 'vue';
import { ModelConfigOperationState } from './model-config-operation';

const props = defineProps<{
	node: WorkflowNode<ModelConfigOperationState>;
}>();
const emit = defineEmits(['open-drilldown', 'append-input-port']);

const modelInput = props.node.inputs.find((input) => input.type === 'modelId');
const isModelInputConnected = computed(() => modelInput?.status === WorkflowPortStatus.CONNECTED);

// Update the node with the new input ports
watch(
	() => props.node.inputs,
	() => {
		const documentInputs = props.node.inputs.filter((input) => input.type === 'documentId');
		const datasetInputs = props.node.inputs.filter((input) => input.type === 'datasetId');

		if (documentInputs.every((input) => input.status === WorkflowPortStatus.CONNECTED)) {
			emit('append-input-port', { type: 'documentId', label: 'Document', isOptional: true });
		}

		if (datasetInputs.every((input) => input.status === WorkflowPortStatus.CONNECTED)) {
			emit('append-input-port', { type: 'datasetId', label: 'Dataset', isOptional: true });
		}
	},
	{ deep: true }
);
</script>
