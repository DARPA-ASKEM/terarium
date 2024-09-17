<template>
	<Dialog
		v-model:visible="showSaveInput"
		modal
		header="Save as new dataset"
		class="save-dialog w-4"
		@hide="closedSaveInput"
	>
		<div class="dialog-content">
			<div class="flex flex-column mt-2 gap-2">
				<label for="saveNameInput">What do you want to call it?</label>
				<tera-input-text v-model="saveAsName" id="saveNameInput" placeholder="Enter name" />
			</div>
		</div>
		<template #footer>
			<div class="p-dialog-footer p-0 mb-2">
				<Button label="Cancel" outlined severity="secondary" @click="closedSaveInput" />
				<Button label="Save" :disabled="!hasValidDatasetName" @click="saveDatasetToProject" />
			</div>
		</template>
	</Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { saveDataset } from '@/services/dataset';
import { useProjects } from '@/composables/project';
import Dialog from 'primevue/dialog';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import Button from 'primevue/button';
import { logger } from '@/utils/logger';

const props = defineProps<{
	simulationRunId?: string;
	showDialog: boolean;
}>();

const saveAsName = ref('');
const showSaveInput = ref<boolean>(false);
const hasValidDatasetName = computed<boolean>(() => saveAsName.value !== '');
const emit = defineEmits(['hide-dialog']);
const saveDatasetToProject = async () => {
	const { activeProject, refresh } = useProjects();
	if (activeProject.value?.id) {
		if (await saveDataset(activeProject.value.id, props.simulationRunId, saveAsName.value)) {
			logger.success(`Added dataset: ${saveAsName.value}`);
			refresh();
		}
	}
	closedSaveInput();
};

const closedSaveInput = () => {
	showSaveInput.value = false;
	emit('hide-dialog');
};

watch(
	() => props.showDialog,
	(value) => {
		if (value) {
			showSaveInput.value = true;
		}
	}
);
</script>

<style scoped>
.show-save-input {
	padding-left: 1em;
	padding-right: 2em;
}
</style>
