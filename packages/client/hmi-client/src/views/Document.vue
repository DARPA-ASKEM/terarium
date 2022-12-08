<template>
	<section class="doc-view-container">
		<div v-if="doc">
			<div v-if="docLink" class="title">
				<a :href="docLink" target="_blank" rel="noreferrer noopener">{{ doc.title }}</a>
			</div>
			<div v-else class="title">{{ doc.title }}</div>

			<div class="authors">{{ formatArticleAuthors(doc) }}</div>
			<div class="journal">{{ doc.journal }}</div>
			<div class="publisher">{{ doc.publisher }}</div>
			<div class="desc">{{ formatDescription(doc) }}</div>
			<div class="doi">DOI: {{ doi }}</div>
			<div class="artifacts-header">
				<b>Document Artifacts:</b> Found {{ artifacts.length }} Extractions
			</div>
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
				<div v-for="ex in groupedExtractions[extractionType]" :key="ex.askemId">
					<template
						v-if="
							ex.properties.image &&
							(ex.askemClass === XDDExtractionType.Figure ||
								ex.askemClass === XDDExtractionType.Table ||
								ex.askemClass === XDDExtractionType.Equation)
						"
					>
						<!-- render figure -->
						{{ ex.properties.caption ? ex.properties.caption : ex.properties.contentText }}
						<img id="img" :src="'data:image/jpeg;base64,' + ex.properties.image" :alt="''" />
					</template>
					<template v-else>
						<!-- render textual content -->
						<b>{{ ex.properties.title }}</b>
						{{ ex.properties.caption }}
						{{ ex.properties.abstractText }}
						{{ ex.properties.contentText }}
					</template>
				</div>
			</div>
		</div>
		<div v-else class="invalid-doc"></div>
	</section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { getDocumentById, getXDDArtifacts } from '@/services/data';
import { XDDArticle, XDDArtifact, XDDExtractionType } from '@/types/XDD';
import { groupBy } from 'lodash';
import { getDocumentDoi } from '@/utils/data-util';

const props = defineProps({
	// this id is received as the document id mapped from the route param
	id: {
		type: String,
		default: ''
	}
	// NOTE that project is automatically injected as prop as well
});

const doc = ref<XDDArticle | null>(null);

watch(
	props,
	async () => {
		const id = props.id;
		if (id !== '') {
			// fetch doc from XDD
			const d = await getDocumentById(id);
			if (d) {
				doc.value = d;
			}
		} else {
			doc.value = null;
		}
	},
	{
		immediate: true
	}
);

const formatArticleAuthors = (d: XDDArticle) => d.author.map((a) => a.name).join(', ');

const docLink = computed(() =>
	doc.value?.link && doc.value.link.length > 0 ? doc.value.link[0].url : null
);

const formatDescription = (d: XDDArticle) =>
	(d.abstractText && typeof d.abstractText === 'string' ? d.abstractText : false) ||
	'[no abstract]';

const doi = computed(() => getDocumentDoi(doc.value));

const extractionType = ref('');

const artifacts = ref<XDDArtifact[]>([]);

const groupedExtractions = computed(() => groupBy(artifacts.value, 'askemClass'));

// @ts-ignore
// eslint-disable-next-line @typescript-eslint/no-unused-vars
watch(artifacts, (currentValue, oldValue) => {
	if (artifacts.value.length > 0) {
		extractionType.value = artifacts.value[0].askemClass;
	}
});

const fetchArtifacts = async () => {
	if (doi.value !== '') {
		const allArtifacts = await getXDDArtifacts(doi.value);
		// filter out Document extraction type
		artifacts.value = allArtifacts.filter((art) => art.askemClass !== XDDExtractionType.Document);
	} else {
		// note that some XDD documents do not have a valid doi
		artifacts.value = [];
	}
};

watch(doi, (currentValue, oldValue) => {
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
	padding: 2rem;
	font-size: large;
	height: calc(100vh - 50px);
	width: 100%;
	overflow: auto;
	background: var(--un-color-body-surface-primary);
	margin: 1rem;
}

.title {
	font-weight: bold;
	font-size: x-large;
	line-height: 2rem;
}

.authors {
	font-style: italic;
	padding-top: 8px;
}

.journal,
.publisher,
.doi {
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
	width: 100%;
	height: 100%;
}

.nav-container {
	display: flex;
	align-items: center;
	background-color: white;
	overflow: auto;
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
