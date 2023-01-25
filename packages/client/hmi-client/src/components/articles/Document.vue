<template>
	<section class="doc-view-container" ref="sectionElem">
		<div v-if="doc">
			<header>
				<div class="journal">{{ doc.journal }}</div>
				<h4 class="title">{{ doc.title }}</h4>
				<div class="authors">{{ formatArticleAuthors(doc) }}</div>
				<div class="details">
					<div v-if="docLink || doi">
						DOI:
						<a :href="`https://doi.org/${doi}`" target="_blank" rel="noreferrer noopener">
							{{ doi }}
						</a>
					</div>
					<div>{{ doc.publisher }}</div>
					<!-- TODO -->
					<!-- Journal impact factor -->
					<!-- # Citations -->
					<Button
						v-if="docLink || doi"
						class="p-button-sm p-button-outlined"
						label="Open PDF"
						@click="openPDF"
					/>
				</div>
			</header>
			<Accordion :multiple="true" :active-index="[0, 1, 2, 3, 4, 5, 6, 7]" class="accordian">
				<AccordionTab v-if="formattedAbstract.length > 0" header="Abstract">
					{{ formattedAbstract }}
				</AccordionTab>
				<AccordionTab
					v-if="doc.knownEntities && doc.knownEntities.summaries.sections"
					header="Section summaries"
				>
					<div v-for="(v, k) of doc.knownEntities.summaries.sections" :key="k">
						<div>
							<strong>{{ k }}</strong>
						</div>
						<div>{{ v }}</div>
						<br />
					</div>
				</AccordionTab>
				<AccordionTab v-if="figureArtifacts.length > 0" header="Figures">
					<div v-for="ex in figureArtifacts" :key="ex.askemId" class="extracted-item">
						<div class="img-container">
							<img id="img" :src="'data:image/jpeg;base64,' + ex.properties.image" :alt="''" />
							<span>{{
								ex.properties.caption ? ex.properties.caption : ex.properties.contentText
							}}</span>
						</div>
					</div>
				</AccordionTab>
				<AccordionTab v-if="tableArtifacts.length > 0" header="Tables">
					<div v-for="ex in tableArtifacts" :key="ex.askemId" class="extracted-item">
						<div class="img-container">
							<img id="img" :src="'data:image/jpeg;base64,' + ex.properties.image" :alt="''" />
							<span>{{
								ex.properties.caption ? ex.properties.caption : ex.properties.contentText
							}}</span>
						</div>
					</div>
				</AccordionTab>
				<AccordionTab v-if="equationArtifacts.length > 0" header="Equations">
					<div v-for="ex in equationArtifacts" :key="ex.askemId" class="extracted-item">
						<div class="img-container">
							<img id="img" :src="'data:image/jpeg;base64,' + ex.properties.image" :alt="''" />
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
				<AccordionTab header="References">
					<div v-for="(citation, key) of doc.citationList" :Key="key">
						{{ key + 1 }}. {{ formatCitation(citation) }}
					</div>
				</AccordionTab>
				<!--
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
	if (docLink.value) window.open(docLink.value as string);
	else if (doi.value) window.open(`https://doi.org/${doi.value}`);
};

/**
 * Format from xDD citation_list object.
 *
 * -  author (year), title, journal, doi
 */
const formatCitation = (obj: { [key: string]: string }) => {
	if (Object.keys(obj).length <= 1) {
		if (obj.unstructured_citation) return obj.unstructured_citation;
		return '';
	}
	return `${obj.author}, ${obj.year}, "${obj.title}", ${obj.journal}, ${obj.doi}`;
};

// fetch artifacts from COSMOS using the doc doi
onMounted(async () => {
	fetchDocumentArtifacts();
	fetchRelatedTerariumArtifacts();
});
</script>

<style scoped>
header {
	padding: 0rem 1rem;
	color: var(--text-color-subdued);
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

div,
span {
	overflow-wrap: break-word;
}

a {
	color: var(--text-color-subdued);
}

a:hover {
	color: var(--primary-color-dark);
}

.p-button.p-button-outlined {
	margin-top: 1rem;
	background-color: transparent;
	color: var(--text-color-primary);
	font-weight: bold;
	box-shadow: var(--text-color-disabled) inset 0 0 0 1px;
}

.title {
	color: var(--text-color-primary);
}

.authors,
.journal {
	color: var(--primary-color-dark);
}

.accordian {
	font-size: 14px;
	margin-top: 1rem;
	margin-bottom: 1rem;
}

.extracted-item {
	padding-bottom: 0.5rem;
}

.img-container {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

.img-container > img {
	max-height: 10rem;
	width: 100%;
	object-fit: contain;
	border: 1px solid var(--gray-300);
}
</style>
