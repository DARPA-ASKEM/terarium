<template>
	<ResponsiveCellBarGraph
		class="cell cell-selected"
		:style="getSelectedCellStyle(selectedCell)"
		:update="update"
		:selectedCell="selectedCell"
		:dataRowList="dataRowList"
		:dataColList="dataColList"
		:labelRowList="labelRowList"
		:labelColList="labelColList"
		:parameters="parameters"
		:parametersMin="parametersMin"
		:parametersMax="parametersMax"
		:colorFn="colorFn"
		@click="$emit('click')"
	/>
</template>

<script lang="ts">
import { PropType } from 'vue';

import { CellData, ParamMinMax, SelectedCell, SelectedCellValue } from '@/types/ResponsiveMatrix';
import ResponsiveCellBarGraph from './cell-bar-graph.vue';

export default {
	// ---------------------------------------------------------------------------- //
	// emits                                                                        //
	// ---------------------------------------------------------------------------- //

	emits: ['click'],

	// ---------------------------------------------------------------------------- //
	// components                                                                   //
	// ---------------------------------------------------------------------------- //

	components: {
		ResponsiveCellBarGraph
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
		update: {
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
		dataRowList: {
			type: Array as PropType<CellData[][]>,
			default() {
				return [];
			}
		},
		dataColList: {
			type: Array as PropType<CellData[][]>,
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
			type: Array as PropType<number[]>,
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
		}
	},

	// ---------------------------------------------------------------------------- //
	// computed                                                                     //
	// ---------------------------------------------------------------------------- //

	// ///////////////////////////////////////////////////////////////////////////////

	computed: {
		selectedRows(): SelectedCell[] {
			const startRow = this.selectedCell[SelectedCellValue.START_ROW];
			const endRow = this.selectedCell[SelectedCellValue.END_ROW];
			const startCol = this.selectedCell[SelectedCellValue.START_COL];
			const endCol = this.selectedCell[SelectedCellValue.END_COL];

			const selectedRowArray: any[] = [];

			for (let row = startRow; row <= endRow; row++) {
				const selectedRow = Array(4);
				selectedRow[SelectedCellValue.START_ROW] = row;
				selectedRow[SelectedCellValue.END_ROW] = row;
				selectedRow[SelectedCellValue.START_COL] = startCol;
				selectedRow[SelectedCellValue.END_COL] = endCol;
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
}
</style>
