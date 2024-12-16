<template>
	<tera-asset :id="assetId" :name="document?.name ?? ''" :is-loading="documentLoading">
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
import { ref, watch } from 'vue';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraTextEditor from '@/components/documents/tera-text-editor.vue';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import { downloadDocumentAsset, getDocumentAsset, getDocumentFileAsText } from '@/services/document-assets';
import { type DocumentAsset } from '@/types/Types';

enum DocumentView {
	PDF = 'PDF',
	TXT = 'Text'
}

const props = defineProps<{
	assetId: string;
}>();

const document = ref<DocumentAsset | null>(null);
const documentLoading = ref(false);
const docText = ref<string | null>(null);
const pdfLink = ref<string | null>(null);
const view = ref(DocumentView.PDF);

watch(
	() => props.assetId,
	async (assetId, oldAssetId) => {
		if (assetId === oldAssetId) return;
		if (assetId) {
			view.value = DocumentView.PDF;
			pdfLink.value = null;
			documentLoading.value = true;
			document.value = await getDocumentAsset(assetId);
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
</script>
