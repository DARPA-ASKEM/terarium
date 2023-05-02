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
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue';

import TeraCategoricalFacet from '@/page/data-explorer/components/facets/tera-categorical-facet.vue';
import TeraNumericalFacet from '@/page/data-explorer/components/facets/tera-numerical-facet.vue';

import { FacetBucket, Facets, ResourceType } from '@/types/common';
import { getFacetNameFormatter, getFacetsDisplayNames } from '@/utils/facets';
import { XDDFacetsItemResponse } from '@/types/Types';

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
		if (props.filteredFacets[key] && 'buckets' in props.filteredFacets[key]) {
			// accessing via ['buckets'] for now
			buckets = props.filteredFacets[key].buckets;
		} else {
			buckets = props.filteredFacets[key];
		}

		const filteredFacetDict = props.filteredFacets[key]
			? buckets.reduce((dict, category) => {
					// eslint-disable-next-line no-param-reassign
					dict[category.key] = Number(category.docCount);
					return dict;
			  }, {} as { [key: string]: number })
			: {};

		// Temp hack fix while model/dataset facets are on divergent paths from XDD facets
		if (props.facets[key] && 'buckets' in props.facets[key]) {
			// accessing via ['buckets'] for now
			buckets = props.facets[key].buckets;
		} else {
			buckets = props.facets[key];
		}

		buckets.forEach((category) => {
			baseData.push({
				key: category.key,
				value: props.docCount
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
