<template>
	<component
		v-if="currentComponent"
		:is="currentComponent.name"
		v-bind="currentComponent.properties"
		@asset-loaded="emit('asset-loaded')"
		@open-code="openCode"
		@open-workflow="openWorkflow"
		@update-tab-name="updateTabName"
	/>
	<section v-else>
		<img src="@assets/svg/seed.svg" alt="Seed" />
		<p>You can open resources from the resource panel.</p>
		<Button label="Open project overview" @click="openOverview" />
	</section>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import * as ProjectService from '@/services/project';
import { ProjectAssetTypes, IProject } from '@/types/Project';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import { isEmpty, cloneDeep } from 'lodash';
import { Tab } from '@/types/common';
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
	assetType?: ProjectAssetTypes | 'overview' | 'workflow' | '';
	tabs?: Tab[];
	activeTabIndex?: number;
	isDrilldown?: boolean; // temp just to preview one workflow node
}>();

const emit = defineEmits(['update:tabs', 'asset-loaded', 'update-tab-name']);

const router = useRouter();

const code = ref<string>();

// Add new process/asset views here
const currentComponent = computed(() => {
	// All components share these properties
	const properties = { project: props.project, isEditable: true };

	if (props.assetId && (!isEmpty(props.tabs) || props.isDrilldown)) {
		const assetProperties = { ...properties, assetId: props.assetId };

		switch (props.assetType) {
			case ProjectAssetTypes.DOCUMENTS:
				return {
					name: TeraDocument,
					// Add additional props for a specific asset like this
					properties: { ...properties, previewLineLimit: 10, xddUri: getXDDuri(props.assetId) }
				};
			case ProjectAssetTypes.DATASETS:
				return { name: TeraDataset, properties: assetProperties };
			case ProjectAssetTypes.PLANS:
				return { name: SimulationPlan, properties: assetProperties };
			case ProjectAssetTypes.SIMULATION_RUNS:
				return { name: SimulationRun, properties: assetProperties };
			default:
				break;
		}
	}
	// Not all are assets or not treated like one in the backend yet
	switch (props.assetType) {
		case ProjectAssetTypes.MODELS:
			return { name: TeraModel, properties: { ...properties, assetId: props.assetId } };
		case ProjectAssetTypes.CODE:
			return {
				name: CodeEditor,
				properties: { ...properties, initialCode: code.value }
			};
		case 'overview':
			return { name: TeraProjectOverview, properties };
		case 'workflow':
			return { name: TeraSimulationWorkflow, properties };
		default:
			return null;
	}
});

const getXDDuri = (assetId: Tab['assetId']): string =>
	ProjectService.getDocumentAssetXddUri(props?.project, assetId) ?? '';

// These 3 open functions can potentially make use of openAssetFromSidebar in tera-project.vue
const openWorkflow = () => {
	router.push({
		name: RouteName.ProjectRoute,
		params: { assetName: 'Workflow', assetType: 'workflow', assetId: undefined }
	});
};
const openOverview = () => {
	router.push({
		name: RouteName.ProjectRoute,
		params: { assetName: 'Overview', assetType: 'overview', assetId: undefined }
	});
};
function openCode(assetToOpen: Tab, newCode?: string) {
	code.value = newCode;
	router.push({
		name: RouteName.ProjectRoute,
		params: assetToOpen
	});
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
