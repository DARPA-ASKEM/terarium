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

<script lang="ts">
import { defineComponent, PropType } from 'vue';

import CategoricalFacet from '@/components/facets/categorical-facet.vue';
import NumericalFacet from '@/components/facets/numerical-facet.vue';
import SidePanel from '@/components/side-panel/side-panel.vue';

import { Facets, FacetBucket } from '@/types/common';
import { getFacetsDisplayNames } from '@/utils/facets';

const TAB_NAME = 'Data Facets';

export default defineComponent({
	name: 'FacetsPanel',
	components: {
		CategoricalFacet,
		NumericalFacet,
		SidePanel
	},
	props: {
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
			default: 'all'
		}
	},
	watch: {
		resultType: {
			handler() {
				this.setActive(this.resultType === 'all' ? '' : this.tabName);
			},
			immediate: true
		}
	},
	data: () => ({
		tabName: TAB_NAME,
		// FIXME: add label for the facet tab that matches the current resultType
		facetTabs: [{ name: TAB_NAME, icon: 'fa-lg fa-solid fa-file-lines' }],
		currentTab: TAB_NAME
	}),
	computed: {
		formattedFacets() {
			const keys = Object.keys(this.facets);

			// mux the filtered data and base data into facets.
			const facetList = keys.map((key) => {
				const baseData: FacetBucket[] = [];
				const filteredData: FacetBucket[] = [];

				const filteredFacetDict = this.filteredFacets[key]
					? this.filteredFacets[key].reduce((dict, category) => {
							// eslint-disable-next-line no-param-reassign
							dict[category.key] = category.value;
							return dict;
					  }, {} as { [key: string]: number })
					: {};

				this.facets[key].forEach((category) => {
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
					label: getFacetsDisplayNames(this.resultType, key),
					isNumerical: false,
					baseData,
					filteredData
				};
			});
			return facetList;
		}
	},
	methods: {
		setActive(tab: string) {
			this.currentTab = tab;
		}
	}
});
</script>

<style lang="scss" scoped>
@import '../../styles/variables';
.facet-panel-container {
	margin-top: 5px;
}
.facet-panel-list {
	padding-bottom: 10rem;
}
</style>
