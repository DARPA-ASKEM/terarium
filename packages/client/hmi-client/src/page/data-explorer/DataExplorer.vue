<template>
	<main>
		<section class="flex h-full relative overflow-hidden">
			<tera-slider-panel content-width="240px" direction="left" header="Filters" v-model:is-open="isSliderFacetsOpen">
			</tera-slider-panel>
			<div class="results-content">
				<div class="search">
					<nav>
						<SelectButton
							:model-value="assetType"
							@change="if ($event.value) changeAssetType($event.value);"
							:options="assetOptions"
							option-value="value"
							option-label="label"
						/>
						<div class="toggles" v-if="assetType !== AssetType.Model">
							<span>
								<label class="mr-2">Source</label>
								<Dropdown
									v-model="chosenSource"
									:options="sourceOptions"
									@change="if (assetType === AssetType.Dataset) executeNewQuery();"
								/>
							</span>
							<tera-filter-bar :topic-options="topicOptions" @filter-changed="executeNewQuery" />
						</div>
					</nav>
					<tera-searchbar
						ref="searchBarRef"
						@toggle-search-by-example="searchByExampleModalToggled"
						:source="chosenSource"
						:show-suggestions="false"
					/>
					<!-- <aside class="suggested-terms" v-if="!isEmpty(terms)">
						Suggested terms:
						<Chip v-for="term in terms" :key="term" removable remove-icon="pi pi-times">
							<span @click="searchBarRef?.addToQuery(term)">{{ term }}</span>
						</Chip>
					</aside> -->
				</div>
				<tera-search-results-list
					:data-items="resultsToShow"
					:resource-type="resourceType"
					:search-term="searchTerm"
					:is-loading="isLoading"
					:source="chosenSource"
					:doc-count="docCount"
					@toggle-data-item-selected="toggleDataItemSelected"
				/>
			</div>
			<tera-preview-panel
				:content-width="`${sliderWidth.slice(0, -1)} - 20px)`"
				tab-width="0"
				direction="right"
				v-model:preview-item="previewItem"
				:source="chosenSource"
				:resource-type="resourceType"
				:search-term="searchTerm"
			/>
		</section>
	</main>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import SelectButton from 'primevue/selectbutton';
import Dropdown from 'primevue/dropdown';
import { fetchData } from '@/services/data';
import { searchByAssetType } from '@/services/search';
import { ResourceType, ResultType, SearchParameters, SearchResults } from '@/types/common';
import { DatasetSource } from '@/types/search';
import type { Source } from '@/types/search';
import useQueryStore from '@/stores/query';
import filtersUtil from '@/utils/filters-util';
import { getResourceID, isDataset, isModel } from '@/utils/data-util';
import { cloneDeep, intersectionBy, isEmpty, isEqual, unionBy } from 'lodash';
import { useRoute } from 'vue-router';
import TeraPreviewPanel from '@/page/data-explorer/components/tera-preview-panel.vue';
import TeraSearchResultsList from '@/page/data-explorer/components/tera-search-results-list.vue';
import { AssetType } from '@/types/Types';
import TeraSearchbar from '@/components/navbar/tera-searchbar.vue'; // import Chip from 'primevue/chip';
import { DatasetSearchParams } from '@/types/Dataset';
import { ModelSearchParams } from '@/types/Model';
import { useSearchByExampleOptions } from './search-by-example';
import TeraFilterBar from './components/tera-filter-bar.vue';

// FIXME: page count is not taken into consideration

const route = useRoute();
const queryStore = useQueryStore();

const { searchByExampleOptions, searchByExampleItem } = useSearchByExampleOptions();

const dataItems = ref<SearchResults[]>([]);
const dataItemsUnfiltered = ref<SearchResults[]>([]);
const searchResults = ref<SearchResults[]>([]);

const selectedSearchItems = ref<ResultType[]>([]);
const executeSearchByExample = ref(false);
const previewItem = ref<ResultType | null>(null);
const searchTerm = ref('');
// default slider state
const isSliderFacetsOpen = ref(false);
const isSliderResourcesOpen = ref(false);
// facets
const docCount = ref(0);
let modelTotal: number = 0;
let documentTotal: number = 0;
let datasetTotal: number = 0;

const isLoading = ref<boolean>(false);
// optimize search performance: only fetch as needed
const dirtyResults = ref<{ [resourceType: string]: boolean }>({});

const clientFilters = computed(() => queryStore.clientFilters);

const assetType = ref(AssetType.Document);
const assetOptions = ref([
	{ label: 'Documents', value: AssetType.Document },
	{ label: 'Models', value: AssetType.Model },
	{ label: 'Datasets', value: AssetType.Dataset }
]);
// TODO: Get rid of this once we have fully moved to the new search (search-by-asset-type)
// This is here now for preservation of the hacky services/data.ts
// Child components should be updated to accept AssetType instead of ResourceType
const resourceType = computed(() => {
	if (assetType.value === AssetType.Document) {
		return ResourceType.DOCUMENT;
	}
	if (assetType.value === AssetType.Model) {
		return ResourceType.MODEL;
	}
	return ResourceType.DATASET;
});

const topicOptions = ref([
	{ label: 'Covid-19', value: 'xdd-covid-19' },
	{ label: 'Climate Weather', value: 'climate-change-modeling' }
]);

const sourceOptions = ref<Source[]>(Object.values(DatasetSource));
const chosenSource = ref<Source>(DatasetSource.TERARIUM);

const sliderWidth = computed(() => (isSliderFacetsOpen.value ? 'calc(50% - 120px)' : 'calc(50% - 20px)'));

// Chooses source for search
const resultsToShow = computed(() => {
	if (assetType.value === AssetType.Document || assetType.value === AssetType.Model) {
		return searchResults.value;
	}
	return dataItems.value;
});

// close resources if preview opens
watch(isSliderResourcesOpen, () => {
	if (isSliderResourcesOpen.value) {
		previewItem.value = null;
	}
});

function changeAssetType(type: AssetType) {
	assetType.value = type;
	if (assetType.value === AssetType.Dataset) {
		sourceOptions.value = Object.values(DatasetSource);
		chosenSource.value = DatasetSource.TERARIUM;
	}
}

const mergeResultsKeepRecentDuplicates = (existingResults: SearchResults[], newResults: SearchResults[]) => {
	const mergeId = 'searchSubsystem';
	const mergedResults = unionBy(existingResults, newResults, mergeId);
	// replace existing old results with new ones, if any
	const overlapping = intersectionBy(existingResults, newResults, mergeId);
	overlapping.forEach((res) => {
		const existingOldIndex = mergedResults.findIndex((u) => u?.searchSubsystem === res?.searchSubsystem);
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
	if (!dirtyResults.value[resourceType.value]) return;

	isLoading.value = true;
	//
	// search across artifects: XDD, HMI SERVER DB including models, projects, etc.
	//
	// this requires hitting the backend twice to grab filtered and filtered data (and facets)
	//

	let searchWords = searchTerm.value;

	const matchAll = !isEmpty(searchWords) && searchWords.startsWith('"') && searchWords.endsWith('"');
	const allSearchTerms = searchWords.split(' ');
	if (matchAll && !isEmpty(allSearchTerms)) {
		// multiple words are provided as search term and the user requested to match all of them
		// the XDD api expects all search terms to be comma-separated if the user requested inclusive results
		searchWords = allSearchTerms.join(',');
	}

	// start with initial search parameters
	const searchParams: SearchParameters = {
		model: {},
		[ResourceType.DATASET]: {
			source: chosenSource.value as DatasetSource,
			topic: 'covid-19'
		}
	};

	// handle the search-by-example for finding related documents, models, and/or datasets
	if (executeSearchByExample.value && searchByExampleItem.value) {
		const id = getResourceID(searchByExampleItem.value) as string;

		//
		// find related models (which utilizes the TDS provenance API through the HMI server)
		//
		if (isModel(searchByExampleItem.value) && searchParams.model) {
			searchParams.model.related_search_enabled = executeSearchByExample.value;
			searchParams.model.related_search_id = id;
			assetType.value = AssetType.Model;
		}
		//
		// find related datasets (which utilizes the TDS provenance API through the HMI server)
		//
		if (isDataset(searchByExampleItem.value) && searchParams.dataset) {
			searchParams.dataset.related_search_enabled = executeSearchByExample.value;
			searchParams.dataset.related_search_id = id;
			assetType.value = AssetType.Dataset;
		}
	}
	const searchParamsWithFacetFilters: SearchParameters = cloneDeep(searchParams);

	let modelSearchParams: ModelSearchParams;
	if (searchParamsWithFacetFilters?.[ResourceType.MODEL]?.filters) {
		modelSearchParams = searchParamsWithFacetFilters[ResourceType.MODEL];
	} else {
		modelSearchParams = { filters: clientFilters.value };
	}
	let datasetSearchParams: DatasetSearchParams;
	if (searchParamsWithFacetFilters?.[ResourceType.DATASET]?.filters) {
		datasetSearchParams = searchParamsWithFacetFilters[ResourceType.DATASET];
	} else {
		datasetSearchParams = {
			filters: clientFilters.value,
			source: chosenSource.value as DatasetSource,
			topic: 'covid-19' // TODO - this should be dynamic
		};
	}

	// update search parameters object
	searchParamsWithFacetFilters.model = modelSearchParams;
	searchParamsWithFacetFilters.dataset = datasetSearchParams;

	// TODO: Make this the main method of fetching data
	// Works for models and documents
	if (assetType.value !== AssetType.Dataset) {
		searchResults.value = [
			{
				results: await searchByAssetType(searchWords, AssetType.Document),
				searchSubsystem: ResourceType.DOCUMENT
			},
			{
				results: await searchByAssetType(searchWords, AssetType.Model),
				searchSubsystem: ResourceType.MODEL
			}
		];
	}

	// TODO: Remove old method of fetching data
	// fetch the data
	const { allData, allDataFilteredWithFacets } = await fetchData(
		searchWords,
		searchParams,
		searchParamsWithFacetFilters,
		resourceType.value
	);

	// cache unfiltered data
	dataItemsUnfiltered.value = mergeResultsKeepRecentDuplicates(dataItemsUnfiltered.value, allData);
	// the list of results displayed in the data explorer is always the final filtered data
	dataItems.value = mergeResultsKeepRecentDuplicates(dataItems.value, allDataFilteredWithFacets);
	// final step: cache the facets and filteredFacets objects
	// calculateFacets(allData, allDataFilteredWithFacets);

	let total = 0;
	allData.forEach((res) => {
		const count = res?.hits ?? res?.results.length;
		total += count;
	});

	// Note that we only do xDD document and Dataset searches on demand and don't have a way of knowing in advance how many results there are
	if (resourceType.value === ResourceType.DOCUMENT) {
		documentTotal = total ?? 0;
	} else if (resourceType.value === ResourceType.DATASET) {
		datasetTotal = total ?? 0;
	}

	// Models and terarium documents are fetched in the same search on every search so these will always be accurate.
	for (let i = 0; i < searchResults.value.length; i++) {
		if (searchResults.value[i].searchSubsystem === ResourceType.MODEL) {
			modelTotal = searchResults.value[i].results.length ?? 0;
		} else if (resourceType.value === ResourceType.DOCUMENT) {
			// Note on the above, when we say XDD here we apparently don't actually mean XDD we mean Terarium Documents.
			documentTotal = searchResults.value[i].results.length ?? 0;
		}
	}

	docCount.value = total;

	assetOptions.value = [
		{ label: `Documents (${documentTotal})`, value: AssetType.Document },
		{ label: `Models (${modelTotal})`, value: AssetType.Model },
		{ label: `Datasets (${datasetTotal})`, value: AssetType.Dataset }
	];

	isLoading.value = false;
};

const clearSearchByExampleSelections = () => {
	// clear out the search by example option selections
	searchByExampleOptions.value = {
		similarContent: false,
		forwardCitation: false,
		backwardCitation: false,
		relatedContent: false
	};
	// clear out search by example item
	searchByExampleItem.value = null;
};

const disableSearchByExample = () => {
	// disable search by example, if it was enabled
	// FIXME/REVIEW: should switching to another tab make all fetches dirty?
	executeSearchByExample.value = false;

	clearSearchByExampleSelections();
};

const toggleDataItemSelected = (dataItem: { item: ResultType; type?: string }) => {
	let foundIndx = -1;
	const item = dataItem.item;

	if (dataItem.type && dataItem.type === 'clicked') {
		// toggle preview
		if (isEqual(dataItem.item, previewItem.value)) {
			// clear preview item and close the preview panel
			previewItem.value = null;
		} else {
			// open the preview panel
			previewItem.value = item;
			isSliderResourcesOpen.value = false;
		}
		return; // do not add to cart if the purpose is to toggle preview
	}

	// by now, the user has explicitly asked for this item to be added to the cart
	foundIndx = selectedSearchItems.value.indexOf(item);
	if (foundIndx >= 0) {
		// item was already in the list so remove it
		selectedSearchItems.value.splice(foundIndx, 1);
	} else {
		// add it to the list
		selectedSearchItems.value = [...selectedSearchItems.value, item];
		// open cart and close preview panel
		previewItem.value = null;
		isSliderResourcesOpen.value = true;
	}
};

// Update asset type
watch(resourceType, async (newResourceType, oldResourceType) => {
	if (executeSearchByExample.value) return;

	// if no data currently exist for the selected tab,
	// or if data exists but outdated then we should refetch
	const resList = dataItemsUnfiltered.value.find((res) => res?.searchSubsystem === newResourceType);

	/** clear filters if they exist, we when to set the old resource type to
	 * have dirty results since now they will need to be refetched when the facets filters are cleared
	 * */
	if (!isEmpty(clientFilters.value.clauses)) {
		queryStore.reset();
		dirtyResults.value[oldResourceType] = true;
	}

	if (!resList || dirtyResults.value[newResourceType]) {
		disableSearchByExample();
		await executeSearch();
		dirtyResults.value[newResourceType] = false;
	}
});

async function executeNewQuery() {
	if (route.query?.q?.toString() === '' || route.query?.q?.toString()) {
		searchTerm.value = route.query?.q?.toString();
	}

	// If the search term is the same as the previous term don't execute a search

	// search term has changed, so all search results are dirty; need re-fetch
	disableSearchByExample();

	// initially, all search results are dirty; need re-fetch
	Object.values(ResourceType).forEach((key) => {
		dirtyResults.value[key as string] = true;
	});

	// re-fetch data from the server, apply filters, and re-calculate the facets
	await executeSearch();

	// done with initial fetch for the currently selected tab, so reset
	dirtyResults.value[resourceType.value] = false;
}

// this is called whenever the user apply some facet filter(s)
watch(clientFilters, async (n, o) => {
	if (filtersUtil.isEqual(n, o)) return;

	// We support facet filters for search by example for documents but not for models or datasets
	if (resourceType.value !== ResourceType.DOCUMENT) {
		disableSearchByExample();
	}

	// user has changed some of the facet filter, so re-fetch data
	dirtyResults.value[resourceType.value] = true;

	await executeSearch();

	// since facet filters differ across tabs,
	// there is no need to refetch the data on the next tab switch
	dirtyResults.value[resourceType.value] = false;
});

// Gets query from search-bar.vue
watch(
	() => route.query,
	() => {
		executeNewQuery();
	}
);

// Default query on reload
onMounted(async () => {
	executeNewQuery();
});

onUnmounted(() => {
	queryStore.reset();
});

/*
 * Search
 */
const searchBarRef = ref();

function searchByExampleModalToggled() {
	// TODO
	// toggle the search by example modal represented by the component search-by-example
	// which may be used as follows
	/*
	<search-by-example
		v-if="searchByExampleModal"
		:item="searchByExampleItem"
		@search="onSearchByExample"
		@hide="searchByExampleModal = false"
	/>
	*/
}
</script>

<style scoped>
main {
	display: flex;
	flex-direction: column;
}
.results-content {
	display: flex;
	flex-direction: column;
	flex: 1;
	flex-grow: 1;
	gap: 0.5rem;
	margin: 0.5rem 0.5rem 0;
}

.toggles {
	align-items: center;
	display: flex;
	gap: var(--gap);

	& .p-dropdown {
		min-width: 8rem;
	}
}

main > section:first-of-type {
	display: flex;
	flex-direction: row;
}

.p-selectbutton:deep(.p-button) {
	min-width: 7rem;
}

.search {
	display: flex;
	flex-direction: column;
	gap: var(--gap-small);
	& > nav {
		display: flex;
		justify-content: space-between;
	}
}
</style>
