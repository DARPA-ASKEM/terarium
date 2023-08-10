<template>
	<tera-asset :name="artifactName || `New file`" overline="Python">
		<template #edit-buttons>
			<FileUpload
				v-if="!artifactName"
				name="demo[]"
				:customUpload="true"
				@uploader="onFileOpen"
				mode="basic"
				auto
				chooseLabel="Load file"
				class="p-button-sm p-button-secondary outline-upload-button"
			/>
		</template>

		<div class="code-content" :style="{ height: !artifactName ? `100%` : `74%`, width: `100%` }">
			<section class="model-metadata">
				<InputText v-model="modelName" placeholder="Name" class="name"></InputText>
				<InputText
					v-model="modelDescription"
					placeholder="Description"
					class="description"
				></InputText>
			</section>
			<div>
				<Button
					v-if="!artifactName"
					label="Create Model from Code"
					:class="[
						'p-button-sm',
						'extract-button',
						{
							'p-disabled': editor?.getValue().length === 0
						}
					]"
					:loading="isExtractModelLoading"
					@click="onExtractModel"
				/>
				<Button
					v-else
					label="Extract All Code"
					:class="[
						'p-button-sm',
						'extract-button',
						{
							'p-disabled': selectedText !== selectionTextDefault
						}
					]"
					:loading="isExtractModelLoading"
					@click="onExtractModel"
				/>
				<div></div>
			</div>
			<v-ace-editor
				v-model:value="code"
				@init="initialize"
				lang="python"
				theme="chrome"
				:style="{ height: !artifactName ? `100%` : `100%`, width: `100%` }"
				class="ace-editor"
				:readonly="artifactName !== '' && artifactName !== null"
			/>
		</div>
		<div class="selection-content" v-if="artifactName">
			<div>
				<Button
					v-if="artifactName"
					label="Extract Selected Code"
					:class="[
						'p-button-sm',
						'extract-button',
						{
							'p-disabled': selectedText.length === 0 || selectedText === selectionTextDefault
						}
					]"
					:loading="isExtractModelLoading"
					@click="onExtractModel"
				/>
			</div>
			<v-ace-editor
				v-model:value="selectedText"
				lang="python"
				theme="github"
				class="ace-editor2"
				:style="{
					height: `${
						selectedText.split('\n').length > 1 ? selectedText.split('\n').length * 17 : 30
					}px`,
					maxHeight: `27%`
				}"
				:readonly="artifactName !== ''"
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
				Terarium can extract metadata about this code from related papers. Select the artifacts you
				would like to use.
			</h6>

			<DataTable v-model:selection="selectedPapers" :value="resources" dataKey="id">
				<Column selectide="multiple" />
				<Column field="title" header="Title" />
			</DataTable>
			<template #footer>
				<Button label="Cancel" @click="codeExtractionDialogVisible = false" text />
				<Button label="Create model" @click="createModelFromCode()" :loading="createModelLoading" />
			</template>
		</Dialog>
	</tera-asset>
</template>

<script setup lang="ts">
import { ref, watch, computed, PropType } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import FileUpload from 'primevue/fileupload';
import Button from 'primevue/button';
import '@node_modules/ace-builds/src-noconflict/mode-python';
import '@node_modules/ace-builds/src-noconflict/theme-chrome';
import '@node_modules/ace-builds/src-noconflict/theme-github';
import { logger } from '@/utils/logger';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import Dialog from 'primevue/dialog';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import useResourcesStore from '@/stores/resources';
import { runDagreLayout } from '@/services/graph';
import { PetrinetRenderer } from '@/model-representation/petrinet/petrinet-renderer';
import { parsePetriNet2IGraph, PetriNet, NodeData, EdgeData } from '@/petrinet/petrinet-service';
import { IGraph } from '@graph-scaffolder/index';
import { ProjectAssetTypes, IProject } from '@/types/Project';
import { getDocumentById } from '@/services/data';
import { DocumentAsset, EventType } from '@/types/Types';
import { PDFExtractionResponseType } from '@/types/common';
import { getDocumentDoi } from '@/utils/data-util';
import TeraAsset from '@/components/asset/tera-asset.vue';
import {
	findVarsFromText,
	FindVarsFromTextResponseType,
	getlinkedAnnotations
} from '@/services/mit-askem';
import { getPDFURL } from '@/services/generate-download-link';
import API, { Poller } from '@/api/api';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import { createModel } from '@/services/model';
import * as EventService from '@/services/event';
import { codeToAMR } from '@/services/models/extractions';
import * as ProjectService from '@/services/project';
import InputText from 'primevue/inputtext';
import { CodeArtifactExtractionMetaData } from '@/types/Code';
import { convertAMRToACSet } from '@/model-representation/petrinet/petrinet-service';

const props = defineProps({
	assetId: { type: String, default: null, required: false },
	artifactName: { type: String, default: null, required: false },
	project: {
		type: Object as PropType<IProject> | null,
		default: null,
		required: false
	},
	initialCode: {
		type: String,
		default: '# Paste some python code here or import from the controls above'
	}
});

const router = useRouter();

const selectionTextDefault = 'Select some code above to convert a block of code into a Model.';
const code = ref(props.initialCode);
const editor = ref<VAceEditorInstance['_editor'] | null>(null);
const selectedText = ref(selectionTextDefault);
const selectedRange = ref();
const selectedTextMetaData = ref<CodeArtifactExtractionMetaData | null>(null);
const codeExtractionDialogVisible = ref(false);
const acset = ref<PetriNet | null>(null);
const graphElement = ref<HTMLDivElement | null>(null);
const modelName = ref<string>(props.artifactName || 'NewFile.py');
const modelDescription = ref<string>(
	`This model was created from ${modelName.value || 'the code editor'}`
);

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

watch(
	() => modelName.value,
	() => {
		modelDescription.value = `This model was created from ${modelName.value}`;
	}
);

watch(
	() => props.initialCode,
	() => {
		code.value = props.initialCode;
	}
);

const selectedPapers = ref<DocumentAsset[]>();
const createModelLoading = ref(false);
const isExtractModelLoading = ref(false);
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

/**
 * Event handler for the create model button in the code extraction dialog
 */
async function onExtractModel() {
	isExtractModelLoading.value = true;
	const amr = await codeToAMR(
		props.assetId,
		modelName.value,
		modelDescription.value,
		selectedTextMetaData.value
	);

	EventService.create(EventType.ExtractModel, useResourcesStore().activeProject?.id);

	isExtractModelLoading.value = false;
	if (amr) {
		acset.value = convertAMRToACSet(amr);
	}
	codeExtractionDialogVisible.value = true;
}

/**
 * Event handler for selected text change in the code editor
 */
function onSelectedTextChange() {
	selectedText.value =
		editor.value?.getSelectedText() === undefined || editor.value?.getSelectedText() === ''
			? selectionTextDefault
			: editor.value?.getSelectedText();
	selectedRange.value = editor.value?.getSelectionRange();
	const block = `L${selectedRange.value.start.row}-L${selectedRange.value.end.row}`;
	selectedTextMetaData.value = <CodeArtifactExtractionMetaData>{
		metadata: {
			dynamics: [{ name: props.artifactName, filename: props.artifactName, block }]
		}
	};
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

function extractMetadataElements(listOfObjects) {
	return listOfObjects.reduce((newList, obj) => {
		if (Array.isArray(obj.metadata)) {
			newList.push(...obj.metadata);
		}
		return newList;
	}, []);
}

async function createModelFromCode() {
	createModelLoading.value = true;
	if (selectedPapers.value) {
		const selectedDocs = await selectedPapers.value.map(async (dAsset) => {
			const paperToExtractMetadata = await getDocumentById(dAsset.xdd_uri);
			const doi = getDocumentDoi(paperToExtractMetadata);

			const pdfURL = await getPDFURL(doi);

			let text: string = '';
			let metadata: FindVarsFromTextResponseType | null = null;
			if (pdfURL !== '') {
				const results = await getPDFContents(pdfURL);
				text = results.text ? results.text : '';
				metadata = await findVarsFromText(text);
			}
			return {
				pdf_name: dAsset.title,
				xdd_uri: dAsset.xdd_uri,
				extracted_text: text || '',
				doi,
				metadata
			};
		});

		const info = { pdf_name: '', DOI: '' };
		const extractedMetadataElements = extractMetadataElements(selectedDocs);

		const linkAnnotationData = {
			pyacset: JSON.stringify(acset.value),
			annotations: JSON.stringify(extractedMetadataElements),
			info: JSON.stringify(info)
		};

		const linkedMetadata = await getlinkedAnnotations(linkAnnotationData);
		const newModelName = 'New model';
		const newModel = {
			name: newModelName,
			content: JSON.stringify({ ...acset.value, metadata: linkedMetadata })
		};
		const model = await createModel(newModel);
		if (model && props.project && resourcesStore) {
			await ProjectService.addAsset(
				props.project.id,
				ProjectAssetTypes.MODELS,
				model.id.toString()
			);

			router.push({
				name: RouteName.ProjectRoute,
				params: {
					assetName: newModelName,
					assetId: model.id,
					pageType: ProjectAssetTypes.MODELS
				}
			});
		} else {
			logger.error(`Something went wrong.`);
		}
	}
	createModelLoading.value = false;
}

async function getPDFContents(url: string): Promise<PDFExtractionResponseType> {
	const result = await API.get(`/extract/convertpdfurl/`, {
		params: {
			url,
			extraction_method: 'pymupdf',
			extract_images: 'false'
		}
	});

	if (result) {
		const taskID = result.data.task_id;

		const poller = new Poller<object>()
			.setInterval(2000)
			.setThreshold(90)
			.setPollAction(async () => {
				const response = await API.get(`/extract/task-result/${taskID}`);

				if (response.data.status === 'SUCCESS' && response.data.result) {
					return {
						data: response.data.result,
						progress: null,
						error: null
					};
				}
				return {
					data: null,
					progress: null,
					error: null
				};
			});
		const pollerResults = await poller.start();

		if (pollerResults.data) {
			return pollerResults.data as PDFExtractionResponseType;
		}
	}
	return { text: '', images: [] } as PDFExtractionResponseType;
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

.code-content {
	display: flex;
	flex-direction: column;
	height: 100%;
	margin-bottom: 5px;
	transition: height 2s ease-out;
}

.ace-editor {
	border-top: 1px solid var(--surface-border-light);
	margin-bottom: 5px;
	border-radius: 5px;
}

.selection-content {
	display: flex;
	flex-direction: column;
	max-height: 25%;
	margin-bottom: 5px;
	transition: height 1s ease-out;
}

.ace-editor2 {
	margin-bottom: 5px;
	border-radius: 5px;
	transition: height 1s ease-out;
}
.extract-button {
	margin-bottom: 5px;
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

.model-metadata {
	display: flex;
	flex-direction: row;
	margin-bottom: 5px;
}

.name {
	padding: 0.51rem 0.5rem;
	width: 300px;
	font-size: var(--font-caption);
}

.description {
	padding: 0.51rem 0.5rem;
	width: 500px;
	font-size: var(--font-caption);
}
</style>
