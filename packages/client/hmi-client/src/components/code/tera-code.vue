<template>
	<tera-asset>
		<template #name-input>
			<section class="header">
				<section class="name">
					<InputText
						v-model="codeName"
						class="name-input"
						@change="
							() => {
								programmingLanguage = getProgrammingLanguage(codeName);
								saveCode();
							}
						"
					/>
				</section>
				<section class="buttons">
					<Dropdown
						v-model="programmingLanguage"
						:options="programmingLanguages"
						@change="codeName = setFileExtension(codeName)"
					/>
					<FileUpload
						name="demo[]"
						:customUpload="true"
						@uploader="onFileOpen"
						mode="basic"
						auto
						chooseLabel="Load file"
						class="p-button-secondary"
					/>
					<Button label="Save" @click="saveCode" />
					<Button label="Save as new" @click="isCodeNamingModalVisible = true" />
					<Button label="Create model from code" @click="isModelNamingModalVisible = true" />
				</section>
			</section>
		</template>
		<v-ace-editor
			v-model:value="codeText"
			@init="initialize"
			:lang="programmingLanguage"
			theme="chrome"
			style="height: 100%; width: 100%"
			class="ace-editor"
		/>
		<Teleport to="body">
			<tera-modal
				v-if="isModelNamingModalVisible"
				class="modal"
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
					</form>
				</template>
				<template #footer>
					<Button
						label="Cancel"
						class="p-button-secondary"
						@click="isModelNamingModalVisible = false"
					/>
					<Button
						label="Create model"
						@click="
							() => {
								isModelNamingModalVisible = false;
								extractModel();
							}
						"
					/>
				</template>
			</tera-modal>
			<tera-modal
				v-if="isCodeNamingModalVisible"
				class="modal"
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
import { ref, watch } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import 'ace-builds/src-noconflict/mode-python';
import 'ace-builds/src-noconflict/mode-julia';
import 'ace-builds/src-noconflict/mode-r';
import Button from 'primevue/button';
import {
	uploadCodeToProject,
	getCodeFileAsText,
	getCodeAsset,
	updateCodeAsset,
	getFileExtension,
	getProgrammingLanguage
} from '@/services/code';
import { useToastService } from '@/services/toast';
import { codeToAMR } from '@/services/knowledge';
import { AssetType, Code, ProgrammingLanguage } from '@/types/Types';
import TeraModal from '@/components/widgets/tera-modal.vue';
import InputText from 'primevue/inputtext';
import router from '@/router';
import { RouteName } from '@/router/routes';
import useResourceStore from '@/stores/resources';
import FileUpload from 'primevue/fileupload';
import { IProject } from '@/types/Project';
import Textarea from 'primevue/textarea';
import TeraAsset from '@/components/asset/tera-asset.vue';
import Dropdown from 'primevue/dropdown';

const INITIAL_TEXT = '# Paste some code here';

const props = defineProps<{
	project: IProject;
	assetId: string;
}>();

const emit = defineEmits(['asset-loaded']);

const resourceStore = useResourceStore();
const toast = useToastService();

const codeName = ref('');
const codeText = ref(INITIAL_TEXT);
const codeAsset = ref<Code | null>(null);
const editor = ref<VAceEditorInstance['_editor'] | null>(null);
const selectedText = ref('');
const progress = ref(0);
const isModelDiagramModalVisible = ref(false);
const isModelNamingModalVisible = ref(false);
const isCodeNamingModalVisible = ref(false);
const newCodeName = ref('');
const newModelName = ref('');
const newModelDescription = ref('');
const programmingLanguage = ref<ProgrammingLanguage>(ProgrammingLanguage.Python);
const programmingLanguages = [
	ProgrammingLanguage.Julia,
	ProgrammingLanguage.Python,
	ProgrammingLanguage.R
];

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
}

function setFileExtension(fileName: string) {
	// check name for file extension
	// if there is no extension, add the appropriate one based on the selected language
	const name = fileName.split('.');
	if (name.length > 0) {
		return name[0].concat('.').concat(getFileExtension(programmingLanguage.value));
	}
	return fileName;
}

async function saveCode() {
	// programmingLanguage.value = getProgrammingLanguage(codeName.value);
	const existingCode = resourceStore.activeProjectAssets?.code.find((c) => c.id === props.assetId);
	if (existingCode?.id) {
		codeName.value = setFileExtension(codeName.value);
		const file = new File([codeText.value], codeName.value);
		const updatedCode = await updateCodeAsset(
			{ ...existingCode, name: codeName.value },
			file,
			progress
		);
		if (!updatedCode) {
			toast.error('', 'Unable to save file');
		} else {
			toast.success('', `File saved as ${codeName.value}`);
			codeAsset.value = updatedCode;
		}
		return updatedCode;
	}
	newCodeName.value = codeName.value;
	return saveNewCode();
}

async function saveNewCode() {
	newCodeName.value = setFileExtension(newCodeName.value);
	const file = new File([codeText.value], newCodeName.value);
	const newCodeAsset = await uploadCodeToProject(props.project.id, file, progress);
	if (!newCodeAsset) {
		toast.error('', 'Unable to save file');
	} else {
		toast.success('', `File saved as ${newCodeName.value}`);
		codeAsset.value = newCodeAsset;
		router.push({
			name: RouteName.Project,
			params: {
				pageType: AssetType.Code,
				projectId: props.project.id,
				assetId: codeAsset.value.id
			}
		});
	}
	return newCodeAsset;
}

async function extractModel() {
	const newCodeAsset = await saveCode();
	if (newCodeAsset && newCodeAsset.id) {
		const extractedModelId = await codeToAMR(
			newCodeAsset.id,
			newModelName.value,
			newModelDescription.value
		);
		if (extractedModelId) {
			router.push({
				name: RouteName.Project,
				params: {
					pageType: AssetType.Models,
					projectId: props.project.id,
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
		codeText.value = evt?.target?.result?.toString() ?? codeText.value;
		codeName.value = file.name;
	};
}

watch(
	() => props.assetId,
	async () => {
		if (props.assetId !== 'code') {
			// FIXME: assetId is 'code' for a newly opened code asset; a hack to get around some weird tab behaviour
			const code = await getCodeAsset(props.assetId);
			if (code) {
				codeAsset.value = code;
				codeName.value = code.name;
				const text = await getCodeFileAsText(props.assetId, code.name);
				if (text) {
					codeText.value = text;
				}
				programmingLanguage.value = getProgrammingLanguage(codeName.value);
			} else {
				codeAsset.value = null;
				codeName.value = 'newcode.py';
				codeText.value = INITIAL_TEXT;
				programmingLanguage.value = ProgrammingLanguage.Python;
			}
		} else {
			codeAsset.value = null;
			codeName.value = 'newcode.py';
			codeText.value = INITIAL_TEXT;
			programmingLanguage.value = ProgrammingLanguage.Python;
		}
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

.buttons {
	display: flex;
	gap: 0.5rem;
}

.buttons > * {
	height: 2.5rem;
}

.ace-editor {
	border-top: 1px solid var(--surface-border-light);
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
</style>
