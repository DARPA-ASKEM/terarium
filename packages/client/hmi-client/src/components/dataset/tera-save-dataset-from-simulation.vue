<template>
	<Button
		:disabled="isSaveDisabled"
		outlined
		title="Saves the current version of the model as a new Terarium asset"
		@click="showSaveInput = !showSaveInput"
	>
		<span class="pi pi-save p-button-icon p-button-icon-left"></span>
		<span class="p-button-text">Save as new dataset</span>
	</Button>
	<span v-if="showSaveInput" style="padding-left: 1em; padding-right: 2em">
		<InputText v-model="saveAsName" class="post-fix" placeholder="New dataset name" />
		<i
			class="pi pi-times i"
			:class="{ clear: hasValidDatasetName }"
			@click="(saveAsName = ''), (showSaveInput = false)"
		></i>
		<i
			v-if="useProjects().activeProject.value?.id"
			class="pi pi-check i"
			:class="{ save: hasValidDatasetName }"
			@click="saveDatasetToProject"
		></i>
	</span>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { saveDataset } from '@/services/dataset';
import { useProjects } from '@/composables/project';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';

const props = defineProps<{
	simulationRunId: string;
}>();

const saveAsName = ref('');
const showSaveInput = ref(<boolean>false);
const hasValidDatasetName = computed<boolean>(() => saveAsName.value !== '');
const isSaveDisabled = computed<boolean>(() => {
	if (props.simulationRunId === '' || !useProjects().activeProject.value?.id) return true;
	return false;
});
const saveDatasetToProject = async () => {
	const { activeProject, refresh } = useProjects();
	console.log(activeProject.value?.id);
	if (activeProject.value?.id) {
		console.log(props.simulationRunId);
		if (await saveDataset(activeProject.value.id, props.simulationRunId, saveAsName.value)) {
			refresh();
		}
	}
};
</script>

<style scoped></style>
