<template>
	<main>
		<tera-modal v-if="visible" @modal-mask-clicked="emit('close')">
			<template #header>
				<h2>
					https://github.com/{{ repoOwnerAndName }}<template v-if="isInDirectory">/{{ currentDirectory }}</template>
				</h2>
				<b>({{ directoryContent?.totalFiles }}) files found in: </b>
				<div class="flex justify-content-left">
					<Breadcrumb :home="home" :model="directories" style="color: black" />
				</div>
			</template>
			<template #default>
				<div style="display: flex; flex-direction: row">
					<div style="max-width: 90%; width: 30%">
						<ul>
							<li v-if="isInDirectory" @click="openDirectory('')">
								<i class="pi pi-folder-open" />
								<b> ..</b>
							</li>
							<li v-if="hasDirectories">
								<header>
									<b>Directories</b>
									<span class="artifact-amount">({{ directoryContent?.files.Directory?.length }})</span>
								</header>
							</li>
							<li
								v-for="(content, index) in directoryContent?.files.Directory"
								:key="index"
								@click="openDirectory(content.path)"
							>
								<i class="pi pi-folder" />
								{{ content.name }}
							</li>
							<li v-if="hasCode">
								<header>
									<b>Code</b>
									<span class="artifact-amount">({{ directoryContent?.files.Code?.length }})</span>
								</header>
							</li>
							<li
								v-for="(content, index) in directoryContent?.files.Code"
								:key="index"
								@click="previewTextFile(content)"
							>
								<Checkbox class="file-checkboxes" v-model="selectedFiles" :inputId="content.name" :value="content" />
								<i class="pi pi-file file-checkboxes" />
								<label :for="content.name">{{ content.name }}</label>
							</li>
							<li v-if="hasData">
								<header>
									<b>Data</b>
									<span class="artifact-amount">({{ directoryContent?.files.Data?.length }})</span>
								</header>
							</li>
							<li
								v-for="(content, index) in directoryContent?.files.Data"
								:key="index"
								@click="previewTextFile(content)"
							>
								<Checkbox class="file-checkboxes" v-model="selectedFiles" :inputId="content.name" :value="content" />
								<i class="pi pi-file file-checkboxes" />
								<label :for="content.name">{{ content.name }}</label>
							</li>
							<li v-if="hasDocuments">
								<header>
									<b>Documents</b>
									<span class="artifact-amount">({{ directoryContent?.files.Documents?.length }})</span>
								</header>
							</li>
							<li
								v-for="(content, index) in directoryContent?.files.Documents"
								:key="index"
								@click="previewTextFile(content)"
							>
								<Checkbox class="file-checkboxes" v-model="selectedFiles" :inputId="content.name" :value="content" />
								<i class="pi pi-file file-checkboxes" />
								<label :for="content.name">{{ content.name }}</label>
							</li>
							<li v-if="hasOther">
								<header>
									<b>Unknown File Types</b>
									<span class="artifact-amount">({{ directoryContent?.files.Other?.length }})</span>
								</header>
							</li>
							<li
								class="t"
								v-for="(content, index) in directoryContent?.files.Other"
								:key="index"
								@click="previewTextFile(content)"
							>
								<div class="unknown-check">
									<Checkbox v-model="selectedUnknownFiles" :inputId="content.name" :value="content" />
									<i class="pi pi-file unknown-icon" />
									{{ content.name }}
								</div>
								<div>
									<Dropdown
										v-model="content.fileCategory"
										:options="asDocumentTypes"
										optionLabel="name"
										placeholder="Select a document type"
										style="width: 90%; margin: 2px"
									/>
								</div>
							</li>
						</ul>
					</div>
					<section class="preview-container" style="width: 70%">
						<div class="code-editor-container">
							<h4 class="preview-title">Preview</h4>
							<v-ace-editor
								v-model:value="displayCode"
								@init="initialize"
								lang="python"
								theme="chrome"
								class="code-editor"
							/>
						</div>
					</section>
				</div>
			</template>
			<template #footer>
				<Button v-if="useProjects().activeProject.value" @click="addRepoToCodeAsset()">Import repo to project</Button>
				<Dropdown
					v-else
					placeholder="Import repo to project"
					class="p-button dropdown-button"
					:is-dropdown-left-aligned="false"
					:options="projectOptions"
					option-label="name"
					@change="addRepoToCodeAsset"
				/>
				<Button
					v-if="useProjects().activeProject.value"
					:disabled="selectedFiles.length + selectedUnknownFiles.length < 1"
					@click="openSelectedFiles()"
					>Import {{ selectedFiles.length + selectedUnknownFiles.length }} file{{
						selectedFiles.length == 1 ? '' : 's'
					}}</Button
				>
				<Dropdown
					v-else
					:disabled="selectedFiles.length + selectedUnknownFiles.length < 1"
					placeholder="Import files to project"
					class="p-button dropdown-button"
					:is-dropdown-left-aligned="false"
					:options="projectOptions"
					option-label="name"
					@change="openSelectedFiles"
				/>
				<Button class="p-button-outlined" label="Cancel" @click="emit('close')" />
			</template>
		</tera-modal>
	</main>
</template>

<script setup lang="ts">
import { computed, ComputedRef, ref, Ref, watch } from 'vue';
import Button from 'primevue/button';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { isEmpty } from 'lodash';
import { getGithubCode, getGithubRepositoryContent } from '@/services/github-import';
import type { DocumentAsset, GithubFile, GithubRepo } from '@/types/Types';
import { AssetType, FileCategory } from '@/types/Types';
import { VAceEditor } from 'vue3-ace-editor';
import type { VAceEditorInstance } from 'vue3-ace-editor/types';
import { getModeForPath } from 'ace-builds/src-noconflict/ext-modelist';
import '@/ace-config';
import Checkbox from 'primevue/checkbox';
import Dropdown, { DropdownChangeEvent } from 'primevue/dropdown';
import Breadcrumb from 'primevue/breadcrumb';
import { extractPDF } from '@/services/knowledge';
import useAuthStore from '@/stores/auth';
import { useProjects } from '@/composables/project';
import { uploadCodeFromGithubRepo, uploadCodeToProjectFromGithub } from '@/services/code';
import { createNewDocumentFromGithubFile } from '@/services/document-assets';
import { createNewDatasetFromGithubFile } from '@/services/dataset';
import { useToastService } from '@/services/toast';

const props = defineProps<{
	visible: boolean;
	urlString: string;
}>();

const emit = defineEmits(['close']);

const repoOwnerAndName: Ref<string> = ref('');
const currentDirectory: Ref<string> = ref('');
const directoryContent: Ref<GithubRepo | null> = ref(null);
const selectedFiles: Ref<GithubFile[]> = ref([]);
const selectedUnknownFiles: Ref<GithubFile[]> = ref([]);
const editor: Ref<VAceEditorInstance['_editor'] | null> = ref(null);
const selectedText: Ref<string> = ref('');
const displayCode: Ref<string> = ref('');
const auth = useAuthStore();
// Breadcrumb home setup
const home = ref({
	icon: 'pi pi-home',
	disabled: true
});

const asDocumentTypes = ref([
	{ name: 'Import as document', code: FileCategory.Documents },
	{ name: 'Import as code', code: FileCategory.Code },
	{ name: 'Import as data', code: FileCategory.Data }
]);

const directories = computed(() => currentDirectory.value.split('/').map((d) => ({ label: d, disabled: true })));

const isInDirectory: ComputedRef<boolean> = computed(() => !isEmpty(currentDirectory.value));
// There may be a more concise way to do this?
const hasDirectories: ComputedRef<boolean> = computed(() => !isEmpty(directoryContent?.value?.files?.Directory));
const hasCode: ComputedRef<boolean> = computed(() => !isEmpty(directoryContent.value?.files?.Code));
const hasData: ComputedRef<boolean> = computed(() => !isEmpty(directoryContent.value?.files?.Data));
const hasDocuments: ComputedRef<boolean> = computed(() => !isEmpty(directoryContent?.value?.files?.Documents));
const hasOther: ComputedRef<boolean> = computed(() => !isEmpty(directoryContent?.value?.files?.Other));

const projectOptions = computed(() => useProjects().allProjects.value?.map((p) => ({ name: p.name, id: p.id })));

async function initializeCodeBrowser() {
	repoOwnerAndName.value = new URL(props.urlString).pathname.substring(1); // owner/repo
	await openDirectory(''); // Goes back to root directory if modal is closed then opened again
}

/**
 * Editor initialization function
 * @param editorInstance	the Ace editor instance
 */
async function initialize(editorInstance) {
	editor.value = editorInstance;
	editorInstance.session.selection.on('changeSelection', onSelectedTextChange);
	editorInstance.setShowPrintMargin(false);
	editorInstance.value = '';
}

/**
 * Event handler for selected text change in the code editor
 */
function onSelectedTextChange() {
	selectedText.value = editor.value?.getSelectedText() ?? '';
}

/**
 * Opens the selected files in the code editor
 */
async function openSelectedFiles(event?: DropdownChangeEvent) {
	const projectId = event?.value?.id;
	// split the selected files into their respective categories
	const selectedCodeFiles: GithubFile[] = [...selectedFiles.value, ...selectedUnknownFiles.value].filter(
		(file) => file.fileCategory === FileCategory.Code
	);

	// Import code files, if any were selected
	if (selectedCodeFiles.length > 0) {
		await openCodeFiles(selectedCodeFiles, projectId);
	}
	const selectedDataFiles: GithubFile[] = [...selectedFiles.value, ...selectedUnknownFiles.value].filter(
		(file) => file.fileCategory === FileCategory.Data
	);

	if (selectedDataFiles.length > 0) {
		await importDataFiles(selectedDataFiles);
	}

	const selectedDocumentFiles: GithubFile[] = selectedFiles.value.filter(
		(file) => file.fileCategory === FileCategory.Documents
	);

	if (selectedDocumentFiles.length > 0) {
		await importDocumentFiles(selectedDocumentFiles, projectId);
	}

	// TODO: make this number account for files that were not succussfully imported
	const numUploadedFiles = selectedCodeFiles.length + selectedDataFiles.length + selectedDocumentFiles.length;
	const resourceMsg = numUploadedFiles > 1 ? 'resources were' : 'resource was';
	useToastService().success('Success!', `${numUploadedFiles} ${resourceMsg} successfuly added to this project`);
	// FIXME: Files aren't opening
	emit('close');
}

/**
 * Refreshes the directory content and sets the current directory
 * @param directory The directory to set and refresh from
 */
async function openDirectory(directory: string) {
	currentDirectory.value = directory;
	directoryContent.value = await getGithubRepositoryContent(repoOwnerAndName.value, currentDirectory.value);
}

/**
 * Previews the selected code file by fetching its contents from github
 * @param file The file to preview
 */
async function previewTextFile(file: GithubFile) {
	if (file.path.toLowerCase().endsWith('.pdf')) {
		displayCode.value = 'PDF Preview not supported at this time';
		return;
	}
	displayCode.value = await getGithubCode(repoOwnerAndName.value, file.path);
	const mode = getModeForPath(file.path);
	editor.value?.session.setMode(mode);
}

/**
 * async function to import the selected data files and create new datasets from them
 * @param githubFiles the data files to open
 */
async function importDataFiles(githubFiles: GithubFile[]) {
	// iterate through our files and fetch their contents
	githubFiles.forEach(async (githubFile) => {
		// Create a new dataset from this GitHub file
		const newDataset = await createNewDatasetFromGithubFile(
			repoOwnerAndName.value,
			githubFile.path,
			auth.user?.id ?? '',
			githubFile.htmlUrl
		);
		if (newDataset && newDataset.id) {
			await useProjects().addAsset(AssetType.Dataset, newDataset.id);
		}
	});
}

async function importDocumentFiles(githubFiles: GithubFile[], projectId?: string) {
	githubFiles.forEach(async (githubFile) => {
		const document: DocumentAsset | null = await createNewDocumentFromGithubFile(
			repoOwnerAndName.value,
			githubFile.path,
			useAuthStore().user?.id ?? ''
		);
		let newAsset;
		if (document && document.id) {
			newAsset = await useProjects().addAsset(AssetType.Document, document.id, projectId);
		}
		if (document?.id && newAsset && githubFile.name?.toLowerCase().endsWith('.pdf')) {
			extractPDF(document.id);
		}
	});
}

/**
 * Opens the code editor with the selected file
 * @param githubFiles The code files to open
 */
async function openCodeFiles(githubFiles: GithubFile[], projectId?: string) {
	githubFiles.forEach(async (githubFile) => {
		const newCode = await uploadCodeToProjectFromGithub(repoOwnerAndName.value, githubFile.path, githubFile.htmlUrl);
		if (newCode && newCode.id) {
			await useProjects().addAsset(AssetType.Code, newCode.id, projectId);
		}
	});
}

async function addRepoToCodeAsset(event?: DropdownChangeEvent) {
	const projectId = event?.value?.id;

	const newCodeAsset = await uploadCodeFromGithubRepo(repoOwnerAndName.value, props.urlString);

	if (newCodeAsset && newCodeAsset.id) {
		const assetId = await useProjects().addAsset(AssetType.Code, newCodeAsset.id, projectId);

		if (assetId) {
			useToastService().success('Success!', 'The Github repository was successfuly added to this project');
		}
	}
}

watch(
	() => props.visible,
	() => {
		if (props.visible) {
			initializeCodeBrowser();
		}
	}
);
</script>

<style scoped>
main {
	display: flex;
	align-items: center;
	gap: 0.5rem;
}

ul {
	list-style: none;
	display: flex;
	flex-direction: column;
	gap: 0.25rem;
	height: 50vh;
	overflow-y: auto;
}

ul li {
	padding: 0.25rem;
	cursor: pointer;
	border-radius: 0.5rem;
	max-width: 90%;
}

ul li:hover {
	background-color: var(--surface-hover);
}

.preview-container {
	width: 70%;
	margin: 10px;
}

.preview-title {
	padding-bottom: 10px;
}

.code-editor-container {
	background-color: rgb(231, 231, 231);
	border-radius: 5px;
	padding: 15px;
	height: 90%;
}

.code-editor {
	border-top: 1px solid var(--surface-border-light);
	width: 100%;
	height: 95%;
	border-radius: 5px;
}

.file-checkboxes {
	margin-left: 10px;
}

.t {
	display: flex;
	flex-direction: column;
	margin-left: 10px;
}

.unknown-check {
	padding-bottom: 8px;
}

.unknown-icon {
	padding-left: 10px;
}

.dropdown-button {
	height: 3rem;
	border-radius: var(--border-radius);
	gap: 16px;
}

.dropdown-button:hover {
	background-color: var(--primary-color-dark);
}

:deep(.dropdown-button.p-dropdown .p-dropdown-label.p-placeholder) {
	display: contents;
	color: white;
	font-size: small;
}
:deep .dropdown-button.p-dropdown .p-dropdown-trigger {
	color: white;
}
</style>
