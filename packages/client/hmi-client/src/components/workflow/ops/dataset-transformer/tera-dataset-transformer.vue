<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<template #header-actions>
			<div class="flex align-items-center ml-auto">
				<Dropdown
					v-model="selectedDataset"
					placeholder="Select a dataframe"
					:options="Object.keys(kernelState || [])"
					class="5"
					:disabled="!kernelState"
				/>
				<Button label="Save for reuse" severity="secondary" outlined :disabled="!kernelState" @click="onSave" />
			</div>
		</template>
		<div class="background">
			<Suspense>
				<tera-dataset-jupyter-panel
					ref="jupyterPanel"
					:assets="assets"
					:show-kernels="showKernels"
					:show-chat-thoughts="showChatThoughts"
					@new-dataset-saved="addOutputPort"
					:notebook-session="notebookSession"
					:programming-language="node.state.programmingLanguage"
					@update-language="(lang) => onUpdateLanguage(lang)"
					@update-kernel-state="updateKernelState"
					:kernelState="kernelState"
					:selected-dataset="selectedDataset"
				/>
			</Suspense>
		</div>
	</tera-drilldown>
</template>

<script setup lang="ts">
// Proxy to use tera-dataset via a workflow context

import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import TeraDatasetJupyterPanel from '@/components/dataset/tera-dataset-jupyter-panel.vue';
import { computed, onMounted, ref } from 'vue';
import { createNotebookSession, getNotebookSessionById } from '@/services/notebook-session';
import type { NotebookSession } from '@/types/Types';
import { cloneDeep } from 'lodash';
import { v4 as uuidv4 } from 'uuid';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';

import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import { DatasetTransformerState } from './dataset-transformer-operation';

const props = defineProps<{
	node: WorkflowNode<DatasetTransformerState>;
}>();
const emit = defineEmits(['append-output', 'update-state', 'close']);

const showKernels = ref(<boolean>false);
const showChatThoughts = ref(<boolean>false);
const assets = computed(() =>
	props.node.inputs
		.filter((inputNode) => inputNode.status === WorkflowPortStatus.CONNECTED && inputNode.value)
		.map((inputNode) => ({
			type: inputNode.type,
			id: inputNode.value![0]
		}))
);

const kernelState = ref(null);
const jupyterPanel = ref();
const selectedDataset = ref<string | null>(null);
const updateKernelState = (newKernelState: any) => {
	kernelState.value = newKernelState;
	// Default the dropdown to the first dataframe
	if (!selectedDataset.value) {
		selectedDataset.value = Object.keys(newKernelState)[0];
	}
};

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

	notebookSession.value = await getNotebookSessionById(notebookSessionId!);
});

const onUpdateLanguage = (language: string) => {
	const state = cloneDeep(props.node.state);
	state.programmingLanguage = language;
	emit('update-state', state);
};

const addOutputPort = (data: any) => {
	emit('append-output', {
		id: data.id,
		label: data.name,
		type: 'datasetId',
		value: data.id
	});
};

const onSave = () => {
	jupyterPanel.value?.openDialog();
};
</script>

<style scoped>
.background {
	background: white;
	height: 100%;
}
</style>
