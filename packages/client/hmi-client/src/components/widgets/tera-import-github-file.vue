<template>
	<main>
		<Button
			v-if="showImportButton"
			label="Import"
			class="p-button-sm p-button-outlined"
			icon="pi pi-cloud-download"
			@click="initializeCodeBrowser"
		/>
		<a :href="urlString" rel="noreferrer noopener">{{ urlString }}</a>
		<Teleport to="body">
			<tera-modal v-if="isModalVisible" class="modal" @modal-mask-clicked="!isModalVisible">
				<template #header>
					<h2>
						https://github.com/{{ repoOwnerAndName
						}}<template v-if="isInDirectory">/{{ currentDirectory }}</template>
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
										<span class="artifact-amount"
											>({{ directoryContent?.files.Directory?.length }})</span
										>
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
										<span class="artifact-amount"
											>({{ directoryContent?.files.Code?.length }})</span
										>
									</header>
								</li>
								<li
									v-for="(content, index) in directoryContent?.files.Code"
									:key="index"
									@click="previewTextFile(content)"
								>
									<Checkbox
										class="file-checkboxes"
										v-model="selectedFiles"
										:inputId="content.name"
										:value="content"
									/>
									<i class="pi pi-file file-checkboxes" />
									<label :for="content.name">{{ content.name }}</label>
								</li>
								<li v-if="hasData">
									<header>
										<b>Data</b>
										<span class="artifact-amount"
											>({{ directoryContent?.files.Data?.length }})</span
										>
									</header>
								</li>
								<li
									v-for="(content, index) in directoryContent?.files.Data"
									:key="index"
									@click="previewTextFile(content)"
								>
									<Checkbox
										class="file-checkboxes"
										v-model="selectedFiles"
										:inputId="content.name"
										:value="content"
									/>
									<i class="pi pi-file file-checkboxes" />
									<label :for="content.name">{{ content.name }}</label>
								</li>
								<li v-if="hasDocuments">
									<header>
										<b>Documents</b>
										<span class="artifact-amount"
											>({{ directoryContent?.files.Documents?.length }})</span
										>
									</header>
								</li>
								<li
									v-for="(content, index) in directoryContent?.files.Documents"
									:key="index"
									@click="previewTextFile(content)"
								>
									<Checkbox
										class="file-checkboxes"
										v-model="selectedFiles"
										:inputId="content.name"
										:value="content"
									/>
									<i class="pi pi-file file-checkboxes" />
									<label :for="content.name">{{ content.name }}</label>
								</li>
								<li v-if="hasOther">
									<header>
										<b>Unknown File Types</b>
										<span class="artifact-amount"
											>({{ directoryContent?.files.Other?.length }})</span
										>
									</header>
								</li>
								<li
									class="t"
									v-for="(content, index) in directoryContent?.files.Other"
									:key="index"
									@click="previewTextFile(content)"
								>
									<div class="unknown-check">
										<Checkbox
											v-model="selectedUnknownFiles"
											:inputId="content.name"
											:value="content"
										/>
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
							<div class="provenance-container">
								<h4 class="provenance-title">Provenance</h4>
								<div>{{ provenanceDescription }}</div>
							</div>
						</section>
					</div>
				</template>
				<template #footer>
					<Button
						:disabled="selectedFiles.length + selectedUnknownFiles.length < 1"
						@click="openSelectedFiles()"
						>Import {{ selectedFiles.length + selectedUnknownFiles.length }} file{{
							selectedFiles.length == 1 ? '' : 's'
						}}</Button
					>
					<Button class="p-button-outlined" label="Cancel" @click="isModalVisible = false" />
				</template>
			</tera-modal>
		</Teleport>
	</main>
</template>

<script setup lang="ts">
import { computed, ComputedRef, ref, Ref } from 'vue';
import Button from 'primevue/button';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { ProjectAssetTypes } from '@/types/Project';
import { isEmpty } from 'lodash';
import { getGithubCode, getGithubRepositoryContent } from '@/services/github-import';
import { FileCategory, GithubFile, GithubRepo } from '@/types/Types';
import { CodeRequest, Tab } from '@/types/common';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import { getModeForPath } from 'ace-builds/src-noconflict/ext-modelist';
import Checkbox from 'primevue/checkbox';
import Dropdown from 'primevue/dropdown';
import Breadcrumb from 'primevue/breadcrumb';

const props = defineProps<{
	urlString: string;
	showImportButton: boolean;
}>();

const emit = defineEmits(['open-code']);

const repoOwnerAndName: Ref<string> = ref('');
const currentDirectory: Ref<string> = ref('');
const directoryContent: Ref<GithubRepo | null> = ref(null);
const isModalVisible: Ref<boolean> = ref(false);
const selectedFiles: Ref<GithubFile[]> = ref([]);
const selectedUnknownFiles: Ref<GithubFile[]> = ref([]);
const editor: Ref<VAceEditorInstance['_editor'] | null> = ref(null);
const selectedText: Ref<string> = ref('');
const displayCode: Ref<string> = ref('');

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

const directories = computed(() =>
	currentDirectory.value.split('/').map((d) => ({ label: d, disabled: true }))
);

const isInDirectory: ComputedRef<boolean> = computed(() => !isEmpty(currentDirectory.value));
// There may be a more concise way to do this?
const hasDirectories: ComputedRef<boolean> = computed(
	() => !isEmpty(directoryContent?.value?.files?.Directory)
);
const hasCode: ComputedRef<boolean> = computed(() => !isEmpty(directoryContent.value?.files?.Code));
const hasData: ComputedRef<boolean> = computed(() => !isEmpty(directoryContent.value?.files?.Data));
const hasDocuments: ComputedRef<boolean> = computed(
	() => !isEmpty(directoryContent?.value?.files?.Documents)
);
const hasOther: ComputedRef<boolean> = computed(
	() => !isEmpty(directoryContent?.value?.files?.Other)
);

const provenanceDescription: ComputedRef<string> = computed(
	() =>
		`These files were found in the following repository: https://github.com/${repoOwnerAndName.value}`
);

async function initializeCodeBrowser() {
	isModalVisible.value = true;
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
async function openSelectedFiles() {
	// split the selected files into their respective categories
	const selectedCodeFiles: GithubFile[] = [
		...selectedFiles.value,
		...selectedUnknownFiles.value
	].filter((file) => file.fileCategory === FileCategory.Code);

	// const selectedDataFiles : GithubFile[] = selectedFiles.value.filter(file => file.fileCategory === FileCategory.Data);
	// const selectedDocumentFiles : GithubFile[] = selectedFiles.value.filter(file => file.fileCategory === FileCategory.Documents);

	// Import code files, if any were selected
	if (selectedCodeFiles.length > 0) {
		await openCodeFiles(selectedCodeFiles);
	}
}

/**
 * Refreshes the directory content and sets the current directory
 * @param directory The directory to set and refresh from
 */
async function openDirectory(directory: string) {
	currentDirectory.value = directory;
	directoryContent.value = await getGithubRepositoryContent(
		repoOwnerAndName.value,
		currentDirectory.value
	);
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
 * Opens the code editor with the selected file
 * @param githubFiles The code files to open
 */
async function openCodeFiles(githubFiles: GithubFile[]) {
	const codeRequests: CodeRequest[] = [] as CodeRequest[];

	// iterate through our files and fetch their contents
	githubFiles.forEach(async (file) => {
		const code = await getGithubCode(repoOwnerAndName.value, file.path);
		const asset: Tab = {
			assetName: file.name,
			pageType: ProjectAssetTypes.CODE,
			assetId: undefined
		};
		codeRequests.push({
			asset,
			code
		});
	});

	emit('open-code', codeRequests);
}
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
	height: 70%;
}

.code-editor {
	border-top: 1px solid var(--surface-border-light);
	width: 100%;
	height: 95%;
	border-radius: 5px;
}

.provenance-title {
	padding-bottom: 10px;
}

.provenance-container {
	background-color: rgb(231, 231, 231);
	border-radius: 5px;
	height: 30%;
	margin-top: 10px;
	padding: 15px;
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
</style>
