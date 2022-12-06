<template>
	<ResponsiveCellLineGraph
		class="cell cell-selected"
		v-for="(selectedRow, idx) in selectedRows"
		:key="idx"
		:iterateSelectionIndexes="iterateSelectionIndexes"
		:style="getSelectedCellStyle(selectedRow)"
		:update="update"
		:selectedCell="selectedRow"
		:dataCellList="dataCellList"
		:labelRowList="labelRowList"
		:labelColList="labelColList"
		:parameters="parameters"
		:parametersMin="parametersMin"
		:parametersMax="parametersMax"
		:colorFn="colorFn"
		:labelRowFormatFn="labelRowFormatFn"
		:labelColFormatFn="labelColFormatFn"
		@click="$emit('click')"
	/>
</template>

<script lang="ts">
import { PropType } from 'vue';
import { NumberValue } from 'd3';

import { CellData, ParamMinMax, SelectedCell, SelectedCellValue } from '@/types/ResponsiveMatrix';
import ResponsiveCellLineGraph from './cell-line-graph.vue';

export default {
	// ---------------------------------------------------------------------------- //
	// emits                                                                        //
	// ---------------------------------------------------------------------------- //

	emits: ['click'],

	// ---------------------------------------------------------------------------- //
	// components                                                                   //
	// ---------------------------------------------------------------------------- //

	components: {
		ResponsiveCellLineGraph
	},

	// ---------------------------------------------------------------------------- //
	// props                                                                        //
	// ---------------------------------------------------------------------------- //

	props: {
		getSelectedCellStyle: {
			type: Function,
			default() {
				return { top: '0px', left: '0px', bottom: '0px', right: '0px' };
			}
		},
		iterateSelectionIndexes: {
			type: Function,
			default() {
				return () => {};
			}
		},
		update: {
			type: Number,
			default() {
				return 0;
			}
		},
		move: {
			type: Number,
			default() {
				return 0;
			}
		},
		selectedCell: {
			type: Array as unknown as PropType<SelectedCell>,
			default() {
				return [0, 0, 0, 0];
			}
		},
		dataCellList: {
			type: Array as PropType<CellData[]>,
			default() {
				return [];
			}
		},
		labelRowList: {
			type: Array as PropType<number[]>,
			default() {
				return [];
			}
		},
		labelColList: {
			type: Array as PropType<number[] | Date[]>,
			default() {
				return [];
			}
		},
		parameters: {
			type: Array as PropType<string[]>,
			default() {
				return [];
			}
		},
		parametersMin: {
			type: Object as PropType<ParamMinMax>,
			default() {
				return {};
			}
		},
		parametersMax: {
			type: Object as PropType<ParamMinMax>,
			default() {
				return {};
			}
		},
		colorFn: {
			type: Function,
			default() {
				return '#000000';
			}
		},
		labelRowFormatFn: {
			type: Function as PropType<(value: NumberValue, index: number) => string>,
			default(v) {
				return v;
			}
		},
		labelColFormatFn: {
			type: Function as PropType<(value: NumberValue, index: number) => string>,
			default(v) {
				return v;
			}
		}
	},

	// ---------------------------------------------------------------------------- //
	// computed                                                                     //
	// ---------------------------------------------------------------------------- //

	// ///////////////////////////////////////////////////////////////////////////////

	computed: {
		selectedRows(): SelectedCell[] {
			const { START_ROW, END_ROW, START_COL, END_COL } = SelectedCellValue;

			const startRow = this.selectedCell[START_ROW];
			const endRow = this.selectedCell[END_ROW];
			const startCol = this.selectedCell[START_COL];
			const endCol = this.selectedCell[END_COL];

			const selectedRowArray: SelectedCell[] = [];

			for (let row = startRow; row <= endRow; row++) {
				const selectedRow = Array(4) as SelectedCell;
				selectedRow[START_ROW] = row;
				selectedRow[END_ROW] = row;
				selectedRow[START_COL] = startCol;
				selectedRow[END_COL] = endCol;
				selectedRowArray.push(selectedRow);
			}

			return selectedRowArray;
		}
	}
};
</script>

<style scoped>
.cell {
	position: absolute;
	pointer-events: none;
	user-select: none;
}
</style>
