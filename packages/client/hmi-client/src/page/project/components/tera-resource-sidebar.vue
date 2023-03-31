<template>
	<nav>
		<header>
			<Button
				icon="pi pi-trash"
				:disabled="!openedAssetRoute.assetId || openedAssetRoute.assetName === 'overview'"
				v-tooltip="`Remove ${openedAssetRoute.assetName}`"
				class="p-button-icon-only p-button-text p-button-rounded"
				@click="isRemovalModal = true"
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
			:active="openedAssetRoute.assetType === 'overview'"
			:icon="iconClassname('overview')"
			plain
			text
			size="small"
			@click="emit('open-overview')"
		/>
		<Accordion v-if="!isEmpty(assets)" :multiple="true">
			<AccordionTab v-for="[type, tabs] in assets" :key="type">
				<template #header>
					{{ capitalize(type) }}
					<aside>({{ tabs.size }})</aside>
				</template>
				<Button
					v-for="tab in tabs"
					:key="tab.assetId"
					:active="isEqual(tab, openedAssetRoute)"
					:icon="iconClassname(tab.assetType?.toString() ?? null)"
					:label="tab.assetName"
					:title="tab.assetName"
					class="asset-button"
					plain
					text
					size="small"
					@click="emit('open-asset', tab as Tab)"
				/>
			</AccordionTab>
		</Accordion>
		<Teleport to="body">
			<modal
				v-if="isRemovalModal"
				@modal-mask-clicked="isRemovalModal = false"
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
					<Button label="Cancel" class="p-button-secondary" @click="isRemovalModal = false" />
				</template>
			</modal>
		</Teleport>
	</nav>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { capitalize, isEmpty, isEqual } from 'lodash';
import { Tab } from '@/types/common';
import Modal from '@/components/widgets/Modal.vue';
import { iconClassname } from '@/services/project';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import { IProject, ProjectAssetTypes, isProjectAssetTypes } from '@/types/Project';

type IProjectAssetTabs = Map<ProjectAssetTypes, Set<Tab>>;

const props = defineProps<{
	project: IProject;
	openedAssetRoute: Tab;
	tabs: Tab[];
}>();

const emit = defineEmits(['open-asset', 'open-overview', 'remove-asset', 'close-tab']);

const isRemovalModal = ref(false);

const assets = computed((): IProjectAssetTabs => {
	const tabs = new Map<ProjectAssetTypes, Set<Tab>>();

	const projectAssets = props.project?.assets;
	if (!projectAssets) return tabs;

	// Run through all the assets type within the project
	Object.keys(projectAssets).forEach((type) => {
		if (isProjectAssetTypes(type) && !isEmpty(projectAssets[type])) {
			const projectAssetType = type as ProjectAssetTypes;
			const typeAssets = projectAssets[projectAssetType].map((asset) => {
				const assetName = (asset?.name || asset?.title || asset.id).toString();
				const assetType = asset?.type ?? projectAssetType;
				const assetId =
					projectAssetType === ProjectAssetTypes.DOCUMENTS ? asset.xdd_uri : asset?.id.toString();
				return { assetName, assetType, assetId };
			}) as Tab[];
			tabs.set(projectAssetType, new Set(typeAssets));
		}
	});

	return tabs;
});

function removeAsset(asset = props.openedAssetRoute) {
	emit('remove-asset', asset);
	isRemovalModal.value = false;
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
	padding: 0.5rem 1rem;
}

::v-deep(.p-accordion .p-accordion-header .p-accordion-header-link aside) {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
	margin-left: 0.25rem;
}

::v-deep(.asset-button.p-button) {
	display: inline-flex;
	overflow: hidden;
	padding: 0.375rem 1rem;
}

::v-deep(.asset-button.p-button[active='true']) {
	background-color: var(--surface-highlight);
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
