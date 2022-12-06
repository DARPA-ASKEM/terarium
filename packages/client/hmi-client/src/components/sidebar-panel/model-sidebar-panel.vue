<template>
	<ArtifactList
		:artifacts="models"
		:selected-artifact-id="modelId"
		@artifact-clicked="openModelPage"
		@remove-artifact="removeModel"
	/>
</template>

<script setup lang="ts">
/**
 * Model Sidebar Panel
 * Display a file tree like structure of models available in the current Project.
 */

import { useRouter } from 'vue-router';
import useResourcesStore from '@/stores/resources';
import { onMounted, ref } from 'vue';
import { deleteAsset } from '@/services/project';
import { MODELS } from '@/types/Project';
import { RouteName } from '@/router/routes';
import { Model } from '@/types/Model';
import ArtifactList from '@/components/sidebar-panel/artifact-list.vue';

const router = useRouter();
const resourcesStore = useResourcesStore();

const modelId = ref<string | number>('');
const models = ref<Model[]>([]);

const openModelPage = async (id: string | number) => {
	// pass this model id as param
	modelId.value = id; // track selection
	router.push({
		name: RouteName.ModelRoute,
		params: { projectId: resourcesStore.activeProject?.id, modelId: id }
	});
};

const removeModel = async (id: string | number) => {
	// remove the model from the project assets
	if (resourcesStore.activeProject && resourcesStore.activeProjectAssets) {
		const assetsType = MODELS;
		deleteAsset(resourcesStore.activeProject.id, assetsType, id);
		// remove also from the local cache
		resourcesStore.activeProject.assets[MODELS] = resourcesStore.activeProject.assets[
			MODELS
		].filter((modId) => modId !== id);
		resourcesStore.activeProjectAssets[MODELS] = resourcesStore.activeProjectAssets[MODELS].filter(
			(a) => a.id !== id
		);
		models.value = resourcesStore.activeProjectAssets[MODELS];
	}

	// if the user deleted the currently selected model, then clear its content from the view
	if (id === modelId.value) {
		// clear the model ID as a URL param
		router.push({
			name: RouteName.ModelRoute,
			params: { projectId: resourcesStore.activeProject?.id, modelId: '' }
		});
	}
};

onMounted(() => {
	// get the list of models associated with this project and display them
	const modelsInCurrentProject = resourcesStore.activeProjectAssets?.models;
	if (modelsInCurrentProject) {
		models.value = modelsInCurrentProject;
	}
});
</script>
