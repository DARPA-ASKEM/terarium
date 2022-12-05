<template>
	<div class="model-list-container">
		<Button action @click="goToTheia">
			<IconScript16 />
			New model from code
		</Button>
		<div
			v-for="model in models"
			:key="model.id"
			class="model-link"
			:class="{ active: model.id === modelId }"
			@click="openModelPage(model)"
		>
			<span class="model-title">
				{{ model.name }}
			</span>
			<span class="model-delete-btn" @click.stop="removeModel(model)">
				<IconClose32 />
			</span>
		</div>
	</div>
</template>

<script setup lang="ts">
/**
 * Model Sidebar Panel
 * Display a file tree like structure of models available in the current Project.
 */

import { useRouter } from 'vue-router';
import Button from '@/components/Button.vue';
import IconScript16 from '@carbon/icons-vue/es/script/16';
import useResourcesStore from '@/stores/resources';
import { onMounted, ref } from 'vue';
import IconClose32 from '@carbon/icons-vue/es/close/32';
import { deleteAsset } from '@/services/project';
import { MODELS } from '@/types/Project';
import { RouteName } from '@/router';
import { Model } from '@/types/Model';

const router = useRouter();
const resourcesStore = useResourcesStore();

const goToTheia = () => router.push('/theia');

const modelId = ref<string | number>('');
const models = ref<Model[]>([]);

const openModelPage = async (model: Model) => {
	// pass this model id as param
	modelId.value = model.id; // track selection
	router.push({
		name: RouteName.ModelRoute,
		params: { projectId: resourcesStore.activeProject?.id, modelId: model.id }
	});
};

const removeModel = async (model: Model) => {
	// remove the model from the project assets
	if (resourcesStore.activeProject && resourcesStore.activeProjectAssets) {
		const assetsType = MODELS;
		deleteAsset(resourcesStore.activeProject.id, assetsType, model.id);
		// remove also from the local cache
		resourcesStore.activeProject.assets[MODELS] = resourcesStore.activeProject.assets[
			MODELS
		].filter((modId) => modId !== model.id);
		resourcesStore.activeProjectAssets[MODELS] = resourcesStore.activeProjectAssets[MODELS].filter(
			(a) => a.id !== model.id
		);
		models.value = resourcesStore.activeProjectAssets[MODELS];
	}

	// if the user deleted the currently selected model, then clear its content from the view
	if (model.id === modelId.value) {
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

<style scoped>
.model-list-container {
	overflow-y: auto;
	margin-top: 1rem;
	height: 100%;
}

.model-link {
	cursor: pointer;
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
}

.model-link:hover:not(.active) {
	background-color: var(--un-color-body-surface-secondary);
}

.active {
	font-size: var(--un-font-body);
	background-color: var(--un-color-body-surface-background);
}

.model-view-icon {
	padding-right: 0.5rem;
}

.model-delete-btn {
	color: var(--un-color-body-text-disabled);
}

.model-delete-btn:hover {
	/* color: var(--un-color-body-text-primary); */
	color: red;
}

span {
	display: inline-flex;
	align-items: center;
}

.model-title {
	text-overflow: ellipsis;
	overflow: hidden;
	white-space: nowrap;
	display: inline;
}
</style>
