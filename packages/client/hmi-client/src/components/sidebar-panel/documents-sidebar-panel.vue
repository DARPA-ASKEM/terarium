<template>
	<main>
		<header>Documents space</header>
		<section>
			Project Documents:
			<div class="document-list-container">
				<div
					v-for="doc in documents"
					:key="doc.gddid"
					class="doc-link"
					:class="{ active: doc.gddid === docID }"
					@click="openDocumentPage(doc)"
				>
					<span>{{ formatTitle(doc) }}</span>
					<span class="doc-delete-btn" @click.stop="removeDocument(doc)">
						<IconClose32 />
					</span>
				</div>
			</div>
		</section>
	</main>
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

const router = useRouter();

const resourcesStore = useResourcesStore();
const documents = computed(() => resourcesStore.documents);

const formatTitle = (doc: XDDArticle) => {
	const maxSize = 32;
	const itemTitle = doc.title;
	return itemTitle.length < maxSize ? itemTitle : `${itemTitle.substring(0, maxSize)}...`;
};

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
main {
	background-color: var(--un-color-body-surface-primary);
	display: flex;
	flex-grow: 1;
	flex-direction: column;
	gap: 1rem;
	padding: 1rem;
	height: calc(100vh - 50px);
}

header {
	color: var(--un-color-body-text-secondary);
	font: var(--un-font-h6);
}

section {
	height: 100%;
}

.document-list-container {
	overflow-y: auto;
	height: 100%;
	margin-top: 8px;
}

.doc-link {
	padding: 2px 4px;
	color: blue;
	cursor: pointer;
	display: flex;
	flex-direction: row;
	justify-content: space-between;
}
.doc-link:hover {
	text-decoration: underline;
}

.active {
	text-decoration: underline;
	font-size: var(--un-font-body);
	background-color: var(--un-color-accent-light);
}

.doc-delete-btn {
	color: red;
	padding-right: 1rem;
}
</style>
