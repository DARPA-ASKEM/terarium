<template>
	<section class="asset" v-if="doc" ref="sectionElem">
		<header>
			<div class="journal" v-html="highlightSearchTerms(doc.journal)" />
			<h4 v-html="highlightSearchTerms(doc.title)" />
			<div class="authors" v-html="formatDocumentAuthors(doc)" />
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
				v-if="linkIsPDF()"
				class="p-button-sm p-button-outlined"
				label="Open PDF"
				@click="openPDF"
			/>
			<a
				v-if="doi"
				class="download-pdf"
				:href="isEmpty(pdfLink) ? 'javascript:void(0)' : pdfLink"
				:download="`${doi}.pdf`"
			>
				<Button
					class="p-button-sm p-button-outlined"
					:icon="isEmpty(pdfLink) ? 'pi pi-spin pi-spinner' : 'pi pi-cloud-download'"
					:disabled="isEmpty(pdfLink)"
					>Download PDF</Button
				>
			</a>
		</header>
		<Accordion :multiple="true" :active-index="[0, 1, 2, 3, 4, 5, 6, 7]">
			<AccordionTab v-if="!isEmpty(formattedAbstract)" header="Abstract">
				<span v-html="formattedAbstract" />
			</AccordionTab>
			<AccordionTab v-if="doc?.knownEntities?.summaries?.sections" header="Section summaries">
				<template v-for="(section, index) of doc.knownEntities.summaries.sections" :key="index">
					<h6>{{ index }}</h6>
					<p v-html="highlightSearchTerms(section)" />
				</template>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(figureArtifacts)">
				<template #header>
					Figures<span class="artifact-amount">({{ figureArtifacts.length }})</span>
				</template>
				<div v-for="ex in figureArtifacts" :key="ex.askemId" class="extracted-item">
					<img id="img" :src="'data:image/jpeg;base64,' + ex.properties.image" :alt="''" />
					<tera-show-more-text
						:text="highlightSearchTerms(ex.properties?.caption ?? ex.properties.contentText)"
						:lines="previewLineLimit"
					/>
				</div>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(tableArtifacts)">
				<template #header>
					Tables<span class="artifact-amount">({{ tableArtifacts.length }})</span>
				</template>
				<div v-for="ex in tableArtifacts" :key="ex.askemId" class="extracted-item">
					<img id="img" :src="'data:image/jpeg;base64,' + ex.properties.image" :alt="''" />
					<tera-show-more-text
						:text="highlightSearchTerms(ex.properties?.caption ?? ex.properties.contentText)"
						:lines="previewLineLimit"
					/>
				</div>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(equationArtifacts)">
				<template #header>
					Equations<span class="artifact-amount">({{ equationArtifacts.length }})</span>
				</template>
				<div v-for="ex in equationArtifacts" :key="ex.askemId" class="extracted-item">
					<img id="img" :src="'data:image/jpeg;base64,' + ex.properties.image" :alt="''" />
					<tera-show-more-text
						:text="highlightSearchTerms(ex.properties?.caption ?? ex.properties.contentText)"
						:lines="previewLineLimit"
					/>
				</div>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(githubUrls)">
				<template #header>
					Github URLs<span class="artifact-amount">({{ githubUrls.length }})</span>
				</template>
				<ul>
					<li class="github-link" v-for="(url, index) in githubUrls" :key="index">
						<import-code-button v-if="isEditable" :urlString="url" @open-code="openCode" />
						<a :href="url" rel="noreferrer noopener">{{ url }}</a>
					</li>
				</ul>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(urlArtifacts)">
				<template #header>
					URLs<span class="artifact-amount">({{ urlArtifacts.length }})</span>
				</template>
				<ul>
					<li v-for="ex in urlArtifacts" :key="ex.url">
						<b>{{ ex.resourceTitle }}</b>
						<div>
							<a :href="ex.url" rel="noreferrer noopener">{{ ex.url }}</a>
						</div>
					</li>
				</ul>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(otherArtifacts)">
				<template #header>
					Others<span class="artifact-amount">({{ otherArtifacts.length }})</span>
				</template>
				<div v-for="ex in otherArtifacts" :key="ex.askemId" class="extracted-item">
					<b v-html="highlightSearchTerms(ex.properties.title)" />
					<span v-html="highlightSearchTerms(ex.properties.caption)" />
					<span v-html="highlightSearchTerms(ex.properties.abstractText)" />
					<span v-html="highlightSearchTerms(ex.properties.contentText)" />
				</div>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(doc.citationList)">
				<template #header>
					References<span class="artifact-amount">({{ doc.citationList.length }})</span>
				</template>
				<ul>
					<li v-for="(citation, key) of doc.citationList" :key="key">
						<template v-if="!isEmpty(formatCitation(citation))">
							{{ key + 1 }}. <span v-html="formatCitation(citation)"></span>
						</template>
					</li>
				</ul>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(relatedTerariumArtifacts)">
				<template #header>
					Associated resources
					<span class="artifact-amount">({{ relatedTerariumArtifacts.length }})</span>
				</template>
				<DataTable :value="relatedTerariumModels">
					<Column field="name" header="Models"></Column>
				</DataTable>
				<DataTable :value="relatedTerariumDatasets">
					<Column field="name" header="Datasets"></Column>
				</DataTable>
				<DataTable :value="relatedTerariumDocuments">
					<Column field="name" header="Documents"></Column>
				</DataTable>
			</AccordionTab>
		</Accordion>
	</section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { isEmpty, isEqual, uniqWith } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';
import { getDocumentById, getXDDArtifacts } from '@/services/data';
import { XDDExtractionType } from '@/types/XDD';
import { XDDArtifact, DocumentType } from '@/types/Document';
import { getDocumentDoi, isModel, isDataset, isDocument } from '@/utils/data-util';
import { ResultType, Tab } from '@/types/common';
import { getRelatedArtifacts } from '@/services/provenance';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import ImportCodeButton from '@/components/widgets/import-code-button.vue';
import { Model } from '@/types/Model';
import { Dataset } from '@/types/Dataset';
import { ProvenanceType } from '@/types/Types';
import * as textUtil from '@/utils/text';
import API from '@/api/api';
import { logger } from '@/utils/logger';
import { toQueryString } from '@/utils/query-string';

const props = defineProps<{
	xddUri: string;
	isEditable: boolean;
	highlight?: string;
	previewLineLimit?: number;
}>();

const sectionElem = ref<HTMLElement | null>(null);
const doc = ref<DocumentType | null>(null);
const pdfLink = ref('');

const emit = defineEmits(['open-asset']);

function openCode(assetToOpen: Tab, newCode?: string) {
	emit('open-asset', assetToOpen, newCode);
}

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
		const id = props.xddUri;
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

const formatDocumentAuthors = (d: DocumentType) =>
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
		? uniqWith(doc.value.knownEntities.urlExtractions, isEqual) // removes duplicate urls
		: []
);
const githubUrls = computed(() => doc.value?.githubUrls ?? []);
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
		const results = await getRelatedArtifacts(props.xddUri, ProvenanceType.Publication);
		relatedTerariumArtifacts.value = results;
	} else {
		relatedTerariumArtifacts.value = [];
	}
};

async function fetchPDF() {
	pdfLink.value = '';
	if (!doi.value) return;
	const query = { doi: doi.value };
	const URL = `/download?${toQueryString(query)}`;
	const response = await API.get(URL, { responseType: 'arraybuffer' }).catch((error) => {
		logger.error(`Error: Unable to download pdf for doi ${doi.value}: ${error}`);
	});
	const blob = new Blob([response?.data], { type: 'application/pdf' });
	pdfLink.value = window.URL.createObjectURL(blob);
}

watch(doi, (currentValue, oldValue) => {
	if (currentValue !== oldValue) {
		fetchDocumentArtifacts();
		fetchRelatedTerariumArtifacts();
		fetchPDF();
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
	() => relatedTerariumArtifacts.value.filter((d) => isDocument(d)) as DocumentType[]
);

function linkIsPDF() {
	const link = docLink.value ?? doi.value;
	return link.match(/^.*\.(pdf|PDF)$/);
}

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
		citation = citation.replace(/\bhttps?:\/\/\S+/, `<a href=$&>$&</a>`);
	} else {
		citation = `${obj.author}, ${obj.year}, "${obj.title}", ${obj.journal}, ${obj.doi}`;
	}
	return highlightSearchTerms(citation);
};

onMounted(async () => {
	fetchDocumentArtifacts();
	fetchRelatedTerariumArtifacts();
});
</script>
<style>
.download-pdf,
.download-pdf:hover,
.download-pdf:focus {
	display: inline-block;
	text-decoration: none;
}
</style>
