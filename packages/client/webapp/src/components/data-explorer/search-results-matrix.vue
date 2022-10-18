<template>
	<div class="search-matrixview-container">
		<div class="table-fixed-head">
			<table>
				<thead>
					<tr>
						<th></th>
						<th v-for="v in clustersInfo.variables" :key="v">{{ v }}</th>
					</tr>
				</thead>
				<tbody>
					<tr
						v-for="c in clustersInfo.clusters"
						:key="c.name"
						class="tr-item"
						:class="{ selected: c.selected }"
					>
						<td class="name-col">
							<div class="name-layout">
								<div class="radio" @click.stop="updateSelection(c)">
									<span v-show="c.selected"><i class="fa-lg fa-regular fa-square-check"></i></span>
									<span v-show="!c.selected"><i class="fa-lg fa-regular fa-square"></i></span>
								</div>
								<div class="content">
									<div>{{ c.name + ' (' + c.items.length + ')' }}</div>
								</div>
							</div>
						</td>
						<td v-for="v in clustersInfo.variables" :key="v">
							<div v-if="c.clusterVariables.includes(v)" class="preview-container"></div>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { Model } from '@/types/Model';
import { XDDArticle } from '@/types/XDD';
import { SearchResults, ResourceType, ResultType } from '@/types/common';
import { groupBy } from 'lodash';
import { isModel, isXDDArticle } from '@/utils/data-util';

type ResultsCluster = {
	name: string;
	selected: boolean;
	items: ResultType[];
	clusterVariables: string[];
};

export default defineComponent({
	name: 'SearchResultsMatrix',
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
		},
		clustersInfo() {
			const res = [] as ResultsCluster[];
			const vars = [] as string[];

			if (this.resultType === ResourceType.MODEL) {
				const clusterVariable = 'status';
				const clusteredModels = groupBy(this.filteredModels, clusterVariable);
				const names = Object.keys(clusteredModels);
				vars.push(...names);
				names.forEach((name) => {
					// are all the cluster items selected?
					const clusterItems = clusteredModels[name];
					// FIXME: this is not reflected in the facets panel
					const isClusterSelected = clusterItems.every((clusterItem) =>
						this.isDataItemSelected(clusterItem)
					);
					const c: ResultsCluster = {
						name,
						selected: isClusterSelected,
						items: clusterItems,
						clusterVariables: [name]
					};
					res.push(c);
				});
			}

			if (this.resultType === ResourceType.XDD) {
				const clusterVariable = 'journal';
				const clusteredArticles = groupBy(this.filteredArticles, clusterVariable);
				const names = Object.keys(clusteredArticles);
				vars.push(...names);
				names.forEach((name) => {
					// are all the cluster items selected?
					const clusterItems = clusteredArticles[name];
					// FIXME: this is not reflected in the facets panel
					const isClusterSelected = clusterItems.every((clusterItem) =>
						this.isDataItemSelected(clusterItem)
					);
					const c: ResultsCluster = {
						name,
						selected: isClusterSelected,
						items: clusterItems,
						clusterVariables: [name]
					};
					res.push(c);
				});
			}

			return {
				clusters: res,
				variables: vars
			};
		}
	},
	methods: {
		updateSelection(cluster: ResultsCluster) {
			cluster.selected = !cluster.selected;
			cluster.items.forEach((item) => {
				this.$emit('toggle-data-item-selected', item);
			});
		},
		isDataItemSelected(item: ResultType) {
			// FIXME: refactor as util func
			return this.selectedSearchItems.find((searchItem) => {
				if (isModel(item)) {
					const itemAsModel = item as Model;
					const searchItemAsModel = searchItem as Model;
					return searchItemAsModel.id === itemAsModel.id;
				}
				if (isXDDArticle(item)) {
					const itemAsArticle = item as XDDArticle;
					const searchItemAsArticle = searchItem as XDDArticle;
					return searchItemAsArticle.title === itemAsArticle.title;
				}
				return false;
			});
		}
	}
});
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
.search-matrixview-container {
	background: $background-light-2;
	color: black;
	min-height: 0px;
	width: 100%;
	display: flex;
	flex-direction: column;
	padding-right: 10px;
	gap: 1px;
	flex: 1;

	table {
		border-collapse: collapse;
		width: 100%;
		vertical-align: top;
	}
	th,
	td {
		padding: 2px 4px;
	}
	tr {
		border: 2px solid $separator;
	}
	thead {
		tr {
			border: none;
		}

		th {
			border: none;
			text-align: left;
		}
	}
	td {
		background: $background-light-1;
	}
	tr th {
		font-size: $font-size-small;
		font-weight: normal;
	}
	.table-fixed-head {
		overflow-y: auto;
		overflow-x: hidden;
		height: 100%;
		width: 100%;
	}
	.table-fixed-head thead th {
		transform: rotate(-180deg);
		writing-mode: vertical-lr;
	}

	.tr-item {
		height: 50px;
		padding: 8px;
	}
	.tr-item.selected {
		border: 2px double black;
	}
	.name-col {
		width: 20%;
		.name-layout {
			display: flex;
			align-content: stretch;
			align-items: stretch;
			.radio {
				flex: 0 0 auto;
				align-self: flex-start;
				margin: 0px 5px 0 0;
			}
			.content {
				flex: 1 1 auto;
				overflow-wrap: anywhere;
			}
		}
	}
	.preview-container {
		background-color: lightgray;
		height: 50px;
		width: 50px;
		margin: 0 auto;
	}
}
</style>
