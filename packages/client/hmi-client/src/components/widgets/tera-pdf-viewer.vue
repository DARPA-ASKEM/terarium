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
				<span class="w-6rem text-center">
					<InputNumber
						type="number"
						v-model.number="currentPage"
						:min="1"
						:max="pages"
						@keydown.enter="goToPage(currentPage)"
					/>
					/ {{ pages }}
				</span>
				<Button
					icon="pi pi-arrow-right"
					size="small"
					outlined
					severity="secondary"
					@click="nextPage"
					:disabled="currentPage >= pages"
				/>
			</div>
			<div class="search-controls">
				<Button icon="pi pi-search" size="small" outlined severity="secondary" @click="toggleSearchPopover" />
				<OverlayPanel ref="searchPopover" :showCloseIcon="false">
					<div class="search-popover-content">
						<div class="flex items-center gap-2 mb-2">
							<InputText
								v-model="searchTextModel"
								placeholder="Find text in document"
								@keydown.enter="handleSearch"
								class="w-full"
							/>
						</div>
						<div class="flex items-center gap-2">
							<div v-if="!isEmpty(searchResults)">
								<Button icon="pi pi-chevron-up" size="small" outlined @click="searchNavigate('prev')" />
								<span v-if="!isEmpty(searchResults)" class="text-sm">
									{{ searchMatchIndex }} of {{ searchMatchCount }}
								</span>
								<Button icon="pi pi-chevron-down" size="small" outlined @click="searchNavigate('next')" />
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
					Loading...
				</VuePDF>
			</template>
		</div>
	</div>
</template>

<script setup lang="ts">
import '@tato30/vue-pdf/style.css';
import { groupBy, isEmpty, debounce } from 'lodash';
import { computed, ref, watch, useTemplateRef } from 'vue';
import { VuePDF, usePDF } from '@tato30/vue-pdf';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import OverlayPanel from 'primevue/overlaypanel';

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
const searchPopover = ref<InstanceType<typeof OverlayPanel> | null>(null);

const annotationsByPage = computed(() => groupBy(props.annotations ?? [], 'pageNo'));
const { pdf, pages } = usePDF(computed(() => props.pdfLink));

// ================================================
// Page Navigation
// ================================================
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
		targetPage?.scrollIntoView({ behavior: 'auto', block: 'start' });
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
	focusedSearchItems.value = searchMatches.value[index - 1]?.textElements ?? null;
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

.search-popover-content {
	display: flex;
	flex-direction: row;
	gap: var(--gap-2);
	min-width: 250px;
}

.textLayer .highlight {
	/* Default highlight color */
	--highlight-bg-color: rgba(230, 195, 0, 0.35);
}

.highlight-focus span {
	/* Focused highlight color */
	background-color: rgb(230, 80, 0, 0.4) !important;
}
</style>
