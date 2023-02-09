<template>
	<Card>
		<template #header>
			<section class="image">
				<img src="@assets/images/project-card.png" alt="Paper image" />
			</section>
		</template>
		<template #content>
			<section>
				<span class="journal">{{ article.journal }}</span> | {{ article.year }}
			</section>
			<section>
				<span class="title">{{ article.title }}</span>
			</section>
			<section></section>
		</template>
	</Card>
</template>

<script setup lang="ts">
import { getXDDArtifacts } from '@/services/data';
import { XDDArticle, XDDArtifact, XDDExtractionType } from '@/types/XDD';
import { getDocumentDoi } from '@/utils/data-util';
import Card from 'primevue/card';
import { onMounted, ref } from 'vue';

const props = defineProps<{ article: XDDArticle }>();
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
	const doi = await getDocumentDoi(props.article);
	await fetchArtifacts(doi);
	if (artifacts.value.length > 0) {
		extractionType.value = artifacts.value[0].askemClass;
	}
});
</script>

<style scoped>
.image {
	width: 248px;
	height: 185px;
	padding: 1rem 1rem 0 1rem;
}

.image img {
	border-radius: 1rem;
	object-fit: scale-down;
	width: 100%;
	height: 100%;
}

.journal {
	color: var(--primary-color);
}

.title {
	font-size: 1.5rem;
	font-weight: 700;
}

section {
	margin: 0.5rem 0 0.5rem 0;
}
</style>
