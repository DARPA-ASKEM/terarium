<template>
	<tera-modal v-if="visible" @modal-mask-clicked="() => emit('close')">
		<template #header>
			<h4>Upload resources</h4>
		</template>
		<template #default>
			<section class="main-section">
				<section>
					<label class="subheader">Add project here.</label>
					<tera-drag-and-drop-importer
						:accept-types="[AcceptedTypes.JSON]"
						:accept-extensions="[AcceptedExtensions.JSON]"
						:import-action="processProjects"
						:progress="progress"
						@import-completed="importCompleted"
						@imported-files-updated="(value) => (importedFiles = value)"
					></tera-drag-and-drop-importer>
				</section>
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
import { useProjects } from '@/composables/project';
import { AssetType } from '@/types/Types';
import { createProjectFromFile } from '@/services/project';
import { ref } from 'vue';
import TeraDragAndDropImporter from '@/components/extracting/tera-drag-n-drop-importer.vue';
import { useToastService } from '@/services/toast';
import { extractPDF } from '@/services/knowledge';

defineProps<{
	visible: boolean;
}>();
const emit = defineEmits(['close']);

const progress = ref(0);
const results = ref<{ id: string; name: string; assetType: AssetType }[] | null>(null);
const urlToUpload = ref('');
const isImportGithubFileModalVisible = ref(false);
const importedFiles = ref<File[]>([]);

async function processProjects(files: File[]) {
	return files.map(async (file) => {
		await createProjectFromFile(file);
	});
}

function importCompleted(newResults: { id: string; name: string; assetType: AssetType }[] | null) {
	results.value = newResults?.filter((r) => r.id !== '') ?? [];
}

async function upload() {
	if (results.value) {
		const createAssetsPromises = (results.value ?? []).map(({ id, assetType }) =>
			useProjects().addAsset(assetType, id)
		);
		const createdAssets = await Promise.all(createAssetsPromises);
		createdAssets.forEach((_, index) => {
			const { name, id } = (results.value ?? [])[index];
			if (name && name.toLowerCase().endsWith('.pdf')) {
				extractPDF(id);
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
</style>
