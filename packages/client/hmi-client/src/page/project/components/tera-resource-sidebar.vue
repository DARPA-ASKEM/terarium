<template>
	<nav>
		<header class="resource-panel-toolbar">
			<span class="p-input-icon-left">
				<i class="pi pi-search" />
				<InputText v-model="searchAsset" class="resource-panel-search" placeholder="Find" />
			</span>
			<Button
				icon="pi pi-plus"
				label="New"
				class="p-button-sm secondary-button"
				@click="toggleOptionsMenu"
			/>
			<Menu ref="optionsMenu" :model="optionsMenuItems" :popup="true" />
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
		<Accordion v-if="!isEmpty(assets)" :multiple="true" :active-index="[0, 1, 2, 3, 4]">
			<AccordionTab v-for="[type, tabs] in assets" :key="type">
				<template #header>
					<template v-if="type === ProjectAssetTypes.DOCUMENTS">Publications & Documents</template>
					<template v-else>{{ capitalize(type) }}</template>
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
					@mouseover="activeAssetId = tab.assetId"
					@mouseleave="activeAssetId = undefined"
					@focus="activeAssetId = tab.assetId"
					@focusout="activeAssetId = undefined"
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
						fallback-class="original-asset"
						:force-fallback="true"
					>
						<vue-feather
							v-if="typeof getAssetIcon(tab.pageType ?? null) === 'string'"
							class="p-button-icon-left icon"
							:type="getAssetIcon(tab.pageType ?? null)"
							size="1rem"
							:stroke="isEqual(draggedAsset, tab) ? 'var(--text-color-primary)' : 'rgb(16, 24, 40)'"
						/>
						<component
							v-else
							:is="getAssetIcon(tab.pageType ?? null)"
							class="p-button-icon-left icon"
						/>
						<span class="p-button-label">{{ tab.assetName }}</span>
					</span>
					<!-- This 'x' only shows while hovering over the row -->
					<i
						v-if="activeAssetId && activeAssetId === tab.assetId"
						class="pi pi-times removeResourceButton"
						@click.stop="
							assetToDelete = tab;
							isRemovalModal = true;
						"
					/>
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
						Removing <em>{{ assetToDelete?.assetName }}</em> will permanently remove it from
						{{ project.name }}.
					</p>
				</template>
				<template #footer>
					<Button label="Remove" class="p-button-danger" @click="removeAsset" />
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
import InputText from 'primevue/inputtext';
import Menu from 'primevue/menu';

interface ResourceTab {
	assetName?: string;
	pageType?: string;
	assetId?: string;
}

type IProjectAssetTabs = Map<ProjectAssetTypes, Set<ResourceTab>>;

const props = defineProps<{
	project: IProject;
	activeTab: Tab;
}>();

const emit = defineEmits(['open-asset', 'remove-asset']);

const activeAssetId = ref<string | undefined>('');
const isRemovalModal = ref(false);
const draggedAsset = ref<Tab | null>(null);
const assetToDelete = ref<Tab | null>(null);
const searchAsset = ref<string | null>('');

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
				const assetId = asset?.id?.toString();
				return { assetName, pageType, assetId };
			}) as Tab[];
			tabs.set(projectAssetType, new Set(typeAssets));
		}
	});
	return tabs;
});

function removeAsset() {
	emit('remove-asset', assetToDelete.value);
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

const optionsMenu = ref();
const optionsMenuItems = ref([
	{
		icon: 'pi pi-code',
		label: 'Code editor',
		command() {
			emit('open-asset', {
				assetName: 'New file',
				pageType: ProjectAssetTypes.CODE,
				assetId: undefined
			});
		}
	}
]);

const toggleOptionsMenu = (event) => {
	optionsMenu.value.toggle(event);
};
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

.removeResourceButton {
	color: var(--text-color-subdued);
	margin-right: 0.75rem;
	font-size: 0.75rem;
}

.removeResourceButton:hover {
	color: var(--text-color-danger);
}

.dragged-asset {
	background-color: var(--surface-highlight);
	border-radius: var(--border-radius);
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

.remove-modal:deep(main) {
	max-width: 50rem;
}

.remove-modal p {
	max-width: 40rem;
}

.remove-modal em {
	font-weight: var(--font-weight-semibold);
}

.resource-panel-toolbar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 0.5rem;
}

.resource-panel-search {
	padding: 0.51rem 0.5rem;
	width: 100%;
	font-size: var(--font-caption);
}

/* We should make a proper secondary outline button. Until then this works. */
.secondary-button {
	color: var(--text-color-secondary);
	font-size: var(--font-caption);
	background-color: var(--surface-0);
	border: 1px solid var(--surface-border);
	width: 6rem;
}

.secondary-button:hover {
	color: var(--text-color-secondary) !important;
	background-color: var(--surface-highlight) !important;
}
</style>
