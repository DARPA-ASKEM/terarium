<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<div class="background">
			<Suspense>
				<tera-dataset-jupyter-regridding-panel
					:assets="assets"
					:show-kernels="showKernels"
					:show-chat-thoughts="showChatThoughts"
					@new-dataset-saved="addOutputPort"
					:notebook-session="notebookSession"
				/>
			</Suspense>
		</div>
	</tera-drilldown>
</template>

<script setup lang="ts">
// Proxy to use tera-dataset via a workflow context

import { WorkflowNode } from '@/types/workflow';
import TeraDatasetJupyterRegriddingPanel from '@/components/dataset/tera-dataset-jupyter-regridding-panel.vue';
import { onMounted, ref } from 'vue';
import { createNotebookSession, getNotebookSessionById } from '@/services/notebook-session';
import { getDataset } from '@/services/dataset';
import type { NotebookSession, Dataset } from '@/types/Types';
import { cloneDeep } from 'lodash';
import { v4 as uuidv4 } from 'uuid';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import type { RegriddingOperationState } from './regridding-operation';

const props = defineProps<{
	node: WorkflowNode<RegriddingOperationState>;
}>();
const emit = defineEmits(['append-output', 'update-state', 'close']);

const showKernels = ref(<boolean>false);
const showChatThoughts = ref(<boolean>false);
const assets = ref<{ id: string; filename: string }[]>([]);

const notebookSession = ref(<NotebookSession | undefined>undefined);

onMounted(async () => {
	const state = cloneDeep(props.node.state);
	let notebookSessionId = props.node.state?.notebookSessionId;
	if (!notebookSessionId) {
		// create a new notebook session log if it does not exist
		const response = await createNotebookSession({
			id: uuidv4(),
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

	// Get filenames and set up assets object to be sent as context:
	const allDatasets: Dataset[] = [];
	await Promise.all(
		props.node.inputs.map(async (ele) => {
			if (ele?.value?.[0]) {
				const dataset = await getDataset(ele?.value?.[0]);
				if (dataset) allDatasets.push(dataset);
			}
		})
	);

	allDatasets.forEach((dataset) => {
		if (dataset.id && dataset.fileNames?.[0]) {
			assets.value.push({
				id: dataset.id,
				filename: dataset.fileNames[0]
			});
		}
	});

	notebookSession.value = await getNotebookSessionById(notebookSessionId!);
});

const addOutputPort = (data: any) => {
	emit('append-output', {
		id: data.id,
		label: data.name,
		type: 'datasetId',
		value: data.id
	});
};
</script>

<style scoped>
.background {
	background: white;
	height: 100%;
	overflow-y: auto;
}

:deep(.chat-input-container) {
	left: 1.5rem;
}
</style>
