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
					:active-tab="openedAssetRoute"
					@open-asset="(asset) => openAssetFromSidebar(asset)"
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
					:tabs="[{ assetName: workflowNode.displayName }]"
					:active-tab-index="0"
					:loading-tab-index="null"
					@close-tab="
						workflowEventBus.emit('clearActiveNode');
						workflowNode = null;
					"
				/>
				<tera-calibrate-julia
					v-if="
						workflowNode && workflowNode.operationType === WorkflowOperationTypes.CALIBRATION_JULIA
					"
					:node="workflowNode"
				/>
				<tera-calibrate-ciemss
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
					:key="workflowNode.id"
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
				<tera-dataset-transformer
					v-if="
						workflowNode &&
						workflowNode.operationType === WorkflowOperationTypes.DATASET_TRANSFORMER
					"
					:project="project"
					:node="workflowNode"
				/>
				<tera-model-transformer
					v-if="
						workflowNode && workflowNode.operationType === WorkflowOperationTypes.MODEL_TRANSFORMER
					"
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
import { ref, watch, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { isEmpty } from 'lodash';
import TeraModelWorkflowWrapper from '@/workflow/ops/model/tera-model-workflow-wrapper.vue';
import TeraDatasetWorkflowWrapper from '@//workflow/ops/dataset/tera-dataset-workflow-wrapper.vue';
import TeraCalibrateJulia from '@/workflow/ops/calibrate-julia/tera-calibrate-julia.vue';
import TeraCalibrateCiemss from '@/workflow/ops/calibrate-ciemss/tera-calibrate-ciemss.vue';
import TeraSimulateJulia from '@/workflow/ops/simulate-julia/tera-simulate-julia.vue';
import TeraStratify from '@/workflow/ops/stratify-julia/tera-stratify.vue';
import TeraSimulateCiemss from '@/workflow/ops/simulate-ciemss/tera-simulate-ciemss.vue';
import teraSimulateEnsembleCiemss from '@/workflow/ops/simulate-ensemble-ciemss/tera-simulate-ensemble-ciemss.vue';
import teraCalibrateEnsembleCiemss from '@/workflow/ops/calibrate-ensemble-ciemss/tera-calibrate-ensemble-ciemss.vue';
import TeraDatasetTransformer from '@/workflow/ops/dataset-transformer/tera-dataset-transformer.vue';
import TeraModelTransformer from '@/workflow/ops/model-transformer/tera-model-transformer.vue';

import { WorkflowNode, WorkflowOperationTypes } from '@/types/workflow';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraTabGroup from '@/components/widgets/tera-tab-group.vue';
import TeraResourceSidebar from '@/page/project/components/tera-resource-sidebar.vue';
import TeraNotesSidebar from '@/page/project/components/tera-notes-sidebar.vue';
import { RouteName } from '@/router/routes';
import * as ProjectService from '@/services/project';
import { useTabStore } from '@/stores/tabs';
import { Tab } from '@/types/common';
import { IProject, ProjectPages, isProjectAssetTypes } from '@/types/Project';
import { logger } from '@/utils/logger';
import Splitter from 'primevue/splitter';
import SplitterPanel from 'primevue/splitterpanel';
import { createWorkflow, emptyWorkflow, workflowEventBus } from '@/services/workflow';
import { AssetType } from '@/types/Types';
import TeraModelModal from './components/tera-model-modal.vue';
import TeraProjectPage from './components/tera-project-page.vue';

// Asset props are extracted from route
const props = defineProps<{
	project: IProject;
	assetId?: string;
	pageType?: AssetType | ProjectPages;
}>();

const tabStore = useTabStore();

const router = useRouter();

const workflowNode = ref<WorkflowNode | null>(null);

workflowEventBus.on('drilldown', (payload: any) => {
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
	pageType: props.pageType,
	assetId: props.assetId
}));
const loadingTabIndex = ref<number | null>(null);

const isSameTab = (a: Tab, b: Tab) => a.assetId === b.assetId && a.pageType === b.pageType;

function setActiveTab() {
	activeTabIndex.value = tabStore.getActiveTabIndex(projectContext.value);
	loadingTabIndex.value = null;
}

async function openAsset(index: number = tabStore.getActiveTabIndex(projectContext.value)) {
	activeTabIndex.value = null;
	const asset: Tab = tabs.value[index];
	if (asset) {
		if (!(asset.assetId === props.assetId && asset.pageType === props.pageType)) {
			loadingTabIndex.value = index;
			router.push({
				name: RouteName.Project,
				params: { assetId: asset.assetId, pageType: asset.pageType }
			});
		}
	}
}

function openAssetFromSidebar(asset: Tab) {
	router.push({
		name: RouteName.Project,
		params: { assetId: asset.assetId, pageType: asset.pageType }
	});
	loadingTabIndex.value = tabs.value.length;
}

function removeClosedTab(tabIndexToRemove: number) {
	tabStore.removeTab(projectContext.value, tabIndexToRemove);
	activeTabIndex.value = tabStore.getActiveTabIndex(projectContext.value);

	if (tabs.value.length > 0) {
		openAsset();
	} else {
		tabStore.addTab(projectContext.value, overviewResource);
		openAsset();
	}
}

async function removeAsset(asset: Tab) {
	const { assetId, pageType } = asset;

	// Delete only Asset with an ID and of ProjectAssetType
	if (assetId && pageType && isProjectAssetTypes(pageType) && pageType !== ProjectPages.OVERVIEW) {
		const isRemoved = await ProjectService.deleteAsset(
			props.project.id,
			pageType as AssetType,
			assetId
		);

		if (isRemoved) {
			removeClosedTab(tabs.value.findIndex((tab: Tab) => isSameTab(tab, asset)));
			logger.info(`${assetId} was removed.`, { showToast: true });
			return;
		}
	}

	logger.error(`Failed to remove ${assetId}`, { showToast: true });
}

const openWorkflow = async () => {
	// Create a new workflow
	let wfName = 'workflow';
	if (props.project && props.project.assets) {
		wfName = `workflow ${props.project.assets[AssetType.Workflows].length + 1}`;
	}
	const wf = emptyWorkflow(wfName, '');

	// Add the workflow to the project
	const response = await createWorkflow(wf);
	const workflowId = response.id;
	await ProjectService.addAsset(props.project.id, AssetType.Workflows, workflowId);

	router.push({
		name: RouteName.Project,
		params: {
			pageType: AssetType.Workflows,
			assetId: workflowId
		}
	});
};

const openCode = () => {
	router.push({
		name: RouteName.Project,
		params: codeResource
	});
};

const openNewAsset = (assetType: string) => {
	switch (assetType) {
		case AssetType.Models:
			isNewModelModalVisible.value = true;
			break;
		case AssetType.Workflows:
			openWorkflow();
			break;
		case AssetType.Code:
			openCode();
			break;
		default:
			break;
	}
};

const onCloseModelModal = () => {
	isNewModelModalVisible.value = false;
};

const overviewResource = {
	pageType: ProjectPages.OVERVIEW,
	assetId: ''
};

const codeResource = {
	pageType: AssetType.Code,
	assetId: 'code', // FIXME: hack to get around weird tab behaviour,
	assetName: 'New file'
};

const adjustTabsProjectChange = () => {
	const pageType = openedAssetRoute.value.pageType;
	const projectId = projectContext.value;

	if (pageType || !projectContext.value) return;

	if (tabs.value.length > 0) {
		openAsset();
		return;
	}

	// If there aren't anything, push in overview
	if (tabs.value.length === 0) {
		tabStore.addTab(projectId, overviewResource);
		openAsset(0);
	}
};

const adjustTabs = () => {
	const projectId = projectContext.value;
	const assetId = openedAssetRoute.value.assetId;
	const pageType = openedAssetRoute.value.pageType;

	if (!pageType) return;

	// Handle new regular tab
	const tabExist = tabs.value.some((tab) => isSameTab(tab, openedAssetRoute.value));
	if (!tabExist && assetId) {
		tabStore.addTab(projectContext.value, openedAssetRoute.value);
		return;
	}

	// Handle existing tab
	if (tabExist) {
		const index = tabs.value.findIndex((tab) => isSameTab(tab, openedAssetRoute.value));
		tabStore.setActiveTabIndex(projectContext.value, index);
		return;
	}

	/** Quirky special logic for this that are not really assets * */

	// If new code or overview
	if (!tabExist && !assetId) {
		if (pageType === AssetType.Code) {
			tabStore.addTab(projectId, codeResource);
			openAsset();
		} else if (pageType === ProjectPages.OVERVIEW) {
			tabStore.addTab(projectId, overviewResource);
			openAsset(0);
		}
	}
};

watch(
	() => projectContext.value,
	() => adjustTabsProjectChange()
);

watch(
	() => openedAssetRoute.value,
	() => adjustTabs()
);

onMounted(() => {
	setTimeout(() => {
		adjustTabsProjectChange();
		adjustTabs();
	}, 1000);
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
