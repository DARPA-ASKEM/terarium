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
					:opened-asset-route="openedAssetRoute"
					@open-asset="openAsset"
					@remove-asset="removeAsset"
					@open-new-asset="openNewAsset"
				/>
			</template>
		</tera-slider-panel>
		<section class="project-page">
			<tera-project-page
				:asset-id="openedAssetRoute.assetId"
				:page-type="openedAssetRoute.pageType"
				@open-new-asset="openNewAsset"
			/>
		</section>
		<tera-slider-panel
			v-model:is-open="isNotesSliderOpen"
			content-width="240px"
			direction="right"
			header="Notes"
		>
			<template v-slot:content>
				<tera-notes-sidebar
					:asset-id="openedAssetRoute.assetId"
					:page-type="openedAssetRoute.pageType"
				/>
			</template>
		</tera-slider-panel>
		<!-- New model modal -->
		<tera-model-modal :is-visible="isNewModelModalVisible" @close-modal="onCloseModelModal" />
	</main>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { isEqual } from 'lodash';
import { useRoute, useRouter } from 'vue-router';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraResourceSidebar from '@/page/project/components/tera-resource-sidebar.vue';
import TeraNotesSidebar from '@/page/project/components/tera-notes-sidebar.vue';
import { RouteName } from '@/router/routes';
import { AssetRoute } from '@/types/common';
import { ProjectPages, isProjectAssetTypes } from '@/types/Project';
import { logger } from '@/utils/logger';
import { createWorkflow, emptyWorkflow } from '@/services/workflow';
import { AssetType } from '@/types/Types';
import { useProjects } from '@/composables/project';
import TeraModelModal from './components/tera-model-modal.vue';
import TeraProjectPage from './components/tera-project-page.vue';

const route = useRoute();
const router = useRouter();

const isResourcesSliderOpen = ref(true);
const isNotesSliderOpen = ref(false);
const isNewModelModalVisible = ref(false);

// Passed down to tera-project-page and tera-notes-sidebar
const openedAssetRoute = computed<AssetRoute>(() => ({
	pageType: (route.params.pageType as ProjectPages | AssetType) ?? ProjectPages.EMPTY,
	assetId: (route.params.assetId as string) ?? ''
}));

function openAsset(assetRoute: AssetRoute) {
	if (!isEqual(assetRoute, openedAssetRoute.value)) {
		router.push({
			name: RouteName.Project,
			params: assetRoute
		});
	}
}

async function removeAsset(assetRoute: AssetRoute) {
	const { assetId, pageType } = assetRoute;

	// Delete only Asset with an ID and of ProjectAssetType
	if (assetId && pageType && isProjectAssetTypes(pageType) && pageType !== ProjectPages.OVERVIEW) {
		const isRemoved = await useProjects().deleteAsset(pageType as AssetType, assetId);

		if (isRemoved) {
			logger.info(`${assetId} was removed.`, { showToast: true });
			return;
		}
	}
	logger.error(`Failed to remove ${assetId}`, { showToast: true });
}

const openWorkflow = async () => {
	// Create a new workflow
	let wfName = 'workflow';
	const { activeProject } = useProjects();
	if (activeProject.value && activeProject.value?.assets) {
		wfName = `workflow ${activeProject.value.assets[AssetType.Workflows].length + 1}`;
	}
	const wf = emptyWorkflow(wfName, '');

	// Add the workflow to the project
	const response = await createWorkflow(wf);
	const workflowId = response.id;
	await useProjects().addAsset(AssetType.Workflows, workflowId);

	openAsset({ pageType: AssetType.Workflows, assetId: workflowId });
};

const openNewAsset = (assetType: AssetType) => {
	switch (assetType) {
		case AssetType.Models:
			isNewModelModalVisible.value = true;
			break;
		case AssetType.Workflows:
			openWorkflow();
			break;
		case AssetType.Code:
			openAsset({
				pageType: AssetType.Code,
				assetId: 'code' // FIXME: hack to get around weird tab behaviour,
			});
			break;
		default:
			break;
	}
};

const onCloseModelModal = () => {
	isNewModelModalVisible.value = false;
};

onMounted(() => {
	if (!route.params.assetId || !route.params.pageType) {
		const overview = { assetId: '', pageType: ProjectPages.OVERVIEW };
		openAsset(overview);
	}
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

section {
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
