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
			<Menu ref="optionsMenu" :model="optionsMenuItems" :popup="true">
				<!-- A way to use vue feather icons in the MenuItem component, it might be better to try to use 1 icon library for easier integration -->
				<template #item="slotProps">
					<a class="p-menuitem-link">
						<vue-feather
							v-if="typeof getAssetIcon(slotProps.item.key ?? null) === 'string'"
							class="p-button-icon-left icon"
							:type="getAssetIcon(slotProps.item.key ?? null)"
							size="1rem"
						/>
						<component
							v-else
							:is="getAssetIcon(slotProps.item.key ?? null)"
							class="p-button-icon-left icon"
						/>
						<span class="p-menuitem-text">
							{{ slotProps.item.label }}
						</span>
					</a>
				</template>
			</Menu>
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
					<template v-if="type === AssetType.Publications">Publications & Documents</template>
					<template v-else>{{ capitalize(type) }}</template>
					<aside>({{ tabs.size }})</aside>
				</template>
				<Button
					v-for="tab in tabs"
					:key="tab.assetId"
					:active="tab.assetId === activeTab.assetId"
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
							activeTab.pageType === AssetType.Workflows &&
							(tab.pageType === AssetType.Models || tab.pageType === AssetType.Datasets)
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
				@modal-enter-press="removeAsset"
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
import { IProject, ProjectPages, isProjectAssetTypes } from '@/types/Project';
import { useDragEvent } from '@/services/drag-drop';
import InputText from 'primevue/inputtext';
import Menu from 'primevue/menu';
import { AssetType } from '@/types/Types';

type IProjectAssetTabs = Map<AssetType, Set<Tab>>;

const props = defineProps<{
	project: IProject;
	activeTab: Tab;
}>();

const emit = defineEmits(['open-asset', 'remove-asset', 'open-new-asset']);

const activeAssetId = ref<string | undefined>('');
const isRemovalModal = ref(false);
const draggedAsset = ref<Tab | null>(null);
const assetToDelete = ref<Tab | null>(null);
const searchAsset = ref<string | null>('');

const assets = computed((): IProjectAssetTabs => {
	const tabs = new Map<AssetType, Set<Tab>>();

	const projectAssets = props.project?.assets;
	if (!projectAssets) return tabs;

	// Run through all the assets type within the project
	Object.keys(projectAssets).forEach((type) => {
		if (isProjectAssetTypes(type) && !isEmpty(projectAssets[type])) {
			const projectAssetType = type as AssetType;
			const typeAssets = projectAssets[projectAssetType]
				.map((asset) => {
					let assetName = (asset?.name || asset?.title || asset?.id)?.toString();

					// FIXME should unify upstream via a summary endpoint
					if (asset.header && asset.header.name) {
						assetName = asset.header.name;
					}

					const pageType = asset?.type ?? projectAssetType;
					const assetId = asset?.id?.toString();
					return { assetName, pageType, assetId };
				})
				.filter((asset) => {
					// filter assets
					if (!searchAsset.value?.trim()) {
						return true;
					}
					const searchTermLower = searchAsset.value?.trim().toLowerCase();
					return asset.assetName.toLowerCase().includes(searchTermLower);
				}) as Tab[];
			if (!isEmpty(typeAssets)) {
				tabs.set(projectAssetType, new Set(typeAssets));
			}
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
		key: AssetType.Code,
		label: 'New code',
		command() {
			emit('open-new-asset', AssetType.Code);
		}
	},
	{
		key: AssetType.Models,
		label: 'New Model',
		command() {
			emit('open-new-asset', AssetType.Models);
		}
	},
	{
		key: AssetType.Workflows,
		label: 'New Workflow',
		command() {
			emit('open-new-asset', AssetType.Workflows);
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

:deep(.p-button-icon-left.icon) {
	margin-right: 0.5rem;
}
</style>
