<template>
	<section class="asset" v-if="doc" ref="sectionElem">
		<div class="two-columns">
			<div class="content-navigator" v-if="isEditable">
				<!-- TODO: Set these buttons up to toggle between PDF and extraction view -->
				<div class="p-buttonset content-switcher">
					<Button
						class="p-button-secondary p-button-sm"
						label="Extractions"
						icon="pi pi-list"
						outlined
						:active="true"
					/>
					<Button
						class="p-button-secondary p-button-sm"
						label="PDF"
						icon="pi pi-file"
						outlined
						:active="false"
						disabled
					/>
				</div>

				<!-- TODO: Add scroll to anchor feature -->
				<div class="scroll-to-section-links">
					<a>Top</a>
					<a v-if="!isEmpty(formattedAbstract)">Abstract</a>
					<a v-if="doc?.knownEntities?.summaries?.sections">Section summaries</a>
					<a v-if="!isEmpty(figureArtifacts)"
						>Figures <span class="artifact-amount">({{ figureArtifacts.length }})</span></a
					>
					<a v-if="!isEmpty(tableArtifacts)"
						>Tables <span class="artifact-amount">({{ tableArtifacts.length }})</span></a
					>
					<a v-if="!isEmpty(equationArtifacts)"
						>Equations <span class="artifact-amount">({{ equationArtifacts.length }})</span></a
					>
					<a v-if="!isEmpty(githubUrls)"
						>GitHub URLs <span class="artifact-amount">({{ githubUrls.length }})</span></a
					>
					<a v-if="!isEmpty(urlArtifacts)"
						>Other URLs <span class="artifact-amount">({{ urlArtifacts.length }})</span></a
					>
					<a v-if="!isEmpty(otherArtifacts)"
						>Other extractions <span class="artifact-amount">({{ otherArtifacts.length }})</span></a
					>
					<a v-if="!isEmpty(doc.citationList)"
						>References <span class="artifact-amount">({{ doc.citationList.length }})</span></a
					>
					<a v-if="!isEmpty(relatedTerariumArtifacts)"
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
				-->
			</div>
			<div class="asset" v-bind:class="{ 'main-content': isEditable === true }">
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
					<Button
						v-if="doi"
						class="p-button-sm p-button-outlined"
						@click="downloadPDF"
						:icon="'pi pi-cloud-download'"
						:loading="!pdfLink"
						label="Download PDF"
					/>
				</header>
				<Accordion :multiple="true" :active-index="[0, 1, 2, 3, 4, 5, 6, 7]">
					<AccordionTab v-if="!isEmpty(formattedAbstract)" header="Abstract" id="Abstract">
						<div class="constrain-width">
							<span v-html="formattedAbstract" />
						</div>
					</AccordionTab>
					<AccordionTab
						v-if="doc?.knownEntities?.summaries?.sections"
						header="Section summaries"
						id="SectionSummaries"
					>
						<template v-for="(section, index) of doc.knownEntities.summaries.sections" :key="index">
							<h6>{{ index }}</h6>
							<p v-html="highlightSearchTerms(section)" />
						</template>
					</AccordionTab>
					<AccordionTab v-if="!isEmpty(figureArtifacts)" id="Figures">
						<template #header>
							Figures<span class="artifact-amount">({{ figureArtifacts.length }})</span>
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
					<AccordionTab v-if="!isEmpty(tableArtifacts)" id="Tables">
						<template #header>
							Tables<span class="artifact-amount">({{ tableArtifacts.length }})</span>
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
					<AccordionTab v-if="!isEmpty(equationArtifacts)" id="Equations">
						<template #header>
							Equations<span class="artifact-amount">({{ equationArtifacts.length }})</span>
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
					<AccordionTab v-if="!isEmpty(githubUrls)" id="GitHubLinks">
						<template #header>
							GitHub links<span class="artifact-amount">({{ githubUrls.length }})</span>
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
					<AccordionTab v-if="!isEmpty(urlArtifacts)" id="OtherURLs">
						<template #header>
							Other URLs<span class="artifact-amount">({{ urlArtifacts.length }})</span>
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
					<AccordionTab v-if="!isEmpty(otherArtifacts)" id="OtherArtifacts">
						<template #header>
							Other artifacts<span class="artifact-amount">({{ otherArtifacts.length }})</span>
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
					<AccordionTab v-if="!isEmpty(doc.citationList)" id="References">
						<template #header>
							References<span class="artifact-amount">({{ doc.citationList.length }})</span>
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
					<AccordionTab v-if="!isEmpty(relatedTerariumArtifacts)" id="AssociatedResources">
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
			</div>
		</div>
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

const props = defineProps<{
	xddUri: string;
	isEditable: boolean;
	highlight?: string;
	previewLineLimit?: number;
}>();

const sectionElem = ref<HTMLElement | null>(null);
const doc = ref<DocumentType | null>(null);
const pdfLink = ref<string | null>(null);

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

// Better than wrapping download button with an anchor
function downloadPDF() {
	if (pdfLink.value) {
		const link = document.createElement('a');
		link.href = pdfLink.value;
		link.download = `${doi.value}.pdf`;
		link.click();
	}
}

watch(doi, async (currentValue, oldValue) => {
	if (currentValue !== oldValue) {
		fetchDocumentArtifacts();
		fetchRelatedTerariumArtifacts();
		pdfLink.value = '';
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
	display: inline;
}

.content-navigator {
	padding: 1rem;
	margin-top: 8.5rem;
	display: flex;
	flex-direction: column;
	gap: 3rem;
	position: fixed;
	top: 0px;
	height: 100vh;
}

.content-switcher {
	width: 12rem;
}

.p-button.p-button-secondary {
	border: 1px solid var(--surface-border);
	box-shadow: none;
}

.p-button[active='false'].p-button-secondary,
.p-button[active='false'].p-button-secondary:focus,
.p-button[active='false'].p-button-secondary:enabled {
	border-color: var(--surface-border);
	background-color: var(--surface-0);
	color: var(--text-color-subdued);
}

.p-button[active='false'].p-button-secondary:hover {
	border-color: var(--surface-border);
	background-color: var(--surface-hover) !important;
	color: var(--text-color-subdued) !important;
}

.p-button[active='true'].p-button-secondary,
.p-button[active='true'].p-button-secondary:focus,
.p-button[active='true'].p-button-secondary:enabled {
	border-color: var(--surface-border);
	background-color: var(--surface-highlight);
	color: var(--text-color-primary);
}

.p-button[active='true'].p-button-secondary:hover {
	border-color: var(--surface-border);
	background-color: var(--surface-highlight) !important;
	color: var(--text-color-primary) !important;
}

.p-button.p-button-sm {
	padding: 0.5rem 0.75rem;
}

.scroll-to-section-links {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.find-in-page {
	border: 1px solid var(--surface-border-light) !important;
	padding: 0.75rem;
	width: 11.75rem;
}

.main-content {
	height: 100vh;
	margin-left: 15rem;
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
