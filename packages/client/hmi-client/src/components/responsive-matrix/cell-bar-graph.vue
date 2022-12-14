<template>
	<div class="cell-selected-bar" ref="container"></div>
</template>

<script lang="ts">
import { PropType } from 'vue';
import { select, scaleLog, scaleBand, axisBottom, axisLeft, NumberValue } from 'd3';

import {
	D3SvgSelection,
	CellData,
	ParamMinMax,
	SelectedCellValue,
	SelectedCell,
	SelectedCellData
} from '@/types/ResponsiveMatrix';
import { formatAxis } from './matrix-util';

export default {
	// ---------------------------------------------------------------------------- //
	// props                                                                        //
	// ---------------------------------------------------------------------------- //

	props: {
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
	// data                                                                         //
	// ---------------------------------------------------------------------------- //

	data() {
		return {
			topMargin: 10,
			bottomMargin: 20,
			leftMargin: 30,
			rightMargin: 20,
			containerBoundingBox: {} as DOMRect,
			svg: null as unknown as D3SvgSelection
		};
	},

	// ---------------------------------------------------------------------------- //
	// computed                                                                     //
	// ---------------------------------------------------------------------------- //

	computed: {
		selectedCells(): SelectedCellData {
			const startRow = this.selectedCell[SelectedCellValue.START_ROW];
			const endRow = this.selectedCell[SelectedCellValue.END_ROW];
			const startCol = this.selectedCell[SelectedCellValue.START_COL];
			const endCol = this.selectedCell[SelectedCellValue.END_COL];

			// const selectedCells: any[] = [];
			const selectedCells = this.parameters.reduce((acc, param) => {
				acc[param] = [];
				return acc;
			}, {});

			this.iterateSelectionIndexes([startRow, -1, endRow, -1], (idx) =>
				this.extractCellValuesByParam(idx, selectedCells, startCol, endCol)
			);

			return selectedCells;
		},

		labelRowSelected() {
			const startRow = this.selectedCell[SelectedCellValue.START_ROW];
			const endRow = this.selectedCell[SelectedCellValue.END_ROW];
			return this.labelRowList.slice(startRow, endRow + 1);
		},

		labelColSelected() {
			const startCol = this.selectedCell[SelectedCellValue.START_COL];
			const endCol = this.selectedCell[SelectedCellValue.END_COL];
			return this.labelColList.slice(startCol, endCol + 1);
		},

		// assume that timestep for all cells in same column are equal
		labelColSelectedMin() {
			return Math.min(...this.labelColSelected);
		},

		// assume that timestep for all cells in same column are equal
		labelColSelectedMax() {
			return Math.max(...this.labelColSelected);
		},

		parametersMinAll() {
			return Math.min(...(Object.values(this.parametersMin) as number[]));
		},

		parametersMaxAll() {
			return Math.max(...(Object.values(this.parametersMax) as number[]));
		},

		xRange() {
			return [0, this.containerBoundingBox.width - this.leftMargin - this.rightMargin];
		},

		xScaleBand() {
			return scaleBand().range(this.xRange).domain(this.parameters).padding(0.15);
		},

		xSubgroup() {
			return scaleBand()
				.domain(this.labelRowSelected as any)
				.range([0, this.xScaleBand.bandwidth()])
				.padding(0.1);
		},

		yScale() {
			return scaleLog()
				.domain([this.parametersMaxAll, this.parametersMinAll])
				.range([0, this.containerBoundingBox.height - this.bottomMargin - this.topMargin]);
		}
	},

	// ---------------------------------------------------------------------------- //
	// mounted                                                                      //
	// ---------------------------------------------------------------------------- //

	// ///////////////////////////////////////////////////////////////////////////////

	mounted() {
		this.updateContainerBoundingBox();
		// this.renderGraph();
	},

	// ---------------------------------------------------------------------------- //
	// watch                                                                        //
	// ---------------------------------------------------------------------------- //

	// ///////////////////////////////////////////////////////////////////////////////

	watch: {
		data() {
			this.renderGraph();
		},

		update() {
			this.updateContainerBoundingBox();
			this.renderGraph();
		}
	},

	// ---------------------------------------------------------------------------- //
	// methods                                                                      //
	// ---------------------------------------------------------------------------- //

	// ///////////////////////////////////////////////////////////////////////////////

	methods: {
		updateContainerBoundingBox() {
			const container = this.$refs.container as HTMLElement;
			this.containerBoundingBox = container.getBoundingClientRect();
		},

		extractCellValuesByParam(idx, selectedCells, startCol, endCol) {
			const cell = this.dataCellList[idx];
			if (cell.col <= endCol && cell.col >= startCol) {
				this.parameters.forEach((param) => selectedCells[param].push(cell[param]));
			}
		},

		renderGraph() {
			const container = this.$refs.container as HTMLElement;
			const { height } = this.containerBoundingBox;
			const { leftMargin, bottomMargin, topMargin } = this;

			// remove old svg if present
			if (this.svg) {
				(this.svg as any).remove();
			}

			this.svg = select(container)
				.append('svg')
				.attr('class', 'cell-selected-line-graph')
				.attr('height', '100%')
				.attr('width', '100%')
				.style('background', 'white');

			const xAxisGen = axisBottom(this.xScaleBand);
			const xAxis = this.svg
				.append('g')
				.attr('transform', `translate(${leftMargin},${height - bottomMargin})`)
				.call(xAxisGen);
			formatAxis(xAxis);

			const yAxisGen = axisLeft(this.yScale).tickFormat(this.labelRowFormatFn).ticks(4);
			const yAxis = this.svg
				.append('g')
				.attr('transform', `translate(${leftMargin},${topMargin})`)
				.call(yAxisGen);
			formatAxis(yAxis);

			Object.keys(this.selectedCells).forEach((parameter) => {
				this.renderBars(this.svg, parameter, this.selectedCells[parameter], this.labelRowSelected);
			});

			const rangeText = this.labelColFormatFn(
				this.labelColList[this.selectedCell[SelectedCellValue.START_COL]],
				1
			);
			this.svg.append('text').attr('x', 50).attr('y', 20).style('fill', '#333').text(rangeText);
		},

		renderBars(svg: any, parameter: string, colValueArray: any, rowValueArray: any) {
			const { height } = this.containerBoundingBox;
			const { topMargin, bottomMargin, leftMargin } = this;

			svg
				.append('g')
				.attr('transform', () => `translate(${this.xScaleBand(parameter)},0)`)
				.selectAll('rect')
				.data(colValueArray.map((v, i) => ({ x: rowValueArray[i], y: v })))
				.enter()
				.append('rect')
				.attr('x', (d) => (this.xSubgroup(d.x) || 0) + leftMargin)
				.attr('y', (d) => this.yScale(d.y) + topMargin)
				.attr('width', this.xSubgroup.bandwidth())
				.attr('height', (d) => height - bottomMargin - topMargin - this.yScale(d.y))
				.attr('fill', () => this.colorFn(parameter));
		}
	}
};
</script>

<style scoped>
.cell-selected-bar {
	border: 2px solid var(--un-color-black-10);
	border-radius: 5px;
}
</style>
