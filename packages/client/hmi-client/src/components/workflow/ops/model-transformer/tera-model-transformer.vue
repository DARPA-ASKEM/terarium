<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<div class="background">
			<Suspense>
				<tera-model-jupyter-panel
					:model-id="modelId"
					:model="null"
					:show-kernels="false"
					:show-chat-thoughts="false"
					@new-model-saved="addOutputPort"
					:notebook-session="notebookSession"
				/>
			</Suspense>
		</div>
	</tera-drilldown>
</template>

<script setup lang="ts">
// Proxy to use tera-dataset via a workflow context

import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import TeraModelJupyterPanel from '@/components/model/tera-model-jupyter-panel.vue';
import { computed, onMounted, ref } from 'vue';
import { createNotebookSession, getNotebookSessionById } from '@/services/notebook-session';
import { v4 as uuidv4 } from 'uuid';
import type { NotebookSession } from '@/types/Types';
import { cloneDeep } from 'lodash';
import { getModel } from '@/services/model';
import { addDefaultConfiguration } from '@/services/model-configurations';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';

import { ModelTransformerState } from './model-transformer-operation';

const props = defineProps<{
	node: WorkflowNode<ModelTransformerState>;
}>();
const emit = defineEmits(['append-output', 'update-state', 'close']);

const modelId = computed(() => {
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
			workflowId: props.node.id,
			description: '',
			data: { history: [] }
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

	if (!model || !model.id) return;

	const state = cloneDeep(props.node.state);
	state.modelId = model?.id;
	// update node state with the model id
	emit('update-state', state);

	// set default configuration
	await addDefaultConfiguration(model);

	emit('append-output', {
		id: uuidv4(),
		label: model.header.name,
		type: 'modelId',
		value: [model.id]
	});
};
</script>

<style scoped>
.background {
	background: white;
	height: 100%;
	overflow-y: auto;
}
</style>
