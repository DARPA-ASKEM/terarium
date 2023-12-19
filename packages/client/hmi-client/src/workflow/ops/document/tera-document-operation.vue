<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div>
			<tera-drilldown-section :is-loading="fetchingPDF">
				<tera-pdf-embed v-if="pdfLink" :pdf-link="pdfLink" :title="document?.name || ''" />
				<!-- <tera-text-editor v-else-if="view === DocumentView.TXT" :initial-text="docText" /> -->
			</tera-drilldown-section>
			<tera-drilldown-preview>
				<h5>{{ document?.name }}</h5>
				<h6>{{ document?.source }}</h6>
				<h7>{{ document?.description }}</h7>
				<Accordion multiple :active-index="[0, 1, 2]">
					<AccordionTab v-if="!isEmpty(equations)">
						<template #header>
							<header>Equation Images</header>
						</template>
						<tera-expandable-panel
							v-for="(equation, i) in equations"
							:key="i"
							hide-delete
							hide-edit
						>
							<template #header>
								<h5>Equation {{ i + 1 }}</h5>
							</template>
							<Image id="img" :src="equation.metadata?.url" :alt="''" preview />
						</tera-expandable-panel>
					</AccordionTab>
					<AccordionTab v-if="!isEmpty(figures)">
						<template #header>
							<header>Figure Images</header>
						</template>
					</AccordionTab>
					<AccordionTab v-if="!isEmpty(tables)">
						<template #header>
							<header>Table Images</header>
						</template>
					</AccordionTab>
				</Accordion>
				<template #footer?>
					<Button label="Close" @click="emit('close')"></Button>
				</template>
			</tera-drilldown-preview>
		</div>
	</tera-drilldown>
</template>

<script setup lang="ts">
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import { WorkflowNode } from '@/types/workflow';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import { computed, onMounted, ref } from 'vue';
import { DocumentAsset, ExtractionAssetType } from '@/types/Types';
import { downloadDocumentAsset, getDocumentAsset } from '@/services/document-assets';
import { isEmpty } from 'lodash';
import TeraExpandablePanel from '@/components/widgets/tera-expandable-panel.vue';
import Image from 'primevue/image';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { DocumentOperationState } from './document-operation';

const emit = defineEmits(['close']);
const props = defineProps<{
	node: WorkflowNode<DocumentOperationState>;
}>();

const document = ref<DocumentAsset | null>();
const pdfLink = ref<string | null>();
const fetchingPDF = ref(false);

const figures = computed(
	() =>
		document.value?.assets?.filter((asset) => asset.assetType === ExtractionAssetType.Figure) || []
);
const tables = computed(
	() =>
		document.value?.assets?.filter((asset) => asset.assetType === ExtractionAssetType.Table) || []
);
const equations = computed(
	() =>
		document.value?.assets?.filter((asset) => asset.assetType === ExtractionAssetType.Equation) ||
		[]
);

onMounted(async () => {
	if (props.node.state.documentId) {
		fetchingPDF.value = true;
		document.value = await getDocumentAsset(props.node.state.documentId);
		const filename = document.value?.fileNames?.[0];
		if (document.value?.id && filename)
			pdfLink.value = await downloadDocumentAsset(document.value.id, filename);
		fetchingPDF.value = false;
	}
});
</script>

<style scoped></style>
