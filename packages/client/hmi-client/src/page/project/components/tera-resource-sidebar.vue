<template>
	<nav>
		<header class="resource-panel-toolbar">
			<span class="p-input-icon-left">
				<i class="pi pi-search" />
				<InputText v-model="searchAsset" class="resource-panel-search" placeholder="Find" />
			</span>
			<Button
				class="new"
				icon="pi pi-plus"
				label="New"
				severity="secondary"
				size="small"
				outlined
				@click="toggleOptionsMenu"
			/>
			<Menu ref="optionsMenu" :model="optionsMenuItems" :popup="true">
				<template #item="slotProps">
					<a class="p-menuitem-link">
						<tera-asset-icon :asset-type="(slotProps.item.key as AssetType)" />
						<span class="p-menuitem-text">
							{{ slotProps.item.label }}
						</span>
					</a>
				</template>
			</Menu>
		</header>
		<Button
			class="asset-button"
			:active="pageType === ProjectPages.OVERVIEW"
			plain
			text
			size="small"
			@click="emit('open-asset', overview)"
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
		<Accordion
			v-if="!isEmpty(assetItemsMap)"
			:multiple="true"
			:active-index="Array.from(activeAccordionTabs)"
			@tab-open="
				activeAccordionTabs.add($event.index);
				saveAccordionTabsState();
			"
			@tab-close="
				activeAccordionTabs.delete($event.index);
				saveAccordionTabsState();
			"
		>
			<AccordionTab v-for="[type, assetItems] in assetItemsMap" :key="type">
				<template #header>
					<template v-if="type === AssetType.Publications">External Publications</template>
					<template v-else-if="type === AssetType.Documents">Documents</template>
					<template v-else>{{ capitalize(type) }}</template>
					<aside>({{ assetItems.size }})</aside>
				</template>
				<Button
					v-for="assetItem in assetItems"
					:key="assetItem.assetId"
					:active="assetItem.assetId === assetId && assetItem.pageType === pageType"
					:title="assetItem.assetName"
					class="asset-button"
					plain
					text
					size="small"
					@click="emit('open-asset', { assetId: assetItem.assetId, pageType: assetItem.pageType })"
					@mouseover="activeAssetId = assetItem.assetId"
					@mouseleave="activeAssetId = undefined"
					@focus="activeAssetId = assetItem.assetId"
					@focusout="activeAssetId = undefined"
				>
					<span
						:draggable="
							pageType === AssetType.Workflows &&
							(assetItem.pageType === AssetType.Models ||
								assetItem.pageType === AssetType.Datasets ||
								assetItem.pageType === AssetType.Code)
						"
						@dragstart="startDrag({ assetId: assetItem.assetId, pageType: assetItem.pageType })"
						@dragend="endDrag"
						:class="isEqual(draggedAsset, assetItem) ? 'dragged-asset' : ''"
						fallback-class="original-asset"
						:force-fallback="true"
					>
						<tera-asset-icon :asset-type="(assetItem.pageType as AssetType)" />
						<span class="p-button-label">{{ assetItem.assetName }}</span>
					</span>
					<!-- This 'x' only shows while hovering over the row -->
					<i
						v-if="activeAssetId && activeAssetId === assetItem.assetId"
						class="pi pi-times removeResourceButton"
						@click.stop="
							assetToDelete = assetItem;
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
						{{ useProjects().activeProject.value?.name }}.
					</p>
				</template>
				<template #footer>
					<Button label="Remove" class="p-button-danger" @click="removeAsset" />
					<Button label="Cancel" severity="secondary" outlined @click="isRemovalModal = false" />
				</template>
			</tera-modal>
		</Teleport>
	</nav>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { capitalize, isEmpty, isEqual } from 'lodash';
import { AssetItem, AssetRoute } from '@/types/common';
import TeraModal from '@/components/widgets/tera-modal.vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import { ProjectPages } from '@/types/Project';
import { useDragEvent } from '@/services/drag-drop';
import InputText from 'primevue/inputtext';
import Menu from 'primevue/menu';
import { AssetType } from '@/types/Types';
import { useProjects } from '@/composables/project';
import { generateProjectAssetsMap } from '@/utils/map-project-assets';
import TeraAssetIcon from '@/components/widgets/tera-asset-icon.vue';

defineProps<{
	pageType: ProjectPages | AssetType;
	assetId: string;
}>();

const emit = defineEmits(['open-asset', 'remove-asset', 'open-new-asset']);

const overview = { assetId: '', pageType: ProjectPages.OVERVIEW };

const activeAssetId = ref<string | undefined>('');
const isRemovalModal = ref(false);
const draggedAsset = ref<AssetRoute | null>(null);
const assetToDelete = ref<AssetItem | null>(null);
const searchAsset = ref<string>('');
const activeAccordionTabs = ref(
	new Set(
		localStorage.getItem('activeResourceBarTabs')?.split(',').map(Number) ?? [0, 1, 2, 3, 4, 5, 6]
	)
);

const assetItemsMap = computed(() => generateProjectAssetsMap(searchAsset.value));

function removeAsset() {
	if (assetToDelete.value) {
		const { assetId, pageType } = assetToDelete.value;
		emit('remove-asset', { assetId, pageType } as AssetRoute); // Pass as AssetRoute
		isRemovalModal.value = false;
	}
}

function saveAccordionTabsState() {
	localStorage.setItem('activeResourceBarTabs', Array.from(activeAccordionTabs.value).join());
}

const { setDragData, deleteDragData } = useDragEvent();

function startDrag(assetRoute: AssetRoute) {
	const { assetId, pageType } = assetRoute;
	setDragData('initAssetNode', { assetId, assetType: pageType });
	draggedAsset.value = assetRoute;
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

.new {
	width: 6rem;
}

:deep(.p-button-icon-left.icon) {
	margin-right: 0.5rem;
}
</style>
