<template>
	<tera-asset
		v-if="doc"
		:feature-config="featureConfig"
		:name="highlightSearchTerms(doc.name)"
		:overline="highlightSearchTerms(doc.source)"
		@close-preview="emit('close-preview')"
		:hide-intro="documentView === DocumentView.PDF"
		:stretch-content="documentView === DocumentView.PDF"
		:show-sticky-header="documentView === DocumentView.PDF"
	>
		<template #edit-buttons>
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
					:loading="!docLink"
					@click="documentView = DocumentView.PDF"
					:active="documentView === DocumentView.PDF"
				/>
			</span>
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
			v-else-if="documentView === DocumentView.PDF && pdfLink"
			:pdf-link="pdfLink"
			:title="doc.name"
		/>
	</tera-asset>
</template>

<script setup lang="ts">
import { computed, ref, watch, onUpdated } from 'vue';
import { isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';
import Message from 'primevue/message';
import { isModel, isDataset, isDocument } from '@/utils/data-util';
import { ResultType, FeatureConfig } from '@/types/common';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import { Model, Document, Dataset, DocumentAsset } from '@/types/Types';
import * as textUtil from '@/utils/text';
import TeraAsset from '@/components/asset/tera-asset.vue';
import { getDocumentAsset } from '@/services/document-assets';
import { generatePdfDownloadLink } from '@/services/generate-download-link';

enum DocumentView {
	EXRACTIONS = 'extractions',
	PDF = 'pdf'
}

const props = defineProps<{
	assetId: string;
	highlight?: string;
	previewLineLimit: number;
	featureConfig?: FeatureConfig;
}>();

const doc = ref<DocumentAsset | null>(null);
const pdfLink = ref<string | null>(null);
const documentView = ref(DocumentView.EXRACTIONS);

const docLink = computed(() =>
	doc.value?.document_url && doc.value.document_url.length > 0 ? doc.value.document_url : null
);

const emit = defineEmits(['open-code', 'close-preview', 'asset-loaded']);

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}

watch(
	() => props.assetId,
	async () => {
		if (props.assetId) {
			const document = await getDocumentAsset(props.assetId);
			if (document) {
				doc.value = document;
			}
		} else {
			doc.value = null;
		}
	},
	{
		immediate: true
	}
);

const formattedAbstract = computed(() => {
	if (!doc.value || !doc.value.description) return '';
	return highlightSearchTerms(doc.value.description);
});

const associatedResources = ref<ResultType[]>([]);

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

watch(docLink, async (currentValue, oldValue) => {
	if (currentValue !== oldValue) {
		// fetchDocumentArtifacts();
		// fetchAssociatedResources();
		pdfLink.value = null;
		pdfLink.value = await generatePdfDownloadLink(docLink.value!); // Generate PDF download link on (doi change)
	}
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
