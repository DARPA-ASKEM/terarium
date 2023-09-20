<template>
	<tera-model
		v-if="pageType === AssetType.Models"
		:asset-id="assetId ?? ''"
		@asset-loaded="emit('asset-loaded')"
	/>
	<tera-code
		:asset-id="assetId ?? ''"
		v-else-if="pageType === AssetType.Code"
		@asset-loaded="emit('asset-loaded')"
	/>
	<code-editor
		v-else-if="pageType === AssetType.Artifacts && !assetName?.endsWith('.pdf')"
		:initial-code="code"
		@vue:mounted="
			emit('asset-loaded');
			openTextArtifact();
		"
	/>
	<tera-pdf-embed
		v-else-if="pageType === AssetType.Artifacts && assetName?.endsWith('.pdf')"
		:title="assetName"
		:file-promise="getPDFBytes()"
	/>
	<tera-project-overview
		v-else-if="pageType === ProjectPages.OVERVIEW"
		@vue:mounted="emit('asset-loaded')"
		@open-new-asset="(assetType) => emit('open-new-asset', assetType)"
	/>
	<tera-workflow
		v-else-if="pageType === AssetType.Workflows"
		:asset-id="assetId ?? ''"
		@vue:mounted="emit('asset-loaded')"
		@page-loaded="emit('asset-loaded')"
	/>
	<!--Add new process/asset views here-->
	<template v-else-if="assetId && !isEmpty(tabs)">
		<tera-document
			v-if="pageType === AssetType.Publications"
			:xdd-uri="getXDDuri(assetId)"
			:previewLineLimit="10"
			@open-code="openCode"
			@asset-loaded="emit('asset-loaded')"
		/>
		<tera-dataset
			v-else-if="pageType === AssetType.Datasets"
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
import { ProjectPages } from '@/types/Project';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import { isEmpty } from 'lodash';
import { Tab } from '@/types/common';
import Button from 'primevue/button';
import TeraDocument from '@/components/documents/tera-document.vue';
import TeraDataset from '@/components/dataset/tera-dataset.vue';
import TeraModel from '@/components/model/tera-model.vue';
import CodeEditor from '@/page/project/components/code-editor.vue';
import TeraProjectOverview from '@/page/project/components/tera-project-overview.vue';
import { getArtifactArrayBuffer, getArtifactFileAsText } from '@/services/artifact';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import { AssetType } from '@/types/Types';
import { getCodeFileAsText } from '@/services/code';
import TeraCode from '@/components/code/tera-code.vue';
import { useProjects } from '@/composables/project';
import TeraWorkflow from '@/workflow/tera-workflow.vue';

const props = defineProps<{
	assetId?: string;
	pageType?: AssetType | ProjectPages;
	tabs?: Tab[];
	activeTabIndex?: number;
}>();

const emit = defineEmits(['asset-loaded', 'open-new-asset']);

const router = useRouter();

const code = ref<string>();

const assetName = computed<string>(() => {
	if (props.pageType === ProjectPages.OVERVIEW) return 'Overview';

	const assets = useProjects().activeProject.value?.assets;

	/**
	 * FIXME: to properly type this we'd want to have a base type with common attributes id/name ... etc
	 *
	 *   const list = assets[ props.pageType as string] as IdetifiableAsset[]
	 *   const asset = list.find(...)
	 */
	if (assets) {
		const asset: any = assets[props.pageType as string].find((d: any) => d.id === props.assetId);

		// FIXME should unify upstream via a summary endpoint
		if (asset.header && asset.header.name) return asset.header.name;

		if (asset.name) return asset.name;
	}
	if (props.pageType === AssetType.Code) return 'New File';
	return 'n/a';
});

// This conversion should maybe be done in the document component - tera-preview-panel.vue does this conversion differently though...
const getXDDuri = (assetId: Tab['assetId']): string =>
	useProjects().activeProject.value?.assets?.[AssetType.Publications]?.find(
		(document) => document?.id === Number.parseInt(assetId ?? '', 10)
	)?.xdd_uri ?? '';

const openOverview = () => {
	router.push({
		name: RouteName.Project,
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

async function openTextArtifact() {
	const res: string | null = await getArtifactFileAsText(props.assetId!, assetName.value!);
	if (!res) return;
	code.value = res;
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
