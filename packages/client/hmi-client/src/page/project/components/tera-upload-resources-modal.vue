<template>
	<Teleport to="body">
		<tera-modal v-if="visible" class="modal" @modal-mask-clicked="() => emit('close')">
			<template #header>
				<h4>Upload resources</h4>
			</template>
			<template #default>
				<section class="main-section">
					<section>
						<p class="subheader">Add resources to your project here</p>
						<tera-drag-and-drop-importer
							:accept-types="[
								AcceptedTypes.PDF,
								AcceptedTypes.CSV,
								AcceptedTypes.TXT,
								AcceptedTypes.MD,
								AcceptedTypes.PY,
								AcceptedTypes.R,
								AcceptedTypes.JL
							]"
							:accept-extensions="[
								AcceptedExtensions.PDF,
								AcceptedExtensions.CSV,
								AcceptedExtensions.TXT,
								AcceptedExtensions.MD,
								AcceptedExtensions.PY,
								AcceptedExtensions.R,
								AcceptedExtensions.JL
							]"
							:import-action="processFiles"
							:progress="progress"
							@import-completed="importCompleted"
						></tera-drag-and-drop-importer>
					</section>
					<section>
						<p>Or upload from a URL</p>
						<InputText v-model="urlToUpload"></InputText>
					</section>
					<tera-import-github-file
						:visible="isImportGithubFileModalVisible"
						:url-string="urlToUpload"
						@close="
							isImportGithubFileModalVisible = false;
							emit('close');
						"
					/>
				</section>
			</template>
			<template #footer>
				<Button label="Upload" class="p-button-primary" @click="upload" />
				<Button label="Cancel" class="p-button-secondary" @click="() => emit('close')" />
			</template>
		</tera-modal>
	</Teleport>
</template>

<script setup lang="ts">
import TeraModal from '@/components/widgets/tera-modal.vue';
import Button from 'primevue/button';
import { AcceptedTypes, AcceptedExtensions } from '@/types/common';
import { uploadCodeToProject } from '@/services/code';
import { useProjects } from '@/composables/project';
import { Artifact, AssetType, Dataset } from '@/types/Types';
import { uploadArtifactToProject } from '@/services/artifact';
import { createNewDatasetFromCSV } from '@/services/dataset';
import useAuthStore from '@/stores/auth';
import { ref } from 'vue';
import TeraDragAndDropImporter from '@/components/extracting/tera-drag-n-drop-importer.vue';
import InputText from 'primevue/inputtext';
import { useToastService } from '@/services/toast';
import TeraImportGithubFile from '@/components/widgets/tera-import-github-file.vue';

defineProps<{
	visible: boolean;
}>();
const emit = defineEmits(['close']);

const progress = ref(0);
const results = ref<{ id: string; name: string; assetType: AssetType }[] | null>(null);
const urlToUpload = ref('');
const isImportGithubFileModalVisible = ref(false);

async function processFiles(files: File[], csvDescription: string) {
	return files.map(async (file) => {
		switch (file.type) {
			case AcceptedTypes.CSV:
				return processDataset(file, csvDescription);
			case AcceptedTypes.PDF:
			case AcceptedTypes.TXT:
			case AcceptedTypes.MD:
				return processArtifact(file);
			case AcceptedTypes.PY:
			case AcceptedTypes.R:
			case AcceptedTypes.JL:
				return processCode(file);
			default:
				return { id: '', assetType: '' };
		}
	});
}

/**
 * Process a python, R or Julia file into a code asset
 * @param file
 */
async function processCode(file: File) {
	// This is pdf, txt, md files
	const newCode = await uploadCodeToProject(file, progress);
	return { id: newCode?.id ?? '', assetType: AssetType.Code };
}

/**
 * Process a pdf, txt, md file into an artifact
 * @param file
 */
async function processArtifact(file: File) {
	// This is pdf, txt, md files
	const artifact: Artifact | null = await uploadArtifactToProject(
		progress,
		file,
		useProjects().activeProject.value?.username ?? '',
		''
	);
	return { id: artifact?.id ?? '', assetType: AssetType.Artifacts };
}

/**
 * Process a csv file into a dataset
 * @param file
 * @param description
 */
async function processDataset(file: File, description: string) {
	// let addedCSV: CsvAsset | null = null;
	const addedDataset: Dataset | null = await createNewDatasetFromCSV(
		progress,
		file,
		useAuthStore().user?.name ?? '',
		description
	);
	return { id: addedDataset?.id ?? '', assetType: AssetType.Datasets };
}

function importCompleted(newResults: { id: string; name: string; assetType: AssetType }[] | null) {
	results.value = newResults?.filter((r) => r.id !== '') ?? [];
}

function uploadCompleted(uploadedFiles: { id: string; name: string; assetType: AssetType }[]) {
	emit('close');
	const resourceMsg = uploadedFiles.length > 1 ? 'resources were' : 'resource was';
	useToastService().success(
		'Success!',
		`${uploadedFiles.length} ${resourceMsg} successfuly added to this project`
	);
}

async function upload() {
	if (results.value) {
		await Promise.all(
			results.value?.map(({ id, assetType }): Promise<any> => useProjects().addAsset(assetType, id))
		).then((resolved) => {
			uploadCompleted(resolved);
		});
	} else if (urlToUpload.value) {
		isImportGithubFileModalVisible.value = true;
	} else {
		useToastService().error('Error!', 'No files were selected');
	}
}
</script>

<style scoped>
.main-section {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.main-section section {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}
</style>
