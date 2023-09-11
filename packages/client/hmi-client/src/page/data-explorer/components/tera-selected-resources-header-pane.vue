<template>
	<div class="breakdown-pane-container">
		<div class="add-selected-buttons">
			<Button
				v-if="selectedSearchItems.length > 0"
				class="p-button-secondary spacer"
				@click="emit('clear-selected')"
			>
				Remove all
			</Button>

			<dropdown
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
import { isDataset, isModel, isDocument } from '@/utils/data-util';
import { ResultType } from '@/types/common';
import { AssetType, Document, DocumentAsset } from '@/types/Types';
import dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import { addDocuments } from '@/services/external';
import { useRouter } from 'vue-router';
import { useProjects } from '@/composables/project';

const router = useRouter();
const { addAsset, activeProject, allProjects } = useProjects();

const props = defineProps({
	selectedSearchItems: {
		type: Array as PropType<ResultType[]>,
		required: true
	}
});

const emit = defineEmits(['close', 'clear-selected']);

const projectOptions = computed(() => allProjects.value?.map((p) => ({ name: p.name, id: p.id })));

const addResourcesToProject = async (projectId: string) => {
	// send selected items to the store
	props.selectedSearchItems.forEach(async (selectedItem) => {
		if (isDocument(selectedItem)) {
			const body: DocumentAsset = {
				xdd_uri: (selectedItem as Document).gddId,
				title: (selectedItem as Document).title
			};

			// FIXME: handle cases where assets is already added to the project

			// first, insert into the proper table/collection
			const res = await addDocuments(body);
			if (res) {
				const documentId = res.id;

				// then, link and store in the project assets
				const assetsType = AssetType.Publications;
				await addAsset(projectId, assetsType, documentId);
			}
		}
		if (isModel(selectedItem)) {
			// FIXME: handle cases where assets is already added to the project
			const modelId = selectedItem.id;
			// then, link and store in the project assets
			const assetsType = AssetType.Models;
			await addAsset(projectId, assetsType, modelId);
		}
		if (isDataset(selectedItem)) {
			// FIXME: handle cases where assets is already added to the project
			const datasetId = selectedItem.id;
			// then, link and store in the project assets
			const assetsType = AssetType.Datasets;
			if (datasetId) {
				await addAsset(projectId, assetsType, datasetId);
			}
		}
	});
};

const addAssetsToProject = async (projectOption) => {
	if (props.selectedSearchItems.length === 0) return;

	const projectId = projectOption.id ?? activeProject.value?.id;
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

/* TODO: Create a proper secondary outline button in PrimeVue theme */
.p-button.p-button-secondary {
	box-shadow: none;
	color: var(--text-color-subdued);
	background-color: var(--surface-0);
	font-weight: 400;
	font-size: 14px;
	padding-right: 16px;
	padding-left: 16px;
	height: 3rem;
}

.p-button.p-button-secondary:enabled:hover {
	background-color: var(--surface-highlight);
}

.spacer {
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
