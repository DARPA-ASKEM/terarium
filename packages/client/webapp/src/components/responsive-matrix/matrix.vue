<template>
	<div class="matrix" ref="matrix">
		<!-- <component
			:is="'ResponsiveCellFill'"
			v-for="dataCell in dataCellList" :key="dataCell.__idx__"
			:style="getCellStyle(dataCell)"
			@mousedown="cellMouseDown(dataCell.__row__, dataCell.__col__)"
			@mouseup="cellMouseUp(dataCell.__row__, dataCell.__col__)"
			:cellData="dataCell"
			:parameters="dataParametersArray"
			:parametersMin="dataParametersMin"
			:parametersMax="dataParametersMax"
			:colorFn="fillColorFn"
		/>
		<component
			v-for="(selectedCell, idx) in selectedCellList" :key="idx"
			:is="getSelectGraphType(selectedCell)"
			:update="update"
			:selectedCell="selectedCell"
			:dataRowList="dataRowList"
			:dataColList="dataColList"
			:labelRowList="labelRowList"
			:labelColList="labelColList"
			:parameters="dataParametersArray"
			:parametersMin="dataParametersMin"
			:parametersMax="dataParametersMax"
			:colorFn="getSelectedGraphColorFn(selectedCell)"
			@click="selectedCellClick(idx)"
		/> -->
	</div>
</template>

<script lang="ts">
	import { nextTick } from 'vue';

	import ResponsiveCellFill from './cell-fill.vue';
	import ResponsiveCellBarContainer from './cell-bar-container.vue';
	import ResponsiveCellLineContainer from './cell-line-container.vue';

	import chroma from 'chroma-js';
	import * as PIXI from 'pixi.js';
	import { Viewport } from 'pixi-viewport';
	import { uint32ArrayToRedIntTex } from './pixi-utils';

	PIXI.settings.PREFER_ENV = PIXI.ENV.WEBGL2;

	export enum SelectedCellValue {
		START_ROW,
		START_COL,
		END_ROW,
		END_COL,
	}

	export enum CellType {
		FILL = 'ResponsiveCellFill',
		BAR = 'ResponsiveCellBarContainer',
		LINE = 'ResponsiveCellLineContainer',
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// exported style methods

	export function getSelectedCellStyle(selectedCell: any) {
		return {
			gridRow: `${selectedCell[SelectedCellValue.START_ROW] * 2 + 1}`
				+ ` / span ${(selectedCell[SelectedCellValue.END_ROW] - selectedCell[SelectedCellValue.START_ROW]) * 2 + 1}`,
			gridColumn: `${selectedCell[SelectedCellValue.START_COL] * 2 + 1}`
				+ ` / span ${(selectedCell[SelectedCellValue.END_COL] - selectedCell[SelectedCellValue.START_COL]) * 2 + 1}`,
		};
	}

	export default {

		// ---------------------------------------------------------------------------- //
		// components                                                                   //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		components: {
			ResponsiveCellFill,
			ResponsiveCellBarContainer,
			ResponsiveCellLineContainer,
		},



		// ---------------------------------------------------------------------------- //
		// props                                                                        //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		props: {
			data: {
				type: null,
				default: [[], []], // e.g. [[{}, {}, {}], [{}, {}, {}]]
			},
			// labels: {
			// 	type: null,
			// 	default: [[], []],
			// },
			cellLabelRow: {
				type: Array,
				default() {
					return [];
				},
			},
			cellLabelCol: {
				type: Array,
				default() {
					return [];
				},
			},
			// cellType: {
			// 	type: Number,
			// 	default: CellTypes.DISCRETE
			// },
			fillColorFn: {
				type: Function,
				default() {
					return '#000000';
				},
			},
			barColorFn: {
				type: Function,
				default() {
					return '#000000';
				},
			},
			lineColorFn: {
				type: Function,
				default() {
					return '#000000';
				},
			},
			backgroundColor: {
				type: String,
				default: '#FFFFFF',
			},
			cellMarginRow: {
				type: [String, Array],
				default: '2px',
			},
			cellMarginCol: {
				type: [String, Array],
				default: '2px',
			},
		},



		// ---------------------------------------------------------------------------- //
		// data                                                                         //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		data() {
			return {
				update: 0,
				dataCellList: [],
				dataRowList: [], // e.g. [[row1col1, row1col2]]
				dataColList: [], // e.g. [[row1col1, row2col1]]
				dataParameters: new Set(), // e.g. ["age", "height", "weight"]
				dataParametersMin: {}, // e.g. {param1: 0, param2: 3}
				dataParametersMax: {}, // e.g. {param1: 10, param2: 17}
				labelRowList: [],
				labelColList: [],
				numRows: 0,
				numCols: 0,
				selectedCellList: [], // e.g. [[startrow, startcol, endrow, endcol]]
				selectedRows: [], // e.g. [false, true, true, false]
				selectedCols: [], // e.g. [false, true, true, false]
				selectedCells: Array(4), // e.g. [startrow, startcol, endrow, endcol]
			};
		},



		// ---------------------------------------------------------------------------- //
		// computed                                                                     //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		computed: {
			// SelectedCellValue(): typeof SelectedCellValue {
			// 	return SelectedCellValue;
			// },

			dataParametersArray(): string[] {
				return [...this.dataParameters];
			},

			numSelectedRows(): number {
				return this.selectedRows.reduce((acc: number, val: boolean) => acc + Number(val), 0);
			},

			numSelectedCols(): number {
				return this.selectedCols.reduce((acc: number, val: boolean) => acc + Number(val), 0);
			},

			// refactor?
			responsiveGridStyle(): any {
				let gridTemplateRows = '';
				const selectedRowsSpace = this.numSelectedRows
					? (60 + this.numSelectedRows / this.numRows * 40) / this.numSelectedRows
					: 0;
				this.selectedRows.forEach((isActiveRow: boolean, rowIdx: number) => {
					const notLastRow = rowIdx !== this.numRows - 1;
					const rowMargin = this.getRowMargin(rowIdx);
					gridTemplateRows += `[row${rowIdx}] ${isActiveRow ? `calc(${selectedRowsSpace}% - ${notLastRow ? rowMargin : '0px'})` : 'auto'} `;
					if(notLastRow) {
						gridTemplateRows += `[rowspace${rowIdx}] ${rowMargin} `;
					}
				});
				gridTemplateRows += '[end]';

				let gridTemplateColumns = '';
				const selectedColsSpace = this.numSelectedCols
					? (60 + this.numSelectedCols / this.numCols * 40) / this.numSelectedCols
					: 0;
				this.selectedCols.forEach((isActiveCol: boolean, colIdx: number) => {
					const notLastCol = colIdx !== this.numCols - 1;
					const colMargin = this.getColMargin(colIdx);
					gridTemplateColumns += `[col${colIdx}] ${isActiveCol ? `calc(${selectedColsSpace}% - ${notLastCol ? colMargin : '0px'})` : 'auto'} `;
					if(notLastCol) {
						gridTemplateColumns += `[colspace${colIdx}] ${colMargin} `;
					}
				});
				gridTemplateColumns += '[end]';

				return {
					gridTemplateRows,
					gridTemplateColumns,
				}
			},
		},



		// ---------------------------------------------------------------------------- //
		// beforeMount                                                                  //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		async beforeMount() {
			this.processData(this.data);
			this.processLabels();
			this.processActiveCells();
		},



		// ---------------------------------------------------------------------------- //
		// mounted                                                                      //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		async mounted() {
			// create pixi inputs
			const numRow = this.numRows;
			const numCol = this.numCols;
			const numMicroRowPerFill = 10;
			const numMicroColPerFill = 10;
			const createMicroArray = (numEl: number, microPerEl: number): Uint32Array => {
				const microArrayLen = numEl * microPerEl;
				const microArray = new Uint32Array(microArrayLen);
				let i = 0;

				for(let elIdx = 0; elIdx < numEl; elIdx++) {
					for(let fillIdx = 0; fillIdx < microPerEl; fillIdx++) {
						microArray[i++] = elIdx;
					}
				}

				return microArray;
			}
			const microRowArray = createMicroArray(numRow, numMicroRowPerFill);
			const microColArray = createMicroArray(numCol, numMicroColPerFill);
			const cellAspectRatio = 25;
			const quadAspectRatio = microRowArray.length / microColArray.length * cellAspectRatio; // height / width
			const createColorArray = (data: any): Uint8Array => { // assume datacelllist is an array with cells left-to-right top-to-bottom
				const colorArray = new Uint8Array(data.length * 4);
				let i = 0; // colorArrayIdx
				
				for(let dataIdx = 0; dataIdx < data.length; dataIdx++) {
					const color = this.fillColorFn(
						data[dataIdx],
						this.dataParametersMin,
						this.dataParametersMax,
						this.dataParametersArray,
					);

					// normalise color
					const rgba = chroma(color).rgba(); // dumb chroma typings
					colorArray[i++] = rgba[0];
					colorArray[i++] = rgba[1];
					colorArray[i++] = rgba[2];
					colorArray[i++] = Math.round(rgba[3] * 255);
				}

				return colorArray;
			}

			const color = createColorArray(this.dataCellList);
			// const color = new Uint8Array(Array(numRow * numCol * 4).fill(0).map((v, i) => i % 4 === 3 ? 255 : 255 * Math.floor(i / 4) / (numRow * numCol)));

			// ///////////////////////////////////////////////////////////////////////////////

			const { matrix } = this.$refs;

			// initialize pixi.js
			const app = new PIXI.Application({
				resizeTo: matrix,
				// width: 900,
				// height: 1200,
				backgroundColor: 0x2c3e50
			});
			matrix.appendChild(app.view);

			// create viewport
			const worldWidth = 2;
			const worldHeight = 2;
			const viewport = new Viewport({
				screenWidth: 1736,
				screenHeight: 882,
				worldWidth,
				worldHeight,
				passiveWheel: false,
				interaction: app.renderer.plugins.interaction // the interaction module is important for wheel to work properly when renderer.view is placed or scaled
			});

			// add the viewport to the stage
			app.stage.addChild(viewport);

			// activate plugins
			viewport
				.drag()
				.pinch()
				.wheel();

			// build quad
			const vertexPosition = quadAspectRatio > 1 // is height long?
				? [-1 / quadAspectRatio, -1, // height long
					1 / quadAspectRatio, -1, // x, y
					1 / quadAspectRatio, 1,
					-1 / quadAspectRatio, 1]
				: [-1, -1 / quadAspectRatio, // width long
					1, -1 / quadAspectRatio, // x, y
					1, 1 / quadAspectRatio,
					-1, 1 / quadAspectRatio];
			const geometry = new PIXI.Geometry()
				.addAttribute('aVertexPosition',
					vertexPosition,
					2) // the size of the attribute
				.addAttribute('aUvs',
					[0, 0, // u, v
						1, 0, // u, v
						1, 1,
						0, 1], // u, v
					2) // the size of the attribute
				.addIndex([0, 1, 2, 0, 2, 3]);

			const vertexSrc = `
				#version 300 es

				precision lowp sampler2D;
				precision highp float;

				in vec2 aVertexPosition;
				in vec2 aUvs;

				uniform mat3 translationMatrix;
				uniform mat3 projectionMatrix;

				out vec2 vUvs;

				void main() {

					vUvs = aUvs;
					gl_Position = vec4((projectionMatrix * translationMatrix * vec3(aVertexPosition, 1.0)).xy, 0.0, 1.0);

				}`;

			const fragmentSrc = `
				#version 300 es

				precision highp usampler2D;
				precision mediump sampler2D;
				precision highp float;

				in vec2 vUvs;

				uniform sampler2D uColor;
				uniform usampler2D uMicroRow;
				uniform usampler2D uMicroCol;

				out vec4 fragColor;

				vec4 getColorFromRowCol(sampler2D tex, int index) {
					int texWidth = textureSize(tex, 0).x;
					int col = index % texWidth;
					int row = index / texWidth;
					return texelFetch(tex, ivec2(col, row), 0);
				}

				void main() {
					float microColLen = float(textureSize(uMicroCol, 0).x);
					float microRowLen = float(textureSize(uMicroRow, 0).x);
					uint colIndex = texelFetch(uMicroCol, ivec2(int(vUvs.x * microColLen), 0), 0).r;
					uint rowIndex = texelFetch(uMicroRow, ivec2(int(vUvs.y * microRowLen), 0), 0).r;

					fragColor = texelFetch(uColor, ivec2(colIndex, rowIndex), 0);
					// fragColor = vec4(1., 1., 1., 1.);
				}`;

			// build uniforms
			const uniforms = new PIXI.UniformGroup({
				uMicroRow: uint32ArrayToRedIntTex(microRowArray, microRowArray.length, 1),
				uMicroCol: uint32ArrayToRedIntTex(microColArray, microColArray.length, 1),
				uColor: PIXI.Texture.fromBuffer(color, numCol, numRow),
			});

			// run shader on quad
			const shader = PIXI.Shader.from(vertexSrc, fragmentSrc, uniforms);
			const quad = new PIXI.Mesh(geometry, shader);

			// center quad in world
			quad.position.set(1, 1);

			// add quad to viewport
			viewport.addChild(quad);

			// draw border around world
			// const line = viewport.addChild(new PIXI.Graphics())
			// line.lineStyle(0.01, 0xff0000).drawRect(0, 0, viewport.worldWidth, viewport.worldHeight)

			// center and zoom camera to world
			viewport.fit();
			viewport.moveCenter(worldWidth / 2, worldHeight / 2);
		},



		// ---------------------------------------------------------------------------- //
		// watch                                                                        //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		watch: {
			data(newData: any) {
				this.processData(newData);
				this.processLabels();
				this.processActiveCells();
			},
			cellLabelRow() {
				this.processLabels();
			},
			cellLabelCol() {
				this.processLabels();
			},
		},



		// ---------------------------------------------------------------------------- //
		// methods                                                                      //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		methods: {
			
			// ///////////////////////////////////////////////////////////////////////////////
			// style methods

			getCellStyle(dataCell: any) {
				return {
					gridRow: dataCell.__row__ * 2 + 1,
					gridColumn: dataCell.__col__ * 2 + 1,
				};
			},

			getSelectedCellStyle(selectedCell: any) {
				return getSelectedCellStyle(selectedCell);
			},

			// ///////////////////////////////////////////////////////////////////////////////
			// cell margin methods

			getRowMargin(row: number): String {
				if(this.cellMarginRow.constructor === Array) {
					return this.cellMarginRow[row % this.cellMarginRow.length] || '0px';
				}

				return this.cellMarginRow;
			},

			getColMargin(col: number): String {
				if(this.cellMarginCol.constructor === Array) {
					return this.cellMarginCol[col % this.cellMarginCol.length] || '0px';
				}
				
				return this.cellMarginCol;
			},

			// ///////////////////////////////////////////////////////////////////////////////
			// data methods

			isDataValid(data: any) {
				if (data?.constructor !== Array || data[0]?.constructor !== Array) {
					return false;
				}

				return !data.some((row: any) => row.constructor !== Array || row.length !== data[0].length);
			},

			processData(newData: any) {
				if(this.isDataValid(newData)) {
					this.numRows = newData.length;
					this.numCols = newData[0]?.length;

					// find more efficient way to initialize 2D array?
					this.dataRowList = Array(this.numRows).fill().map(() => []);
					this.dataColList = Array(this.numCols).fill().map(() => []);

					newData.forEach((row: any, indexRow: number) => row.forEach((cell: any, indexCol: number) => {
						if(cell) {
							this.extractParams(cell);
							const cellData = {
								...cell,
								__row__: indexRow,
								__col__: indexCol,
								__idx__: this.dataCellList.length,
							};

							this.dataCellList.push(cellData);
							this.dataRowList[indexRow].push(cellData);
							this.dataColList[indexCol].push(cellData);
						}
					}));
				} else {
					console.error('data invalid');
				}
			},

			// assume processData called previously
			processLabels() {
				if(this.cellLabelRow.length) {
					this.labelRowList = this.cellLabelRow;
				} else { 
					this.labelRowList = Array(this.numRows).fill().map((v, i) => i);
				}

				if(this.cellLabelCol.length) {
					this.labelColList = this.cellLabelCol;
				} else { 
					this.labelColList = Array(this.numCols).fill().map((v, i) => i);
				}
			},

			extractParams(cellObject: any) {
				Object.keys(cellObject).forEach(param => {
					if(!this.dataParameters.has(param)) {
						this.dataParametersMin[param] = cellObject[param];
						this.dataParametersMax[param] = cellObject[param];
					} else {
						this.dataParametersMin[param] = Math.min(this.dataParametersMin[param], cellObject[param]);
						this.dataParametersMax[param] = Math.max(this.dataParametersMax[param], cellObject[param]);
					}
					this.dataParameters.add(param);
				});
			},

			// ///////////////////////////////////////////////////////////////////////////////
			// selected data methods

			// for loop instead of while?
			async processActiveCells() {
				const {
					START_ROW,
					END_ROW,
					START_COL,
					END_COL
				} = SelectedCellValue;

				this.selectedRows = Array(this.numRows).fill(false);
				this.selectedCols = Array(this.numCols).fill(false);

				this.selectedCellList.forEach(selectedCell => {
					let row = selectedCell[START_ROW];
					do {
						this.selectedRows[row++] = true;
					} while (row <= selectedCell[END_ROW]);

					let col = selectedCell[START_COL];
					do {
						this.selectedCols[col++] = true;
					} while (col <= selectedCell[END_COL]);
				});

				// wait for the grid recalculation to complete before triggering children update
				await nextTick();
				this.incrementUpdate();
			},

			// function can be cleaned up if the enum is abandoned...
			// getCellsForSelected(selectedCellsArray: any[]): any {
			// 	const startRow = selectedCellsArray[SelectedCellValue.START_ROW];
			// 	const endRow = selectedCellsArray[SelectedCellValue.END_ROW];
			// 	const startCol = selectedCellsArray[SelectedCellValue.START_COL];
			// 	const endCol = selectedCellsArray[SelectedCellValue.END_COL];

			// 	const selectedArr: any[] = [];
			// 	for(let row = startRow; row <= endRow; row++) {
			// 		this.dataRowList[row].forEach((cell: any) => {
			// 			if(cell.__col__ <= endCol && cell.__col__ >= startCol) {
			// 				selectedArr.push(cell);
			// 			}
			// 		});
			// 	}

			// 	return selectedArr;
			// },

			getSelectGraphType(selectedCell: any): string {
				const {
					START_COL,
					END_COL
				} = SelectedCellValue;

				const startCol = selectedCell[START_COL];
				const endCol = selectedCell[END_COL];
				return startCol === endCol ? CellType.BAR : CellType.LINE;
			},

			getSelectedGraphColorFn(selectedCell: any): any {
				switch(this.getSelectGraphType(selectedCell)) {
					case CellType.BAR:
						return this.barColorFn;
					case CellType.LINE:
						return this.lineColorFn;
					default:
						return () => '#FF00FF';
				}
			},

			// ///////////////////////////////////////////////////////////////////////////////
			// interaction event handlers

			cellMouseDown(selectedRow, selectedCol) {
				const {
					START_ROW,
					START_COL,
				} = SelectedCellValue;

				this.selectedCells = Array(4);
				this.selectedCells[START_ROW] = selectedRow;
				this.selectedCells[START_COL] = selectedCol;
			},

			// function can be cleaned up if the enum is abandoned...
			cellMouseUp(selectedRow, selectedCol) {
				const {
					START_ROW,
					END_ROW,
					START_COL,
					END_COL
				} = SelectedCellValue;

				const startRow = Math.min(
					this.selectedCells[START_ROW],
					selectedRow
				);
				const endRow = Math.max(
					this.selectedCells[START_ROW],
					selectedRow
				);
				const startCol = Math.min(
					this.selectedCells[START_COL],
					selectedCol,
				);
				const endCol = Math.max(
					this.selectedCells[START_COL],
					selectedCol,
				);

				this.selectedCells[START_ROW] = startRow;
				this.selectedCells[START_COL] = startCol;
				this.selectedCells[END_ROW] = endRow;
				this.selectedCells[END_COL] = endCol;

				this.selectedCellList.push(this.selectedCells);
				this.processActiveCells();
			},

			selectedCellClick(selectedCellIndex) {
				this.selectedCellList.splice(selectedCellIndex, 1);
				this.processActiveCells();
			},

			// ///////////////////////////////////////////////////////////////////////////////
			// update handlers

			incrementUpdate() {
				this.update++;
			}
		},
	};
</script>

<style scoped>
	.matrix {
		display: grid;
		/* height: 900px;
		width: 900px; */
		/* transition: all 1s ease; */
	}
</style>