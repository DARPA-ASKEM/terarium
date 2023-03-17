<template>
	<div class="result-details">
		<span class="result-count">
			<template v-if="isLoading">Loading...</template>
			<template v-else
				>{{ resultsText }} <span>"{{ props.searchTerm }}"</span></template
			>
		</span>
	</div>
	<div v-if="chosenFacets.length > 0" class="facet-chips">
		<template v-for="facet in chosenFacets">
			<Chip
				v-for="(value, index) in facet.values"
				:key="index"
				icon="pi pi-filter"
				removable
				@remove="removeFacetValue(facet.field, facet.values, value)"
				remove-icon="pi pi-times"
			>
				{{ facet.field.charAt(0).toUpperCase() + facet.field.slice(1) }}:<span>{{ value }}</span>
			</Chip>
		</template>
	</div>
	<div v-if="isLoading" class="explorer-status loading-spinner">
		<div><i class="pi pi-spin pi-spinner" /></div>
	</div>
	<div v-else-if="resultsCount === 0" class="explorer-status">
		<img src="@assets/svg/seed.svg" alt="Seed" />
		<h2 class="no-results-found">No results found</h2>
		<span>Try adjusting your search or filters and try again.</span>
	</div>
	<ul v-else>
		<li v-for="(asset, index) in filteredAssets" :key="index">
			<SearchItem
				:asset="(asset as DocumentType & Model & Dataset)"
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
import { XDDExtractionType } from '@/types/XDD';
import { DocumentType } from '@/types/Document';
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
			let documentsFromExtractions: DocumentType[] = [];

			if (searchResults.xddExtractions && searchResults.xddExtractions.length > 0) {
				const docMap: { [docid: string]: DocumentType } = {};

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
				documentsFromExtractions = Object.values(docMap) as DocumentType[];
			}
			const documentSearchResults = searchResults.results as DocumentType[];

			return [...documentsFromExtractions, ...documentSearchResults];
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

const resultsText = computed(() => {
	if (resultsCount.value === 0) {
		return 'No results found for';
	}
	const s = resultsCount.value === 1 ? '' : 's';
	return `Showing ${resultsCount.value} result${s} for `;
});
</script>

<style scoped>
ul {
	display: flex;
	flex-direction: column;
	list-style: none;
	overflow-y: scroll;
}

.explorer-status {
	display: flex;
	flex-direction: column;
	justify-content: center;
	gap: 1rem;
	align-items: center;
	margin-bottom: 8rem;
	flex-grow: 1;
	font-size: var(--font-body-small);
	color: var(--text-color-subdued);
}

.loading-spinner {
	color: var(--primary-color);
}

.pi-spinner {
	font-size: 5rem;
}

.no-results-found {
	font-weight: var(--font-weight);
	margin-top: 1.5rem;
}

.result-details {
	display: flex;
	align-items: center;
	overflow: visible;
	gap: 0.5rem;
}

.result-details {
	color: var(--text-color-subdued);
}

.result-count {
	font-size: var(--font-caption);
	white-space: nowrap;
}

.result-count span {
	color: var(--text-color-primary);
}

.facet-chips {
	display: inline-flex;
	gap: 1rem;
}

.p-chip {
	background-color: var(--surface-section);
	font-weight: 400;
}

.p-chip span {
	color: var(--text-color-primary);
	margin-left: 0.25rem;
}

.search-container {
	overflow-y: auto;
}
</style>
