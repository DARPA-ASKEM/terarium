<template>
	<div class="search-container">
		<div class="nav-bar">
			<ul class="nav">
				<li>
					<button
						type="button"
						:class="{ active: resultType === 'all' }"
						@click="onResultTypeChanged('all')"
					>
						All
					</button>
				</li>
				<li>
					<button
						type="button"
						:class="{ active: resultType === 'datacube' }"
						@click="onResultTypeChanged('datacube')"
					>
						Models
					</button>
				</li>
				<li>
					<button
						type="button"
						:class="{ active: resultType === 'xdd' }"
						@click="onResultTypeChanged('xdd')"
					>
						Articles
					</button>
				</li>
			</ul>
			<div class="results-count">Found {{ resultsCount }} results</div>
			<slot v-if="resultType === 'xdd'" name="xdd"></slot>
			<slot v-if="resultType === 'datacube'" name="datacube"></slot>
		</div>
		<datacubes-listview
			v-if="resultType === 'datacube'"
			class="list-view"
			:datacubes="filteredDatacubes"
			:enable-multiple-selection="enableMultipleSelection"
			:selected-search-items="selectedSearchItems"
			@toggle-datacube-selected="toggleDataItemSelected"
			@set-datacube-selected="setDataItemSelected"
		/>
		<articles-listview
			v-if="resultType === 'xdd'"
			class="list-view"
			:articles="filteredArticles"
			:enable-multiple-selection="enableMultipleSelection"
			:selected-search-items="selectedSearchItems"
			@toggle-article-selected="toggleDataItemSelected"
			@set-article-selected="setDataItemSelected"
		/>
		<common-listview v-if="resultType === 'all'" class="list-view" :input-items="dataItems" />
	</div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import DatacubesListview from '@/components/data-explorer/datacubes-listview.vue';
import ArticlesListview from '@/components/data-explorer/articles-listview.vue';
import CommonListview from '@/components/data-explorer/common-listview.vue';
import { Datacube } from '@/types/Datacube';
import { XDDArticle } from '@/types/XDD';
import { SearchResults } from '@/types/common';

export default defineComponent({
	name: 'Search',
	components: {
		DatacubesListview,
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
			default: 'all'
		},
		resultsCount: {
			type: Number,
			default: 0
		}
	},
	emits: ['toggle-data-item-selected', 'set-data-item-selected', 'result-type-changed'],
	computed: {
		filteredDatacubes() {
			const resList = this.dataItems.find((res) => res.searchSubsystem === 'datacube');
			if (resList) {
				return resList.results as Datacube[];
			}
			return [];
		},
		filteredArticles() {
			const resList = this.dataItems.find((res) => res.searchSubsystem === 'xdd');
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

	.nav-bar {
		display: flex;
		align-items: center;
		background-color: #f3f3f3;

		.nav {
			list-style-type: none;
			margin: 0;
			padding: 0;
			overflow: hidden;
			border: 1px solid #e7e7e7;
			background-color: #f3f3f3;
			margin-right: 2rem;
			margin-left: 5rem;

			li {
				float: left;
			}

			li button {
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

			li button:hover:not(.active) {
				background-color: #ddd;
				border: none;
			}

			li button.active {
				color: white;
				background-color: #04aa6d;
				border: none;
			}
		}

		.results-count {
			color: blue;
			margin-right: 2rem;
		}
	}
}
</style>
