<!-- This template is a copy of tera-external-publication with some elements stripped out.  TODO: merge the concept of external publication and document asset -->
<template>
	<tera-asset
		:feature-config="featureConfig"
		:name="highlightSearchTerms(doc?.name)"
		:overline="highlightSearchTerms(doc?.source)"
		@close-preview="emit('close-preview')"
		:hide-intro="view === DocumentView.PDF"
		:stretch-content="view === DocumentView.PDF"
		:show-sticky-header="view === DocumentView.PDF"
		:is-loading="documentLoading"
	>
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
							!pdfLink &&
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
			<AccordionTab v-if="!isEmpty(figures)">
				<template #header>
					<header id="Figures">
						Figures<span class="artifact-amount">({{ figures.length }})</span>
					</header>
				</template>
				<ul>
					<li v-for="(ex, index) in figures" :key="index" class="extracted-item">
						<Image id="img" class="extracted-image" :src="ex.metadata?.url" :alt="''" preview />
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
							<Image id="img" :src="ex.metadata?.url" :alt="''" preview />
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
							<Image id="img" :src="ex.metadata?.url" :alt="''" preview />
							<tera-show-more-text
								:text="highlightSearchTerms(ex.metadata?.content ?? '')"
								:lines="previewLineLimit"
							/>
						</div>
						<tera-math-editor v-if="ex.metadata.equation" :latex-equation="ex.metadata.equation" />
					</li>
				</ul>
			</AccordionTab>
		</Accordion>
		<!-- Adding this here for now...we will need a way to listen to the extraction job since this takes some time in the background when uploading a doucment-->
		<p
			class="pl-3"
			v-if="
				isEmpty(doc?.assets) &&
				view === DocumentView.EXTRACTIONS &&
				viewOptions[1]?.value === DocumentView.PDF
			"
		>
			PDF Extractions may still be processsing please refresh in some time...
		</p>
		<tera-pdf-embed
			v-else-if="view === DocumentView.PDF && pdfLink"
			:pdf-link="pdfLink"
			:title="doc?.name || ''"
		/>
		<tera-text-editor v-else-if="view === DocumentView.TXT" :initial-text="docText" />
	</tera-asset>
</template>

<script setup lang="ts">
import { computed, ref, watch, onUpdated } from 'vue';
import { isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { FeatureConfig } from '@/types/common';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import { DocumentAsset, ExtractionAssetType } from '@/types/Types';
import * as textUtil from '@/utils/text';
import TeraAsset from '@/components/asset/tera-asset.vue';
import {
	downloadDocumentAsset,
	getDocumentAsset,
	getDocumentFileAsText
} from '@/services/document-assets';
import Image from 'primevue/image';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import SelectButton from 'primevue/selectbutton';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import TeraTextEditor from './tera-text-editor.vue';

enum DocumentView {
	EXTRACTIONS = 'Extractions',
	PDF = 'PDF',
	TXT = 'Text',
	NOT_FOUND = 'Not found'
}
const props = defineProps<{
	assetId: string;
	highlight?: string;
	previewLineLimit: number;
	featureConfig?: FeatureConfig;
}>();

const doc = ref<DocumentAsset | null>(null);
const pdfLink = ref<string | null>(null);
const view = ref(DocumentView.EXTRACTIONS);

const extractionsOption = { value: DocumentView.EXTRACTIONS, icon: 'pi pi-list' };
const pdfOption = { value: DocumentView.PDF, icon: 'pi pi-file-pdf' };
const txtOption = { value: DocumentView.TXT, icon: 'pi pi-file' };
const notFoundOption = { value: DocumentView.NOT_FOUND, icon: 'pi pi-file', disabled: true };

const viewOptions = computed(() => {
	const options: { value: DocumentView; icon: string; disabled?: boolean }[] = [extractionsOption];
	if (!isEmpty(doc.value?.fileNames)) {
		if (doc.value?.fileNames?.at(0)?.endsWith('.pdf')) {
			options.push(pdfOption);
		} else {
			options.push(txtOption);
		}
	} else {
		options.push(notFoundOption);
	}
	return options;
});

const docText = ref<string>('');

const documentLoading = ref(false);

const docLink = computed(() =>
	doc.value?.fileNames && doc.value.fileNames.length > 0 ? doc.value.fileNames[0] : null
);

const figures = computed(
	() => doc.value?.assets?.filter((asset) => asset.assetType === ExtractionAssetType.Figure) || []
);
const tables = computed(
	() => doc.value?.assets?.filter((asset) => asset.assetType === ExtractionAssetType.Table) || []
);
const equations = computed(
	() => doc.value?.assets?.filter((asset) => asset.assetType === ExtractionAssetType.Equation) || []
);

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
	docText.value = res;
}

watch(
	() => props.assetId,
	async () => {
		if (props.assetId) {
			documentLoading.value = true;
			const document = await getDocumentAsset(props.assetId);

			if (!document) {
				return;
			}
			doc.value = document;
			if (doc.value?.fileNames?.at(0)?.endsWith('.pdf')) {
				if (view.value === DocumentView.TXT) {
					view.value = DocumentView.PDF;
				}
			} else {
				await openTextDocument();
				if (view.value === DocumentView.PDF) {
					view.value = DocumentView.TXT;
				}
			}
			documentLoading.value = false;
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
