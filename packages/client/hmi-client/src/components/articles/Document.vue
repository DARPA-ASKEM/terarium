<template>
	<section class="doc-view-container" ref="sectionElem">
		<div v-if="doc">
			<div class="journal">{{ doc.journal }}</div>
			<h4 class="title">
				{{ doc.title }}
			</h4>
			<div class="authors">{{ formatArticleAuthors(doc) }}</div>
			<br />

			<div class="row">
				<!-- TODO -->
				<!-- Journal impact factor -->
				<!-- # Citations -->
				<div>
					<div class="publisher">{{ doc.publisher }}</div>
					<div class="doi">DOI: {{ doi }}</div>
				</div>
				<Button v-if="docLink" label="Open PDF" @click="openPDF"> </Button>
			</div>

			<Accordion :multiple="true" :active-index="[0, 1, 2, 3, 4, 5, 6, 7]" class="accordian">
				<AccordionTab v-if="formattedAbstract.length > 0" header="Abstract">
					{{ formattedAbstract }}
				</AccordionTab>

				<!--
				<AccordionTab header="Snippets"> </AccordionTab>
				-->

				<AccordionTab v-if="figureArtifacts.length > 0" header="Figures">
					<div v-for="ex in figureArtifacts" :key="ex.askemId" class="extracted-item">
						<div class="img-container">
							<img
								id="img"
								:src="'data:image/jpeg;base64,' + ex.properties.image"
								:alt="''"
								:style="{ 'max-width': imageSize }"
							/>
							<span>{{
								ex.properties.caption ? ex.properties.caption : ex.properties.contentText
							}}</span>
						</div>
					</div>
				</AccordionTab>

				<AccordionTab v-if="tableArtifacts.length > 0" header="Tables">
					<div v-for="ex in tableArtifacts" :key="ex.askemId" class="extracted-item">
						<div class="img-container">
							<img
								id="img"
								:src="'data:image/jpeg;base64,' + ex.properties.image"
								:alt="''"
								:style="{ 'max-width': imageSize }"
							/>
							<span>{{
								ex.properties.caption ? ex.properties.caption : ex.properties.contentText
							}}</span>
						</div>
					</div>
				</AccordionTab>

				<AccordionTab v-if="equationArtifacts.length > 0" header="Equations">
					<div v-for="ex in equationArtifacts" :key="ex.askemId" class="extracted-item">
						<div class="img-container">
							<img
								id="img"
								:src="'data:image/jpeg;base64,' + ex.properties.image"
								:alt="''"
								:style="{ 'max-width': imageSize }"
							/>
							<span>{{
								ex.properties.caption ? ex.properties.caption : ex.properties.contentText
							}}</span>
						</div>
					</div>
				</AccordionTab>

				<AccordionTab v-if="urlArtifacts.length > 0" header="URLs">
					<div v-for="ex in urlArtifacts" :key="ex.url">
						<b>{{ ex.resourceTitle }}</b>
						<div>
							<a :href="ex.url" target="_blank" rel="noreferrer noopener">{{ ex.url }}</a>
						</div>
					</div>
				</AccordionTab>

				<AccordionTab v-if="otherArtifacts.length > 0" header="Others">
					<div v-for="ex in otherArtifacts" :key="ex.askemId" class="extracted-item">
						<b>{{ ex.properties.title }}</b>
						{{ ex.properties.caption }}
						{{ ex.properties.abstractText }}
						{{ ex.properties.contentText }}
					</div>
				</AccordionTab>

				<!--
				<AccordionTab header="References"> </AccordionTab>
				<AccordionTab header="Cited by"> </AccordionTab>
				-->

				<AccordionTab
					v-if="relatedTerariumArtifacts.length > 0"
					header="Related TERARium artifacts"
				>
					<DataTable :value="relatedTerariumModels">
						<Column field="name" header="Models"></Column>
					</DataTable>
					<br />
					<DataTable :value="relatedTerariumDatasets">
						<Column field="name" header="Datasets"></Column>
					</DataTable>
					<br />
					<DataTable :value="relatedTerariumDocuments">
						<Column field="name" header="Papers"></Column>
					</DataTable>
				</AccordionTab>

				<!--
				<AccordionTab header="Provenance"> </AccordionTab>
				-->
			</Accordion>
		</div>
	</section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';
import { getDocumentById, getXDDArtifacts } from '@/services/data';
import { XDDArticle, XDDArtifact, XDDExtractionType } from '@/types/XDD';
import { getDocumentDoi, isModel, isDataset, isXDDArticle } from '@/utils/data-util';
import { ResultType } from '@/types/common';
import { getRelatedArtifacts } from '@/services/provenance';
import { Model } from '@/types/Model';
import { Dataset } from '@/types/Dataset';
import { ProvenanceType } from '@/types/Provenance';

const sectionElem = ref<HTMLElement | null>(null);

const props = defineProps<{
	assetId: string;
}>();

const doc = ref<XDDArticle | null>(null);

watch(
	props,
	async () => {
		const id = props.assetId;
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

// const formatAbstract = (d: XDDArticle) =>
// 	(d.abstractText && typeof d.abstractText === 'string' ? d.abstractText : false) ||
// 	'[no abstract]';

const formattedAbstract = computed(() => {
	if (!doc.value || !doc.value.abstractText) return '';
	return doc.value.abstractText;
});

const doi = computed(() => getDocumentDoi(doc.value));

const artifacts = ref<XDDArtifact[]>([]);
const relatedTerariumArtifacts = ref<ResultType[]>([]);

const figureArtifacts = computed(
	() => artifacts.value.filter((d) => d.askemClass === XDDExtractionType.Figure) || []
);
const tableArtifacts = computed(
	() => artifacts.value.filter((d) => d.askemClass === XDDExtractionType.Table) || []
);
const equationArtifacts = computed(
	() => artifacts.value.filter((d) => d.askemClass === XDDExtractionType.Equation) || []
);
const urlArtifacts = computed(() =>
	doc.value?.knownEntities && doc.value.knownEntities.urlExtractions.length > 0
		? doc.value.knownEntities.urlExtractions
		: []
);

const relatedTerariumModels = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isModel(d)) as Model[]
);
const relatedTerariumDatasets = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDataset(d)) as Dataset[]
);
const relatedTerariumDocuments = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isXDDArticle(d)) as XDDArticle[]
);

const otherArtifacts = computed(() => {
	const exclusion = [
		XDDExtractionType.URL,
		XDDExtractionType.Table,
		XDDExtractionType.Figure,
		XDDExtractionType.Equation
	];

	return artifacts.value.filter((d) => !exclusion.includes(d.askemClass as XDDExtractionType));
});

// This fetches various parts of the document: figures, tables, equations ... etc
const fetchDocumentArtifacts = async () => {
	if (doi.value !== '') {
		const allArtifacts = await getXDDArtifacts(doi.value);
		// filter out Document extraction type
		artifacts.value = allArtifacts.filter((art) => art.askemClass !== XDDExtractionType.Document);
	} else {
		// note that some XDD documents do not have a valid doi
		artifacts.value = [];
	}
};

const fetchRelatedTerariumArtifacts = async () => {
	if (doc.value) {
		const results = await getRelatedArtifacts(props.assetId, ProvenanceType.Publication);
		relatedTerariumArtifacts.value = results;
	} else {
		relatedTerariumArtifacts.value = [];
	}
};

watch(doi, (currentValue, oldValue) => {
	if (currentValue !== oldValue) {
		fetchDocumentArtifacts();
		fetchRelatedTerariumArtifacts();
	}
});

const openPDF = () => {
	window.open(docLink.value as string);
};

// Image size will adapt depend on available space
const imageSize = ref('160px');

// fetch artifacts from COSMOS using the doc doi
onMounted(async () => {
	const rect = (sectionElem.value as HTMLElement).getBoundingClientRect();
	if (rect.width > 800) {
		imageSize.value = '400px';
	}
	fetchDocumentArtifacts();
	fetchRelatedTerariumArtifacts();
});
</script>

<style scoped>
.doc-view-container {
	padding: 2rem;
	font-size: large;
	overflow: auto;
}

.p-column-title {
	font-weight: 600;
}

.row {
	display: flex;
	justify-content: space-between;
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

.accordian {
	margin-top: 1rem;
	margin-bottom: 1rem;
}

.extracted-item {
	padding-bottom: 20px;
}

/* Meant for left:right image:caption */
.img-container {
	display: flex;
	flex-direction: row;
}

.img-container > img {
	margin: 5px;
	border: 1px solid var(--gray-300);
}
</style>
