<template>
	<ArtifactList
		:artifacts="models"
		:selected-artifact-ids="modelIds"
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
import { useTabStore } from '@/stores/tabs';
import { ModelProps } from '@/components/models/Model.vue';
import { Tab } from '@/types/common';

const router = useRouter();
const resourcesStore = useResourcesStore();
const tabStore = useTabStore();

const modelIds = ref<string[] | undefined>([]);
const models = ref<Model[]>([]);
const tabContext = `model${resourcesStore.activeProject?.id}`;

// @ts-ignore
// eslint-disable-next-line @typescript-eslint/no-unused-vars
tabStore.$subscribe((mutation, state) => {
	modelIds.value = state.tabMap.get(tabContext)?.map((tab) => {
		const tabProps = tab.props as ModelProps;
		return tabProps.modelId;
	});
});

const openModelPage = async (id: string | number, name: string) => {
	// if the current route has the same model id as the one we want to open
	// don't push a new route since the view would not re-render
	// just open a new tab by adding it to the tab store instead
	if (
		router.currentRoute.value.params.modelId === id.toString() &&
		!modelIds.value?.includes(id.toString())
	) {
		const newTab = {
			name,
			props: {
				modelId: id.toString()
			}
		} as Tab;
		tabStore.addTab(tabContext, newTab);
	} else {
		// pass this model id as param
		router.push({
			name: RouteName.ModelRoute,
			params: { projectId: resourcesStore.activeProject?.id, modelId: id }
		});
	}
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
	const openTabs = tabStore.getTabs(tabContext);
	if (openTabs) {
		const tabIndexToRemove = openTabs.findIndex((tab) => {
			const tabProps = tab.props as ModelProps;
			return tabProps.modelId.toString() === id.toString();
		});
		tabStore.removeTab(tabContext, tabIndexToRemove);
	}
};

onMounted(() => {
	// get the list of models associated with this project and display them
	const modelsInCurrentProject = resourcesStore.activeProjectAssets?.models;
	if (modelsInCurrentProject) {
		models.value = modelsInCurrentProject;
	}
	// set active selections from tab store
	modelIds.value = tabStore.getTabs(tabContext)?.map((tab) => {
		const tabProps = tab.props as ModelProps;
		return tabProps.modelId;
	});
});
</script>
