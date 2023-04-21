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

import { Facets, FacetBucket, ResourceType } from '@/types/common';
import { getFacetsDisplayNames, getFacetNameFormatter } from '@/utils/facets';

const props = defineProps({
	facets: {
		type: Object as PropType<Facets>,
		default: () => {}
	},
	filteredFacets: {
		type: Object as PropType<Facets>,
		default: () => {}
	},
	resultType: {
		type: String,
		default: ResourceType.ALL
	}
});

// FIXME: add label for the facet tab that matches the current resultType

const formattedFacets = computed(() => {
	const keys = Object.keys(props.facets);

	// mux the filtered data and base data into facets.
	const facetList = keys.map((key) => {
		const baseData: FacetBucket[] = [];
		const filteredData: FacetBucket[] = [];

		const filteredFacetDict = props.filteredFacets[key]
			? props.filteredFacets[key].reduce((dict, category) => {
					// eslint-disable-next-line no-param-reassign
					dict[category.key] = category.value;
					return dict;
			  }, {} as { [key: string]: number })
			: {};

		props.facets[key].forEach((category) => {
			baseData.push({
				key: category.key,
				value: category.value
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
	return facetList;
});
</script>
