<template>
	<div class="pdf-viewer-container">
		<div class="controls">
			<div v-if="!fitToWidth" class="zoom-controls">
				<Button icon="pi pi-search-minus" size="small" text severity="secondary" @click="zoomOut" />
				<span class="w-3rem text-center">{{ Math.round(scale * 100) }}%</span>
				<Button icon="pi pi-search-plus" size="small" text severity="secondary" @click="zoomIn" />
				<Divider layout="vertical" />
			</div>
			<div class="page-navigation">
				<Button
					icon="pi pi-arrow-left"
					size="small"
					text
					severity="secondary"
					@click="prevPage"
					:disabled="currentPageNumber <= 1"
				/>
				<span class="w-6rem text-center">
					<InputNumber
						type="number"
						v-model.number="currentPageNumber"
						:min="1"
						:max="pages"
						@keydown.enter="goToPage(currentPageNumber)"
					/>
					/ {{ pages }}
				</span>
				<Button
					icon="pi pi-arrow-right"
					size="small"
					text
					severity="secondary"
					@click="nextPage"
					:disabled="currentPageNumber >= pages"
				/>
			</div>
			<Divider layout="vertical" />
			<div class="search-controls">
				<Button icon="pi pi-search" size="small" text severity="secondary" @click="toggleSearchPopover" />
				<OverlayPanel class="pdf-viewer-search-text-overlay" ref="searchPopover" :showCloseIcon="false">
					<div class="search-popover-content gap-2">
						<div class="flex gap-2 flex-grow-1">
							<InputText
								v-model="searchTextModel"
								placeholder="Find text in document"
								@keydown.enter="handleSearch"
								class="w-full pr-[8rem]"
							/>
						</div>
						<div class="flex gap-2">
							<div class="flex flex-row align-items-center gap-1" v-if="!isEmpty(searchResults)">
								<span v-if="!isEmpty(searchResults)" class="highlights-counter">
									{{ searchMatchIndex }} of {{ searchMatchCount }}
								</span>
								<Button text severity="secondary" icon="pi pi-angle-up" @click="searchNavigate('prev')" />
								<Button text severity="secondary" icon="pi pi-angle-down" @click="searchNavigate('next')" />
							</div>
							<Button icon="pi pi-times" size="small" text @click="clearSearch" class="ml-auto" />
						</div>
					</div>
				</OverlayPanel>
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
					:highlight-text="searchTextHighlight"
					@highlight="onSearchTextHighlight"
					@loaded="(data) => onPageLoaded(data, page)"
				>
					<div class="flex justify-content-center align-items-center h-full relative">
						<div class="page-loading-backdrop" />
						<i class="page-loading-spinner pi pi-spin pi-spinner" />
					</div>
				</VuePDF>
			</template>
		</div>
	</div>
</template>

<script setup lang="ts">
import '@tato30/vue-pdf/style.css';
import { groupBy, isEmpty, debounce, isEqual } from 'lodash';
import { computed, ref, watch, useTemplateRef } from 'vue';
import { VuePDF, usePDF } from '@tato30/vue-pdf';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import OverlayPanel from 'primevue/overlaypanel';
import Divider from 'primevue/divider';

const DEFAULT_SCALE = 1.5;
const SCALE_INCREMENT = 0.25;
const MIN_SCALE = 0.5;
const MAX_SCALE = 4;
const DEFAULT_CURRENT_PAGE = 1;
const HIGHLIGHT_DEFAULT_COLOR = 'rgba(255, 255, 0, 0.5)';
const BBOX_DEFAULT_COLOR = 'green';

export interface PdfAnnotation {
	pageNo: number;
	bbox: { left: number; top: number; right: number; bottom: number };
	color: string;
	isHighlight: boolean;
}
export type PageScrollPosition = 'start' | 'center' | 'end';

const props = defineProps<{
	pdfLink: string;
	title?: string;
	/**
	 * Whether to fit the PDF to the width of the container. When true, the zoom controls are hidden.
	 */
	fitToWidth?: boolean;
	currentPage?: {
		page: number;
		scrollPosition?: PageScrollPosition;
	};
	annotations?: PdfAnnotation[];
}>();

const vuePdfs = useTemplateRef<InstanceType<typeof VuePDF>[] | null>('vuePdfs');
const searchPopover = ref<InstanceType<typeof OverlayPanel> | null>(null);

const annotationsByPage = computed(() => groupBy(props.annotations ?? [], 'pageNo'));
const { pdf, pages } = usePDF(computed(() => props.pdfLink));

// ================================================
// Page Navigation
// ================================================
const currentPageNumber = ref(props.currentPage?.page ?? DEFAULT_CURRENT_PAGE);
watch(
	() => props.currentPage,
	(newVal) => {
		if (newVal && !isEqual(newVal, currentPageNumber.value)) {
			goToPage(newVal.page, newVal.scrollPosition);
		}
	}
);

const getPdfPage = (pageNumber: number) => {
	if (!vuePdfs.value?.[pageNumber - 1] || pageNumber < 1 || pageNumber > pages.value) return null;
	return vuePdfs.value[pageNumber - 1];
};

const goToPage = (pageNumber: number, pageScrollPosition = 'start') => {
	if (pageNumber >= 1 && pageNumber <= pages.value) {
		currentPageNumber.value = pageNumber;
		const targetPage = getPdfPage(pageNumber)?.$el;
		targetPage?.scrollIntoView({ behavior: 'auto', block: pageScrollPosition });
	}
};

const prevPage = () => {
	if (currentPageNumber.value > 1) {
		currentPageNumber.value--;
		goToPage(currentPageNumber.value);
	}
};

const nextPage = () => {
	if (currentPageNumber.value < pages.value) {
		currentPageNumber.value++;
		goToPage(currentPageNumber.value);
	}
};

// ================================================
// Annotations
// ================================================
const applyAnnotations = (annotations: PdfAnnotation[]) => {
	annotations.forEach((annotation) =>
		drawBbox(annotation.pageNo, annotation.bbox, annotation.isHighlight, annotation.color)
	);
};

const onPageLoaded = (_payload: any, pageNumber: number) => {
	const annotations = annotationsByPage.value[pageNumber] ?? [];
	applyAnnotations(annotations);
};

const drawBbox = (pageNumber: number, bbox: PdfAnnotation['bbox'], isHighlight: boolean, color?: string) => {
	const pageElement = getPdfPage(pageNumber)?.$el;
	if (!pageElement) return;
	const canvas = pageElement.querySelector('canvas');
	if (!canvas) return;
	const context = canvas.getContext('2d');
	if (!context) return;

	const { top, right, bottom, left } = bbox;
	const width = canvas.width;
	const height = canvas.height;

	context.save();
	const x = left * width;
	const y = top * height;
	const w = (right - left) * width;
	const h = (bottom - top) * height;
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
	},
	{ deep: true }
);

// ================================================
// Zoom Controls
// ================================================
const scale = ref(DEFAULT_SCALE);

const zoomIn = () => {
	scale.value = Math.min(MAX_SCALE, scale.value + SCALE_INCREMENT);
};

const zoomOut = () => {
	scale.value = Math.max(MIN_SCALE, scale.value - SCALE_INCREMENT);
};

// ================================================
// Text Search
// ================================================
const FOCUSED_HIGHLIGHT_CLASS = 'highlight-focus';
const searchTextModel = ref('');
const searchTextHighlight = ref('');
const searchResults = ref<Record<string, any>[]>([]);
const searchMatches = ref<{ page: number; textElements: HTMLElement[] }[]>([]);
const searchMatchCount = computed(() => searchMatches.value.length);
const searchMatchIndex = ref(0);
const focusedSearchItems = ref<HTMLElement[] | null>(null);

watch(
	() => searchResults.value,
	debounce(() => {
		// Since on highlight event triggers for every page, we need to debounce the search results to wait for all pages to be processed.
		// Update searchMatches after all pages are processed.
		searchMatches.value = searchResults.value
			.map((r) => {
				// Note that search results are ordered by page
				const matches = r.matches.map((m) => {
					const elementsIndices = Array.from({ length: m.end.idx - m.start.idx + 1 }, (_, i) => m.start.idx + i);
					return {
						page: r.page,
						textElements: elementsIndices.map((i) => r.textDivs[i])
					};
				});
				return matches;
			})
			.flat();
		searchMatchIndex.value = isEmpty(searchMatches.value) ? 0 : 1;
		focusSearchItem(searchMatchIndex.value);
	}, 300),
	{ deep: true }
);

const clearFocusedSearchItem = () => {
	if (!isEmpty(focusedSearchItems.value) && focusedSearchItems.value !== null) {
		focusedSearchItems.value.forEach((item) => {
			item.classList.remove(FOCUSED_HIGHLIGHT_CLASS);
		});
	}
	focusedSearchItems.value = null;
};

const focusSearchItem = (index: number) => {
	clearFocusedSearchItem();
	const focusedMatch = searchMatches.value[index - 1];
	focusedSearchItems.value = focusedMatch?.textElements ?? null;
	focusedSearchItems.value?.forEach((item) => {
		item.classList.add(FOCUSED_HIGHLIGHT_CLASS);
	});
	const firstFocusedItem = focusedSearchItems.value?.[0];
	if (firstFocusedItem) {
		// Scroll the first focused item into view if it's not visible
		const rect = firstFocusedItem.getBoundingClientRect();
		const isVisible =
			rect.top >= 0 &&
			rect.left >= 0 &&
			rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
			rect.right <= (window.innerWidth || document.documentElement.clientWidth);

		if (!isVisible) {
			firstFocusedItem.scrollIntoView({ behavior: 'auto', block: 'center' });
			currentPageNumber.value = focusedMatch.page; // Update current page to the page of the focused item
		}
	}
};

const toggleSearchPopover = (event) => {
	searchPopover.value?.toggle(event);
};

const handleSearch = () => {
	if (searchTextModel.value === '') {
		searchResults.value = [];
		clearFocusedSearchItem();
	}
	if (searchTextModel.value !== searchTextHighlight.value) {
		searchTextHighlight.value = searchTextModel.value;
	} else {
		searchNavigate('next');
	}
};

const onSearchTextHighlight = (value) => {
	// Since page starts from 1, subtract 1 from the page number to index the results array ordered by page
	searchResults.value[value.page - 1] = value;
};

const searchNavigate = (direction: 'next' | 'prev') => {
	if (searchMatchCount.value === 0) return;

	if (direction === 'next') {
		searchMatchIndex.value = searchMatchIndex.value < searchMatchCount.value ? searchMatchIndex.value + 1 : 1;
	} else {
		searchMatchIndex.value = searchMatchIndex.value > 1 ? searchMatchIndex.value - 1 : searchMatchCount.value;
	}
	focusSearchItem(searchMatchIndex.value);
};

const clearSearch = () => {
	searchTextModel.value = '';
	searchTextHighlight.value = '';
	searchResults.value = [];
	searchMatchIndex.value = 0;
	clearFocusedSearchItem();
	searchPopover.value?.hide();
};

defineExpose({
	goToPage
});
</script>

<style>
/*
 * Note that this style block isn't scoped since the overlay component is appended to the body html dynamically when opened
 * and is placed outside of this component's scope and the scoped styles aren't applied to it.
 */
.pdf-viewer-search-text-overlay.p-overlaypanel {
	.p-overlaypanel-content {
		padding: 0;
	}
}
</style>
<style scoped>
.pdf-viewer-container {
	position: relative;
	height: 100%;
	width: 100%;
	display: flex;
	flex-direction: column;
}

.controls {
	display: flex;
	padding: 0.5rem;
	align-items: center;
	border-bottom: 1px solid #ccc;
}

.zoom-controls,
.page-navigation,
.search-controls {
	display: flex;
	align-items: center;
	gap: var(--gap-2);
}

.page-navigation :deep(input) {
	width: 3rem;
}

.pages-container {
	flex-grow: 1;
	width: 100%;
	display: flex;
	flex-direction: column;
	overflow: auto;
	align-items: center;
	padding: 1.25rem;
}

.page {
	margin-bottom: 1.25rem;
	box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.search-popover-content {
	padding: 0.5rem;
	display: flex;
	flex-direction: row;
	gap: var(--gap-2);
	min-width: 18rem;
	justify-content: right;

	&:deep(input.p-inputtext) {
		width: 100%;
		min-width: 10rem;
		padding-right: 5.5rem;
	}

	.highlights-counter {
		position: absolute;
		right: 9.5rem;
		font-size: var(--font-caption);
	}
}

:deep(.textLayer .highlight) {
	/* Default highlight color */
	--highlight-bg-color: rgba(230, 195, 0, 0.35);
}

:deep(.highlight-focus span) {
	/* Focused highlight color */
	background-color: rgb(230, 80, 0, 0.4) !important;
}

.page-loading-backdrop {
	background-color: rgba(0, 0, 0, 0.2);
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	z-index: 1;
}
.page-loading-spinner {
	color: var(--primary-color);
	font-size: 2rem;
	z-index: 2;
}
</style>
