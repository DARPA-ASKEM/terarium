<template>
	<slider-panel
		v-model:is-open="isResourcesSliderOpen"
		content-width="240px"
		header="Resources"
		direction="left"
	>
		<template v-slot:content>
			<tera-resource-sidebar :project="project" />
		</template>
	</slider-panel>
	<section>
		<!-- <TabMenu :model="tabbedResources"> // https://github.com/tupilabs/vue-lumino consider better alternatives
			<template #item="{ item }">
				<a :to="item.to || ''" :projectId="project.id">
					<Chip :label="item.to.params.assetType" />
					{{ item.label }}
				</a>
			</template>
		</TabMenu> -->
		<code-component v-if="showCode" />
		<template v-else-if="assetId">
			<document
				v-if="assetType === ProjectAssetTypes.DOCUMENTS"
				:asset-id="assetId"
				:previewLineLimit="5"
				:project="resources.activeProject"
				:is-editable="true"
			/>
			<dataset
				v-else-if="assetType === ProjectAssetTypes.DATASETS"
				:asset-id="assetId"
				:project="resources.activeProject"
				:is-editable="true"
			/>
			<model
				v-else-if="assetType === ProjectAssetTypes.MODELS"
				:asset-id="assetId"
				:project="resources.activeProject"
				:is-editable="true"
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
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import SliderPanel from '@/components/widgets/slider-panel.vue';
import TeraResourceSidebar from '@/page/project/components/tera-resource-sidebar.vue';
import TeraProjectOverview from '@/page/project/components/tera-project-overview.vue';
import Document from '@/components/documents/Document.vue';
import Dataset from '@/components/dataset/Dataset.vue';
import Model from '@/components/models/Model.vue';
import SimulationPlan from '@/page/project/components/Simulation.vue';
import SimulationRun from '@/page/project/components/SimulationResult.vue';
import CodeComponent from '@/page/project/components/CodeView.vue';
// import TabMenu from 'primevue/tabmenu';
import Textarea from 'primevue/textarea';
import Button from 'primevue/button';
// import Chip from 'primevue/chip';
import { RouteName } from '@/router/routes';
import { ProjectType, ProjectAssetTypes } from '@/types/Project';
import { ResourceType, Annotation } from '@/types/common';
import useResourcesStore from '@/stores/resources';
import API from '@/api/api';

const props = defineProps<{
	project: ProjectType;
	resourceName?: string;
	assetId?: string;
	assetType?: string;
}>();

const resources = useResourcesStore();
const showCode = false; // temp integration

const isResourcesSliderOpen = ref(true);
const isNotesSliderOpen = ref(false);
const annotations = ref<Annotation[]>([]);
const annotationContent = ref<string>('');

const tabbedResources = ref<any>([
	{
		label: 'Overview',
		icon: '',
		to: {
			name: RouteName.ProjectRoute,
			params: {
				resourceName: 'Overview',
				assetId: null,
				assetType: null
			}
		}
	}
]);

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

const formatDate = (millis: number) => new Date(millis).toLocaleDateString();

watch(
	() => [props.resourceName],
	() => {
		// If new name add to resource tab otherwise switch to tab
		if (!tabbedResources.value.some(({ label }) => label === props.resourceName)) {
			tabbedResources.value.push({
				label: props.resourceName,
				icon: '',
				to: {
					name: RouteName.ProjectRoute,
					params: {
						resourceName: props.resourceName,
						assetId: props.assetId,
						assetType: props.assetType
					}
				}
			});
		}
	}
);
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
