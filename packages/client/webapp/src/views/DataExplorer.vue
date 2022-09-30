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
			<search-bar class="search-bar" @search-text-changed="filterData" />
			<search
				class="search"
				:filtered-data-items="filteredDataItems"
				:enable-multiple-selection="true"
				:selected-search-items="selectedSearchItems"
				@toggle-data-item-selected="toggleDataItemSelected"
			/>
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

import ModalHeader from '@/components/data-explorer/modal-header.vue';
import Search from '@/components/data-explorer/search.vue';
import SimplePagination from '@/components/data-explorer/simple-pagination.vue';
import SearchBar from '@/components/data-explorer/search-bar.vue';

import { Datacube, DatacubeFilterAttributes } from '@/types/Datacube';
import { ArticleFilterAttributes, XDDArticle } from '@/types/XDD';

import fetchData from '@/services/data';

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
		SearchBar
	},
	setup() {
		const filteredDataItems = ref<(Datacube | XDDArticle)[]>([]);
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
		pageSize: 50
	}),
	computed: {
		navBackLabel() {
			return 'Back to Home';
		},
		filteredDataItemsCount() {
			return this.filteredDataItems.length;
		}
	},
	watch: {
		filter() {
			this.refresh();
		}
	},
	mounted() {
		this.refresh();
	},
	methods: {
		prevPage() {
			this.pageCount -= 1;
			this.fetchDataItemList();
		},
		nextPage() {
			this.pageCount += 1;
			this.fetchDataItemList();
		},
		// retrieves filtered data items list
		async fetchDataItemList() {
			// get the filtered data

			// const options = {
			//   from: this.pageCount * this.pageSize,
			//   size: this.pageSize
			// };

			const allData: (Datacube | XDDArticle)[] = await fetchData(true /* xdd */);
			const filteredData: (Datacube | XDDArticle)[] = [];

			const addToFilteredData = (items: (Datacube | XDDArticle)[]) => {
				// should only include unique items
				filteredData.push(...items);
			};

			// apply filter to allData according to search type: for datacubes or articles
			//  match against relevant search attributes, DatacubeFilterAttributes
			if (allData.length > 0 && this.filter.length > 0) {
				const firstDataItem = allData[0];
				const receviedDatacubes = firstDataItem.outputs !== undefined;
				this.filter.forEach((filterTerm) => {
					if (receviedDatacubes) {
						// datacubes
						DatacubeFilterAttributes.forEach((datacubeAttr) => {
							const resultsAsDatacubes = allData as Datacube[];
							const fr = resultsAsDatacubes.filter((d) => d[datacubeAttr].includes(filterTerm));
							addToFilteredData(fr);
						});
					} else {
						// articles

						ArticleFilterAttributes.forEach((articleAttr) => {
							const resultsAsArticles = allData as XDDArticle[];
							const fr = resultsAsArticles.filter((d) =>
								d[articleAttr].toLowerCase().includes(filterTerm)
							);
							addToFilteredData(fr);
						});
					}
				});
			}

			this.filteredDataItems = this.filter.length > 0 ? filteredData : allData;
		},
		async refresh() {
			this.pageCount = 0;
			await this.fetchDataItemList();
		},
		filterData(filterTerms: string[]) {
			this.filter = filterTerms;
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
				const dataitem = this.filteredDataItems.find(
					(item) => item.id === itemID || item.title === itemID
				);
				if (dataitem === undefined) {
					return;
				}
				this.selectedSearchItems = [...this.selectedSearchItems, itemID];
			}
		},
		onSelection() {
			console.log(`received ${this.selectedSearchItems.length.toString()} items!`);
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
}
</style>
