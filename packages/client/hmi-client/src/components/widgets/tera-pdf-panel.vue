<template>
	<TabView
		v-if="pdfs?.length"
		:activeIndex="activeTabIndex"
		class="container"
		:class="pdfs?.length < 2 ? 'hide-tab-selectors' : ''"
	>
		<TabPanel :header="pdf.name" v-for="(pdf, index) in pdfs" :key="pdf.name">
			<tera-pdf-viewer
				v-if="pdf.isPdf"
				ref="pdfRef"
				:pdf-link="pdf.data"
				:title="pdf.name || ''"
				:annotations="pdfActions[index].pdfAnnotations.value"
				:current-page="pdfActions[index].pdfCurrentPage.value"
			/>
			<tera-text-editor v-else :initial-text="pdf.data" />
		</TabPanel>
	</TabView>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { keyBy } from 'lodash';

import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';

import TeraPdfViewer from '@/components/widgets/tera-pdf-viewer.vue';
import TeraTextEditor from '@/components/documents/tera-text-editor.vue';
import type { DocumentAsset } from '@/types/Types';
import { usePDFViewerActions } from '@/composables/usePDFViewerActions';

interface PdfData {
	document: DocumentAsset;
	data: string;
	isPdf: boolean;
	name: string;
}

const props = defineProps<{
	pdfs: PdfData[];
}>();

const pdfs = ref<PdfData[]>(props.pdfs);
const pdfActions = computed(() => pdfs.value.map((pdf) => (pdf.isPdf ? usePDFViewerActions() : null)));
const pdfRef = ref(null);
const activeTabIndex = ref();
const pdfDocs = computed(() =>
	keyBy(
		pdfs.value.map((pdf, index) => ({
			id: pdf.document.id,
			index,
			pdf
		})),
		'id'
	)
);

async function selectPdf(docId: string) {
	activeTabIndex.value = null;
	if (pdfDocs.value?.[docId]?.index) {
		activeTabIndex.value = pdfDocs.value[docId].index;
	} else {
		activeTabIndex.value = 0;
	}
	await nextTick();
	return activeTabIndex.value;
}

const getPdfActions = (docId: string) => {
	const pdfIndex = pdfDocs.value?.[docId]?.index ? pdfDocs.value[docId].index : 0;
	return pdfActions.value[pdfIndex];
};

defineExpose({ pdfRef, selectPdf, getPdfActions });
</script>

<style scoped>
.container {
	display: flex;
	flex-direction: column;
	flex: 1;
	height: 100%;
}

:deep(.p-tabview-panels) {
	display: flex;
	flex-direction: column;
	flex: 1;
	padding: 0;
	height: calc(100% - 42px); /* 42px is the height of the tab header */
}

:deep(.p-tabview-panel) {
	flex: 1;
	height: 100%;
}

:deep(.p-tabview-header) {
	max-width: 150px;
}

:deep(.p-tabview-title) {
	text-overflow: ellipsis;
	white-space: nowrap;
	overflow: hidden;
}
/* Hide tab selectors if there is only one tab */
.hide-tab-selectors :deep(.p-tabview-nav) {
	display: none;
}
</style>
