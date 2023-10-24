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
						@change="codeName = setFileExtension(codeName, programmingLanguage)"
					/>
					<FileUpload
						name="demo[]"
						:customUpload="true"
						@uploader="onFileOpen"
						mode="basic"
						auto
						chooseLabel="Load file"
					/>
					<Button label="Save" @click="saveCode()" />
					<Button label="Save as new" @click="isCodeNamingModalVisible = true" />
					<Button
						label="Create model from code"
						@click="isModelNamingModalVisible = true"
						:loading="isCodeToModelLoading"
					/>
					<Button
						label="Add dynamics"
						:disabled="selectionRange === null"
						@click="isDynamicsModalVisible = true"
					/>
					<Button
						label="Remove all dynamics"
						:disabled="!codeAsset?.files"
						@click="removeAllDynamics"
					/>
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
				class="modal"
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
import { ref, watch, computed } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import 'ace-builds/src-noconflict/mode-python';
import 'ace-builds/src-noconflict/mode-julia';
import 'ace-builds/src-noconflict/mode-r';
import Button from 'primevue/button';
import Checkbox from 'primevue/checkbox';
import {
	getCodeFileAsText,
	getCodeAsset,
	updateCodeAsset,
	uploadCodeToProject,
	setFileExtension,
	getProgrammingLanguage
} from '@/services/code';
import { useToastService } from '@/services/toast';
import { codeToAMR } from '@/services/knowledge';
import { AssetType, Code, ProgrammingLanguage } from '@/types/Types';
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
import { isEmpty } from 'lodash';

const INITIAL_TEXT = '# Paste some code here';

const props = defineProps<{
	assetId: string;
}>();

const emit = defineEmits(['asset-loaded']);

const toast = useToastService();

const existingMarkers = new Set();

const codeName = ref('');
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

const selectedRangeToString = computed(() =>
	selectionRange.value
		? `L${selectionRange.value.start.row + 1}-L${selectionRange.value.end.row + 1}`
		: ''
);

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
	if (codeAsset.value?.files) {
		const { name, files } = codeAsset.value;

		Object.keys(files).forEach((fileName) => {
			if (fileName === name) {
				const { block } = files[fileName].dynamics;
				// Loop through every highlighted block
				for (let i = 0; i < block.length; i++) {
					// Avoids rehighlighting
					if (!existingMarkers.has(block[i])) {
						// Extract start and end rows and highlight them in the editor
						const match = block[i].match(/L(\d+)-L(\d+)/) || [];
						const startRow = parseInt(match[1], 10) - 1;
						const endRow = parseInt(match[2], 10) - 1;
						editor.value?.session.addMarker(
							new Range(startRow, 0, endRow, 0),
							'ace_active-line',
							'fullLine'
						);
						existingMarkers.add(block[i]);
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

async function removeAllDynamics() {
	const codeAssetClone = codeAsset.value;
	delete codeAssetClone?.files;
	saveCode(codeAssetClone);
}

async function addDynamic() {
	const codeAssetClone = codeAsset.value;
	// Add highlighted block to dynamics
	if (selectedRangeToString.value && codeAssetClone) {
		if (!codeAssetClone.files) codeAssetClone.files = {};

		// For now we are supporting one file per asset
		if (codeAssetClone.files[codeName.value]) {
			codeAssetClone.files[codeName.value].dynamics.block.push(selectedRangeToString.value);
		} else {
			codeAssetClone.files[codeName.value] = {
				language: getProgrammingLanguage(codeName.value),
				dynamics: {
					name: newDynamicsName.value,
					description: newDynamicsDescription.value,
					block: [selectedRangeToString.value]
				}
			};
		}
	}
	saveCode(codeAssetClone);
}

async function saveCode(codeAssetToSave: Code | null = codeAsset.value) {
	if (codeAssetToSave?.id) {
		codeName.value = setFileExtension(codeName.value, programmingLanguage.value);
		const code = { ...codeAssetToSave, name: codeName.value };
		const file = new File([codeText.value], codeName.value);

		const res = await updateCodeAsset(code, file, progress); // This returns an object with an id not the whole code asset...
		if (!res?.id) {
			toast.error('', 'Unable to save file');
		} else {
			toast.success('', `File saved as ${codeName.value}`);
			codeAsset.value = await getCodeAsset(res.id);
			highlightDynamics();
		}
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
	if (newAsset) {
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
	toast.error('', 'Unable to save file');
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
				AssetType.Models,
				extractedModelId,
				useProjects().activeProject.value?.id
			);
			router.push({
				name: RouteName.Project,
				params: {
					pageType: AssetType.Models,
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
		if (props.assetId === AssetType.Code) {
			// FIXME: assetId is 'code' for a newly opened code asset; a hack to get around some weird tab behaviour
			codeAsset.value = null;
			codeName.value = 'newcode.py';
			codeText.value = INITIAL_TEXT;
			programmingLanguage.value = ProgrammingLanguage.Python;
		} else {
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
		}
		// Remove dynamics of previous file then add the new ones
		removeMarkers();
		highlightDynamics();
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
</style>
