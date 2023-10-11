<!-- This template is a copy of tera-external-publication with some elements stripped out.  TODO: merge the concept of external publication and document asset -->
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
					v-if="documentType === DocumentView.PDF"
					class="p-button-secondary p-button-sm"
					label="PDF"
					icon="pi pi-file"
					:loading="!pdfLink"
					@click="documentView = DocumentView.PDF"
					:active="documentView === DocumentView.PDF"
				/>
				<Button
					v-if="documentType === DocumentView.TXT"
					class="p-button-secondary p-button-sm"
					label="txt"
					icon="pi pi-file"
					:loading="!pdfLink"
					@click="documentView = DocumentView.TXT"
					:active="documentView === DocumentView.TXT"
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
			<AccordionTab v-if="!isEmpty(figures)">
				<template #header>
					<header id="Figures">
						Figures<span class="artifact-amount">({{ figures.length }})</span>
					</header>
				</template>
				<ul>
					<li v-for="(ex, index) in figures" :key="index" class="extracted-item">
						<Image id="img" class="extracted-image" :src="ex.metadata?.img_pth" :alt="''" preview />
						<tera-show-more-text
							:text="highlightSearchTerms(ex.metadata?.content ?? '')"
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
					<li v-for="(ex, index) in tables" :key="index" class="extracted-item">
						<div class="extracted-image">
							<Image id="img" :src="ex.metadata?.img_pth" :alt="''" preview />
							<tera-show-more-text
								:text="highlightSearchTerms(ex.metadata?.content ?? '')"
								:lines="previewLineLimit"
							/>
						</div>
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
					<li v-for="(ex, index) in equations" :key="index" class="extracted-item">
						<div class="extracted-image">
							<Image id="img" :src="ex.metadata?.img_pth" :alt="''" preview />
							<tera-show-more-text
								:text="highlightSearchTerms(ex.metadata?.content ?? '')"
								:lines="previewLineLimit"
							/>
						</div>
					</li>
				</ul>
			</AccordionTab>
		</Accordion>
		<tera-pdf-embed
			v-else-if="documentView === DocumentView.PDF && pdfLink"
			:pdf-link="pdfLink"
			:title="doc.name || ''"
		/>
		<code-editor v-else-if="documentView === DocumentView.TXT" :initial-code="code" />
	</tera-asset>
</template>

<script setup lang="ts">
import { computed, ref, watch, onUpdated } from 'vue';
import { isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import Message from 'primevue/message';
import { FeatureConfig } from '@/types/common';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import { DocumentAsset } from '@/types/Types';
import * as textUtil from '@/utils/text';
import TeraAsset from '@/components/asset/tera-asset.vue';
import {
	downloadDocumentAsset,
	getDocumentAsset,
	getDocumentFileAsText
} from '@/services/document-assets';
import Image from 'primevue/image';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import CodeEditor from '@/page/project/components/code-editor.vue';

enum DocumentView {
	EXRACTIONS = 'extractions',
	PDF = 'pdf',
	TXT = 'txt'
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
const code = ref<string>();

const docLink = computed(() =>
	doc.value?.fileNames && doc.value.fileNames.length > 0 ? doc.value.fileNames[0] : null
);

const figures = computed(
	() => doc.value?.assets?.filter((asset) => asset.assetType === 'figure') || []
);
const tables = computed(
	() => doc.value?.assets?.filter((asset) => asset.assetType === 'table') || []
);
const equations = computed(
	() => doc.value?.assets?.filter((asset) => asset.assetType === 'equation') || []
);

const documentType = computed(() => {
	if (doc.value?.fileNames?.at(0)?.endsWith('.pdf')) {
		return DocumentView.PDF;
	}
	return DocumentView.TXT;
});

const emit = defineEmits(['close-preview', 'asset-loaded']);

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}

async function openTextDocument() {
	const filename: string | undefined = doc.value?.fileNames?.at(0);
	const res: string | null = await getDocumentFileAsText(props.assetId!, filename!);
	if (!res) return;
	code.value = res;
}

watch(
	() => props.assetId,
	async () => {
		if (props.assetId) {
			const document = await getDocumentAsset(props.assetId);
			if (document) {
				doc.value = document;
				openTextDocument();
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

watch(docLink, async (currentValue, oldValue) => {
	if (currentValue !== oldValue) {
		// fetchDocumentArtifacts();
		// fetchAssociatedResources();
		pdfLink.value = null;
		pdfLink.value = await downloadDocumentAsset(props.assetId, docLink.value!); // Generate PDF download link on (doi change)
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
