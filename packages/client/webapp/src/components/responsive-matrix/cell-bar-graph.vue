<template>
	<div class="cell-selected-bar" ref="container">
	</div>
</template>

<script lang="ts">
	import { PropType } from 'vue';
	import { select } from 'd3-selection';
	import { scaleLinear, scaleBand } from 'd3-scale';
	import { axisBottom, axisLeft } from 'd3-axis';

	import { SelectedCellValue, SelectedCell } from '@/types/ResponsiveMatrix';

	export default {

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
				type: Array as unknown as PropType<SelectedCell>,
				default() {
					return [0, 0, 0, 0];
				},
			},
			dataRowList: {
				type: null,
				default() {
					return [];
				}
			},
			dataColList: {
				type: null,
				default() {
					return [];
				}
			},
			labelRowList: {
				type: Array as PropType<number[]>,
				default() {
					return [];
				},
			},
			labelColList: {
				type: Array as PropType<number[]>,
				default() {
					return [];
				},
			},
			parameters: {
				type: Array as PropType<string[]>,
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
				topMargin: 10,
				bottomMargin: 20,
				leftMargin: 30,
				rightMargin: 20,
				containerBoundingBox: {} as DOMRect,
				svg: null as any,
			};
		},



		// ---------------------------------------------------------------------------- //
		// computed                                                                     //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		computed: {
			selectedCells(): any {
				const startRow = this.selectedCell[SelectedCellValue.START_ROW];
				const endRow = this.selectedCell[SelectedCellValue.END_ROW];
				const startCol = this.selectedCell[SelectedCellValue.START_COL];
				const endCol = this.selectedCell[SelectedCellValue.END_COL];

				// const selectedCells: any[] = [];
				const selectedCells = this.parameters.reduce((acc, param) => {
						acc[param] = [];
						return acc;
					}, {});

				for(let row = startRow, i = 0; row <= endRow; row++, i++) {
					this.dataRowList[row].forEach((cell: any) => {
						if(cell._col <= endCol && cell._col >= startCol) {
							this.parameters.forEach(param => selectedCells[param].push(cell[param]));
						}
					});
				}

				return selectedCells;
			},

			labelRowSelected(): number[] {
				const startRow = this.selectedCell[SelectedCellValue.START_ROW];
				const endRow = this.selectedCell[SelectedCellValue.END_ROW];
				return this.labelRowList.slice(startRow, endRow + 1);
			},

			labelRowSelectedIndexMap(): number[] {
				return this.labelRowSelected;
			},

			labelColSelected(): number[] {
				const startCol = this.selectedCell[SelectedCellValue.START_COL];
				const endCol = this.selectedCell[SelectedCellValue.END_COL];
				return this.labelColList.slice(startCol, endCol + 1);
			},

			 // assume that timestep for all cells in same column are equal
			labelColSelectedMin(): number {
				return Math.min(...this.labelColSelected);
			},

			// assume that timestep for all cells in same column are equal
			labelColSelectedMax(): number {
				return Math.max(...this.labelColSelected);
			},

			parametersMinAll(): number {
				return Math.min(...Object.values(this.parametersMin) as number[]);
			},

			parametersMaxAll(): number {
				return Math.max(...Object.values(this.parametersMax) as number[]);
			},

			xRange(): any {
				return [0, this.containerBoundingBox.width - this.leftMargin - this.rightMargin];
			},

			xScaleLinear(): any {
				return scaleLinear()
					.domain([this.labelColSelectedMin, this.labelColSelectedMax])
					.range(this.xRange);
			},

			xScaleBand(): any {
				return scaleBand()
					.range(this.xRange)
					.domain(this.parameters)
					.padding(0.15);
			},

			xSubgroup(): any {
				return scaleBand()
					.domain(this.labelRowSelected as any)
					.range([0, this.xScaleBand.bandwidth()])
					.padding(0.1);
			},

			yScale(): any {
				return scaleLinear()
					.domain([this.parametersMaxAll, this.parametersMinAll])
					.range([0, this.containerBoundingBox.height - this.bottomMargin - this.topMargin]);
			},
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
			},
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

			renderGraph() {
				const container = this.$refs.container as HTMLElement;
				const { height } = this.containerBoundingBox;
				const {leftMargin, bottomMargin, topMargin} = this;

				// remove old svg if present
				if(this.svg) {
					(this.svg as any).remove();
				}

				this.svg = select(container)
					.append("svg")
					.attr('class', "cell-selected-line-graph")
					.attr("height", '100%')
					.attr("width", '100%')
					.style('background', 'white');

				const xAxis = axisBottom(this.xScaleBand);

				this.svg.append("g")
					.attr('transform', `translate(${leftMargin},${height - bottomMargin})`)
					.call(xAxis);

				const yAxis = axisLeft(this.yScale);

				this.svg.append("g")
					.attr('transform', `translate(${leftMargin},${topMargin})`)
				.call(yAxis);

				Object.keys(this.selectedCells).forEach(parameter => {
					this.renderBars(this.svg, parameter, this.selectedCells[parameter], this.labelRowSelected);
				});
			},

			renderBars(svg: any, parameter: string, colValueArray: any, rowValueArray: any) {
				const { height } = this.containerBoundingBox;
				const {topMargin, bottomMargin, leftMargin} = this;

				svg.append("g")
					.attr("transform", () => `translate(${this.xScaleBand(parameter)},0)`)
					.selectAll("rect")
					.data(colValueArray.map((v, i) => ({x: rowValueArray[i], y: v })))
					.enter().append("rect")
						.attr("x", d => this.xSubgroup(d.x) + leftMargin)
						.attr("y", d => this.yScale(d.y) + topMargin)
						.attr("width", this.xSubgroup.bandwidth())
						.attr("height", d => height - bottomMargin - topMargin - this.yScale(d.y))
						.attr("fill", () => this.colorFn(parameter));
			},
		},
	}
</script>