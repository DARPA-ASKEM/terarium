<template>
	<div class="pdf-viewer-container">
		<div class="controls">
			<div class="zoom-controls">
				<Button icon="pi pi-search-minus" size="small" outlined severity="secondary" @click="zoomOut" />
				<span class="w-3rem text-center">{{ Math.round(scale * 100) }}%</span>
				<Button icon="pi pi-search-plus" size="small" outlined severity="secondary" @click="zoomIn" />
			</div>
			<div class="page-navigation">
				<Button
					icon="pi pi-arrow-left"
					size="small"
					outlined
					severity="secondary"
					@click="prevPage"
					:disabled="currentPage <= 1"
				/>
				<span class="w-6rem text-center">Page {{ currentPage }} / {{ pages }}</span>
				<Button
					icon="pi pi-arrow-right"
					size="small"
					outlined
					severity="secondary"
					@click="nextPage"
					:disabled="currentPage >= pages"
				/>
				<InputNumber
					type="number"
					v-model.number="currentPage"
					:min="1"
					:max="pages"
					@keydown.enter="goToPage(currentPage)"
				/>
			</div>
			<div class="search-controls">
				<InputText type="text" v-model="searchText" placeholder="Search text" />
			</div>
		</div>
		<div class="pages-container">
			<template v-for="page in pages" :key="page">
				<VuePDF
					ref="vuePdfs"
					class="page"
					:pdf="pdf"
					:page="page"
					:fit-parent="fitToWidth"
					:scale="scale"
					text-layer
					:highlight-text="searchText"
					@loaded="(data) => onPageLoaded(data, page)"
				/>
			</template>
		</div>
	</div>
</template>

<script setup lang="ts">
import '@tato30/vue-pdf/style.css';
import { groupBy } from 'lodash';
import { computed, ref, watch, useTemplateRef } from 'vue';
import { VuePDF, usePDF } from '@tato30/vue-pdf';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';

const DEFAULT_SCALE = 1.5;
const SCALE_INCREMENT = 0.25;
const MIN_SCALE = 0.5;
const MAX_SCALE = 4;
const DEFAULT_CURRENT_PAGE = 1;
const HIGHLIGHT_DEFAULT_COLOR = 'rgba(255, 255, 0, 0.5)';
const BBOX_DEFAULT_COLOR = 'green';

export interface PdfAnnotation {
	pageNo: number;
	bbox: { l: number; t: number; r: number; b: number };
	color: string;
	isHighlight: boolean;
}

const props = defineProps<{
	pdfLink: string;
	fitToWidth?: boolean;
	currentPage?: number;
	annotations?: PdfAnnotation[];
}>();

const vuePdfs = useTemplateRef<InstanceType<typeof VuePDF>[] | null>('vuePdfs');
const annotationsByPage = computed(() => groupBy(props.annotations ?? [], 'pageNo'));
const { pdf, pages } = usePDF(computed(() => props.pdfLink));

const currentPage = ref(props.currentPage ?? DEFAULT_CURRENT_PAGE);
watch(
	() => props.currentPage,
	(newVal) => {
		if (newVal && newVal !== currentPage.value) {
			currentPage.value = newVal;
		}
	}
);

const getPdfPage = (pageNumber: number) => {
	if (!vuePdfs.value?.[pageNumber - 1] || pageNumber < 1 || pageNumber > pages.value) return null;
	return vuePdfs.value[pageNumber - 1];
};

const goToPage = (pageNumber: number) => {
	if (pageNumber >= 1 && pageNumber <= pages.value) {
		currentPage.value = pageNumber;
		const targetPage = getPdfPage(pageNumber)?.$el;
		targetPage?.scrollIntoView({ behavior: 'smooth', block: 'start' });
	}
};

const prevPage = () => {
	if (currentPage.value > 1) {
		currentPage.value--;
		goToPage(currentPage.value);
	}
};

const nextPage = () => {
	if (currentPage.value < pages.value) {
		currentPage.value++;
		goToPage(currentPage.value);
	}
};

const applyAnnotations = (annotations: PdfAnnotation[]) => {
	annotations.forEach((annotation) =>
		drawBbox(annotation.pageNo, annotation.bbox, annotation.isHighlight, annotation.color)
	);
};

const onPageLoaded = (_payload: any, pageNumber: number) => {
	const annotations = annotationsByPage.value[pageNumber] ?? [];
	applyAnnotations(annotations);
};

const drawBbox = (
	pageNumber: number,
	bbox: { t: number; r: number; b: number; l: number },
	isHighlight: boolean,
	color?: string
) => {
	const pageElement = getPdfPage(pageNumber)?.$el;
	if (!pageElement) return;
	const canvas = pageElement.querySelector('canvas');
	if (!canvas) return;
	const context = canvas.getContext('2d');
	if (!context) return;

	const { t, r, b, l } = bbox;
	const width = canvas.width;
	const height = canvas.height;

	context.save();
	const x = l * width;
	const y = t * height;
	const w = (r - l) * width;
	const h = (b - t) * height;
	if (isHighlight) {
		context.fillStyle = color ?? HIGHLIGHT_DEFAULT_COLOR;
		context.globalCompositeOperation = 'multiply';
		context.fillRect(x, y, w, h);
	} else {
		context.strokeStyle = color ?? BBOX_DEFAULT_COLOR;
		context.lineWidth = 2;
		context.strokeRect(x, y, w, h);
	}
	context.restore();
};

watch(
	() => props.annotations,
	(annotations, oldAnnotations) => {
		const pageNumbers = [...(annotations ?? []), ...(oldAnnotations ?? [])].map((annotation) => annotation.pageNo);
		const uniquePages = Array.from(new Set(pageNumbers));
		uniquePages.forEach((page) => getPdfPage(page)?.reload());
	}
);

const scale = ref(DEFAULT_SCALE);

const zoomIn = () => {
	scale.value = Math.min(MAX_SCALE, scale.value + SCALE_INCREMENT);
};

const zoomOut = () => {
	scale.value = Math.max(MIN_SCALE, scale.value - SCALE_INCREMENT);
};

const searchText = ref('');

defineExpose({
	goToPage
});
</script>

<style>
.pdf-viewer-container {
	position: relative;
	height: 100%;
	width: 100%;
	display: flex;
	flex-direction: column;
}

.controls {
	display: flex;
	padding: 10px;
	gap: 20px;
	align-items: center;
	border-bottom: 1px solid #ccc;
}

.zoom-controls,
.page-navigation,
.search-controls {
	display: flex;
	align-items: center;
	gap: 10px;
}

.page-navigation input {
	width: 50px;
}

.pages-container {
	flex-grow: 1;
	width: 100%;
	display: flex;
	flex-direction: column;
	overflow: auto;
	align-items: center;
	padding: 20px;
}

.page {
	margin-bottom: 20px;
	box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}
</style>
