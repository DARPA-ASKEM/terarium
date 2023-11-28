<template>
	<div class="background">
		<Suspense>
			<tera-model-jupyter-panel
				:model-configuration-id="modelConfigurationId"
				:model="null"
				:show-kernels="false"
				:show-chat-thoughts="false"
				@new-model-saved="addOutputPort"
				:notebook-session="notebookSession"
			/>
		</Suspense>
	</div>
</template>

<script setup lang="ts">
// Proxy to use tera-dataset via a workflow context

import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import TeraModelJupyterPanel from '@/components/model/tera-model-jupyter-panel.vue';
import { computed, onMounted, ref } from 'vue';
import { createNotebookSession, getNotebookSessionById } from '@/services/notebook-session';
import { v4 as uuidv4 } from 'uuid';
import { NotebookSession } from '@/types/Types';
import { cloneDeep } from 'lodash';
import { getModel, getModelConfigurations } from '@/services/model';
import { addDefaultConfiguration } from '@/services/model-configurations';
import { ModelTransformerState } from './model-transformer-operation';

const props = defineProps<{
	node: WorkflowNode<ModelTransformerState>;
}>();
const emit = defineEmits(['append-output-port', 'update-state']);

const modelConfigurationId = computed(() => {
	// for now we are only using 1 model configuration for the llm at a time, this can be expanded in the future
	const modelConfirgurationList = props.node?.inputs
		.filter((inputNode) => inputNode.status === WorkflowPortStatus.CONNECTED && inputNode.value)
		.map((inputNode) => inputNode.value![0]);

	return modelConfirgurationList[0];
});

const notebookSession = ref(<NotebookSession | undefined>undefined);

onMounted(async () => {
	const state = cloneDeep(props.node.state);
	let notebookSessionId = state.notebookSessionId;
	if (!notebookSessionId) {
		// create a new notebook session log if it does not exist
		const response = await createNotebookSession({
			id: uuidv4(),
			name: props.node.id,
			description: '',
			data: { history: [] },
			timestamp: new Date().toISOString()
		});
		notebookSessionId = response?.id;

		if (notebookSessionId) {
			// update the node state with the notebook session id
			state.notebookSessionId = notebookSessionId;
			emit('update-state', state);
		}
	}
	notebookSession.value = await getNotebookSessionById(notebookSessionId!);
});

const addOutputPort = async (data) => {
	if (!data.id) return;
	// get model
	const model = await getModel(data.id);

	if (!model) return;

	const state = cloneDeep(props.node.state);
	state.modelId = model?.id;
	// update node state with the model id
	emit('update-state', state);

	// set default configuration
	await addDefaultConfiguration(model);

	// setting timeout...elastic search might not update default config in time
	setTimeout(async () => {
		const configurationList = await getModelConfigurations(model.id);
		configurationList.forEach((configuration) => {
			emit('append-output-port', {
				id: uuidv4(),
				label: configuration.name,
				type: 'modelConfigId',
				value: [configuration.id]
			});
		});
	}, 800);
};
</script>

<style scoped>
.background {
	background: white;
	height: 100%;
	overflow-y: auto;
}
</style>
