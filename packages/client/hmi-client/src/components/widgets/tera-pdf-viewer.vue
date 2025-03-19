<template>
	<div class="pdf-viewer-container">
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
					@loaded="(data) => onPageLoaded(data, page)"
				/>
			</template>
		</div>
		<div class="controls"></div>
	</div>
</template>

<script setup lang="ts">
import '@tato30/vue-pdf/style.css';
import { groupBy } from 'lodash';
import { computed, ref, watch, useTemplateRef } from 'vue';
import { VuePDF, usePDF } from '@tato30/vue-pdf';

const DEFAULT_SCALE = 2;
const HIGHLIGHT_DEFAULT_COLOR = 'rgba(255, 255, 0, 0.5)'; // Yellow with 50% opacity
const BBOX_DEFAULT_COLOR = 'green';

export interface PdfAnnotation {
	pageNo: number;
	bbox: { l: number; t: number; r: number; b: number };
	color: string;
	isHighlight: boolean;
}

const props = defineProps<{
	pdfLink?: string;
	// title?: string;
	// filePromise?: Promise<ArrayBuffer | null>;
	fitToWidth?: boolean;
	currentPage?: number;
	annotations?: PdfAnnotation[];
}>();

const vuePdfs = useTemplateRef('vuePdfs');
const annotationsByPage = computed(() => groupBy(props.annotations ?? [], 'pageNo'));

const { pdf, pages } = usePDF(computed(() => props.pdfLink));

const getPdfPage = (pageNumber: number) => {
	if (!vuePdfs.value?.[pageNumber - 1] || pageNumber < 1 || pageNumber > vuePdfs.value.length) return null;
	return vuePdfs.value[pageNumber - 1];
};

const goToPage = (pageNumber) => {
	const targetPage = getPdfPage(pageNumber)?.$el;
	targetPage?.scrollIntoView({ behavior: 'smooth', block: 'start' });
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

// Update based on the props changes
watch(() => props.currentPage, goToPage);
watch(
	() => props.annotations,
	(annotations, oldAnnotations) => {
		// Get the pages that needs to be updated
		const pageNumbers = [...(annotations ?? []), ...(oldAnnotations ?? [])].map((annotation) => annotation.pageNo);
		const uniquePages = Array.from(new Set(pageNumbers));
		// Reloads the pages to redraw the annotations
		uniquePages.forEach((page) => getPdfPage(page)?.reload());
	}
);

const scale = ref(DEFAULT_SCALE);

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
.pages-container {
	flex-grow: 1;
	width: 100%;
	display: flex;
	flex-direction: column;
	overflow: auto;
	align-items: center;
}
</style>
