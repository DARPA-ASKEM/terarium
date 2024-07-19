<template>
	<div class="breakdown-pane-container">
		<div class="add-selected-buttons">
			<Button
				v-if="selectedSearchItems.length > 0"
				severity="secondary"
				@click="emit('clear-selected')"
				label="Remove all"
			/>
			<Dropdown
				v-if="selectedSearchItems.length > 0"
				placeholder="Add to project"
				class="p-button dropdown-button"
				:is-dropdown-left-aligned="false"
				:options="projectOptions"
				option-label="name"
				v-on:change="addAssetsToProject"
			/>
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue';
import { isDataset, isModel } from '@/utils/data-util';
import { ResultType } from '@/types/common';
import { AssetType } from '@/types/Types';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import { useRouter } from 'vue-router';
import { useProjects } from '@/composables/project';

const router = useRouter();

const props = defineProps({
	selectedSearchItems: {
		type: Array as PropType<ResultType[]>,
		required: true
	}
});

const emit = defineEmits(['close', 'clear-selected']);

const projectOptions = computed(() => useProjects().allProjects.value?.map((p) => ({ name: p.name, id: p.id })));

const addResourcesToProject = async (projectId: string) => {
	// send selected items to the store
	props.selectedSearchItems.forEach(async (selectedItem) => {
		if (isModel(selectedItem)) {
			// FIXME: handle cases where assets is already added to the project
			const modelId = selectedItem.id;
			// then, link and store in the project assets
			const assetsType = AssetType.Model;
			await useProjects().addAsset(assetsType, modelId, projectId);
		}
		if (isDataset(selectedItem)) {
			// FIXME: handle cases where assets is already added to the project
			const datasetId = selectedItem.id;
			// then, link and store in the project assets
			const assetsType = AssetType.Dataset;
			if (datasetId) {
				await useProjects().addAsset(assetsType, datasetId, projectId);
			}
		}
	});
};

const addAssetsToProject = async (projectOption) => {
	if (props.selectedSearchItems.length === 0) return;

	const projectId = projectOption.value.id ?? useProjects().activeProject.value?.id;
	addResourcesToProject(projectId);

	emit('close');
	router.push(`/projects/${projectId}`);
};
</script>

<style scoped>
.invalid-project {
	background-color: gray;
	cursor: not-allowed;
}

:deep(.p-dropdown .p-dropdown-label.p-placeholder) {
	display: contents;
	color: white;
	font-size: small;
}

:deep .p-dropdown .p-dropdown-trigger {
	color: white;
}

.p-button.p-button-secondary {
	height: 3rem;
	margin-right: 16px;
}
.dropdown-button {
	width: 156px;
	height: 3rem;
	border-radius: var(--border-radius);
	gap: 16px;
}

.dropdown-button:hover {
	background-color: var(--primary-color-dark);
}

.add-selected-buttons {
	display: flex;
	flex-direction: row;
}

.add-selected-buttons button {
	margin-bottom: 5px;
	padding-top: 4px;
	padding-bottom: 4px;
}

.breakdown-pane-container {
	min-height: 0;
	display: flex;
	flex-direction: column;
}

.cart-item {
	border-bottom: 1px solid var(--surface-ground);
}

i {
	padding: 0.2rem;
	border-radius: var(--border-radius);
}

i:hover {
	cursor: pointer;
	background-color: hsla(0, 0%, 0%, 0.1);
	background-color: hsla(0, 0%, 0%, 0.1);
}
</style>
