<template>
	<tera-asset
		v-if="doc"
		:feature-config="featureConfig"
		:name="highlightSearchTerms(doc.title)"
		:overline="highlightSearchTerms(doc.journal)"
		:authors="formatDocumentAuthors(doc)"
		:doi="highlightSearchTerms(doi)"
		:publisher="highlightSearchTerms(doc.publisher)"
		@close-preview="emit('close-preview')"
		:hide-intro="view === DocumentView.PDF"
		:stretch-content="view === DocumentView.PDF"
		:show-sticky-header="view === DocumentView.PDF"
	>
		<template #bottom-header-buttons>
			<Button
				v-if="featureConfig.isPreview"
				class="p-button-sm p-button-outlined"
				icon="pi pi-external-link"
				label="Open PDF"
				@click="openPDF"
				:loading="!pdfLink && !linkIsPDF()"
			/>
		</template>
		<template #edit-buttons>
			<SelectButton
				:model-value="view"
				@change="if ($event.value) view = $event.value;"
				:options="viewOptions"
				option-value="value"
			>
				<template #option="{ option }">
					<i
						:class="`${
							!pdfLink && option.value !== DocumentView.EXRACTIONS
								? 'pi pi-spin pi-spinner'
								: option.icon
						} p-button-icon-left`"
					/>
					<span class="p-button-label">{{ option.value }}</span>
				</template>
			</SelectButton>
		</template>
		<template #info-bar>
			<div class="container">
				<Message class="inline-message" icon="none"
					>This page contains extractions from the document. Use the content switcher above to see
					the original PDF if it is available.</Message
				>
			</div>
		</template>
		<Accordion
			v-if="view === DocumentView.EXRACTIONS"
			:multiple="true"
			:active-index="[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]"
		>
			<AccordionTab>
				<template #header>
					<header id="Abstract">Abstract</header>
				</template>
				<p v-html="formattedAbstract" />
			</AccordionTab>
			<AccordionTab>
				<template #header>
					<header id="Section-Summaries">Section Summaries</header>
				</template>
				<ul v-if="doc?.knownEntities?.summaries">
					<li v-for="(section, index) of doc.knownEntities.summaries" :key="index">
						<h6>{{ index }}</h6>
						<p v-html="highlightSearchTerms(section[index])" />
					</li>
				</ul>
			</AccordionTab>
			<AccordionTab>
				<template #header>
					<header id="Figures">
						Figures<span class="artifact-amount">({{ figures.length }})</span>
					</header>
				</template>
				<ul v-if="!isEmpty(figures)">
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
			<AccordionTab>
				<template #header>
					<header id="Tables">
						Tables<span class="artifact-amount">({{ tables.length }})</span>
					</header>
				</template>
				<ul v-if="!isEmpty(tables)">
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
			<AccordionTab>
				<template #header>
					<header id="Equations">
						Equations<span class="artifact-amount">({{ equations.length }})</span>
					</header>
				</template>
				<ul v-if="!isEmpty(equations)">
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
			<AccordionTab>
				<template #header>
					<header id="Github-URLs">
						GitHub URLs<span class="artifact-amount">({{ githubUrls.length }})</span>
					</header>
				</template>
				<ul v-if="!isEmpty(githubUrls)">
					<li class="extracted-item" v-for="(url, index) in githubUrls" :key="index">
						<Button
							v-if="!featureConfig.isPreview"
							label="Import"
							class="p-button-sm p-button-outlined"
							icon="pi pi-cloud-download"
							@click="openImportGithubFileModal(url)"
						/>
						<a :href="url" rel="noreferrer noopener">{{ url }}</a>
					</li>
				</ul>
				<tera-import-github-file
					:visible="isImportGithubFileModalVisible"
					:url-string="openedUrl"
					@close="isImportGithubFileModalVisible = false"
					@open-code="openCode"
				/>
			</AccordionTab>
			<AccordionTab>
				<template #header>
					<header id="Other-URLs">
						Other URLs<span class="artifact-amount">({{ otherUrls.length }})</span>
					</header>
				</template>
				<ul v-if="!isEmpty(otherUrls)">
					<li v-for="ex in otherUrls" :key="ex.url" class="extracted-item">
						<b>{{ ex.resourceTitle }}</b>
						<div>
							<a :href="ex.url" rel="noreferrer noopener">{{ ex.url }}</a>
						</div>
					</li>
				</ul>
			</AccordionTab>
			<AccordionTab>
				<template #header>
					<header id="Other-Extractions">
						Other extractions<span class="artifact-amount">({{ otherExtractions.length }})</span>
					</header>
				</template>
				<ul v-if="!isEmpty(otherExtractions)">
					<li v-for="ex in otherExtractions" :key="ex.askemId" class="extracted-item">
						<b v-html="highlightSearchTerms(ex.properties.title)" />
						<span v-html="highlightSearchTerms(ex.properties.caption)" />
						<span v-html="highlightSearchTerms(ex.properties.abstractText)" />
						<span v-html="highlightSearchTerms(ex.properties.contentText)" />
					</li>
				</ul>
			</AccordionTab>
			<AccordionTab>
				<template #header>
					<header id="References">
						References<span class="artifact-amount">({{ doc.citationList.length }})</span>
					</header>
				</template>
				<ul v-if="!isEmpty(doc.citationList)">
					<li v-for="(citation, key) of doc.citationList" :key="key">
						<template v-if="!isEmpty(formatCitation(citation))">
							{{ key + 1 }}. <span v-html="formatCitation(citation)"></span>
						</template>
					</li>
				</ul>
			</AccordionTab>
			<AccordionTab>
				<template #header>
					<header id="Associated-Resources">
						Associated resources
						<span class="artifact-amount">({{ associatedResources.length }})</span>
					</header>
				</template>
				<template v-if="!isEmpty(associatedResources)">
					<DataTable :value="relatedTerariumModels">
						<Column field="name" header="Models"></Column>
					</DataTable>
					<DataTable :value="relatedTerariumDatasets">
						<Column field="name" header="Datasets"></Column>
					</DataTable>
					<DataTable :value="relatedTerariumDocuments">
						<Column field="name" header="Documents"></Column>
					</DataTable>
				</template>
			</AccordionTab>
		</Accordion>
		<tera-pdf-embed
			v-else-if="view === DocumentView.PDF && pdfLink"
			:pdf-link="pdfLink"
			:title="doc.title"
		/>
	</tera-asset>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch, onUpdated, PropType } from 'vue';
import { isEmpty, isEqual, uniqWith } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';
import Message from 'primevue/message';
import { getDocumentById, getXDDArtifacts } from '@/services/data';
import { XDDExtractionType } from '@/types/XDD';
import { getDocumentDoi, isModel, isDataset, isDocument } from '@/utils/data-util';
import { ResultType, FeatureConfig, CodeRequest } from '@/types/common';
import { getRelatedArtifacts } from '@/services/provenance';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import TeraImportGithubFile from '@/components/widgets/tera-import-github-file.vue';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import { Model, Extraction, ProvenanceType, Document, Dataset } from '@/types/Types';
import * as textUtil from '@/utils/text';
import Image from 'primevue/image';
import { generatePdfDownloadLink } from '@/services/generate-download-link';
import TeraAsset from '@/components/asset/tera-asset.vue';
import SelectButton from 'primevue/selectbutton';

enum DocumentView {
	EXRACTIONS = 'Extractions',
	PDF = 'PDF'
}

const props = defineProps({
	xddUri: {
		type: String,
		required: true
	},
	highlight: {
		type: String,
		default: null
	},
	previewLineLimit: {
		type: Number,
		default: null
	},
	featureConfig: {
		type: Object as PropType<FeatureConfig>,
		default: { isPreview: false } as FeatureConfig
	}
});

const doc = ref<Document | null>(null);
const pdfLink = ref<string | null>(null);
const isImportGithubFileModalVisible = ref(false);
const openedUrl = ref('');
const view = ref(DocumentView.EXRACTIONS);
const viewOptions = ref([
	{ value: DocumentView.EXRACTIONS, icon: 'pi pi-list' },
	{ value: DocumentView.PDF, icon: 'pi pi-file-pdf' }
]);

const emit = defineEmits(['open-code', 'close-preview', 'asset-loaded']);

function openCode(codeRequests: CodeRequest[]) {
	emit('open-code', codeRequests);
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

const formatDocumentAuthors = (d: Document) =>
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
const artifacts = ref<Extraction[]>([]);
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
	doc.value?.knownEntities && doc.value.knownEntities.urlExtractions?.length > 0
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
	() => associatedResources.value.filter((d) => isDocument(d)) as Document[]
);
/*
// This is the model content that is displayed in the scroll-to-section featuer
// That feature was removed, but way may want to bring it back.
// I suggest we keep this unil we decide to remove it for good.
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
*/

// This fetches various parts of the document: figures, tables, equations ... etc
const fetchDocumentArtifacts = async () => {
	if (doi.value !== '') {
		const allArtifacts = await getXDDArtifacts(doi.value);
		// filter out Document extraction type
		artifacts.value = allArtifacts.filter((art) => art.askemClass !== XDDExtractionType.Doc);
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

/*
// Jamie: The 'Download PDF' button was removed from the UI but I left the code here in case we want to add it back in the future.
function downloadPDF() {
	if (pdfLink.value) {
		const link = document.createElement('a');
		link.href = pdfLink.value;
		link.download = `${doi.value}.pdf`;
		link.click();
	}
}
*/
function linkIsPDF() {
	const link = docLink.value ?? doi.value;
	return link.toLowerCase().endsWith('.pdf');
}

const openPDF = () => {
	if (linkIsPDF()) {
		if (docLink.value) window.open(docLink.value as string);
		else if (doi.value) window.open(`https://doi.org/${doi.value}`);
		return;
	}
	if (pdfLink.value) {
		const pdfWindow = window.open(pdfLink.value);
		if (pdfWindow) pdfWindow.document.title = doi.value;
	}
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

function openImportGithubFileModal(url: string) {
	openedUrl.value = url;
	isImportGithubFileModalVisible.value = true;
}

onMounted(async () => {
	fetchDocumentArtifacts();
	fetchAssociatedResources();
});

onUpdated(() => {
	if (doc.value) {
		emit('asset-loaded');
	}
});
</script>
<style scoped>
.container {
	margin-left: 1rem;
	margin-right: 1rem;
	max-width: 70rem;
}

.inline-message:deep(.p-message-wrapper) {
	padding-top: 0.5rem;
	padding-bottom: 0.5rem;
	background-color: var(--surface-highlight);
	color: var(--text-color-primary);
	border-radius: var(--border-radius);
	border: 4px solid var(--primary-color);
	border-width: 0px 0px 0px 6px;
}

.extracted-item {
	border: 1px solid var(--surface-border-light);
	padding: 1rem;
	border-radius: var(--border-radius);
	gap: 1rem;
	display: flex;
	align-items: center;
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
