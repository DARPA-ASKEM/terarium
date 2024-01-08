<!-- This template is a copy of tera-external-publication with some elements stripped out.  TODO: merge the concept of external publication and document asset -->
<template>
	<tera-asset
		:feature-config="featureConfig"
		:name="document?.name"
		:overline="document?.source"
		@close-preview="emit('close-preview')"
		:is-loading="isFetchingPDF"
		overflow-hidden
	>
		<div class="document-asset-container">
			<tera-drilldown-section>
				<tera-pdf-embed v-if="pdfLink" :pdf-link="pdfLink" :title="document?.name || ''" />
				<tera-text-editor v-else-if="docText" :initial-text="docText" />
			</tera-drilldown-section>
			<tera-drilldown-section class="extractions-section">
				<Accordion multiple :active-index="[0, 1, 2]">
					<AccordionTab v-if="!isEmpty(clonedState.equations)">
						<template #header>
							<header>Equation Images</header>
						</template>
						<tera-asset-block
							v-for="(equation, i) in clonedState.equations"
							:key="i"
							:is-included="equation.includeInProcess"
						>
							<template #header>
								<h5>{{ equation.name }}</h5>
							</template>
							<Image id="img" :src="equation.asset.metadata?.url" :alt="''" preview />
						</tera-asset-block>
					</AccordionTab>
					<AccordionTab v-if="!isEmpty(clonedState.figures)">
						<template #header>
							<header>Figure Images</header>
						</template>
						<tera-asset-block
							v-for="(figure, i) in clonedState.figures"
							:key="i"
							:is-included="figure.includeInProcess"
						>
							<template #header>
								<h5>{{ figure.name }}</h5>
							</template>
							<Image id="img" :src="figure.asset.metadata?.url" :alt="''" preview />
						</tera-asset-block>
					</AccordionTab>
					<AccordionTab v-if="!isEmpty(clonedState.tables)">
						<template #header>
							<header>Table Images</header>
						</template>
						<tera-asset-block
							v-for="(table, i) in clonedState.tables"
							:key="i"
							:is-included="table.includeInProcess"
						>
							<template #header>
								<h5>{{ table.name }}</h5>
							</template>
							<Image id="img" :src="table.asset.metadata?.url" :alt="''" preview />
						</tera-asset-block>
					</AccordionTab>
					<AccordionTab v-if="!isEmpty(document?.text)">
						<template #header>
							<header>Text</header>
						</template>
						<p>{{ document?.text }}</p>
					</AccordionTab>
				</Accordion>
			</tera-drilldown-section>
		</div>
	</tera-asset>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import { DocumentAsset, DocumentExtraction, ExtractionAssetType } from '@/types/Types';
import {
	downloadDocumentAsset,
	getDocumentAsset,
	getDocumentFileAsText
} from '@/services/document-assets';
import Image from 'primevue/image';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { AssetBlock } from '@/types/workflow';
import { DocumentOperationState } from '@/workflow/ops/document/document-operation';
import { FeatureConfig } from '@/types/common';
import TeraAssetBlock from '../widgets/tera-asset-block.vue';
import TeraAsset from '../asset/tera-asset.vue';
import TeraTextEditor from './tera-text-editor.vue';

const props = defineProps<{
	assetId: string;
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
}

.extractions-section {
	padding: 1rem;
}

:deep(.p-accordion-content > :not(:last-child)) {
	margin-bottom: 0.5rem;
}
</style>
