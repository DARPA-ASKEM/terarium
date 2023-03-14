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
					target="_blank"
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
			<AccordionTab v-if="!isEmpty(urlArtifacts) && !isEmpty(githubUrls)">
				<template #header>
					URLs<span class="artifact-amount">({{ urlArtifacts.length }})</span>
				</template>
				<ul>
					<template v-if="isEditable">
						<li class="github-link" v-for="(url, index) in githubUrls" :key="index">
							<Button
								label="Import"
								class="p-button-sm p-button-outlined"
								icon="pi pi-cloud-download"
								@click="openCodeBrowser(url)"
							/>
							<a :href="url.html_url" target="_blank" rel="noreferrer noopener">{{
								url.html_url
							}}</a>
						</li>
					</template>
					<li v-for="ex in urlArtifacts" :key="ex.url">
						<b>{{ ex.resourceTitle }}</b>
						<div>
							<a :href="ex.url" target="_blank" rel="noreferrer noopener">{{ ex.url }}</a>
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
		<Teleport to="body">
			<modal v-if="isModalVisible" class="modal" @modal-mask-clicked="isModalVisible = false">
				<template #header>
					<h5>Choose file to open from {{ chosenRepositoryName }}</h5>
				</template>
				<template #default>
					<ul class="repository-content">
						<li v-for="(content, index) in filesToSelect" :key="index" @click="openCode(content)">
							<i v-if="content.download_url === null" class="pi pi-folder" />
							<i v-else class="pi pi-file" />
							{{ content.name }}
						</li>
					</ul>
				</template>
			</modal>
		</Teleport>
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
import { XDDExtractionType } from '@/types/XDD';
import { XDDArtifact, DocumentType } from '@/types/Document';
import { getDocumentDoi, isModel, isDataset, isDocument } from '@/utils/data-util';
import { ResultType } from '@/types/common';
import { getRelatedArtifacts } from '@/services/provenance';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import { Model } from '@/types/Model';
import { Dataset } from '@/types/Dataset';
import { ProvenanceType } from '@/types/Provenance';
import { getGithubUrls, getGithubRepositoryContent, getGithubCode } from '@/utils/github-import';
import modal from '@/components/widgets/Modal.vue';
import { ProjectAssetTypes } from '@/types/Project';
import * as textUtil from '@/utils/text';

const props = defineProps<{
	assetId: string;
	isEditable: boolean;
	highlight?: string;
	previewLineLimit?: number;
}>();

const emit = defineEmits(['open-asset']);

const sectionElem = ref<HTMLElement | null>(null);
const doc = ref<DocumentType | null>(null);

const githubUrls = ref();
const isModalVisible = ref(false);
const chosenRepositoryName = ref('');
const filesToSelect = ref();

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}

onMounted(async () => {
	githubUrls.value = await getGithubUrls();
});

async function openCodeBrowser(url) {
	// console.log(url)
	isModalVisible.value = true;
	chosenRepositoryName.value = url.full_name;
	filesToSelect.value = await getGithubRepositoryContent(url.contents_url.slice(0, -8));
}

async function openCode(url) {
	console.log(url);
	if (url.download_url === null) {
		chosenRepositoryName.value = `${chosenRepositoryName.value}/${url.name}`;
		filesToSelect.value = await getGithubRepositoryContent(url.url);
		return;
	}

	emit(
		'open-asset',
		{ assetName: 'New file', assetType: ProjectAssetTypes.CODE },
		// { assetName: url.name, assetId: url.name, assetType: ProjectAssetTypes.CODE },
		await getGithubCode(url.download_url)
	);
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
		const results = await getRelatedArtifacts(props.assetId, ProvenanceType.Document);
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

<style scoped>
.repository-content {
	list-style: none;
	display: flex;
	flex-direction: column;
	gap: 0.25rem;
	margin-top: 1rem;
}

.repository-content li {
	padding: 0.25rem;
	cursor: pointer;
	border-radius: 0.5rem;
}

.repository-content li:hover {
	background-color: var(--surface-hover);
}
</style>
