<template>
	<section>
		<div v-if="!isEmpty(node.state.transientModelConfig.id)" class="configuration-card">
			<div class="content">
				<p class="text-sm font-semibold line-wrap">{{ node.state.transientModelConfig.name }}</p>
				<p class="text-sm">{{ node.state.transientModelConfig.description }}</p>
			</div>
		</div>
		<tera-operator-placeholder v-else :node="node" />

		<tera-operator-status :status="props.node.status" />
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
import { cloneDeep, omit, isEmpty } from 'lodash';
import Button from 'primevue/button';
import { watch, ref } from 'vue';

import { AssetType, ClientEventType } from '@/types/Types';
import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import { getModel, getModelConfigurationsForModel } from '@/services/model';
import { postAsConfiguredModel } from '@/services/model-configurations';

import { useProjects } from '@/composables/project';
import { createTaskListClientEventHandler, useClientEvent } from '@/composables/useClientEvent';

import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraOperatorStatus from '@/components/operator/tera-operator-status.vue';
import { ModelConfigOperation, ModelConfigOperationState } from './model-config-operation';

const props = defineProps<{
	node: WorkflowNode<ModelConfigOperationState>;
}>();
const emit = defineEmits(['open-drilldown', 'append-input-port', 'update-state', 'append-output']);

useClientEvent(
	[ClientEventType.TaskGollmConfigureModelFromDocument, ClientEventType.TaskGollmConfigureModelFromDataset],
	createTaskListClientEventHandler(props.node, 'modelConfigTaskIds', emit)
);

const isModelInputConnected = ref(false);

// Update the node with the new input ports
watch(
	() => props.node.inputs,
	async () => {
		const state = cloneDeep(props.node.state);
		const inputs = props.node.inputs;

		const documentInputs = inputs.filter((input) => input.type === 'documentId');
		const datasetInputs = inputs.filter((input) => input.type === 'datasetId');
		const modelInputs = inputs.filter((input) => input.type === 'modelId');
		const modelId = modelInputs[0]?.value?.[0];

		if (modelInputs[0].status === WorkflowPortStatus.CONNECTED) {
			isModelInputConnected.value = true;
		}

		// If all document inputs are connected, add a new document input port
		if (documentInputs.every((input) => input.status === WorkflowPortStatus.CONNECTED)) {
			emit('append-input-port', { type: 'documentId', label: 'Document', isOptional: true });
		}

		// If all dataset inputs are connected, add a new dataset input port
		if (datasetInputs.every((input) => input.status === WorkflowPortStatus.CONNECTED)) {
			emit('append-input-port', { type: 'datasetId', label: 'Dataset', isOptional: true });
		}

		// model
		if (!modelId || modelId === state.transientModelConfig?.modelId) return;
		let configs = await getModelConfigurationsForModel(modelId);
		if (!configs[0]?.id) {
			const model = await getModel(modelId);
			if (model) {
				const modelConfig = await postAsConfiguredModel(model); // Create a model configuration if it does not exist
				useProjects().addAsset(AssetType.ModelConfiguration, modelConfig.id);
			}
			configs = await getModelConfigurationsForModel(modelId);
		}

		if (configs[0]?.id) {
			const config = configs[0];
			state.transientModelConfig = config;

			// Auto append output if and only if there isnt already an output
			if (!props.node.outputs.at(0)?.value) {
				emit(
					'append-output',
					{
						type: ModelConfigOperation.outputs[0].type,
						label: config.name,
						value: config.id,
						isSelected: false,
						state: omit(state, ['transientModelConfig'])
					},
					state
				);
			}
		}
	},
	{ immediate: true, deep: true }
);
</script>
<style scoped>
.configuration-card {
	background: var(--surface-section);
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	box-shadow: 0 2px 4px -1px rgba(0, 0, 0, 0.08);
	overflow: hidden;
	margin-bottom: var(--gap-1);
}

.configuration-card .content {
	padding-top: var(--gap-2);
	padding-right: var(--gap-2);
	padding-bottom: var(--gap-3);
	padding-left: var(--gap-2-5);
	border-left: 4px solid var(--primary-color);
}

.line-wrap {
	white-space: normal;
	overflow-wrap: break-word;
	word-break: break-word;
	max-width: 100%;
}
h6 + p {
	color: var(--text-color);
}
</style>
