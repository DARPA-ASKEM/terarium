<template>
	<main>
		<tera-slider-panel
			v-model:is-open="isResourcesSliderOpen"
			class="resource-panel"
			content-width="240px"
			direction="left"
			header="Resources"
		>
			<template v-slot:content>
				<tera-resource-sidebar
					:page-type="pageType"
					:asset-id="assetId"
					@open-asset="openAsset"
					@remove-asset="removeAsset"
					@open-new-workflow="openNewWorkflow"
				/>
			</template>
		</tera-slider-panel>
		<section>
			<tera-code v-if="pageType === AssetType.Code" :asset-id="assetId" />
			<tera-dataset v-if="pageType === AssetType.Dataset" :asset-id="assetId" />
			<tera-document-asset v-if="pageType === AssetType.Document" :assetId="assetId" />
			<tera-model v-if="pageType === AssetType.Model" :asset-id="assetId" />
			<tera-project-overview v-if="pageType === ProjectPages.OVERVIEW" />
			<tera-workflow v-if="pageType === AssetType.Workflow" :asset-id="assetId" />
		</section>
		<tera-create-workflow-modal v-if="showWorkflowModal" @close-modal="showWorkflowModal = false" />
	</main>
</template>

<script setup lang="ts">
import { isEqual } from 'lodash';
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import TeraCode from '@/components/code/tera-code.vue';
import TeraDataset from '@/components/dataset/tera-dataset.vue';
import TeraDocumentAsset from '@/components/documents/tera-document-asset.vue';
import TeraModel from '@/components/model/tera-model.vue';
import TeraProjectOverview from '@/components/project/tera-project-overview.vue';
import TeraResourceSidebar from '@/components/project/tera-resource-sidebar.vue';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraWorkflow from '@/components/workflow/tera-workflow.vue';
import { useProjects } from '@/composables/project';
import { RouteName } from '@/router/routes';
import { AssetRoute } from '@/types/common';
import { ProjectPages, isProjectAssetTypes } from '@/types/Project';
import { AssetType, TerariumAsset } from '@/types/Types';
import { logger } from '@/utils/logger';
import TeraCreateWorkflowModal from '@/components/workflow/tera-create-workflow-modal.vue';

const route = useRoute();
const router = useRouter();

const isResourcesSliderOpen = ref(true);
const showWorkflowModal = ref(false);

const pageType = computed(() => (route.params.pageType as ProjectPages | AssetType) ?? '');
const assetId = computed(() => (route.params.assetId as TerariumAsset['id']) ?? '');
const openedAssetRoute = computed(() => ({ pageType: pageType.value, assetId: assetId.value }) as AssetRoute);

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

const openNewWorkflow = () => {
	showWorkflowModal.value = true;
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

section {
	display: flex;
	flex: 1;
	flex-direction: column;
	overflow-x: auto;
	overflow-y: hidden;
}
</style>
