<template>
	<template v-if="assetId && !isEmpty(tabs)">
		<tera-document
			v-if="assetType === ProjectAssetTypes.DOCUMENTS"
			:xdd-uri="getXDDuri(assetId)"
			:previewLineLimit="10"
			:project="project"
			is-editable
			@open-asset="emit('open-asset')"
			@asset-loaded="emit('asset-loaded')"
		/>
		<tera-model
			v-else-if="assetType === ProjectAssetTypes.MODELS"
			:asset-id="assetId"
			:project="project"
			is-editable
			@asset-loaded="emit('asset-loaded')"
		/>
		<tera-dataset
			v-else-if="assetType === ProjectAssetTypes.DATASETS"
			:asset-id="assetId"
			:project="project"
			is-editable
			@asset-loaded="emit('asset-loaded')"
		/>
		<simulation-plan
			v-else-if="assetType === ProjectAssetTypes.PLANS"
			:asset-id="assetId"
			:project="project"
			@asset-loaded="emit('asset-loaded')"
		/>
		<simulation-run
			v-else-if="assetType === ProjectAssetTypes.SIMULATION_RUNS"
			:asset-id="assetId"
			:project="project"
			@asset-loaded="emit('asset-loaded')"
		/>
	</template>
	<code-editor
		v-else-if="assetType === ProjectAssetTypes.CODE"
		:initial-code="code"
		@on-model-created="openNewModelFromCode"
	/>
	<tera-model
		v-else-if="assetType === ProjectAssetTypes.MODELS"
		:asset-id="newModelId"
		:project="project"
		@update-tab-name="emit('update-tab-name')"
		@create-new-model="createNewModel"
		is-editable
	/>
	<tera-project-overview
		v-else-if="assetType === 'overview'"
		:project="project"
		@open-workflow="openWorkflow"
	/>
	<tera-simulation-workflow
		v-else-if="assetType === 'workflow'"
		:project="project"
		ref="workflowRef"
	/>
	<section v-else class="no-open-tabs">
		<img src="@assets/svg/seed.svg" alt="Seed" />
		<p>You can open resources from the resource panel.</p>
		<Button label="Open project overview" @click="openOverview" />
	</section>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import * as ProjectService from '@/services/project';
import { createModel, addModelToProject } from '@/services/model';
import { ProjectAssetTypes, IProject } from '@/types/Project';
import { PetriNet } from '@/petrinet/petrinet-service';
import { useRouter } from 'vue-router';
import useResourcesStore from '@/stores/resources';
import { RouteName } from '@/router/routes';
import { isEmpty } from 'lodash';
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
	tabs: Tab[];
	newModelId: string;
	code?: string;
}>();

// open asset may not work
const emit = defineEmits(['open-asset', 'asset-loaded', 'assign-new-model-id', 'update-tab-name']);

const router = useRouter();
const resources = useResourcesStore();

const isNewModel = ref<boolean>(true);

const getXDDuri = (assetId: Tab['assetId']): string =>
	ProjectService.getDocumentAssetXddUri(props?.project, assetId) ?? '';

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

// Create the new model
const createNewModel = async (newModel: PetriNet) => {
	const newModelResp = await createModel(newModel);
	if (newModelResp) {
		emit('assign-new-model-id', newModelResp.id.toString());
		await addModelToProject(props.project.id, props.newModelId, resources);
		isNewModel.value = false;
	}
};

async function openNewModelFromCode(modelId: string, modelName: string) {
	await addModelToProject(props.project.id, modelId, resources);

	router.push({
		name: RouteName.ProjectRoute,
		params: {
			assetName: modelName,
			assetId: modelId,
			assetType: ProjectAssetTypes.MODELS
		}
	});
}
</script>
