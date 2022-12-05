<template>
	<div class="data-explorer-container">
		<modal-header :nav-back-label="'Back'" class="header" @close="onClose">
			<template #content>
				<search-bar :focus-input="true" @search-text-changed="filterData">
					<template #dataset>
						<dropdown-button
							:inner-button-label="'Dataset'"
							:is-dropdown-left-aligned="true"
							:items="xddDatasets"
							:selected-item="xddDataset"
							@item-selected="xddDatasetSelectionChanged"
						/>
					</template>
					<template #params>
						<toggle-button
							:value="isSearchTitle"
							:label="'Searching by document title'"
							@change="toggleIsSearchTitle"
						/>
					</template>
				</search-bar>
			</template>
		</modal-header>
		<div class="secondary-header">
			<span class="section-label">View only</span>
			<div class="button-group">
				<button
					type="button"
					:class="{ active: resultType === ResourceType.XDD }"
					@click="onResultTypeChanged(ResourceType.XDD)"
				>
					<component :is="getResourceTypeIcon(ResourceType.XDD)" />
					Papers
				</button>
				<button
					type="button"
					:class="{ active: resultType === ResourceType.MODEL }"
					@click="onResultTypeChanged(ResourceType.MODEL)"
				>
					<component :is="getResourceTypeIcon(ResourceType.MODEL)" />
					Models
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

			<!--
			<span v-if="resultType === ResourceType.XDD" class="section-label">
				Filter by XDD Dictionary
			</span>
			<div v-if="resultType === ResourceType.XDD" class="xdd-known-terms">
				<auto-complete
					:focus-input="false"
					:style-results="true"
					:placeholder-color="'gray'"
					:placeholder-message="'Search XDD dictionaries'"
					:search-fn="searchXDDDictionaries"
					@item-selected="addDictName"
				/>
				<div v-for="term in dictNames" :key="term" class="flex-aligned-item">
					{{ term }}
					<span class="flex-aligned-item-delete-btn" @click.stop="removeDictName(term)">
						<IconClose16 />
					</span>
				</div>
			</div>
			-->
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
						@toggle-data-item-selected="toggleDataItemSelected"
					/>
					<div class="results-count-label">Showing {{ resultsCount }} item(s).</div>
					<!--
					<simple-pagination
						:current-page-length="resultsCount"
						:page-count="pageCount"
						:page-size="pageSize"
						@next-page="nextPage"
						@prev-page="prevPage"
					/>
					-->
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
				@close="onClose"
			/>
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';

import ModalHeader from '@/components/data-explorer/modal-header.vue';
import SearchResultsList from '@/components/data-explorer/search-results-list.vue';
import SearchResultsMatrix from '@/components/data-explorer/search-results-matrix.vue';
import SearchBar from '@/components/data-explorer/search-bar.vue';
import DropdownButton from '@/components/widgets/dropdown-button.vue';
import ToggleButton from '@/components/widgets/toggle-button.vue';
// import AutoComplete from '@/components/widgets/autocomplete.vue';
// import SimplePagination from '@/components/data-explorer/simple-pagination.vue';
import FacetsPanel from '@/components/data-explorer/facets-panel.vue';
import SelectedResourcesOptionsPane from '@/components/drilldown-panel/selected-resources-options-pane.vue';

import { fetchData, getXDDSets, getXDDDictionaries } from '@/services/data';
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
	XDDDictionary,
	FACET_FIELDS as XDD_FACET_FIELDS,
	YEAR
} from '@/types/XDD';
import { Model } from '@/types/Model';
import useQueryStore from '@/stores/query';
import filtersUtil from '@/utils/filters-util';
import useResourcesStore from '@/stores/resources';
import { getResourceTypeIcon, isModel, isXDDArticle, validate } from '@/utils/data-util';
import { isEmpty, max, min } from 'lodash';

// import IconClose16 from '@carbon/icons-vue/es/close/16';

// FIXME: page count is not taken into consideration

const emit = defineEmits(['hide', 'show-overlay', 'hide-overlay']);

const dataItems = ref<SearchResults[]>([]);
const selectedSearchItems = ref<ResultType[]>([]);
const searchTerm = ref('');
const query = useQueryStore();
const resources = useResourcesStore();

const pageCount = ref(0);
const pageSize = ref(XDD_RESULT_DEFAULT_PAGE_SIZE);
// xdd
const xddDatasets = ref<string[]>([]);
const dictNames = ref<string[]>([]);
const rankedResults = ref(true); // disable sorted/ranked results to enable pagination
const isSearchTitle = ref(false); // is the input search term represents a document identifier such as title or DOI
const xddDictionaries = ref<XDDDictionary[]>([]);
// facets
const facets = ref<Facets>({});
const filteredFacets = ref<Facets>({});
//
const resultType = ref<string>(ResourceType.XDD);
const viewType = ref<string>(ViewType.LIST);

const xddDataset = computed(() => resources.xddDataset);
const clientFilters = computed(() => query.clientFilters);
const resultsCount = computed(() => {
	let total = 0;
	if (resultType.value === ResourceType.ALL) {
		// count the results from all subsystems
		dataItems.value.forEach((res) => {
			const count = res?.hits ?? res?.results.length;
			total += count;
		});
	} else {
		// only return the results count for the selected subsystems
		const resList = dataItems.value.find((res) => res.searchSubsystem === resultType.value);
		if (resList) {
			// eslint-disable-next-line no-unsafe-optional-chaining
			total += resList?.hits ?? resList?.results.length;
		}
	}
	return total;
});

const updateResultType = (newResultType: string) => {
	resultType.value = newResultType;
};

const toggleIsSearchTitle = () => {
	isSearchTitle.value = !isSearchTitle.value;
};

const xddDatasetSelectionChanged = (newDataset: string) => {
	if (xddDataset.value !== newDataset) {
		resources.setXDDDataset(newDataset);
	}
};

const calculateFacets = (unfilteredData: SearchResults[], filteredData: SearchResults[]) => {
	// retrieves filtered & unfiltered facet data
	// const defaultFilters = { clauses: [] };
	facets.value = getFacets(unfilteredData, resultType.value /* , defaultFilters */);
	filteredFacets.value = getFacets(filteredData, resultType.value /* , this.clientFilters */);
};

const fetchDataItemList = async () => {
	// const options = {
	//   from: this.pageCount * this.pageSize,
	//   size: this.pageSize
	// };

	emit('show-overlay');

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
			dataset: xddDataset.value === ResourceType.ALL ? null : xddDataset.value,
			max: pageSize.value,
			perPage: pageSize.value,
			fullResults: !rankedResults.value,
			doi: isValidDOI ? searchWords : undefined,
			title: isSearchTitle.value && !isValidDOI ? searchWords : undefined,
			includeHighlights: true,
			inclusive: matchAll,
			facets: true // include facets aggregation data in the search results
		}
	};

	// first: fetch the data unfiltered by facets
	const allData: SearchResults[] = await fetchData(searchWords, searchParams);

	//
	// extend search parameters by converting facet filters into proper search parameters
	//

	const xddSearchParams = searchParams?.xdd || {};
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
	const modelSearchParams = searchParams?.model || {
		filters: clientFilters.value
	};

	// update search parameters object
	searchParams.xdd = xddSearchParams;
	searchParams.model = modelSearchParams;

	// fetch second time with facet filtered applied
	const allDataFilteredWithFacets: SearchResults[] = await fetchData(searchWords, searchParams);

	// the list of results displayed in the data explorer is always the final filtered data
	dataItems.value = allDataFilteredWithFacets;

	// final step: cache the facets and filteredFacets objects
	calculateFacets(allData, allDataFilteredWithFacets);

	emit('hide-overlay');
};

// const prevPage = () => {
// 	// this won't work with XDD since apparently there is no way to navigate results backward
// 	pageCount.value -= 1;
// 	fetchDataItemList();
// };

// const nextPage = () => {
// 	pageCount.value += 1;
// 	// check if previous results "hasMore" and continue fetching results
// 	// note the next_page URL would need to be cached and passed down to the fetch service
// 	//  but this is only valid for XDD,
// 	//  and thus we need to cache both the original URL and the pagination one
// 	fetchDataItemList();
// };

const refresh = async () => {
	pageCount.value = 0;
	await fetchDataItemList();
};

const filterData = (filterTerm: string) => {
	searchTerm.value = filterTerm;
	// re-fetch data from the server, apply filters, and re-calculate the facets
	refresh();
};

const onClose = () => {
	emit('hide');
};

// FIXME: refactor as util func
const isDataItemSelected = (item: ResultType) =>
	selectedSearchItems.value.find((searchItem) => {
		if (isModel(item)) {
			const itemAsModel = item as Model;
			const searchItemAsModel = searchItem as Model;
			return searchItemAsModel.id === itemAsModel.id;
		}
		if (isXDDArticle(item)) {
			const itemAsArticle = item as XDDArticle;
			const searchItemAsArticle = searchItem as XDDArticle;
			return searchItemAsArticle.title === itemAsArticle.title;
		}
		return false;
	});

const toggleDataItemSelected = (item: ResultType) => {
	const itemID = (item as Model).id || (item as XDDArticle).title;
	if (isDataItemSelected(item)) {
		selectedSearchItems.value = selectedSearchItems.value.filter(
			(searchItem) =>
				(searchItem as Model).id !== itemID && (searchItem as XDDArticle).title !== itemID
		);
	} else {
		selectedSearchItems.value = [...selectedSearchItems.value, item];
	}
};

// const removeDictName = (term: string) => {
// 	dictNames.value = dictNames.value.filter((t) => t !== term);
// };

// const addDictName = (term: string) => {
// 	if (term === undefined || term === '') return;
// 	if (!dictNames.value.includes(term)) {
// 		dictNames.value = [...dictNames.value, term]; // clone to trigger reactivity
// 	}
// };

// const searchXDDDictionaries = (q: string) =>
// 	new Promise((resolve) => {
// 		const suggestionResults: string[] = [];
// 		if (q.length < 1) resolve(suggestionResults); // early exit
// 		resolve(
// 			xddDictionaries.value.map((dic) => dic.name).filter((dictName) => dictName.includes(q))
// 		);
// 	});

const onResultTypeChanged = (newResultType: string) => {
	updateResultType(newResultType);
};

// @ts-ignore
// eslint-disable-next-line @typescript-eslint/no-unused-vars
watch(clientFilters, (n, o) => {
	if (filtersUtil.isEqual(n, o)) return;
	// data has not changed; the user just changed one of the facet filters
	refresh(); // this will trigger facet re-calculation
});

watch(dictNames, () => {
	refresh();
});

watch(rankedResults, () => {
	// re-fetch data from the server, apply filters, and re-calculate the facets
	refresh();
});

watch(resultType, () => {
	// data has not changed; the user has just switched the result tab, e.g., from ALL to Articles
	// re-calculate the facets
	// REVIEW
	refresh();
});

onMounted(async () => {
	xddDatasets.value = await getXDDSets();
	xddDictionaries.value = (await getXDDDictionaries()) as XDDDictionary[];
	if (xddDatasets.value.length > 0 && xddDataset.value === null) {
		xddDatasetSelectionChanged(xddDatasets.value[xddDatasets.value.length - 1]);
		xddDatasets.value.push(ResourceType.ALL);
	}

	refresh();
});
</script>

<style scoped>
.data-explorer-container {
	position: absolute;
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

.data-explorer-container .results-content .results-count-label {
	font-weight: bold;
	margin: 4px;
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
