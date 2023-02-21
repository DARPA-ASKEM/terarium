<template>
	<main>
		<template v-for="facet in formattedFacets" :key="facet.id">
			<numerical-facet
				v-if="facet.isNumerical || facet.id === 'year'"
				:facet="facet.id"
				:label="facet.label"
				:base-data="facet.baseData"
				:selected-data="facet.filteredData"
			/>
			<categorical-facet
				v-else
				:facet="facet.id"
				:label="facet.label"
				:base-data="facet.baseData"
				:selected-data="facet.filteredData"
				:rescale-after-select="true"
			/>
		</template>
	</main>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue';

import CategoricalFacet from '@/components/facets/categorical-facet.vue';
import NumericalFacet from '@/components/facets/numerical-facet.vue';

import { Facets, FacetBucket, ResourceType } from '@/types/common';
import { getFacetsDisplayNames } from '@/utils/facets';

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
			baseData,
			filteredData
		};
	});
	return facetList;
});
</script>

<style scoped>
.facet-blueprint-content {
	background-color: var(--surface-a);
}

.slot-header {
	padding: 2px 15px 0px 10px;
}

.slot-header,
.slot-left,
.slot-right {
	background-color: var(--surface-a);
}
</style>
