<template>
	<Card>
		<template #header>
			<section class="content">
				<div class="image"></div>
				<section class="journal">
					<span class="journal-name">{{ document.journal }}</span> | {{ document.year }}
				</section>
				<section class="title">
					{{ document.title }}
				</section>
				<section class="authors">
					<ul>
						<li v-for="(author, index) in document.author" :key="index">{{ author.name }}</li>
					</ul>
				</section>
			</section>
		</template>
	</Card>
</template>

<script setup lang="ts">
import { getXDDArtifacts } from '@/services/data';
import { DocumentType, XDDArtifact } from '@/types/Document';
import { XDDExtractionType } from '@/types/XDD';
import { getDocumentDoi } from '@/utils/data-util';
import Card from 'primevue/card';
import { onMounted, ref } from 'vue';

const props = defineProps<{ document: DocumentType }>();
const artifacts = ref<XDDArtifact[]>([]);
const extractionType = ref('');

async function fetchArtifacts(doi) {
	if (doi !== '') {
		const allArtifacts = await getXDDArtifacts(doi);
		artifacts.value = allArtifacts.filter((art) => art.askemClass !== XDDExtractionType.Document);
	} else {
		artifacts.value = [];
	}
}

onMounted(async () => {
	const doi = await getDocumentDoi(props.document);
	await fetchArtifacts(doi);
	if (artifacts.value.length > 0) {
		extractionType.value = artifacts.value[0].askemClass;
	}
});
</script>

<style scoped>
.content {
	padding: 0 1rem 0 1rem;
}

.image {
	width: calc(280px - 2rem);
	height: 190px;
	margin: 1rem 0 1rem 0;
	background-color: var(--surface-ground);
	border-radius: 1rem;
	display: inline-block;
}

.journal {
	margin: 0 0 1rem 0;
}

.journal-name {
	color: var(--primary-color);
}

.title {
	font-size: 1.5rem;
	font-weight: 700;
	height: 77px;
	display: inline-block;
	overflow: hidden;
	text-overflow: ellipsis;
}

.authors {
	display: inline-block;
	overflow: hidden;
	height: 3.6rem;
}

.p-card {
	width: 280px;
}

li {
	list-style: none;
	display: inline;
	margin-right: 0.5rem;
	color: var(--text-color-secondary);
}
</style>
