<template>
	<div class="data-explorer-container">
		<modal-header
			:nav-back-label="'Back to Home'"
			:select-label="'Add selected items'"
			:selected-search-items="selectedSearchItems"
			@close="onClose"
			@selection="onSelection"
		/>
		<div class="explorer-content">
			<search-bar
				class="search-bar"
				:enable-multi-term-search="false"
				:show-sorted-results="true"
				@search-text-changed="filterData"
			>
				<template #sort>
					<toggle-button
						:value="sortedResults"
						:label="'Sorted Results'"
						@change="toggleSortedResults"
					/>
				</template>
			</search-bar>
			<search
				class="search"
				:filtered-data-items="filteredDataItems"
				:enable-multiple-selection="true"
				:selected-search-items="selectedSearchItems"
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
						<search-bar
							class="search-bar"
							:enable-multi-term-search="true"
							:search-label="''"
							:enable-clear-button="false"
							:enable-search-button="false"
							:search-placeholder="'known terms...'"
							@search-text-changed="addKnownTerm"
						/>
					</div>
					<button
						type="button"
						class="co-occurrence-matrix-btn"
						:disabled="knownTerms.length !== 2"
					>
						<i class="fa-light fa-table-cells" />&nbsp;co-occurrence matrix
					</button>
				</template>
			</search>
			<simple-pagination
				:current-page-length="filteredDataItemsCount"
				:page-count="pageCount"
				:page-size="pageSize"
				@next-page="nextPage"
				@prev-page="prevPage"
			/>
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

import { fetchData, getXDDSets } from '@/services/data';
import { SearchParameters, SearchResults } from '@/types/common';

// FIXME: page count is not taken into consideration
// FIXME: consider facets
// FIXME: improve the search bar with auto-complete
// FIXME: remove SASS

export default defineComponent({
	name: 'DataExplorer',
	components: {
		Search,
		ModalHeader,
		SimplePagination,
		SearchBar,
		DropdownButton,
		ToggleButton
	},
	setup() {
		const filteredDataItems = ref<SearchResults[]>([]);
		const selectedSearchItems = ref<string[]>([]);
		const filter = ref<string[]>([]);
		return {
			filter,
			filteredDataItems,
			selectedSearchItems
		};
	},
	data: () => ({
		pageCount: 0,
		pageSize: 50,
		// xdd
		xddDatasets: [] as string[],
		xddDataset: null as string | null,
		knownTerms: [] as string[],
		sortedResults: false // disable sorted results to enable pagination
	}),
	computed: {
		navBackLabel() {
			return 'Back to Home';
		},
		filteredDataItemsCount() {
			// FIXME: this should depend on the selected results tab (e.g., ALL, Models, Articles)
			let total = 0;
			this.filteredDataItems.forEach((resList) => {
				total += resList?.results.length ?? 0;
			});
			return total;
		}
	},
	watch: {
		filter() {
			this.refresh();
		},
		knownTerms() {
			this.refresh();
		},
		sortedResults() {
			this.refresh();
		}
	},
	async mounted() {
		this.xddDatasets = await getXDDSets();
		if (this.xddDatasets.length > 0 && this.xddDataset === null) {
			this.xddDatasets.push('all');
			this.xddDataset = 'all';
		}

		this.refresh();
	},
	methods: {
		toggleSortedResults() {
			this.sortedResults = !this.sortedResults;
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
			this.xddDataset = newDataset === 'all' ? null : newDataset;
		},
		// retrieves filtered data items list
		async fetchDataItemList() {
			// get the filtered data

			// const options = {
			//   from: this.pageCount * this.pageSize,
			//   size: this.pageSize
			// };

			const searchParams: SearchParameters = {
				xdd: {
					known_terms: this.knownTerms,
					dataset: this.xddDataset === 'all' ? null : this.xddDataset,
					pageSize: this.pageSize,
					enablePagination: !this.sortedResults
				}
			};

			// FIXME: should we allow multiple search terms?
			const searchTerm = this.filter.length > 0 ? this.filter[0] : '';

			const allData: SearchResults[] = await fetchData(searchTerm, searchParams);
			this.filteredDataItems = allData;
		},
		async refresh() {
			this.pageCount = 0;
			await this.fetchDataItemList();
		},
		filterData(filterTerms: string[]) {
			this.filter = cloneDeep(filterTerms);
		},
		async onClose() {
			this.$router.push('/');
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
					.find((item) => item.id === itemID || item.title === itemID);
				if (dataitem === undefined) {
					return;
				}
				this.selectedSearchItems = [...this.selectedSearchItems, itemID];
			}
		},
		onSelection() {
			console.log(`received ${this.selectedSearchItems.length.toString()} items!`);
		},
		removeKnownTerm(term: string) {
			this.knownTerms = this.knownTerms.filter((t) => t !== term);
		},
		addKnownTerm(knownTerms: string[]) {
			if (knownTerms.length === 0) {
				this.knownTerms = [];
			}
			knownTerms.forEach((term) => {
				if (!this.knownTerms.includes(term)) {
					this.knownTerms.push(term);
				}
			});
		}
	}
});
</script>

<style lang="scss" scoped>
@import '../styles/variables.scss';

.data-explorer-container {
	position: relative;
	box-sizing: border-box;
	overflow: hidden;

	.explorer-content {
		.search {
			height: calc(100% - 100px);
		}

		display: flex;
		flex-direction: column;
		flex: 1;
	}

	.xdd-known-terms {
		margin-left: 1rem;
		display: flex;

		:deep(.search-bar-container input) {
			margin: 4px;
			padding: 4px;
			min-width: 100px;
		}
	}

	.co-occurrence-matrix-btn {
		background-color: darkcyan;
		padding: 4px;
		padding-left: 8px;
		padding-right: 8px;
		margin: 4px;
	}
}
</style>
