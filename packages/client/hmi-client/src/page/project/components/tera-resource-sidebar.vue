<template>
	<nav>
		<header>
			<Button
				icon="pi pi-trash"
				:disabled="!route.params.assetId || route.params.assetId === ''"
				v-tooltip="`Remove ${route.params.assetName}`"
				class="p-button-icon-only p-button-text p-button-rounded"
				@click="isConfirmRemovalModalVisible = true"
			/>
		</header>
		<tree
			v-if="!isEmpty(resources)"
			:value="resources"
			selectionMode="single"
			v-on:node-select="emit('open-asset', $event.asset)"
		>
			<template #default="slotProps">
				<span :active="route.params.assetName === slotProps.node.label">
					{{ slotProps.node.label }}
					<Chip :label="slotProps.node.asset.assetType" />
				</span>
			</template>
		</tree>
		<div v-else class="loading-spinner">
			<div><i class="pi pi-spin pi-spinner" /></div>
		</div>
		<Teleport to="body">
			<modal
				v-if="isConfirmRemovalModalVisible"
				@modal-mask-clicked="isConfirmRemovalModalVisible = false"
			>
				<template #header>
					<h5>
						Are you sure you want remove "{{ route.params.assetName }}" from {{ project.name }}?
					</h5>
				</template>
				<template #footer>
					<Button label="Yes" @click="removeAsset()" />
					<Button
						label="No"
						class="p-button-secondary"
						@click="isConfirmRemovalModalVisible = false"
					/>
				</template>
			</modal>
		</Teleport>
	</nav>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { logger } from '@/utils/logger';
import { isEmpty } from 'lodash';
import { Tab } from '@/types/common';
import Modal from '@/components/widgets/Modal.vue';
import { deleteAsset } from '@/services/project';
import useResourcesStore from '@/stores/resources';
import { useRoute } from 'vue-router';
import Tree from 'primevue/tree';
import Button from 'primevue/button';
import Chip from 'primevue/chip';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import { DocumentAsset } from '@/types/Types';
import { Model } from '@/types/Model';
import { Dataset } from '@/types/Dataset';

const props = defineProps<{
	project: IProject;
	tabs: Tab[];
}>();

const emit = defineEmits(['open-asset', 'close-tab']);

const route = useRoute();
const resourcesStore = useResourcesStore();

const isConfirmRemovalModalVisible = ref(false);

const resources = computed(() => {
	const storedAssets = resourcesStore.activeProjectAssets ?? [];
	const projectAssetTypes = Object.keys(storedAssets);
	const resourceTreeNodes: any[] = [];

	if (!isEmpty(storedAssets)) {
		resourceTreeNodes.push({
			key: 'Overview',
			label: 'Overview',
			asset: {
				assetName: 'Overview',
				assetType: 'overview',
				assetId: ''
			},
			selectable: true
		});

		// Basic new code file (temp)
		resourceTreeNodes.push({
			key: 'New file',
			label: 'New file',
			asset: {
				assetName: 'New file',
				assetType: ProjectAssetTypes.CODE,
				assetId: ''
			},
			selectable: true
		});

		for (let i = 0; i < projectAssetTypes.length; i++) {
			if (projectAssetTypes[i] == null || storedAssets[projectAssetTypes[i]] == null) continue;
			const assets: (DocumentAsset & Model & Dataset)[] =
				Object.values(storedAssets[projectAssetTypes[i]]) ?? [];

			for (let j = 0; j < assets.length; j++) {
				resourceTreeNodes.push({
					key: j.toString(),
					label: assets[j]?.name || assets[j]?.title || assets[j]?.id,
					asset: {
						// Matches Tab type
						assetName: assets[j]?.name || assets[j]?.title || assets[j]?.id,
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

function removeAsset(
	assetToRemove: Tab = {
		assetName: route.params.assetName as string,
		assetId: route.params.assetId as string,
		assetType: route.params.assetType as ProjectAssetTypes | 'overview'
	}
) {
	const { assetId, assetType } = assetToRemove;

	if (!assetType || !resourcesStore.activeProject || !resourcesStore.activeProjectAssets) {
		return; // See about removing this check somehow, it may be best to pass the resourceStore from App.vue
	}

	const asset = resourcesStore.activeProjectAssets[assetType].find((a) =>
		assetType === ProjectAssetTypes.DOCUMENTS ? a.xdd_uri === assetId : a.id.toString() === assetId
	);

	if (!asset) {
		logger.error('Failed to remove asset');
		return;
	}

	// Remove asset from resource storage
	deleteAsset(resourcesStore.activeProject.id, assetType, asset.id);
	resourcesStore.activeProjectAssets[assetType] = resourcesStore.activeProjectAssets[
		assetType
	].filter(({ id }) => id !== asset.id);
	// Remove also from the local cache
	resourcesStore.activeProject.assets[assetType] = resourcesStore.activeProject.assets[
		assetType
	].filter((id: string | number) => id !== asset.id);

	// If asset to be removed is open in a tab, close it
	const tabIndex = props.tabs.findIndex((tab: Tab) => tab === assetToRemove);
	if (tabIndex) emit('close-tab', tabIndex);

	isConfirmRemovalModalVisible.value = false;
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
