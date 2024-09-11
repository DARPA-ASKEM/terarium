<template>
	<div class="result-details">
		<span class="result-count">
			<template v-if="isLoading">Loading...</template>
			<template v-else-if="props.searchTerm || searchByExampleOptionsStr">
				{{ resultsText }}
				<span v-if="isEmpty(searchByExampleOptionsStr)"> "{{ props.searchTerm }}" </span>
				<div v-else-if="!isEmpty(searchByExampleOptionsStr)" class="search-by-example-card"></div>
			</template>
			<template v-else>{{ itemsText }} </template>
		</span>
	</div>
	<div v-if="!isEmpty(chosenFacets)" class="facet-chips">
		<template v-for="facet in chosenFacets">
			<Chip
				v-for="(value, index) in facet.values"
				:key="index"
				icon="pi pi-filter"
				removable
				@remove="removeFacetValue(facet.field, facet.values, value)"
				remove-icon="pi pi-times"
			>
				{{ facet.field.charAt(0).toUpperCase() + facet.field.slice(1) }}:<span>{{ value }}</span>
			</Chip>
		</template>
	</div>

	<!-- Loading animation -->
	<div v-if="isLoading" class="explorer-status loading-spinner">
		<Vue3Lottie :animationData="LoadingWateringCan" :height="200" :width="200" />
	</div>

	<!-- Nothing found -->
	<div v-else-if="resultsCount === 0" class="empty-state-image">
		<!-- <img src="@assets/svg/seed.svg" alt="Seed" /> -->
		<Vue3Lottie :animationData="EmptySeed" :height="150"></Vue3Lottie>
		<h4 class="no-results-found">No results found</h4>
		<span>Try adjusting your search or filters and try again.</span>
	</div>
</template>

<script setup lang="ts">
import EmptySeed from '@/assets/images/lottie-empty-seed.json';
import LoadingWateringCan from '@/assets/images/lottie-loading-watering-can.json';
import Chip from 'primevue/chip';
import { isEmpty } from 'lodash';
import { computed, PropType } from 'vue';
import { Vue3Lottie } from 'vue3-lottie';

// Delete potential
import { ResourceType, SearchResults } from '@/types/common'; // ResultType,
import { DatasetSource } from '@/types/search';
import type { Source } from '@/types/search';
import useQueryStore from '@/stores/query';
import { ClauseValue } from '@/types/Filter';
import { getSearchByExampleOptionsString } from '@/page/data-explorer/search-by-example'; // useSearchByExampleOptions
// import { isDataset, isModel } from '@/utils/data-util';

// const { searchByExampleItem } = useSearchByExampleOptions();

const props = defineProps({
	dataItems: {
		type: Array as PropType<SearchResults[]>,
		default: () => []
	},
	resourceType: {
		type: String as PropType<ResourceType>,
		default: ResourceType.ALL
	},
	searchTerm: {
		type: String,
		default: ''
	},
	isLoading: {
		type: Boolean,
		default: true
	},
	docCount: {
		type: Number,
		default: 0
	},
	source: {
		type: String as PropType<Source>,
		default: DatasetSource.TERARIUM
	}
});

// const selectedAsset: Ref<ResultType> = ref({} as ResultType);
// const isAdding = ref(false);

// const projectOptions = computed(() => {
// 	const items =
// 		useProjects().allProjects.value?.map((project) => ({
// 			label: project.name,
// 			command: async () => {
// 				let response: ProjectAsset['id'] | null = null;
// 				let assetName: string = '';
// 				isAdding.value = true;

// 				if (isModel(selectedAsset.value)) {
// 					const modelAsset: Model = selectedAsset.value as Model;

// 					const modelId = modelAsset.id;
// 					response = await useProjects().addAsset(AssetType.Model, modelId, project.id);
// 					assetName = modelAsset.header.name;
// 				} else if (isDataset(selectedAsset.value)) {
// 					let datasetId = selectedAsset.value.id;

// 					if (useProjects().hasEditPermission()) {
// 						if (!datasetId && selectedAsset.value.esgfId) {
// 							// The selectedAsset is a light asset for front end and we need the whole thing.
// 							const climateDataset: Dataset | null = await getClimateDataset(selectedAsset.value.esgfId);
// 							if (climateDataset) {
// 								const dataset: Dataset | null = await createDataset(climateDataset);
// 								if (dataset) {
// 									datasetId = dataset.id;
// 								}
// 							}
// 						}
// 					}

// 					// then, link and store in the project assets
// 					if (datasetId) {
// 						response = await useProjects().addAsset(AssetType.Dataset, datasetId, project.id);
// 						assetName = selectedAsset.value.name ?? '';
// 					}
// 				} else {
// 					const document = selectedAsset.value as DocumentAsset;
// 					const assetType = AssetType.Document;
// 					response = await useProjects().addAsset(assetType, document.id, project.id);
// 					assetName = document.name ?? '';
// 				}

// 				if (response) logger.info(`Added ${assetName} to ${project.name}`);

// 				isAdding.value = false;
// 			}
// 		})) ?? [];

// 	const lastProjectUpdated = orderBy(useProjects().allProjects.value, ['updatedOn'], ['desc'])[0];
// 	const lastUpdatedProjectItem = remove(items, (item) => item.label === lastProjectUpdated.name);

// 	return [
// 		{
// 			label: 'Add to which project?',
// 			items: [...lastUpdatedProjectItem, ...sortBy(items, (item) => item.label?.toString().toLowerCase())]
// 		}
// 	];
// });

// const previewedAsset = ref<ResultType | null>(null);

const chosenFacets = computed(() => useQueryStore().clientFilters.clauses);

const removeFacetValue = (field: string, values: ClauseValue[], valueToRemove: ClauseValue) => {
	const query = useQueryStore();
	values.splice(values.indexOf(valueToRemove), 1);
	query.setSearchClause({ field, values });
};

// const updateSelection = (asset: ResultType) => {
// 	selectedAsset.value = asset;
// };

// const togglePreview = (asset: ResultType) => {
// 	emit('toggle-data-item-selected', { item: asset, type: 'clicked' });
// 	previewedAsset.value = previewedAsset.value === asset ? null : asset;
// };

// const rawConceptFacets = computed(() => {
// 	const searchResults = props.dataItems.find((res) => res.searchSubsystem === props.resourceType);
// 	if (searchResults) {
// 		return searchResults.rawConceptFacets;
// 	}
// 	return null;
// });

const filteredAssets = computed(() => {
	const searchResults = props.dataItems.find((res) => res.searchSubsystem === props.resourceType)?.results ?? [];
	return searchResults;
});

const resultsCount = computed(() => {
	let total = 0;
	if (props.resourceType === ResourceType.ALL) {
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

const searchByExampleOptionsStr = computed(() => getSearchByExampleOptionsString());

const resultsText = computed(() => {
	if (resultsCount.value === 0) {
		return 'No results found for';
	}
	const truncated = props.docCount > resultsCount.value ? `of ${props.docCount} ` : '';
	const s = resultsCount.value === 1 ? '' : 's';
	const toOrFor = searchByExampleOptionsStr.value.length > 0 ? `with ${searchByExampleOptionsStr.value} to` : 'for';
	return `Showing ${resultsCount.value} ${truncated}result${s} ${toOrFor} `;
});

const itemsText = computed(() => {
	if (resultsCount.value === 0) {
		return 'No results found';
	}
	const truncated = props.docCount > resultsCount.value ? `of ${props.docCount} ` : '';
	const s = resultsCount.value === 1 ? '' : 's';
	return `Showing ${resultsCount.value} ${truncated}item${s}.`;
});
</script>

<style scoped>
ul {
	display: flex;
	flex-direction: column;
	list-style: none;
	overflow-y: scroll;
}

.explorer-status {
	display: flex;
	flex-direction: column;
	justify-content: center;
	gap: 1rem;
	align-items: center;
	margin-bottom: 8rem;
	flex-grow: 1;
	font-size: var(--font-body-small);
	color: var(--text-color-subdued);
}
.empty-state-image {
	display: flex;
	flex-direction: column;
	justify-content: center;
	gap: var(--gap-1);
	align-items: center;
	margin-bottom: 8rem;
	flex-grow: 1;
	font-size: var(--font-body-small);
	color: var(--text-color-subdued);
}

.pi-spinner {
	font-size: 8rem;
}

.no-results-found {
	font-weight: var(--font-weight);
	margin-top: var(--gap-1);
	font-size: 1.5rem;
}

.result-details {
	display: flex;
	align-items: center;
	overflow: visible;
	gap: 0.5rem;
}

.result-details {
	margin-left: var(--gap-small);
}

.facet-chips {
	display: inline-flex;
	gap: 1rem;
}

.p-chip {
	background-color: var(--surface-section);
	font-weight: 400;
}

.p-chip span {
	color: var(--text-color-primary);
	margin-left: var(--gap-1);
}

.search-container {
	overflow-y: auto;
}

.search-by-example-card {
	margin-top: 1rem;
	margin-bottom: 2rem;
}
</style>
