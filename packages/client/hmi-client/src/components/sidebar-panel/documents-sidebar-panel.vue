<template>
	<div class="document-list-container">
		<div
			v-for="doc in documents"
			:key="doc._gddid"
			class="doc-link"
			:class="{ active: doc._gddid === docID }"
			@click="openDocumentPage(doc)"
		>
			<span class="doc-view-icon">
				<DocumentView />
			</span>
			<span class="doc-title">
				{{ doc.title }}
			</span>
			<span class="doc-delete-btn" @click.stop="removeDocument(doc)">
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
import { XDDArticle } from '@/types/XDD';
import { getResourceID } from '@/utils/data-util';
import { isEmpty } from 'lodash';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import IconClose32 from '@carbon/icons-vue/es/close/32';
import DocumentView from '@carbon/icons-vue/es/document--view/32';

const router = useRouter();

const resourcesStore = useResourcesStore();
const documents = computed(() => resourcesStore.documents);

const docID = ref('');

const openDocumentPage = (doc: XDDArticle) => {
	// pass this doc id as param
	docID.value = getResourceID(doc);
	router.push({ path: `/docs/${docID.value}` });
};

const removeDocument = (doc: XDDArticle) => {
	resourcesStore.removeResource(doc);
	router.push('/docs'); // clear the doc ID as a URL param
};

onMounted(() => {
	const routeParams = router.currentRoute.value.params;
	if (!isEmpty(routeParams) && routeParams.id !== '' && docID.value === '') {
		docID.value = routeParams.id as string;
	}
});
</script>

<style scoped>
.document-list-container {
	overflow-y: auto;
	margin-top: 1rem;
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
