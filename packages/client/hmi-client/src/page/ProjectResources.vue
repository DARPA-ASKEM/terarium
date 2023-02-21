<template>
	<slider-panel
		v-model:is-open="isResourceSidebarOpen"
		content-width="240px"
		header="Resources"
		direction="left"
	>
		<template v-slot:content>
			<tera-resource-sidebar :project="project" />
		</template>
	</slider-panel>
	<section>
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
</template>

<script setup lang="ts">
import { ref } from 'vue';
import SliderPanel from '@/components/widgets/slider-panel.vue';
import TeraResourceSidebar from '@/components/tera-resource-sidebar.vue';
import Document from '@/components/documents/Document.vue';
import Dataset from '@/components/dataset/Dataset.vue';
import Model from '@/components/models/Model.vue';
import { ProjectType } from '@/types/Project';
import useResourcesStore from '@/stores/resources';

const props = defineProps<{
	assetId: string;
	resourceType?: string;
	project: ProjectType;
}>();

const resources = useResourcesStore();

const isResourceSidebarOpen = ref(true);

console.log(props);
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
