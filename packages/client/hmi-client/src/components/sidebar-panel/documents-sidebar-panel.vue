<template>
	<!-- It's safe to force id to be a string since we use XDD URIs (all strings) as artifact IDs for the purposes of this list -->
	<ArtifactList
		:artifacts="documentsAsArtifactList"
		:selected-artifact-id="documentId"
		@artifact-clicked="(id) => openDocumentPage(id as string)"
		@remove-artifact="(id) => removeDocument(id as string)"
	/>
</template>

<script setup lang="ts">
/**
 * Documents Sidebar Panel
 * Display a list of documents available in the current Project.
 */
import useResourcesStore from '@/stores/resources';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { deleteAsset } from '@/services/project';
import { PUBLICATIONS } from '@/types/Project';
import { PublicationAsset } from '@/types/XDD';
import { RouteName } from '@/router/routes';
import ArtifactList from './artifact-list.vue';

const router = useRouter();

const resourcesStore = useResourcesStore();

const documentId = ref('');
const documents = ref<PublicationAsset[]>([]);

const documentsAsArtifactList = computed(() =>
	documents.value.map((document) => ({ id: document.xdd_uri, name: document.title }))
);

const openDocumentPage = async (xddUri: string) => {
	// pass this doc id as param
	documentId.value = xddUri; // track selection
	router.push({
		name: RouteName.DocumentRoute,
		params: { projectId: resourcesStore.activeProject?.id, id: xddUri }
	});
};

const removeDocument = async (xddUri: string) => {
	const docAsset = documents.value.find((document) => document.xdd_uri === xddUri);
	if (docAsset === undefined) {
		console.error('Failed to remove document with XDD uri', xddUri);
		return;
	}
	// remove the document from the project assets
	if (resourcesStore.activeProject && resourcesStore.activeProjectAssets) {
		const assetsType = PUBLICATIONS;
		deleteAsset(resourcesStore.activeProject.id, assetsType, docAsset.id);
		// remove also from the local cache
		resourcesStore.activeProject.assets[PUBLICATIONS] = resourcesStore.activeProject.assets[
			PUBLICATIONS
		].filter((docId) => docId !== docAsset.id);
		resourcesStore.activeProjectAssets[PUBLICATIONS] = resourcesStore.activeProjectAssets[
			PUBLICATIONS
		].filter((document) => document.id !== docAsset.id);
		documents.value = resourcesStore.activeProjectAssets[PUBLICATIONS];
	}

	// if the user deleted the currently selected document, then clear its content from the view
	if (docAsset.xdd_uri === documentId.value) {
		router.push('/docs'); // clear the doc ID as a URL param
	}
};

onMounted(() => {
	// get the list of publications associated with this project and display them
	const documentsInCurrentProject = resourcesStore.activeProjectAssets?.publications;
	if (documentsInCurrentProject) {
		documents.value = documentsInCurrentProject;
	}
});
</script>
