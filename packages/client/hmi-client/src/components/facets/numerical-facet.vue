<template>
	<facet-bars
		:data.prop="facetData"
		:selection.prop="selection"
		:subselection.prop="subSelection"
		@facet-element-updated="updateSelection"
		action-buttons="0"
	>
		<div slot="header-label">{{ label }}</div>

		<facet-template
			v-if="facetData.values.length > 0"
			target="facet-bars-value"
			title="${tooltip}"
			class="facet-pointer"
		/>
		<div v-else slot="content" />

		<footer slot="footer" class="facet-footer-container">
			<facet-plugin-zoom-bar
				v-if="facetData.values.length > 0"
				min-bar-width="8"
				auto-hide="true"
				round-caps="true"
			/>
			<template v-else>No Data Available</template>
		</footer>
	</facet-bars>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { isEqual } from 'lodash';
import '@uncharted.software/facets-core';
import '@uncharted.software/facets-plugins';
import { FacetBucket } from '@/types/common';
import filtersUtil from '@/utils/filters-util';
import useQueryStore from '@/stores/query';

const query = useQueryStore();

const props = defineProps<{
	label: string;
	facet: string;
	selectedData: FacetBucket[];
	baseData: FacetBucket[];
}>();

const max = computed(() => Math.max(...props.baseData.map((b) => b.value)));

const facetData = computed(() => {
	const values = props.baseData.map((b) => ({
		ratio: b.value / max.value,
		label: b.key,
		tooltip: `${props.label}: ${b.key}\nCount: ${b.value}`
	}));
	return {
		label: props.label,
		values
	};
});

const selection = computed(() => {
	const facetClause = filtersUtil.findPositiveFacetClause(query.clientFilters, props.facet);
	if (facetClause) {
		const values = facetClause.values[0];
		const selIndexes = props.baseData.reduce(
			(a, b, i) => {
				if (parseFloat(b.key) <= parseFloat(values[0])) {
					a[0] = i; // largest matching index for the start
				} else if (parseFloat(b.key) <= parseFloat(values[1])) {
					a[1] = i; // largest matching index for the end
				}
				return a;
			},
			[null, null] as [number | null, number | null]
		);

		// if the largest key is still less than the last value or is a special string
		// set to the max length of the baseData array
		if (values[1] > props.baseData[props.baseData.length - 1].key || values[1] === '--') {
			selIndexes[1] = props.baseData?.length ?? 0;
		}
		return selIndexes;
	}

	return [0, props.baseData.length];
});
const subSelection = computed(() =>
	props.selectedData ? props.selectedData.map((s) => s.value / max.value) : []
);

const updateSelection = (event) => {
	const facet = event.currentTarget;
	if (
		event.detail.changedProperties.get('selection') !== undefined &&
		!isEqual(facet.selection, selection)
	) {
		if (facet.selection) {
			console.log('selection');
		}
	}
};
</script>

<style scoped>
.facet-pointer {
	cursor: pointer;
}
</style>
