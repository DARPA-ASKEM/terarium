<template>
	<main>
		<tera-slider-panel
			v-model:is-open="isResourcesSliderOpen"
			content-width="240px"
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
					class="tab-group"
					v-if="!isEmpty(tabs)"
					:tabs="tabs"
					:active-tab-index="activeTabIndex"
					:loading-tab-index="loadingTabIndex"
					@close-tab="removeClosedTab"
					@select-tab="openAsset"
				/>
				<tera-project-page
					v-if="project"
					:project="project"
					:asset-id="assetId"
					:page-type="pageType"
					:asset-name="assetName"
					v-model:tabs="tabs"
					@asset-loaded="setActiveTab"
					@close-current-tab="removeClosedTab(activeTabIndex as number)"
				/>
			</SplitterPanel>
			<SplitterPanel class="project-page top-z-index" v-if="workflowNode" :size="20">
				<tera-tab-group
					v-if="workflowNode"
					class="tab-group"
					:tabs="[{ assetName: workflowNode.operationType }]"
					:active-tab-index="0"
					:loading-tab-index="null"
					@close-tab="workflowNode = null"
				/>
				<tera-calibration
					v-if="workflowNode && workflowNode.operationType === WorkflowOperationTypes.CALIBRATION"
					:node="workflowNode"
				/>
				<tera-simulate
					v-if="workflowNode && workflowNode.operationType === WorkflowOperationTypes.SIMULATE"
					:node="workflowNode"
				/>
				<tera-stratify
					v-if="workflowNode && workflowNode.operationType === WorkflowOperationTypes.STRATIFY"
					:node="workflowNode"
				/>
				<tera-model-workflow-wrapper
					v-if="workflowNode && workflowNode.operationType === WorkflowOperationTypes.MODEL"
					:project="project"
					:node="workflowNode"
				/>
				<tera-dataset-workflow-wrapper
					v-if="workflowNode && workflowNode.operationType === WorkflowOperationTypes.DATASET"
					:project="project"
					:node="workflowNode"
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
import TeraModelWorkflowWrapper from '@/components/workflow/tera-model-workflow-wrapper.vue';
import TeraDatasetWorkflowWrapper from '@/components/workflow/tera-dataset-workflow-wrapper.vue';
import { WorkflowNode, WorkflowOperationTypes } from '@/types/workflow';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraTabGroup from '@/components/widgets/tera-tab-group.vue';
import TeraResourceSidebar from '@/page/project/components/tera-resource-sidebar.vue';
import TeraNotesSidebar from '@/page/project/components/tera-notes-sidebar.vue';
import { RouteName } from '@/router/routes';
import * as ProjectService from '@/services/project';
import { useTabStore } from '@/stores/tabs';
import { Tab } from '@/types/common';
import { IProject, ProjectAssetTypes, ProjectPages, isProjectAssetTypes } from '@/types/Project';
import { logger } from '@/utils/logger';
import Splitter from 'primevue/splitter';
import SplitterPanel from 'primevue/splitterpanel';
import TeraCalibration from '@/components/workflow/tera-calibration.vue';
import TeraSimulate from '@/components/workflow/tera-simulate.vue';
import TeraStratify from '@/components/workflow/tera-stratify.vue';
import { workflowEventBus } from '@/services/workflow';
import { PresignedURL } from '@/types/Types';
import { getPresignedDownloadURL } from '@/services/artifact';
import TeraProjectPage from './components/tera-project-page.vue';

// Asset props are extracted from route
const props = defineProps<{
	project: IProject;
	assetName?: string;
	assetId?: string;
	pageType?: ProjectAssetTypes | ProjectPages;
}>();

const tabStore = useTabStore();

const router = useRouter();

const workflowNode = ref<WorkflowNode | null>(null);
// const workflowOperation = ref<string>('');

workflowEventBus.on('drilldown', (payload: any) => {
	console.log('listener', payload);
	workflowNode.value = payload;
});

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

async function openAsset(index: number = tabStore.getActiveTabIndex(projectContext.value)) {
	activeTabIndex.value = null;
	const asset: Tab = tabs.value[index];
	if (asset && asset.assetId && asset.assetName.toLowerCase().endsWith('.pdf')) {
		const url: PresignedURL | null = await getPresignedDownloadURL(asset.assetId, asset.assetName);
		if (url) window.open(url.url, '_blank');
	} else if (
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
	if (assetId && pageType && isProjectAssetTypes(pageType) && pageType !== ProjectPages.OVERVIEW) {
		const isRemoved = await ProjectService.deleteAsset(
			props.project.id,
			pageType as ProjectAssetTypes,
			assetId
		);

		if (isRemoved) {
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
		if (projectContext.value) {
			// Automatically go to overview page when project is opened
			router.push({
				name: RouteName.ProjectRoute,
				params: { assetName: 'Overview', pageType: ProjectPages.OVERVIEW, assetId: undefined }
			});
		}
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
	z-index: 1000;
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
	overflow-x: hidden;
}

section,
.p-splitter:deep(.project-page) {
	display: flex;
	flex-direction: column;
	flex: 1;
	overflow-x: auto;
	overflow-y: hidden;
}

.p-splitter:deep(.p-splitter-gutter) {
	z-index: 1000;
}

.top-z-index {
	z-index: 1000;
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
