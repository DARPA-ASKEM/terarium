<template>
	<Button label="Export xDD IDs" @click="exportIds" />
	<!-- It's safe to force id to be a string since we use XDD URIs (all strings) as artifact IDs for the purposes of this list -->
	<ArtifactList
		:artifacts="documentsAsArtifactList"
		:selected-artifact-ids="[documentId]"
		@artifact-clicked="(id) => openDocumentPage(id as string)"
		@remove-artifact="(id) => removeDocument(id as string)"
	/>
</template>

<script setup lang="ts">
/**
 * Documents Sidebar Panel
 * Display a list of documents available in the current Project.
 */
import Button from 'primevue/button';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import { deleteAsset } from '@/services/project';
import useResourcesStore from '@/stores/resources';
import { ProjectAssetTypes } from '@/types/Project';
import { DocumentAsset } from '@/types/Document';
import ArtifactList from './artifact-list.vue';

const router = useRouter();
const resourcesStore = useResourcesStore();

const documentId = ref('');
const documents = ref<DocumentAsset[]>([]);

const documentsAsArtifactList = computed(() =>
	documents.value.map((document) => ({ id: document?.xdd_uri, name: document?.title }))
);

const openDocumentPage = async (xdd_uri: string) => {
	// pass this doc id as param
	documentId.value = xdd_uri; // track selection
	router.push({
		name: RouteName.DocumentRoute,
		params: { projectId: resourcesStore.activeProject?.id, assetId: xdd_uri }
	});
};

const removeDocument = async (xdd_uri: string) => {
	const docAsset = documents.value.find((document) => document.xdd_uri === xdd_uri);
	if (docAsset === undefined) {
		console.error('Failed to remove document with XDD uri', xdd_uri);
		return;
	}
	// remove the document from the project assets
	if (resourcesStore.activeProject && resourcesStore.activeProjectAssets) {
		const assetsType = ProjectAssetTypes.DOCUMENTS;
		deleteAsset(resourcesStore.activeProject.id, assetsType, docAsset.id);
		// remove also from the local cache
		resourcesStore.activeProject.assets[ProjectAssetTypes.DOCUMENTS] =
			resourcesStore.activeProject.assets[ProjectAssetTypes.DOCUMENTS].filter(
				(docId) => docId !== docAsset.id
			);
		resourcesStore.activeProjectAssets[ProjectAssetTypes.DOCUMENTS] =
			resourcesStore.activeProjectAssets[ProjectAssetTypes.DOCUMENTS].filter(
				(document) => document.id !== docAsset.id
			);
		documents.value = resourcesStore.activeProjectAssets[ProjectAssetTypes.DOCUMENTS];
	}

	// if the user deleted the currently selected document, then clear its content from the view
	if (docAsset.xdd_uri === documentId.value) {
		router.push('/document'); // clear the doc ID as a URL param
	}
};

// Get the documents associated with this project
onMounted(() => {
	documents.value = resourcesStore.activeProjectAssets?.[ProjectAssetTypes.DOCUMENTS] ?? [];
});

function exportIds() {
	console.log(
		'List of xDD _gddid ',
		documents.value.map((document) => document)
	);
}
</script>
