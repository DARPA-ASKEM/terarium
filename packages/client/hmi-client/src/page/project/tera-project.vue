<template>
	<main>
		<slider-panel
			v-model:is-open="isResourcesSliderOpen"
			content-width="300px"
			header="Resources"
			direction="left"
		>
			<template v-slot:content>
				<tera-resource-sidebar
					:project="project"
					:tabs="tabs"
					:opened-asset-route="openedAssetRoute"
					@open-asset="openAsset"
					@close-tab="removeClosedTab"
				/>
			</template>
		</slider-panel>
		<section>
			<tera-tab-group
				v-if="!isEmpty(tabs)"
				:tabs="tabs"
				:active-tab-index="activeTabIndex"
				@close-tab="removeClosedTab"
				@select-tab="openAsset"
			/>
			<template v-if="assetId && !isEmpty(tabs)">
				<document
					v-if="assetType === ProjectAssetTypes.DOCUMENTS"
					:asset-id="assetId"
					:previewLineLimit="10"
					:project="project"
					is-editable
					@open-asset="openAsset"
				/>
				<dataset
					v-else-if="assetType === ProjectAssetTypes.DATASETS"
					:asset-id="assetId"
					:project="project"
					is-editable
				/>
				<model
					v-else-if="assetType === ProjectAssetTypes.MODELS"
					:asset-id="assetId"
					:project="project"
					is-editable
				/>
				<simulation-plan
					v-else-if="assetType === ProjectAssetTypes.PLANS"
					:asset-id="assetId"
					:project="project"
				/>
				<simulation-run
					v-else-if="assetType === ProjectAssetTypes.SIMULATION_RUNS"
					:asset-id="assetId"
					:project="project"
				/>
			</template>
			<code-editor
				v-else-if="assetType === ProjectAssetTypes.CODE"
				:initial-code="code"
				@on-model-created="openNewModelFromCode"
			/>
			<tera-project-overview v-else-if="assetType === 'overview'" :project="project" />
			<section v-else class="no-open-tabs">
				<img src="@assets/svg/seed.svg" alt="Seed" />
				<h5>Open resources from the resource panel.</h5>
			</section>
		</section>
		<slider-panel
			class="slider"
			content-width="300px"
			direction="right"
			header="Notes"
			v-model:is-open="isNotesSliderOpen"
		>
			<template v-slot:content>
				<div v-for="(annotation, idx) of annotations" :key="idx" class="annotation-panel">
					<p class="annotation-content">{{ annotation.content }}</p>
					<div class="annotation-footer">
						<div>{{ annotation.username }}</div>
						<div>{{ formatDate(annotation.timestampMillis) }}</div>
					</div>
				</div>
				<div class="annotation-panel">
					<Textarea v-model="annotationContent" rows="5" cols="30" aria-labelledby="annotation" />
					<Button @click="addAnnotation()" label="Add note" />
				</div>
			</template>
		</slider-panel>
	</main>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import SliderPanel from '@/components/widgets/slider-panel.vue';
import TeraResourceSidebar from '@/page/project/components/tera-resource-sidebar.vue';
import TeraProjectOverview from '@/page/project/components/tera-project-overview.vue';
import Document from '@/components/documents/Document.vue';
import Dataset from '@/components/dataset/Dataset.vue';
import Model from '@/components/models/Model.vue';
import SimulationPlan from '@/page/project/components/Simulation.vue';
import SimulationRun from '@/temp/SimulationResult2.vue';
import CodeEditor from '@/page/project/components/code-editor.vue';
import TeraTabGroup from '@/components/widgets/tera-tab-group.vue';
import { Tab, ResourceType, Annotation } from '@/types/common';
import { isEmpty, isEqual } from 'lodash';
import { useTabStore } from '@/stores/tabs';
import Textarea from 'primevue/textarea';
import Button from 'primevue/button';
import { RouteName } from '@/router/routes';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import { useRouter } from 'vue-router';
import API from '@/api/api';
import * as ProjectService from '@/services/project';
import useResourcesStore from '@/stores/resources';
import { getModel } from '@/services/model';
import { logger } from '@/utils/logger';

// Asset props are extracted from route
const props = defineProps<{
	project: IProject;
	assetName?: string;
	assetId?: string;
	assetType?: ProjectAssetTypes | 'overview' | '';
}>();

const tabStore = useTabStore();
const router = useRouter();
const resources = useResourcesStore();

const isResourcesSliderOpen = ref(true);
const isNotesSliderOpen = ref(false);
const annotations = ref<Annotation[]>([]);
const annotationContent = ref<string>('');
const code = ref<string>();

// Associated with tab storage
const projectContext = computed(() => props.project?.id.toString());
const tabs = computed(() => tabStore.getTabs(projectContext.value) ?? []);
const activeTabIndex = computed(() => tabStore.getActiveTabIndex(projectContext.value));
const openedAssetRoute = computed<Tab>(() => ({
	assetName: props.assetName ?? '',
	assetType: props.assetType,
	assetId: props.assetId
}));

function openAsset(assetToOpen: Tab = tabs.value[activeTabIndex.value], newCode?: string) {
	router.push({ name: RouteName.ProjectRoute, params: assetToOpen });
	if (newCode) code.value = newCode;
}

function removeClosedTab(tabIndexToRemove: number) {
	tabStore.removeTab(projectContext.value, tabIndexToRemove);
}

async function openNewModelFromCode(modelId, modelName) {
	await ProjectService.addAsset(props.project.id, ProjectAssetTypes.MODELS, modelId);
	const model = await getModel(modelId);
	console.log(model);
	if (model) {
		resources.activeProjectAssets?.[ProjectAssetTypes.MODELS].push(model);
	} else {
		logger.warn('Could not add new model to project.');
	}

	router.push({
		name: RouteName.ProjectRoute,
		params: {
			assetName: modelName,
			assetId: modelId,
			assetType: ProjectAssetTypes.MODELS
		}
	});
}

// When a new tab is chosen, reflect that by opening its associated route
tabStore.$subscribe(() => openAsset());

// Nice to have: Show overview tab on mount if no tabs were open in the previous session

watch(
	() => [
		openedAssetRoute.value, // Once route attributes change, add/switch to another tab
		projectContext.value // Make sure we are in the proper project context before opening assets
	],
	() => {
		if (projectContext.value) {
			// If name isn't recognized, its a new asset so add a new tab
			if (
				props.assetName &&
				props.assetType &&
				!tabs.value.some((tab) => isEqual(tab, openedAssetRoute.value))
			) {
				tabStore.addTab(projectContext.value, openedAssetRoute.value);
			}
			// Tab switch
			else if (props.assetName) {
				const index = tabs.value.findIndex((tab) => isEqual(tab, openedAssetRoute.value));
				tabStore.setActiveTabIndex(projectContext.value, index);
			}
			// Goes to tab from previous session
			else openAsset();
		}
	}
);

const formatDate = (millis: number) => new Date(millis).toLocaleDateString();

// FIXME:
// - Need to establish terarium artifact types
// - Move to service layer
const fetchAnnotations = async () => {
	const response = await API.get('/annotations', {
		params: {
			artifact_type: ResourceType.XDD,
			artifact_id: props.assetId
		}
	});
	if (response) {
		annotations.value = response.data;
	}
};

const addAnnotation = async () => {
	const content = annotationContent.value;
	await API.post('/annotations', {
		content,
		artifact_id: props.assetId,
		artifact_type: ResourceType.XDD
	});
	annotationContent.value = '';

	// Refresh
	await fetchAnnotations();
};
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	flex: 1;
	overflow: auto;
}

.no-open-tabs {
	justify-content: center;
	gap: 2rem;
	margin-bottom: 8rem;
	align-items: center;
	color: var(--text-color-subdued);
}

.asset {
	padding-top: 1rem;
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
