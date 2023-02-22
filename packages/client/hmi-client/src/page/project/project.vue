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
		<TabMenu :model="tabbedResources" />
		<tera-project-overview :project="project" />
		<document
			v-if="resourceType === 'publications'"
			:asset-id="assetId"
			:previewLineLimit="5"
			:project="resources.activeProject"
			:is-editable="true"
		/>
		<dataset
			v-else-if="resourceType === 'datasets'"
			:asset-id="assetId"
			:project="resources.activeProject"
			:is-editable="true"
		/>
		<model
			v-else-if="resourceType === 'models'"
			:asset-id="assetId"
			:project="resources.activeProject"
			:is-editable="true"
		/>
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
import TabMenu from 'primevue/tabmenu';
import { ProjectType } from '@/types/Project';
import { ResourceType, Annotation } from '@/types/common';
import useResourcesStore from '@/stores/resources';
import API from '@/api/api';

const props = defineProps<{
	assetId: string;
	resourceName: string;
	resourceType?: string;
	project: ProjectType;
}>();

const resources = useResourcesStore();

const isResourcesSliderOpen = ref(true);
const isNotesSliderOpen = ref(false);
const annotations = ref<Annotation[]>([]);
const annotationContent = ref<string>('');

const tabbedResources = ref([]);

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
</style>
