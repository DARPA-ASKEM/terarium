<template>
	<div class="search-matrixview-container">
		<table>
			<thead>
				<tr>
					<th></th>
					<th v-for="v in clustersInfo.variables" :key="v" :title="formatFullColumnName(v)">
						{{ formatColumnName(v) }}
					</th>
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
						<div v-if="isClusterIncludesVariable(c, v)" class="preview-container"></div>
					</td>
				</tr>
			</tbody>
		</table>
	</div>
</template>

<script setup lang="ts">
import { computed, PropType, ref } from 'vue';
import { Model } from '@/types/Model';
import { PUBLISHER, XDDArticle } from '@/types/XDD';
import { SearchResults, ResourceType, ResultType } from '@/types/common';
import { groupBy, omit, orderBy, uniq } from 'lodash';
import { isDataset, isModel, isXDDArticle } from '@/utils/data-util';
import { Dataset } from '@/types/Dataset';

export type ResultsCluster = {
	name: string;
	selected: boolean;
	items: ResultType[]; // rows
	clusterVariables: string[]; // columns
};

const props = defineProps({
	dataItems: {
		type: Array as PropType<SearchResults[]>,
		default: () => []
	},
	selectedSearchItems: {
		type: Array as PropType<ResultType[]>,
		required: true
	},
	dictNames: {
		type: Array as PropType<string[]>,
		default: () => []
	},
	resultType: {
		type: String,
		default: ResourceType.ALL
	}
});

const emit = defineEmits(['toggle-data-item-selected']);

const defaultClusterName = ref('[No Name]');

const isClusterIncludesVariable = (c: ResultsCluster, v: string) => {
	if (c.clusterVariables.length === 0 && v === defaultClusterName.value) return true;
	return c.clusterVariables.includes(v);
};

const updateSelection = (cluster: ResultsCluster) => {
	cluster.selected = !cluster.selected;
	cluster.items.forEach((item) => {
		emit('toggle-data-item-selected', item);
	});
};

// FIXME: refactor as util func
const isDataItemSelected = (item: ResultType) =>
	props.selectedSearchItems.find((searchItem) => {
		if (isModel(item)) {
			const itemAsModel = item as Model;
			const searchItemAsModel = searchItem as Model;
			return searchItemAsModel.id === itemAsModel.id;
		}
		if (isDataset(item)) {
			const itemAsDataset = item as Dataset;
			const searchItemAsDataset = searchItem as Dataset;
			return searchItemAsDataset.id === itemAsDataset.id;
		}
		if (isXDDArticle(item)) {
			const itemAsArticle = item as XDDArticle;
			const searchItemAsArticle = searchItem as XDDArticle;
			return searchItemAsArticle.title === itemAsArticle.title;
		}
		return false;
	});

const modelsMap = computed(() => {
	const modelMap: { [modelId: string]: Model } = {};
	const resList = props.dataItems.find((res) => res.searchSubsystem === ResourceType.MODEL);
	if (resList) {
		const models = resList.results as Model[];
		models.forEach((model) => {
			modelMap[model.id] = model;
		});
	}
	return modelMap;
});

const datasetsMap = computed(() => {
	const datsetMap: { [datasetId: string]: Dataset } = {};
	const resList = props.dataItems.find((res) => res.searchSubsystem === ResourceType.DATASET);
	if (resList) {
		const datasets = resList.results as Dataset[];
		datasets.forEach((dataset) => {
			datsetMap[dataset.id] = dataset;
		});
	}
	return datsetMap;
});

const filteredArticles = computed(() => {
	const resList = props.dataItems.find((res) => res.searchSubsystem === ResourceType.XDD);
	if (resList) {
		return resList.results as XDDArticle[];
	}
	return [] as XDDArticle[];
});

const rawConceptFacets = computed(() => {
	const resList = props.dataItems.find((res) => res.searchSubsystem === props.resultType);
	if (resList) {
		return resList.rawConceptFacets;
	}
	return null;
});

const getConceptNameFromCurie = (curie: string) =>
	rawConceptFacets.value?.facets.concepts[curie].name ?? curie;

const formatColumnName = (v: string) => {
	if (props.resultType === ResourceType.MODEL || props.resultType === ResourceType.DATASET) {
		v = getConceptNameFromCurie(v);
	}
	const maxColumnNameChars = 24;
	return v.length < maxColumnNameChars ? v : `${v.substring(0, maxColumnNameChars)}...`;
};

const formatFullColumnName = (v: string) => {
	if (props.resultType === ResourceType.MODEL || props.resultType === ResourceType.DATASET) {
		v = getConceptNameFromCurie(v);
	}
	return v;
};

const clustersInfo = computed(() => {
	const res = [] as ResultsCluster[];
	const vars = [] as string[];

	if (props.resultType === ResourceType.MODEL || props.resultType === ResourceType.DATASET) {
		const clusters = [] as ResultsCluster[];
		// concepts are columns or cluter variables
		const concepts = rawConceptFacets.value?.facets.concepts ?? {};
		const curies = Object.keys(concepts); // concept IDs

		vars.push(...curies);

		const getConceptsForItem = (item) =>
			rawConceptFacets.value?.results.filter((c) => c.id === item.id) ?? [];

		const rowItemsMap = props.resultType === ResourceType.MODEL ? modelsMap : datasetsMap;
		const rowItems = Object.values(rowItemsMap.value);

		// cluster models/datasets the share the same set of concepts
		// const rowItemsWithEmbeddedConcepts = rowItems.map(item => ({...item, concepts: getConceptsForItem(item)}))
		// const clusteredRowItems = groupBy(rowItemsWithEmbeddedConcepts, 'concepts');

		// one row per model/dataset (cluster)
		rowItems.forEach((item) => {
			if (rawConceptFacets.value) {
				// are all the cluster items selected?
				const conceptsForItem = getConceptsForItem(item);

				const clusterItemsRaw = [item];

				// FIXME: this is not reflected in the facets panel
				const isClusterSelected = clusterItemsRaw.every((clusterItem) =>
					isDataItemSelected(
						props.resultType === ResourceType.MODEL
							? modelsMap[clusterItem.id]
							: datasetsMap[clusterItem.id]
					)
				);

				// TEMP
				// FIXME: currently many (datasets) items are duplicate so we need to filter them out
				// only add if no similar cluster exist
				// REVIEW: since the following line checks for similarity by name, while it should also check if cluster have the same items/concepts
				const existingCluster = clusters.find((cluster) => cluster.name === item.name); // isEqual(cluster.items, clusterItemsRaw)
				if (!existingCluster) {
					const cluster: ResultsCluster = {
						name: item.name,
						selected: isClusterSelected,
						items: clusterItemsRaw,
						clusterVariables: conceptsForItem.map((c) => c.curie)
					};
					clusters.push(cluster);
				}
			}
		});

		// sort by using the concepts of the first item in the cluster
		// NOTE that the assumption here is that if the cluster has multiple items,
		//      then they would share the same set of concepts
		//
		// first sort by the number of concepts, then by the actual concepts
		const sortedClusters = orderBy(
			clusters,
			[
				(c) => getConceptsForItem(c.items[0]).length,
				(c) => getConceptsForItem(c.items[0]).map((n) => n.curie)
			],
			['desc']
		);
		res.push(...sortedClusters);
	}

	if (props.resultType === ResourceType.XDD) {
		// cluster by known_terms, e.g., genes, and if not available then by some default field
		let clusterVariable = PUBLISHER;
		let articlesToCluster = filteredArticles.value;
		let clusteredArticles: { [clusterKey: string]: XDDArticle[] } = {};
		const mutualExclusiveClutering = true;

		const areKnownTermsIncluded = props.dictNames.length > 0;
		if (areKnownTermsIncluded) {
			// REVIEW: not sure why when known_terms are included the results are coming as an array
			//         for each article although the known terms
			//         are actually included as a multi-key object in the known_terms array
			// FIXME: should we allow clustering across multiple known_terms?
			const dicNamesIndex = 0;

			clusterVariable = props.dictNames[dicNamesIndex];
			articlesToCluster = filteredArticles.value.map((ar) => ({
				...ar,
				[clusterVariable]:
					ar.knownTerms && ar.knownTerms.length > 0
						? ar.knownTerms[dicNamesIndex][clusterVariable]
						: ([] as XDDArticle[])
			}));

			if (mutualExclusiveClutering) {
				// i.e., each cluster will only includes the items that exactly match the cluster variable
				clusteredArticles = groupBy(articlesToCluster, clusterVariable);
			} else {
				// special clustering is needed
				//  since the cluster field (or key), e.g., known_terms, is an array
				// and also because each cluster may include items included in other clusters
				clusteredArticles = articlesToCluster.reduce((carry, element) => {
					if (element.knownTerms !== undefined) {
						if (element.knownTerms.length > 0) {
							// check current known terms and add them to the relevant cluster
							element.knownTerms[dicNamesIndex][clusterVariable].forEach((tag) => {
								carry[tag] = carry[tag] || [];
								carry[tag].push({ ...element });
							});
						} else {
							// no known terms associated with this article, so add to the default cluster (with key = '')
							const tag = '';
							carry[tag] = carry[tag] || [];
							carry[tag].push({ ...element });
						}
					}
					return carry;
				}, {});
			}
		} else {
			clusteredArticles = groupBy(articlesToCluster, clusterVariable);
		}

		const names = Object.keys(clusteredArticles);
		const invalidClusterNameIndex = names.findIndex((name) => name === '');
		if (invalidClusterNameIndex >= 0) {
			clusteredArticles[defaultClusterName.value] = clusteredArticles[''];
			clusteredArticles = omit(clusteredArticles, ['']); // remove invalid cluster
			names[invalidClusterNameIndex] = defaultClusterName.value;
		}

		vars.push(...names);
		let letterCounter = 'A'.charCodeAt(0);
		names.forEach((name) => {
			const clusterItems = clusteredArticles[name];

			// are all the cluster items selected?
			// FIXME: this is not reflected in the facets panel
			const isClusterSelected = clusterItems.every((clusterItem) =>
				isDataItemSelected(clusterItem)
			);

			const clusterVars = areKnownTermsIncluded
				? uniq(clusterItems.map((item) => (item as any)[clusterVariable]).flat())
				: [name];

			const clusterName = String.fromCharCode(letterCounter++);
			const c: ResultsCluster = {
				name: `Cluster ${clusterName}`, // name // 'Cluster ' + clusterName
				selected: isClusterSelected,
				items: clusterItems,
				clusterVariables: clusterVars
			};
			res.push(c);
		});
	}

	return {
		clusters: res, // @TODO: consider sorting clusters
		variables: vars
	};
});
</script>

<style lang="scss" scoped>
.search-matrixview-container {
	color: black;
	width: 100%;
	display: flex;
	flex-direction: column;
	gap: 1px;
	flex: 1;
	overflow-y: auto;
	overflow-x: auto;
	height: 100%;
	width: 100%;

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
		border: 2px solid var(--separator);
	}
	thead {
		tr {
			border: none;
		}

		th {
			border: none;
			text-align: left;
			transform: rotate(-180deg);
			writing-mode: vertical-lr;
			position: sticky;
			top: -1px;
			z-index: 1;
			font-weight: normal;
			/* FIXME: shouldn't need to be manually kept in sync with data explorer bg colour */
			background-color: var(--un-color-body-surface-background);
		}
	}
	td {
		background: var(--un-color-body-surface-primary);
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
			}
		}
	}
	.preview-container {
		background-color: gray;
		height: 30px;
		width: 20px;
		margin: 0 auto;
	}
}
</style>
