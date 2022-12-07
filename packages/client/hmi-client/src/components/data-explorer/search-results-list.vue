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
		<articles-listview
			v-if="resultType === ResourceType.XDD"
			class="list-view"
			:articles="filteredArticles"
			:raw-concept-facets="rawConceptFacets"
			:selected-search-items="selectedSearchItems"
			@toggle-article-selected="toggleDataItemSelected"
		/>
	</div>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue';
import ModelsListview from '@/components/data-explorer/models-listview.vue';
import DatasetsListview from '@/components/data-explorer/datasets-listview.vue';
import ArticlesListview from '@/components/data-explorer/articles-listview.vue';
import { Model } from '@/types/Model';
import { XDDArticle } from '@/types/XDD';
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
const filteredArticles = computed(() => {
	const resList = props.dataItems.find((res) => res.searchSubsystem === ResourceType.XDD);
	if (resList) {
		return resList.results as XDDArticle[];
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
		min-height: 0;
	}
}
</style>
