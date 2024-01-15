<template>
	<div class="result-details">
		<span class="result-count">
			<template v-if="isLoading">Loading...</template>
			<template v-else-if="props.searchTerm || searchByExampleOptionsStr">
				{{ resultsText }}
				<span v-if="searchByExampleOptionsStr.length === 0"> "{{ props.searchTerm }}" </span>
				<div v-else-if="searchByExampleOptionsStr.length > 0" class="search-by-example-card">
					<tera-asset-card
						:asset="searchByExampleItem!"
						:resource-type="resultType as ResourceType"
					/>
				</div>
			</template>
			<template v-else>{{ itemsText }} </template>
		</span>
	</div>
	<div v-if="chosenFacets.length > 0" class="facet-chips">
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
	<div v-if="isLoading" class="explorer-status loading-spinner">
		<div><i class="pi pi-spin pi-spinner" /></div>
	</div>
	<div v-else-if="resultsCount === 0" class="explorer-status">
		<img src="@assets/svg/seed.svg" alt="Seed" />
		<h2 class="no-results-found">No results found</h2>
		<span>Try adjusting your search or filters and try again.</span>
	</div>
	<ul v-else>
		<li v-for="(asset, index) in filteredAssets" :key="index">
			<tera-search-item
				:asset="asset as Document & Model & Dataset"
				:is-previewed="previewedAsset === asset"
				:is-adding-asset="isAdding && selectedAsset === asset"
				:resource-type="resultType as ResourceType"
				:search-term="searchTerm"
				:project-options="projectOptions"
				@select-asset="updateSelection(asset)"
				@toggle-asset-preview="togglePreview(asset)"
			/>
		</li>
	</ul>
</template>

<script setup lang="ts">
import { computed, PropType, ref } from 'vue';
import { AssetType, Dataset, Document, Model, XDDFacetsItemResponse } from '@/types/Types';
import useQueryStore from '@/stores/query';
import { ResourceType, ResultType, SearchResults } from '@/types/common';
import Chip from 'primevue/chip';
import { ClauseValue } from '@/types/Filter';
import TeraAssetCard from '@/page/data-explorer/components/tera-asset-card.vue';
import {
	getSearchByExampleOptionsString,
	useSearchByExampleOptions
} from '@/page/data-explorer/search-by-example';
import { useProjects } from '@/composables/project';
import { createDocumentFromXDD } from '@/services/document-assets';
import { isDataset, isDocument, isModel } from '@/utils/data-util';
import { logger } from '@/utils/logger';
import TeraSearchItem from './tera-search-item.vue';

const { searchByExampleItem } = useSearchByExampleOptions();

const emit = defineEmits(['toggle-data-item-selected']);

const props = defineProps({
	dataItems: {
		type: Array as PropType<SearchResults[]>,
		default: () => []
	},
	facets: {
		type: Object as PropType<{ [index: string]: XDDFacetsItemResponse }>,
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
	isLoading: {
		type: Boolean,
		default: true
	},
	docCount: {
		type: Number,
		default: 0
	}
});

const selectedAsset = ref();
const isAdding = ref(false);

const projectOptions = computed(() => [
	{
		label: 'Add to which project?',
		items:
			useProjects().allProjects.value?.map((project) => ({
				label: project.name,
				command: async () => {
					let response: any = null;
					let assetName = '';
					isAdding.value = true;

					if (isDocument(selectedAsset.value)) {
						const document = selectedAsset.value as Document;
						await createDocumentFromXDD(document, project.id as string);
						// finally add asset to project
						response = await useProjects().get(project.id);
						assetName = selectedAsset.value.title;
					}
					if (isModel(selectedAsset.value)) {
						// FIXME: handle cases where assets is already added to the project
						const modelId = selectedAsset.value.id;
						// then, link and store in the project assets
						const assetType = AssetType.Model;
						response = await useProjects().addAsset(assetType, modelId, project.id);
						assetName = selectedAsset.value.header.name;
					}
					if (isDataset(selectedAsset.value)) {
						// FIXME: handle cases where assets is already added to the project
						const datasetId = selectedAsset.value.id;
						// then, link and store in the project assets
						const assetType = AssetType.Dataset;
						if (datasetId) {
							response = await useProjects().addAsset(assetType, datasetId, project.id);
							assetName = selectedAsset.value.name;
						}
					}

					if (response) logger.info(`Added ${assetName} to ${project.name}`);
					else logger.error(`Failed adding ${assetName} to ${project.name}`);

					isAdding.value = false;
				}
			})) ?? []
	}
]);

// onMounted(() => {
// 	// To preview if the asset is already in a project we need to grab the assets of all projects...
// 	const projs =
// 		useProjects().allProjects.value?.forEach(async (project) => {
// 			const assets = await useProjects().get(project.id);
// 		    console.log(project, props.resultType, assets);
// 		}) ?? [];
// 	console.log(projs);
// });

const previewedAsset = ref<ResultType | null>(null);

const chosenFacets = computed(() => useQueryStore().clientFilters.clauses);

const removeFacetValue = (field: string, values: ClauseValue[], valueToRemove: ClauseValue) => {
	const query = useQueryStore();
	values.splice(values.indexOf(valueToRemove), 1);
	query.setSearchClause({ field, values });
};

const updateSelection = (asset: ResultType) => {
	selectedAsset.value = asset;
};

const togglePreview = (asset: ResultType) => {
	emit('toggle-data-item-selected', { item: asset, type: 'clicked' });
	previewedAsset.value = previewedAsset.value === asset ? null : asset;
};

// const rawConceptFacets = computed(() => {
// 	const searchResults = props.dataItems.find((res) => res.searchSubsystem === props.resultType);
// 	if (searchResults) {
// 		return searchResults.rawConceptFacets;
// 	}
// 	return null;
// });

const filteredAssets = computed(() => {
	const searchResults = props.dataItems.find((res) => res.searchSubsystem === props.resultType);

	if (searchResults) {
		if (props.resultType === ResourceType.XDD) {
			const documentSearchResults = searchResults.results as Document[];

			return [...documentSearchResults];
		}
		if (props.resultType === ResourceType.MODEL || props.resultType === ResourceType.DATASET) {
			return searchResults.results;
		}
	}
	return [];
});

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

const searchByExampleOptionsStr = computed(() => getSearchByExampleOptionsString());

const resultsText = computed(() => {
	if (resultsCount.value === 0) {
		return 'No results found for';
	}
	const truncated = props.docCount > resultsCount.value ? `of ${props.docCount} ` : '';
	const s = resultsCount.value === 1 ? '' : 's';
	const toOrFor =
		searchByExampleOptionsStr.value.length > 0
			? `with ${searchByExampleOptionsStr.value} to`
			: 'for';
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

.loading-spinner {
	color: var(--surface-highlight);
}

.pi-spinner {
	font-size: 8rem;
}

.no-results-found {
	font-weight: var(--font-weight);
	margin-top: 1.5rem;
}

.result-details {
	display: flex;
	align-items: center;
	overflow: visible;
	gap: 0.5rem;
}

.result-details {
	color: var(--text-color-subdued);
}

.result-count {
	font-size: var(--font-caption);
}

.result-count span {
	color: var(--text-color-primary);
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
	margin-left: 0.25rem;
}

.search-container {
	overflow-y: auto;
}

.search-by-example-card {
	margin-top: 1rem;
	margin-bottom: 2rem;
}
</style>
