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
	<tera-project-overview
		v-else-if="pageType === ProjectPages.OVERVIEW"
		:project="project"
		@vue:mounted="emit('asset-loaded')"
		@open-workflow="openWorkflow"
		@update-project="updateProject"
	/>
	<tera-simulation-workflow
		v-else-if="pageType === ProjectAssetTypes.SIMULATION_WORKFLOW"
		:project="project"
		@vue:mounted="emit('asset-loaded')"
	/>
	<!--Add new process/asset views here-->
	<template v-else-if="assetId && (!isEmpty(tabs) || isDrilldown)">
		<tera-document
			v-if="pageType === ProjectAssetTypes.DOCUMENTS"
			:xdd-uri="getXDDuri(assetId)"
			:previewLineLimit="10"
			is-editable
			@open-code="openCode"
			@asset-loaded="emit('asset-loaded')"
		/>
		<tera-dataset
			v-else-if="pageType === ProjectAssetTypes.DATASETS"
			:asset-id="assetId"
			is-editable
			@asset-loaded="emit('asset-loaded')"
		/>
		<simulation-plan
			v-else-if="pageType === ProjectAssetTypes.PLANS"
			@asset-loaded="emit('asset-loaded')"
		/>
		<simulation-run
			v-else-if="pageType === ProjectAssetTypes.SIMULATION_RUNS"
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
import * as ProjectService from '@/services/project';
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
import SimulationPlan from '@/page/project/components/Simulation.vue';
import SimulationRun from '@/temp/SimulationResult3.vue';
import TeraProjectOverview from '@/page/project/components/tera-project-overview.vue';
import TeraSimulationWorkflow from '@/components/workflow/tera-simulation-workflow.vue';

const props = defineProps<{
	project: IProject;
	assetId?: string;
	pageType?: ProjectAssetTypes | ProjectPages;
	tabs?: Tab[];
	activeTabIndex?: number;
	isDrilldown?: boolean; // temp just to preview one workflow node
}>();

const emit = defineEmits([
	'update:tabs',
	'asset-loaded',
	'update-tab-name',
	'close-current-tab',
	'update-project'
]);

const router = useRouter();

const code = ref<string>();
const queuedCodeRequests: Ref<CodeRequest[]> = ref([]);

// This conversion should maybe be done in the document component - tera-preview-panel.vue does this conversion differently though...
const getXDDuri = (assetId: Tab['assetId']): string =>
	ProjectService.getDocumentAssetXddUri(props?.project, assetId) ?? '';

// These 3 open functions can potentially make use of openAssetFromSidebar in tera-project.vue
const openWorkflow = () => {
	router.push({
		name: RouteName.ProjectRoute,
		params: {
			assetName: 'Workflow',
			pageType: ProjectAssetTypes.SIMULATION_WORKFLOW,
			assetId: undefined
		}
	});
};

function updateProject(id: IProject['id']) {
	emit('update-project', id);
}

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

// Just preserving this as this didn't even work when it was in tera-project.vue - same error occurs on staging
// I think this is meant to make the tab name and the model name to be the same as you're editing it which isn't important/necessary
const updateTabName = (tabName: string) => {
	const tabsClone = cloneDeep(props.tabs);
	if (tabsClone) {
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
