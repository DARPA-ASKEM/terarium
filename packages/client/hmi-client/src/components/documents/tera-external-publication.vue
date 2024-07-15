<template>
	<tera-asset
		v-if="doc"
		:id="doc.gddId"
		:feature-config="featureConfig"
		:name="highlightSearchTerms(doc.title)"
		:overline="highlightSearchTerms(doc.journal)"
		:authors="formatDocumentAuthors(doc)"
		:doi="highlightSearchTerms(doi)"
		:publisher="highlightSearchTerms(doc.publisher)"
		@close-preview="emit('close-preview')"
		:hide-intro="view === DocumentView.PDF"
	>
		<template #bottom-header-buttons>
			<Button
				v-if="featureConfig.isPreview"
				size="small"
				severity="secondary"
				outlined
				icon="pi pi-external-link"
				label="Open PDF"
				@click="openPDF"
				:loading="pdfIsLoading"
				:disabled="!pdfLink && !linkIsPDF()"
			/>
		</template>
		<template #edit-buttons>
			<SelectButton
				:model-value="view"
				@change="if ($event.value) view = $event.value;"
				:options="viewOptions"
				option-value="value"
				option-disabled="disabled"
			>
				<template #option="{ option }">
					<i
						:class="`${
							pdfIsLoading &&
							option.value !== DocumentView.EXTRACTIONS &&
							option.value !== DocumentView.NOT_FOUND
								? 'pi pi-spin pi-spinner'
								: option.icon
						} p-button-icon-left`"
					/>
					<span class="p-button-label">{{ option.value }}</span>
				</template>
			</SelectButton>
		</template>
		<Accordion
			v-if="view === DocumentView.EXTRACTIONS"
			:multiple="true"
			:active-index="[0, 1, 2, 3, 4, 5, 6, 7]"
		>
			<AccordionTab v-if="!isEmpty(formattedAbstract)">
				<template #header>
					<header id="Abstract">Abstract</header>
				</template>
				<p v-html="formattedAbstract" />
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(sectionSummaries)">
				<template #header>
					<header id="Section-Summaries">Section summaries</header>
				</template>
				<ul>
					<template v-for="section in sectionSummaries">
						<li v-for="(entries, index) in Object.entries(section)" :key="index">
							<h6>{{ entries[0] }}</h6>
							<p v-html="highlightSearchTerms(entries[1] as string)" />
						</li>
					</template>
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
						<Button
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
		<tera-pdf-embed
			v-else-if="view === DocumentView.PDF && pdfLink"
			:pdf-link="pdfLink"
			:title="doc.title"
		/>
	</tera-asset>
</template>

<script setup lang="ts">
import { CodeRequest, FeatureConfig, ResultType } from '@/types/common';
import { getDocumentDoi, isDataset, isDocument, isModel } from '@/utils/data-util';
import { isEmpty, isEqual, uniqWith } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import { PropType, computed, onUpdated, ref, watch } from 'vue';
// import { getRelatedArtifacts } from '@/services/provenance';
import TeraImportGithubFile from '@/components/widgets/tera-import-github-file.vue';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import type { Dataset, Document, Model } from '@/types/Types';
// import { ProvenanceType } from '@/types/Types';
import TeraAsset from '@/components/asset/tera-asset.vue';
import { generatePdfDownloadLink } from '@/services/generate-download-link';
import * as textUtil from '@/utils/text';
import Image from 'primevue/image';
import SelectButton from 'primevue/selectbutton';

enum DocumentView {
	EXTRACTIONS = 'Extractions',
	PDF = 'PDF',
	TXT = 'Text',
	NOT_FOUND = 'Not found'
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
const pdfIsLoading = ref(false);
const isImportGithubFileModalVisible = ref(false);
const openedUrl = ref('');
const view = ref(DocumentView.EXTRACTIONS);

const extractionsOption = { value: DocumentView.EXTRACTIONS, icon: 'pi pi-list' };
const pdfOption = { value: DocumentView.PDF, icon: 'pi pi-file-pdf' };
const notFoundOption = { value: DocumentView.NOT_FOUND, icon: 'pi pi-file', disabled: true };
const viewOptions = ref([extractionsOption, pdfOption]);

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

const formatDocumentAuthors = (d: Document) =>
	highlightSearchTerms(d.author.map((a) => a.name).join(', '));

const docLink = computed(() =>
	doc?.value && !isEmpty(doc?.value?.link) ? doc.value.link[0].url : null
);

const formattedAbstract = computed(() => {
	if (!doc.value || !doc.value.abstractText) return '';
	return highlightSearchTerms(doc.value.abstractText);
});

const doi = computed(() => getDocumentDoi(doc.value));

/* Artifacts */
const associatedResources = ref<ResultType[]>([]);

const otherUrls = computed(() =>
	doc.value?.knownEntities && doc.value.knownEntities.urlExtractions?.length > 0
		? uniqWith(doc.value.knownEntities.urlExtractions, isEqual) // removes duplicate urls
		: []
);
const sectionSummaries = computed(
	() => doc.value?.knownEntities?.summaries.map(({ sections }) => sections) ?? []
);
const githubUrls = computed(() => doc.value?.githubUrls ?? []);

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
	const link = (docLink.value ?? doi.value).toLowerCase().endsWith('.pdf');
	return link;
}

const openPDF = () => {
	if (linkIsPDF()) {
		if (docLink.value) window.open(docLink.value);
		else if (doi.value) window.open(`https://doi.org/${doi.value}`);
	} else if (pdfLink.value) {
		const pdfWindow = window.open(pdfLink.value);
		if (pdfWindow) pdfWindow.document.title = doi.value;
	}
};

watch(
	doi,
	async (currentValue, oldValue) => {
		if (currentValue !== oldValue) {
			viewOptions.value = [extractionsOption, pdfOption];
			view.value = DocumentView.EXTRACTIONS;
			pdfLink.value = null;
			pdfIsLoading.value = true;
			pdfLink.value = await generatePdfDownloadLink(doi.value); // Generate PDF download link on (doi change)
			if (!pdfLink.value && !linkIsPDF()) {
				// TODO: the document cannot be added to a project
				viewOptions.value[1] = notFoundOption;
			}
			pdfIsLoading.value = false;
		}
	},
	{ immediate: true }
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

function openImportGithubFileModal(url: string) {
	openedUrl.value = url;
	isImportGithubFileModalVisible.value = true;
}

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
