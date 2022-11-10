<template>
	<div class="search-container">
		<div class="nav-bar">
			<ul class="nav">
				<li>
					<button
						type="button"
						:class="{ active: resultType === ResourceType.ALL }"
						@click="onResultTypeChanged(ResourceType.ALL)"
					>
						All
					</button>
				</li>
				<li>
					<button
						type="button"
						:class="{ active: resultType === ResourceType.MODEL }"
						@click="onResultTypeChanged(ResourceType.MODEL)"
					>
						Models
					</button>
				</li>
				<li>
					<button
						type="button"
						:class="{ active: resultType === ResourceType.XDD }"
						@click="onResultTypeChanged(ResourceType.XDD)"
					>
						Articles
					</button>
				</li>
			</ul>
			<div class="results-count">Found {{ resultsCount }} results</div>
			<slot v-if="resultType === ResourceType.XDD" name="xdd"></slot>
			<slot v-if="resultType === ResourceType.MODEL" name="model"></slot>
		</div>
		<models-listview
			v-if="resultType === ResourceType.MODEL"
			class="list-view"
			:models="filteredModels"
			:enable-multiple-selection="enableMultipleSelection"
			:selected-search-items="selectedSearchItems"
			@toggle-model-selected="toggleDataItemSelected"
			@set-model-selected="setDataItemSelected"
		/>
		<articles-listview
			v-if="resultType === ResourceType.XDD"
			class="list-view"
			:articles="filteredArticles"
			:enable-multiple-selection="enableMultipleSelection"
			:selected-search-items="selectedSearchItems"
			@toggle-article-selected="toggleDataItemSelected"
			@set-article-selected="setDataItemSelected"
		/>
		<common-listview
			v-if="resultType === ResourceType.ALL"
			class="list-view"
			:input-items="dataItems"
		/>
	</div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { Model } from '@/types/Model';
import { XDDArticle } from '@/types/XDD';
import { SearchResults, ResourceType } from '@/types/common';
import ModelsListview from '@/components/data-explorer/models-listview.vue';
import ArticlesListview from '@/components/data-explorer/articles-listview.vue';
import CommonListview from '@/components/data-explorer/common-listview.vue';

export default defineComponent({
	name: 'Search',
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
		enableMultipleSelection: {
			type: Boolean,
			default: false
		},
		selectedSearchItems: {
			type: Array as PropType<string[]>,
			required: true
		},
		resultType: {
			type: String,
			default: ResourceType.ALL
		},
		resultsCount: {
			type: Number,
			default: 0
		}
	},
	emits: ['toggle-data-item-selected', 'set-data-item-selected', 'result-type-changed'],
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
		toggleDataItemSelected(item: string) {
			this.$emit('toggle-data-item-selected', item);
		},
		setDataItemSelected(item: string) {
			this.$emit('set-data-item-selected', item);
		},
		onResultTypeChanged(newResultType: string) {
			this.$emit('result-type-changed', newResultType);
		}
	}
});
</script>

<style scoped>
.search-container {
	min-height: 0px;
	width: 100%;
	display: flex;
	flex-direction: column;
	padding-right: 10px;
	gap: 1px;
}

.list-view {
	flex: 1;
	min-height: 0;
}

.nav-bar {
	display: flex;
	align-items: center;
	background-color: #f3f3f3;
}

.nav {
	list-style-type: none;
	margin: 0;
	padding: 0;
	overflow: hidden;
	border: 1px solid #e7e7e7;
	background-color: #f3f3f3;
	margin-right: 2rem;
	margin-left: 5rem;
}

.nav li {
	float: left;
}

.nav li button {
	display: block;
	color: #666;
	text-align: center;
	padding: 14px 16px;
	text-decoration: none;
	border-radius: 0;
	background: darkgray;
	border: none;
	padding: 4px 8px;
}

.nav li button:hover:not(.active) {
	background-color: #ddd;
	border: none;
}

.nav li button.active {
	color: white;
	background-color: var(--un-color-accent);
	border: none;
}

.results-count {
	color: var(--un-color-accent);
	margin-right: 2rem;
}
</style>
