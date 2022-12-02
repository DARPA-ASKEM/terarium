<template>
	<div class="model-list-container">
		<Button action @click="goToTheia">
			<IconScript16 />
			New model from code
		</Button>
		<div
			v-for="mId in models"
			:key="mId"
			class="model-link"
			:class="{ active: mId === modelId }"
			@click="openModelPage(mId)"
		>
			<span class="model-view-icon">
				<IconMachineLearningModel32 />
			</span>
			<span class="model-title">
				{{ mId }}
			</span>
			<span class="model-delete-btn" @click.stop="removeModel(mId)">
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
import IconMachineLearningModel32 from '@carbon/icons-vue/es/machine-learning-model/32';
import useResourcesStore from '@/stores/resources';
import { onMounted, ref } from 'vue';
import IconClose32 from '@carbon/icons-vue/es/close/32';
import { deleteAsset } from '@/services/project';
import { MODELS } from '@/types/Project';
// import { getModel } from '@/services/model';
import { RouteName } from '@/router';

const router = useRouter();
const resourcesStore = useResourcesStore();

const goToTheia = () => router.push('/theia');

const modelId = ref('');
const models = ref<string[]>([]);

const openModelPage = async (mId: string) => {
	// const publicationDetails = await getModel(mId);
	// pass this model id as param
	// if (publicationDetails) {
	modelId.value = mId; // track selection
	router.push({
		name: RouteName.ModelRoute,
		params: { projectId: resourcesStore.activeProject?.id, modelId: mId }
	});
	// }
};

const removeModel = async (mId: string) => {
	// remove the model from the project assets
	if (resourcesStore.activeProject) {
		const assetsType = MODELS;
		deleteAsset(resourcesStore.activeProject.id, assetsType, mId);
		// remove also from the local cache
		resourcesStore.activeProject.assets[MODELS] = resourcesStore.activeProject.assets[
			MODELS
		].filter((a) => a !== mId);
		models.value = resourcesStore.activeProject.assets[MODELS];
	}

	// if the user deleted the currently selected model, then clear its content from the view
	if (mId === modelId.value) {
		// clear the model ID as a URL param
		router.push({
			name: RouteName.ModelRoute,
			params: { projectId: resourcesStore.activeProject?.id, modelId: '' }
		});
	}
};

onMounted(() => {
	// get the list of models associated with this project and display them
	const modelsInCurrentProject = resourcesStore.activeProject?.assets.models;
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
	overflow-y: auto;
}

.model-link {
	padding: 0.5rem;
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
