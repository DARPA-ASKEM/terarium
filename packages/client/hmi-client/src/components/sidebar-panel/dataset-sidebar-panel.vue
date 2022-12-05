<template>
	<div class="dataset-list-container">
		<div
			v-for="dataset in datasets"
			:key="dataset.id"
			class="dataset-link"
			:class="{ active: dataset.id === datasetId }"
			@click="openDatasetPage(dataset)"
		>
			<span class="dataset-title">
				{{ dataset.name }}
			</span>
			<span class="dataset-delete-btn" @click.stop="removeDataset(dataset)">
				<IconClose32 />
			</span>
		</div>
	</div>
</template>

<script setup lang="ts">
/**
 * Dataset Sidebar Panel
 * Display a file tree like structure of datasets available in the current Project.
 */

import { useRouter } from 'vue-router';
import useResourcesStore from '@/stores/resources';
import { onMounted, ref } from 'vue';
import IconClose32 from '@carbon/icons-vue/es/close/32';
import { deleteAsset } from '@/services/project';
import { DATASETS } from '@/types/Project';
import { RouteName } from '@/router';
import { Dataset } from '@/types/Dataset';

const router = useRouter();
const resourcesStore = useResourcesStore();

const datasetId = ref<string | number>('');
const datasets = ref<Dataset[]>([]);

const openDatasetPage = async (dataset: Dataset) => {
	// pass this dataset id as param
	datasetId.value = dataset.id; // track selection
	router.push({
		name: RouteName.DatasetRoute,
		params: { projectId: resourcesStore.activeProject?.id, datasetId: dataset.id }
	});
};

const removeDataset = async (dataset: Dataset) => {
	// remove the dataset from the project assets
	if (resourcesStore.activeProject && resourcesStore.activeProjectAssets) {
		const assetsType = DATASETS;
		deleteAsset(resourcesStore.activeProject.id, assetsType, dataset.id);
		// remove also from the local cache
		resourcesStore.activeProject.assets[DATASETS] = resourcesStore.activeProject.assets[
			DATASETS
		].filter((modId) => modId !== dataset.id);
		resourcesStore.activeProjectAssets[DATASETS] = resourcesStore.activeProjectAssets[
			DATASETS
		].filter((a) => a.id !== dataset.id);
		datasets.value = resourcesStore.activeProjectAssets[DATASETS];
	}

	// if the user deleted the currently selected dataset, then clear its content from the view
	if (dataset.id === datasetId.value) {
		// clear the dataset ID as a URL param
		router.push({
			name: RouteName.DatasetRoute,
			params: { projectId: resourcesStore.activeProject?.id, datasetId: '' }
		});
	}
};

onMounted(() => {
	// get the list of datasets associated with this project and display them
	const datasetsInCurrentProject = resourcesStore.activeProjectAssets?.datasets;
	if (datasetsInCurrentProject) {
		datasets.value = datasetsInCurrentProject;
	}
});
</script>

<style scoped>
.dataset-list-container {
	overflow-y: auto;
	margin-top: 1rem;
	height: 100%;
}

.dataset-link {
	cursor: pointer;
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
}

.dataset-link:hover:not(.active) {
	background-color: var(--un-color-body-surface-secondary);
}

.active {
	font-size: var(--un-font-body);
	background-color: var(--un-color-body-surface-background);
}

.dataset-view-icon {
	padding-right: 0.5rem;
}

.dataset-delete-btn {
	color: var(--un-color-body-text-disabled);
}

.dataset-delete-btn:hover {
	/* color: var(--un-color-body-text-primary); */
	color: red;
}

span {
	display: inline-flex;
	align-items: center;
}

.dataset-title {
	text-overflow: ellipsis;
	overflow: hidden;
	white-space: nowrap;
	display: inline;
}
</style>
