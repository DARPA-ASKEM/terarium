<template>
	<div class="cell-selected-line" ref="container"></div>
</template>

<script lang="ts">
import _ from 'lodash';
import { PropType } from 'vue';
import {
	select,
	extent,
	scaleLinear,
	scaleLog,
	scaleTime,
	axisBottom,
	axisLeft,
	NumberValue
} from 'd3';

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

			const selectedCells = this.parameters.reduce((acc, param) => {
				acc[param] = [];
				return acc;
			}, {});

			this.iterateSelectionIndexes([startRow, -1, endRow, -1], (idx) =>
				this.extractCellValuesByParam(idx, selectedCells, startCol, endCol)
			);

			return selectedCells;
		},

		labelColSelected() {
			const startCol: number = this.selectedCell[SelectedCellValue.START_COL];
			const endCol = this.selectedCell[SelectedCellValue.END_COL];
			return this.labelColList.slice(startCol, endCol + 1);
		},

		parametersMinAll() {
			return Math.min(...(Object.values(this.parametersMin) as number[]));
		},

		parametersMaxAll() {
			return Math.max(...(Object.values(this.parametersMax) as number[]));
		},

		xScale() {
			if (this.labelColSelected[0]?.constructor === Date) {
				return scaleTime()
					.domain(extent(this.labelColSelected as unknown as Date[]) as [Date, Date])
					.range([0, this.containerBoundingBox.width - this.leftMargin - this.rightMargin]);
			}

			return scaleLinear()
				.domain(extent(this.labelColSelected as number[]) as [number, number])
				.range([0, this.containerBoundingBox.width - this.leftMargin - this.rightMargin]);
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

	mounted() {
		this.getContainerBoundingBox();
		// this.renderGraph();
	},

	// ---------------------------------------------------------------------------- //
	// watch                                                                        //
	// ---------------------------------------------------------------------------- //

	watch: {
		data() {
			this.renderGraph();
		},

		update() {
			this.getContainerBoundingBox();
			this.renderGraph();
		}
	},

	// ---------------------------------------------------------------------------- //
	// methods                                                                      //
	// ---------------------------------------------------------------------------- //

	methods: {
		getContainerBoundingBox() {
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
			const { width, height } = this.containerBoundingBox;
			const { leftMargin, bottomMargin, topMargin } = this;

			// remove old svg if present
			if (this.svg) {
				this.svg.remove();
			}

			this.svg = select(container)
				.append('svg')
				.attr('class', 'cell-selected-line-graph')
				.attr('height', '100%')
				.attr('width', '100%')
				.attr('viewBox', `0 0 ${width} ${height}`)
				.style('background', 'white');

			const xAxisGen = axisBottom(this.xScale).tickFormat(this.labelColFormatFn);
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

			const numRows = 4;
			const rowSize = 15;
			const colSize = 95;
			Object.keys(this.selectedCells).forEach((parameter, i) => {
				this.renderLine(this.svg, parameter, this.selectedCells[parameter], this.labelColSelected);

				this.svg
					.append('rect')
					.attr('x', 50 + colSize * Math.floor(i / numRows))
					.attr('y', 30 + (i % numRows) * rowSize)
					.attr('width', 8)
					.attr('height', 8)
					.attr('fill', this.colorFn(parameter));

				this.svg
					.append('text')
					.attr('x', 60 + colSize * Math.floor(i / numRows))
					.attr('y', 38 + (i % numRows) * rowSize)
					.attr('font-size', '80%')
					.style('fill', '#333')
					.text(parameter);
			});
			const rangeText = `From ${this.labelColFormatFn(
				_.first(this.labelColSelected as NumberValue[]) as NumberValue,
				1
			)} to ${this.labelColFormatFn(
				_.last(this.labelColSelected as NumberValue[]) as NumberValue,
				1
			)}`;
			this.svg.append('text').attr('x', 50).attr('y', 20).style('fill', '#333').text(rangeText);
		},

		renderLine(
			svg: D3SvgSelection,
			parameter: string,
			yValueArray: number[],
			xValueArray: number[] | Date[]
		) {
			const dataLength = yValueArray.length;

			let path = 'M';
			for (let i = 0; i < dataLength; i++) {
				path +=
					` ${this.xScale(xValueArray[i])},` +
					`${this.yScale(yValueArray[i])}` +
					` ${i !== dataLength - 1 ? ' L' : ''}`;
			}

			svg
				.append('g')
				.attr('transform', `translate(${this.leftMargin},${this.topMargin})`)
				.append('path')
				.attr('d', path)
				.style('fill', 'none')
				.style('stroke', this.colorFn(parameter))
				.style('stroke-width', 1.5);
		}
	}
};
</script>

<style scoped>
.cell-selected-line {
	border: 2px solid var(--un-color-black-10);
	border-radius: 5px;
}
</style>
