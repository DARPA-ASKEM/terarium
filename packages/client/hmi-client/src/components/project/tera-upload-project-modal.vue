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
						:accept-types="[AcceptedTypes.PROJECTCONFIG]"
						:accept-extensions="[AcceptedExtensions.PROJECTCONFIG]"
						:import-action="verifyProject"
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
import { createProjectFromFile } from '@/services/project';
import { ref } from 'vue';
import TeraDragAndDropImporter from '@/components/extracting/tera-drag-n-drop-importer.vue';

defineProps<{
	visible: boolean;
}>();
const emit = defineEmits(['close']);

const progress = ref(0);
const importedFiles = ref<File[]>([]);

async function verifyProject(files: File[]) {
	return files.map(async (file) => validateProjectFile(file));
}

// Checks that the project file contains the correct extension.
function validateProjectFile(file: File) {
	if (!file.name.endsWith(AcceptedExtensions.PROJECTCONFIG)) return null;
	return true;
}

async function upload() {
	importedFiles.value.forEach((file) => {
		createProjectFromFile(file, progress);
	});
	emit('close');
}

function importCompleted() {
	progress.value = 100;
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
