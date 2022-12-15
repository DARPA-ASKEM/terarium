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
			<slot name="header"></slot>
			<articles-listview
				class="list-view"
				:articles="filteredArticles"
				:raw-concept-facets="rawConceptFacets"
				:extractions="filteredXDDExtractions"
				:selected-search-items="selectedSearchItems"
				:view-type="xddViewType"
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
import { SearchResults, ResourceType, ResultType, XDDViewType } from '@/types/common';

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
	xddViewType: {
		type: String,
		default: XDDViewType.PUBLICATIONS
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
const filteredArticles = computed(() => {
	if (filteredXDDResults.value) {
		return filteredXDDResults.value.results as XDDArticle[];
	}
	return [];
});
const filteredXDDExtractions = computed(() => {
	if (filteredXDDResults.value && filteredXDDResults.value.xddExtractions) {
		return filteredXDDResults.value.xddExtractions;
	}
	return [] as XDDArtifact[];
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
