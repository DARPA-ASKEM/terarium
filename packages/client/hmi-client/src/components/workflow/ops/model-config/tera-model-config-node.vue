<template>
	<section>
		<tera-operator-placeholder :node="node" />
		<tera-progress-spinner is-centered :font-size="2" v-if="isLoading" />
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
import { cloneDeep } from 'lodash';
import { computed, ref, watch } from 'vue';
import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import Button from 'primevue/button';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { getModel, getModelConfigurationsForModel } from '@/services/model';
import { postAsConfiguredModel } from '@/services/model-configurations';
import { useClientEvent } from '@/composables/useClientEvent';
import type { ClientEvent, TaskResponse } from '@/types/Types';
import { ClientEventType, TaskStatus } from '@/types/Types';
import { blankModelConfig, ModelConfigOperation, ModelConfigOperationState } from './model-config-operation';

const props = defineProps<{
	node: WorkflowNode<ModelConfigOperationState>;
}>();
const emit = defineEmits(['open-drilldown', 'append-input-port', 'update-state', 'append-output']);

const modelConfigTaskIds = ref<string[]>([]);

const configModelEventHandler = async (event: ClientEvent<TaskResponse>) => {
	if (!modelConfigTaskIds.value.includes(event.data?.id)) return;
	if ([TaskStatus.Success, TaskStatus.Cancelled, TaskStatus.Failed].includes(event.data.status)) {
		modelConfigTaskIds.value = modelConfigTaskIds.value.filter((id) => id !== event.data.id);
	}
};

useClientEvent(ClientEventType.TaskGollmConfigureModelFromDocument, configModelEventHandler);
useClientEvent(ClientEventType.TaskGollmConfigureModelFromDataset, configModelEventHandler);

const isLoading = computed(() => modelConfigTaskIds.value.length > 0);

const modelInput = props.node.inputs.find((input) => input.type === 'modelId');
const isModelInputConnected = computed(() => modelInput?.status === WorkflowPortStatus.CONNECTED);

// Update the node with the new input ports
watch(
	() => props.node.inputs,
	async () => {
		const state = cloneDeep(props.node.state);
		const inputs = props.node.inputs;

		const documentInputs = inputs.filter((input) => input.type === 'documentId');
		const datasetInputs = inputs.filter((input) => input.type === 'datasetId');
		const modelInputs = inputs.filter((input) => input.type === 'modelId');
		const modelId = modelInputs?.[0]?.value?.[0];

		if (modelId) {
			let configs = await getModelConfigurationsForModel(modelId);
			if (!configs[0]?.id) {
				const model = await getModel(modelId);
				if (model) await postAsConfiguredModel(model); // Create a model configuration if it does not exist
				configs = await getModelConfigurationsForModel(modelId);
			}
			// Auto append output
			if (configs[0]?.id) {
				const config = configs[0];
				state.transientModelConfig = config;
				emit('update-state', state);
				emit('append-output', {
					type: ModelConfigOperation.outputs[0].type,
					label: config.name,
					value: config.id,
					isSelected: false,
					state
				});
			}
		} else {
			// Reset previous model cache
			state.transientModelConfig = blankModelConfig;
			emit('update-state', state);
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

watch(
	() => props.node.state.modelConfigTaskIds,
	() => {
		modelConfigTaskIds.value = props.node.state.modelConfigTaskIds ?? [];
	}
);

watch(
	() => isLoading.value,
	() => {
		if (!isLoading.value) {
			const state = cloneDeep(props.node.state);
			state.modelConfigTaskIds = [];
			emit('update-state', state);
		}
	}
);
</script>
