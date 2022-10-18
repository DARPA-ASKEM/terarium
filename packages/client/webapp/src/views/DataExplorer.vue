<template>
	<div class="data-explorer-container" style="z-index: 3">
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

<script lang="ts">
import { defineComponent, ref } from 'vue';

import { cloneDeep } from 'lodash';
import ModalHeader from '@/components/data-explorer/modal-header.vue';
import Search from '@/components/data-explorer/search.vue';
import SimplePagination from '@/components/data-explorer/simple-pagination.vue';
import SearchBar from '@/components/data-explorer/search-bar.vue';
import DropdownButton from '@/components/widgets/dropdown-button.vue';
import ToggleButton from '@/components/widgets/toggle-button.vue';
import AutoComplete from '@/components/widgets/autocomplete.vue';
import FacetsPanel from '@/components/data-explorer/facets-panel.vue';

import { fetchData, getXDDSets, getXDDDictionaries } from '@/services/data';
import { SearchParameters, SearchResults, Facets, ResourceType } from '@/types/common';
import { getFacets } from '@/utils/facets';
import { XDD_RESULT_DEFAULT_PAGE_SIZE, XDDArticle, XDDDictionary } from '@/types/XDD';
import { Model } from '@/types/Model';
import useQueryStore from '@/stores/query';
import filtersUtil from '@/utils/filters-util';
import { applyFacetFiltersToData } from '@/utils/data-util';

// FIXME: page count is not taken into consideration
// FIXME: remove SASS

export default defineComponent({
	name: 'DataExplorer',
	components: {
		Search,
		ModalHeader,
		SimplePagination,
		SearchBar,
		DropdownButton,
		ToggleButton,
		FacetsPanel,
		AutoComplete
	},
	emits: ['hide', 'show-overlay', 'hide-overlay'],
	setup() {
		const dataItems = ref<SearchResults[]>([]);
		const filteredDataItems = ref<SearchResults[]>([]); // after applying facet-based filters
		const selectedSearchItems = ref<string[]>([]);
		const filter = ref<string[]>([]);
		const query = useQueryStore();
		return {
			filter,
			dataItems,
			filteredDataItems,
			selectedSearchItems,
			query
		};
	},
	data: () => ({
		pageCount: 0,
		pageSize: XDD_RESULT_DEFAULT_PAGE_SIZE,
		// xdd
		xddDatasets: [] as string[],
		xddDataset: null as string | null,
		dictNames: [] as string[],
		rankedResults: true, // disable sorted/ranked results to enable pagination
		xddDictionaries: [] as XDDDictionary[],
		// facets
		facets: {} as Facets,
		filteredFacets: {} as Facets,
		//
		resultType: ResourceType.ALL as string
	}),
	computed: {
		resultsCount() {
			let total = 0;
			if (this.resultType === ResourceType.ALL) {
				// count the results from all subsystems
				this.filteredDataItems.forEach((res) => {
					const count = res?.hits ?? res?.results.length;
					total += count;
				});
			} else {
				// only return the results count for the selected subsystems
				const resList = this.filteredDataItems.find(
					(res) => res.searchSubsystem === this.resultType
				);
				if (resList) {
					// eslint-disable-next-line no-unsafe-optional-chaining
					total += resList?.hits ?? resList?.results.length;
				}
			}
			return total;
		},
		clientFilters() {
			return this.query.clientFilters;
		}
	},
	watch: {
		filter() {
			// re-fetch data from the server, apply filters, and re-calculate the facets
			this.refresh();
		},
		clientFilters(n, o) {
			if (filtersUtil.isEqual(n, o)) return;
			// data has not changed; the user just changed one of the facet filters
			this.applyFiltersToData(); // this will trigger facet re-calculation
		},
		dictNames() {
			// re-fetch data from the server, apply filters, and re-calculate the facets
			this.refresh();
		},
		rankedResults() {
			// re-fetch data from the server, apply filters, and re-calculate the facets
			this.refresh();
		},
		resultType() {
			// data has not changed; the user has just switched the result tab, e.g., from ALL to Articles
			// re-calculate the facets
			this.calculateFacets();
		},
		dataItems() {
			// new data has arrived, apply filters, if any
			this.applyFiltersToData(); // this will trigger facet re-calculation
		},
		filteredDataItems() {
			this.calculateFacets();
		}
	},
	async mounted() {
		this.xddDatasets = await getXDDSets();
		this.xddDictionaries = (await getXDDDictionaries()) as XDDDictionary[];
		if (this.xddDatasets.length > 0 && this.xddDataset === null) {
			this.xddDatasets.push(ResourceType.ALL);
			this.xddDataset = ResourceType.ALL;
		}

		this.refresh();
	},
	methods: {
		searchXDDDictionaries(query: string) {
			return new Promise((resolve) => {
				const suggestionResults: string[] = [];
				if (query.length < 1) resolve(suggestionResults); // early exit
				resolve(
					this.xddDictionaries.map((dic) => dic.name).filter((dictName) => dictName.includes(query))
				);
			});
		},
		updateResultType(newResultType: string) {
			this.resultType = newResultType;
		},
		toggleRankedResults() {
			this.rankedResults = !this.rankedResults;
		},
		prevPage() {
			// this won't work with XDD since apparently there is no way to navigate results backward
			this.pageCount -= 1;
			this.fetchDataItemList();
		},
		nextPage() {
			this.pageCount += 1;
			// check if previous results "hasMore" and continue fetching results
			// note the next_page URL would need to be cached and passed down to the fetch service
			//  but this is only valid for XDD,
			//  and thus we need to cache both the original URL and the pagination one
			this.fetchDataItemList();
		},
		xddDatasetSelectionChanged(newDataset: string) {
			this.xddDataset = newDataset === ResourceType.ALL ? null : newDataset;
		},
		calculateFacets() {
			// retrieves filtered & unfiltered facet data
			// const defaultFilters = { clauses: [] };
			this.facets = getFacets(this.dataItems, this.resultType /* , defaultFilters */);
			this.filteredFacets = getFacets(
				this.filteredDataItems,
				this.resultType /* , this.clientFilters */
			);
		},
		// retrieves filtered data items list
		async fetchDataItemList() {
			// get the filtered data

			// const options = {
			//   from: this.pageCount * this.pageSize,
			//   size: this.pageSize
			// };

			this.$emit('show-overlay');

			const searchParams: SearchParameters = {
				xdd: {
					known_terms: this.dictNames,
					dataset: this.xddDataset === ResourceType.ALL ? null : this.xddDataset,
					pageSize: this.pageSize,
					enablePagination: !this.rankedResults
				}
			};

			// FIXME: should we allow multiple search terms?
			const searchTerm = this.filter.length > 0 ? this.filter[0] : '';

			const allData: SearchResults[] = await fetchData(searchTerm, searchParams);
			this.dataItems = allData;

			this.$emit('hide-overlay');
		},
		applyFiltersToData() {
			const allDataCloned = cloneDeep(this.dataItems);
			applyFacetFiltersToData(allDataCloned, this.resultType, this.clientFilters);
			this.filteredDataItems = allDataCloned;
		},
		async refresh() {
			this.pageCount = 0;
			await this.fetchDataItemList();
		},
		filterData(filterTerms: string[]) {
			this.filter = cloneDeep(filterTerms);
		},
		onClose() {
			this.$emit('hide');
		},
		isDataItemSelected(id: string) {
			return this.selectedSearchItems.find((item) => item === id) !== undefined;
		},
		toggleDataItemSelected(itemID: string) {
			if (this.isDataItemSelected(itemID)) {
				this.selectedSearchItems = this.selectedSearchItems.filter((item) => item !== itemID);
			} else {
				const dataitem = this.filteredDataItems
					.map((res) => res.results)
					.flat()
					.find((item) => (item as Model).id === itemID || (item as XDDArticle).title === itemID);
				if (dataitem === undefined) {
					return;
				}
				this.selectedSearchItems = [...this.selectedSearchItems, itemID];
			}
		},
		onSelection() {
			console.log(`received ${this.selectedSearchItems.length.toString()} items!`);
		},
		removeDictName(term: string) {
			this.dictNames = this.dictNames.filter((t) => t !== term);
		},
		addDictName(term: string) {
			if (term === undefined || term === '') return;
			if (!this.dictNames.includes(term)) {
				this.dictNames = [...this.dictNames, term]; // clone to trigger reactivity
			}
		}
	}
});
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/util.scss';

.data-explorer-container {
	position: absolute;
	left: 0px;
	top: 0px;
	box-sizing: border-box;
	width: 100%;
	height: calc(100vh - 50px);

	.facets-panel-container {
		background-color: $background-light-2;
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

		.flex-aligned-item {
			display: flex;
			align-items: center;
			color: var(--un-color-accent-darker);

			.flex-aligned-item-delete-btn {
				color: red;
			}
			.flex-aligned-item-delete-btn:hover {
				cursor: pointer;
			}
		}

		:deep(.search-bar-container input) {
			margin: 4px;
			padding: 4px;
			min-width: 100px;
		}
	}

	.co-occurrence-matrix-btn {
		background-color: darkcyan;
		padding-left: 8px;
		padding-right: 8px;
	}
}
</style>
