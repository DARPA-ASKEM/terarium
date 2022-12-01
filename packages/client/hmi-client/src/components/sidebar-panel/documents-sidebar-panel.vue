<template>
	<div class="document-list-container">
		<div
			v-for="docAsset in documents"
			:key="docAsset.xdd_uri"
			class="doc-link"
			:class="{ active: docAsset.xdd_uri === documentId }"
			@click="openDocumentPage(docAsset)"
		>
			<span class="doc-title">
				{{ docAsset.title }}
			</span>
			<span class="doc-delete-btn" @click.stop="removeDocument(docAsset)">
				<IconClose32 />
			</span>
		</div>
	</div>
</template>

<script setup lang="ts">
/**
 * Documents Sidebar Panel
 * Display a list of documents available in the current Project.
 */
import useResourcesStore from '@/stores/resources';
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import IconClose32 from '@carbon/icons-vue/es/close/32';
import { deleteAsset } from '@/services/project';
import { PUBLICATIONS } from '@/types/Project';
import { PublicationAsset } from '@/types/XDD';

const router = useRouter();

const resourcesStore = useResourcesStore();

const documentId = ref('');
const documents = ref<PublicationAsset[]>([]);

const openDocumentPage = async (docAsset: PublicationAsset) => {
	// pass this doc id as param
	documentId.value = docAsset.xdd_uri; // track selection
	router.push({ path: `/docs/${docAsset.xdd_uri}` });
};

const removeDocument = async (docAsset: PublicationAsset) => {
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

<style scoped>
.document-list-container {
	overflow-y: auto;
	margin-top: 1rem;
	height: 100%;
}

.doc-link {
	cursor: pointer;
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
}

.doc-link:hover:not(.active) {
	background-color: var(--un-color-body-surface-secondary);
}

.active {
	font-size: var(--un-font-body);
	background-color: var(--un-color-body-surface-background);
}

.doc-view-icon {
	padding-right: 0.5rem;
}

.doc-delete-btn {
	color: var(--un-color-body-text-disabled);
}

.doc-delete-btn:hover {
	/* color: var(--un-color-body-text-primary); */
	color: red;
}

span {
	display: inline-flex;
	align-items: center;
}

.doc-title {
	text-overflow: ellipsis;
	overflow: hidden;
	white-space: nowrap;
	display: inline;
}
</style>
