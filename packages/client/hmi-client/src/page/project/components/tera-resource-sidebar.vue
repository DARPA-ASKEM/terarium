<template>
	<nav>
		<header>
			<Button
				icon="pi pi-trash"
				:disabled="!openedAssetRoute.assetId"
				v-tooltip="`Remove ${openedAssetRoute.assetName}`"
				class="p-button-icon-only p-button-text p-button-rounded"
				@click="isConfirmRemovalModalVisible = true"
			/>
			<Button
				icon="pi pi-file"
				v-tooltip="`New code file`"
				class="p-button-icon-only p-button-text p-button-rounded"
				@click="
					emit('open-asset', {
						assetName: 'New file',
						assetType: ProjectAssetTypes.CODE,
						assetId: undefined
					})
				"
			/>
		</header>
		<Button
			class="asset-button"
			label="Overview"
			:icon="iconClassname('overview')"
			plain
			text
			size="small"
			@click="emit('open-asset', { type: 'overview' })"
		/>
		<Accordion v-if="!isEmpty(project?.assets)" :multiple="true">
			<AccordionTab
				v-for="(assets, type) in project.assets"
				:key="type"
				:header="capitalize(type)"
				:disabled="isEmpty(assets)"
			>
				<Button
					v-for="asset in assets"
					:key="asset.id"
					:icon="iconClassname(type)"
					:label="(asset?.name || asset?.title || asset?.id).toString()"
					:title="asset?.name || asset?.title"
					class="asset-button"
					plain
					text
					size="small"
					@click="emit('open-asset', asset)"
				/>
			</AccordionTab>
		</Accordion>
		<div v-else class="loading-spinner">
			<div><i class="pi pi-spin pi-spinner" /></div>
		</div>
		<Teleport to="body">
			<modal
				v-if="isConfirmRemovalModalVisible"
				@modal-mask-clicked="isConfirmRemovalModalVisible = false"
				class="remove-modal"
			>
				<template #header>
					<h4>Confirm remove</h4>
				</template>
				<template #default>
					<p>
						Removing <em>{{ openedAssetRoute.assetName }}</em> will permanently remove it from
						{{ project.name }}.
					</p>
				</template>
				<template #footer>
					<Button label="Remove" class="p-button-danger" @click="removeAsset()" />
					<Button
						label="Cancel"
						class="p-button-secondary"
						@click="isConfirmRemovalModalVisible = false"
					/>
				</template>
			</modal>
		</Teleport>
	</nav>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { logger } from '@/utils/logger';
import { capitalize, isEmpty, isEqual } from 'lodash';
import { Tab } from '@/types/common';
import Modal from '@/components/widgets/Modal.vue';
import { deleteAsset, iconClassname } from '@/services/project';
import useResourcesStore from '@/stores/resources';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import { IProject, ProjectAssetTypes } from '@/types/Project';

const props = defineProps<{
	project: IProject;
	openedAssetRoute: Tab;
	tabs: Tab[];
}>();

const emit = defineEmits(['open-asset', 'close-tab']);

const resourcesStore = useResourcesStore();

const isConfirmRemovalModalVisible = ref(false);

/*
const resources = computed(() => {
	if (!isEmpty(storedAssets)) {
		resourceTreeNodes.push({
			key: 'Overview',
			label: 'Overview',
			asset: {
				assetName: 'Overview',
				assetType: 'overview',
				assetId: undefined
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
});
*/

function removeAsset(assetToRemove: Tab = props.openedAssetRoute) {
	const { assetName, assetId, assetType } = assetToRemove;

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
	const tabIndex = props.tabs.findIndex((tab: Tab) => isEqual(tab, assetToRemove));
	if (tabIndex !== undefined) emit('close-tab', tabIndex);

	isConfirmRemovalModalVisible.value = false;
	logger.info(`${assetName} was removed.`);
}
</script>

<style scoped>
nav {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

header {
	padding: 0 0.5rem;
}

::v-deep(.p-accordion .p-accordion-content) {
	display: flex;
	flex-direction: column;
	padding: 0 0 1rem;
}

::v-deep(.p-accordion .p-accordion-header .p-accordion-header-link) {
	font-size: var(--font-body-small);
	gap: 1rem;
	padding: 0.5rem 1rem;
}

::v-deep(.asset-button.p-button) {
	display: inline-flex;
	overflow: hidden;
	padding: 0.375rem 1rem;
}

::v-deep(.asset-button.p-button .p-button-label) {
	overflow: hidden;
	text-align: left;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.loading-spinner {
	display: flex;
	flex: 1;
	justify-content: center;
	align-items: center;
	color: var(--primary-color);
	flex-grow: 1;
}

.pi-spinner {
	font-size: 4rem;
}

.remove-modal p {
	max-width: 40rem;
}

.remove-modal em {
	font-weight: var(--font-weight-semibold);
}
</style>
