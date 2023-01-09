<template>
	<div class="search-container">
		<models-listview
			v-if="resultType === ResourceType.MODEL"
			class="list-view"
			:models="filteredModels"
			:raw-concept-facets="rawConceptFacets"
			:selected-search-items="selectedSearchItems"
			:search-term="searchTerm"
			@toggle-model-selected="toggleDataItemSelected"
		/>
		<datasets-listview
			v-if="resultType === ResourceType.DATASET"
			class="list-view"
			:datasets="filteredDatasets"
			:raw-concept-facets="rawConceptFacets"
			:selected-search-items="selectedSearchItems"
			:search-term="searchTerm"
			@toggle-dataset-selected="toggleDataItemSelected"
		/>
		<template v-if="resultType === ResourceType.XDD">
			<articles-listview
				class="list-view"
				:articles="filteredArticles"
				:raw-concept-facets="rawConceptFacets"
				:selected-search-items="selectedSearchItems"
				@toggle-article-selected="toggleDataItemSelected"
			/>
		</template>
	</div>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue';
import ModelsListview from '@/components/data-explorer/models-listview.vue';
import DatasetsListview from '@/components/data-explorer/datasets-listview.vue';
import ArticlesListview from '@/components/data-explorer/articles-listview.vue';
import { Model } from '@/types/Model';
import { XDDArticle, XDDArtifact } from '@/types/XDD';
import { Dataset } from '@/types/Dataset';
import { SearchResults, ResourceType, ResultType } from '@/types/common';

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

const emit = defineEmits(['toggle-data-item-selected']);

const toggleDataItemSelected = (item: ResultType) => {
	emit('toggle-data-item-selected', item);
};

const filteredModels = computed(() => {
	const resList = props.dataItems.find((res) => res.searchSubsystem === ResourceType.MODEL);
	if (resList) {
		return resList.results as Model[];
	}
	return [];
});
const filteredDatasets = computed(() => {
	const resList = props.dataItems.find((res) => res.searchSubsystem === ResourceType.DATASET);
	if (resList) {
		return resList.results as Dataset[];
	}
	return [];
});

const filteredXDDResults = computed(() => {
	const resList = props.dataItems.find((res) => res.searchSubsystem === ResourceType.XDD);
	if (resList) {
		return resList;
	}
	return null;
});
const filteredXDDExtractions = computed(() => {
	if (filteredXDDResults.value && filteredXDDResults.value.xddExtractions) {
		return filteredXDDResults.value.xddExtractions;
	}
	return [] as XDDArtifact[];
});
const filteredArticles = computed(() => {
	if (filteredXDDResults.value) {
		let articlesFromExtractions: XDDArticle[] = [];
		if (filteredXDDExtractions.value && filteredXDDExtractions.value.length > 0) {
			const docMap: { [docid: string]: XDDArticle } = {};
			filteredXDDExtractions.value.forEach((ex) => {
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
		const xDDArticlesSearchResults = filteredXDDResults.value.results as XDDArticle[];
		return [...articlesFromExtractions, ...xDDArticlesSearchResults];
	}
	return [];
});
const rawConceptFacets = computed(() => {
	const resList = props.dataItems.find((res) => res.searchSubsystem === props.resultType);
	if (resList) {
		return resList.rawConceptFacets;
	}
	return null;
});
</script>

<style lang="scss" scoped>
.search-container {
	min-height: 0px;
	width: 100%;
	display: flex;
	flex-direction: column;
	gap: 1px;

	.list-view {
		flex: 1;
		min-height: 0px;
	}
}
</style>
