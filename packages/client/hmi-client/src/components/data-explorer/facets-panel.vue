<template>
	<side-panel
		class="facet-panel-container"
		:tabs="facetTabs"
		:current-tab-name="currentTab"
		@set-active="setActive"
	>
		<div v-if="currentTab === tabName" class="facet-panel-list">
			<div v-for="facet in formattedFacets" :key="facet.label">
				<numerical-facet
					v-if="facet.isNumerical"
					:key="facet.label"
					:facet="facet.id"
					:label="facet.label"
					:base-data="facet.baseData"
					:selected-data="facet.filteredData"
				/>
				<categorical-facet
					v-else
					:key="facet.label"
					:facet="facet.id"
					:label="facet.label"
					:base-data="facet.baseData"
					:selected-data="facet.filteredData"
					:rescale-after-select="true"
				/>
			</div>
		</div>
	</side-panel>
</template>

<script setup lang="ts">
import { computed, PropType, ref } from 'vue';

import CategoricalFacet from '@/components/facets/categorical-facet.vue';
import NumericalFacet from '@/components/facets/numerical-facet.vue';

import { Facets, FacetBucket, ResourceType } from '@/types/common';
import { getFacetsDisplayNames } from '@/utils/facets';
import IconFilterRemove32 from '@carbon/icons-vue/es/filter--remove/32';
import SidePanel from '@/components/side-panel/side-panel.vue';

const TAB_NAME = 'Data Facets';

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

const tabName = ref(TAB_NAME);
// FIXME: add label for the facet tab that matches the current resultType
const facetTabs = ref([{ name: TAB_NAME, icon: IconFilterRemove32 }]);
const currentTab = ref(TAB_NAME);

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

const setActive = (tab: string) => {
	currentTab.value = tab;
};
</script>

<style scoped>
.facet-panel-container {
	margin-top: 5px;
}

.facet-panel-list {
	padding-bottom: 10rem;
}
</style>
