<template>
	<div class="cell-selected-line" ref="container">
	</div>
</template>

<script lang="ts">
	import { select } from 'd3-selection';
	import { scaleLinear } from 'd3-scale';
	import { axisBottom, axisLeft } from 'd3-axis';

	import { SelectedCellValue } from './matrix.vue';

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
				type: Array,
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
				topMargin: 10,
				bottomMargin: 20,
				leftMargin: 30,
				rightMargin: 20,
				containerBoundingBox: {},
				svg: null,
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
						if(cell.__col__ <= endCol && cell.__col__ >= startCol) {
							this.parameters.forEach(param => selectedCells[param].push(cell[param]));
						}
					});
				}

				return selectedCells;
			},

			labelColSelected(): [] {
				const startCol = this.selectedCell[SelectedCellValue.START_COL];
				const endCol = this.selectedCell[SelectedCellValue.END_COL];
				return this.labelColList.slice(startCol, endCol + 1);
			},

			 // assume that timestep for all cells in same column are equal
			timeMin(): number {
				return Math.min(...this.labelColSelected);
			},

			// assume that timestep for all cells in same column are equal
			timeMax(): number {
				return Math.max(...this.labelColSelected);
			},

			parametersMinAll(): number {
				return Math.min(...Object.values(this.parametersMin) as number[]);
			},

			parametersMaxAll(): number {
				return Math.max(...Object.values(this.parametersMax) as number[]);
			},

			xScale(): any {
				return scaleLinear()
					.domain([this.timeMin, this.timeMax])
					.range([0, this.containerBoundingBox.width - this.leftMargin - this.rightMargin]);
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
			this.getContainerBoundingBox();
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
				this.getContainerBoundingBox();
				this.renderGraph();
			},
		},



		// ---------------------------------------------------------------------------- //
		// methods                                                                      //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		methods: {
			getContainerBoundingBox() {
				this.containerBoundingBox = this.$refs.container.getBoundingClientRect();
			},

			renderGraph() {
				const { height } = this.containerBoundingBox;
				const {leftMargin, bottomMargin, topMargin} = this;

				// remove old svg if present
				if(this.svg) {
					this.svg.remove();
				}

				this.svg = select(this.$refs.container)
					.append("svg")
					.attr('class', "cell-selected-line-graph")
					.attr("height", '100%')
					.attr("width", '100%')
					.style('background', 'white');

				const xAxis = axisBottom()
					.scale(this.xScale);

				this.svg.append("g")
					.attr('transform', `translate(${leftMargin},${height - bottomMargin})`)
					.call(xAxis);

				const yAxis = axisLeft()
					.scale(this.yScale);

				this.svg.append("g")
					.attr('transform', `translate(${leftMargin},${topMargin})`)
				.call(yAxis);

				Object.keys(this.selectedCells).forEach(parameter => {
					this.renderLine(this.svg, parameter, this.selectedCells[parameter], this.labelColSelected);
				});
			},

			renderLine(svg: any, parameter: string, yValueArray: any, xValueArray: any) {
				const dataLength = yValueArray.length;

				let path = 'M';
				for(let i = 0; i < dataLength; i++) {
					path += ` ${this.xScale(xValueArray[i])},`
						+ `${this.yScale(yValueArray[i])}`
						+ ` ${i !== dataLength - 1 ? ' L' : ''}`;
				}

				svg.append('g')
					.attr('transform', `translate(${this.leftMargin},${this.topMargin})`)
					.append('path')
					.attr('d', path)
					.style('fill', 'none')
					.style('stroke', this.colorFn(parameter))
					.style('stroke-width', 2);
			},
		},
	}
</script>