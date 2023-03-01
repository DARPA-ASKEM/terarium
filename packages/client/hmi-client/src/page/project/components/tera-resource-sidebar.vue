<template>
	<nav>
		<header>
			<Button icon="pi pi-file-edit" class="p-button-icon-only p-button-text p-button-rounded" />
			<Button icon="pi pi-folder" class="p-button-icon-only p-button-text p-button-rounded" />
			<Button
				icon="pi pi-sort-amount-down"
				class="p-button-icon-only p-button-text p-button-rounded"
			/>
			<Button icon="pi pi-arrows-v" class="p-button-icon-only p-button-text p-button-rounded" />
			<Button
				icon="pi pi-trash"
				class="p-button-icon-only p-button-text p-button-rounded"
				@click="removeAsset"
			/>
		</header>
		<Tree
			v-if="!isEmpty(resources)"
			:value="resources"
			selectionMode="single"
			v-on:node-select="openAsset"
		>
			<template #default="slotProps">
				<span
					:active="
						route.params.assetId === slotProps.node.data.assetId &&
						route.params.assetType === slotProps.node.data.assetType
					"
				>
					{{ slotProps.node.label }}
					<Chip :label="slotProps.node.data.assetType" />
				</span>
			</template>
		</Tree>
		<div v-else class="loading-spinner">
			<div><i class="pi pi-spin pi-spinner" /></div>
		</div>
	</nav>
</template>

<script setup lang="ts">
import { computed } from 'vue';
// import { logger } from '@/utils/logger';
import { isEmpty } from 'lodash';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import { deleteAsset } from '@/services/project';
import { RouteName } from '@/router/routes';

import useResourcesStore from '@/stores/resources';
import { useRoute, useRouter } from 'vue-router';
import Tree from 'primevue/tree';
import Button from 'primevue/button';
import Chip from 'primevue/chip';
import { DocumentAsset } from '@/types/Document';
import { Model } from '@/types/Model';
import { Dataset } from '@/types/Dataset';

const router = useRouter();
const route = useRoute();
const resourcesStore = useResourcesStore();

defineProps<{
	project: IProject;
}>();

const resources = computed(() => {
	const storedAssets = resourcesStore.activeProjectAssets ?? [];
	const projectAssetTypes = Object.keys(storedAssets);
	const resourceTreeNodes: any[] = [];

	if (!isEmpty(storedAssets)) {
		// Basic new code file (temp)
		resourceTreeNodes.push({
			key: 'New file',
			label: 'New file',
			data: {
				assetType: ProjectAssetTypes.CODE
			},
			selectable: true
		});

		for (let i = 0; i < projectAssetTypes.length; i++) {
			const assets: (DocumentAsset & Model & Dataset)[] =
				Object.values(storedAssets[projectAssetTypes[i]]) ?? [];

			for (let j = 0; j < assets.length; j++) {
				resourceTreeNodes.push({
					key: assets[j]?.name || assets[j]?.title,
					label: assets[j]?.name || assets[j]?.title || assets[j]?.id,
					data: {
						assetType: projectAssetTypes[i],
						assetId:
							projectAssetTypes[i] === ProjectAssetTypes.DOCUMENTS
								? assets[j].xdd_uri
								: assets[j]?.id.toString()
					},
					selectable: true
				});
			}
		}
	}
	return resourceTreeNodes;
});

// Remove an asset - will be adjusted later
function removeAsset() {
	const storedAssets = resourcesStore.activeProjectAssets ?? [];
	const assetId = route.params.assetId;
	const assetType = route.params.assetType as ProjectAssetTypes;

	const asset = storedAssets[assetType].find((a) =>
		assetType === ProjectAssetTypes.DOCUMENTS ? a.xdd_uri === assetId : a.id === assetId
	);

	if (asset === undefined) {
		console.error('Failed to remove asset');
		return;
	}
	// remove the document from the project assets
	if (resourcesStore.activeProject && storedAssets) {
		deleteAsset(resourcesStore.activeProject.id, assetType, asset.id);

		storedAssets[assetType] = storedAssets[assetType].filter(({ id }) => id !== asset.id);

		// Remove also from the local cache - TO DO
		// resourcesStore.activeProject.assets[assetType] =
		// 	resourcesStore.activeProject.assets[assetType].filter(
		// 		(docId) => docId !== asset.id
		// 	);
	}

	// if the user deleted the currently selected asset, then clear its content from the view TO DO
	// if (asset.xdd_uri === documentId.value) {
	// 	router.push('/document'); // clear the doc ID as a URL param
	// }

	// look at model-sidebar-panel.vue in previous versions if you want to see about using the old tab system
}

function openAsset(event: any) {
	router.push({
		name: RouteName.ProjectRoute,
		params: {
			assetName: event.key,
			assetId: event.data.assetId,
			assetType: event.data.assetType
		}
	});
}

// function exportIds() {
// 	logger.info(
// 		'List of xDD _gddid ',
// 		{},
// 		documents.value.map((document) => document)
// 	);
// }
</script>

<style scoped>
nav {
	display: flex;
	flex-direction: column;
	margin: 0.75rem;
	margin-top: 0;
	gap: 1rem;
	min-height: 75%;
}

.p-chip {
	padding: 0 0.5rem;
	border-radius: 0.5rem;
	text-transform: uppercase;
}

.p-tree:deep(.p-treenode-label) {
	text-overflow: ellipsis;
	overflow: hidden;
	white-space: nowrap;
}

.p-tree:deep(.p-treenode-content:has(span[active='true'])),
.p-tree:deep(.p-treenode-content:hover:has(span[active='true'])) {
	background-color: var(--surface-highlight);
}

.loading-spinner {
	display: flex;
	flex: 1;
	justify-content: center;
	align-items: center;
	color: var(--primary-color);
}

.pi-spinner {
	font-size: 4rem;
}
</style>
