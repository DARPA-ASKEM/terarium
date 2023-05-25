<template>
	<nav>
		<header>
			<Button
				icon="pi pi-trash"
				:disabled="!activeTab.assetId || activeTab.assetName === ProjectPages.OVERVIEW"
				v-tooltip="`Remove ${activeTab.assetName}`"
				class="p-button-icon-only p-button-text p-button-rounded"
				@click="isRemovalModal = true"
			/>
			<Button
				icon="pi pi-code"
				v-tooltip="`New code file`"
				class="p-button-icon-only p-button-text p-button-rounded"
				@click="
					emit('open-asset', {
						assetName: 'New file',
						pageType: ProjectAssetTypes.CODE,
						assetId: undefined
					})
				"
			/>
			<Button
				icon="pi pi-user-edit"
				v-tooltip="`Create model from Equation`"
				class="p-button-icon-only p-button-text p-button-rounded"
				@click="
					emit('open-asset', {
						assetName: 'New Model',
						pageType: ProjectAssetTypes.MODELS,
						assetId: undefined
					})
				"
			/>
		</header>
		<Button
			class="asset-button"
			:active="activeTab.pageType === ProjectPages.OVERVIEW"
			plain
			text
			size="small"
			@click="
				emit('open-asset', {
					assetName: 'Overview',
					pageType: ProjectPages.OVERVIEW,
					assetId: undefined
				})
			"
		>
			<span>
				<vue-feather
					class="p-button-icon-left"
					type="layout"
					size="1rem"
					stroke="rgb(16, 24, 40)"
				/>
				<span class="p-button-label">Overview</span>
			</span>
		</Button>
		<Accordion v-if="!isEmpty(assets)" :multiple="true">
			<AccordionTab v-for="[type, tabs] in assets" :key="type">
				<template #header>
					{{ capitalize(type) }}
					<aside>({{ tabs.size }})</aside>
				</template>
				<Button
					v-for="tab in tabs"
					:key="tab.assetId"
					:active="isEqual(tab, activeTab)"
					:title="tab.assetName"
					class="asset-button"
					plain
					text
					size="small"
					@click="emit('open-asset', tab)"
				>
					<span
						:draggable="
							activeTab.pageType === ProjectAssetTypes.SIMULATION_WORKFLOW &&
							(tab.pageType === ProjectAssetTypes.MODELS ||
								tab.pageType === ProjectAssetTypes.DATASETS)
						"
						@dragstart="startDrag(tab)"
						@dragend="endDrag"
						:class="isEqual(draggedAsset, tab) ? 'dragged-asset' : ''"
					>
						<vue-feather
							v-if="typeof getAssetIcon(tab.pageType ?? null) === 'string'"
							class="p-button-icon-left icon"
							:type="getAssetIcon(tab.pageType ?? null)"
							size="1rem"
							:stroke="isEqual(draggedAsset, tab) ? 'white' : 'rgb(16, 24, 40)'"
						/>
						<component
							v-else
							:is="getAssetIcon(tab.pageType ?? null)"
							class="p-button-icon-left icon"
						/>
						<span class="p-button-label">{{ tab.assetName }}</span>
					</span>
				</Button>
			</AccordionTab>
		</Accordion>
		<Teleport to="body">
			<tera-modal
				v-if="isRemovalModal"
				@modal-mask-clicked="isRemovalModal = false"
				class="remove-modal"
			>
				<template #header>
					<h4>Confirm remove</h4>
				</template>
				<template #default>
					<p>
						Removing <em>{{ activeTab.assetName }}</em> will permanently remove it from
						{{ project.name }}.
					</p>
				</template>
				<template #footer>
					<Button label="Remove" class="p-button-danger" @click="removeAsset()" />
					<Button label="Cancel" class="p-button-secondary" @click="isRemovalModal = false" />
				</template>
			</tera-modal>
		</Teleport>
	</nav>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { capitalize, isEmpty, isEqual } from 'lodash';
import { Tab } from '@/types/common';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { getAssetIcon } from '@/services/project';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import { IProject, ProjectAssetTypes, ProjectPages, isProjectAssetTypes } from '@/types/Project';
import { useDragEvent } from '@/services/drag-drop';

type IProjectAssetTabs = Map<ProjectAssetTypes, Set<Tab>>;

const props = defineProps<{
	project: IProject;
	activeTab: Tab;
	tabs: Tab[];
}>();

const emit = defineEmits(['open-asset', 'open-overview', 'remove-asset', 'close-tab']);

const isRemovalModal = ref(false);
const draggedAsset = ref<Tab | null>(null);

const assets = computed((): IProjectAssetTabs => {
	const tabs = new Map<ProjectAssetTypes, Set<Tab>>();

	const projectAssets = props.project?.assets;
	if (!projectAssets) return tabs;

	// Run through all the assets type within the project
	Object.keys(projectAssets).forEach((type) => {
		if (isProjectAssetTypes(type) && !isEmpty(projectAssets[type])) {
			const projectAssetType = type as ProjectAssetTypes;
			const typeAssets = projectAssets[projectAssetType].map((asset) => {
				const assetName = (asset?.name || asset?.title || asset?.id)?.toString();
				const pageType = asset?.type ?? projectAssetType;
				const assetId = asset?.id.toString();
				return { assetName, pageType, assetId };
			}) as Tab[];
			tabs.set(projectAssetType, new Set(typeAssets));
		}
	});

	return tabs;
});

function removeAsset(asset = props.activeTab) {
	emit('remove-asset', asset);
	isRemovalModal.value = false;
}

const { setDragData, deleteDragData } = useDragEvent();

function startDrag(tab: Tab) {
	const { assetId, pageType } = tab;
	if (assetId && pageType) {
		setDragData('initAssetNode', { assetId, assetType: pageType });
		draggedAsset.value = tab;
	}
}

function endDrag() {
	deleteDragData('assetNode');
	draggedAsset.value = null;
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

.icon {
	fill: var(--text-color-primary);
	overflow: visible;
}

.dragged-asset {
	background-color: var(--primary-color);
	color: var(--gray-0);
}

.dragged-asset .icon {
	fill: var(--gray-0);
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
	padding: 0;
}

::v-deep(.asset-button.p-button > span) {
	display: inline-flex;
	width: 100%;
	padding: 0.375rem 1rem;
	overflow: hidden;
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
