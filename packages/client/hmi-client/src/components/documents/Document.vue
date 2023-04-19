<template>
	<tera-asset
		v-if="doc"
		:is-editable="isEditable"
		:name="highlightSearchTerms(doc.title)"
		:overline="highlightSearchTerms(doc.journal)"
		:authors="formatDocumentAuthors(doc)"
		:doi="highlightSearchTerms(doi)"
		:publisher="highlightSearchTerms(doc.publisher)"
	>
		<template #nav>
			<tera-asset-nav
				:asset-content="documentContent"
				:extraction-mode="documentView === DocumentView.EXRACTIONS"
				v-if="isEditable"
			>
				<template #viewing-mode>
					<span class="p-buttonset">
						<Button
							class="p-button-secondary p-button-sm"
							label="Extractions"
							icon="pi pi-list"
							@click="documentView = DocumentView.EXRACTIONS"
							:active="documentView === DocumentView.EXRACTIONS"
						/>
						<Button
							class="p-button-secondary p-button-sm"
							label="PDF"
							icon="pi pi-file"
							@click="documentView = DocumentView.PDF"
							:active="documentView === DocumentView.PDF"
						/>
					</span>
				</template>
				<template #page-search>
					<!-- TODO: Add search on page function (highlight matches and scroll to the next one?)-->
					<span class="p-input-icon-left">
						<i class="pi pi-search" />
						<InputText placeholder="Find in page" class="p-inputtext-sm" />
					</span>
				</template>
			</tera-asset-nav>
		</template>
		<template #bottom-header-buttons>
			<Button
				class="p-button-sm p-button-outlined"
				icon="pi pi-external-link"
				label="Open PDF"
				@click="openPDF"
				:loading="!pdfLink && !linkIsPDF()"
			/>
			<Button
				class="p-button-sm p-button-outlined"
				@click="downloadPDF"
				:icon="'pi pi-cloud-download'"
				:loading="!pdfLink"
				label="Download PDF"
			/>
		</template>
		<Accordion
			v-if="documentView === DocumentView.EXRACTIONS"
			:multiple="true"
			:active-index="[0, 1, 2, 3, 4, 5, 6, 7]"
		>
			<AccordionTab v-if="!isEmpty(formattedAbstract)">
				<template #header>
					<header id="Abstract">Abstract</header>
				</template>
				<p v-html="formattedAbstract" />
			</AccordionTab>
			<AccordionTab v-if="doc?.knownEntities?.summaries?.sections">
				<template #header>
					<header id="Section-Summaries">Section Summaries</header>
				</template>
				<ul>
					<li v-for="(section, index) of doc.knownEntities.summaries.sections" :key="index">
						<h6>{{ index }}</h6>
						<p v-html="highlightSearchTerms(section)" />
					</li>
				</ul>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(figures)">
				<template #header>
					<header id="Figures">
						Figures<span class="artifact-amount">({{ figures.length }})</span>
					</header>
				</template>
				<ul>
					<li v-for="ex in figures" :key="ex.askemId" class="extracted-item">
						<Image
							id="img"
							class="extracted-image"
							:src="'data:image/jpeg;base64,' + ex.properties.image"
							:alt="''"
							preview
						/>
						<tera-show-more-text
							:text="highlightSearchTerms(ex.properties?.caption ?? ex.properties.contentText)"
							:lines="previewLineLimit"
						/>
					</li>
				</ul>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(tables)">
				<template #header>
					<header id="Tables">
						Tables<span class="artifact-amount">({{ tables.length }})</span>
					</header>
				</template>
				<ul>
					<li v-for="ex in tables" :key="ex.askemId" class="extracted-item">
						<div class="extracted-image">
							<Image
								id="img"
								:src="'data:image/jpeg;base64,' + ex.properties.image"
								:alt="''"
								preview
							/>
						</div>
						<tera-show-more-text
							:text="highlightSearchTerms(ex.properties?.caption ?? ex.properties.contentText)"
							:lines="previewLineLimit"
						/>
					</li>
				</ul>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(equations)">
				<template #header>
					<header id="Equations">
						Equations<span class="artifact-amount">({{ equations.length }})</span>
					</header>
				</template>
				<ul>
					<li v-for="ex in equations" :key="ex.askemId" class="extracted-item">
						<div class="extracted-image">
							<Image
								id="img"
								:src="'data:image/jpeg;base64,' + ex.properties.image"
								:alt="''"
								preview
							/>
						</div>
						<tera-show-more-text
							:text="highlightSearchTerms(ex.properties?.caption ?? ex.properties.contentText)"
							:lines="previewLineLimit"
						/>
					</li>
				</ul>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(githubUrls)">
				<template #header>
					<header id="Github-URLs">
						GitHub URLs<span class="artifact-amount">({{ githubUrls.length }})</span>
					</header>
				</template>
				<ul>
					<li class="extracted-item" v-for="(url, index) in githubUrls" :key="index">
						<import-code-button v-if="isEditable" :urlString="url" @open-code="openCode" />
					</li>
				</ul>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(otherUrls)">
				<template #header>
					<header id="Other-URLs">
						Other URLs<span class="artifact-amount">({{ otherUrls.length }})</span>
					</header>
				</template>
				<ul>
					<li v-for="ex in otherUrls" :key="ex.url" class="extracted-item">
						<b>{{ ex.resourceTitle }}</b>
						<div>
							<a :href="ex.url" rel="noreferrer noopener">{{ ex.url }}</a>
						</div>
					</li>
				</ul>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(otherExtractions)">
				<template #header>
					<header id="Other-Extractions">
						Other extractions<span class="artifact-amount">({{ otherExtractions.length }})</span>
					</header>
				</template>
				<ul>
					<li v-for="ex in otherExtractions" :key="ex.askemId" class="extracted-item">
						<b v-html="highlightSearchTerms(ex.properties.title)" />
						<span v-html="highlightSearchTerms(ex.properties.caption)" />
						<span v-html="highlightSearchTerms(ex.properties.abstractText)" />
						<span v-html="highlightSearchTerms(ex.properties.contentText)" />
					</li>
				</ul>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(doc.citationList)">
				<template #header>
					<header id="References">
						References<span class="artifact-amount">({{ doc.citationList.length }})</span>
					</header>
				</template>
				<ul>
					<li v-for="(citation, key) of doc.citationList" :key="key">
						<template v-if="!isEmpty(formatCitation(citation))">
							{{ key + 1 }}. <span v-html="formatCitation(citation)"></span>
						</template>
					</li>
				</ul>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(associatedResources)">
				<template #header>
					<header id="Associated-Resources">
						Associated resources
						<span class="artifact-amount">({{ associatedResources.length }})</span>
					</header>
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
		<section v-else-if="DocumentView.PDF" class="asset">PDF Preview</section>
	</tera-asset>
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
import Image from 'primevue/image';
import { generatePdfDownloadLink } from '@/services/generate-download-link';
import TeraAsset from '@/components/widgets/tera-asset.vue';
import TeraAssetNav from '@/components/widgets/tera-asset-nav.vue';
import InputText from 'primevue/inputtext';

enum DocumentView {
	EXRACTIONS = 'extractions',
	PDF = 'pdf'
}

const props = defineProps<{
	xddUri: string;
	isEditable: boolean;
	highlight?: string;
	previewLineLimit?: number;
}>();

const doc = ref<DocumentType | null>(null);
const pdfLink = ref<string | null>(null);
const documentView = ref(DocumentView.EXRACTIONS);

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
const associatedResources = ref<ResultType[]>([]);

const figures = computed(
	() => artifacts.value.filter((d) => d.askemClass === XDDExtractionType.Figure) || []
);
const tables = computed(
	() => artifacts.value.filter((d) => d.askemClass === XDDExtractionType.Table) || []
);
const equations = computed(
	() => artifacts.value.filter((d) => d.askemClass === XDDExtractionType.Equation) || []
);
const otherUrls = computed(() =>
	doc.value?.knownEntities && doc.value.knownEntities.urlExtractions.length > 0
		? uniqWith(doc.value.knownEntities.urlExtractions, isEqual) // removes duplicate urls
		: []
);
const githubUrls = computed(() => doc.value?.githubUrls ?? []);
const otherExtractions = computed(() => {
	const exclusion = [
		XDDExtractionType.URL,
		XDDExtractionType.Table,
		XDDExtractionType.Figure,
		XDDExtractionType.Equation
	];

	return artifacts.value.filter((d) => !exclusion.includes(d.askemClass as XDDExtractionType));
});

/* Provenance */
const relatedTerariumModels = computed(
	() => associatedResources.value.filter((d) => isModel(d)) as Model[]
);
const relatedTerariumDatasets = computed(
	() => associatedResources.value.filter((d) => isDataset(d)) as Dataset[]
);
const relatedTerariumDocuments = computed(
	() => associatedResources.value.filter((d) => isDocument(d)) as DocumentType[]
);

const documentContent = computed(() => [
	{ key: 'Abstract', value: formattedAbstract.value },
	{ key: 'Section-Summaries', value: doc.value?.knownEntities?.summaries?.sections },
	{ key: 'Figures', value: figures.value },
	{ key: 'Tables', value: tables.value },
	{ key: 'Equations', value: equations.value },
	{ key: 'Github-URLs', value: githubUrls.value },
	{ key: 'Other-URLs', value: otherUrls.value },
	{ key: 'Other-Extractions', value: otherExtractions.value },
	{ key: 'References', value: doc.value?.citationList },
	{ key: 'Associated-Resources', value: associatedResources.value }
]);

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

const fetchAssociatedResources = async () => {
	if (doc.value) {
		const results = await getRelatedArtifacts(props.xddUri, ProvenanceType.Publication);
		associatedResources.value = results;
	} else {
		associatedResources.value = [];
	}
};

// Better than wrapping download button with an anchor
function downloadPDF() {
	if (pdfLink.value) {
		const link = document.createElement('a');
		link.href = pdfLink.value;
		link.download = `${doi.value}.pdf`;
		link.click();
	}
}

function linkIsPDF() {
	const link = docLink.value ?? doi.value;
	return link.match(/^.*\.(pdf|PDF)$/);
}

const openPDF = () => {
	if (linkIsPDF()) {
		if (docLink.value) window.open(docLink.value as string);
		else if (doi.value) window.open(`https://doi.org/${doi.value}`);
		return;
	}
	if (pdfLink.value) window.open(pdfLink.value);
};

watch(doi, async (currentValue, oldValue) => {
	if (currentValue !== oldValue) {
		fetchDocumentArtifacts();
		fetchAssociatedResources();
		pdfLink.value = null;
		pdfLink.value = await generatePdfDownloadLink(doi.value); // Generate PDF download link on (doi change)
	}
});

/**
 * Format from xDD citation_list object.
 * -  author (year), title, journal, doi
 */
const formatCitation = (obj: { [key: string]: string }) => {
	let citation: string;
	if (Object.keys(obj).length <= 1) {
		citation =
			obj?.unstructured_citation?.replace(/\bhttps?:\/\/\S+/, `<a href=$&>$&</a>`) ??
			`<a href="https://doi.org/${obj.doi}">${obj.doi}</a>`;
	} else {
		citation = `${obj.author}, ${obj.year}, "${obj.title}", ${obj.journal}`;
		if (obj.doi !== undefined) {
			citation += `, <a href="https://doi.org/${obj.doi}">${obj.doi}</a>`;
		}
	}
	return highlightSearchTerms(citation);
};

onMounted(async () => {
	fetchDocumentArtifacts();
	fetchAssociatedResources();
});
</script>
<style scoped>
.find-in-page {
	border: 1px solid var(--surface-border-light) !important;
	padding: 0.75rem;
	width: 11.75rem;
}

.extracted-item {
	border: 1px solid var(--surface-border-light);
	padding: 1rem;
	border-radius: var(--border-radius);
}

.extracted-item > .extracted-image {
	display: block;
	max-width: 30rem;
	margin-bottom: 0.5rem;
	width: fit-content;
	padding: 8px;
	border: 1px solid var(--gray-300);
	border-radius: 6px;
	object-fit: contain;
}
</style>
