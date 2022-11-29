<template>
	<div class="document-list-container">
		<div
			v-for="docId in documents"
			:key="docId"
			class="doc-link"
			:class="{ active: docId === documentId }"
			@click="openDocumentPage(docId)"
		>
			<span class="doc-view-icon">
				<DocumentView />
			</span>
			<span class="doc-title">
				{{ docId }}
			</span>
			<span class="doc-delete-btn" @click.stop="removeDocument(docId)">
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
import DocumentView from '@carbon/icons-vue/es/document--view/32';
import { getPublication } from '@/services/external';
import { deleteAsset } from '@/services/project';
import { PUBLICATIONS } from '@/types/Project';

const router = useRouter();

const resourcesStore = useResourcesStore();

const documentId = ref('');
const documents = ref<string[]>([]);

const openDocumentPage = async (docId: string) => {
	const publicationDetails = await getPublication(docId);
	// pass this doc id as param
	if (publicationDetails) {
		documentId.value = docId; // track selection
		router.push({ path: `/docs/${publicationDetails.xdd_uri}` });
	}
};

const removeDocument = async (docId: string) => {
	// remove the document from the project assets
	if (resourcesStore.activeProject) {
		const assetsType = PUBLICATIONS;
		deleteAsset(resourcesStore.activeProject.id, assetsType, docId);
		// remove also from the local cache
		resourcesStore.activeProject.assets[PUBLICATIONS] = resourcesStore.activeProject.assets[
			PUBLICATIONS
		].filter((a) => a !== docId);
		documents.value = resourcesStore.activeProject.assets[PUBLICATIONS];
	}

	// if the user deleted the currently selected document, then clear its content from the view
	if (docId === documentId.value) {
		router.push('/docs'); // clear the doc ID as a URL param
	}
};

onMounted(() => {
	// get the list of publications associated with this project and display them
	const documentsInCurrentProject = resourcesStore.activeProject?.assets.publications;
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
	overflow-y: auto;
}

.doc-link {
	padding: 0.5rem;
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
