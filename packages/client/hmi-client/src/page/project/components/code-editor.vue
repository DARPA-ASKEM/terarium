<template>
	<div class="code">
		<div class="controls">
			<Button
				label="Extract Model"
				:class="`p-button ${selectedText.length === 0 ? 'p-disabled' : ''}`"
				@click="onExtractModel"
			></Button>
			<FileUpload
				name="demo[]"
				:customUpload="true"
				@uploader="onFileOpen"
				mode="basic"
				auto
				chooseLabel="Load File"
			/>
			<Button
				label="Extract petri net"
				:class="`p-button ${selectedText.length === 0 ? 'p-disabled' : ''}`"
				@click="onExtractGraph"
			></Button>
		</div>
		<v-ace-editor
			v-model:value="content"
			@init="initialize"
			lang="python"
			theme="chrome"
			style="height: 100%; width: 100%"
		/>
	</div>
	<Dialog
		v-model:visible="codeExtractionDialogVisible"
		modal
		header="Confirm extraction"
		:style="{ width: '50vw' }"
	>
		<div ref="graphElement" class="graph-element" />
		<template #footer>
			<Button label="Cancel" @click="codeExtractionDialogVisible = false" text />
			<Button label="Create model" @click="createModelFromCode()" />
		</template>
	</Dialog>
</template>

<script setup lang="ts">
import { VAceEditor } from 'vue3-ace-editor';
import FileUpload from 'primevue/fileupload';
import Button from 'primevue/button';
import '@node_modules/ace-builds/src-noconflict/mode-python';
import '@node_modules/ace-builds/src-noconflict/theme-chrome';
import { ref, watch } from 'vue';
import { logger } from '@/utils/logger';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import API from '@/api/api';
import Dialog from 'primevue/dialog';

import { runDagreLayout } from '@/services/graph';
import { PetrinetRenderer } from '@/petrinet/petrinet-renderer';
import { parsePetriNet2IGraph, PetriNet, NodeData, EdgeData } from '@/petrinet/petrinet-service';
import { IGraph } from '@graph-scaffolder/index';
import { createModel } from '@/services/model';

const DEFAULT_TEXT = '# Paste some python code here or import from the controls above';
const content = ref(DEFAULT_TEXT);
const editor = ref<VAceEditorInstance['_editor'] | null>(null);
const selectedText = ref('');
const codeExtractionDialogVisible = ref(false);
const acset = ref<PetriNet | null>(null);
const graphElement = ref<HTMLDivElement | null>(null);
// Render graph whenever a new model is fetched or whenever the HTML element
//	that we render the graph to changes.
watch([graphElement], async () => {
	if (acset.value === null || graphElement.value === null) return;
	// Convert petri net into a graph
	const g: IGraph<NodeData, EdgeData> = parsePetriNet2IGraph(acset.value);
	// Create renderer
	const renderer = new PetrinetRenderer({
		el: graphElement.value as HTMLDivElement,
		useAStarRouting: true,
		runLayout: runDagreLayout,
		dragSelector: 'no-drag'
	});

	// Render graph
	await renderer?.setData(g);
	await renderer?.render();
});

const emit = defineEmits(['on-model-created']);

/**
 * File open/add event handler.  Immediately render the contents of the file to the editor
 * content
 * @param event	the input event when a file is added
 */
async function onFileOpen(event) {
	const reader = new FileReader();
	reader.readAsText(event.files[0], 'UTF-8');
	reader.onload = (evt) => {
		content.value = evt?.target?.result?.toString() ?? DEFAULT_TEXT;
	};
}

async function onExtractGraph() {
	// const response = await API.post(`code/to_acset?code=${selectedText.value}`);
	const response = {
		S: [
			{ sname: 'S', uid: 1 },
			{ sname: 'I', uid: 2 },
			{ sname: 'D', uid: 3 },
			{ sname: 'A', uid: 4 },
			{ sname: 'R', uid: 5 },
			{ sname: 'T', uid: 6 },
			{ sname: 'H', uid: 7 },
			{ sname: 'E', uid: 8 }
		],
		T: [
			{ tname: 'alpha', uid: 10 },
			{ tname: ' beta', uid: 11 },
			{ tname: ' gamma', uid: 12 },
			{ tname: ' delta', uid: 13 },
			{ tname: ' epsilon', uid: 14 },
			{ tname: ' mu', uid: 15 },
			{ tname: ' zeta', uid: 16 },
			{ tname: ' lamda', uid: 17 },
			{ tname: ' eta', uid: 18 },
			{ tname: ' rho', uid: 19 },
			{ tname: ' theta', uid: 20 },
			{ tname: ' kappa', uid: 21 },
			{ tname: ' nu', uid: 22 },
			{ tname: ' xi', uid: 23 },
			{ tname: ' sigma', uid: 24 },
			{ tname: ' tau', uid: 25 }
		],
		I: [
			{ it: 1, is: 1 },
			{ it: 2, is: 2 },
			{ it: 3, is: 2 },
			{ it: 4, is: 2 },
			{ it: 5, is: 3 },
			{ it: 6, is: 4 },
			{ it: 7, is: 4 },
			{ it: 8, is: 5 },
			{ it: 9, is: 6 },
			{ it: 10, is: 2 },
			{ it: 11, is: 3 },
			{ it: 12, is: 4 },
			{ it: 13, is: 5 },
			{ it: 14, is: 6 }
		],
		O: [
			{ ot: 1, os: 2 },
			{ ot: 2, os: 3 },
			{ ot: 3, os: 4 },
			{ ot: 4, os: 5 },
			{ ot: 5, os: 8 },
			{ ot: 6, os: 5 },
			{ ot: 7, os: 6 },
			{ ot: 8, os: 7 },
			{ ot: 9, os: 7 },
			{ ot: 10, os: 7 },
			{ ot: 11, os: 7 },
			{ ot: 12, os: 7 },
			{ ot: 13, os: 7 },
			{ ot: 14, os: 8 }
		]
	};
	acset.value = response;
	codeExtractionDialogVisible.value = true;
}

/**
 * Send the selected contents of the editor to the backend for persistence and model extraction
 * via TA1
 */
async function onExtractModel() {
	logger.info(`Transforming: ${selectedText.value}`);
	const response = await API.post('/code', selectedText.value);
	// eslint-disable-next-line
	alert(JSON.stringify(response.data));
}

/**
 * Event handler for selected text change in the code editor
 */
function onSelectedTextChange() {
	selectedText.value = editor.value?.getSelectedText() ?? '';
}

/**
 * Editor initialization function
 * @param editorInstance	the Ace editor instance
 */
async function initialize(editorInstance) {
	editor.value = editorInstance;
	editorInstance.session.selection.on('changeSelection', onSelectedTextChange);
	editorInstance.setShowPrintMargin(false);
}

async function createModelFromCode() {
	const modelName = 'New model';
	const newModel = {
		name: 'New model',
		framework: 'Petri Net',
		content: JSON.stringify(acset.value)
	};
	const model = await createModel(newModel);
	if (model) {
		emit('on-model-created', model.id, modelName);
	}
}
</script>
<style>
.code {
	display: flex;
	flex-direction: column;
	flex: 1;
	padding-top: 1rem;
}

.controls {
	margin-left: 40px;
	margin-bottom: 10px;
	display: flex;
	gap: 10px;
}

.graph-element {
	flex: 1;
	height: 400px;
	width: 100%;
	border: 1px solid var(--surface-border);
	overflow: hidden;
	border-radius: 0.25rem;
}
</style>
