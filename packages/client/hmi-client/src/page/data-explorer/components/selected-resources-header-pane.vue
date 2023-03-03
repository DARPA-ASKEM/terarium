<template>
	<div class="breakdown-pane-container">
		<div class="add-selected-buttons">
			<Button
				v-if="selectedSearchItems.length > 0"
				class="p-button-secondary spacer"
				@click="emit('clear-selected')"
			>
				EMPTY CART
			</Button>

			<dropdown
				v-if="selectedSearchItems.length > 0"
				placeholder="ADD TO PROJECT"
				class="p-button dropdown-button"
				:is-dropdown-left-aligned="false"
				:options="projectsNames"
				v-on:change="addAssetsToProject"
			/>
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, PropType, ref } from 'vue';
import { isDataset, isModel, isDocument } from '@/utils/data-util';
import { ResultType, AssetType } from '@/types/common';
import { DocumentAsset, DocumentType } from '@/types/Document';
import useResourcesStore from '@/stores/resources';
import { IProject } from '@/types/Project';
import dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import * as ProjectService from '@/services/project';
import { addDocuments } from '@/services/external';
import { useRouter } from 'vue-router';

const router = useRouter();

const props = defineProps({
	selectedSearchItems: {
		type: Array as PropType<ResultType[]>,
		required: true
	}
});

const emit = defineEmits(['close', 'clear-selected']);
const resources = useResourcesStore();

const validProject = computed(() => resources.activeProject);

const projectsList = ref<IProject[]>([]);
const projectsNames = computed(() => projectsList.value.map((p) => p.name));

const addResourcesToProject = async (projectId: string) => {
	// send selected items to the store
	props.selectedSearchItems.forEach(async (selectedItem) => {
		if (isDocument(selectedItem)) {
			const body: DocumentAsset = {
				xdd_uri: (selectedItem as DocumentType).gddId,
				title: (selectedItem as DocumentType).title
			};

			// FIXME: handle cases where assets is already added to the project

			// first, insert into the proper table/collection
			const res = await addDocuments(body);
			if (res) {
				const documentId = res.id;

				// then, link and store in the project assets
				const assetsType = AssetType.DOCUMENT;
				await ProjectService.addAsset(projectId, assetsType, documentId);

				// update local copy of project assets
				validProject.value?.assets?.[AssetType.DOCUMENT].push(documentId);
				resources.activeProjectAssets?.[AssetType.DOCUMENT].push(body);
			}
		}
		if (isModel(selectedItem)) {
			// FIXME: handle cases where assets is already added to the project
			const modelId = selectedItem.id;
			// then, link and store in the project assets
			const assetsType = AssetType.MODEL;
			await ProjectService.addAsset(projectId, assetsType, modelId);

			// update local copy of project assets
			validProject.value?.assets.model.push(modelId);
			resources.activeProjectAssets?.[AssetType.MODEL].push(selectedItem);
		}
		if (isDataset(selectedItem)) {
			// FIXME: handle cases where assets is already added to the project
			const datasetId = selectedItem.id;
			// then, link and store in the project assets
			const assetsType = AssetType.DATASET;
			await ProjectService.addAsset(projectId, assetsType, datasetId);

			// update local copy of project assets
			validProject.value?.assets.dataset.push(datasetId);
			resources.activeProjectAssets?.[AssetType.DATASET].push(selectedItem);
		}
	});
};

const addAssetsToProject = async (projectName) => {
	if (props.selectedSearchItems.length === 0) return;

	let projectId = '';
	if (projectName !== undefined && typeof projectName.value === 'string') {
		const project = projectsList.value.find((p) => p.name === projectName.value);
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
	const projects = await ProjectService.getAll();
	if (projects !== null) {
		projectsList.value = projects;
	}
});
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
	border: 1px solid var(--surface-border);
	box-shadow: none;
	font-weight: 600;
	font-size: 14px;
	padding-right: 10px;
	padding-left: 10px;
}

.spacer {
	margin-right: 20px;
}

.dropdown-button {
	width: 156px;
	height: 25px;
	border-radius: 6px;
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

button {
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
