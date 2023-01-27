<template>
	<div class="breakdown-pane-container">
		<div class="selected-title">{{ selectedSearchItems.length }} selected</div>
		<div class="add-selected-buttons">
			<Button
				@click="addAssetsToProject()"
				:class="{ 'invalid-project': !validProject || selectedSearchItems.length === 0 }"
				label="Add to current project"
			/>
			<dropdown-button
				v-if="selectedSearchItems.length > 0"
				:inner-button-label="'Add to another project'"
				:is-dropdown-left-aligned="false"
				:items="projectsNames"
				@item-selected="addAssetsToProject"
			/>
		</div>
		<ul>
			<li v-for="(asset, idx) in selectedSearchItems" class="cart-item" :key="idx">
				<asset-card
					:asset="(asset as XDDArticle & Model & Dataset)"
					:resourceType="(getType(asset) as ResourceType)"
				>
					<button type="button" @click.stop="(e) => toggleContextMenu(e, idx)">
						<i class="pi pi-ellipsis-v" />
					</button>
					<Menu ref="contextMenu" :model="getMenuItemsForItem(asset)" :popup="true" />
				</asset-card>
			</li>
		</ul>
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, PropType, ref } from 'vue';
import { isDataset, isModel, isXDDArticle } from '@/utils/data-util';
import { ResourceType, ResultType } from '@/types/common';
import { Model } from '@/types/Model';
import { PublicationAsset, XDDArticle } from '@/types/XDD';
import useResourcesStore from '@/stores/resources';
import { Project, ProjectAssetTypes } from '@/types/Project';
import DropdownButton from '@/components/widgets/dropdown-button.vue';
import * as ProjectService from '@/services/project';
import { addPublication } from '@/services/external';
import { Dataset } from '@/types/Dataset';
import { useRouter } from 'vue-router';
import AssetCard from '@/components/data-explorer/asset-card.vue';
import Button from 'primevue/button';
import Menu from 'primevue/menu';

const router = useRouter();

const props = defineProps({
	selectedSearchItems: {
		type: Array as PropType<ResultType[]>,
		required: true
	}
});

const emit = defineEmits([
	'close',
	'toggle-data-item-selected',
	'find-related-content',
	'find-similar-content'
]);
const resources = useResourcesStore();

const contextMenu = ref();
const validProject = computed(() => resources.activeProject);

const projectsList = ref<Project[]>([]);
const projectsNames = computed(() => projectsList.value.map((p) => p.name));

const getMenuItemsForItem = (item: ResultType) => [
	{
		label: 'Remove',
		command: () => emit('toggle-data-item-selected', { item, type: 'selected' })
	},
	{
		label: 'Find Related Content',
		command: () => emit('find-related-content', { item, type: 'selected' })
	},
	{
		label: 'Find Similar Content', // only for publications
		command: () => emit('find-similar-content', { item, type: 'selected' })
	}
];

const getType = (item: ResultType) => {
	if (isModel(item)) {
		return (item as Model).type;
	}
	if (isDataset(item)) {
		return (item as Dataset).type;
	}
	if (isXDDArticle(item)) {
		return ResourceType.XDD;
	}
	return ResourceType.ALL;
};

const addResourcesToProject = async (projectId: string) => {
	// send selected items to the store
	props.selectedSearchItems.forEach(async (selectedItem) => {
		if (isXDDArticle(selectedItem)) {
			const body: PublicationAsset = {
				xdd_uri: (selectedItem as XDDArticle).gddId,
				title: (selectedItem as XDDArticle).title
			};

			// FIXME: handle cases where assets is already added to the project

			// first, insert into the proper table/collection
			const res = await addPublication(body);
			if (res) {
				const publicationId = res.id;

				// then, link and store in the project assets
				const assetsType = ProjectAssetTypes.PUBLICATIONS;
				await ProjectService.addAsset(projectId, assetsType, publicationId);

				// update local copy of project assets
				validProject.value?.assets.publications.push(publicationId);
				resources.activeProjectAssets?.publications.push(body);
			}
		}
		if (isModel(selectedItem)) {
			// FIXME: handle cases where assets is already added to the project
			const modelId = selectedItem.id;
			// then, link and store in the project assets
			const assetsType = ProjectAssetTypes.MODELS;
			await ProjectService.addAsset(projectId, assetsType, modelId);

			// update local copy of project assets
			validProject.value?.assets.models.push(modelId);
			resources.activeProjectAssets?.[ProjectAssetTypes.MODELS].push(selectedItem);
		}
		if (isDataset(selectedItem)) {
			// FIXME: handle cases where assets is already added to the project
			const datasetId = selectedItem.id;
			// then, link and store in the project assets
			const assetsType = ProjectAssetTypes.DATASETS;
			await ProjectService.addAsset(projectId, assetsType, datasetId);

			// update local copy of project assets
			validProject.value?.assets.datasets.push(datasetId);
			resources.activeProjectAssets?.[ProjectAssetTypes.DATASETS].push(selectedItem);
		}
	});
};

const addAssetsToProject = async (projectName?: string) => {
	if (props.selectedSearchItems.length === 0) return;

	let projectId = '';
	if (projectName !== undefined && typeof projectName === 'string') {
		const project = projectsList.value.find((p) => p.name === projectName);
		projectId = project?.id as string;
	} else {
		if (!validProject.value) return;
		projectId = validProject.value.id;
	}

	addResourcesToProject(projectId);

	emit('close');
	router.push(`/projects/${projectId}`);
};

onMounted(async () => {
	const all = await ProjectService.getAll();
	if (all !== null) {
		projectsList.value = all;
	}
});

const toggleContextMenu = (event, idx: number) => {
	contextMenu.value[idx].toggle(event);
};
</script>

<style scoped>
.invalid-project {
	background-color: gray;
	cursor: not-allowed;
}

.selected-title {
	margin-bottom: 5px;
	font-size: larger;
	text-align: center;
	font-weight: bold;
	color: var(--primary-color);
}

.add-selected-buttons {
	display: flex;
	flex-direction: column;
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

button {
	border: none;
	background-color: transparent;
	height: min-content;
	padding: 0;
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
