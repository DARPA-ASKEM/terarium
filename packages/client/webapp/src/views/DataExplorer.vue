<template>
	<div class="data-explorer-container">
		<div class="left-content">
			<modal-header :nav-back-label="'Back'" class="header" @close="onClose">
				<template #content>
					<search-bar :enable-multi-term-search="false" @search-text-changed="filterData">
						<template #dataset>
							<dropdown-button
								:inner-button-label="'Dataset'"
								:is-dropdown-left-aligned="true"
								:items="xddDatasets"
								:selected-item="xddDataset"
								class="dataset-container"
								@item-selected="xddDatasetSelectionChanged"
							/>
						</template>
						<template #sort>
							<toggle-button
								:value="rankedResults"
								:label="'Ranked Results'"
								@change="toggleRankedResults"
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
								:class="{ active: resultType === ResourceType.ALL }"
								@click="onResultTypeChanged(ResourceType.ALL)"
							>
								All
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
						<li>
							<button
								type="button"
								:class="{ active: resultType === ResourceType.XDD }"
								@click="onResultTypeChanged(ResourceType.XDD)"
							>
								Articles
							</button>
						</li>
					</ul>
					<template v-if="resultType === ResourceType.XDD">
						<div class="xdd-known-terms">
							<auto-complete
								:focus-input="true"
								:style-results="true"
								:placeholder-color="'gray'"
								:placeholder-message="'dictionary name...'"
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
							:data-items="filteredDataItems"
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
							:data-items="filteredDataItems"
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
				<selected-resources-options-pane :selected-search-items="selectedSearchItems" />
			</template>
		</drilldown-panel>
	</div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';

import { cloneDeep } from 'lodash';
import ModalHeader from '@/components/data-explorer/modal-header.vue';
import SearchResultsList from '@/components/data-explorer/search-results-list.vue';
import SearchResultsMatrix from '@/components/data-explorer/search-results-matrix.vue';
import SimplePagination from '@/components/data-explorer/simple-pagination.vue';
import SearchBar from '@/components/data-explorer/search-bar.vue';
import DropdownButton from '@/components/widgets/dropdown-button.vue';
import ToggleButton from '@/components/widgets/toggle-button.vue';
import AutoComplete from '@/components/widgets/autocomplete.vue';
import FacetsPanel from '@/components/data-explorer/facets-panel.vue';

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
import { XDD_RESULT_DEFAULT_PAGE_SIZE, XDDArticle, XDDDictionary } from '@/types/XDD';
import { Model } from '@/types/Model';
import useQueryStore from '@/stores/query';
import filtersUtil from '@/utils/filters-util';
import { applyFacetFiltersToData, isModel, isXDDArticle } from '@/utils/data-util';
import DrilldownPanel from '@/components/drilldown-panel.vue';
import SelectedResourcesOptionsPane from '@/components/drilldown-panel/selected-resources-options-pane.vue';

// FIXME: page count is not taken into consideration
// FIXME: remove SASS

const DRILLDOWN_TABS = [
	{
		name: 'Manage Resources',
		id: 'selected-resources',
		icon: 'fa-gear'
	}
];

export default defineComponent({
	name: 'DataExplorer',
	components: {
		SearchResultsList,
		SearchResultsMatrix,
		ModalHeader,
		SimplePagination,
		SearchBar,
		DropdownButton,
		ToggleButton,
		FacetsPanel,
		AutoComplete,
		DrilldownPanel,
		SelectedResourcesOptionsPane
	},
	emits: ['hide', 'show-overlay', 'hide-overlay'],
	setup() {
		const dataItems = ref<SearchResults[]>([]);
		const filteredDataItems = ref<SearchResults[]>([]); // after applying facet-based filters
		const selectedSearchItems = ref<ResultType[]>([]);
		const filter = ref<string[]>([]);
		const query = useQueryStore();
		const activeDrilldownTab = ref<string | null>('selected-resources');

		return {
			filter,
			dataItems,
			filteredDataItems,
			selectedSearchItems,
			query,
			drilldownTabs: DRILLDOWN_TABS,
			activeDrilldownTab
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
		resultType: ResourceType.ALL as string,
		viewType: ViewType.LIST as string,
		ResourceType,
		ViewType
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
			if (this.xddDataset !== newDataset) {
				this.xddDataset = newDataset;
			}
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
					dict_names: this.dictNames,
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
		filterData(filterTerms: string) {
			this.filter = cloneDeep(filterTerms);
		},
		onClose() {
			this.$emit('hide');
		},
		isDataItemSelected(item: ResultType) {
			// FIXME: refactor as util func
			return this.selectedSearchItems.find((searchItem) => {
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
		},
		toggleDataItemSelected(item: ResultType) {
			const itemID = (item as Model).id || (item as XDDArticle).title;
			if (this.isDataItemSelected(item)) {
				this.selectedSearchItems = this.selectedSearchItems.filter(
					(searchItem) =>
						(searchItem as Model).id !== itemID && (searchItem as XDDArticle).title !== itemID
				);
			} else {
				const dataitem = this.filteredDataItems
					.map((res) => res.results)
					.flat()
					.find(
						(searchItem) =>
							(searchItem as Model).id === itemID || (searchItem as XDDArticle).title === itemID
					);
				if (dataitem === undefined) {
					return;
				}
				this.selectedSearchItems = [...this.selectedSearchItems, item];
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
		},
		onResultTypeChanged(newResultType: string) {
			this.updateResultType(newResultType);
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
	right: 0px;
	display: flex;

	--header-height: 50px;
	--footer-height: 50px;
	--nav-bar-height: 50px;

	.left-content {
		display: flex;
		flex-direction: column;
		flex-grow: 1;

		.header {
			height: var(--header-height);
		}

		.nav-bar {
			display: flex;
			justify-content: space-between;
			background-color: lightgray;
			padding: 0.5rem;
			align-items: center;
			height: var(--nav-bar-height);

			.nav-left-container {
				display: flex;
				align-items: center;

				.nav-left {
					list-style-type: none;
					margin: 0;
					padding: 0;
					overflow: hidden;
					margin-right: 2rem;
					margin-left: 5rem;

					li {
						float: left;
					}

					li button {
						display: block;
						color: black;
						text-align: center;
						padding: 8px 12px;
						text-decoration: none;
						border: none;
						background-color: transparent;
						font-size: larger;
					}

					li button:hover:not(.active) {
						text-decoration: underline;
						border: none;
						cursor: pointer;
					}

					li button.active {
						text-decoration: underline;
						font-weight: bold;
						border: none;
					}
				}
			}

			.nav-right {
				list-style-type: none;
				margin-right: 2rem;
				margin-left: 5rem;

				li {
					float: left;
				}

				li button {
					display: block;
					color: black;
					text-align: center;
					text-decoration: none;
					background: transparent;
					padding: 8px 12px;
					border: 1px solid black;
				}

				li button:hover {
					background: var(--un-color-black-5);
					cursor: pointer;
				}

				li button.active {
					background: white;
					font-weight: bold;
				}
			}
		}
	}

	.facets-and-results-container {
		background-color: $background-light-2;
		height: calc(100vh - var(--footer-height) - var(--nav-bar-height));
		display: flex;
	}

	.results-content {
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

	:deep(.dropdown-btn) {
		max-width: 200px;
		width: 200px;
	}
}
</style>
