<script setup lang="ts">
import { defineEmits, ref, computed } from 'vue';
import ModalHeader from '@/components/data-explorer/modal-header.vue';
import FacetsPanel from '@/components/data-explorer/facets-panel.vue';
import SearchBar from '@/components/data-explorer/search-bar.vue';
import ToggleButton from '@/components/widgets/toggle-button.vue';
import Search from '@/components/data-explorer/search.vue';
import DropdownButton from '@/components/widgets/dropdown-button.vue';
import AutoComplete from '@/components/widgets/autocomplete.vue';
import SimplePagination from '@/components/data-explorer/simple-pagination.vue';

import { Facets, ResourceType, SearchParameters, SearchResults } from '@/types/common';
import { XDDArticle, XDDDictionary, XDD_RESULT_DEFAULT_PAGE_SIZE } from '@/types/XDD';
import { cloneDeep } from 'lodash';
import { fetchData } from '@/services/data';
import { Model } from '@/types/Model';

const selectedSearchItems = ref<string[]>([]);
const facets = ref<Facets>({});
const filteredFacets = ref<Facets>({});
const resultType = ref<string>(ResourceType.ALL);
const rankedResults = ref(true);
const filteredDataItems = ref<SearchResults[]>([]);
const xddDatasets = ref<string[]>([]);
const xddDataset = ref<string | null>(null);
const dictNames = ref<string[]>([]);
const pageCount = ref(0);
const pageSize = ref(XDD_RESULT_DEFAULT_PAGE_SIZE);
const filter = ref<string[]>([]);
const dataItems = ref<SearchResults[]>([]);
const xddDictionaries = ref<XDDDictionary[]>([]);

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

const emit = defineEmits(['hide', 'show-overlay', 'hide-overlay']);

function addDictName(term: string) {
	if (term === undefined || term === '') return;
	if (!dictNames.value.includes(term)) {
		dictNames.value.push(term);
	}
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

function removeDictName(term: string) {
	dictNames.value = dictNames.value.filter((t) => t !== term);
}

function searchXDDDictionaries(query: string) {
	return new Promise((resolve) => {
		const suggestionResults: string[] = [];
		if (query.length < 1) {
			resolve(suggestionResults);
		}
		resolve(
			xddDictionaries.value.map((dic) => dic.name).filter((dictName) => dictName.includes(query))
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
									<i class="fa fa-fw fa-close" />
								</span>
							</div>
						</div>
						<button
							type="button"
							class="co-occurrence-matrix-btn"
							:disabled="dictNames.length !== 2"
						>
							<i class="fa-solid fa-table-cells-large" />&nbsp;co-occurrence matrix
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

<style lang="css" scoped>
@import '@/styles/util.css';

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
</style>
