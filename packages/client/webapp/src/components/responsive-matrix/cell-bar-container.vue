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
	import { SelectedCellValue, getSelectedCellStyle } from './matrix.vue';
	import ResponsiveCellBarGraph from './cell-bar-graph.vue';

	export default {

		// ---------------------------------------------------------------------------- //
		// emits                                                                        //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		emits: ["click"],



		// ---------------------------------------------------------------------------- //
		// components                                                                   //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		components: {
			ResponsiveCellBarGraph,
		},



		// ---------------------------------------------------------------------------- //
		// props                                                                        //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		props: {
			update: {
				type: Number,
				default() {
					return 0;
				},
			},
			selectedCell: {
				type: Array,
				default() {
					return [0, 0, 0, 0];
				},
			},
			dataRowList: {
				type: null,
				default() {
					return [];
				},
			},
			dataColList: {
				type: null,
				default() {
					return [];
				},
			},
			labelRowList: {
				type: Array,
				default() {
					return [];
				},
			},
			labelColList: {
				type: Array,
				default() {
					return [];
				},
			},
			parameters: {
				type: Array,
				default() {
					return [];
				},
			},
			parametersMin: {
				type: Object,
				default() {
					return {};
				},
			},
			parametersMax: {
				type: Object,
				default() {
					return {};
				},
			},
			colorFn: {
				type: Function,
				default() {
					return '#000000';
				},
			},
		},



		// ---------------------------------------------------------------------------- //
		// data                                                                         //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		data() {
			return {

			};
		},



		// ---------------------------------------------------------------------------- //
		// computed                                                                     //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		computed: {

			selectedRows(): any {
				const startRow = this.selectedCell[SelectedCellValue.START_ROW];
				const endRow = this.selectedCell[SelectedCellValue.END_ROW];
				const startCol = this.selectedCell[SelectedCellValue.START_COL];
				const endCol = this.selectedCell[SelectedCellValue.END_COL];

				const selectedRowArray: any[] = [];

				for(let row = startRow; row <= endRow; row++) {
					const selectedRow = Array(4);
					selectedRow[SelectedCellValue.START_ROW] = row;
					selectedRow[SelectedCellValue.END_ROW] = row;
					selectedRow[SelectedCellValue.START_COL] = startCol;
					selectedRow[SelectedCellValue.END_COL] = endCol;
					selectedRowArray.push(selectedRow);
				}

				return selectedRowArray;
			},

			// parametersMinAll(): number {
			// 	return Math.min(...Object.values(this.parametersMin) as number[]);
			// },

			// parametersMaxAll(): number {
			// 	return Math.max(...Object.values(this.parametersMax) as number[]);
			// },
		},



		// ---------------------------------------------------------------------------- //
		// mounted                                                                      //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		mounted() {

		},



		// ---------------------------------------------------------------------------- //
		// watch                                                                        //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		watch: {

		},



		// ---------------------------------------------------------------------------- //
		// methods                                                                      //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		methods: {
			getSelectedCellStyle(selectedCell: any): any {
				return getSelectedCellStyle(selectedCell);
			},
		},
	}
</script>