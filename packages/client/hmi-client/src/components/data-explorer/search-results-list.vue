<template>
	<div class="result-details">
		<span class="result-count">
			<template v-if="isLoading">Loading...</template>
			<template v-else>Showing {{ resultsCount }} results(s)</template>
		</span>
		<template v-for="facet in chosenFacets">
			<Chip
				v-for="(value, index) in facet.values"
				:label="(value as string)"
				:key="index"
				removable
				@remove="removeFacetValue(facet.field, facet.values, value)"
				remove-icon="pi pi-times"
			/>
		</template>
	</div>
	<div v-if="isLoading" class="loading-spinner">
		<div><i class="pi pi-spin pi-spinner" style="font-size: 5rem" /></div>
	</div>
	<div v-else-if="resultsCount === 0" class="loading-spinner">No results found</div>
	<ul v-else>
		<li v-for="(asset, index) in filteredAssets" :key="index">
			<SearchItem
				:asset="(asset as XDDArticle & Model & Dataset)"
				:selectedSearchItems="selectedSearchItems"
				:isPreviewed="previewedAsset === asset"
				:resourceType="(resultType as ResourceType)"
				:searchTerm="searchTerm"
				@toggle-selected-asset="updateSelection(asset)"
				@toggle-asset-preview="togglePreview(asset)"
			/>
		</li>
	</ul>
</template>

<script setup lang="ts">
import { ref, computed, PropType } from 'vue';
import { XDDArticle, XDDExtractionType } from '@/types/XDD';
import useQueryStore from '@/stores/query';
import { Model } from '@/types/Model';
import { Dataset } from '@/types/Dataset';
import { Facets, SearchResults, ResourceType, ResultType } from '@/types/common';
import Chip from 'primevue/chip';
import { ClauseValue } from '@/types/Filter';
import SearchItem from './search-item.vue';

const props = defineProps({
	dataItems: {
		type: Array as PropType<SearchResults[]>,
		default: () => []
	},
	facets: {
		type: Object as PropType<Facets>,
		required: true
	},
	selectedSearchItems: {
		type: Array as PropType<ResultType[]>,
		required: true
	},
	resultType: {
		type: String,
		default: ResourceType.ALL
	},
	searchTerm: {
		type: String,
		default: ''
	},
	isLoading: {
		type: Boolean,
		default: true
	}
});

const previewedAsset = ref<ResultType | null>(null);

const emit = defineEmits(['toggle-data-item-selected']);

const chosenFacets = computed(() => useQueryStore().clientFilters.clauses);

const removeFacetValue = (field: string, values: ClauseValue[], valueToRemove: ClauseValue) => {
	const query = useQueryStore();
	values.splice(values.indexOf(valueToRemove), 1);
	query.setSearchClause({ field, values });
};

const updateSelection = (asset: ResultType) => {
	emit('toggle-data-item-selected', { item: asset, type: 'selected' });
};

const togglePreview = (asset: ResultType) => {
	emit('toggle-data-item-selected', { item: asset, type: 'clicked' });
	previewedAsset.value = previewedAsset.value === asset ? null : asset;
};

// const rawConceptFacets = computed(() => {
// 	const searchResults = props.dataItems.find((res) => res.searchSubsystem === props.resultType);
// 	if (searchResults) {
// 		return searchResults.rawConceptFacets;
// 	}
// 	return null;
// });

const filteredAssets = computed(() => {
	const searchResults = props.dataItems.find((res) => res.searchSubsystem === props.resultType);

	if (searchResults) {
		if (props.resultType === ResourceType.XDD) {
			let articlesFromExtractions: XDDArticle[] = [];

			if (searchResults.xddExtractions && searchResults.xddExtractions.length > 0) {
				const docMap: { [docid: string]: XDDArticle } = {};

				searchResults.xddExtractions.forEach((ex) => {
					const docid = ex.properties.documentBibjson.gddId;
					if (docMap[docid] === undefined) {
						docMap[docid] = ex.properties.documentBibjson;
						docMap[docid].relatedExtractions = [];
					}
					// Avoid duplicate documents
					else if (ex.askemClass === XDDExtractionType.Document) {
						const docExtractions = docMap[docid].relatedExtractions?.filter(
							(extraction) => extraction.askemClass === XDDExtractionType.Document
						);

						if (docExtractions) {
							for (let i = 0; i < docExtractions.length; i++) {
								if (ex.properties.DOI === docExtractions[i].properties.DOI) return; // Skip
							}
						}
					}
					docMap[docid].relatedExtractions?.push(ex);
				});
				articlesFromExtractions = Object.values(docMap) as XDDArticle[];
			}
			const xDDArticlesSearchResults = searchResults.results as XDDArticle[];

			return [...articlesFromExtractions, ...xDDArticlesSearchResults];
		}
		if (props.resultType === ResourceType.MODEL || props.resultType === ResourceType.DATASET) {
			return searchResults.results;
		}
	}
	return [];
});

const resultsCount = computed(() => {
	let total = 0;
	if (props.resultType === ResourceType.ALL) {
		// count the results from all subsystems
		props.dataItems.forEach((res) => {
			const count = res?.hits ?? res?.results.length;
			total += count;
		});
	} else {
		// only return the results count for the selected subsystems
		total = filteredAssets.value.length;
	}
	return total;
});
</script>

<style scoped>
ul {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	list-style: none;
	overflow-y: scroll;
}

.loading-spinner {
	display: flex;
	justify-content: center;
	align-items: center;
	margin-bottom: 8rem;
	flex-grow: 1;
	background-color: var(--surface-ground);
	color: var(--primary-color-dark);
	font-weight: bold;
}

.result-details {
	display: flex;
	align-items: center;
	overflow: visible;
	gap: 0.5rem;
}

.result-details,
.p-chip {
	color: var(--text-color-subdued);
}

.result-count {
	font-size: var(--font-size);
	white-space: nowrap;
}

.p-chip {
	outline: 1px solid var(--gray-300);
	font-weight: bold;
	padding: 0 0.75rem;
	background-color: var(--surface-200);
}

.p-chip :deep(.p-chip-text) {
	margin: 0.2rem 0;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 1;
	overflow: hidden;
}

.p-chip,
.p-chip :deep(.p-chip-remove-icon) {
	font-size: var(--font-caption);
}

.search-container {
	overflow-y: auto;
}
</style>
