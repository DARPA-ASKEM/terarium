<!-- This template is a copy of tera-external-publication with some elements stripped out.  TODO: merge the concept of external publication and document asset -->
<template>
	<tera-asset
		:feature-config="featureConfig"
		:name="highlightSearchTerms(document?.name)"
		:overline="highlightSearchTerms(document?.source)"
		@close-preview="emit('close-preview')"
		:is-loading="isFetchingPDF"
		overflow-hidden
	>
		<div class="document-asset-container">
			<section>
				<tera-pdf-embed v-if="pdfLink" :pdf-link="pdfLink" :title="document?.name || ''" />
				<tera-text-editor v-else-if="docText" :initial-text="docText" />
			</section>
			<section class="extractions-section">
				<tera-extractions :document="document" :state="clonedState" @update="onUpdateAsset" />
			</section>
		</div>
	</tera-asset>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import type { DocumentAsset, DocumentExtraction } from '@/types/Types';
import { ExtractionAssetType } from '@/types/Types';
import {
	downloadDocumentAsset,
	getDocumentAsset,
	getDocumentFileAsText
} from '@/services/document-assets';
import { AssetBlock } from '@/types/workflow';
import { DocumentOperationState } from '@/workflow/ops/document/document-operation';
import { FeatureConfig } from '@/types/common';
import * as textUtil from '@/utils/text';
import TeraAsset from '../asset/tera-asset.vue';
import TeraTextEditor from './tera-text-editor.vue';
import TeraExtractions from './tera-extractions.vue';

const props = defineProps<{
	assetId: string;
	highlight?: string;
	featureConfig?: FeatureConfig;
}>();

const document = ref<DocumentAsset | null>();
const pdfLink = ref<string | null>();
const docText = ref<string | null>();
const isFetchingPDF = ref(false);

const clonedState = ref<DocumentOperationState>({
	equations: [],
	figures: [],
	tables: [],
	documentId: null
});

const emit = defineEmits(['close-preview', 'asset-loaded']);

function onUpdateAsset(state: DocumentOperationState) {
	clonedState.value = state;
}

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
			isFetchingPDF.value = true;
			document.value = await getDocumentAsset(props.assetId);
			const filename = document.value?.fileNames?.[0];
			const isPdf = document.value?.fileNames?.[0]?.endsWith('.pdf');
			if (document.value?.id && filename)
				if (isPdf) {
					pdfLink.value = await downloadDocumentAsset(document.value.id, filename);
				} else {
					docText.value = await getDocumentFileAsText(document.value.id, filename);
				}

			const figures: AssetBlock<DocumentExtraction>[] =
				document.value?.assets
					?.filter((asset) => asset.assetType === ExtractionAssetType.Figure)
					.map((asset, i) => ({
						name: `Figure ${i + 1}`,
						includeInProcess: false,
						asset
					})) || [];
			const tables: AssetBlock<DocumentExtraction>[] =
				document.value?.assets
					?.filter((asset) => asset.assetType === ExtractionAssetType.Table)
					.map((asset, i) => ({
						name: `Table ${i + 1}`,
						includeInProcess: false,
						asset
					})) || [];
			const equations: AssetBlock<DocumentExtraction>[] =
				document.value?.assets
					?.filter((asset) => asset.assetType === ExtractionAssetType.Equation)
					.map((asset, i) => ({
						name: `Equation ${i + 1}`,
						includeInProcess: true,
						asset
					})) || [];

			clonedState.value.equations = equations;
			clonedState.value.figures = figures;
			clonedState.value.tables = tables;
			clonedState.value.documentId = props.assetId;
			isFetchingPDF.value = false;
		} else {
			document.value = null;
		}
	},
	{
		immediate: true
	}
);
</script>
<style scoped>
.document-asset-container {
	display: grid;
	grid-auto-flow: column;
	grid-template-columns: repeat(auto-fit, minmax(0, 1fr));
	overflow: hidden;
	height: 100%;

	> section {
		overflow-y: auto;
	}
}

.extractions-section {
	padding: 1rem;
}
</style>
