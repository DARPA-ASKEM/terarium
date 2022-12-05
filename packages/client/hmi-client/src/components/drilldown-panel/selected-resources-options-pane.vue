<template>
	<div class="breakdown-pane-container">
		<div class="selected-title">{{ selectedSearchItems.length }} selected</div>
		<div class="add-selected-buttons">
			<Button
				action
				@click="addAssetsToProject"
				:class="{ 'invalid-project': !validProject || selectedSearchItems.length === 0 }"
				>Add to current project</Button
			>
			<dropdown-button
				v-if="selectedSearchItems.length > 0"
				:inner-button-label="'Add to another project'"
				:is-dropdown-left-aligned="false"
				:items="projectsNames"
				@item-selected="addAssetsToProject"
			/>
		</div>
		<div class="selected-items-container">
			<div v-for="(item, indx) in selectedSearchItems" class="selected-item" :key="`item-${indx}`">
				<div class="item-header">
					<component class="icon" :is="getResourceTypeIcon(getType(item))" />
					<div class="item-title" :title="getTitle(item)">
						{{ formatTitle(item) }}
					</div>
					<div class="item-delete-btn" @click.stop="removeItem(item)">
						<IconClose16 />
					</div>
				</div>
				<div class="content">
					<multiline-description :text="formatDescription(item)" />
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, PropType, ref } from 'vue';
import Button from '@/components/Button.vue';
import { getResourceTypeIcon, isDataset, isModel, isXDDArticle } from '@/utils/data-util';
import MultilineDescription from '@/components/widgets/multiline-description.vue';
import { ResourceType, ResultType } from '@/types/common';
import { Model } from '@/types/Model';
import { PublicationAsset, XDDArticle } from '@/types/XDD';
import useResourcesStore from '@/stores/resources';
import { DATASETS, MODELS, Project, PUBLICATIONS } from '@/types/Project';
import DropdownButton from '@/components/widgets/dropdown-button.vue';
import * as ProjectService from '@/services/project';
import { addPublication } from '@/services/external';
import { Dataset } from '@/types/Dataset';
import IconClose16 from '@carbon/icons-vue/es/close/16';

const props = defineProps({
	selectedSearchItems: {
		type: Array as PropType<ResultType[]>,
		required: true
	}
});

const emit = defineEmits(['close', 'remove-item']);
const resources = useResourcesStore();

const validProject = computed(() => resources.activeProject);

const projectsList = ref<Project[]>([]);
const projectsNames = computed(() => projectsList.value.map((p) => p.name));

const getTitle = (item: ResultType) => (item as Model).name || (item as XDDArticle).title;

const formatTitle = (item: ResultType) => {
	const maxSize = 36;
	const itemTitle = getTitle(item);
	return itemTitle.length < maxSize ? itemTitle : `${itemTitle.substring(0, maxSize)}...`;
};

const formatDescription = (item: ResultType) => {
	const maxSize = 120;
	let itemDesc = '[No Desc]';
	if (isModel(item)) {
		itemDesc = (item as Model).description || itemDesc;
	}
	if (isDataset(item)) {
		itemDesc = (item as Dataset).description || itemDesc;
	}
	if (isXDDArticle(item)) {
		itemDesc =
			((item as XDDArticle).abstractText && typeof (item as XDDArticle).abstractText === 'string'
				? (item as XDDArticle).abstractText
				: false) ||
			(item as XDDArticle).journal ||
			(item as XDDArticle).publisher ||
			itemDesc;
	}
	return itemDesc.length < maxSize ? itemDesc : `${itemDesc.substring(0, maxSize)}...`;
};

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
				xdd_uri: (selectedItem as XDDArticle).gddid,
				title: (selectedItem as XDDArticle).title
			};

			// FIXME: handle cases where assets is already added to the project

			// first, insert into the proper table/collection
			const res = await addPublication(body);
			if (res) {
				const publicationId = res.id;

				// then, link and store in the project assets
				const assetsType = PUBLICATIONS;
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
			const assetsType = MODELS;
			await ProjectService.addAsset(projectId, assetsType, modelId);

			// update local copy of project assets
			validProject.value?.assets.models.push(modelId);
			resources.activeProjectAssets?.[MODELS].push(selectedItem);
		}
		if (isDataset(selectedItem)) {
			// FIXME: handle cases where assets is already added to the project
			const datasetId = selectedItem.id;
			// then, link and store in the project assets
			const assetsType = DATASETS;
			await ProjectService.addAsset(projectId, assetsType, datasetId);

			// update local copy of project assets
			validProject.value?.assets.datasets.push(datasetId);
			resources.activeProjectAssets?.[DATASETS].push(selectedItem);
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
};

const removeItem = (item: ResultType) => {
	emit('remove-item', item);
};

onMounted(async () => {
	const all = await ProjectService.getAll();
	if (all !== null) {
		projectsList.value = all;
	}
});
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
	color: var(--un-color-accent);
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
	margin-bottom: 40px;
	min-height: 0;
	display: flex;
	flex-direction: column;
}

.selected-items-container {
	display: flex;
	flex-direction: column;
	overflow-y: auto;
	margin-top: 10px;
}

.selected-items-container .selected-item {
	padding: 5px;
	background: var(--un-color-white);
	margin-top: 1px;
}

.selected-items-container .item-header {
	display: flex;
	flex-direction: row;
	justify-content: space-between;
}

.selected-items-container .item-header .item-title {
	font-weight: 500;
}

.selected-items-container .item-header .icon {
	margin-right: 5px;
}

.selected-items-container .selected-item .content {
	margin-left: 22px;
}

.item-delete-btn {
	color: var(--un-color-body-text-disabled);
	cursor: pointer;
}

.item-delete-btn:hover {
	/* color: var(--un-color-body-text-primary); */
	color: red;
}
</style>
