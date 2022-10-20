<template>
	<div class="doc-view-container">
		<div v-if="doc">
			<div class="title">{{ doc.title }}</div>
			<div class="authors">{{ formatArticleAuthors(doc) }}</div>
			<div class="desc">{{ formatDescription(doc) }}</div>
			<div class="doi">{{ doi }}</div>
			<div class="artifacts-header"><b>Document Artifacts:</b></div>
			<div>Found {{ artifacts.length }} Extractions</div>
			<div class="extractions-container">
				<div class="nav-container">
					<ul class="nav">
						<li v-for="exType in Object.keys(groupedExtractions)" :key="exType">
							<button
								type="button"
								:class="{ active: extractionType === exType }"
								@click="extractionType = exType"
							>
								{{ exType }} ({{ groupedExtractions[exType].length }})
							</button>
						</li>
					</ul>
				</div>
				<div v-for="ex in groupedExtractions[extractionType]" :key="ex.id">
					<template
						v-if="
							ex.bytes &&
							(ex.cls === XDDExtractionType.Figure ||
								ex.cls === XDDExtractionType.Table ||
								ex.cls === XDDExtractionType.Equation)
						"
					>
						<!-- render figure -->
						<img id="img" :src="'data:image/jpeg;base64,' + ex.bytes" :alt="ex.content" />
					</template>
					<template v-else>
						<!-- render textual content -->
						{{ ex.content }}
						{{ ex.header_content }}
					</template>
				</div>
			</div>
		</div>
		<div v-else class="invalid-doc"></div>
	</div>
</template>

<script setup lang="ts">
import { getXDDArtifacts } from '@/services/data';
import useResourcesStore from '@/stores/resources';
import {
	XDDArticle,
	XDDArtifact,
	XDDSearchParams,
	XDDExtractionType,
	XDDArtifactExtraction
} from '@/types/XDD';
import { groupBy } from 'lodash';
import { computed, onMounted, ref, watch } from 'vue';

const props = defineProps({
	// this id is received as the document id mapped from the route param
	id: {
		type: String,
		default: ''
	}
});

const resourcesStore = useResourcesStore();

const doc = computed(() => resourcesStore.documents[props.id] || null);
const xddDataset = computed(() => resourcesStore.xddDataset);

const formatArticleAuthors = (d: XDDArticle) => d.author.map((a) => a.name).join(', ');

const formatDescription = (d: XDDArticle) => d.abstract || '[no abstract]';

const doi = computed(() => {
	let docIdentifier = '';
	if (doc.value && doc.value.identifier.length > 0) {
		const defaultDOI = doc.value.identifier.find((i) => i.type === 'doi');
		if (defaultDOI) {
			docIdentifier = defaultDOI.id;
		}
	}
	return docIdentifier;
});

const extractionType = ref('');

const artifacts = ref<XDDArtifact[]>([]);

const extractions = computed(() => {
	const allExtractions: XDDArtifactExtraction[] = [];
	artifacts.value.forEach((e) => {
		allExtractions.push(...e.children);
	});
	return allExtractions;
});

const groupedExtractions = computed(() => groupBy(extractions.value, 'cls'));

// eslint-disable-next-line @typescript-eslint/no-unused-vars
watch(extractions, (currentValue, oldValue) => {
	if (extractions.value.length > 0) {
		extractionType.value = extractions.value[0].cls;
	}
});

const fetchArtifacts = async () => {
	if (doc.value !== null && doi.value !== '') {
		// a 'type' may be used to filter the extractions to a given artifact types, e.g. Figure
		// Note: the dataset MUST be specified for the COSMOS API to work
		const searchParams: XDDSearchParams = {
			dataset: xddDataset.value
		};
		artifacts.value = await getXDDArtifacts(doi.value, searchParams);
	}
};

watch(doc, (currentValue, oldValue) => {
	if (currentValue !== oldValue) {
		fetchArtifacts();
	}
});

// fetch artifacts from COSMOS using the doc doi
onMounted(async () => {
	fetchArtifacts();
});
</script>

<style scoped>
.doc-view-container {
	padding: 1rem;
	font-size: large;
}

.title {
	font-weight: bold;
	font-size: x-large;
	padding-top: 1rem;
	line-height: 2rem;
}

.authors {
	font-style: italic;
	padding-top: 8px;
}

.desc {
	padding-top: 8px;
}

.invalid-doc {
	color: red;
}

.artifacts-header {
	padding-top: 2rem;
	padding-bottom: 8px;
}

.extractions-container {
	display: flex;
	flex-direction: column;
	gap: 1rem;
	padding-left: 1rem;
}

.nav-container {
	display: flex;
	align-items: center;
}

.nav {
	list-style-type: none;
	margin: 0;
	padding: 0;
	overflow: hidden;
	margin-right: 2rem;
	margin-left: 5rem;
}

li {
	float: left;
}

li button {
	display: block;
	color: blue;
	text-align: center;
	padding: 8px 12px;
	text-decoration: none;
	border: none;
	background-color: transparent;
	font-size: larger;
	cursor: pointer;
}

li button:hover:not(.active) {
	text-decoration: underline;
	border: none;
}

li button.active {
	text-decoration: underline;
	font-weight: bold;
	border: none;
}
</style>
