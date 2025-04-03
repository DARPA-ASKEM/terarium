import { PDFAnnotation, PDFPageScrollPosition } from '@/types/common';
import { BBox } from '@/types/Types';
import { ref } from 'vue';

const HIGHLIGHT_COLOR = '#EBF4F3';

const calculatePageScrollPosition = (bbox: BBox) => {
	const { top, bottom } = bbox;
	const mid = (top + bottom) / 2;
	if (mid < 0.33) {
		return 'start';
	}
	if (mid > 0.67) {
		return 'end';
	}
	return 'center';
};

export function usePDFViewerActions() {
	const pdfAnnotations = ref<PDFAnnotation[]>([]);
	const pdfCurrentPage = ref({ page: 1, scrollPosition: 'start' as PDFPageScrollPosition });

	/**
	 * Scroll to the given bounding box on the given page
	 * @param page page number
	 * @param bbox bounding box
	 * @returns
	 */
	const scrollToBBox = (page: number, bbox: BBox) => {
		pdfCurrentPage.value = {
			page,
			scrollPosition: calculatePageScrollPosition(bbox)
		};
	};

	const goToPage = (page: number) => {
		pdfCurrentPage.value = {
			page,
			scrollPosition: 'start'
		};
	};

	const highlightBBox = (page: number, bbox: BBox) => {
		pdfAnnotations.value.push({
			pageNo: page,
			bbox,
			color: HIGHLIGHT_COLOR,
			isHighlight: true
		});
	};

	/**
	 * Highlight the given bounding box on the given page.
	 * @param bboxes array of bounding boxes with page number
	 * @param replace if true, clear all previous annotations
	 */
	const highlightBBoxes = (bboxes: { page: number; bbox: BBox }[], replace = true) => {
		if (replace) clearAllAnnotations();
		bboxes.forEach((bbox) => highlightBBox(bbox.page, bbox.bbox));
	};

	const highlightAndScrollToBBox = (page: number, bbox: BBox, replace = true) => {
		if (replace) clearAllAnnotations();
		highlightBBox(page, bbox);
		scrollToBBox(page, bbox);
	};

	const clearAllAnnotations = () => {
		pdfAnnotations.value = [];
	};

	return {
		pdfAnnotations,
		pdfCurrentPage,
		goToPage,
		scrollToBBox,
		highlightBBox,
		highlightBBoxes,
		highlightAndScrollToBBox,
		clearAllAnnotations
	};
}
