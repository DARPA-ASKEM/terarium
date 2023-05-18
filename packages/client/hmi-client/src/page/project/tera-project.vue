<template>
	<main>
		<tera-slider-panel
			v-model:is-open="isResourcesSliderOpen"
			content-width="300px"
			header="Resources"
			direction="left"
			class="resource-panel"
		>
			<template v-slot:content>
				<tera-resource-sidebar
					:project="project"
					:tabs="tabs"
					:active-tab="openedAssetRoute"
					@open-asset="openAssetFromSidebar"
					@close-tab="removeClosedTab"
					@remove-asset="removeAsset"
				/>
			</template>
		</tera-slider-panel>
		<Splitter>
			<SplitterPanel class="project-page" :size="20">
				<tera-tab-group
					v-if="!isEmpty(tabs)"
					class="tab-group"
					:tabs="tabs"
					:active-tab-index="activeTabIndex"
					:loading-tab-index="loadingTabIndex"
					@close-tab="removeClosedTab"
					@select-tab="openAsset"
				/>
				<tera-project-page
					:project="project"
					:asset-id="assetId"
					:page-type="pageType"
					v-model:tabs="tabs"
					@asset-loaded="setActiveTab"
					@close-current-tab="removeClosedTab(activeTabIndex as number)"
					@update-project="updateProject"
				/>
			</SplitterPanel>
			<SplitterPanel
				class="project-page"
				v-if="
					openedWorkflowNodeStore.assetId &&
					openedWorkflowNodeStore.pageType &&
					pageType === ProjectAssetTypes.SIMULATION_WORKFLOW
				"
				:size="20"
			>
				<tera-project-page
					:project="project"
					:asset-id="openedWorkflowNodeStore.assetId ?? undefined"
					:page-type="openedWorkflowNodeStore.pageType ?? undefined"
					is-drilldown
					@asset-loaded="setActiveTab"
				/>
			</SplitterPanel>
		</Splitter>
		<tera-slider-panel
			v-model:is-open="isNotesSliderOpen"
			content-width="240px"
			direction="right"
			header="Notes"
		>
			<template v-slot:content>
				<tera-notes-sidebar :asset-id="assetId" :page-type="pageType" />
			</template>
		</tera-slider-panel>
	</main>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { useRouter } from 'vue-router';
import { isEmpty, isEqual } from 'lodash';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraTabGroup from '@/components/widgets/tera-tab-group.vue';
import TeraResourceSidebar from '@/page/project/components/tera-resource-sidebar.vue';
import TeraNotesSidebar from '@/page/project/components/tera-notes-sidebar.vue';
import { RouteName } from '@/router/routes';
import * as ProjectService from '@/services/project';
import { useTabStore } from '@/stores/tabs';
import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';
import { Tab } from '@/types/common';
import { IProject, ProjectAssetTypes, ProjectPages, isProjectAssetTypes } from '@/types/Project';
import { logger } from '@/utils/logger';
import Splitter from 'primevue/splitter';
import SplitterPanel from 'primevue/splitterpanel';
import TeraProjectPage from './components/tera-project-page.vue';

// Asset props are extracted from route
const props = defineProps<{
	project: IProject;
	assetName?: string;
	assetId?: string;
	pageType?: ProjectAssetTypes | ProjectPages;
}>();

const emit = defineEmits(['update-project']);

const tabStore = useTabStore();
const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();

const router = useRouter();

const isResourcesSliderOpen = ref(true);
const isNotesSliderOpen = ref(false);

// Associated with tab storage
const projectContext = computed(() => props.project?.id.toString());
const tabs = computed(() => tabStore.getTabs(projectContext.value) ?? []);
const activeTabIndex = ref<number | null>(0);
const openedAssetRoute = computed<Tab>(() => ({
	assetName: props.assetName ?? '',
	pageType: props.pageType,
	assetId: props.assetId
}));
const loadingTabIndex = ref<number | null>(null);

function setActiveTab() {
	activeTabIndex.value = tabStore.getActiveTabIndex(projectContext.value);
	loadingTabIndex.value = null;
}

function updateProject(id: IProject['id']) {
	emit('update-project', id);
}

function openAsset(index: number = tabStore.getActiveTabIndex(projectContext.value)) {
	activeTabIndex.value = null;
	const asset: Tab = tabs.value[index];
	if (
		!(
			asset &&
			asset.assetId === props.assetId &&
			asset.assetName === props.assetName &&
			asset.pageType === props.pageType
		)
	) {
		loadingTabIndex.value = index;
		router.push({ name: RouteName.ProjectRoute, params: asset });
	}
}

function openAssetFromSidebar(asset: Tab = tabs.value[activeTabIndex.value!]) {
	router.push({ name: RouteName.ProjectRoute, params: asset });
	loadingTabIndex.value = tabs.value.length;
}

function removeClosedTab(tabIndexToRemove: number) {
	tabStore.removeTab(projectContext.value, tabIndexToRemove);
	activeTabIndex.value = tabStore.getActiveTabIndex(projectContext.value);
}

async function removeAsset(asset: Tab) {
	const { assetName, assetId, pageType } = asset;

	// Delete only Asset with an ID and of ProjectAssetType
	if (
		assetId &&
		pageType &&
		isProjectAssetTypes(pageType) &&
		pageType !== ProjectPages.OVERVIEW &&
		pageType !== ProjectAssetTypes.SIMULATION_WORKFLOW
	) {
		const isRemoved = await ProjectService.deleteAsset(props.project.id, pageType, assetId);

		if (isRemoved) {
			emit('update-project', props.project.id);
			removeClosedTab(tabs.value.findIndex((tab: Tab) => isEqual(tab, asset)));
			logger.info(`${assetName} was removed.`, { showToast: true });
			return;
		}
	}

	logger.error(`Failed to remove ${assetName}`, { showToast: true });
}

watch(
	() => projectContext.value,
	() => {
		if (
			tabs.value.length > 0 &&
			tabs.value.length >= tabStore.getActiveTabIndex(projectContext.value)
		) {
			openAsset();
		} else if (openedAssetRoute.value && openedAssetRoute.value.assetName) {
			tabStore.addTab(projectContext.value, openedAssetRoute.value);
		}
	}
);

watch(
	() => openedAssetRoute.value, // Once route attributes change, add/switch to another tab
	(newOpenedAssetRoute) => {
		if (newOpenedAssetRoute.assetName) {
			// If name isn't recognized, its a new asset so add a new tab
			if (
				props.assetName &&
				props.pageType &&
				!tabs.value.some((tab) => isEqual(tab, newOpenedAssetRoute))
			) {
				tabStore.addTab(projectContext.value, newOpenedAssetRoute);
			}
			// Tab switch
			else if (props.assetName) {
				const index = tabs.value.findIndex((tab) => isEqual(tab, newOpenedAssetRoute));
				tabStore.setActiveTabIndex(projectContext.value, index);
			}
			// Goes to tab from previous session
			else {
				openAsset(tabStore.getActiveTabIndex(projectContext.value));
			}
		}
	}
);

tabStore.$subscribe(() => {
	openAsset(tabStore.getActiveTabIndex(projectContext.value));
});
</script>

<style scoped>
.resource-panel {
	z-index: 2;
	isolation: isolate;
}

.tab-group {
	z-index: 2;
	isolation: isolate;
	position: relative;
}

.p-splitter {
	display: flex;
	flex: 1;
	background: none;
	border: none;
}

section,
.p-splitter:deep(.project-page) {
	display: flex;
	flex-direction: column;
	flex: 1;
	overflow-x: auto;
	overflow-y: hidden;
}

.p-tabmenu:deep(.p-tabmenuitem) {
	display: inline;
	max-width: 15rem;
}

.p-tabmenu:deep(.p-tabmenu-nav .p-tabmenuitem .p-menuitem-link) {
	padding: 1rem;
	text-decoration: none;
}

.p-tabmenu:deep(.p-menuitem-text) {
	height: 1rem;
	display: inline-block;
	overflow: hidden;
	text-overflow: ellipsis;
}
</style>
