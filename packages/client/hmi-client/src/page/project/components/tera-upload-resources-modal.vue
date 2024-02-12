<template>
	<Teleport to="body">
		<tera-modal v-if="visible" @modal-mask-clicked="() => emit('close')">
			<template #header>
				<h4>Upload resources</h4>
			</template>
			<template #default>
				<section class="main-section">
					<section>
						<label class="subheader">Add resources to your project here</label>
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
							@imported-files-updated="(value) => (importedFiles = value)"
						></tera-drag-and-drop-importer>
					</section>
					<section v-if="importedFiles.length < 1">
						<label>Or upload from a Github repository URL</label>
						<InputText v-model="urlToUpload" class="upload-from-github-url"></InputText>
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
import { AcceptedExtensions, AcceptedTypes } from '@/types/common';
import { uploadCodeToProject } from '@/services/code';
import { useProjects } from '@/composables/project';
import type { DocumentAsset, Dataset } from '@/types/Types';
import { AssetType } from '@/types/Types';
import { uploadDocumentAssetToProject } from '@/services/document-assets';
import { createNewDatasetFromCSV } from '@/services/dataset';
import useAuthStore from '@/stores/auth';
import { ref } from 'vue';
import TeraDragAndDropImporter from '@/components/extracting/tera-drag-n-drop-importer.vue';
import InputText from 'primevue/inputtext';
import { useToastService } from '@/services/toast';
import TeraImportGithubFile from '@/components/widgets/tera-import-github-file.vue';
import { extractPDF } from '@/services/knowledge';
import { modelCard } from '@/services/goLLM';

defineProps<{
	visible: boolean;
}>();
const emit = defineEmits(['close']);

const progress = ref(0);
const results = ref<{ id: string; name: string; assetType: AssetType }[] | null>(null);
const urlToUpload = ref('');
const isImportGithubFileModalVisible = ref(false);
const importedFiles = ref<File[]>([]);

async function processFiles(files: File[], csvDescription: string) {
	return files.map(async (file) => {
		switch (file.name.split('.').pop()) {
			case AcceptedExtensions.CSV:
				return processDataset(file, csvDescription);
			case AcceptedExtensions.PDF:
			case AcceptedExtensions.TXT:
			case AcceptedExtensions.MD:
				return processDocument(file);
			case AcceptedExtensions.PY:
			case AcceptedExtensions.R:
			case AcceptedExtensions.JL:
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
	const newCode = await uploadCodeToProject(file, progress);
	return { id: newCode?.id ?? '', assetType: AssetType.Code };
}

/**
 * Process a pdf, txt, md file into an artifact
 * @param file
 */
async function processDocument(file: File) {
	// This is pdf, txt, md files
	const document: DocumentAsset | null = await uploadDocumentAssetToProject(
		file,
		useAuthStore().user?.id ?? '',
		'',
		progress
	);
	return { id: document?.id ?? '', assetType: AssetType.Document, name: file.name };
}

/**
 * Process a csv file into a dataset
 * @param file
 * @param description
 */
async function processDataset(file: File, description: string) {
	const addedDataset: Dataset | null = await createNewDatasetFromCSV(
		progress,
		file,
		useAuthStore().user?.id ?? '',
		description
	);
	return { id: addedDataset?.id ?? '', assetType: AssetType.Dataset };
}

function importCompleted(newResults: { id: string; name: string; assetType: AssetType }[] | null) {
	results.value = newResults?.filter((r) => r.id !== '') ?? [];
}

async function upload() {
	if (results.value) {
		await Promise.all(
			results.value?.map(({ id, assetType, name }): Promise<any> => {
				const newAsset = useProjects().addAsset(assetType, id);
				if (name && name.toLowerCase().endsWith('.pdf')) {
					extractPDF(id);
				} else if (
					(name && name.toLowerCase().endsWith('.txt')) ||
					(name && name.toLowerCase().endsWith('.md'))
				) {
					modelCard(id);
				}
				return newAsset;
			})
		).then((resolved) => {
			emit('close');
			const resourceMsg = resolved.length > 1 ? 'resources were' : 'resource was';
			useToastService().success(
				'Success!',
				`${resolved.length} ${resourceMsg} successfuly added to this project`
			);
			importedFiles.value = [];
			results.value = null;
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

:deep(.upload-from-github-url) {
	margin-bottom: 0;
}
</style>
