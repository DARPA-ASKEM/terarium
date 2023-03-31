<template>
	<div class="code">
		<div class="controls">
			<Button
				label="Extract model"
				:class="`p-button ${selectedText.length === 0 ? 'p-disabled' : ''}`"
				@click="onExtractModel"
				:loading="extractPetrinetLoading"
			></Button>
			<FileUpload
				name="demo[]"
				:customUpload="true"
				@uploader="onFileOpen"
				mode="basic"
				auto
				chooseLabel="Load file"
				class="p-button-secondary outline-upload-button"
			/>
		</div>
		<v-ace-editor
			v-model:value="code"
			@init="initialize"
			lang="python"
			theme="chrome"
			style="height: 100%; width: 100%"
			class="ace-editor"
		/>
	</div>
	<Dialog
		v-model:visible="codeExtractionDialogVisible"
		modal
		header="Confirm extraction"
		:style="{ width: '50vw' }"
	>
		<div ref="graphElement" class="graph-element" />
		<h6>
			Terarium can extract metadata about this model from related papers. Select the papers you
			would like to use.
		</h6>
		<DataTable v-model:selection="selectedPaper" :value="resources" dataKey="id">
			<Column selectionMode="multiple"></Column>
			<Column field="title" header="Title"></Column>
			<!-- <Column field="authors" header="Authors"></Column> -->
			<!-- <Column field="year" header="Year"></Column> -->
		</DataTable>
		<template #footer>
			<Button label="Cancel" @click="codeExtractionDialogVisible = false" text />
			<Button label="Create model" @click="createModelFromCode()" :loading="createModelLoading" />
		</template>
	</Dialog>
</template>

<script setup lang="ts">
import { VAceEditor } from 'vue3-ace-editor';
import FileUpload from 'primevue/fileupload';
import Button from 'primevue/button';
import '@node_modules/ace-builds/src-noconflict/mode-python';
import '@node_modules/ace-builds/src-noconflict/theme-chrome';
import { ref, watch, computed } from 'vue';
import { logger } from '@/utils/logger';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import Dialog from 'primevue/dialog';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import useResourcesStore from '@/stores/resources';
import { runDagreLayout } from '@/services/graph';
import { PetrinetRenderer } from '@/petrinet/petrinet-renderer';
import { parsePetriNet2IGraph, PetriNet, NodeData, EdgeData } from '@/petrinet/petrinet-service';
import { IGraph } from '@graph-scaffolder/index';
import { createModel } from '@/services/model';
import { ProjectAssetTypes } from '@/types/Project';
import { getDocumentById } from '@/services/data';
import { DocumentAsset } from '@/types/Types';
import { getDocumentDoi } from '@/utils/data-util';
import { codeToAcset, findVarsFromText, getlinkedAnnotations } from '@/services/mit-askem';

const props = defineProps({
	initialCode: {
		type: String,
		default: '# Paste some python code here or import from the controls above'
	}
});

const code = ref(props.initialCode);
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

const selectedPaper = ref<DocumentAsset>();
const createModelLoading = ref(false);
const extractPetrinetLoading = ref(false);
const resourcesStore = useResourcesStore();
const resources = computed(() => {
	const storedAssets = resourcesStore.activeProjectAssets ?? [];
	const storedPapers: DocumentAsset[] = storedAssets[ProjectAssetTypes.DOCUMENTS];
	if (storedPapers) {
		const first =
			'Modelling the COVID-19 epidemic and implementation of population-wide interventions in Italy';
		storedPapers.sort((x, y) => {
			if (x.title === first) {
				return -1;
			}
			if (y.title === first) {
				return 1;
			}
			return 0;
		});
	}
	return storedPapers;
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

async function onExtractModel() {
	extractPetrinetLoading.value = true;
	const response = await codeToAcset(selectedText.value);
	extractPetrinetLoading.value = false;
	acset.value = response;
	codeExtractionDialogVisible.value = true;
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
	createModelLoading.value = true;
	if (selectedPaper.value) {
		const paperToExtractMetadata = await getDocumentById(selectedPaper.value[0].xdd_uri);
		if (paperToExtractMetadata) {
			const info = { pdf_name: '', DOI: getDocumentDoi(paperToExtractMetadata) };
			const metadata = await findVarsFromText(paperToExtractMetadata.abstractText);
			const linkAnnotationData = {
				pyacset: JSON.stringify(acset.value),
				annotations: JSON.stringify(metadata),
				info: JSON.stringify(info)
			};
			const linkedMetadata = await getlinkedAnnotations(linkAnnotationData);
			const newModelName = 'New model';
			const newModel = {
				name: newModelName,
				framework: 'Petri Net',
				content: JSON.stringify({ ...acset.value, metadata: linkedMetadata })
			};
			const model = await createModel(newModel);
			if (model) {
				emit('on-model-created', model.id, newModelName);
			} else {
				logger.error(`Something went wrong.`);
			}
		}
	}
	createModelLoading.value = false;
}
</script>

<style>
.code {
	display: flex;
	flex-direction: column;
	flex: 1;
	padding-top: 1rem;
	background-color: var(--surface-0);
}

.controls {
	margin-left: 1rem;
	margin-right: 1rem;
	margin-bottom: 1rem;
	display: flex;
	gap: 10px;
	justify-content: space-between;
}

.control-group {
	display: flex;
	gap: 1rem;
}

.outline-upload-button {
	background-color: var(--surface) !important;
	color: var(--text-color-primary) !important;
	border: 1px solid var(--surface-border) !important;
	width: 100%;
	font-size: var(--font-body-small);
}
.outline-upload-button:hover {
	background-color: var(--surface-hover) !important;
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
