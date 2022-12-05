<template>
	<div class="cell-selected-line" ref="container"></div>
</template>

<script lang="ts">
import { PropType } from 'vue';
import { select, extent, scaleLinear, axisBottom, axisLeft } from 'd3';

import {
	D3SvgSelection,
	CellData,
	ParamMinMax,
	SelectedCellValue,
	SelectedCell,
	SelectedCellData
} from '@/types/ResponsiveMatrix';

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
			return scaleLinear()
				.domain(extent(this.labelColSelected) as [number, number])
				.range([0, this.containerBoundingBox.width - this.leftMargin - this.rightMargin]);
		},

		yScale() {
			return scaleLinear()
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

			const xAxis = axisBottom(this.xScale);

			this.svg
				.append('g')
				.attr('transform', `translate(${leftMargin},${height - bottomMargin})`)
				.call(xAxis);

			const yAxis = axisLeft(this.yScale);

			this.svg.append('g').attr('transform', `translate(${leftMargin},${topMargin})`).call(yAxis);

			Object.keys(this.selectedCells).forEach((parameter) => {
				this.renderLine(this.svg, parameter, this.selectedCells[parameter], this.labelColSelected);
			});
		},

		renderLine(
			svg: D3SvgSelection,
			parameter: string,
			yValueArray: number[],
			xValueArray: number[]
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
				.style('stroke-width', 2);
		}
	}
};
</script>
