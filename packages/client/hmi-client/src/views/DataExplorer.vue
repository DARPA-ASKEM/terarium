<template>
	<div class="data-explorer-container">
		<div class="left-content">
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
						<template #sort>
							<!--
								KEEP this code because we may need to add pagination later on!
							<toggle-button
								:value="rankedResults"
								:label="'Ranked Results'"
								@change="toggleRankedResults"
							/>
							-->
						</template>
						<template #params>
							<toggle-button
								:value="isSearchTitle"
								:label="'Title'"
								@change="toggleIsSearchTitle"
							/>
						</template>
					</search-bar>
				</template>
			</modal-header>
			<div class="nav-bar">
				<div class="nav-left-container">
					<ul class="nav-left">
						<li>
							<button
								type="button"
								:class="{ active: resultType === ResourceType.XDD }"
								@click="onResultTypeChanged(ResourceType.XDD)"
							>
								Papers
							</button>
						</li>
						<li>
							<button
								type="button"
								:class="{ active: resultType === ResourceType.MODEL }"
								@click="onResultTypeChanged(ResourceType.MODEL)"
							>
								Models
							</button>
						</li>
					</ul>
					<template v-if="resultType === ResourceType.XDD">
						<div class="xdd-known-terms">
							<auto-complete
								:focus-input="false"
								:style-results="true"
								:placeholder-color="'gray'"
								:placeholder-message="'dictionary name...'"
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
					</template>
				</div>
				<div>
					<ul class="nav-right">
						<li>
							<button
								type="button"
								:class="{ active: viewType === ViewType.LIST }"
								@click="viewType = ViewType.LIST"
							>
								List
							</button>
						</li>
						<li>
							<button
								type="button"
								:class="{ active: viewType === ViewType.MATRIX }"
								@click="viewType = ViewType.MATRIX"
							>
								Matrix
							</button>
						</li>
						<li>
							<button
								type="button"
								:class="{ active: viewType === ViewType.GRAPH }"
								@click="viewType = ViewType.GRAPH"
							>
								Graph
							</button>
						</li>
					</ul>
				</div>
			</div>
			<div class="facets-and-results-container">
				<template v-if="viewType === ViewType.LIST">
					<facets-panel
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
						<simple-pagination
							:current-page-length="resultsCount"
							:page-count="pageCount"
							:page-size="pageSize"
							@next-page="nextPage"
							@prev-page="prevPage"
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
			</div>
		</div>
		<drilldown-panel
			:active-tab-id="activeDrilldownTab"
			:has-transition="false"
			:hide-close="true"
			:is-open="activeDrilldownTab !== null"
			:tabs="drilldownTabs"
			@close="
				() => {
					activeDrilldownTab = null;
				}
			"
		>
			<template #content>
				<selected-resources-options-pane
					:selected-search-items="selectedSearchItems"
					@close="onClose"
				/>
			</template>
		</drilldown-panel>
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';

import ModalHeader from '@/components/data-explorer/modal-header.vue';
import SearchResultsList from '@/components/data-explorer/search-results-list.vue';
import SearchResultsMatrix from '@/components/data-explorer/search-results-matrix.vue';
import SimplePagination from '@/components/data-explorer/simple-pagination.vue';
import SearchBar from '@/components/data-explorer/search-bar.vue';
import DropdownButton from '@/components/widgets/dropdown-button.vue';
import ToggleButton from '@/components/widgets/toggle-button.vue';
import AutoComplete from '@/components/widgets/autocomplete.vue';
import FacetsPanel from '@/components/data-explorer/facets-panel.vue';
import SelectedResourcesOptionsPane from '@/components/drilldown-panel/selected-resources-options-pane.vue';
import DrilldownPanel from '@/components/drilldown-panel.vue';

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
import { isModel, isXDDArticle, validate } from '@/utils/data-util';

import IconClose16 from '@carbon/icons-vue/es/close/16';

// FIXME: page count is not taken into consideration

const DRILLDOWN_TABS = [
	{
		name: 'Manage Resources',
		id: 'selected-resources',
		icon: 'fa-gear'
	}
];

const emit = defineEmits(['hide', 'show-overlay', 'hide-overlay']);

const dataItems = ref<SearchResults[]>([]);
const selectedSearchItems = ref<ResultType[]>([]);
const searchTerm = ref('');
const query = useQueryStore();
const activeDrilldownTab = ref<string | null>('selected-resources');
const resources = useResourcesStore();
const drilldownTabs = DRILLDOWN_TABS;

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

const searchXDDDictionaries = (q: string) =>
	new Promise((resolve) => {
		const suggestionResults: string[] = [];
		if (q.length < 1) resolve(suggestionResults); // early exit
		resolve(
			xddDictionaries.value.map((dic) => dic.name).filter((dictName) => dictName.includes(q))
		);
	});

const updateResultType = (newResultType: string) => {
	resultType.value = newResultType;
};

// const toggleRankedResults = () => {
// 	rankedResults.value = !rankedResults.value;
// };

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

	const isValidDOI = validate(searchTerm.value);

	// start with initial search parameters
	const searchParams: SearchParameters = {
		xdd: {
			dict: dictNames.value,
			dataset: xddDataset.value === ResourceType.ALL ? null : xddDataset.value,
			max: pageSize.value,
			perPage: pageSize.value,
			fullResults: !rankedResults.value,
			doi: isValidDOI ? searchTerm.value : undefined,
			title: isSearchTitle.value && !isValidDOI ? searchTerm.value : undefined,
			facets: true // include facets aggregation data in the search results
		}
	};

	// first: fetch the data unfiltered by facets
	const allData: SearchResults[] = await fetchData(searchTerm.value, searchParams);

	//
	// extend search parameters by converting facet filters into proper search parameters
	//

	const xddSearchParams = searchParams?.xdd || {};
	// transform facet filters into xdd search parameters
	clientFilters.value.clauses.forEach((clause) => {
		if (XDD_FACET_FIELDS.includes(clause.field)) {
			// NOTE: special case
			if (clause.field === YEAR) {
				// FIXME: handle the case when multiple years are selected
				const val = (clause.values as string[]).join(',');
				const formattedVal = `${val}-01-01`; // must be in ISO format; 2020-01-01
				xddSearchParams.min_published = formattedVal;
				xddSearchParams.max_published = formattedVal;
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
	const allDataFilteredWithFacets: SearchResults[] = await fetchData(
		searchTerm.value,
		searchParams
	);

	// the list of results displayed in the data explorer is always the final filtered data
	dataItems.value = allDataFilteredWithFacets;

	// final step: cache the facets and filteredFacets objects
	calculateFacets(allData, allDataFilteredWithFacets);

	emit('hide-overlay');
};

const prevPage = () => {
	// this won't work with XDD since apparently there is no way to navigate results backward
	pageCount.value -= 1;
	fetchDataItemList();
};

const nextPage = () => {
	pageCount.value += 1;
	// check if previous results "hasMore" and continue fetching results
	// note the next_page URL would need to be cached and passed down to the fetch service
	//  but this is only valid for XDD,
	//  and thus we need to cache both the original URL and the pagination one
	fetchDataItemList();
};

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

const removeDictName = (term: string) => {
	dictNames.value = dictNames.value.filter((t) => t !== term);
};

const addDictName = (term: string) => {
	if (term === undefined || term === '') return;
	if (!dictNames.value.includes(term)) {
		dictNames.value = [...dictNames.value, term]; // clone to trigger reactivity
	}
};

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

watch(dictNames, () => {
	// re-fetch data from the server, apply filters, and re-calculate the facets
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
:root {
	--header-height: 50px;
	--footer-height: 50px;
	--nav-bar-height: 50px;
}

.data-explorer-container {
	position: absolute;
	left: 0px;
	top: 0px;
	right: 0px;
	display: flex;
	width: 100vw;
	height: 100%;
}

.data-explorer-container .left-content {
	display: flex;
	flex-direction: column;
	flex-grow: 1;
	overflow: auto;
}

.data-explorer-container .nav-bar {
	display: flex;
	justify-content: space-between;
	background-color: lightgray;
	padding: 0.5rem;
	align-items: center;
	height: var(--nav-bar-height);
}

.data-explorer-container .header {
	height: var(--header-height);
}

.data-explorer-container .nav-left-container {
	display: flex;
	align-items: center;
}

.data-explorer-container .nav-left {
	list-style-type: none;
	margin: 0;
	padding: 0;
	overflow: hidden;
	margin-right: 2rem;
	margin-left: 5rem;
}

.data-explorer-container .nav-left li {
	float: left;
}

.data-explorer-container .nav-left li button {
	display: block;
	color: black;
	text-align: center;
	padding: 8px 12px;
	text-decoration: none;
	border: none;
	background-color: transparent;
	font-size: larger;
}

.data-explorer-container .nav-left li button:hover:not(.active) {
	text-decoration: underline;
	border: none;
	cursor: pointer;
}

.data-explorer-container .nav-left li button.active {
	text-decoration: underline;
	font-weight: bold;
	border: none;
}

.data-explorer-container .nav-right {
	list-style-type: none;
	margin-right: 2rem;
	margin-left: 5rem;
}

.data-explorer-container .nav-right li {
	float: left;
}

.data-explorer-container .nav-right li button {
	display: block;
	color: black;
	text-align: center;
	text-decoration: none;
	background: transparent;
	padding: 8px 12px;
	border: 1px solid black;
}

.data-explorer-container .nav-right li button:hover {
	background: var(--un-color-black-5);
	cursor: pointer;
}

.data-explorer-container .nav-right li button.active {
	background: white;
	font-weight: bold;
}

.data-explorer-container .facets-and-results-container {
	background-color: var(--un-color-body-surface-background);
	height: calc(100vh - var(--footer-height) - var(--nav-bar-height));
	display: flex;
	flex-grow: 1;
	overflow: auto;
}

.data-explorer-container .results-content {
	display: flex;
	flex-direction: column;
	flex: 1;
}

.data-explorer-container :deep(.dropdown-btn) {
	max-width: 200px;
	width: 200px;
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

.data-explorer-container :deep(.search-button) {
	background-color: var(--un-color-accent);
}

.data-explorer-container :deep(.search-button-disabled) {
	background-color: var(--un-color-accent);
	cursor: not-allowed;
	color: gray;
}

.data-explorer-container :deep(.clear-button-disabled) {
	background-color: gray;
	cursor: not-allowed;
}
</style>
