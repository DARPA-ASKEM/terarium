<template>
	<tera-model
		v-if="pageType === ProjectAssetTypes.MODELS"
		:asset-id="assetId ?? ''"
		:project="project"
		@update-tab-name="updateTabName"
		@asset-loaded="emit('asset-loaded')"
		is-editable
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
		@open-workflow="openWorkflow"
		@new-model="newModel"
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
import { ref, Ref } from 'vue';
import { ProjectAssetTypes, ProjectPages, IProject } from '@/types/Project';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import { isEmpty, cloneDeep } from 'lodash';
import { CodeRequest, Tab } from '@/types/common';
import Button from 'primevue/button';
import TeraDocument from '@/components/documents/tera-document.vue';
import TeraDataset from '@/components/dataset/tera-dataset.vue';
import TeraModel from '@/components/models/tera-model.vue';
import CodeEditor from '@/page/project/components/code-editor.vue';
import TeraProjectOverview from '@/page/project/components/tera-project-overview.vue';
import TeraSimulationWorkflow from '@/components/workflow/tera-simulation-workflow.vue';
import { emptyWorkflow, createWorkflow } from '@/services/workflow';
import * as ProjectService from '@/services/project';
import { getArtifactArrayBuffer, getArtifactFileAsText } from '@/services/artifact';
import { newAMR } from '@/model-representation/petrinet/petrinet-service';
import { createModel } from '@/services/model';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';

const props = defineProps<{
	project: IProject;
	assetId?: string;
	assetName?: string;
	pageType?: ProjectAssetTypes | ProjectPages;
	tabs?: Tab[];
	activeTabIndex?: number;
}>();

const emit = defineEmits(['update:tabs', 'asset-loaded', 'update-tab-name', 'close-current-tab']);

const router = useRouter();

const code = ref<string>();

const queuedCodeRequests: Ref<CodeRequest[]> = ref([]);

// This conversion should maybe be done in the document component - tera-preview-panel.vue does this conversion differently though...
const getXDDuri = (assetId: Tab['assetId']): string =>
	ProjectService.getDocumentAssetXddUri(props?.project, assetId) ?? '';

// These 3 open functions can potentially make use of openAssetFromSidebar in tera-project.vue
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

const newModel = async (modelName: string) => {
	// 1. Load an empty AMR
	const amr = newAMR(modelName);
	(amr as any).id = undefined; // FIXME: id hack

	const response = await createModel(amr);
	const modelId = response?.id;

	// 2. Add the model to the project
	await ProjectService.addAsset(props.project.id, ProjectAssetTypes.MODELS, modelId);

	// 3. Reroute
	router.push({
		name: RouteName.ProjectRoute,
		params: {
			assetName: 'Model',
			pageType: ProjectAssetTypes.MODELS,
			assetId: modelId
		}
	});
};

const openOverview = () => {
	router.push({
		name: RouteName.ProjectRoute,
		params: { assetName: 'Overview', pageType: ProjectPages.OVERVIEW, assetId: undefined }
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
	return getArtifactArrayBuffer(props.assetId!, props.assetName!);
}

async function openTextArtifact() {
	const res: string | null = await getArtifactFileAsText(props.assetId!, props.assetName!);
	if (!res) return;
	code.value = res;
}

// Just preserving this as this didn't even work when it was in tera-project.vue - same error occurs on staging
// I think this is meant to make the tab name and the model name to be the same as you're editing it which isn't important/necessary
const updateTabName = (tabName: string) => {
	const tabsClone = cloneDeep(props.tabs);

	// FIXME: Active tab index is undefined so tab name doesn't get updated when model or workflow name get updated
	// console.log(tabName, tabsClone, props.activeTabIndex)

	if (tabsClone?.[props.activeTabIndex!]?.assetName) {
		tabsClone[props.activeTabIndex!].assetName = tabName;
		emit('update:tabs', tabsClone);
	}
};
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
