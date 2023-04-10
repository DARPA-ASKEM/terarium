<template>
	<section class="two-columns asset" v-if="doc" ref="sectionElem">
		<nav v-if="isEditable">
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
			<div class="scroll-to-section-links" v-if="documentView === DocumentView.EXRACTIONS">
				<a @click="scrollTo('Top')">Top</a>
				<a v-if="!isEmpty(formattedAbstract)" @click="scrollTo('Abstract')">Abstract</a>
				<a v-if="doc?.knownEntities?.summaries?.sections" @click="scrollTo('SectionSummaries')"
					>Section summaries</a
				>
				<a v-if="!isEmpty(figureArtifacts)" @click="scrollTo('Figures')"
					>Figures <span class="artifact-amount">({{ figureArtifacts.length }})</span></a
				>
				<a v-if="!isEmpty(tableArtifacts)" @click="scrollTo('Tables')"
					>Tables <span class="artifact-amount">({{ tableArtifacts.length }})</span></a
				>
				<a v-if="!isEmpty(equationArtifacts)" @click="scrollTo('Equations')"
					>Equations <span class="artifact-amount">({{ equationArtifacts.length }})</span></a
				>
				<a v-if="!isEmpty(githubUrls)" @click="scrollTo('GithubURLs')"
					>GitHub URLs <span class="artifact-amount">({{ githubUrls.length }})</span></a
				>
				<a v-if="!isEmpty(urlArtifacts)" @click="scrollTo('OtherURLs')"
					>Other URLs <span class="artifact-amount">({{ urlArtifacts.length }})</span></a
				>
				<a v-if="!isEmpty(otherArtifacts)" @click="scrollTo('OtherExtractions')"
					>Other extractions <span class="artifact-amount">({{ otherArtifacts.length }})</span></a
				>
				<a v-if="!isEmpty(doc.citationList)" @click="scrollTo('References')"
					>References <span class="artifact-amount">({{ doc.citationList.length }})</span></a
				>
				<a v-if="!isEmpty(relatedTerariumArtifacts)" @click="scrollTo('AssociatedResources')"
					>Associated resources
					<span class="artifact-amount">({{ relatedTerariumArtifacts.length }})</span></a
				>
			</div>
			<!-- TODO: Add search on page function (highlight matches and scroll to the next one?)-->
			<!--- 
				<div class="p-input-icon-left">
					<i class="pi pi-search" />
					<InputText placeholder="Find in page" class="find-in-page" />
				</div>
				--></nav>
		<section class="asset" v-if="documentView === DocumentView.EXRACTIONS" id="Top">
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
				<section class="pdf-buttons" v-if="doi">
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
				</section>
			</header>
			<Accordion :multiple="true" :active-index="[0, 1, 2, 3, 4, 5, 6, 7]">
				<AccordionTab v-if="!isEmpty(formattedAbstract)">
					<template #header>
						<span id="Abstract">Abstract</span>
					</template>
					<div class="constrain-width">
						<span v-html="formattedAbstract" />
					</div>
				</AccordionTab>
				<AccordionTab v-if="doc?.knownEntities?.summaries?.sections">
					<template #header>
						<span id="SectionSummaries">Section Summaries</span>
					</template>
					<template v-for="(section, index) of doc.knownEntities.summaries.sections" :key="index">
						<h6>{{ index }}</h6>
						<p v-html="highlightSearchTerms(section)" />
					</template>
				</AccordionTab>
				<AccordionTab v-if="!isEmpty(figureArtifacts)">
					<template #header>
						Figures<span class="artifact-amount" id="Figures">({{ figureArtifacts.length }})</span>
					</template>
					<div class="constrain-width">
						<div v-for="ex in figureArtifacts" :key="ex.askemId" class="extracted-item">
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
						</div>
					</div>
				</AccordionTab>
				<AccordionTab v-if="!isEmpty(tableArtifacts)">
					<template #header>
						Tables<span class="artifact-amount" id="Tables">({{ tableArtifacts.length }})</span>
					</template>
					<div class="constrain-width">
						<div v-for="ex in tableArtifacts" :key="ex.askemId" class="extracted-item">
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
						</div>
					</div>
				</AccordionTab>
				<AccordionTab v-if="!isEmpty(equationArtifacts)">
					<template #header>
						Equations<span class="artifact-amount" id="Equations"
							>({{ equationArtifacts.length }})</span
						>
					</template>
					<div class="constrain-width">
						<div v-for="ex in equationArtifacts" :key="ex.askemId" class="extracted-item">
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
						</div>
					</div>
				</AccordionTab>
				<AccordionTab v-if="!isEmpty(githubUrls)">
					<template #header>
						GitHub URLs<span class="artifact-amount" id="GithubURLs"
							>({{ githubUrls.length }})</span
						>
					</template>
					<div class="constrain-width">
						<ul>
							<li
								class="github-link extracted-item"
								v-for="(url, index) in githubUrls"
								:key="index"
							>
								<import-code-button v-if="isEditable" :urlString="url" @open-code="openCode" />
								<a :href="url" rel="noreferrer noopener">{{ url }}</a>
							</li>
						</ul>
					</div>
				</AccordionTab>
				<AccordionTab v-if="!isEmpty(urlArtifacts)">
					<template #header>
						Other URLs<span class="artifact-amount" id="OtherURLs"
							>({{ urlArtifacts.length }})</span
						>
					</template>
					<div class="constrain-width">
						<ul>
							<li v-for="ex in urlArtifacts" :key="ex.url" class="extracted-item">
								<b>{{ ex.resourceTitle }}</b>
								<div>
									<a :href="ex.url" rel="noreferrer noopener">{{ ex.url }}</a>
								</div>
							</li>
						</ul>
					</div>
				</AccordionTab>
				<AccordionTab v-if="!isEmpty(otherArtifacts)">
					<template #header>
						Other extractions<span class="artifact-amount" id="OtherExtractions"
							>({{ otherArtifacts.length }})</span
						>
					</template>
					<div class="constrain-width">
						<div v-for="ex in otherArtifacts" :key="ex.askemId" class="extracted-item">
							<b v-html="highlightSearchTerms(ex.properties.title)" />
							<span v-html="highlightSearchTerms(ex.properties.caption)" />
							<span v-html="highlightSearchTerms(ex.properties.abstractText)" />
							<span v-html="highlightSearchTerms(ex.properties.contentText)" />
						</div>
					</div>
				</AccordionTab>
				<AccordionTab v-if="!isEmpty(doc.citationList)">
					<template #header>
						References<span class="artifact-amount" id="References"
							>({{ doc.citationList.length }})</span
						>
					</template>
					<div class="constrain-width">
						<ul>
							<li v-for="(citation, key) of doc.citationList" :key="key">
								<template v-if="!isEmpty(formatCitation(citation))">
									{{ key + 1 }}. <span v-html="formatCitation(citation)"></span>
								</template>
							</li>
						</ul>
					</div>
				</AccordionTab>
				<AccordionTab v-if="!isEmpty(relatedTerariumArtifacts)">
					<template #header>
						Associated resources
						<span class="artifact-amount" id="AssociatedResources"
							>({{ relatedTerariumArtifacts.length }})</span
						>
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
		<section v-else-if="DocumentView.PDF" class="asset">PDF Preview</section>
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
import Image from 'primevue/image';
import { generatePdfDownloadLink } from '@/services/generate-download-link';
// import InputText from 'primevue/inputtext'; // <-- this is for the keyword search feature commented out below

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

const sectionElem = ref<HTMLElement | null>(null);
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

function scrollTo(elementId: string) {
	document.getElementById(elementId)?.scrollIntoView({ behavior: 'smooth' });
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
		fetchRelatedTerariumArtifacts();
		pdfLink.value = null;
		pdfLink.value = await generatePdfDownloadLink(doi.value); // Generate PDF download link on (doi change)
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
	fetchRelatedTerariumArtifacts();
});
</script>
<style scoped>
.two-columns {
	display: flex;
	flex-direction: row;
}

.pdf-buttons {
	display: flex;
	gap: 0.5rem;
}

nav {
	margin-left: 0.5rem;
	min-width: 14rem;
	position: sticky;
	top: 0;
}

.scroll-to-section-links {
	display: flex;
	flex-direction: column;
	gap: 1rem;
	margin-top: 1rem;
}

.find-in-page {
	border: 1px solid var(--surface-border-light) !important;
	padding: 0.75rem;
	width: 11.75rem;
}

.constrain-width {
	max-width: var(--constrain-width);
}

.extracted-item {
	border: 1px solid var(--surface-border-light);
	padding: 1rem;
	border-radius: var(--border-radius);
}

.extracted-image {
	max-width: 30rem;
}
</style>
