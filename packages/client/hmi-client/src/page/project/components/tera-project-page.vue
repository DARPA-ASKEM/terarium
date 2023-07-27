<template>
	<tera-model
		v-if="pageType === ProjectAssetTypes.MODELS"
		:asset-id="assetId ?? ''"
		:project="project"
		@asset-loaded="emit('asset-loaded')"
	/>
	<code-editor
		v-else-if="pageType === ProjectAssetTypes.CODE"
		:initial-code="code"
		@vue:mounted="
			emit('asset-loaded');
			openNextCodeFile();
		"
	/>
	<code-editor
		v-else-if="pageType === ProjectAssetTypes.ARTIFACTS && !assetName?.endsWith('.pdf')"
		:initial-code="code"
		@vue:mounted="
			emit('asset-loaded');
			openTextArtifact();
		"
	/>
	<tera-pdf-embed
		v-else-if="pageType === ProjectAssetTypes.ARTIFACTS && assetName?.endsWith('.pdf')"
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
		v-else-if="pageType === ProjectAssetTypes.SIMULATION_WORKFLOW"
		:asset-id="assetId ?? ''"
		:project="project"
		@vue:mounted="emit('asset-loaded')"
	/>
	<!--Add new process/asset views here-->
	<template v-else-if="assetId && !isEmpty(tabs)">
		<tera-document
			v-if="pageType === ProjectAssetTypes.DOCUMENTS"
			:xdd-uri="getXDDuri(assetId)"
			:previewLineLimit="10"
			:project="project"
			@open-code="openCode"
			@asset-loaded="emit('asset-loaded')"
		/>
		<tera-dataset
			v-else-if="pageType === ProjectAssetTypes.DATASETS"
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
import { ref, Ref, computed } from 'vue';
import { ProjectAssetTypes, ProjectPages, IProject } from '@/types/Project';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import { isEmpty } from 'lodash';
import { CodeRequest, Tab } from '@/types/common';
import Button from 'primevue/button';
import TeraDocument from '@/components/documents/tera-document.vue';
import TeraDataset from '@/components/dataset/tera-dataset.vue';
import TeraModel from '@/components/models/tera-model.vue';
import CodeEditor from '@/page/project/components/code-editor.vue';
import TeraProjectOverview from '@/page/project/components/tera-project-overview.vue';
import TeraSimulationWorkflow from '@/components/workflow/tera-simulation-workflow.vue';
import * as ProjectService from '@/services/project';
import { getArtifactArrayBuffer, getArtifactFileAsText } from '@/services/artifact';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import useResoureStore from '@/stores/resources';

const props = defineProps<{
	project: IProject;
	assetId?: string;
	pageType?: ProjectAssetTypes | ProjectPages;
	tabs?: Tab[];
	activeTabIndex?: number;
}>();

const emit = defineEmits(['asset-loaded', 'open-new-asset']);

const resourceStore = useResoureStore();

const router = useRouter();

const code = ref<string>();

const queuedCodeRequests: Ref<CodeRequest[]> = ref([]);

const assetName = computed<string>(() => {
	if (props.pageType === ProjectPages.OVERVIEW) return 'Overview';
	if (props.pageType === ProjectAssetTypes.CODE) return 'New File';
	const assets = resourceStore.activeProjectAssets;

	if (assets) {
		const asset: any = assets[props.pageType as string].find((d: any) => d.id === props.assetId);
		return asset.name ?? 'n/a';
	}
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
async function openCode(codeRequests: CodeRequest[]) {
	queuedCodeRequests.value = codeRequests;
	await openNextCodeFile();
}

async function openNextCodeFile() {
	if (queuedCodeRequests.value.length > 0) {
		const currentRequest: CodeRequest | undefined = queuedCodeRequests.value.pop();

		if (!currentRequest) return;

		code.value = currentRequest.code;
		await router.push({
			name: RouteName.ProjectRoute,
			params: currentRequest.asset
		});
	}
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
