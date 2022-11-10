<script setup lang="ts">
import { defineEmits, ref, computed, watch, onMounted } from 'vue';
import ModalHeader from '@/components/data-explorer/modal-header.vue';
import FacetsPanel from '@/components/data-explorer/facets-panel.vue';
import SearchBar from '@/components/data-explorer/search-bar.vue';
import ToggleButton from '@/components/widgets/toggle-button.vue';
import DropdownButton from '@/components/widgets/dropdown-button.vue';
import AutoComplete from '@/components/widgets/autocomplete.vue';
import SimplePagination from '@/components/data-explorer/simple-pagination.vue';
import IconScatterMatrix16 from '@carbon/icons-vue/es/scatter-matrix/16';
import IconClose16 from '@carbon/icons-vue/es/close/16';

import { Facets, ResourceType, SearchParameters, SearchResults } from '@/types/common';
import { XDDArticle, XDDDictionary, XDD_RESULT_DEFAULT_PAGE_SIZE } from '@/types/XDD';
import { cloneDeep } from 'lodash';
import { fetchData, getXDDDictionaries, getXDDSets } from '@/services/data';
import { Model } from '@/types/Model';
import filtersUtil from '@/utils/filters-util';
import useQueryStore from '@/stores/query';
import { applyFacetFiltersToData } from '@/utils/data-util';
import { getFacets } from '@/utils/facets';
import Search from '@/components/data-explorer/search.vue';

const selectedSearchItems = ref<string[]>([]);
const facets = ref<Facets>({});
const filteredFacets = ref<Facets>({});
const resultType = ref<string>(ResourceType.ALL);
const rankedResults = ref(true); // disable sorted/ranked results to enable pagination
const filteredDataItems = ref<SearchResults[]>([]); // after applying facet-based filters
const xddDatasets = ref<string[]>([]);
const xddDataset = ref<string | null>(null);
const dictNames = ref<string[]>([]);
const pageCount = ref(0);
const pageSize = ref(XDD_RESULT_DEFAULT_PAGE_SIZE);
const filter = ref<string[]>([]);
const dataItems = ref<SearchResults[]>([]);
const xddDictionaries = ref<XDDDictionary[]>([]);
const query = useQueryStore();

const emit = defineEmits(['hide', 'show-overlay', 'hide-overlay']);

const resultsCount = computed(() => {
	let total = 0;
	if (resultType.value === ResourceType.ALL) {
		// count the results from all subsystems
		filteredDataItems.value.forEach((res) => {
			const count = res?.hits ?? res?.results.length;
			total += count;
		});
	} else {
		// only return the results count for the selected subsystems
		const resList = filteredDataItems.value.find((res) => res.searchSubsystem === resultType.value);
		if (resList) {
			// eslint-disable-next-line no-unsafe-optional-chaining
			total += resList?.hits ?? resList?.results.length;
		}
	}
	return total;
});
const clientFilters = computed(() => query.clientFilters);

function addDictName(term: string) {
	if (term === undefined || term === '') return;
	if (!dictNames.value.includes(term)) {
		dictNames.value.push(term);
	}
}

function applyFiltersToData() {
	const allDataCloned = cloneDeep(dataItems.value);
	applyFacetFiltersToData(allDataCloned, resultType.value, clientFilters.value);
	filteredDataItems.value = allDataCloned;
}

function calculateFacets() {
	// retrieves filtered & unfiltered facet data
	// const defaultFilters = { clauses: [] };
	facets.value = getFacets(dataItems.value, resultType.value /* , defaultFilters */);
	filteredFacets.value = getFacets(
		filteredDataItems.value,
		resultType.value /* , this.clientFilters */
	);
}

async function fetchDataItemList() {
	// get the filtered data

	// const options = {
	//   from: this.pageCount * this.pageSize,
	//   size: this.pageSize
	// };

	emit('show-overlay');

	const searchParams: SearchParameters = {
		xdd: {
			known_terms: dictNames.value,
			dataset: xddDataset.value === ResourceType.ALL ? null : xddDataset.value,
			pageSize: pageSize.value,
			enablePagination: !rankedResults.value
		}
	};

	// FIXME: should we allow multiple search terms?
	const searchTerm = filter.value.length > 0 ? filter.value[0] : '';

	const allData: SearchResults[] = await fetchData(searchTerm, searchParams);
	dataItems.value = allData;

	emit('hide-overlay');
}

function filterData(filterTerms: string[]) {
	filter.value = cloneDeep(filterTerms);
}

function isDataItemSelected(id: string) {
	return selectedSearchItems.value.find((item) => item === id) !== undefined;
}

function nextPage() {
	pageCount.value++;
	// check if previous results "hasMore" and continue fetching results
	// note the next_page URL would need to be cached and passed down to the fetch service
	//  but this is only valid for XDD,
	//  and thus we need to cache both the original URL and the pagination one
	fetchDataItemList();
}

function onClose() {
	emit('hide');
}

function onSelection() {
	console.log(`received ${selectedSearchItems.value.length.toString()} items!`);
}

function prevPage() {
	// this won't work with XDD since apparently there is no way to navigate results backward
	pageCount.value--;
	fetchDataItemList();
}

async function refresh() {
	pageCount.value = 0;
	await fetchDataItemList();
}

function removeDictName(term: string) {
	dictNames.value = dictNames.value.filter((t) => t !== term);
}

function searchXDDDictionaries(searchQuery: string) {
	return new Promise((resolve) => {
		const suggestionResults: string[] = [];
		if (searchQuery.length < 1) {
			resolve(suggestionResults);
		}
		resolve(
			xddDictionaries.value
				.map((dic) => dic.name)
				.filter((dictName) => dictName.includes(searchQuery))
		);
	});
}

function toggleDataItemSelected(itemID: string) {
	if (isDataItemSelected(itemID)) {
		selectedSearchItems.value = selectedSearchItems.value.filter((item) => item !== itemID);
	} else {
		const dataitem = filteredDataItems.value
			.map((res) => res.results)
			.flat()
			.find((item) => (item as Model).id === itemID || (item as XDDArticle).title === itemID);
		if (dataitem === undefined) {
			return;
		}
		selectedSearchItems.value.push(itemID);
	}
}

function toggleRankedResults() {
	rankedResults.value = !rankedResults.value;
}

function updateResultType(newResultType: string) {
	resultType.value = newResultType;
}

function xddDatasetSelectionChanged(newDataset: string) {
	xddDataset.value = newDataset === ResourceType.ALL ? null : newDataset;
}

onMounted(async () => {
	xddDatasets.value = await getXDDSets();
	xddDictionaries.value = (await getXDDDictionaries()) as XDDDictionary[];
	if (xddDatasets.value.length > 0 && xddDataset.value === null) {
		xddDatasets.value.push(ResourceType.ALL);
		xddDataset.value = ResourceType.ALL;
	}
	refresh();
});

watch(clientFilters, (newValue, oldValue) => {
	if (filtersUtil.isEqual(newValue, oldValue)) {
		// data has not changed; the user just changed one of the facet filters
		return;
	}
	applyFiltersToData(); // this will trigger facet re-calculation
});

watch(dataItems, () => {
	// new data has arrived, apply filters, if any
	applyFiltersToData(); // this will trigger facet re-calculation
});

watch(dictNames, () => {
	// re-fetch data from the server, apply filters, and re-calculate the facets
	refresh();
});

watch(filter, () => {
	// re-fetch data from the server, apply filters, and re-calculate the facets
	refresh();
});

watch(filteredDataItems, () => {
	calculateFacets();
});

watch(rankedResults, () => {
	// re-fetch data from the server, apply filters, and re-calculate the facets
	refresh();
});

watch(resultType, () => {
	// data has not changed; the user has just switched the result tab, e.g., from ALL to Articles
	// re-calculate the facets
	calculateFacets();
});
</script>

<template>
	<div class="data-explorer-container">
		<modal-header
			:nav-back-label="'Back to Home'"
			:select-label="'Add selected items'"
			:selected-search-items="selectedSearchItems"
			@close="onClose"
			@selection="onSelection"
		/>
		<div class="flex h-100">
			<div class="flex h-100 facets-panel-container">
				<facets-panel
					:facets="facets"
					:filtered-facets="filteredFacets"
					:result-type="resultType"
				/>
			</div>
			<div class="explorer-content flex-grow-1 h-100">
				<search-bar
					class="search-bar"
					:enable-multi-term-search="false"
					:show-sorted-results="true"
					@search-text-changed="filterData"
				>
					<template #sort>
						<toggle-button
							:value="rankedResults"
							:label="'Ranked Results'"
							@change="toggleRankedResults"
						/>
					</template>
				</search-bar>
				<search
					:data-items="filteredDataItems"
					:result-type="resultType"
					:results-count="resultsCount"
					:enable-multiple-selection="true"
					:selected-search-items="selectedSearchItems"
					@result-type-changed="updateResultType"
					@toggle-data-item-selected="toggleDataItemSelected"
				>
					<template #xdd>
						<dropdown-button
							:inner-button-label="'Dataset'"
							:is-dropdown-left-aligned="true"
							:items="xddDatasets"
							:selected-item="xddDataset"
							@item-selected="xddDatasetSelectionChanged"
						/>
						<div class="xdd-known-terms">
							<auto-complete
								:focus-input="true"
								:style-results="true"
								:placeholder-color="'gray'"
								:placeholder-message="'dict name...'"
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
						<button
							type="button"
							class="co-occurrence-matrix-btn"
							:disabled="dictNames.length !== 2"
						>
							<IconScatterMatrix16 />&nbsp;co-occurrence matrix
						</button>
					</template>
				</search>
				<simple-pagination
					:current-page-length="resultsCount"
					:page-count="pageCount"
					:page-size="pageSize"
					@next-page="nextPage"
					@prev-page="prevPage"
				/>
			</div>
		</div>
	</div>
</template>

<style scoped>
.data-explorer-container {
	position: absolute;
	left: 0px;
	top: 0px;
	box-sizing: border-box;
	width: 100%;
	height: calc(100vh - 50px);
}

.facets-panel-container {
	background-color: #f2f2f2;
	height: calc(100vh - 50px);
}

.explorer-content {
	display: flex;
	flex-direction: column;
	flex: 1;
	height: 100%;
}

.xdd-known-terms {
	margin-left: 1rem;
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

.xdd-known-terms .flex-aligned-item-delete-btn :hover {
	cursor: pointer;
}

.data-explorer-container :deep(.search-bar-container input) {
	margin: 4px;
	padding: 4px;
	min-width: 100px;
}

.co-occurrence-matrix-btn {
	background-color: darkcyan;
	padding-left: 8px;
	padding-right: 8px;
}

/* Flex box utility classes */
.flex {
	display: flex;
	flex-direction: row;
	height: 100%;
}

.flex-grow-1 {
	flex-grow: 1;
}
</style>
