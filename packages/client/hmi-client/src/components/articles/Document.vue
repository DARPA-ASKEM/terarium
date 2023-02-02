<template>
	<section class="doc-view-container" ref="sectionElem">
		<div v-if="doc">
			<header>
				<div class="journal" v-html="highlightSearchTerms(doc.journal)" />
				<h4 class="title" v-html="highlightSearchTerms(doc.title)" />
				<div class="authors" v-html="formatArticleAuthors(doc)" />
				<div class="details">
					<div v-if="docLink || doi">
						DOI:
						<a
							:href="`https://doi.org/${doi}`"
							rel="noreferrer noopener"
							v-html="highlightSearchTerms(doi)"
						/>
					</div>
					<div v-html="highlightSearchTerms(doc.publisher)" />
					<Button
						v-if="docLink || doi"
						class="p-button-sm p-button-outlined"
						label="Open PDF"
						@click="openPDF"
					/>
				</div>
			</header>
			<Accordion :multiple="true" :active-index="[0, 1, 2, 3, 4, 5, 6, 7]" class="accordion">
				<AccordionTab v-if="!isEmpty(formattedAbstract)" header="Abstract">
					<span v-html="formattedAbstract" />
				</AccordionTab>
				<AccordionTab v-if="doc?.knownEntities?.summaries?.sections" header="Section summaries">
					<div v-for="(section, index) of doc.knownEntities.summaries.sections" :key="index">
						<div>
							<strong>{{ index }}</strong>
						</div>
						<div v-html="highlightSearchTerms(section)" />
					</div>
				</AccordionTab>
				<AccordionTab
					v-if="!isEmpty(figureArtifacts)"
					header="Figures"
					:data-count="figureArtifacts.length"
				>
					<div v-for="ex in figureArtifacts" :key="ex.askemId" class="extracted-item">
						<div class="img-container">
							<img id="img" :src="'data:image/jpeg;base64,' + ex.properties.image" :alt="''" />
							<span
								v-html="highlightSearchTerms(ex.properties?.caption ?? ex.properties.contentText)"
							/>
						</div>
					</div>
				</AccordionTab>
				<AccordionTab
					v-if="!isEmpty(tableArtifacts)"
					header="Tables"
					:data-count="tableArtifacts.length"
				>
					<div v-for="ex in tableArtifacts" :key="ex.askemId" class="extracted-item">
						<div class="img-container">
							<img id="img" :src="'data:image/jpeg;base64,' + ex.properties.image" :alt="''" />
							<span
								v-html="highlightSearchTerms(ex.properties?.caption ?? ex.properties.contentText)"
							/>
						</div>
					</div>
				</AccordionTab>
				<AccordionTab
					v-if="!isEmpty(equationArtifacts)"
					:header="`Equations (${equationArtifacts.length})`"
				>
					<div v-for="ex in equationArtifacts" :key="ex.askemId" class="extracted-item">
						<div class="img-container">
							<img id="img" :src="'data:image/jpeg;base64,' + ex.properties.image" :alt="''" />
							<span
								v-html="highlightSearchTerms(ex.properties?.caption ?? ex.properties.contentText)"
							/>
						</div>
					</div>
				</AccordionTab>
				<AccordionTab v-if="!isEmpty(urlArtifacts)" :header="`URLs (${urlArtifacts.length})`">
					<div v-for="ex in urlArtifacts" :key="ex.url">
						<b>{{ ex.resourceTitle }}</b>
						<div>
							<a :href="ex.url" rel="noreferrer noopener">{{ ex.url }}</a>
						</div>
					</div>
				</AccordionTab>
				<AccordionTab v-if="!isEmpty(otherArtifacts)" :header="`Others (${otherArtifacts.length})`">
					<div v-for="ex in otherArtifacts" :key="ex.askemId" class="extracted-item">
						<b v-html="highlightSearchTerms(ex.properties.title)" />
						<span v-html="highlightSearchTerms(ex.properties.caption)" />
						<span v-html="highlightSearchTerms(ex.properties.abstractText)" />
						<span v-html="highlightSearchTerms(ex.properties.contentText)" />
					</div>
				</AccordionTab>
				<AccordionTab
					v-if="!isEmpty(doc.citationList)"
					:header="`References (${doc.citationList.length})`"
				>
					<div v-for="(citation, key) of doc.citationList" :Key="key">
						{{ key + 1 }}. <span v-html="formatCitation(citation)"></span>
					</div>
				</AccordionTab>
				<AccordionTab
					v-if="!isEmpty(relatedTerariumArtifacts)"
					:header="`Associated resources (${relatedTerariumArtifacts.length})`"
				>
					<DataTable :value="relatedTerariumModels">
						<Column field="name" header="Models"></Column>
					</DataTable>
					<DataTable :value="relatedTerariumDatasets">
						<Column field="name" header="Datasets"></Column>
					</DataTable>
					<DataTable :value="relatedTerariumDocuments">
						<Column field="name" header="Papers"></Column>
					</DataTable>
				</AccordionTab>
			</Accordion>
		</div>
	</section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { isEmpty } from 'lodash';
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
import * as textUtil from '@/utils/text';

const sectionElem = ref<HTMLElement | null>(null);

const props = defineProps<{
	assetId: string;
	highlight?: string;
}>();

const doc = ref<XDDArticle | null>(null);

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}

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

const formatArticleAuthors = (d: XDDArticle) =>
	highlightSearchTerms(d.author.map((a) => a.name).join(', '));

const docLink = computed(() =>
	doc.value?.link && doc.value.link.length > 0 ? doc.value.link[0].url : null
);

const formattedAbstract = computed(() => {
	if (!doc.value || !doc.value.abstractText) return '';
	return highlightSearchTerms(doc.value.abstractText);
});

const doi = computed(() => getDocumentDoi(doc.value));

/* Artifacts */
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

/* Provenance */
const relatedTerariumModels = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isModel(d)) as Model[]
);
const relatedTerariumDatasets = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDataset(d)) as Dataset[]
);
const relatedTerariumDocuments = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isXDDArticle(d)) as XDDArticle[]
);

const openPDF = () => {
	if (docLink.value) window.open(docLink.value as string);
	else if (doi.value) window.open(`https://doi.org/${doi.value}`);
};

/**
 * Format from xDD citation_list object.
 * -  author (year), title, journal, doi
 */
const formatCitation = (obj: { [key: string]: string }) => {
	let citation: string;
	if (Object.keys(obj).length <= 1) {
		citation = obj.unstructured_citation ?? '';
	}
	citation = `${obj.author}, ${obj.year}, "${obj.title}", ${obj.journal}, ${obj.doi}`;
	return highlightSearchTerms(citation);
};

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

.accordion {
	margin: 0.5rem;
	margin-top: 1rem;
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
