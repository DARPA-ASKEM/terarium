<template>
	<div class="results-count">
		<template v-if="isLoading">Loading...</template>
		<template v-else>Showing {{ resultsCount }} item(s)</template>
	</div>
	<div v-if="isLoading" class="loading-spinner">
		<div><i class="pi pi-spin pi-spinner" style="font-size: 5rem"></i></div>
	</div>
	<div v-else-if="resultsCount === 0" class="loading-spinner">No results found</div>
	<ul v-else>
		<li v-for="(asset, index) in filteredAssets" :key="index">
			<SearchItem
				:asset="(asset as XDDArticle & Model & Dataset)"
				:selectedSearchItems="selectedSearchItems"
				:isPreviewed="previewedAsset === asset"
				:isInCart="false"
				:resourceType="(resultType as ResourceType)"
				@toggle-selected-asset="updateSelection(asset)"
				@toggle-asset-preview="togglePreview(asset)"
			/>
		</li>
	</ul>
</template>

<script setup lang="ts">
import { ref, computed, PropType } from 'vue';
import { XDDArticle } from '@/types/XDD';
import { Model } from '@/types/Model';
import { Dataset } from '@/types/Dataset';
import { SearchResults, ResourceType, ResultType } from '@/types/common';
import SearchItem from './search-item.vue';

const props = defineProps({
	dataItems: {
		type: Array as PropType<SearchResults[]>,
		default: () => []
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
					if (ex.properties.documentBibjson === undefined) return; // skip
					const docid = ex.properties.documentBibjson.gddId;
					if (docMap[docid] === undefined) {
						docMap[docid] = ex.properties.documentBibjson;
						docMap[docid].relatedExtractions = [];
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

.search-container {
	overflow-y: auto;
}

.results-count {
	color: var(--text-color-subdued);
}
</style>
