<template>
	<TabView
		v-if="pdfs?.length"
		:activeIndex="activeTabIndex"
		class="container"
		:class="pdfs?.length < 2 ? 'hide-tab-selectors' : ''"
	>
		<TabPanel :header="pdf.name" v-for="pdf in pdfs" :key="pdf.name">
			<tera-pdf-embed v-if="pdf.isPdf" ref="pdfRef" :pdf-link="pdf.data" :title="pdf.name || ''" />
			<tera-text-editor v-else :initial-text="pdf.data" />
		</TabPanel>
	</TabView>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { keyBy } from 'lodash';

import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';

import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import TeraTextEditor from '@/components/documents/tera-text-editor.vue';
import type { DocumentAsset } from '@/types/Types';

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

async function selectPdf(id) {
	activeTabIndex.value = null;
	if (pdfDocs.value?.[id]?.index) {
		activeTabIndex.value = pdfDocs.value[id].index;
	} else {
		activeTabIndex.value = 0;
	}
	await nextTick();
	return activeTabIndex.value;
}

defineExpose({ pdfRef, selectPdf });
</script>

<style scoped>
.container {
	display: flex;
	flex-direction: column;
	flex: 1;
}

:deep(.p-tabview) {
	display: flex;
	flex-direction: column;
	flex: 1;
}
:deep(.p-tabview-panels) {
	display: flex;
	flex-direction: column;
	flex: 1;
	padding-top: 0;
}
:deep(.p-tabview-panel) {
	flex: 1;
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
