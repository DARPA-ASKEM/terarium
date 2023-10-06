<template>
	<Teleport to="body">
		<tera-modal v-if="visible" class="modal" @modal-mask-clicked="() => emit('close')">
			<template #header>
				<h4>Upload resources</h4>
			</template>
			<template #default>
				<p class="subheader">Add resources to your project here</p>
				<tera-drag-and-drop-importer
					:show-preview="true"
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

				<section>
					<Card v-for="(item, i) in results" :key="i" class="card">
						<template #title>
							<div class="card-img"></div>
						</template>
						<template #content>
							<div class="card-content">
								<div v-if="item.file" class="file-title">{{ item.file.name }}</div>
								<div v-if="item.response" class="file-content">
									<br />
									<div>Extracted Text</div>
									<div>{{ item.response.text }}</div>
									<br />
									<div v-if="item.response.images">Images Found</div>
									<div v-for="image in item.response.images" :key="image">
										<img :src="`data:image/jpeg;base64,${image}`" alt="" />
									</div>
									<br />
									<i class="pi pi-plus"></i>
								</div>
							</div>
						</template>
					</Card>
				</section>
			</template>
			<template #footer>
				<Button
					label="Upload"
					class="p-button-primary"
					@click="() => emit('close')"
					:disabled="!results"
				/>
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
import { Artifact, AssetType, CsvAsset, Dataset } from '@/types/Types';
import { uploadArtifactToProject } from '@/services/artifact';
import { extractPDF } from '@/services/knowledge';
import { createNewDatasetFromCSV, downloadRawFile } from '@/services/dataset';
import useAuthStore from '@/stores/auth';
import { ref } from 'vue';
import { logger } from '@/utils/logger';
import TeraDragAndDropImporter from '@/components/extracting/tera-drag-n-drop-importer.vue';

defineProps<{
	visible: boolean;
}>();
const emit = defineEmits(['close']);

const progress = ref(0);
const results = ref<
	{ file: File; error: boolean; response: { text: string; images: string[] } }[] | null
>(null);

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
				return { file, error: true, response: { text: '', images: [] } };
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
	let newAsset;
	if (newCode && newCode.id) {
		newAsset = await useProjects().addAsset(AssetType.Code, newCode.id);
	}
	if (newAsset) {
		return { file, error: false, response: { text: '', images: [] } };
	}
	return { file, error: true, response: { text: '', images: [] } };
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
	let newAsset;
	if (artifact && artifact.id) {
		newAsset = await useProjects().addAsset(AssetType.Artifacts, artifact.id);
	}
	if (artifact && newAsset && file.name.toLowerCase().endsWith('.pdf')) {
		await extractPDF(artifact);
		return { file, error: false, response: { text: '', images: [] } };
	}
	return { file, error: true, response: { text: '', images: [] } };
}

/**
 * Process a csv file into a dataset
 * @param file
 * @param description
 */
async function processDataset(file: File, description: string) {
	let addedCSV: CsvAsset | null = null;
	const addedDataset: Dataset | null = await createNewDatasetFromCSV(
		progress,
		file,
		useAuthStore().user?.name ?? '',
		description
	);

	let newAsset;
	if (addedDataset?.id) {
		newAsset = await useProjects().addAsset(AssetType.Datasets, addedDataset.id);
		addedCSV = await downloadRawFile(addedDataset.id, file.name);
	}

	if (addedCSV !== null && newAsset) {
		const text: string = addedCSV?.csv?.join('\r\n') ?? '';
		const images = [];

		return { file, error: false, response: { text, images } };
	}
	return { file, error: true, response: { text: '', images: [] } };
}

async function importCompleted(
	newResults: { file: File; error: boolean; response: { text: string; images: string[] } }[] | null
) {
	// This is a hacky override for dealing with CSVs
	if (
		newResults &&
		newResults.length === 1 &&
		(newResults[0].file.type === AcceptedTypes.CSV ||
			newResults[0].file.type === AcceptedTypes.TXT ||
			newResults[0].file.type === AcceptedTypes.MD)
	) {
		if (newResults[0].error) {
			logger.error('Failed to upload file. Is it too large?', { showToast: true });
		}
		results.value = null;
		emit('close');
	} else {
		results.value = newResults;
	}
}
</script>

<style scoped></style>
