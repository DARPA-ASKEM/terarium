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
			<tera-project-overview v-else-if="pageType === ProjectPages.OVERVIEW" />
			<tera-workflow v-else-if="pageType === AssetType.Workflow" :asset-id="assetId" />
			<!--Add new process/asset views here-->
			<template v-else-if="assetId">
				<tera-document-asset
					v-if="pageType === AssetType.Document"
					:assetId="assetId"
					:previewLineLimit="10"
					@remove="removeAsset({ assetId, pageType })"
				/>
				<tera-dataset v-else-if="pageType === AssetType.Dataset" :asset-id="assetId" />
			</template>
		</section>
		<tera-slider-panel v-model:is-open="isNotesSliderOpen" content-width="240px" direction="right" header="Notes">
			<template v-slot:content>
				<tera-notes-sidebar :asset-id="assetId" :page-type="pageType" />
			</template>
		</tera-slider-panel>
		<!-- New asset modal -->
		<tera-save-asset-modal
			:is-visible="showSaveAssetModal"
			:assetType="assetTypeToCreate"
			open-on-save
			@close-modal="showSaveAssetModal = false"
		/>
	</main>
</template>

<script setup lang="ts">
import TeraCode from '@/components/code/tera-code.vue';
import TeraDataset from '@/components/dataset/tera-dataset.vue';
import TeraDocumentAsset from '@/components/documents/tera-document-asset.vue';
import TeraModel from '@/components/model/tera-model.vue';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraWorkflow from '@/components/workflow/tera-workflow.vue';
import { useProjects } from '@/composables/project';
import TeraProjectOverview from '@/components/project/tera-project-overview.vue';
import TeraResourceSidebar from '@/components/project/tera-resource-sidebar.vue';
import { RouteName } from '@/router/routes';
import { ProjectPages, isProjectAssetTypes } from '@/types/Project';
import { AssetType } from '@/types/Types';
import { AssetRoute } from '@/types/common';
import { logger } from '@/utils/logger';
import { isEqual } from 'lodash';
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import TeraSaveAssetModal from '@/components/project/tera-save-asset-modal.vue';
import TeraNotesSidebar from '@/components/project/tera-notes-sidebar.vue';

const route = useRoute();
const router = useRouter();

const isResourcesSliderOpen = ref(true);
const isNotesSliderOpen = ref(false);
const showSaveAssetModal = ref(false);
const assetTypeToCreate = ref<AssetType>(AssetType.Model);

const pageType = computed(() => (route.params.pageType as ProjectPages | AssetType) ?? '');
const assetId = computed(() => (route.params.assetId as string) ?? '');
const openedAssetRoute = computed(() => ({ pageType: pageType.value, assetId: assetId.value }));

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
		const isRemoved = await useProjects().deleteAsset(assetRoute.pageType as AssetType, assetRoute.assetId);
		if (isRemoved) {
			if (isEqual(assetRoute, openedAssetRoute.value)) {
				openAsset({ assetId: '', pageType: ProjectPages.OVERVIEW });
			}
			logger.info(`${assetRoute.assetId} was removed.`, { showToast: true });
		}
	}
}

// Configures save asset modal to create/open the correct asset type
const openNewAsset = (assetType: AssetType) => {
	assetTypeToCreate.value = assetType;
	showSaveAssetModal.value = true;
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
	outline-color: var(--surface-border);
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
