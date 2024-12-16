<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<template #header-actions>
			<div class="header-action-buttons">
				<Dropdown
					v-model="selectedDataset"
					placeholder="Select a dataframe"
					:options="Object.keys(kernelState || [])"
					class="5"
					:disabled="!kernelState"
				/>
				<Button label="Save for reuse" severity="secondary" outlined :disabled="disableSaveForReuse" @click="onSave" />
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
					@update-selected-outputs="(outputs) => onUpdateSelectedOutputs(outputs)"
					@update-kernel-state="updateKernelState"
					:kernelState="kernelState"
					:selected-dataset="selectedDataset"
					:sample-agent-questions="sampleAgentQuestions"
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
import { useProjects } from '@/composables/project';
import { INotebookItem } from '@/services/jupyter';
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
			id: inputNode.value![0],
			name: useProjects().getAssetName(inputNode.value![0])
		}))
);
const disableSaveForReuse = computed(() => !kernelState.value || !selectedDataset.value);

const kernelState = ref(null);
const jupyterPanel = ref();
const selectedDataset = ref<string | null>(null);
const updateKernelState = (newKernelState: any) => {
	kernelState.value = newKernelState;
	// Default the dropdown to the last dataframe
	if (!selectedDataset.value) {
		const keys = Object.keys(newKernelState);
		selectedDataset.value = keys[keys.length - 1];
	}
};

const sampleAgentQuestions = [
	'I have two dataframes d1, d2. Join them on the column named "date". Name the joined dataframe d3.',
	'I have a dataframe d1. Show me the data types by column.',
	"I have three dataframes d1, d2, d3. d1 is incident case counts. d2 is incident hospitalization counts. d3 is cumulative death counts. Let's assume that average time to recover is 14 days and average time to exit the hospital is 10 days. Can you convert this data into prevalence data? Ideally please map it to SIRHD. Assume a population of 150 million.",
	'Add a new column to the dataframe d1 that indexes the rows from 0, 1, 2 to N.',
	'Download geojson of US counties from the Plotly GitHub repo using urlopen.',
	'I have a geopandas dataframe d1. Use Matplotlib to create a chloropleth map using the column R0. Use the cividis colormap. Add a colorbar.'
];

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

const onUpdateSelectedOutputs = (outputs: INotebookItem[]) => {
	const state = cloneDeep(props.node.state);
	state.selectedOutputs = outputs;
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

/* hacks to make the selector and save button look consistant with other items that appear in the header */
.header-action-buttons {
	display: flex;
	flex-direction: row;
	gap: var(--gap-2);
}
.header-action-buttons:deep(.p-dropdown) {
	padding: 0px 9px;
	height: 2rem;
}
.header-action-buttons:deep(.p-button) {
	height: 2rem;
}
.header-action-buttons:deep(.p-button .p-button-label) {
	font-weight: 500;
	padding: 0px var(--gap-1);
}
</style>
