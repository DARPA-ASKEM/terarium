<template>
	<tera-asset :is-loading="isLoading" stretch-content overflow-hidden>
		<template #name-input v-if="!isPreview">
			<section class="header">
				<section class="name">
					<InputText v-model="codeName" class="name-input" @change="() => saveCode" />
				</section>
				<section class="buttons">
					<template v-if="programmingLanguage !== ProgrammingLanguage.Zip">
						<FileUpload
							name="demo[]"
							:customUpload="true"
							@uploader="onFileOpen"
							mode="basic"
							auto
							chooseLabel="Load file"
						/>
						<Button outlined label="Save" @click="saveCode()" />
						<Button outlined label="Save as new" @click="isCodeNamingModalVisible = true" />
						<Button
							label="Create model from code"
							@click="isModelNamingModalVisible = true"
							:loading="isCodeToModelLoading"
						/>
					</template>
					<template v-else>
						<Button
							label="Create model from code"
							@click="isModelNamingModalVisible = true"
							:loading="isCodeToModelLoading"
						/>
						<Button label="Download Zip" />
					</template>
				</section>
			</section>
		</template>
		<div v-if="programmingLanguage !== ProgrammingLanguage.Zip" class="code-asset-content">
			<tera-directory v-if="fileNames.length > 1" :files="fileNames" @fileClicked="onFileSelect" />
			<div class="code-asset-editor">
				<header class="code-asset-editor-header">
					<h1>{{ codeSelectedFile }}</h1>
					<Dropdown
						v-model="programmingLanguage"
						:options="programmingLanguages"
						@change="onFileTypeChange"
					/>
				</header>
				<v-ace-editor
					v-model:value="codeText"
					@init="initialize"
					:lang="programmingLanguage"
					theme="chrome"
					style="height: 100%; width: 100%"
					class="ace-editor"
				/>
			</div>
			<div class="code-blocks-container">
				<div>
					<h2>Code Blocks</h2>
					<p>Identify the code blocks that represent the core dynamics of your model.</p>
					<Button
						:disabled="selectionRange === null"
						text
						icon="pi pi-plus"
						label="Add a code block"
						@click="isDynamicsModalVisible = true"
					/>
					<tera-code-dynamic
						v-if="codeAssetCopy"
						:code="codeAssetCopy"
						@remove="onRemoveCodeBlock"
						@save="onSaveCodeBlock"
						:is-preview="props.isPreview"
					/>
				</div>
				<div class="code-blocks-buttons-container">
					<Button
						:loading="savingAsset"
						label="Cancel"
						outlined
						size="large"
						@click="onCancelChanges"
					/>
					<Button
						:loading="savingAsset"
						:disabled="isEqual(codeAsset, codeAssetCopy)"
						label="Apply changes"
						size="large"
						@click="onSaveChanges"
					/>
				</div>
			</div>
		</div>
		<div v-else>
			<!-- TODO: show entire file tree for github -->
			<a v-if="repoUrl" :href="repoUrl" target="_blank" rel="noreferrer noopener">{{ repoUrl }}</a>
		</div>
		<Teleport to="body">
			<tera-modal
				v-if="isModelNamingModalVisible"
				@modal-mask-clicked="isModelDiagramModalVisible = false"
				@modal-enter-press="isModelDiagramModalVisible = false"
			>
				<template #header>
					<h4>New model</h4>
				</template>
				<template #default>
					<form @submit.prevent>
						<label for="model-name">Enter a unique name for your model</label>
						<InputText id="model-name" type="text" v-model="newModelName" />
						<label for="model-description">Enter a description (optional)</label>
						<Textarea v-model="newModelDescription" />
						<div class="form-checkbox">
							<Checkbox v-model="willGenerateFromDynamics" binary />
							<label>Generate from dynamics</label>
						</div>
					</form>
				</template>
				<template #footer>
					<Button
						label="Create model"
						@click="
							() => {
								isModelNamingModalVisible = false;
								extractModel();
							}
						"
					/>
					<Button
						label="Cancel"
						severity="secondary"
						outlined
						@click="isModelNamingModalVisible = false"
					/>
				</template>
			</tera-modal>
			<tera-modal
				v-if="isDynamicsModalVisible"
				@modal-mask-clicked="isDynamicsModalVisible = false"
				@modal-enter-press="isDynamicsModalVisible = false"
			>
				<template #header>
					<h4>Add dynamics</h4>
				</template>
				<template #default>
					<form @submit.prevent>
						<label for="model-name">Dynamics name</label>
						<InputText id="model-name" type="text" v-model="newDynamicsName" />
						<label for="model-description">Enter a description (optional)</label>
						<Textarea v-model="newDynamicsDescription" />
					</form>
				</template>
				<template #footer>
					<Button
						label="Add dynamics"
						@click="
							() => {
								isDynamicsModalVisible = false;
								addDynamic();
							}
						"
					/>
					<Button
						label="Cancel"
						severity="secondary"
						outlined
						@click="isDynamicsModalVisible = false"
					/>
				</template>
			</tera-modal>
			<tera-modal
				v-if="isCodeNamingModalVisible"
				@modal-mask-clicked="isCodeNamingModalVisible = false"
				@modal-enter-press="isCodeNamingModalVisible = false"
			>
				<template #header>
					<h4>Save new code</h4>
				</template>
				<template #default>
					<form @submit.prevent>
						<label for="model-name">Name</label>
						<InputText id="model-name" type="text" v-model="newCodeName" />
					</form>
				</template>
				<template #footer>
					<Button
						label="Cancel"
						class="p-button-secondary"
						@click="isCodeNamingModalVisible = false"
					/>
					<Button
						label="Save code"
						@click="
							() => {
								isCodeNamingModalVisible = false;
								saveNewCode();
							}
						"
					/>
				</template>
			</tera-modal>
		</Teleport>
	</tera-asset>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import 'ace-builds/src-noconflict/mode-python';
import 'ace-builds/src-noconflict/mode-julia';
import 'ace-builds/src-noconflict/mode-r';
import Button from 'primevue/button';
import Checkbox from 'primevue/checkbox';
import {
	getCodeAsset,
	getCodeFileAsText,
	getProgrammingLanguage,
	setFileExtension,
	updateCodeAsset,
	uploadCodeToProject
} from '@/services/code';
import { useToastService } from '@/services/toast';
import { codeToAMR } from '@/services/knowledge';
import { AssetType, Code, CodeFile, ProgrammingLanguage } from '@/types/Types';
import TeraModal from '@/components/widgets/tera-modal.vue';
import InputText from 'primevue/inputtext';
import router from '@/router';
import { RouteName } from '@/router/routes';
import FileUpload from 'primevue/fileupload';
import Textarea from 'primevue/textarea';
import TeraAsset from '@/components/asset/tera-asset.vue';
import { useProjects } from '@/composables/project';
import Dropdown from 'primevue/dropdown';
import { Ace, Range } from 'ace-builds';
import { cloneDeep, isEmpty, isEqual } from 'lodash';
import { extractDynamicRows } from '@/utils/code-asset';
import TeraDirectory from './tera-directory.vue';
import TeraCodeDynamic from './tera-code-dynamic.vue';

const INITIAL_TEXT = '# Paste some code here';

const props = defineProps<{
	assetId: string;
	isPreview?: boolean;
}>();

const emit = defineEmits(['asset-loaded', 'apply-changes']);

const toast = useToastService();

const existingMarkers = new Set();

const codeName = ref('');
const codeSelectedFile = ref('');
const codeText = ref(INITIAL_TEXT);
const codeAsset = ref<Code | null>(null);
const editor = ref<VAceEditorInstance['_editor'] | null>(null);
const selectedText = ref('');
const selectionRange = ref<Ace.Range | null>(null);
const progress = ref(0);
const isCodeToModelLoading = ref(false);
const willGenerateFromDynamics = ref(false);
const isModelDiagramModalVisible = ref(false);
const isModelNamingModalVisible = ref(false);
const isCodeNamingModalVisible = ref(false);
const isDynamicsModalVisible = ref(false);
const newCodeName = ref('');
const newModelName = ref('');
const newModelDescription = ref('');
const newDynamicsName = ref('');
const newDynamicsDescription = ref('');
const programmingLanguage = ref<ProgrammingLanguage>(ProgrammingLanguage.Python);
const programmingLanguages = [
	ProgrammingLanguage.Julia,
	ProgrammingLanguage.Python,
	ProgrammingLanguage.R
];
const isLoading = ref(false);

const repoUrl = computed(() => codeAsset.value?.repoUrl ?? '');

const selectedRangeToString = computed(() =>
	selectionRange.value
		? `L${selectionRange.value.start.row + 1}-L${selectionRange.value.end.row + 1}`
		: ''
);

const fileNames = computed<string[]>(() => {
	if (!codeAssetCopy.value?.files) return [];
	return Object.keys(codeAssetCopy.value?.files);
});

const codeAssetCopy = ref<Code | null>(null);
const savingAsset = ref(false);
/**
 * Editor initialization function
 * @param editorInstance	the Ace editor instance
 */
async function initialize(editorInstance) {
	editor.value = editorInstance;
	editorInstance.session.selection.on('changeSelection', onSelectedTextChange);
	editorInstance.setShowPrintMargin(false);
}

/**
 * Event handler for selected text change in the code editor
 */
function onSelectedTextChange() {
	selectedText.value = editor.value?.getSelectedText() ?? '';
	selectionRange.value =
		!isEmpty(selectedText.value) && editor.value ? editor.value.getSelectionRange() : null;
}

function highlightDynamics() {
	if (codeAssetCopy.value?.files) {
		const { files } = codeAssetCopy.value;

		Object.keys(files).forEach((fileName) => {
			if (fileName === codeSelectedFile.value) {
				const block = files[fileName]?.dynamics?.block;
				// Loop through every highlighted block
				for (let i = 0; i < block?.length; i++) {
					// Avoids rehighlighting
					if (!existingMarkers.has(block[i])) {
						// Extract start and end rows and highlight them in the editor
						const { startRow, endRow } = extractDynamicRows(block[i]);
						if (!Number.isNaN(startRow) && !Number.isNaN(endRow)) {
							editor.value?.session.addMarker(
								new Range(startRow, 0, endRow, 0),
								'ace-active-line',
								'fullLine'
							);
							existingMarkers.add(block[i]);
						}
					}
				}
			}
		});
	} else if (!isEmpty(existingMarkers)) {
		removeMarkers();
	}
}

function removeMarkers() {
	existingMarkers.clear();
	if (editor.value) {
		const markers = editor.value.session.getMarkers();
		if (markers) {
			Object.keys(markers).forEach((item) => editor.value?.session.removeMarker(markers[item].id));
		}
	}
}

async function addDynamic() {
	// Add highlighted block to dynamics
	if (selectedRangeToString.value && codeAssetCopy.value) {
		if (!codeAssetCopy.value.files) codeAssetCopy.value.files = {};
		if (codeAssetCopy.value.files[codeSelectedFile.value]?.dynamics?.block) {
			codeAssetCopy.value.files[codeSelectedFile.value].dynamics.block.push(
				selectedRangeToString.value
			);
		} else {
			codeAssetCopy.value.files[codeSelectedFile.value] = {
				language: getProgrammingLanguage(codeName.value),
				dynamics: {
					name: newDynamicsName.value,
					description: newDynamicsDescription.value,
					block: [selectedRangeToString.value]
				}
			};
		}
	}

	highlightDynamics();
}

async function saveCode(codeAssetToSave: Code | null = codeAssetCopy.value) {
	if (codeAssetToSave?.id) {
		const code = { ...codeAssetToSave, name: codeName.value };
		const file = new File([codeText.value], codeSelectedFile.value);

		const res = await updateCodeAsset(code, file, progress); // This returns an object with an id not the whole code asset...
		if (!res?.id) {
			toast.error('', 'Unable to save file');
			return;
		}
		await refreshCodeAsset(res.id);
		toast.success('', `Saved Code Asset`);
		highlightDynamics();
	} else {
		newCodeName.value = codeName.value;
		saveNewCode();
	}
}

async function saveNewCode() {
	newCodeName.value = setFileExtension(newCodeName.value, programmingLanguage.value);
	const file = new File([codeText.value], newCodeName.value);
	const newCode = await uploadCodeToProject(file, progress);
	let newAsset;
	if (newCode?.id) {
		newAsset = await useProjects().addAsset(AssetType.Code, newCode.id);
	}
	if (!newAsset) {
		toast.error('', 'Unable to save file');
		return;
	}
	toast.success('', `File saved as ${codeName.value}`);
	codeAsset.value = newCode;

	router.push({
		name: RouteName.Project,
		params: {
			pageType: AssetType.Code,
			projectId: useProjects().activeProject.value?.id,
			assetId: codeAsset?.value?.id
		}
	});
}

async function refreshCodeAsset(codeId: string) {
	const code = await getCodeAsset(codeId);
	if (code) {
		codeAsset.value = code;
		codeAssetCopy.value = cloneDeep(codeAsset.value);
	}
}

async function extractModel() {
	await saveCode();
	if (codeAsset.value?.id) {
		isCodeToModelLoading.value = true;
		const extractedModelId = await codeToAMR(
			codeAsset.value.id,
			newModelName.value,
			newModelDescription.value,
			willGenerateFromDynamics.value
		);
		isCodeToModelLoading.value = false;
		if (extractedModelId) {
			await useProjects().addAsset(
				AssetType.Model,
				extractedModelId,
				useProjects().activeProject.value?.id
			);
			router.push({
				name: RouteName.Project,
				params: {
					pageType: AssetType.Model,
					assetId: extractedModelId
				}
			});
		}
	}
}

async function onFileOpen(event) {
	const file = event.files[0];
	const reader = new FileReader();
	reader.readAsText(file, 'UTF-8');
	reader.onload = (evt) => {
		removeMarkers();

		if (codeAssetCopy.value) {
			codeAssetCopy.value.files = { ...codeAssetCopy.value.files, [file.name]: {} };
		}
		codeText.value = evt?.target?.result?.toString() ?? codeText.value;
		codeSelectedFile.value = file.name;
	};
}

async function onFileSelect(filePath: string) {
	codeSelectedFile.value = filePath;
	const text = await getCodeFileAsText(props.assetId, filePath);
	if (text) {
		codeText.value = text;
	}

	removeMarkers();
	highlightDynamics();
}

function onRemoveCodeBlock(dynamic: { [index: string]: CodeFile }) {
	if (!codeAssetCopy.value?.files) return;
	codeAssetCopy.value.files = {
		...codeAssetCopy.value?.files,
		...dynamic
	};

	removeMarkers();
	highlightDynamics();
}

function onSaveCodeBlock(dynamic: { [index: string]: CodeFile }) {
	if (!codeAssetCopy.value?.files) return;
	codeAssetCopy.value.files = {
		...codeAssetCopy.value?.files,
		...dynamic
	};

	removeMarkers();
	highlightDynamics();
}

function onCancelChanges() {
	codeAssetCopy.value = cloneDeep(codeAsset.value);
	removeMarkers();
	highlightDynamics();
}

// delete old file key, copy to new file key
function onFileTypeChange() {
	// TODO: changing the file type messes with the ordering in the directory tree by bringing the file to the bottom, but more of an aethetic issue.
	if (codeAssetCopy.value?.files) {
		const oldCodefile = codeAssetCopy.value.files[codeSelectedFile.value];
		delete codeAssetCopy.value.files[codeSelectedFile.value];
		codeSelectedFile.value = setFileExtension(codeSelectedFile.value, programmingLanguage.value);
		codeAssetCopy.value.files[codeSelectedFile.value] = { ...oldCodefile };
	}

	programmingLanguage.value = getProgrammingLanguage(codeSelectedFile.value);
}

async function onSaveChanges() {
	if (!codeAssetCopy.value) return;
	savingAsset.value = true;
	const file = new File([codeText.value], codeSelectedFile.value);
	const updateResponse = await updateCodeAsset(codeAssetCopy.value, file, progress);
	if (!updateResponse || !updateResponse.id) {
		savingAsset.value = false;
		return;
	}
	await refreshCodeAsset(updateResponse.id);
	toast.success('', 'Changes applied succesfully');
	savingAsset.value = false;
	emit('apply-changes', cloneDeep(codeAsset.value));
}

watch(
	// need to wait for the ace editor to render to show initial highlights
	() => editor.value,
	() => {
		if (editor.value) {
			removeMarkers();
			highlightDynamics();
		}
	}
);

watch(
	() => props.assetId,
	async () => {
		if (props.assetId === AssetType.Code) {
			// FIXME: assetId is 'code' for a newly opened code asset; a hack to get around some weird tab behaviour
			codeAsset.value = null;
			codeName.value = 'newcode.py';
			codeText.value = INITIAL_TEXT;
			programmingLanguage.value = ProgrammingLanguage.Python;
		} else {
			isLoading.value = true;
			const code = await getCodeAsset(props.assetId);
			if (code && code.files && Object.keys(code.files)[0]) {
				codeAsset.value = code;
				codeName.value = code.name;

				const filename = Object.keys(code.files)[0];
				codeSelectedFile.value = filename;

				const text = await getCodeFileAsText(props.assetId, filename);
				if (text) {
					codeText.value = text;
				}
				programmingLanguage.value =
					code.files[filename].language ?? getProgrammingLanguage(codeName.value);
			} else {
				codeAsset.value = null;
				codeName.value = 'newcode.py';
				codeText.value = INITIAL_TEXT;
				programmingLanguage.value = ProgrammingLanguage.Python;
			}
		}
		codeAssetCopy.value = cloneDeep(codeAsset.value);
		// Remove dynamics of previous file then add the new ones
		isLoading.value = false;
		emit('asset-loaded');
	},
	{ immediate: true }
);
</script>

<style scoped>
header {
	padding: 0.5rem 1rem;
	background-color: var(--surface-section);
}

header > section {
	display: flex;
	justify-content: space-between;
	flex-direction: row;
}

main {
	height: 100%;
}

h4 {
	margin-top: 0.25rem;
}

.code-asset-content {
	display: flex;
	height: 100%;
}
.p-dropdown {
	height: 2.75rem;
}

.buttons {
	display: flex;
	align-items: center;
	gap: 0.5rem;
}

.ace-editor {
	border-top: 1px solid var(--surface-border-light);
}

.dynamic {
	position: absolute;
	background-color: var(--surface-highlight);
	z-index: 20;
}

.header {
	display: flex;
	gap: 1rem;
}

.name {
	flex-grow: 2;
	display: flex;
}

.name-input {
	height: 2.5rem;
	align-self: center;
	font-size: 20px;
	font-weight: var(--font-weight-semibold);
	width: 100%;
	border: 0;
}

.form-checkbox {
	display: flex;
	gap: 0.5rem;
}

:deep(.p-inputtext:enabled:hover) {
	background-color: var(--surface-hover);
}

:deep(.p-inputtext:enabled:hover:focus) {
	background-color: transparent;
}

:deep(header section) {
	gap: 0;
	max-width: 100%;
}
.code-blocks-container {
	padding: 2rem;
	max-width: 300px;
	height: 100%;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	border-left: solid var(--surface-500);
	overflow-y: auto;
}
.code-blocks-buttons-container {
	display: flex;
	flex-direction: column;
}

.code-blocks-buttons-container > * {
	margin-top: 1rem;
	margin-bottom: 1rem;
}

.code-asset-editor {
	width: 100%;
	display: flex;
	flex-direction: column;
}

.code-asset-editor-header {
	display: flex;
	align-items: center;
	background-color: var(--surface-200);
	justify-content: space-between;
	border-bottom: solid var(--surface-500);
}

:deep(.ace-active-line) {
	background-color: var(--surface-highlight);
	position: absolute;
}
</style>
