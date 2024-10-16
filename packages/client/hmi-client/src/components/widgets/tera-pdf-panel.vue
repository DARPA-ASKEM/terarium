<template>
	<Dropdown v-if="pdfs?.length" class="mx-3" v-model="pdf" :options="pdfs" optionLabel="name" optionsValue="data" />
	<tera-pdf-embed v-if="pdf && pdf.isPdf" :pdf-link="pdf.data" :title="pdf.name || ''" />
	<tera-text-editor v-else-if="pdf && !pdf.isPdf" :initial-text="pdf.data" />
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import Dropdown from 'primevue/dropdown';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import TeraTextEditor from '@/components/documents/tera-text-editor.vue';

interface PdfData {
	document: any;
	data: string;
	isPdf: boolean;
	name: string;
}

const props = defineProps<{
	pdfs: PdfData[];
}>();

const pdfs = ref<PdfData[]>(props.pdfs);
const pdf = ref<PdfData>();

watch(
	props.pdfs,
	() => {
		if (!props.pdfs) return;
		pdf.value = pdfs.value[0];
	},
	{ immediate: true }
);
</script>

<style scoped></style>
