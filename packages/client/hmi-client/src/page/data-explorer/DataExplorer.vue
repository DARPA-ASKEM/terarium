<template>
	<main class="data-explorer-container flex flex-column">
		<section class="filter-bar">
			<tera-filter-bar :topic-options="topicOptions" @filter-changed="executeNewQuery" />
		</section>
		<section class="flex h-full relative overflow-hidden">
			<tera-slider-panel
				content-width="240px"
				direction="left"
				header="Facets"
				v-model:is-open="isSliderFacetsOpen"
			>
				<template v-slot:content>
					<tera-facets-panel
						v-if="viewType === ViewType.LIST"
						:facets="facets"
						:filtered-facets="filteredFacets"
						:result-type="resourceType"
						:docCount="docCount"
					/>
				</template>
			</tera-slider-panel>
			<div class="results-content">
				<SelectButton
					:model-value="resourceType"
					@change="if ($event.value) resourceType = $event.value;"
					:options="assetOptions"
					option-value="value"
				>
					<template #option="slotProps">
						<i :class="`${slotProps.option.icon} p-button-icon-left`" />
						<span class="p-button-label">{{ slotProps.option.label }}</span>
					</template>
				</SelectButton>
				<tera-search-results-list
					:data-items="dataItems"
					:facets="filteredFacets"
					:result-type="resourceType"
					:search-term="searchTerm"
					:is-loading="isLoading"
					:doc-count="docCount"
					@toggle-data-item-selected="toggleDataItemSelected"
				/>
			</div>
			<tera-preview-panel
				:content-width="`${sliderWidth.slice(0, -1)} - 20px)`"
				tab-width="0"
				direction="right"
				v-model:preview-item="previewItem"
				:resource-type="resourceType"
				:selected-search-items="selectedSearchItems"
				:search-term="searchTerm"
				@toggle-data-item-selected="toggleDataItemSelected"
			/>
			<tera-slider-panel
				class="resources-slider"
				:content-width="sliderWidth"
				direction="right"
				header="Selected resources"
				v-model:is-open="isSliderResourcesOpen"
				:indicator-value="selectedSearchItems.length"
			>
				<template v-slot:header>
					<tera-selected-resources-header-pane
						:selected-search-items="selectedSearchItems"
						@close="isSliderResourcesOpen = false"
						@clear-selected="clearItemSelected"
					/>
				</template>
				<template v-slot:subHeader>
					<div v-if="selectedSearchItems.length == 1" class="sub-header-title">
						{{ selectedSearchItems.length }} item
					</div>
					<div v-if="selectedSearchItems.length > 1" class="sub-header-title">
						{{ selectedSearchItems.length }} items
					</div>
					<div v-if="selectedSearchItems.length == 0">
						<div class="sub-header-title">Empty</div>
					</div>
				</template>
				<template v-slot:content>
					<div v-if="selectedSearchItems.length == 0" class="empty-cart-image-container">
						<div class="empty-cart-image">
							<img src="@/assets/svg/seed.svg" alt="Picture of a seed" />
						</div>
						<p>Selected resources will appear here</p>
					</div>
					<tera-selected-resources-options-pane
						:selected-search-items="selectedSearchItems"
						@toggle-data-item-selected="toggleDataItemSelected"
						@find-related-content="onFindRelatedContent"
						@find-similar-content="onFindSimilarContent"
					/>
				</template>
			</tera-slider-panel>
		</section>
	</main>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import SelectButton from 'primevue/selectbutton';
import { fetchData, getDocumentById } from '@/services/data';
import {
	ResourceType,
	ResultType,
	SearchByExampleOptions,
	SearchParameters,
	SearchResults,
	ViewType
} from '@/types/common';
import { getFacets } from '@/utils/facets';
import {
	FACET_FIELDS as XDD_FACET_FIELDS,
	GITHUB_URL,
	XDD_RESULT_DEFAULT_PAGE_SIZE,
	YEAR
} from '@/types/XDD';
import useQueryStore from '@/stores/query';
import filtersUtil from '@/utils/filters-util';
import useResourcesStore from '@/stores/resources';
import { getResourceID, isDataset, isModel, isDocument, validate } from '@/utils/data-util';
import { cloneDeep, intersectionBy, isEmpty, isEqual, max, min, unionBy } from 'lodash';
import { useRoute } from 'vue-router';
import TeraPreviewPanel from '@/page/data-explorer/components/tera-preview-panel.vue';
import TeraSelectedResourcesOptionsPane from '@/page/data-explorer/components/tera-selected-resources-options-pane.vue';
import TeraSelectedResourcesHeaderPane from '@/page/data-explorer/components/tera-selected-resources-header-pane.vue';
import TeraFacetsPanel from '@/page/data-explorer/components/tera-facets-panel.vue';
import TeraSearchResultsList from '@/page/data-explorer/components/tera-search-results-list.vue';
import { XDDFacetsItemResponse } from '@/types/Types';
import { useSearchByExampleOptions } from './search-by-example';
import TeraFilterBar from './components/tera-filter-bar.vue';

// FIXME: page count is not taken into consideration

const route = useRoute();
const queryStore = useQueryStore();
const resources = useResourcesStore();

const { searchByExampleOptions, searchByExampleItem } = useSearchByExampleOptions();
const dataItems = ref<SearchResults[]>([]);
const dataItemsUnfiltered = ref<SearchResults[]>([]);
const selectedSearchItems = ref<ResultType[]>([]);
const executeSearchByExample = ref(false);
const previewItem = ref<ResultType | null>(null);
const searchTerm = ref('');
// default slider state
const isSliderFacetsOpen = ref(true);
const isSliderResourcesOpen = ref(false);
const pageSize = ref(XDD_RESULT_DEFAULT_PAGE_SIZE);
// xdd
const dictNames = ref<string[]>([]);
const rankedResults = ref(true); // disable sorted/ranked results to enable pagination
// facets
const facets = ref<{ [index: string]: XDDFacetsItemResponse }>({});
const docCount = ref(0);
const filteredFacets = ref<{ [index: string]: XDDFacetsItemResponse }>({});
//
const resourceType = ref<ResourceType>(ResourceType.XDD);
const viewType = ref<string>(ViewType.LIST);
const isLoading = ref<boolean>(false);
// optimize search performance: only fetch as needed
const dirtyResults = ref<{ [resourceType: string]: boolean }>({});

const clientFilters = computed(() => queryStore.clientFilters);

const assetOptions = ref([
	{ label: 'Documents', value: ResourceType.XDD, icon: 'pi pi-file' },
	{ label: 'Models', value: ResourceType.MODEL, icon: 'pi pi-share-alt' },
	{ label: 'Datasets', value: ResourceType.DATASET, icon: 'pi pi-database' }
]);

const topicOptions = ref([
	{ label: 'Covid-19', value: 'xdd-covid-19' },
	{ label: 'Climate Weather', value: 'climate-change-modeling' }
]);

const sliderWidth = computed(() =>
	isSliderFacetsOpen.value ? 'calc(50% - 120px)' : 'calc(50% - 20px)'
);

defineExpose({ resourceType });

// close resources if preview opens
watch(isSliderResourcesOpen, () => {
	if (isSliderResourcesOpen.value) {
		previewItem.value = null;
	}
});

const calculateFacets = (unfilteredData: SearchResults[], filteredData: SearchResults[]) => {
	// retrieves filtered & unfiltered facet data
	facets.value = getFacets(unfilteredData, resourceType.value);
	filteredFacets.value = getFacets(filteredData, resourceType.value);
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
	if (!dirtyResults.value[resourceType.value]) return;

	// only search (or fetch data) relevant to the currently selected tab or the search by example item
	let searchType = resourceType.value;
	isLoading.value = true;
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
		[ResourceType.XDD]: {
			dict: dictNames.value,
			dataset: resources.getXddDataset,
			max: pageSize.value,
			perPage: pageSize.value,
			fullResults: !rankedResults.value,
			doi: isValidDOI ? searchWords : undefined,
			includeHighlights: true,
			inclusive: matchAll,
			facets: true, // include facets aggregation data in the search results
			match: true,
			additional_fields: 'title,abstract',
			known_entities: 'url_extractions,askem_object'
		},
		model: {},
		dataset: {}
	};

	// handle the search-by-example for finding related documents, models, and/or datasets
	if (executeSearchByExample.value && searchByExampleItem.value) {
		const id = getResourceID(searchByExampleItem.value) as string;
		//
		// find related documents (which utilizes the xDD doc2vec API through the HMI server)
		//
		if (isDocument(searchByExampleItem.value) && searchParams.xdd) {
			searchParams.xdd.dataset = resources.getXddDataset;
			if (searchByExampleOptions.value.similarContent) {
				searchParams.xdd.similar_search_enabled = executeSearchByExample.value;
			}
			if (searchByExampleOptions.value.relatedContent) {
				searchParams.xdd.related_search_enabled = executeSearchByExample.value;
			}
			searchParams.xdd.related_search_id = id;
			searchType = ResourceType.XDD;
		}
		//
		// find related models (which utilizes the TDS provenance API through the HMI server)
		//
		if (isModel(searchByExampleItem.value) && searchParams.model) {
			searchParams.model.related_search_enabled = executeSearchByExample.value;
			searchParams.model.related_search_id = id;
			searchType = ResourceType.MODEL;
		}
		//
		// find related datasets (which utilizes the TDS provenance API through the HMI server)
		//
		if (isDataset(searchByExampleItem.value) && searchParams.dataset) {
			searchParams.dataset.related_search_enabled = executeSearchByExample.value;
			searchParams.dataset.related_search_id = id;
			searchType = ResourceType.DATASET;
		}
	}
	const searchParamsWithFacetFilters = cloneDeep(searchParams);

	//
	// extend search parameters by converting facet filters into proper search parameters
	//
	const xddSearchParams = searchParamsWithFacetFilters?.[ResourceType.XDD] || {};
	// transform facet filters into xdd search parameters
	clientFilters.value.clauses.forEach((clause) => {
		if (XDD_FACET_FIELDS.includes(clause.field)) {
			// NOTE: special case
			if (clause.field === YEAR) {
				if (clause.values.length === 1) {
					// a single year is selected
					const val = (clause.values as string[]).join(',');
					const minFormattedVal = `${val}-01-01`; // must be in ISO format; 2020-01-01
					const maxFormattedVal = `${val}-12-31`; // must be in ISO format; 2020-01-01
					xddSearchParams.min_published = minFormattedVal;
					xddSearchParams.max_published = maxFormattedVal;
				} else {
					// multiple years are selected, so find their range
					const years = clause.values.map((year) => +year);
					const minYear = min(years);
					const maxYear = max(years);
					const formattedValMinYear = `${minYear}-01-01`; // must be in ISO format; 2020-01-01
					const formattedValMaxYear = `${maxYear}-12-31`; // must be in ISO format; 2020-01-01
					xddSearchParams.min_published = formattedValMinYear;
					xddSearchParams.max_published = formattedValMaxYear;
				}
			} else if (clause.field === GITHUB_URL) {
				xddSearchParams.githubUrls = (clause.values as string[]).join(',');
			} else {
				xddSearchParams[clause.field] = (clause.values as string[]).join(',');
			}
		}
	});

	let modelSearchParams;
	if (searchParamsWithFacetFilters?.[ResourceType.MODEL]?.filters) {
		modelSearchParams = searchParamsWithFacetFilters[ResourceType.MODEL];
	} else {
		modelSearchParams = { filters: clientFilters.value };
	}
	let datasetSearchParams;
	if (searchParamsWithFacetFilters?.[ResourceType.DATASET]?.filters) {
		datasetSearchParams = searchParamsWithFacetFilters[ResourceType.MODEL];
	} else {
		datasetSearchParams = { filters: clientFilters.value };
	}

	// update search parameters object
	searchParamsWithFacetFilters.xdd = xddSearchParams;
	searchParamsWithFacetFilters.model = modelSearchParams;
	searchParamsWithFacetFilters.dataset = datasetSearchParams;

	// fetch the data
	const { allData, allDataFilteredWithFacets } = await fetchData(
		searchWords,
		searchParams,
		searchParamsWithFacetFilters,
		searchType
	);

	// cache unfiltered data
	dataItemsUnfiltered.value = mergeResultsKeepRecentDuplicates(dataItemsUnfiltered.value, allData);

	// the list of results displayed in the data explorer is always the final filtered data
	dataItems.value = mergeResultsKeepRecentDuplicates(dataItems.value, allDataFilteredWithFacets);

	// final step: cache the facets and filteredFacets objects
	calculateFacets(allData, allDataFilteredWithFacets);

	let total = 0;
	allData.forEach((res) => {
		const count = res?.hits ?? res?.results.length;
		total += count;
	});

	docCount.value = total;

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

const onSearchByExample = async (searchOptions: SearchByExampleOptions) => {
	// user has requested a search by example, so re-fetch data
	dirtyResults.value[resourceType.value] = true;

	// REVIEW: executing a similar content search means to find similar objects to the one selected:
	//         if a document is selected then find related documents (from xDD)
	// REVIEW: executing a related content search means to find related artifacts to the one selected:
	//         if a model/dataset/document is selected then find related artifacts from TDS
	if (searchOptions.similarContent || searchOptions.relatedContent) {
		// NOTE the executeSearch will set proper search-by-example search parameters
		//  and let the data service handles the fetch
		executeSearchByExample.value = true;

		await executeSearch();

		dirtyResults.value[resourceType.value] = false;
	}
};

// helper function to bypass the search-by-example modal
//  by executing a search by example and refreshing the output
const onFindRelatedContent = (item) => {
	searchByExampleItem.value = item.item;
	searchByExampleOptions.value = {
		similarContent: false,
		forwardCitation: false,
		backwardCitation: false,
		relatedContent: true
	};
};

// helper function to bypass the search-by-example modal
//  by executing a search by example and refreshing the output
const onFindSimilarContent = (item) => {
	searchByExampleItem.value = item.item;
	searchByExampleOptions.value = {
		similarContent: true,
		forwardCitation: false,
		backwardCitation: false,
		relatedContent: false
	};
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
	const resList = dataItemsUnfiltered.value.find((res) => res.searchSubsystem === newResourceType);

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
	} else {
		// data has not changed; the user has just switched the result tab, e.g., from Documents to Models
		// re-calculate the facets
		calculateFacets(dataItemsUnfiltered.value, dataItems.value);
	}
});

const clearItemSelected = () => {
	selectedSearchItems.value = [];
};
// const addPreviewItemToCart = () => {
// 	if (previewItem.value) {
// 		toggleDataItemSelected( {item: previewItem.value } );
// 		previewItem.value = null;
// 		isSliderResourcesOpen.value = true;
// 	}
// };

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

async function searchByExampleOnPageRefresh(resourceId: string) {
	if (!searchByExampleItem.value) {
		searchByExampleItem.value = await getDocumentById(resourceId);
	}
	if (!Object.values(searchByExampleOptions.value).some((v) => v)) {
		searchByExampleOptions.value.similarContent = true;
	}
	onSearchByExample(searchByExampleOptions.value);
}

// this is called whenever the user apply some facet filter(s)
watch(clientFilters, async (n, o) => {
	if (filtersUtil.isEqual(n, o)) return;

	// We support facet filters for search by example for documents but not for models or datasets
	if (resourceType.value !== ResourceType.XDD) {
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
		// The query changes in the following cases:
		// - when a user does a normal search - there is no `resourceId`
		// - when a user does a search by example - there is a `resourceId`
		// - when a user navigates back and forth on a page

		if (route.query.resourceId) {
			searchByExampleOnPageRefresh(route.query.resourceId.toString());
		} else {
			executeNewQuery();
		}
	}
);

// Default query on reload
onMounted(async () => {
	resources.setXDDDataset('xdd-covid-19'); // give xdd dataset a default value
	// On reload, if the url has a resourceId, we know that a user just did a search by example
	// so we want to preserve the search by example so we perform a search by example instead of a normal search
	if (route.query.resourceId) {
		searchByExampleOnPageRefresh(route.query.resourceId.toString());
	} else {
		executeNewQuery();
	}
});

onUnmounted(() => {
	queryStore.reset();
});
</script>

<style scoped>
.data-explorer-container {
	background-color: var(--surface-100);
}
.results-content {
	display: flex;
	flex-direction: column;
	flex: 1;
	flex-grow: 1;
	gap: 0.5rem;
	margin: 0.5rem 0.5rem 0;
}

.resources-slider {
	z-index: 1;
}

.sub-header-title {
	font-size: var(--font-caption);
	text-align: center;
	color: var(--text-color-subdued);
	display: flex;
}

.empty-cart-image-container {
	justify-content: center;
	display: flex;
	flex-direction: column;
	margin-top: 12rem;
	align-items: center;
	gap: 2rem;
	color: var(--text-color-secondary);
	font-size: var(--font-body-small);
}

.empty-cart-image {
	margin: auto;
}

.breakdown-pane-container {
	margin-left: 0.5rem;
	margin-right: 0.5rem;
}

.cart-item {
	list-style-type: none;
}
</style>
