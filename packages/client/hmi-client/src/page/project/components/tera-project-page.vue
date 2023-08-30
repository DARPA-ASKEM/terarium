<template>
	<tera-model
		v-if="pageType === AssetType.Models"
		:asset-id="assetId ?? ''"
		:project="project"
		@asset-loaded="emit('asset-loaded')"
	/>
	<tera-code
		:projectId="project.id"
		:asset-id="assetId ?? ''"
		v-else-if="pageType === AssetType.Code"
		@vue:mounted="() => emit('asset-loaded')"
	/>
	<tera-pdf-embed
		v-else-if="pageType === AssetType.Artifacts && assetName?.endsWith('.pdf')"
		:title="assetName"
		:file-promise="getPDFBytes()"
	/>
	<tera-project-overview
		v-else-if="pageType === ProjectPages.OVERVIEW"
		:project="project"
		@vue:mounted="emit('asset-loaded')"
		@open-new-asset="(assetType) => emit('open-new-asset', assetType)"
	/>
	<tera-simulation-workflow
		v-else-if="pageType === AssetType.Workflows"
		:asset-id="assetId ?? ''"
		:project="project"
		@vue:mounted="emit('asset-loaded')"
		@page-loaded="emit('asset-loaded')"
	/>
	<!--Add new process/asset views here-->
	<template v-else-if="assetId && !isEmpty(tabs)">
		<tera-document
			v-if="pageType === AssetType.Publications"
			:xdd-uri="getXDDuri(assetId)"
			:previewLineLimit="10"
			:project="project"
			@open-code="openCode"
			@asset-loaded="emit('asset-loaded')"
		/>
		<tera-dataset
			v-else-if="pageType === AssetType.Datasets"
			:project="project"
			:asset-id="assetId"
			@asset-loaded="emit('asset-loaded')"
		/>
	</template>
	<section v-else>
		<img src="@assets/svg/seed.svg" alt="Seed" />
		<p>You can open resources from the resource panel.</p>
		<Button label="Open project overview" @click="openOverview" />
	</section>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { ProjectPages, IProject } from '@/types/Project';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import { isEmpty } from 'lodash';
import { Tab } from '@/types/common';
import Button from 'primevue/button';
import TeraDocument from '@/components/documents/tera-document.vue';
import TeraDataset from '@/components/dataset/tera-dataset.vue';
import TeraModel from '@/components/models/tera-model.vue';
import TeraProjectOverview from '@/page/project/components/tera-project-overview.vue';
import TeraSimulationWorkflow from '@/components/workflow/tera-simulation-workflow.vue';
import * as ProjectService from '@/services/project';
import { getArtifactArrayBuffer } from '@/services/artifact';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import useResourceStore from '@/stores/resources';
import { AssetType } from '@/types/Types';
import { getCodeFileAsText } from '@/services/code';
import TeraCode from '@/components/code/tera-code.vue';

const props = defineProps<{
	project: IProject;
	assetId?: string;
	pageType?: AssetType | ProjectPages;
	tabs?: Tab[];
	activeTabIndex?: number;
}>();

const emit = defineEmits(['asset-loaded', 'open-new-asset']);

const resourceStore = useResourceStore();

const router = useRouter();

const code = ref<string>();

const assetName = computed<string>(() => {
	if (props.pageType === ProjectPages.OVERVIEW) return 'Overview';

	const assets = resourceStore.activeProjectAssets;

	/**
	 * FIXME: to properly type this we'd want to have a base type with common attributes id/name ... etc
	 *
	 *   const list = assets[ props.pageType as string] as IdetifiableAsset[]
	 *   const asset = list.find(...)
	 */
	if (assets) {
		const asset: any = assets[props.pageType as string].find((d: any) => d.id === props.assetId);
		if (asset.name) return asset.name;
	}
	if (props.pageType === AssetType.Code) return 'New File';
	return 'n/a';
});

// This conversion should maybe be done in the document component - tera-preview-panel.vue does this conversion differently though...
const getXDDuri = (assetId: Tab['assetId']): string =>
	ProjectService.getDocumentAssetXddUri(props?.project, assetId) ?? '';

const openOverview = () => {
	router.push({
		name: RouteName.ProjectRoute,
		params: { pageType: ProjectPages.OVERVIEW, assetId: undefined }
	});
};
async function openCode() {
	const res: string | null = await getCodeFileAsText(props.assetId!, assetName.value!);
	if (!res) return;
	code.value = res;
}

function getPDFBytes(): Promise<ArrayBuffer | null> {
	return getArtifactArrayBuffer(props.assetId!, assetName.value!);
}
</script>

<style scoped>
section {
	display: flex;
	align-items: center;
	flex-direction: column;
	justify-content: center;
	flex: 1;
	gap: 2rem;
	margin-bottom: 8rem;
	color: var(--text-color-subdued);
}
</style>
