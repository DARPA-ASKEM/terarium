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
					<h2>{{ modalTitle }}</h2>
					<b>({{ directoryContent?.totalFiles }}) files found in: </b>
					<div class="flex justify-content-left">
						<Breadcrumb :home="home" :model="directories" />
					</div>
				</template>
				<template #default>
					<section>
						<nav>
							<span v-if="isInDirectory" @click="openDirectory('')">
								<i class="pi pi-folder-open" />
								<b> ..</b>
							</span>
							<template v-if="hasDirectories">
								<header :style="{ '--count': `'${directoryContent?.files.Directory?.length}'` }">
									Directories
								</header>
								<ul>
									<li
										v-for="(content, index) in directoryContent?.files.Directory"
										:key="index"
										@click="openDirectory(content.path)"
									>
										<i class="pi pi-folder" />
										{{ content.name }}
									</li>
								</ul>
							</template>
							<template v-if="hasCode">
								<header :style="{ '--count': `'${directoryContent?.files.Code?.length}'` }">
									Code
								</header>
								<ul>
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
								</ul>
							</template>
							<template v-if="hasData">
								<header :style="{ '--count': `'${directoryContent?.files.Data?.length}'` }">
									Data
								</header>
								<ul>
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
								</ul>
							</template>
							<template v-if="hasDocuments">
								<header :style="{ '--count': `'${directoryContent?.files.Documents?.length}'` }">
									<b>Documents</b>
								</header>
								<ul>
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
								</ul>
							</template>
							<template v-if="hasOther">
								<header :style="{ '--count': `'${directoryContent?.files.Other?.length}'` }">
									Unknown File Types
								</header>
								<ul>
									<li
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
												style="width: 90%"
											/>
										</div>
									</li>
								</ul>
							</template>
						</nav>

						<v-ace-editor
							v-model:value="displayCode"
							@init="initialize"
							lang="python"
							class="code-editor"
						/>
					</section>
				</template>
				<template #footer>
					<Button :disabled="isEmpty(filesSelection)" @click="openSelectedFiles">
						{{ filesSelectionButton }}
					</Button>
					<Button class="p-button-outlined" label="Cancel" @click="isModalVisible = false" />
				</template>
			</tera-modal>
		</Teleport>
	</main>
</template>

<script setup lang="ts">
import { computed, ComputedRef, ref } from 'vue';
import Button from 'primevue/button';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { IProject } from '@/types/Project';
import { isEmpty } from 'lodash';
import { getGithubCode, getGithubRepositoryContent } from '@/services/github-import';
import { Artifact, FileCategory, GithubFile, GithubRepo } from '@/types/Types';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import { getModeForPath } from 'ace-builds/src-noconflict/ext-modelist';
import Checkbox from 'primevue/checkbox';
import Dropdown from 'primevue/dropdown';
import Breadcrumb from 'primevue/breadcrumb';
import { createNewDatasetFromGithubFile } from '@/services/dataset';
import { createNewArtifactFromGithubFile } from '@/services/artifact';
import { extractPDF } from '@/services/models/extractions';

const props = defineProps<{
	urlString: string;
	showImportButton: boolean;
	project?: IProject;
}>();

const repoOwnerAndName = ref<string>('');
const currentDirectory = ref<string>('');
const directoryContent = ref<GithubRepo | null>(null);
const isModalVisible = ref<boolean>(false);
const selectedFiles = ref<GithubFile[]>([]);
const selectedUnknownFiles = ref<GithubFile[]>([]);
const editor = ref<VAceEditorInstance['_editor'] | null>(null);
const selectedText = ref<string>('');
const displayCode = ref<string>('');

const modalTitle = computed(
	() =>
		`https://github.com/${repoOwnerAndName.value}/${
			isInDirectory.value ? currentDirectory.value : ''
		}`
);

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

const filesSelection = computed(() => [...selectedFiles.value, ...selectedUnknownFiles.value]);
const filesSelectionButton = computed(
	() => `Import ${filesSelection.value.length} file${filesSelection.value.length > 1 ? 's' : ''}`
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

	// Import code files, if any were selected
	if (selectedCodeFiles.length > 0) {
		await openCodeFiles(selectedCodeFiles);
	}
	const selectedDataFiles: GithubFile[] = [
		...selectedFiles.value,
		...selectedUnknownFiles.value
	].filter((file) => file.fileCategory === FileCategory.Data);

	if (selectedDataFiles.length > 0) {
		await importDataFiles(selectedDataFiles);
	}

	const selectedDocumentFiles: GithubFile[] = selectedFiles.value.filter(
		(file) => file.fileCategory === FileCategory.Documents
	);

	if (selectedDocumentFiles.length > 0) {
		await importDocumentFiles(selectedDocumentFiles);
	}

	// FIXME: Files aren't opening
	isModalVisible.value = false;
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
 * async function to import the selected data files and create new datasets from them
 * @param githubFiles the data files to open
 */
async function importDataFiles(githubFiles: GithubFile[]) {
	// iterate through our files and fetch their contents
	githubFiles.forEach(async (githubFile) => {
		// Create a new dataset from this GitHub file
		await createNewDatasetFromGithubFile(
			repoOwnerAndName.value,
			githubFile.path,
			props.project?.username ?? '',
			props.project?.id ?? ''
		);
	});
}

async function importDocumentFiles(githubFiles: GithubFile[]) {
	githubFiles.forEach(async (githubFile) => {
		const artifact: Artifact | null = await createNewArtifactFromGithubFile(
			repoOwnerAndName.value,
			githubFile.path,
			props.project?.username ?? '',
			props.project?.id ?? ''
		);

		if (artifact && githubFile.name?.toLowerCase().endsWith('.pdf')) {
			extractPDF(artifact);
		}
	});
}

/**
 * Opens the code editor with the selected file
 * @param githubFiles The code files to open
 */
async function openCodeFiles(githubFiles: GithubFile[]) {
	// For now just throw to the document path as they're all artifacts
	await importDocumentFiles(githubFiles);
}
</script>

<style scoped>
main {
	display: flex;
	align-items: center;
}

section {
	display: flex;
	flex-direction: row;
	gap: 1rem;
	justify-content: space-between;
	max-height: 66vh;
}

nav {
	flex-grow: 1;
	list-style: none;
	overflow-y: auto;
}

nav header {
	font-weight: var(--font-weight-semibold);
	margin-bottom: 0.5rem;
}

nav header::after {
	content: '(' var(--count) ')';
}

nav header:not(:first-of-type) {
	margin-top: 1rem;
}

li {
	cursor: pointer;
	border-radius: 0.5rem;
	display: flex;
	gap: 0.3rem;
	align-items: center;
}

li + li {
	margin-top: 0.33rem;
}

li:hover {
	background-color: var(--surface-hover);
}

.code-editor {
	flex-grow: 5;
	border: 1px solid var(--surface-border);
	border-radius: var(--border-radius);
}
</style>
