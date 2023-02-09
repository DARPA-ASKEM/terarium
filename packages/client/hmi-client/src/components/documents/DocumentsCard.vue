<script setup lang="ts">
import { XDDExtractionType } from '@/types/XDD';
import { XDDArtifact, DocumentType } from '@/types/Document';
import { onMounted, ref, computed } from 'vue';
import { getXDDArtifacts } from '@/services/data';
import { getDocumentDoi } from '@/utils/data-util';
import * as stockImages from '@/assets/images/homePageStockImages';

export interface Props {
	document: DocumentType;
}
const props = defineProps<Props>();
const documentName = computed(() => props.document.title);
const extractionType = ref('');
const artifacts = ref<XDDArtifact[]>([]);
const images = computed(() => artifacts.value.map((a) => a.properties.image));
const shownImage = computed(() => images.value.find((element) => element !== undefined));
const backupImages = stockImages.backupImagesTwo;

const fetchArtifacts = async (doi) => {
	if (doi !== '') {
		const allArtifacts = await getXDDArtifacts(doi);
		// filter out Document extraction type
		artifacts.value = allArtifacts.filter((art) => art.askemClass !== XDDExtractionType.Document);
	} else {
		// note that some XDD documents do not have a valid doi
		artifacts.value = [];
	}
};

onMounted(async () => {
	const doi = await getDocumentDoi(props.document);
	await fetchArtifacts(doi);
	if (artifacts.value.length > 0) {
		extractionType.value = artifacts.value[0].askemClass;
	}
});

function getRandomImage() {
	return backupImages[Math.floor(Math.random() * backupImages.length)];
}
</script>
<template>
	<div class="document-card">
		<img
			v-if="images.length > 0"
			class="card-image"
			:src="'data:image/jpeg;base64,' + shownImage"
			alt="''"
		/>
		<img v-else class="card-image" :src="'data:image/jpeg;base64,' + getRandomImage()" alt="''" />
		<footer>{{ documentName }}</footer>
	</div>
</template>

<style scoped>
.document-card {
	border: 1px solid var(--surface-border);
	background-color: var(--surface-section);
	display: flex;
	flex-direction: column;
	align-items: center;
	height: 15rem;
	min-width: 20rem;
	border-radius: 0.5rem;
	text-align: left;
	cursor: pointer;
	/* Round the image corners by clipping them to fit the card */
	overflow: hidden;
}

footer {
	border-top: 1px solid var(--surface-border);
	padding: 0.5rem 1rem;
	align-self: stretch;
}

.card-image {
	flex: 1;
	min-height: 0;
	width: fit-content;
}
</style>
