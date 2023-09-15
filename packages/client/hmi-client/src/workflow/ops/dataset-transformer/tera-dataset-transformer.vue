<template>
	<div class="background">
		<Suspense>
			<tera-dataset-jupyter-panel
				asset-id="e3aa40fd-9eb5-4d11-8932-65d478b2bd1f"
				:asset-ids="assetIds"
				:project="props.project"
				:show-kernels="showKernels"
				:show-chat-thoughts="showChatThoughts"
				@new-dataset-saved="addOutputPort"
				:notebook-session="notebookSession"
			/>
		</Suspense>
	</div>
</template>

<script setup lang="ts">
// Proxy to use tera-dataset via a workflow context

import { IProject } from '@/types/Project';
import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import TeraDatasetJupyterPanel from '@/components/dataset/tera-dataset-jupyter-panel.vue';
import { computed, onMounted, ref } from 'vue';
import { workflowEventBus } from '@/services/workflow';
import { createNotebookSession, getNotebookSessionById } from '@/services/notebook-session';
import { NotebookSession } from '@/types/Types';
import { cloneDeep } from 'lodash';
import crypto from 'crypto';

const props = defineProps<{
	node: WorkflowNode;
	project?: IProject;
}>();
const showKernels = ref(<boolean>false);
const showChatThoughts = ref(<boolean>false);
const assetIds = computed(() =>
	props.node?.inputs
		.filter((inputNode) => inputNode.status === WorkflowPortStatus.CONNECTED && inputNode.value)
		.map((inputNode) => inputNode.value![0])
);

const notebookSession = ref(<NotebookSession | undefined>undefined);

onMounted(async () => {
	let notebookSessionId = props.node.state?.notebookSessionId;
	if (!notebookSessionId) {
		// create a new notebook session log if it does not exist
		const response = await createNotebookSession({
			id: crypto.randomUUID(),
			name: props.node.id,
			description: '',
			data: { history: [] },
			timestamp: new Date().toISOString()
		});
		notebookSessionId = response?.id;

		if (notebookSessionId) {
			// update the node state with the notebook session id
			const state = cloneDeep(props.node.state);
			state.notebookSessionId = notebookSessionId;
			workflowEventBus.emit('update-state', {
				node: props.node,
				state
			});
		}
	}

	notebookSession.value = await getNotebookSessionById(notebookSessionId);
});

const addOutputPort = (data) => {
	workflowEventBus.emit('append-output-port', {
		node: props.node,
		port: { id: data.id, label: data.name, type: 'datasetId', value: data.id }
	});
};
</script>

<style>
.background {
	background: white;
	height: 100%;
	overflow-y: auto;
}
</style>
