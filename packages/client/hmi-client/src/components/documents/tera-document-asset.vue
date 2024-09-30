<template>
	<tera-asset
		:id="assetId"
		:feature-config="featureConfig"
		:name="document?.name ?? ''"
		:overline="document?.source ?? ''"
		@close-preview="emit('close-preview')"
		:is-loading="documentLoading"
	>
		<p class="pl-3" v-if="documentLoading">PDF Loading...</p>
		<tera-pdf-embed
			v-else-if="view === DocumentView.PDF && pdfLink"
			:pdf-link="pdfLink"
			:title="document?.name || ''"
		/>
		<tera-text-editor v-else-if="view === DocumentView.TXT" :initial-text="docText ?? ''" />
	</tera-asset>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, onUpdated, ref, watch } from 'vue';
import type { FeatureConfig, ExtractionStatusUpdate } from '@/types/common';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import TeraAsset from '@/components/asset/tera-asset.vue';
import type { ClientEvent, DocumentAsset } from '@/types/Types';
import { ClientEventType, ProgressState } from '@/types/Types';
import { downloadDocumentAsset, getDocumentAsset, getDocumentFileAsText } from '@/services/document-assets';
import { subscribe, unsubscribe } from '@/services/ClientEventService';
import TeraTextEditor from './tera-text-editor.vue';

enum DocumentView {
	EXTRACTIONS = 'Extractions',
	PDF = 'PDF',
	TXT = 'Text',
	NOT_FOUND = 'Not found'
}
const props = defineProps<{
	assetId: string;
	previewLineLimit: number;
	featureConfig?: FeatureConfig;
}>();

const emit = defineEmits(['close-preview', 'asset-loaded', 'remove']);

const document = ref<DocumentAsset | null>(null);
const pdfLink = ref<string | null>(null);
const view = ref(DocumentView.PDF);

const docText = ref<string | null>(null);

const documentLoading = ref(false);

/* TODO: When fetching a document by id, its id and fileNames don't get returned.
 Once they do see about adjusting the conditionals */
watch(
	() => props.assetId,
	async () => {
		if (props.assetId) {
			view.value = DocumentView.PDF;
			pdfLink.value = null;
			documentLoading.value = true;
			document.value = await getDocumentAsset(props.assetId);
			const filename = document.value?.fileNames?.[0];

			if (filename?.endsWith('.pdf')) {
				// Generate PDF download link on assetId change
				downloadDocumentAsset(props.assetId, filename).then((pdfLinkResponse) => {
					if (pdfLinkResponse) {
						pdfLink.value = pdfLinkResponse;
					}
				});
			} else if (filename && document.value?.id) {
				if (document.value?.text) {
					docText.value = document.value.text;
				} else {
					getDocumentFileAsText(document.value.id, filename).then((text) => {
						docText.value = text;
					});
				}
			} else {
				docText.value = document.value?.text ?? null;
			}

			documentLoading.value = false;
		} else {
			document.value = null;
		}
	},
	{ immediate: true }
);

onUpdated(() => {
	if (document.value) {
		emit('asset-loaded');
	}
});

onMounted(async () => {
	await subscribe(ClientEventType.ExtractionPdf, subscribeToExtraction);
});

async function subscribeToExtraction(event: ClientEvent<ExtractionStatusUpdate>) {
	if (!event.data || event.data.data.documentId !== props.assetId) return;
	const status = event.data.state;
	// FIXME: adding the 'dispatching' check since there seems to be an issue with the status of the extractions.
	if (status === ProgressState.Complete || event.data.message.includes('Dispatching')) {
		document.value = await getDocumentAsset(props.assetId);
	}
}

onUnmounted(async () => {
	await unsubscribe(ClientEventType.ExtractionPdf, subscribeToExtraction);
});
</script>

<style scoped>
.extracted-item {
	display: flex;
	flex-direction: row;
	gap: var(--gap);
}

.extracted-item > .extracted-image {
	display: block;
	padding: 8px;
	border: 1px solid var(--gray-200);
	border-radius: var(--border-radius);
	object-fit: contain;
}

.no-extracted-text {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
	font-style: italic;
	padding: var(--gap-small);
}
</style>
