<template>
	<Card v-if="document">
		<template #content>
			<div class="image">
				<img
					v-if="images.length > 0"
					class="card-image"
					:src="'data:image/jpeg;base64,' + shownImage"
					alt="Paper image"
				/>
				<img
					v-else
					class="card-image"
					:src="'data:image/jpeg;base64,' + getRandomImage()"
					alt="Paper image"
				/>
			</div>
			<section class="journal">
				<span class="journal-name">{{ document?.journal }}</span> | {{ document?.year }}
			</section>
			<section class="title">
				{{ document?.title }}
			</section>
			<section class="authors">
				<ul>
					<li v-for="(author, index) in document?.author" :key="index">{{ author.name }}</li>
				</ul>
			</section>
		</template>
	</Card>
	<Card v-else>
		<template #content>
			<div class="image skeleton">
				<Skeleton height="100%" width="100%" />
			</div>
			<section class="journal">
				<Skeleton />
			</section>
			<section class="title skeleton">
				<Skeleton />
				<Skeleton />
				<Skeleton width="60%" />
			</section>
			<section class="authors">
				<ul class="skeleton">
					<li v-for="(i, index) in [0, 1, 2, 3, 4, 5, 6]" :key="index">
						<Skeleton width="4rem" />
					</li>
				</ul>
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
import { onMounted, ref, computed } from 'vue';
import * as stockImages from '@/assets/images/homePageStockImages';
import Skeleton from 'primevue/skeleton';

const props = defineProps<{ document?: DocumentType }>();
const artifacts = ref<XDDArtifact[]>([]);
const extractionType = ref('');
const images = computed(() => artifacts.value.map((a) => a.properties.image));
const shownImage = computed(() => images.value.find((element) => element !== undefined));
const backupImages = stockImages.backupImagesTwo;

async function fetchArtifacts(doi) {
	if (doi !== '') {
		const allArtifacts = await getXDDArtifacts(doi);
		artifacts.value = allArtifacts.filter((art) => art.askemClass !== XDDExtractionType.Document);
	} else {
		artifacts.value = [];
	}
}

function getRandomImage() {
	return backupImages[Math.floor(Math.random() * backupImages.length)];
}

onMounted(async () => {
	if (props.document) {
		const doi = await getDocumentDoi(props.document);
		await fetchArtifacts(doi);
		if (artifacts.value.length > 0) {
			extractionType.value = artifacts.value[0].askemClass;
		}
	}
});
</script>

<style scoped>
.image {
	height: 8.75rem;
	margin-bottom: 0.5rem;
	background-color: var(--surface-ground);
	border-radius: 1rem;
}

.image.skeleton {
	background-color: transparent;
}

.image img {
	height: 100%;
	width: 100%;
}

.journal {
	margin-bottom: 0.5rem;
	height: 2.4rem;
}

.journal-name {
	color: var(--primary-color);
}

.title {
	height: 3.75rem;
	font-weight: var(--font-weight-semibold);
	overflow: hidden;
	margin-bottom: 0.5rem;
}

.title.skeleton {
	display: flex;
	flex-direction: column;
	row-gap: 0.3rem;
}

.authors {
	overflow: hidden;
	height: 3.75rem;
}

.p-card {
	width: 17rem;
	height: 20rem;
}

ul.skeleton {
	display: flex;
	flex-wrap: wrap;
	row-gap: 0.3rem;
}

li {
	list-style: none;
	display: inline;
	margin-right: 0.5rem;
	color: var(--text-color-secondary);
}
</style>
