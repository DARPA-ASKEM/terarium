<template>
	<div class="data-explorer-container">
		<div class="secondary-header">
			<span class="section-label">View only</span>
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

			<span class="section-label">View as</span>
			<div class="button-group">
				<button
					type="button"
					:class="{ active: viewType === ViewType.LIST }"
					@click="viewType = ViewType.LIST"
				>
					List
				</button>
				<button
					type="button"
					:class="{ active: viewType === ViewType.MATRIX }"
					@click="viewType = ViewType.MATRIX"
				>
					Matrix
				</button>
			</div>
		</div>
		<div class="facets-and-results-container">
			<template v-if="viewType === ViewType.LIST">
				<facets-panel
					class="facets-panel"
					:facets="facets"
					:filtered-facets="filteredFacets"
					:result-type="resultType"
				/>
				<div class="results-content">
					<search-results-list
						:data-items="dataItems"
						:result-type="resultType"
						:selected-search-items="selectedSearchItems"
						:search-term="searchTerm"
						@toggle-data-item-selected="toggleDataItemSelected"
					/>
				</div>
			</template>
			<template v-if="viewType === ViewType.MATRIX">
				<div class="results-content">
					<search-results-matrix
						:data-items="dataItems"
						:result-type="resultType"
						:selected-search-items="selectedSearchItems"
						:dict-names="dictNames"
						@toggle-data-item-selected="toggleDataItemSelected"
					/>
				</div>
			</template>
			<selected-resources-options-pane
				class="selected-resources-pane"
				:selected-search-items="selectedSearchItems"
				@remove-item="toggleDataItemSelected"
			/>
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';

import SearchResultsList from '@/components/data-explorer/search-results-list.vue';
import SearchResultsMatrix from '@/components/data-explorer/search-results-matrix.vue';
import FacetsPanel from '@/components/data-explorer/facets-panel.vue';
import SelectedResourcesOptionsPane from '@/components/drilldown-panel/selected-resources-options-pane.vue';

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
import { cloneDeep, intersectionBy, isEmpty, max, min, unionBy } from 'lodash';
import { Dataset } from '@/types/Dataset';
import { LocationQuery, useRoute } from 'vue-router';

// FIXME: page count is not taken into consideration

const props = defineProps<{
	query?: LocationQuery;
}>();
const searchQuery = computed(() => props.query);
const route = useRoute();
const dataItems = ref<SearchResults[]>([]);
const dataItemsUnfiltered = ref<SearchResults[]>([]);
const selectedSearchItems = ref<ResultType[]>([]);
const searchTerm = ref('');
const query = useQueryStore();
const resources = useResourcesStore();

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
		xdd: {
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
	const xddSearchParams = searchParamsWithFacetFilters?.xdd || {};
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
	const modelSearchParams = searchParamsWithFacetFilters?.model || {
		filters: clientFilters.value
	};
	const datasetSearchParams = searchParamsWithFacetFilters?.dataset || {
		filters: clientFilters.value
	};

	// update search parameters object
	searchParamsWithFacetFilters.xdd = xddSearchParams;
	searchParamsWithFacetFilters.model = modelSearchParams;
	searchParamsWithFacetFilters.dataset = datasetSearchParams;

	// fetch the data
	const { allData, allDataFilteredWithFacets } = await fetchData(
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
};

const toggleDataItemSelected = (item: ResultType) => {
	let foundIndx = -1;
	selectedSearchItems.value.forEach((searchItem, indx) => {
		if (isModel(item) && isModel(searchItem)) {
			const itemAsModel = item as Model;
			const searchItemAsModel = searchItem as Model;
			if (searchItemAsModel.id === itemAsModel.id) foundIndx = indx;
		} else if (isDataset(item) && isDataset(searchItem)) {
			const itemAsDataset = item as Dataset;
			const searchItemAsDataset = searchItem as Dataset;
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
	searchTerm.value = newQuery?.toString() ?? searchTerm.value;
	// search term has changed, so all search results are dirty; need re-fetch
	Object.values(ResourceType).forEach((key) => {
		dirtyResults.value[key as string] = true;
	});

	// re-fetch data from the server, apply filters, and re-calculate the facets
	await executeSearch();
	dirtyResults.value[resultType.value] = false;
});

const updateResultType = async (newResultType: string) => {
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
	width: 100vw;
	height: 100%;
	display: flex;
	flex-direction: column;
	background-color: var(--un-color-body-surface-background);
}

.secondary-header {
	display: flex;
	padding: 10px;
	align-items: center;
	height: var(--nav-bar-height);
}

.secondary-header .section-label {
	margin-right: 5px;
	margin-left: 20px;
}

.data-explorer-container .header {
	height: var(--header-height);
}

.button-group {
	display: flex;
}

.bottom-padding {
	padding-bottom: 2px;
}

.button-group button {
	display: flex;
	align-items: center;
	text-decoration: none;
	background: transparent;
	padding: 5px 10px;
	border: 1px solid black;
	cursor: pointer;
	border-left-width: 0;
	height: 40px;
}

.button-group button:first-child {
	border-left-width: 1px;
	border-top-left-radius: 3px;
	border-bottom-left-radius: 3px;
}

.button-group button:last-child {
	border-top-right-radius: 3px;
	border-bottom-right-radius: 3px;
}

.button-group button:hover {
	background: var(--un-color-black-5);
}

.button-group button.active {
	background: white;
	cursor: default;
}

.data-explorer-container .facets-and-results-container {
	height: calc(100vh - 50px - var(--nav-bar-height));
	display: flex;
	flex-grow: 1;
	min-height: 0;
	gap: 10px;
	/* Add space to the right of the selected assets column */
	padding-right: 10px;
}

.facets-panel {
	margin-top: 10px;
	width: 250px;
	overflow-y: auto;
}

.data-explorer-container .results-content {
	display: flex;
	flex-direction: column;
	flex: 1;
	align-items: center;
}

.xdd-known-terms {
	display: flex;
}

.xdd-known-terms .flex-aligned-item {
	display: flex;
	align-items: center;
	color: var(--un-color-accent-darker);
}

.xdd-known-terms .flex-aligned-item-delete-btn {
	color: red;
}

.xdd-known-terms .flex-aligned-item-delete-btn:hover {
	cursor: pointer;
}

.xdd-known-terms :deep(.search-bar-container input) {
	margin: 4px;
	padding: 4px;
	min-width: 100px;
}

.selected-resources-pane {
	width: 250px;
}
</style>
