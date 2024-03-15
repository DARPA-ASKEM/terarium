<template>
	<div class="facets-panel">
		<div v-for="facet in formattedFacets" :key="facet.label">
			<tera-numerical-facet
				v-if="facet.isNumerical"
				:key="facet.label"
				:facet="facet.id"
				:label="facet.label"
				:base-data="facet.baseData"
				:selected-data="facet.filteredData"
			/>
			<tera-categorical-facet
				v-else
				:key="facet.id"
				:facet="facet.id"
				:label="facet.label"
				:base-data="facet.baseData"
				:selected-data="facet.filteredData"
				:rescale-after-select="true"
				:formatter-fn="facet.formatter"
			/>
		</div>
	</div>

	<!-- Skeleton for when no facets are found -->
	<div v-if="!formattedFacets.length">
		<div v-for="index in 4" :key="index" class="skeleton-facet-group">
			<div class="skeleton-facet-header" />
			<div v-for="index in 5" :key="index" class="skeleton-facet">
				<div class="skeleton-facet-bar-indicator" />
				<div class="skeleton-facet-label" />
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue';

import TeraCategoricalFacet from '@/page/data-explorer/components/facets/tera-categorical-facet.vue';
import TeraNumericalFacet from '@/page/data-explorer/components/facets/tera-numerical-facet.vue';

import { FacetBucket, Facets, ResourceType } from '@/types/common';
import { getFacetNameFormatter, getFacetsDisplayNames } from '@/utils/facets';
import type { XDDFacetsItemResponse } from '@/types/Types';

const BUCKETS = 'buckets';

const props = defineProps({
	facets: {
		type: Object as PropType<{ [index: string]: XDDFacetsItemResponse } | Facets>,
		default: () => {}
	},
	filteredFacets: {
		type: Object as PropType<{ [index: string]: XDDFacetsItemResponse } | Facets>,
		default: () => {}
	},
	resultType: {
		type: String,
		default: ResourceType.ALL
	},
	docCount: {
		type: Number,
		default: 0
	}
});

// FIXME: add label for the facet tab that matches the current resultType

const formattedFacets = computed(() => {
	const keys = Object.keys(props.facets);

	// mux the filtered data and base data into facets.
	return keys.map((key) => {
		const baseData: FacetBucket[] = [];
		const filteredData: FacetBucket[] = [];

		// Temp hack fix while model/dataset facets are on divergent paths from XDD facets
		let buckets;
		let docFacet = false;
		if (props.filteredFacets[key] && 'buckets' in props.filteredFacets[key]) {
			// accessing via ['buckets'] for now
			buckets = props.filteredFacets[key][BUCKETS];
			docFacet = true;
		} else {
			buckets = props.filteredFacets[key];
			docFacet = false;
		}

		const filteredFacetDict = props.filteredFacets[key]
			? buckets.reduce(
					(dict, category) => {
						// eslint-disable-next-line no-param-reassign
						dict[category.key] = docFacet ? Number(category.docCount) : category.value;
						return dict;
					},
					{} as { [key: string]: number }
				)
			: {};

		// Temp hack fix while model/dataset facets are on divergent paths from XDD facets
		if (props.facets[key] && 'buckets' in props.facets[key]) {
			// accessing via ['buckets'] for now
			buckets = props.facets[key][BUCKETS];
		} else {
			buckets = props.facets[key];
		}

		buckets.forEach((category) => {
			baseData.push({
				key: category.key,
				value: docFacet ? Number(props.docCount) : category.value
			});
			filteredData.push({
				key: category.key,
				value: filteredFacetDict[category.key] || 0
			});
		});

		return {
			id: key,
			label: getFacetsDisplayNames(props.resultType, key),
			isNumerical: false,
			formatter: getFacetNameFormatter(props.resultType, key),
			baseData,
			filteredData
		};
	});
});
</script>

<style scoped>
.skeleton-facet-group {
	padding: 1rem;
	padding-top: 0.5rem;
}
.skeleton-facet-header {
	height: 1rem;
	width: 8rem;
	background-color: var(--gray-100);
	margin-bottom: 0.5rem;
	border-radius: 3px;
}

.skeleton-facet-bar-indicator {
	width: 100;
	height: 0.5rem;
	background-color: var(--gray-100);
	margin-bottom: 0.25rem;
	border-radius: 3px;
}
.skeleton-facet-label {
	height: 0.75rem;
	width: 6rem;
	margin-bottom: 0.5rem;
	background-color: var(--gray-100);
	border-radius: 3px;
}
.facets-panel {
	overflow-y: auto;
}

.centered-text {
	position: absolute;
	top: 50%;
	left: 4.5rem;
	color: var(--text-color-secondary);
}
</style>
