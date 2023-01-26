<template>
	<facet-bars
		:data.prop="facetData"
		:selection.prop="selection"
		:subselection.prop="subSelection"
		class="facet-font"
		@facet-element-updated="updateSelection"
	>
		<!-- eslint-disable-next-line vue/no-deprecated-slot-attribute vue/first-attribute-linebreak -->
		<div slot="header-label">
			<span class="facet-font">{{ label }}</span>
		</div>

		<facet-template
			v-if="facetData.values.length > 0"
			target="facet-bars-value"
			title="${tooltip}"
			class="facet-pointer"
		/>

		<!-- eslint-disable-next-line vue/no-deprecated-slot-attribute vue/first-attribute-linebreak -->
		<div v-else slot="content" />

		<!-- eslint-disable-next-line vue/no-deprecated-slot-attribute vue/first-attribute-linebreak -->
		<div slot="footer" v-if="facetData.values.length > 0" class="facet-footer-container">
			<facet-plugin-zoom-bar min-bar-width="8" auto-hide="true" round-caps="true" />
		</div>

		<!-- eslint-disable-next-line vue/no-deprecated-slot-attribute vue/first-attribute-linebreak -->
		<div v-else slot="footer" class="facet-footer-container">No Data Available</div>
	</facet-bars>
</template>

<script lang="ts">
/* eslint-disable */
// @ts-nocheck

import { defineComponent } from 'vue';

import { isEqual } from 'lodash';

import '@uncharted.software/facets-core';
import '@uncharted.software/facets-plugins';

import filtersUtil from '@/utils/filters-util';

import useQueryStore from '@/stores/query';

/**
 * Facet 3 component - displays aggregated search terms and update query state.
 * Note facet does not filter itself, this is to allow term disjunction queries.
 * Properties
 * - label: facet label
 * - facet: field key
 * - selectedData: Array of selected bins
 * - baseData: Array of unfiltered bins
 */
export default defineComponent({
	name: 'NumericalFacet',
	props: {
		label: {
			type: String,
			default: 'Facet'
		},
		facet: {
			type: String,
			default: null
		},
		selectedData: {
			type: Array,
			default: () => []
		},
		baseData: {
			type: Array,
			default: () => []
		}
	},
	setup(props) {
		const query = useQueryStore();
		return {
			query
		};
	},
	computed: {
		max() {
			const values = this.baseData.map((b) => b.value);
			return Math.max(...values);
		},
		facetData() {
			const values = this.baseData.map((b) => {
				return {
					ratio: b.value / this.max,
					label: b.key,
					tooltip: `${this.label}: ${b.key}\nCount: ${b.value}`
				};
			});
			return {
				label: this.label,
				values
			};
		},
		selection() {
			const facetClause = filtersUtil.findPositiveFacetClause(this.query.filters, this.facet);
			if (facetClause) {
				const values = facetClause.values[0];
				const selIndexes = this.baseData.reduce(
					(a, b, i) => {
						if (parseFloat(b.key) <= parseFloat(values[0])) {
							a[0] = i; // largest matching index for the start
						} else if (parseFloat(b.key) <= parseFloat(values[1])) {
							a[1] = i; // largest matching index for the end
						}
						return a;
					},
					[null, null]
				);

				// if the largest key is still less than the last value or is a special string
				// set to the max length of the baseData array
				if (values[1] > this.baseData[this.baseData.length - 1].key || values[1] === '--') {
					selIndexes[1] = this.baseData.length;
				}
				return selIndexes;
			} else {
				return [0, this.baseData.length];
			}
		},
		subSelection() {
			return this.selectedData ? this.selectedData.map((s) => s.value / this.max) : [];
		}
	},
	methods: {
		updateSelection(event) {
			const facet = event.currentTarget;
			if (
				event.detail.changedProperties.get('selection') !== undefined &&
				!isEqual(facet.selection, this.selection)
			) {
				if (facet.selection) {
					const from = this.baseData[facet.selection[0]].key;

					// HACK: numEvidence key has a custom filter expectation for 5+
					const to =
						this.facet === 'numEvidence' && facet.selection[1] >= 5
							? '--'
							: facet.selection[1] !== this.baseData.length
							? this.baseData[facet.selection[1]].key
							: (this.baseData[1].key - this.baseData[0].key) * this.baseData.length +
							  this.baseData[0].key;
					this.query.setSearchClause({ field: this.facet, values: [[from, to]] });
				} else {
					this.query.setSearchClause({ field: this.facet, values: [[0, this.baseData.length]] });
				}
			}
		}
	}
});
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.facet-font {
	font-family: 'Source Sans Pro', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
		'Helvetica Neue', Arial, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol';
}

.facet-pointer {
	cursor: pointer;
}
</style>
