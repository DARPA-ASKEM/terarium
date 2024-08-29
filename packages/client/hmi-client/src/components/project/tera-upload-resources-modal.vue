<template>
	<tera-modal v-if="visible" @modal-mask-clicked="() => emit('close')">
		<template #header>
			<h4>Upload resources</h4>
		</template>
		<template #default>
			<section class="main-section">
				<section>
					<label class="subheader">Add documents, models or datasets to your project here.</label>
					<div class="supported-resources">
						<div><i class="pi pi-file" /><span>Documents</span><span>(PDF, md, txt)</span></div>
						<div><i class="pi pi-share-alt" /><span>Models</span><span>(AMR, sbml, vensim, stella)</span></div>
						<div><dataset-icon /><span>Datasets</span><span>(csv, netcdf)</span></div>
					</div>
					<tera-drag-and-drop-importer
						:accept-types="[
							AcceptedTypes.PDF,
							AcceptedTypes.CSV,
							AcceptedTypes.TXT,
							AcceptedTypes.MD,
							AcceptedTypes.PY,
							AcceptedTypes.R,
							AcceptedTypes.JL,
							AcceptedTypes.NC,
							AcceptedTypes.JSON,
							AcceptedTypes.XML,
							AcceptedTypes.SBML,
							AcceptedTypes.MDL,
							AcceptedTypes.XMILE,
							AcceptedTypes.ITMX,
							AcceptedTypes.STMX
						]"
						:accept-extensions="[
							AcceptedExtensions.PDF,
							AcceptedExtensions.CSV,
							AcceptedExtensions.TXT,
							AcceptedExtensions.MD,
							AcceptedExtensions.PY,
							AcceptedExtensions.R,
							AcceptedExtensions.JL,
							AcceptedExtensions.NC,
							AcceptedExtensions.JSON,
							AcceptedExtensions.XML,
							AcceptedExtensions.SBML,
							AcceptedExtensions.MDL,
							AcceptedExtensions.XMILE,
							AcceptedExtensions.ITMX,
							AcceptedExtensions.STMX
						]"
						:import-action="processFiles"
						:progress="progress"
						@import-completed="importCompleted"
						@imported-files-updated="(value) => (importedFiles = value)"
					></tera-drag-and-drop-importer>
				</section>
				<section v-if="importedFiles.length < 1">
					<label>Or upload from a Github repository URL</label>
					<tera-input-text v-model="urlToUpload" class="upload-from-github-url" />
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
</template>

<script setup lang="ts">
import TeraModal from '@/components/widgets/tera-modal.vue';
import Button from 'primevue/button';
import { AcceptedExtensions, AcceptedTypes } from '@/types/common';
import { uploadCodeToProject } from '@/services/code';
import type { ProjectAsset } from '@/types/Types';
import { AssetType, ProvenanceType } from '@/types/Types';
import { uploadDocumentAssetToProject } from '@/services/document-assets';
import { createNewDatasetFromFile } from '@/services/dataset';
import useAuthStore from '@/stores/auth';
import { ref } from 'vue';
import TeraDragAndDropImporter from '@/components/extracting/tera-drag-n-drop-importer.vue';
import { useToastService } from '@/services/toast';
import TeraImportGithubFile from '@/components/widgets/tera-import-github-file.vue';
import { extractPDF } from '@/services/knowledge';
import DatasetIcon from '@/assets/svg/icons/dataset.svg?component';
import { uploadArtifactToProject } from '@/services/artifact';
import { createModel, processAndAddModelToProject, validateAMRFile } from '@/services/model';
import { createProvenance, RelationshipType } from '@/services/provenance';
import { modelCard } from '@/services/goLLM';
import TeraInputText from '@/components//widgets/tera-input-text.vue';
import { activeProject, activeProjectId } from '@/composables/activeProject';
import * as ProjectService from '@/services/project';

defineProps<{
	visible: boolean;
}>();
const emit = defineEmits(['close']);

const progress = ref(0);
const results = ref<{ asset: ProjectAsset }[] | null>(null);
const urlToUpload = ref('');
const isImportGithubFileModalVisible = ref(false);
const importedFiles = ref<File[]>([]);

async function processFiles(files: File[], description: string) {
	return files.map(async (file) => {
		switch (file.name.split('.').pop()) {
			case AcceptedExtensions.CSV:
			case AcceptedExtensions.NC:
				return processDataset(file, description);
			case AcceptedExtensions.PDF:
			case AcceptedExtensions.TXT:
			case AcceptedExtensions.MD:
				return processDocument(file);
			case AcceptedExtensions.PY:
			case AcceptedExtensions.R:
			case AcceptedExtensions.JL:
				return processCode(file);
			case AcceptedExtensions.JSON:
				return processAMRJson(file);
			case AcceptedExtensions.XML:
			case AcceptedExtensions.SBML:
			case AcceptedExtensions.MDL:
			case AcceptedExtensions.XMILE:
			case AcceptedExtensions.ITMX:
			case AcceptedExtensions.STMX:
				return processModel(file);
			default:
				return { asset: '' };
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
	const addedAsset: ProjectAsset | null = await uploadDocumentAssetToProject(file, '', progress);
	return { asset: addedAsset };
}

/**
 * Process a csv file into a dataset
 * @param file
 * @param description
 */
async function processDataset(file: File, description: string) {
	const addedAsset: ProjectAsset | null = await createNewDatasetFromFile(progress, file, description);
	return { asset: addedAsset };
}

/*
 * Process an AMR file into a model asset
 * @param file
 */
async function processAMRJson(file: File) {
	// Check if the file is an AMR file
	const amr = await validateAMRFile(file);
	if (amr) {
		const model = await createModel(amr);
		return { id: model?.id ?? '', assetType: AssetType.Model };
	}
	// Ignore none AMR json files for now
	return { id: '', assetType: '' };
}

/**
 * Process a model file into a model asset
 * @param file
 */
async function processModel(file: File) {
	// Upload file as an artifact, create an empty model, and link them
	const artifact = await uploadArtifactToProject(file, useAuthStore().user?.id ?? '', '', progress);
	if (!artifact) return { id: '', assetType: '' };
	const newModelId = await processAndAddModelToProject(artifact);
	await createProvenance({
		relation_type: RelationshipType.EXTRACTED_FROM,
		left: newModelId ?? '',
		left_type: ProvenanceType.Model,
		right: artifact.id ?? '',
		right_type: ProvenanceType.Artifact
	});
	return { id: newModelId ?? '', assetType: AssetType.Model };
}

function importCompleted(newResults: { asset: ProjectAsset }[] | null) {
	results.value = newResults ?? [];
}

const TIMEOUT_MS = 100;
async function upload() {
	if (results.value) {
		const createdAssets = results.value;
		setTimeout(async () => {
			activeProject.value = await ProjectService.get(activeProjectId.value);
		}, TIMEOUT_MS);
		createdAssets.forEach((_, index) => {
			const { assetName, assetId } = (results.value ?? [])[index].asset;
			if (assetName && assetName.toLowerCase().endsWith('.pdf')) {
				extractPDF(assetId);
			} else if (assetName && (assetName.toLowerCase().endsWith('.txt') || assetName.toLowerCase().endsWith('.md'))) {
				modelCard(assetId);
			}
		});
		emit('close');
		const resourceMsg = createdAssets.length > 1 ? 'resources were' : 'resource was';
		useToastService().success('Success!', `${createdAssets.length} ${resourceMsg} successfully added to this project`);
		importedFiles.value = [];
		results.value = null;
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
	width: 65vw;
	min-width: 32rem;
	max-width: 48rem;
}

.supported-resources {
	display: flex;
	justify-content: space-between;
	margin-bottom: var(--gap-1);

	div {
		display: flex;
		align-items: end;
		gap: var(--gap-1);
		font-size: 0.75rem;
	}
	span:first-of-type {
		font-weight: 700;
	}
	span:last-of-type {
		color: #667985;
	}
}

:deep(.upload-from-github-url) {
	margin-bottom: 0;
}
</style>
