<template>
	<div class="breakdown-pane-container">
		<div class="selected-title">{{ selectedSearchItems.length }} selected</div>
		<div class="add-to-title">Add to:</div>
		<div class="add-selected-buttons">
			<Button
				action
				@click="addAssetsToProject"
				:class="{ 'invalid-project': !validProject || selectedSearchItems.length === 0 }"
				>Current Project</Button
			>
			<dropdown-button
				v-if="selectedSearchItems.length > 0"
				:inner-button-label="'Other Project'"
				:is-dropdown-left-aligned="false"
				:items="projectsNames"
				@item-selected="addAssetsToProject"
			/>
			<Button v-else action :class="{ 'invalid-project': selectedSearchItems.length === 0 }"
				>Other Project</Button
			>
		</div>
		<div class="selected-items-container">
			<div v-for="(item, indx) in selectedSearchItems" class="selected-item" :key="`item-${indx}`">
				<div class="item-header">
					<div class="title-and-checkbox">
						<span v-show="isSelected(item)"><i class="fa-lg fa-regular fa-square-check"></i></span>
						<span v-show="!isSelected(item)"><i class="fa-lg fa-regular fa-square"></i></span>
						<div class="item-title" :title="getTitle(item)">{{ formatTitle(item) }}</div>
					</div>
					<i
						:class="getResourceTypeIcon(getType(item))"
						style="margin-left: 4px; margin-right: 4px"
					></i>
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
import { getResourceTypeIcon, isModel, isXDDArticle } from '@/utils/data-util';
import MultilineDescription from '@/components/widgets/multiline-description.vue';
import { ResourceType, ResultType } from '@/types/common';
import { Model } from '@/types/Model';
import { XDDArticle } from '@/types/XDD';
import useResourcesStore from '@/stores/resources';
import API from '@/api/api';
import { Project } from '@/types/Project';
import DropdownButton from '@/components/widgets/dropdown-button.vue';
import * as ProjectService from '@/services/project';

const props = defineProps({
	selectedSearchItems: {
		type: Array as PropType<ResultType[]>,
		required: true
	}
});

const emit = defineEmits(['close']);
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

// FIXME: consider refactoring as a util function
const isSelected = (item: ResultType) =>
	props.selectedSearchItems.find((searchItem) => {
		if (isModel(item)) {
			const itemAsModel = item as Model;
			const searchItemAsModel = searchItem as Model;
			return searchItemAsModel.id === itemAsModel.id;
		}
		if (isXDDArticle(item)) {
			const itemAsArticle = item as XDDArticle;
			const searchItemAsArticle = searchItem as XDDArticle;
			return searchItemAsArticle.title === itemAsArticle.title;
		}
		return false;
	});

const getType = (item: ResultType) => {
	if (isModel(item)) {
		return (item as Model).type;
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
			const body = {
				xdd_uri: (selectedItem as XDDArticle).gddid
			};

			// FIXME: handle cases where assets is already added to the project

			// first, insert into the proper table/collection
			const res = await API.post('/external/publications', body);
			const publicationId = res.data.id;

			// then, link and store in the project assets
			const assetsType = 'publications';
			const url = `/projects/${projectId}/assets/${assetsType}/${publicationId}`;
			await API.post(url);

			// update local copy of project assets
			validProject.value?.assets.publications.push(publicationId);
		}
		// FIXME: add similar code for inserting other types of resources
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

.add-to-title {
	font-weight: 500;
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
	margin: 2px;
	display: flex;
	flex-direction: column;
	overflow-y: auto;
}

.selected-items-container .selected-item {
	border-style: solid;
	border-width: 2px;
	border-color: lightgray;
	padding: 3px;
}

.selected-items-container .item-header {
	display: flex;
	flex-direction: row;
	justify-content: space-between;
}

.selected-items-container .item-header .title-and-checkbox {
	display: flex;
}

.selected-items-container .item-header .title-and-checkbox .item-title {
	font-weight: 500;
	margin-bottom: 5px;
	margin-left: 4px;
}

:deep(.dropdown-btn) {
	cursor: pointer;
	background-color: var(--un-color-accent);
	border-color: var(--un-color-accent-dark);
	border-width: 0px;
	width: 100% !important;
	max-width: 100% !important;
}
</style>
