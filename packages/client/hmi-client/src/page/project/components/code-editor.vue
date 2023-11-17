<template>
	<tera-asset name="New file" overline="Python">
		<template #edit-buttons>
			<FileUpload
				name="demo[]"
				:customUpload="true"
				@uploader="onFileOpen"
				mode="basic"
				auto
				chooseLabel="Load file"
				class="p-button-sm p-button-secondary outline-upload-button"
			/>
		</template>
		<v-ace-editor
			v-model:value="code"
			@init="initialize"
			lang="python"
			theme="chrome"
			style="height: 100%; width: 100%"
			class="ace-editor"
		/>
	</tera-asset>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import FileUpload from 'primevue/fileupload';
import '@node_modules/ace-builds/src-noconflict/mode-python';
import '@node_modules/ace-builds/src-noconflict/theme-chrome';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import { runDagreLayout } from '@/services/graph';
import { PetrinetRenderer } from '@/model-representation/petrinet/petrinet-renderer';
import { parsePetriNet2IGraph, PetriNet, NodeData, EdgeData } from '@/petrinet/petrinet-service';
import { IGraph } from '@graph-scaffolder/index';
import TeraAsset from '@/components/asset/tera-asset.vue';

const props = defineProps({
	initialCode: {
		type: String,
		default: '# Paste some python code here or import from the controls above'
	}
});

const code = ref(props.initialCode);
const editor = ref<VAceEditorInstance['_editor'] | null>(null);
const selectedText = ref('');
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

/**
 * File open/add event handler.  Immediately render the contents of the file to the editor
 * content
 * @param event	the input event when a file is added
 */
async function onFileOpen(event) {
	const reader = new FileReader();
	reader.readAsText(event.files[0], 'UTF-8');
	reader.onload = (evt) => {
		code.value = evt?.target?.result?.toString() ?? props.initialCode;
	};
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
</script>

<style scoped>
.p-fileupload-choose.p-button.outline-upload-button {
	background-color: var(--surface-0);
	border: 1px solid var(--surface-border);
	color: var(--text-color-primary);
	width: 100%;
	font-size: var(--font-body-small);
}

.p-fileupload-choose.p-button.p-button.outline-upload-button:hover {
	background-color: var(--surface-hover);
	color: var(--text-color-primary);
}

.ace-editor {
	border-top: 1px solid var(--surface-border-light);
}

.graph-element {
	flex: 1;
	height: 20rem;
	width: 100%;
	border: 1px solid var(--surface-border);
	overflow: hidden;
	border-radius: 0.25rem;
}

.p-dialog .p-dialog-content h6 {
	margin: 1rem 0 1rem 0;
}
</style>
