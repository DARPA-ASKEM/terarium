<template>
	<Button
		:disabled="isSaveDisabled"
		outlined
		severity="secondary"
		title="Saves the current version of the model as a new Terarium asset"
		@click="showSaveInput = true"
		label="Save as new dataset"
		icon="pi pi-save"
	/>
	<Dialog
		v-model:visible="showSaveInput"
		modal
		header="Save as new dataset"
		class="save-dialog w-4"
	>
		<div class="dialog-content">
			<div class="flex flex-column mt-2 gap-2">
				<label for="saveNameInput">What do you want to call it?</label>
				<InputText v-model="saveAsName" id="saveNameInput" placeholder="Enter name" />
			</div>
		</div>
		<template #footer>
			<div class="p-dialog-footer p-0 mb-2">
				<Button label="Cancel" outlined severity="secondary" @click="showSaveInput = false" />
				<Button label="Save" :disabled="!hasValidDatasetName" @click="saveDatasetToProject" />
			</div>
		</template>
	</Dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { saveDataset } from '@/services/dataset';
import { useProjects } from '@/composables/project';
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import { logger } from '@/utils/logger';

const props = defineProps<{
	simulationRunId?: string;
}>();

const saveAsName = ref('');
const showSaveInput = ref<boolean>(false);
const hasValidDatasetName = computed<boolean>(() => saveAsName.value !== '');
const isSaveDisabled = computed<boolean>(() => {
	if (
		props.simulationRunId === undefined ||
		props.simulationRunId === '' ||
		!useProjects().activeProject.value?.id
	)
		return true;
	return false;
});
const saveDatasetToProject = async () => {
	const { activeProject, refresh } = useProjects();
	if (activeProject.value?.id) {
		logger.success(`Added dataset: ${saveAsName.value}`);
		if (await saveDataset(activeProject.value.id, props.simulationRunId, saveAsName.value)) {
			refresh();
		}
	}
	showSaveInput.value = false;
};
</script>

<style scoped>
.show-save-input {
	padding-left: 1em;
	padding-right: 2em;
}
</style>
