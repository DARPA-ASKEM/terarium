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
					:page-type="pageType"
					:asset-id="assetId"
					@open-asset="openAsset"
					@remove-asset="removeAsset"
					@open-new-asset="openNewAsset"
				/>
			</template>
		</tera-slider-panel>
		<section class="project-page">
			<tera-model v-if="pageType === AssetType.Model" :asset-id="assetId" />
			<tera-code :asset-id="assetId" v-else-if="pageType === AssetType.Code" />
			<tera-project-overview
				v-else-if="pageType === ProjectPages.OVERVIEW"
				@open-new-asset="openNewAsset"
			/>
			<tera-workflow v-else-if="pageType === AssetType.Workflow" :asset-id="assetId" />
			<!--Add new process/asset views here-->
			<template v-else-if="assetId">
				<tera-external-publication
					v-if="pageType === AssetType.Publication"
					:xdd-uri="getXDDuri(assetId)"
					:previewLineLimit="10"
					@open-code="openCode"
				/>
				<tera-document-asset
					v-if="pageType === AssetType.Document"
					:assetId="assetId"
					:previewLineLimit="10"
				/>
				<tera-dataset v-else-if="pageType === AssetType.Dataset" :asset-id="assetId" />
			</template>
		</section>
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
		<tera-model-modal :is-visible="isNewModelModalVisible" @close-modal="onCloseModelModal" />
	</main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { isEqual } from 'lodash';
import { useRoute, useRouter } from 'vue-router';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraResourceSidebar from '@/page/project/components/tera-resource-sidebar.vue';
import TeraNotesSidebar from '@/page/project/components/tera-notes-sidebar.vue';
import { RouteName } from '@/router/routes';
import { AssetRoute } from '@/types/common';
import { isProjectAssetTypes, ProjectPages } from '@/types/Project';
import { logger } from '@/utils/logger';
import { createWorkflow, emptyWorkflow } from '@/services/workflow';
import { AssetType } from '@/types/Types';
import { useProjects } from '@/composables/project';
import TeraExternalPublication from '@/components/documents/tera-external-publication.vue';
import TeraDocumentAsset from '@/components/documents/tera-document-asset.vue';
import TeraDataset from '@/components/dataset/tera-dataset.vue';
import TeraModel from '@/components/model/tera-model.vue';
import TeraProjectOverview from '@/page/project/components/tera-project-overview.vue';
import { getCodeFileAsText } from '@/services/code';
import TeraCode from '@/components/code/tera-code.vue';
import TeraWorkflow from '@/workflow/tera-workflow.vue';
import TeraModelModal from './components/tera-model-modal.vue';

const route = useRoute();
const router = useRouter();

const code = ref<string>();
const isResourcesSliderOpen = ref(true);
const isNotesSliderOpen = ref(false);
const isNewModelModalVisible = ref(false);

const pageType = computed(
	() => (route.params.pageType as ProjectPages | AssetType) ?? ProjectPages.EMPTY
);
const assetId = computed(() => (route.params.assetId as string) ?? '');
const openedAssetRoute = computed(() => ({ pageType: pageType.value, assetId: assetId.value }));
const assetName = computed<string>(() => {
	if (pageType.value === ProjectPages.OVERVIEW) return 'Overview';

	const assets = useProjects().activeProject.value?.assets;

	/**
	 * FIXME: to properly type this we'd want to have a base type with common attributes id/name ... etc
	 *
	 *   const list = assets[ pageType.value as string] as IdetifiableAsset[]
	 *   const asset = list.find(...)
	 */
	if (assets) {
		const asset: any = assets[pageType.value as string].find((d: any) => d.id === assetId.value);

		// FIXME should unify upstream via a summary endpoint
		if (asset.header && asset.header.name) return asset.header.name;

		if (asset.name) return asset.name;
	}
	if (pageType.value === AssetType.Code) return 'New File';
	return 'n/a';
});

function openAsset(assetRoute: AssetRoute) {
	if (!isEqual(assetRoute, openedAssetRoute.value)) {
		router.push({
			name: RouteName.Project,
			params: assetRoute
		});
	}
}

async function removeAsset(assetRoute: AssetRoute) {
	// Delete only Asset with an ID and of ProjectAssetType
	if (
		assetRoute.assetId &&
		assetRoute.pageType &&
		isProjectAssetTypes(assetRoute.pageType) &&
		assetRoute.pageType !== ProjectPages.OVERVIEW
	) {
		const isRemoved = await useProjects().deleteAsset(
			assetRoute.pageType as AssetType,
			assetRoute.assetId
		);
		if (isRemoved) {
			if (isEqual(assetRoute, openedAssetRoute.value)) {
				openAsset({ assetId: '', pageType: ProjectPages.OVERVIEW });
			}
			logger.info(`${assetRoute.assetId} was removed.`, { showToast: true });
			return;
		}
	}
	logger.error(`Failed to remove ${assetRoute.assetId}`, { showToast: true });
}

const openWorkflow = async () => {
	// Create a new workflow
	let wfName = 'workflow';
	const { activeProject } = useProjects();
	if (activeProject.value && activeProject.value?.assets) {
		wfName = `workflow ${activeProject.value.assets[AssetType.Workflow].length + 1}`;
	}
	const wf = emptyWorkflow(wfName, '');

	// Add the workflow to the project
	const response = await createWorkflow(wf);
	const workflowId = response.id;
	await useProjects().addAsset(AssetType.Workflow, workflowId);

	openAsset({ pageType: AssetType.Workflow, assetId: workflowId });
};

const openNewAsset = (assetType: AssetType) => {
	switch (assetType) {
		case AssetType.Model:
			isNewModelModalVisible.value = true;
			break;
		case AssetType.Workflow:
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

// TODO:
// This conversion should maybe be done in tera-external-publication.vue - tera-preview-panel.vue does this conversion differently...
// This should be deleted eventually since publications are deprecated
// So delete this when we choose to delete tera-external-publication.vue
const getXDDuri = (docAssetId: string): string =>
	useProjects().activeProject.value?.assets?.[AssetType.Publication]?.find(
		(document) => document?.id === Number.parseInt(docAssetId ?? '', 10)
	)?.xdd_uri ?? '';

async function openCode() {
	const res: string | null = await getCodeFileAsText(assetId.value!, assetName.value!);
	if (!res) return;
	code.value = res;
}

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
