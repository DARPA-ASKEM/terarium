<template>
	<facet-terms
		:data.prop="facetData"
		:selection.prop="selection"
		:subselection.prop="subSelection"
		@facet-element-updated="updateSelection"
		action-buttons="0"
	>
		<!-- eslint-disable-next-line vue/no-deprecated-slot-attribute vue/first-attribute-linebreak -->
		<div slot="header-label">
			<span class="facet-font">{{ label }}</span>
		</div>

		<facet-template target="facet-terms-value" class="facet-pointer">
			<!-- eslint-disable-next-line vue/no-deprecated-slot-attribute vue/first-attribute-linebreak -->
			<div slot="label" class="facet-label-truncated facet-font" title="${label} - ${value}">
				${label}
			</div>
			<!-- eslint-disable-next-line vue/no-deprecated-slot-attribute vue/first-attribute-linebreak -->
			<div slot="value" class="facet-font">${value}</div>
		</facet-template>

		<!-- eslint-disable-next-line vue/no-deprecated-slot-attribute vue/first-attribute-linebreak -->
		<div slot="footer" class="facet-footer-container">
			<div class="facet-footer-more">
				<div class="facet-footer-more-section">
					<div class="facet-footer-more-count">
						<span v-if="facetMoreCount > 0">{{ facetMoreCount }} more</span>
					</div>
					<div class="facet-footer-more-controls">
						<span v-if="hasLess" class="less" @click="viewLess"> <IconChevronUp16 />less </span>
						<span v-if="hasMore" class="more" @click="viewMore"> <IconChevronDown16 />more </span>
					</div>
				</div>
			</div>
		</div>
	</facet-terms>
</template>

<script lang="ts">
/* eslint-disable */
// @ts-nocheck

import { defineComponent } from 'vue';

import { isEqual } from 'lodash';

import '@uncharted.software/facets-core';
import filtersUtil from '@/utils/filters-util';

import useQueryStore from '@/stores/query';
import IconChevronDown16 from '@carbon/icons-vue/es/chevron--down/16';
import IconChevronUp16 from '@carbon/icons-vue/es/chevron--up/16';

const FACET_DEFAULT_SIZE = 5;

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
	name: 'CategoricalFacet',
	components: {
		IconChevronDown16,
		IconChevronUp16
	},
	props: {
		baseData: {
			type: Array,
			default: () => []
		},
		facet: {
			type: String,
			default: null
		},
		formatterFn: {
			type: Function,
			default: null
		},
		label: {
			type: String,
			default: 'Facet'
		},
		selectedData: {
			type: Array,
			default: () => []
		},
		rescaleAfterSelect: {
			type: Boolean,
			default: false
		}
	},
	setup(props) {
		const query = useQueryStore();
		return {
			query
		};
	},
	data() {
		return {
			moreLevel: 0
		};
	},
	computed: {
		facetData() {
			const values = [];
			for (let i = 0; i < this.numToDisplay; ++i) {
				const b = this.sortedJoinedData[i];
				const labelName = this.formatterFn ? this.formatterFn(b.key) : b.key;
				values.push({
					label: labelName,
					ratio: b.value / this.max,
					value: `${b.selectedValue}/${b.value}`
				});
			}
			return {
				label: this.label,
				values
			};
		},
		facetMoreCount() {
			return this.baseData.length - this.numToDisplay;
		},
		hasLess() {
			return this.moreLevel > 0;
		},
		hasMore() {
			return this.moreLevel < 2 && this.facetMoreCount > 0;
		},
		keyIndexDict() {
			return this.sortedJoinedData.reduce((a, d, i) => {
				a[d.key] = i;
				return a;
			}, {});
		},
		max() {
			if (!this.rescaleAfterSelect) {
				const values = this.baseData.map((b) => b.value);
				return Math.max(...values);
			}
			const values = this.selectedData.map((s) => s.value);
			return Math.max(...values);
		},
		moreNumToDisplay() {
			switch (this.moreLevel) {
				case 0:
					return FACET_DEFAULT_SIZE;
				case 1:
					return this.baseData.length < 20 ? this.baseData.length : 20;
				case 2:
				default:
					return this.baseData.length;
			}
		},
		numToDisplay() {
			return this.baseData.length < FACET_DEFAULT_SIZE
				? this.baseData.length
				: this.moreNumToDisplay;
		},
		selection() {
			const facetClause = filtersUtil.findPositiveFacetClause(this.query.clientFilters, this.facet);
			if (facetClause) {
				const keyIndexDict = this.sortedJoinedData.reduce((a, d, i) => {
					a[d.key] = i;
					return a;
				}, {});
				const selectionDict = facetClause.values.reduce((acc, val) => {
					acc[keyIndexDict[val]] = true;
					return acc;
				}, {});
				return selectionDict;
			}
			return null;
		},
		sortedJoinedData() {
			const baseClone = [...this.baseData];
			const selectDictionary = this.selectedData.reduce((acc, s) => {
				acc[s.key] = s.value;
				return acc;
			}, {});

			baseClone.forEach((b) => {
				if (selectDictionary[b.key]) {
					b.selectedValue = selectDictionary[b.key];
				} else {
					b.selectedValue = 0;
				}
			});

			if (this.rescaleAfterSelect) {
				{
					baseClone.sort((a, b) => b.selectedValue - a.selectedValue);
				}
			} else {
				{
					baseClone.sort((a, b) => b.value - a.value);
				}
			}

			return baseClone;
		},
		subSelection() {
			return this.sortedJoinedData
				? this.sortedJoinedData.map((s) => s.selectedValue / this.max)
				: [];
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
					const selectedIndexes = Object.keys(facet.selection);
					const values = selectedIndexes.map((s) => this.sortedJoinedData[parseInt(s)].key);
					this.query.setSearchClause({ field: this.facet, values });
				} else {
					this.query.setSearchClause({ field: this.facet, values: [] });
				}
			}
		},
		viewMore() {
			this.moreLevel += 1;
		},
		viewLess() {
			this.moreLevel -= 1;
		}
	}
});
</script>

<style scoped>
.facet-pointer {
	cursor: pointer;
}

.facet-label-truncated {
	overflow-x: hidden;
	text-overflow: ellipsis;
	max-width: 150px;
}

.facet-footer-container {
	min-height: 12px;
	padding: 6px 12px 5px;
	font-size: 12px;
	font-weight: 600;
	line-height: 16px;
}

.facet-footer-more {
	margin-bottom: 4px;
}

.facet-footer-more-section {
	display: flex;
	flex-direction: row;
	flex-wrap: nowrap;
	justify-content: flex-start;
	align-content: stretch;
	align-items: flex-start;
}

.facet-footer-more-count {
	order: 0;
	flex: 1 1 auto;
	align-self: auto;
}

.facet-footer-more-controls {
	order: 0;
	flex: 0 1 auto;
	align-self: auto;
}

.more,
.less {
	display: inline-flex;
}

.facet-footer-more-controls > span {
	cursor: pointer;
}
</style>
