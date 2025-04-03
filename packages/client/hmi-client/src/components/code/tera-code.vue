<template>
	<tera-asset :id="assetId" :is-loading="isLoading" overflow-hidden :show-header="false">
		<div v-if="programmingLanguage !== ProgrammingLanguage.Zip" class="code-asset-content">
			<tera-directory v-if="fileNames.length > 1" :files="fileNames" @fileClicked="onFileSelect" />
			<div class="code-asset-editor">
				<header class="code-asset-editor-header">
					<div class="left-side w-full flex align-items-center gap-2">
						<InputText
							v-if="isRenamingCode"
							v-model="codeName"
							class="name-input w-10"
							@change="() => saveCode"
							@keyup.enter="() => saveCode"
							@keyup.esc="() => saveCode"
						/>
						<h4 v-else>{{ codeSelectedFile }}</h4>
						<Button
							v-if="!isRenamingCode"
							icon="pi pi-ellipsis-v"
							class="p-button-icon-only p-button-text p-button-rounded"
							@click="toggleOptionsMenu"
						/>
					</div>
					<ContextMenu ref="optionsMenu" :model="optionsMenuItems" :popup="true" :pt="optionsMenuPt" />
					<div class="right-side flex gap-2">
						<Button class="toolbar-button" severity="secondary" outlined label="Save" @click="saveCode()" />
						<Button
							class="toolbar-button white-space-nowrap"
							severity="secondary"
							outlined
							label="Save as"
							@click="showSaveAssetModal = true"
						/>
						<Dropdown
							class="toolbar-button"
							v-model="programmingLanguage"
							:options="programmingLanguages"
							@change="onFileTypeChange"
						/>
					</div>
				</header>
				<v-ace-editor
					v-model:value="codeText"
					@init="initialize"
					:lang="programmingLanguage"
					theme="chrome"
					style="height: 100%; width: 100%"
					class="ace-editor"
					:options="{ showPrintMargin: false }"
				/>
			</div>
			<div class="code-blocks-container">
				<div>
					<h6 class="mb-2">Code blocks</h6>
					<p>Select the code blocks that represent the core dynamics of your model.</p>
					<Button
						:disabled="selectionRange === null"
						text
						size="small"
						icon="pi pi-plus"
						label="Add a code block"
						@click="isDynamicsModalVisible = true"
						class="mb-2"
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
						class="w-7 mb-0"
					/>
					<Button
						:loading="savingAsset"
						:disabled="isEqual(codeAsset, codeAssetCopy)"
						label="Apply changes"
						size="large"
						class="w-full white-space-nowrap mb-0"
						@click="onSaveChanges"
					/>
				</div>
			</div>
		</div>
		<div v-else>
			<!-- TODO: show entire file tree for github -->
			<a v-if="repoUrl" :href="repoUrl" target="_blank" rel="noreferrer noopener">{{ repoUrl }}</a>
		</div>
		<tera-modal
			v-if="isDynamicsModalVisible"
			@modal-mask-clicked="isDynamicsModalVisible = false"
			@modal-enter-press="isDynamicsModalVisible = false"
		>
			<template #header>
				<h4>Save this code block</h4>
				<p>
					Enter a name for the code block you are saving. Choose a name that reflects its purpose or functionality
					within the model.
				</p>
			</template>
			<template #default>
				<form @submit.prevent>
					<label class="text-sm mb-1" for="model-name">Name</label>
					<tera-input-text id="model-name" v-model="newDynamicsName" />
					<label class="text-sm mb-1" for="model-description">Description (optional)</label>
					<Textarea v-model="newDynamicsDescription" />
				</form>
			</template>
			<template #footer>
				<Button
					label="Save"
					size="large"
					@click="
						() => {
							isDynamicsModalVisible = false;
							addDynamic();
						}
					"
				/>
				<Button label="Cancel" severity="secondary" size="large" outlined @click="isDynamicsModalVisible = false" />
			</template>
		</tera-modal>
		<tera-save-asset-modal
			v-if="codeText"
			:is-visible="showSaveAssetModal"
			:asset="codeText"
			:assetType="AssetType.Code"
			:initial-name="codeName"
			@close-modal="showSaveAssetModal = false"
		/>
	</tera-asset>
</template>

<script setup lang="ts">
import '@/ace-config';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { useProjects } from '@/composables/project';
import TeraSaveAssetModal from '@/components/project/tera-save-asset-modal.vue';
import {
	getCodeAsset,
	getCodeFileAsText,
	getProgrammingLanguage,
	setFileExtension,
	updateCodeAsset
} from '@/services/code';
import * as saveAssetService from '@/services/save-asset';
import { useToastService } from '@/services/toast';
import type { Code, CodeFile } from '@/types/Types';
import { AssetType, ProgrammingLanguage } from '@/types/Types';
import { extractDynamicRows } from '@/utils/code-asset';
import { logger } from '@/utils/logger';
import { Ace, Range } from 'ace-builds';
import { cloneDeep, isEmpty, isEqual } from 'lodash';
import Button from 'primevue/button';
import ContextMenu from 'primevue/contextmenu';
import Dropdown from 'primevue/dropdown';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import { computed, ref, watch, onMounted } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import type { VAceEditorInstance } from 'vue3-ace-editor/types';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import TeraCodeDynamic from './tera-code-dynamic.vue';
import TeraDirectory from './tera-directory.vue';

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
const showSaveAssetModal = ref(false);
const isDynamicsModalVisible = ref(false);
const newDynamicsName = ref('');
const newDynamicsDescription = ref('');
const programmingLanguage = ref<ProgrammingLanguage>(ProgrammingLanguage.Python);
const programmingLanguages = [ProgrammingLanguage.Julia, ProgrammingLanguage.Python, ProgrammingLanguage.R];
const isLoading = ref(false);

const repoUrl = computed(() => codeAsset.value?.repoUrl ?? '');

const selectedRangeToString = computed(() =>
	selectionRange.value ? `L${selectionRange.value.start.row + 1}-L${selectionRange.value.end.row + 1}` : ''
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
	selectionRange.value = !isEmpty(selectedText.value) && editor.value ? editor.value.getSelectionRange() : null;
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
							editor.value?.session.addMarker(new Range(startRow, 0, endRow, 0), 'ace-active-line', 'fullLine');
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
			codeAssetCopy.value.files[codeSelectedFile.value].dynamics.block.push(selectedRangeToString.value);
		} else {
			codeAssetCopy.value.files[codeSelectedFile.value] = {
				fileName: codeSelectedFile.value,
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
		isRenamingCode.value = false;
	} else {
		saveAssetService.saveAs(
			new File([codeText.value], setFileExtension(codeName.value, programmingLanguage.value)),
			AssetType.Code
		);
	}
}

async function refreshCodeAsset(codeId: string) {
	const code = await getCodeAsset(codeId);
	if (code) {
		codeAsset.value = code;
		codeAssetCopy.value = cloneDeep(codeAsset.value);
	}
}

// This was causing issues when trying to commit
// probably because I removed the Open File button
//
// async function onFileOpen(event) {
// 	const file = event.files[0];
// 	const reader = new FileReader();
// 	reader.readAsText(file, 'UTF-8');
// 	reader.onload = (evt) => {
// 		removeMarkers();

// 		if (codeAssetCopy.value) {
// 			codeAssetCopy.value.files = { ...codeAssetCopy.value.files, [file.name]: {} };
// 		}
// 		codeText.value = evt?.target?.result?.toString() ?? codeText.value;
// 		codeSelectedFile.value = file.name;
// 	};
// }

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
		isLoading.value = true;
		const code = await getCodeAsset(props.assetId);
		if (code?.files && Object.keys(code.files)[0]) {
			codeAsset.value = code;
			codeName.value = code.name ?? '';

			const filename = Object.keys(code.files)[0];
			codeSelectedFile.value = filename;

			codeText.value = (await getCodeFileAsText(props.assetId, filename)) ?? INITIAL_TEXT;

			programmingLanguage.value = code.files[filename].language ?? getProgrammingLanguage(codeName.value);
		} else {
			codeAsset.value = null;
			codeName.value = 'newcode.py';
			codeText.value = INITIAL_TEXT;
			programmingLanguage.value = ProgrammingLanguage.Python;
		}
		codeAssetCopy.value = cloneDeep(codeAsset.value);
		// Remove dynamics of previous file then add the new ones
		isLoading.value = false;
		emit('asset-loaded');
	},
	{ immediate: true }
);

const isRenamingCode = ref(false);

const optionsMenu = ref();
const optionsMenuItems = ref<any[]>([
	{
		icon: 'pi pi-pencil',
		label: 'Rename',
		command() {
			isRenamingCode.value = true;
		}
	}
]);
const optionsMenuPt = {
	submenu: {
		class: 'max-h-30rem overflow-y-scroll'
	}
};

const toggleOptionsMenu = (event) => {
	optionsMenu.value.toggle(event);
};

onMounted(async () => {
	const addProjectMenuItems = (await useProjects().getAllExceptActive()).map((project) => ({
		label: project.name,
		command: async () => {
			const response = await useProjects().addAsset(AssetType.Code, props.assetId, project.id);
			if (response) logger.info(`Added asset to ${project.name}`);
			else logger.error('Failed to add asset to project');
		}
	}));
	if (addProjectMenuItems.length === 0) return;
	optionsMenuItems.value.splice(1, 0, {
		icon: 'pi pi-plus',
		label: 'Add to project',
		items: addProjectMenuItems
	});
});
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

.code-asset-content {
	display: flex;
}

.p-dropdown {
	height: 2.75rem;
}

.buttons {
	display: flex;
	align-items: center;
	gap: 0.5rem;
}

.dynamic {
	position: absolute;
	background-color: var(--surface-highlight);
	z-index: 20;
}

.name {
	flex-grow: 2;
	display: flex;
}

.name-input {
	height: 2.25rem;
	align-self: center;
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
	padding: var(--gap-4);
	max-width: 300px;
	height: 100%;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	border-left: solid 1px var(--surface-border);
	overflow-y: auto;
}

.code-blocks-buttons-container {
	display: flex;
	flex-direction: row;
	gap: var(--gap-2);
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
	background-color: var(--surface-100);
	justify-content: space-between;
	border-bottom: solid 1px var(--surface-border);
}

:deep(.ace-active-line) {
	background-color: var(--surface-highlight);
	position: absolute;
}

.toolbar-button {
	height: 2.25rem;
}
</style>
