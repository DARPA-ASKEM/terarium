<template>
	<main>
		<header>Documents space</header>
		<section>
			Project Documents:
			<div
				v-for="doc in documents"
				:key="doc._gddid"
				class="doc-link"
				:class="{ active: doc._gddid === docID }"
				@click="openDocumentPage(doc)"
			>
				<span>{{ formatTitle(doc) }}</span>
				<span class="doc-delete-btn" @click.stop="removeDoc(doc)">
					<i class="fa fa-fw fa-close" />
				</span>
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
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';

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

const removeDoc = (doc: XDDArticle) => {
	resourcesStore.removeResource(doc);
};
</script>

<style scoped>
main {
	background-color: var(--un-color-body-surface-primary);
	display: flex;
	flex-grow: 1;
	flex-direction: column;
	gap: 1rem;
	padding: 1rem;
}

header {
	color: var(--un-color-body-text-secondary);
	font: var(--un-font-h6);
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
	font-size: medium;
}

.doc-delete-btn {
	color: red;
}
</style>
