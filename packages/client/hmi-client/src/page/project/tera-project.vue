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
					@open-new-asset="openNewAsset"
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
					@open-new-asset="openNewAsset"
				/>
			</SplitterPanel>
			<SplitterPanel class="project-page top-z-index" v-if="workflowNode" :size="20">
				<tera-tab-group
					v-if="workflowNode"
					class="tab-group"
					:tabs="[{ assetName: workflowNode.operationType }]"
					:active-tab-index="0"
					:loading-tab-index="null"
					@close-tab="
						workflowEventBus.emit('clearActiveNode');
						workflowNode = null;
					"
				/>
				<tera-calibration-julia
					v-if="
						workflowNode && workflowNode.operationType === WorkflowOperationTypes.CALIBRATION_JULIA
					"
					:node="workflowNode"
				/>
				<tera-calibration-ciemss
					v-if="
						workflowNode && workflowNode.operationType === WorkflowOperationTypes.CALIBRATION_CIEMSS
					"
					:node="workflowNode"
				/>
				<tera-simulate-julia
					v-if="
						workflowNode && workflowNode.operationType === WorkflowOperationTypes.SIMULATE_JULIA
					"
					:node="workflowNode"
					:project="project"
				/>
				<tera-simulate-ciemss
					v-if="
						workflowNode && workflowNode.operationType === WorkflowOperationTypes.SIMULATE_CIEMSS
					"
					:node="workflowNode"
					:project="project"
				/>
				<tera-stratify
					v-if="workflowNode && workflowNode.operationType === WorkflowOperationTypes.STRATIFY"
					:node="workflowNode"
					@open-asset="(asset) => openAssetFromSidebar(asset)"
				/>
				<tera-simulate-ensemble-ciemss
					v-if="
						workflowNode &&
						workflowNode.operationType === WorkflowOperationTypes.SIMULATE_ENSEMBLE_CIEMSS
					"
					:node="workflowNode"
					:project="project"
				/>
				<tera-calibrate-ensemble-ciemss
					v-if="
						workflowNode &&
						workflowNode.operationType === WorkflowOperationTypes.CALIBRATE_ENSEMBLE_CIEMSS
					"
					:node="workflowNode"
					:project="project"
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
		<!-- New model modal -->
		<tera-model-modal
			:project="project"
			:is-visible="isNewModelModalVisible"
			@close-modal="onCloseModelModal"
		/>
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
import TeraCalibrationJulia from '@/components/workflow/tera-calibration-julia.vue';
import TeraCalibrationCiemss from '@/components/workflow/tera-calibration-ciemss.vue';
import TeraSimulateJulia from '@/components/workflow/tera-simulate-julia.vue';
import TeraSimulateCiemss from '@/components/workflow/tera-simulate-ciemss.vue';
import TeraStratify from '@/components/workflow/tera-stratify.vue';
import teraSimulateEnsembleCiemss from '@/components/workflow/tera-simulate-ensemble-ciemss.vue';
import teraCalibrateEnsembleCiemss from '@/components/workflow/tera-calibrate-ensemble-ciemss.vue';
import { createWorkflow, emptyWorkflow, workflowEventBus } from '@/services/workflow';
import TeraModelModal from './components/tera-model-modal.vue';

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

const isNewModelModalVisible = ref(false);
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

const openWorkflow = async () => {
	// Create a new workflow
	let wfName = 'workflow';
	if (props.project && props.project.assets) {
		wfName = `workflow ${props.project.assets[ProjectAssetTypes.SIMULATION_WORKFLOW].length + 1}`;
	}
	const wf = emptyWorkflow(wfName, '');

	// FIXME: TDS bug thinks that k is z, June 2023
	// @ts-ignore
	wf.transform.z = 1;

	// Add the workflow to the project
	const response = await createWorkflow(wf);
	const workflowId = response.id;
	await ProjectService.addAsset(
		props.project.id,
		ProjectAssetTypes.SIMULATION_WORKFLOW,
		workflowId
	);

	router.push({
		name: RouteName.ProjectRoute,
		params: {
			assetName: 'Workflow',
			pageType: ProjectAssetTypes.SIMULATION_WORKFLOW,
			assetId: workflowId
		}
	});
};

const openNewAsset = (assetType: string) => {
	switch (assetType) {
		case ProjectAssetTypes.MODELS:
			isNewModelModalVisible.value = true;
			break;
		case ProjectAssetTypes.SIMULATION_WORKFLOW:
			openWorkflow();
			break;
		default:
			break;
	}
};

const onCloseModelModal = () => {
	isNewModelModalVisible.value = false;
};
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

		const overviewResource = {
			assetName: 'Overview',
			pageType: ProjectPages.OVERVIEW,
			assetId: ''
		};
		if (projectContext.value && !tabs.value.some((tab) => isEqual(tab, overviewResource))) {
			// Automatically add overview tab if it does not exist
			tabStore.addTab(projectContext.value, overviewResource);
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
