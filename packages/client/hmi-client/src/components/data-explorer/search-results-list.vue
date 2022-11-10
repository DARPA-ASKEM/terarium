<template>
	<div class="search-container">
		<models-listview
			v-if="resultType === ResourceType.MODEL"
			class="list-view"
			:models="filteredModels"
			:selected-search-items="selectedSearchItems"
			@toggle-model-selected="toggleDataItemSelected"
		/>
		<articles-listview
			v-if="resultType === ResourceType.XDD"
			class="list-view"
			:articles="filteredArticles"
			:selected-search-items="selectedSearchItems"
			@toggle-article-selected="toggleDataItemSelected"
		/>
		<common-listview
			v-if="resultType === ResourceType.ALL"
			class="list-view"
			:input-items="dataItems"
			:selected-search-items="selectedSearchItems"
			@toggle-item-selected="toggleDataItemSelected"
		/>
	</div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import ModelsListview from '@/components/data-explorer/models-listview.vue';
import ArticlesListview from '@/components/data-explorer/articles-listview.vue';
import CommonListview from '@/components/data-explorer/common-listview.vue';
import { Model } from '@/types/Model';
import { XDDArticle } from '@/types/XDD';
import { SearchResults, ResourceType, ResultType } from '@/types/common';

export default defineComponent({
	name: 'SearchResultsList',
	components: {
		ModelsListview,
		ArticlesListview,
		CommonListview
	},
	props: {
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
		}
	},
	emits: ['toggle-data-item-selected'],
	data: () => ({
		ResourceType
	}),
	computed: {
		filteredModels() {
			const resList = this.dataItems.find((res) => res.searchSubsystem === ResourceType.MODEL);
			if (resList) {
				return resList.results as Model[];
			}
			return [];
		},
		filteredArticles() {
			const resList = this.dataItems.find((res) => res.searchSubsystem === ResourceType.XDD);
			if (resList) {
				return resList.results as XDDArticle[];
			}
			return [];
		}
	},
	methods: {
		toggleDataItemSelected(item: ResultType) {
			this.$emit('toggle-data-item-selected', item);
		}
	}
});
</script>

<style lang="scss" scoped>
.search-container {
	min-height: 0px;
	width: 100%;
	display: flex;
	flex-direction: column;
	padding-right: 10px;
	gap: 1px;

	.list-view {
		flex: 1;
		min-height: 0;
	}
}
</style>
