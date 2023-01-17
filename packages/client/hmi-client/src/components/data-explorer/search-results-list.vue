<template>
	<ul>
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
	<!-- <div class="search-container">
		<models-listview v-if="resultType === ResourceType.MODEL" class="list-view"
			:models="(filteredAssets as Model[])" :raw-concept-facets="rawConceptFacets"
			:selected-search-items="selectedSearchItems" :search-term="searchTerm"
			@toggle-model-selected="updateSelection" />
		<datasets-listview v-if="resultType === ResourceType.DATASET" class="list-view"
			:datasets="(filteredAssets as Dataset[])" :raw-concept-facets="rawConceptFacets"
			:selected-search-items="selectedSearchItems" :search-term="searchTerm"
			@toggle-dataset-selected="updateSelection" />
		<articles-listview v-if="resultType === ResourceType.XDD" class="list-view"
			:articles="(filteredAssets as XDDArticle[])" :raw-concept-facets="rawConceptFacets"
			:selected-search-items="selectedSearchItems" @toggle-article-selected="updateSelection" />
	</div> -->
	<div class="results-count-label">Showing {{ resultsCount }} item(s).</div>
</template>

<script setup lang="ts">
import { ref, computed, PropType } from 'vue';
// import ModelsListview from '@/components/data-explorer/models-listview.vue';
// import DatasetsListview from '@/components/data-explorer/datasets-listview.vue';
// import ArticlesListview from '@/components/data-explorer/articles-listview.vue';

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

const filteredAssets = computed(() => {
	const searchResults = props.dataItems.find((res) => res.searchSubsystem === props.resultType);

	if (searchResults) {
		if (searchResults.xddExtractions && props.resultType === ResourceType.XDD) {
			let articlesFromExtractions: XDDArticle[] = [];

			if (searchResults.xddExtractions.length > 0) {
				const docMap: { [docid: string]: XDDArticle } = {};
				searchResults.xddExtractions.forEach((ex) => {
					if (ex.properties.documentBibjson === undefined) return; // skip

					// eslint-disable-next-line no-underscore-dangle
					const docid = ex.properties.documentBibjson.gddid || ex.properties.documentBibjson._gddid; // FIXME: embedded doc metadata has no proper fields mapping
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

// const rawConceptFacets = computed(() => {
// 	const searchResults = props.dataItems.find((res) => res.searchSubsystem === props.resultType);
// 	if (searchResults) {
// 		return searchResults.rawConceptFacets;
// 	}
// 	return null;
// });

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
	overflow-y: scroll;
	list-style: none;
}

.list-view {
	flex: 1;
	min-height: 0px;
}

.results-count-label {
	font-weight: bold;
	margin: 4px;
	align-self: center;
}
</style>
