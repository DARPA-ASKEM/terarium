<template>
	<main>
		<slider-panel
			v-model:is-open="isResourcesSliderOpen"
			content-width="300px"
			header="Resources"
			direction="left"
		>
			<template v-slot:content>
				<tera-resource-sidebar />
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
					:project="resources.activeProject"
					is-editable
				/>
				<dataset
					v-else-if="assetType === ProjectAssetTypes.DATASETS"
					:asset-id="assetId"
					:project="resources.activeProject"
					is-editable
				/>
				<model
					v-else-if="assetType === ProjectAssetTypes.MODELS"
					:asset-id="assetId"
					:project="resources.activeProject"
					is-editable
				/>
				<simulation-plan
					v-else-if="assetType === ProjectAssetTypes.PLANS"
					:asset-id="assetId"
					:project="resources.activeProject"
				/>
				<simulation-run
					v-else-if="assetType === ProjectAssetTypes.SIMULATION_RUNS"
					:asset-id="assetId"
					:project="resources.activeProject"
				/>
			</template>
			<code-editor v-else-if="assetType === ProjectAssetTypes.CODE" />
			<tera-project-overview v-else :project="project" />
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
import { ref, watch, onMounted } from 'vue';
import SliderPanel from '@/components/widgets/slider-panel.vue';
import TeraResourceSidebar from '@/page/project/components/tera-resource-sidebar.vue';
import TeraProjectOverview from '@/page/project/components/tera-project-overview.vue';
import Document from '@/components/documents/Document.vue';
import Dataset from '@/components/dataset/Dataset.vue';
import Model from '@/components/models/Model.vue';
import SimulationPlan from '@/page/project/components/Simulation.vue';
import SimulationRun from '@/temp/SimulationResult.vue';
import CodeEditor from '@/page/project/components/code-editor.vue';
import TeraTabGroup from '@/components/widgets/tera-tab-group.vue';
import { Tab, ResourceType, Annotation } from '@/types/common';
import { isEmpty } from 'lodash';
import { useTabStore } from '@/stores/tabs';
import Textarea from 'primevue/textarea';
import Button from 'primevue/button';
import { RouteName } from '@/router/routes';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import useResourcesStore from '@/stores/resources';
import { useRouter } from 'vue-router';
import API from '@/api/api';

const props = defineProps<{
	project: IProject;
	assetName?: string;
	assetId?: string;
	assetType?: ProjectAssetTypes;
}>();

const resources = useResourcesStore();
const tabStore = useTabStore();
const router = useRouter();

const isResourcesSliderOpen = ref(true);
const isNotesSliderOpen = ref(false);
const annotations = ref<Annotation[]>([]);
const annotationContent = ref<string>('');

// Associated with tab storage
const tabContext = props.project?.id.toString();
const tabs = ref<Tab[]>([]);
const activeTabIndex = ref(0);

function openAsset(selectedTab?: Tab) {
	const assetToOpen = selectedTab ?? tabs.value[activeTabIndex.value] ?? { assetName: 'Overview' };

	// if (props.assetName) // rethink on open behavior
	router.push({ name: RouteName.ProjectRoute, params: assetToOpen });
}

tabStore.$subscribe((/* _mutation, _state */) => {
	// Sync with storage, not sure why computed doesn't work for these
	tabs.value = tabStore.getTabs(tabContext);
	activeTabIndex.value = tabStore.getActiveTabIndex(tabContext);
	openAsset();
});

function removeClosedTab(tabIndexToRemove: number) {
	tabStore.removeTab(tabContext, tabIndexToRemove);
}

const formatDate = (millis: number) => new Date(millis).toLocaleDateString();

// Start at proper route
onMounted(() => {
	openAsset();
	console.log(resources.activeProject, props.project);
});

// Updates on new route
watch(
	() => props,
	() => {
		// If new name, its a new asset so add a new tab
		if (!tabs.value.some(({ assetName }) => assetName === props.assetName) || isEmpty(tabs)) {
			tabStore.addTab(tabContext, {
				assetName: props.assetName || '',
				assetId: props.assetId,
				assetType: props.assetType
			});
		}
		// Tab switch
		else {
			const index = tabs.value.findIndex(({ assetName }) => assetName === props.assetName);
			tabStore.setActiveTabIndex(tabContext, index);
		}
	}
);

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
