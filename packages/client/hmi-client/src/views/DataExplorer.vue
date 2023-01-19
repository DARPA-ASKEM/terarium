<template>
	<div class="data-explorer-container">
		<div class="facets-and-results-container">
			<slider-panel
				content-width="240px"
				direction="left"
				header="Facets"
				v-model:is-open="isSliderFacetsOpen"
			>
				<template v-slot:content>
					<facets-panel
						v-if="viewType === ViewType.LIST"
						class="facets-panel"
						:facets="facets"
						:filtered-facets="filteredFacets"
						:result-type="resultType"
					/>
				</template>
			</slider-panel>
			<div class="results-content">
				<div class="secondary-header">
					<div class="button-group">
						<button
							type="button"
							:class="{ active: resultType === ResourceType.XDD }"
							@click="updateResultType(ResourceType.XDD)"
						>
							<component :is="getResourceTypeIcon(ResourceType.XDD)" />
							Papers
						</button>
						<button
							type="button"
							:class="{ active: resultType === ResourceType.MODEL }"
							@click="updateResultType(ResourceType.MODEL)"
						>
							<component :is="getResourceTypeIcon(ResourceType.MODEL)" />
							Models
						</button>
						<button
							type="button"
							:class="{ active: resultType === ResourceType.DATASET }"
							@click="updateResultType(ResourceType.DATASET)"
						>
							<component :is="getResourceTypeIcon(ResourceType.DATASET)" />
							Datasets
						</button>
					</div>
					<div class="button-group">
						<button
							type="button"
							:class="{ active: viewType === ViewType.LIST }"
							@click="viewType = ViewType.LIST"
						>
							List
						</button>
						<button type="button" @click="viewType = ViewType.MATRIX">Matrix</button>
					</div>
				</div>
				<search-results-list
					v-if="viewType === ViewType.LIST"
					:data-items="dataItems"
					:result-type="resultType"
					:selected-search-items="selectedSearchItems"
					:search-term="searchTerm"
					@toggle-data-item-selected="toggleDataItemSelected"
				/>
				<search-results-matrix
					v-else-if="viewType === ViewType.MATRIX"
					:data-items="dataItems"
					:result-type="resultType"
					:selected-search-items="selectedSearchItems"
					:dict-names="dictNames"
					@toggle-data-item-selected="toggleDataItemSelected"
				/>
			</div>
			<preview-panel
				class="preview-slider"
				content-width="calc(35% - 48px)"
				tab-width="0"
				direction="right"
				v-model:preview-item="previewItem"
				:result-type="resultType"
				@toggle-data-item-selected="toggleDataItemSelected"
			/>
			<slider-panel
				class="resources-slider"
				content-width="35%"
				tab-width="48px"
				direction="right"
				header="Cart"
				v-model:is-open="isSliderResourcesOpen"
			>
				<template v-slot:content>
					<selected-resources-options-pane
						:selected-search-items="selectedSearchItems"
						@remove-item="toggleDataItemSelected"
						@close="isSliderResourcesOpen = false"
					/>
				</template>
			</slider-panel>
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';

import SearchResultsList from '@/components/data-explorer/search-results-list.vue';
import SearchResultsMatrix from '@/components/data-explorer/search-results-matrix.vue';
import FacetsPanel from '@/components/data-explorer/facets-panel.vue';
import SelectedResourcesOptionsPane from '@/components/drilldown-panel/selected-resources-options-pane.vue';
import SliderPanel from '@/components/data-explorer/slider-panel.vue';
import PreviewPanel from '@/components/data-explorer/preview-panel.vue';

import { fetchData, getXDDSets } from '@/services/data';
import {
	SearchParameters,
	SearchResults,
	Facets,
	ResourceType,
	ResultType,
	ViewType
} from '@/types/common';
import { getFacets } from '@/utils/facets';
import {
	XDD_RESULT_DEFAULT_PAGE_SIZE,
	XDDArticle,
	FACET_FIELDS as XDD_FACET_FIELDS,
	YEAR
} from '@/types/XDD';
import { Model } from '@/types/Model';
import useQueryStore from '@/stores/query';
import filtersUtil from '@/utils/filters-util';
import useResourcesStore from '@/stores/resources';
import { getResourceTypeIcon, isDataset, isModel, isXDDArticle, validate } from '@/utils/data-util';
import { cloneDeep, intersectionBy, isEmpty, isEqual, max, min, unionBy } from 'lodash';
import { Dataset as IDataset } from '@/types/Dataset';
import { LocationQuery, useRoute } from 'vue-router';

// FIXME: page count is not taken into consideration
const emit = defineEmits(['search-query-changed', 'related-search-terms-updated']);

const props = defineProps<{
	query?: LocationQuery;
}>();
const searchQuery = computed(() => props.query);
const route = useRoute();

const dataItems = ref<SearchResults[]>([]);
const dataItemsUnfiltered = ref<SearchResults[]>([]);
const selectedSearchItems = ref<ResultType[]>([]);
const previewItem = ref<ResultType | null>(null);
const searchTerm = ref('');
const relatedSearchTerms = ref<string[]>([]);
const query = useQueryStore();
const resources = useResourcesStore();

// default slider state
const isSliderFacetsOpen = ref(true);
const isSliderResourcesOpen = ref(false);
// close resources if preview opens
watch(isSliderResourcesOpen, () => {
	if (isSliderResourcesOpen.value) {
		previewItem.value = null;
	}
});

const pageSize = ref(XDD_RESULT_DEFAULT_PAGE_SIZE);
// xdd
const xddDatasets = ref<string[]>([]);
const dictNames = ref<string[]>([]);
const rankedResults = ref(true); // disable sorted/ranked results to enable pagination
// facets
const facets = ref<Facets>({});
const filteredFacets = ref<Facets>({});
//
const resultType = ref<string>(ResourceType.XDD);
const viewType = ref<string>(ViewType.LIST);

// optimize search performance: only fetch as needed
const dirtyResults = ref<{ [resultType: string]: boolean }>({});

const xddDataset = computed(() =>
	resultType.value === ResourceType.XDD ? resources.xddDataset : 'TERArium'
);
const clientFilters = computed(() => query.clientFilters);

const xddDatasetSelectionChanged = (newDataset: string) => {
	if (xddDataset.value !== newDataset) {
		resources.setXDDDataset(newDataset);
	}
};

const calculateFacets = (unfilteredData: SearchResults[], filteredData: SearchResults[]) => {
	// retrieves filtered & unfiltered facet data
	facets.value = getFacets(unfilteredData, resultType.value);
	filteredFacets.value = getFacets(filteredData, resultType.value);
};

const mergeResultsKeepRecentDuplicates = (
	existingResults: SearchResults[],
	newResults: SearchResults[]
) => {
	const mergeId = 'searchSubsystem';
	const mergedResults = unionBy(existingResults, newResults, mergeId);
	// replace existing old results with new ones, if any
	const overlapping = intersectionBy(existingResults, newResults, mergeId);
	overlapping.forEach((res) => {
		const existingOldIndex = mergedResults.findIndex(
			(u) => u.searchSubsystem === res.searchSubsystem
		);
		const newResult = newResults.find((u) => u.searchSubsystem === res.searchSubsystem);
		// remove the old one and insert the new updated result
		if (newResult) {
			mergedResults.splice(existingOldIndex, 1, newResult);
		}
	});
	return mergedResults;
};

const executeSearch = async () => {
	// only execute search if current data is dirty and a refetch is needed
	if (!dirtyResults.value[resultType.value]) return;
	// TODO: only search (or fetch data) relevant to the currently selected tab

	//
	// search across artifects: XDD, HMI SERVER DB including models, projects, etc.
	//
	// this requires hitting the backend twice to grab filtered and filtered data (and facets)
	//

	let searchWords = searchTerm.value;

	const isValidDOI = validate(searchWords);

	const matchAll =
		!isEmpty(searchWords) && searchWords.startsWith('"') && searchWords.endsWith('"');
	const allSearchTerms = searchWords.split(' ');
	if (matchAll && allSearchTerms.length > 0) {
		// multiple words are provided as search term and the user requested to match all of them
		// the XDD api expects all search terms to be comma-separated if the user requested inclusive results
		searchWords = allSearchTerms.join(',');
	}

	// start with initial search parameters
	const searchParams: SearchParameters = {
		[ResourceType.XDD]: {
			dict: dictNames.value,
			dataset:
				xddDataset.value === ResourceType.ALL || xddDataset.value === 'TERArium'
					? null
					: xddDataset.value,
			max: pageSize.value,
			perPage: pageSize.value,
			fullResults: !rankedResults.value,
			doi: isValidDOI ? searchWords : undefined,
			includeHighlights: true,
			inclusive: matchAll,
			facets: true, // include facets aggregation data in the search results
			match: true,
			additional_fields: 'title,abstract',
			known_entities: 'url_extractions'
		}
	};

	const searchParamsWithFacetFilters = cloneDeep(searchParams);

	//
	// extend search parameters by converting facet filters into proper search parameters
	//
	const xddSearchParams = searchParamsWithFacetFilters?.[ResourceType.XDD] || {};
	// transform facet filters into xdd search parameters
	clientFilters.value.clauses.forEach((clause) => {
		if (XDD_FACET_FIELDS.includes(clause.field)) {
			// NOTE: special case
			if (clause.field === YEAR) {
				if (clause.values.length === 1) {
					// a single year is selected
					const val = (clause.values as string[]).join(',');
					const formattedVal = `${val}-01-01`; // must be in ISO format; 2020-01-01
					xddSearchParams.min_published = formattedVal;
					xddSearchParams.max_published = formattedVal;
				} else {
					// multiple years are selected, so find their range
					const years = clause.values.map((year) => +year);
					const minYear = min(years);
					const maxYear = max(years);
					const formattedValMinYear = `${minYear}-01-01`; // must be in ISO format; 2020-01-01
					const formattedValMaxYear = `${maxYear}-01-01`; // must be in ISO format; 2020-01-01
					xddSearchParams.min_published = formattedValMinYear;
					xddSearchParams.max_published = formattedValMaxYear;
				}
			} else {
				const val = (clause.values as string[]).join(',');
				xddSearchParams[clause.field] = val;
			}
		}
	});
	const modelSearchParams = searchParamsWithFacetFilters?.[ResourceType.MODEL] || {
		filters: clientFilters.value
	};
	const datasetSearchParams = searchParamsWithFacetFilters?.[ResourceType.DATASET] || {
		filters: clientFilters.value
	};

	// update search parameters object
	searchParamsWithFacetFilters.xdd = xddSearchParams;
	searchParamsWithFacetFilters.model = modelSearchParams;
	searchParamsWithFacetFilters.dataset = datasetSearchParams;

	// fetch the data
	const { allData, allDataFilteredWithFacets, relatedWords } = await fetchData(
		searchWords,
		searchParams,
		searchParamsWithFacetFilters,
		resultType.value
	);

	// cache unfiltered data
	dataItemsUnfiltered.value = mergeResultsKeepRecentDuplicates(dataItemsUnfiltered.value, allData);

	// the list of results displayed in the data explorer is always the final filtered data
	dataItems.value = mergeResultsKeepRecentDuplicates(dataItems.value, allDataFilteredWithFacets);

	// final step: cache the facets and filteredFacets objects
	calculateFacets(allData, allDataFilteredWithFacets);

	relatedSearchTerms.value = relatedWords.flat();
};

const toggleDataItemSelected = (dataItem: { item: ResultType; type?: string }) => {
	let foundIndx = -1;
	const item = dataItem.item;

	if (dataItem.type && dataItem.type === 'clicked') {
		// toggle preview
		if (isEqual(dataItem.item, previewItem.value)) {
			// clear preview item and close the preview panel
			// FIXME: should we clear the preview if item is de-selected even if other items are still selected
			previewItem.value = null;
		} else {
			// open the preview panel
			previewItem.value = item;
			isSliderResourcesOpen.value = false;
		}
		return; // do not add to cart if the purpose is to toggel preview
	}

	selectedSearchItems.value.forEach((searchItem, indx) => {
		if (isModel(item) && isModel(searchItem)) {
			const itemAsModel = item as Model;
			const searchItemAsModel = searchItem as Model;
			if (searchItemAsModel.id === itemAsModel.id) foundIndx = indx;
		} else if (isDataset(item) && isDataset(searchItem)) {
			const itemAsDataset = item as IDataset;
			const searchItemAsDataset = searchItem as IDataset;
			if (searchItemAsDataset.id === itemAsDataset.id) foundIndx = indx;
		} else if (isXDDArticle(item) && isXDDArticle(searchItem)) {
			const itemAsArticle = item as XDDArticle;
			const searchItemAsArticle = searchItem as XDDArticle;
			if (searchItemAsArticle.title === itemAsArticle.title) foundIndx = indx;
		}
	});
	if (foundIndx >= 0) {
		// item was already in the list so remove it
		selectedSearchItems.value.splice(foundIndx, 1);
	} else {
		// add it to the list
		selectedSearchItems.value = [...selectedSearchItems.value, item];
	}
};

// this is called whenever the user apply some facet filter(s)
watch(clientFilters, async (n, o) => {
	if (filtersUtil.isEqual(n, o)) return;

	// user has changed some of the facet filter, so re-fetch data
	dirtyResults.value[resultType.value] = true;

	await executeSearch();

	// since facet filters differ across tabs,
	// there is no need to refetch the data on the next tab switch
	dirtyResults.value[resultType.value] = false;
});

watch(searchQuery, async (newQuery) => {
	emit('search-query-changed', newQuery);
	searchTerm.value = newQuery?.toString() ?? searchTerm.value;
	// search term has changed, so all search results are dirty; need re-fetch
	Object.values(ResourceType).forEach((key) => {
		dirtyResults.value[key as string] = true;
	});

	// re-fetch data from the server, apply filters, and re-calculate the facets
	await executeSearch();
	dirtyResults.value[resultType.value] = false;
});

watch(relatedSearchTerms, (newSearchTerms) => {
	emit('related-search-terms-updated', newSearchTerms);
});

const updateResultType = async (newResultType: ResourceType) => {
	if (resultType.value !== newResultType) {
		resultType.value = newResultType;
		// if no data currently exist for the selected tab,
		//  or if data exists but outdated then we should refetch
		const resList = dataItemsUnfiltered.value.find(
			(res) => res.searchSubsystem === resultType.value
		);
		if (!resList || dirtyResults.value[resultType.value]) {
			await executeSearch();
			dirtyResults.value[resultType.value] = false;
		} else {
			// data has not changed; the user has just switched the result tab, e.g., from Articles to Models
			// re-calculate the facets
			calculateFacets(dataItemsUnfiltered.value, dataItems.value);
		}
	}
};

// const addPreviewItemToCart = () => {
// 	if (previewItem.value) {
// 		toggleDataItemSelected( {item: previewItem.value } );
// 		previewItem.value = null;
// 		isSliderResourcesOpen.value = true;
// 	}
// };

onMounted(async () => {
	const { q } = route.query;
	searchTerm.value = q?.toString() ?? searchTerm.value;

	xddDatasets.value = await getXDDSets();
	if (xddDatasets.value.length > 0 && xddDataset.value === null) {
		xddDatasetSelectionChanged(xddDatasets.value[xddDatasets.value.length - 1]);
		xddDatasets.value.push(ResourceType.ALL);
	}

	// initially, all search results are dirty; need re-fetch
	Object.values(ResourceType).forEach((key) => {
		dirtyResults.value[key as string] = true;
	});

	await executeSearch();

	// done with initial fetch for the currently selected tab, so reset
	dirtyResults.value[resultType.value] = false;
});

onUnmounted(() => {
	query.reset();
});
</script>

<style scoped>
.data-explorer-container {
	left: 0px;
	top: 0px;
	right: 0px;
	display: flex;
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: column;
	background-color: var(--surface-ground);
}

.secondary-header {
	display: flex;
	padding: 1rem 0;
	justify-content: space-between;
	align-items: center;
	height: var(--nav-bar-height);
}

.button-group {
	display: flex;
}

.button-group button {
	display: flex;
	align-items: center;
	text-decoration: none;
	background: transparent;
	border: 1px solid black;
	cursor: pointer;
	padding: 0.25rem;
	margin: auto;
	border-left-width: 0;
}

.button-group button:first-child {
	border-left-width: 1px;
	border-top-left-radius: 0.5rem;
	border-bottom-left-radius: 0.5rem;
}

.button-group button:last-child {
	border-top-right-radius: 0.5rem;
	border-bottom-right-radius: 0.5rem;
}

.button-group button:hover {
	background: var(--gray-50);
}

.button-group button.active {
	background: var(--primary-color-lighter);
	cursor: default;
}

.data-explorer-container .facets-and-results-container {
	position: relative;
	height: calc(100vh - 50px - var(--nav-bar-height));
	display: flex;
	flex-grow: 1;
	min-height: 0;
}

.results-content {
	margin: 0 10px;
}

.facets-panel {
	margin-top: 10px;
	overflow-y: auto;
}

.data-explorer-container .results-content {
	display: flex;
	flex-direction: column;
	flex: 1;
}

.preview-slider {
	margin-right: 1px;
}

.resources-slider {
	z-index: 1;
}

.slider {
	background: var(--surface-card);
}
</style>
