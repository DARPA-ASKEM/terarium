<template>
	<main class="matrix-container" ref="matrixContainer">
		<div class="matrix" ref="matrix">
			<component
				v-for="(selectedCell, idx) in selectedCellList"
				:key="idx"
				:is="getSelectedDrilldownType(selectedCell)"
				:getSelectedCellStyle="getSelectedCellStyle"
				:iterateSelectionIndexes="iterateCellIndexes"
				:update="update"
				:move="move"
				:selectedCell="selectedCell"
				:dataCellList="dataCellList"
				:labelRowList="labelRowList"
				:labelColList="labelColList"
				:parameters="dataParametersArray"
				:parametersMin="dataParametersMin"
				:parametersMax="dataParametersMax"
				:colorFn="getSelectedGraphColorFn(selectedCell)"
				@click="selectedCellClick(idx)"
			/>
		</div>
	</main>
</template>

<script lang="ts">
/*
 * Matrix
 *
 * The main entry point for the responsive matrix component. Creates a heatmap
 * using WebGL in order to visualize a 2D data matrix provided. Also serves as the
 * controller for other components associated with the responsive matrix.
 * Uses:
 * - chroma-js: https://gka.github.io/chroma.js/
 * - pixi.js: https://pixijs.com/
 * - pixi-viewport: https://davidfig.github.io/pixi-viewport/jsdoc/index.html
 */

import { nextTick, PropType } from 'vue';

import chroma from 'chroma-js';
import { Viewport } from 'pixi-viewport';
import {
	Application,
	DisplayObject,
	Geometry,
	Texture,
	Shader,
	Mesh,
	FederatedPointerEvent,
	Point
} from 'pixi.js';

import {
	SelectedCell,
	SelectedCellValue,
	CellData,
	CellStatus,
	CellType,
	Uniforms
} from '@/types/ResponsiveMatrix';
import { uint32ArrayToRedIntTex } from './pixi-utils';

import ResponsiveCellBarContainer from './cell-bar-container.vue';
import ResponsiveCellLineContainer from './cell-line-container.vue';

import matrixVS from './matrix.vs.glsl';
import matrixFS from './matrix.fs.glsl';
import matrixGridFS from './matrix-grid.fs.glsl';

// EMPTY_POINT used as a fallback value
const EMPTY_POINT = new Point();

export default {
	// ---------------------------------------------------------------------------- //
	// components                                                                   //
	// ---------------------------------------------------------------------------- //

	components: {
		ResponsiveCellBarContainer,
		ResponsiveCellLineContainer
	},

	// ---------------------------------------------------------------------------- //
	// props                                                                        //
	// ---------------------------------------------------------------------------- //

	props: {
		data: {
			type: Array as PropType<object[][]>,
			default() {
				return [[], []]; // e.g. [[{}, {}, {}], [{}, {}, {}]]
			}
		},
		// labels: {
		// 	type: null,
		// 	default: [[], []],
		// },
		cellLabelRow: {
			type: Array as PropType<number[] | string[]>,
			default() {
				return [];
			}
		},
		cellLabelCol: {
			type: Array as PropType<number[] | string[]>,
			default() {
				return [];
			}
		},
		fillColorFn: {
			type: Function,
			default() {
				return '#000000';
			}
		},
		barColorFn: {
			type: Function,
			default() {
				return '#000000';
			}
		},
		lineColorFn: {
			type: Function,
			default() {
				return '#000000';
			}
		},
		backgroundColor: {
			type: String,
			default: '#FFFFFF'
		},
		cellMarginRow: {
			type: [String, Array],
			default: '2px'
		},
		cellMarginCol: {
			type: [String, Array],
			default: '2px'
		}
	},

	// ---------------------------------------------------------------------------- //
	// data                                                                         //
	// ---------------------------------------------------------------------------- //

	data() {
		return {
			dataCellList: [] as CellData[],
			dataParameters: new Set() as Set<string>, // e.g. ["age", "height", "weight"]
			dataParametersMin: {}, // e.g. {param1: 0, param2: 3}
			dataParametersMax: {}, // e.g. {param1: 10, param2: 17}
			labelRowList: [] as number[] | string[],
			labelColList: [] as number[] | string[],
			numRows: 0,
			numCols: 0,

			// break down real rows and columns (with arbitrarily different dimensions)
			// into uniformly sized "micro" rows and columns using a process analogous
			// to grid supersampling
			microRowArray: new Uint32Array(0) as Uint32Array,
			microColArray: new Uint32Array(0) as Uint32Array,

			uniforms: {} as Uniforms,
			worldWidth: 0,
			worldHeight: 0,

			screenHeight: 0,
			screenWidth: 0,

			selectedCellList: [] as SelectedCell[], // e.g. [[startrow, startcol, endrow, endcol]]
			selectedRows: [] as CellStatus[], // e.g. [0, 0, 1, 0]
			selectedCols: [] as CellStatus[], // e.g. [0, 0, 1, 0]
			selectedElements: [] as CellStatus[], // e.g. [0, 0, 1, 0]
			selectedCell: Array(4) as SelectedCell, // e.g. [startrow, startcol, endrow, endcol]

			// properties which are updated to trigger functions in children
			update: 0,
			move: 0,

			enableDrag: false,
			resizeObserver: null as unknown as ResizeObserver
		};
	},

	// ---------------------------------------------------------------------------- //
	// computed                                                                     //
	// ---------------------------------------------------------------------------- //

	computed: {
		screenAspectRatio(): number {
			return this.screenHeight / this.screenWidth;
		},

		dataParametersArray(): string[] {
			return [...this.dataParameters];
		},

		numSelectedRows(): number {
			return this.selectedRows.filter((d) => d !== CellStatus.NONE).length;
		},

		numSelectedCols(): number {
			return this.selectedCols.filter((d) => d !== CellStatus.NONE).length;
		},

		microRowSettings(): number[] {
			// work out the percentage of the total row length taken by all selected rows
			// as a value between 0 and 1
			const percentageSelectedRows = this.numSelectedRows
				? 0.5 + (this.numSelectedRows / this.numRows) * 0.5
				: 0;

			// find the number of micro rows assigned to rows of every status using the equation:
			// micro_rows_per_row_of_status = percentage_row_len_of_status * total_num_rows / total_num_rows_with_status
			const microRowSettings = Array(2).fill(0);
			microRowSettings[CellStatus.NONE] = percentageSelectedRows
				? ((1 - percentageSelectedRows) * this.numRows) / (this.numRows - this.numSelectedRows)
				: 1;
			microRowSettings[CellStatus.SELECTED] = percentageSelectedRows
				? (percentageSelectedRows * this.numRows) / this.numSelectedRows
				: 1;

			// normalise all micro row settings to have a minimum value of 1
			const microRowSettingsMin = Math.min(...microRowSettings);
			if (microRowSettingsMin < 1) {
				for (let i = 0; i < microRowSettings.length; i++) {
					microRowSettings[i] /= microRowSettingsMin;
				}
			}

			return microRowSettings;
		},
		microColSettings(): number[] {
			// work out the percentage of the total col length taken by all selected cols
			// as a value between 0 and 1
			const percentageSelectedCols = this.numSelectedCols
				? 0.5 + (this.numSelectedCols / this.numCols) * 0.5
				: 0;

			// find the number of micro cols assigned to cols of every status using the equation:
			// micro_cols_per_col_of_status = percentage_col_len_of_status * total_num_cols / total_num_cols_with_status
			const microColSettings = Array(2).fill(0);
			microColSettings[CellStatus.NONE] = percentageSelectedCols
				? ((1 - percentageSelectedCols) * this.numCols) / (this.numCols - this.numSelectedCols)
				: 1;
			microColSettings[CellStatus.SELECTED] = percentageSelectedCols
				? (percentageSelectedCols * this.numCols) / this.numSelectedCols
				: 1;

			// normalise all micro col settings to have a minimum value of 1
			const microColSettingsMin = Math.min(...microColSettings);
			if (microColSettingsMin < 1) {
				for (let i = 0; i < microColSettings.length; i++) {
					microColSettings[i] /= microColSettingsMin;
				}
			}

			return microColSettings;
		}
	},

	// ---------------------------------------------------------------------------- //
	// setup                                                                        //
	// ---------------------------------------------------------------------------- //

	setup() {
		return {
			app: undefined as undefined | Application,
			viewport: undefined as undefined | Viewport
		};
	},

	// ---------------------------------------------------------------------------- //
	// beforeMount                                                                  //
	// ---------------------------------------------------------------------------- //

	async beforeMount() {
		this.processData();
		this.processLabels();
		this.processActiveCells();
	},

	// ---------------------------------------------------------------------------- //
	// mounted                                                                      //
	// ---------------------------------------------------------------------------- //

	async mounted() {
		const matrix = this.$refs.matrix as HTMLElement;
		const matrixContainer = this.$refs.matrixContainer as HTMLElement;

		// ///////////////////////////////////////////////////////////////////////////////

		// initialize screen height/width
		this.screenHeight = matrix.offsetHeight;
		this.screenWidth = matrix.offsetWidth;

		// start the resize observer
		this.resizeObserver = new ResizeObserver((entries) => {
			this.screenHeight = entries[0].contentRect.height;
			this.screenWidth = entries[0].contentRect.width;
		});
		this.resizeObserver.observe(matrixContainer);

		// start event listeners
		window.addEventListener('keydown', this.handleKey);
		window.addEventListener('keyup', this.handleKey);
		matrixContainer.addEventListener('wheel', this.handleScroll);

		// ///////////////////////////////////////////////////////////////////////////////

		// initialize pixi.js
		this.app = new Application({
			resizeTo: matrixContainer,
			backgroundColor: this.backgroundColor
		});
		matrix.appendChild(this.app.view as unknown as Node);

		// create viewport
		const screenHeight = this.screenHeight;
		const screenWidth = this.screenWidth;
		const cellAspectRatio = 1 || this.microRowArray.length / this.microColArray.length;
		const quadAspectRatio = this.screenAspectRatio * cellAspectRatio; // height / width
		const vertexPosition =
			quadAspectRatio > 1 // is height long?
				? [
						-1 / quadAspectRatio, // x, y
						-1,
						1 / quadAspectRatio,
						-1,
						1 / quadAspectRatio,
						1,
						-1 / quadAspectRatio,
						1
				  ]
				: [
						-1, // x, y
						-1 * quadAspectRatio,
						1,
						-1 * quadAspectRatio,
						1,
						1 * quadAspectRatio,
						-1,
						1 * quadAspectRatio
				  ];
		this.worldHeight = vertexPosition[5] - vertexPosition[3];
		this.worldWidth = vertexPosition[2] - vertexPosition[0];
		this.viewport = new Viewport({
			screenWidth: this.screenWidth,
			screenHeight: this.screenHeight,
			worldWidth: this.worldWidth,
			worldHeight: this.worldHeight,
			divWheel: matrix,
			interaction: this.app.renderer.plugins.interaction // the interaction module is important for wheel to work properly when renderer.view is placed or scaled
		});

		// add the viewport to the stage
		this.app.stage.addChild(this.viewport as unknown as DisplayObject);

		// activate camera interaction plugins
		this.viewport.drag().pinch().wheel();
		this.viewport.pause = true;

		// clamp camera
		this.viewport.clampZoom({
			maxWidth: this.worldWidth * 2,
			maxHeight: this.worldHeight * 2
		});

		// build quad
		const geometry = new Geometry()
			.addAttribute('aVertexPosition', vertexPosition, 2)
			.addAttribute(
				'aUvs',
				[
					0, // u, v
					0,
					1,
					0,
					1,
					1,
					0,
					1
				],
				2
			)
			.addIndex([0, 1, 2, 0, 2, 3]);

		// build color array
		const color = this.createColorArray();

		// build uniforms
		this.uniforms = {
			// screen data
			uScreenWidth: screenWidth,
			uScreenHeight: screenHeight,

			// geometry/viewport data
			uWorldWidth: this.viewport.worldWidth,
			uWorldHeight: this.viewport.worldHeight,
			uViewportWorldWidth: this.viewport.worldWidth,
			uViewportWorldHeight: this.viewport.worldHeight,

			// grid settings
			uGridDisplayBorder: false,
			uGridRowDisplayLim: 60,
			uGridRowDisplayTransition: 12,
			uGridColDisplayLim: 60,
			uGridColDisplayTransition: 12,

			// row/col data
			uNumRow: this.numRows,
			uNumCol: this.numCols,
			uMicroRow: this.uniforms.uMicroRow,
			uMicroCol: this.uniforms.uMicroCol,

			// cell element color data
			uColor: Texture.fromBuffer(color, this.numCols, this.numRows)
		};

		// set callback to update uniforms every render call
		this.app.ticker.add(this.handleTick);

		// run shader on quad
		const quadShader = Shader.from(matrixVS, matrixFS, this.uniforms);
		const quad = new Mesh(geometry, quadShader);

		// center quad in world
		quad.position.set(this.viewport.worldWidth / 2, this.viewport.worldHeight / 2);

		// add quad to viewport
		this.viewport.addChild(quad);

		// run shader on grid
		const gridShader = Shader.from(matrixVS, matrixGridFS, this.uniforms);
		const grid = new Mesh(geometry, gridShader);

		// center grid in world
		grid.position.set(this.viewport.worldWidth / 2, this.viewport.worldHeight / 2);

		// add grid to viewport
		this.viewport.addChild(grid as any);

		// center and zoom camera to world
		this.centerGraph();

		// add pixi interaction handlers
		this.viewport.on('pointerdown', this.handleMouseDown);
		this.viewport.on('pointerup', this.handleMouseUp);
		this.viewport.on('moved' as any, this.incrementMove);
		this.viewport.on('zoomed-end' as any, this.incrementUpdate);
	},

	// ---------------------------------------------------------------------------- //
	// watch                                                                        //
	// ---------------------------------------------------------------------------- //

	watch: {
		data() {
			this.processData();
			this.processLabels();
			this.processActiveCells();
		},
		cellLabelRow() {
			this.processLabels();
		},
		cellLabelCol() {
			this.processLabels();
		}
	},

	// ---------------------------------------------------------------------------- //
	// unmounted                                                                    //
	// ---------------------------------------------------------------------------- //

	unmounted() {
		this.resizeObserver.disconnect();
		this.app?.destroy(false, true);
		window.removeEventListener('keydown', this.handleKey);
		window.removeEventListener('keyup', this.handleKey);
	},

	// ---------------------------------------------------------------------------- //
	// methods                                                                      //
	// ---------------------------------------------------------------------------- //

	methods: {
		// ///////////////////////////////////////////////////////////////////////////////
		// data methods

		/**
		 * Determines if data object array provided is structured correctly (as a 2D matrix).
		 * @param {CellData[][]} data
		 */
		isDataValid(data: object[][]) {
			if (data?.constructor !== Array || data[0]?.constructor !== Array) {
				return false;
			}

			return !data.some((row: any) => row.constructor !== Array || row.length !== data[0].length);
		},

		/**
		 * Ingests data, process and stores it in internal component state stores.
		 */
		processData() {
			if (this.isDataValid(this.data)) {
				this.numRows = this.data.length;
				this.numCols = this.data[0]?.length;

				this.selectedElements = Array(this.numRows * this.numCols).fill(CellStatus.NONE);

				// iterate through all the data and push the cell objects into data lists
				this.data.forEach((row, indexRow) =>
					row.forEach((cell, indexCol) => {
						if (cell) {
							this.extractParams(cell);
							// find better solution than using reserved properties
							// for cell identification
							const cellData = {
								...cell,
								row: indexRow,
								col: indexCol,
								_idx: this.dataCellList.length
							};

							this.dataCellList.push(cellData);
						}
					})
				);
			} else {
				console.error('Data Invalid');
			}
		},

		/**
		 * Ingests labels data, process and stores it in internal component state stores.
		 * Assumes that processData has already been called.
		 */
		processLabels() {
			if (this.cellLabelRow.length) {
				this.labelRowList = this.cellLabelRow;
			} else {
				this.labelRowList = Array(this.numRows)
					.fill(0)
					.map((_, i) => i);
			}

			if (this.cellLabelCol.length) {
				this.labelColList = this.cellLabelCol;
			} else {
				this.labelColList = Array(this.numCols)
					.fill(0)
					.map((_, i) => i);
			}
		},

		/**
		 * Processes a single cell object and extract the parameters from it.
		 * As well update the parameters min and max state objects.
		 * @param {object} cellObject
		 */
		extractParams(cellObject: object) {
			Object.keys(cellObject).forEach((param) => {
				if (!this.dataParameters.has(param)) {
					this.dataParametersMin[param] = cellObject[param];
					this.dataParametersMax[param] = cellObject[param];
				} else {
					this.dataParametersMin[param] = Math.min(
						this.dataParametersMin[param],
						cellObject[param]
					);
					this.dataParametersMax[param] = Math.max(
						this.dataParametersMax[param],
						cellObject[param]
					);
				}
				this.dataParameters.add(param);
			});
		},

		/**
		 * Processes the dataCellList to generate the color array.
		 */
		createColorArray(): Uint8Array {
			const data = this.dataCellList;
			const colorArray = new Uint8Array(data.length * 4);
			let i = 0; // colorArrayIdx

			for (let dataIdx = 0; dataIdx < data.length; dataIdx++) {
				const color = this.fillColorFn(
					data[dataIdx],
					this.dataParametersMin,
					this.dataParametersMax,
					this.dataParametersArray
				);

				// normalise color
				const rgba = chroma(color).rgba(); // dumb chroma typings
				colorArray[i++] = rgba[0];
				colorArray[i++] = rgba[1];
				colorArray[i++] = rgba[2];
				colorArray[i++] = Math.round(rgba[3] * 255);
			}

			return colorArray;
		},

		/**
		 * Creates a Uint32Array representing the row or column asignments for each micro row or column,
		 * using the status of each row or column and the number of micro rows or columns associated with
		 * each status.
		 * @param {CellStatus[]} elStatusArray
		 * @param {number[]} statusSettingsArray
		 */
		createMicroArray(elStatusArray: CellStatus[], statusSettingsArray: number[]): Uint32Array {
			const microArray: number[] = [];
			let i = 0;

			for (let elIdx = 0; elIdx < elStatusArray.length; elIdx++) {
				for (let fillIdx = 0; fillIdx < statusSettingsArray[elStatusArray[elIdx]]; fillIdx++) {
					microArray[i++] = elIdx;
				}
			}

			return new Uint32Array(microArray);
		},

		// ///////////////////////////////////////////////////////////////////////////////
		// selected data methods

		/**
		 * Process selectedCellList to generate selected rows and column status arrays as well as
		 * row and column micro arrays for the renderer.
		 */
		async processActiveCells() {
			const { START_ROW, END_ROW, START_COL, END_COL } = SelectedCellValue;

			this.selectedRows = Array(this.numRows).fill(CellStatus.NONE);
			this.selectedCols = Array(this.numCols).fill(CellStatus.NONE);

			this.selectedCellList.forEach((selectedCell) => {
				let row = selectedCell[START_ROW];
				do {
					this.selectedRows[row++] = CellStatus.SELECTED;
				} while (row <= selectedCell[END_ROW]);

				let col = selectedCell[START_COL];
				do {
					this.selectedCols[col++] = CellStatus.SELECTED;
				} while (col <= selectedCell[END_COL]);
			});

			this.microRowArray = this.createMicroArray(this.selectedRows, this.microRowSettings);
			// TODO: switch microRows to use 2D textures to avoid running into texture size limits
			this.uniforms.uMicroRow = uint32ArrayToRedIntTex(
				this.microRowArray,
				this.microRowArray.length,
				1
			);

			this.microColArray = this.createMicroArray(this.selectedCols, this.microColSettings);
			// TODO: switch microRows to use 2D textures to avoid running into texture size limits
			this.uniforms.uMicroCol = uint32ArrayToRedIntTex(
				this.microColArray,
				this.microColArray.length,
				1
			);

			// wait for the grid recalculation to complete before triggering children update
			await nextTick();
			this.incrementUpdate();
		},

		/**
		 * Given a selected cell array, determines if selection has a possibility of
		 * containing cells which have already been selected.
		 * @param {SelectedCell} selectedCell
		 */
		isSelectionPossiblySelected(selectedCell: SelectedCell) {
			const { START_COL, END_COL, START_ROW, END_ROW } = SelectedCellValue;
			const startCol = selectedCell[START_COL];
			const endCol = selectedCell[END_COL];
			const startRow = selectedCell[START_ROW];
			const endRow = selectedCell[END_ROW];

			for (let i = startRow; i <= endRow; i++) {
				if (this.selectedRows[i] === CellStatus.NONE) {
					return false;
				}
			}
			for (let i = startCol; i <= endCol; i++) {
				if (this.selectedCols[i] === CellStatus.NONE) {
					return false;
				}
			}
			return true;
		},

		/**
		 * Given a 1D cell array with cells pushed in from left-to-right and top-to-bottom, iterate through
		 * the indexes of all the cells in the selection. A negative value in the selection instructs the
		 * function to use the topmost/leftmost row/col in the case of a start value or the bottommost/rightmost
		 * row/col in the case of end value. A callback is executed with the index as a parameter for each
		 * index. If the callback returns true then stop iterating (as a mechanism to support early
		 * termination). The function returns a boolean indicating whether early termination was triggered.
		 * @param {SelectedCell} selectedCell
		 * @param {(idx: number) => boolean | void} cb
		 */
		iterateCellIndexes(selectedCell: SelectedCell, cb: (idx: number) => boolean | void) {
			const { START_ROW, END_ROW, START_COL, END_COL } = SelectedCellValue;

			// process selectedCell to replace invalid values with row/col min/max values
			const startCol = selectedCell[START_COL] > -1 ? selectedCell[START_COL] : 0;
			const endCol = selectedCell[END_COL] > -1 ? selectedCell[END_COL] : this.numCols - 1;
			const startRow = selectedCell[START_ROW] > -1 ? selectedCell[START_ROW] : 0;
			const endRow = selectedCell[END_ROW] > -1 ? selectedCell[END_ROW] : this.numRows - 1;

			// calculate the number of rows, cols, and cells contained in the selection
			const numSelectedRows = endRow - startRow + 1;
			const numSelectedCols = endCol - startCol + 1;
			const numSelectedCells = numSelectedRows * numSelectedCols;

			// iterate through every selected cell from left-to-right and top-to-bottom
			for (let i = 0; i < numSelectedCells; i++) {
				// calculate the row and col each cell belongs to
				const col = startCol + (i % numSelectedCols);
				const row = startRow + Math.floor(i / numSelectedCols);

				// use col and row to find the index of the cell in a 1-dim array
				const idx = row * this.numCols + col;

				// call callback with the index as a parameter
				// if cb returns true, then exit early
				if (cb(idx)) return true;
			}

			// since exit early not done, return false
			return false;
		},

		/**
		 * Set all values in a selected area to the given value in the selected elements array.
		 * @param {SelectedCell} selectedCell
		 * @param {CellStatus} newVal
		 */
		setSelectedElementArea(selectedCell: SelectedCell, newVal: CellStatus) {
			this.iterateCellIndexes(selectedCell, (idx) => {
				this.selectedElements[idx] = newVal;
			});
		},

		/**
		 * Given a selected cell array, determines if selection only consists of cells which
		 * have already been selected.
		 * @param {SelectedCell} selectedCell
		 */
		isSelectionSelected(selectedCell: SelectedCell): boolean {
			return !this.iterateCellIndexes(selectedCell, (idx) => {
				if (this.selectedElements[idx] === CellStatus.NONE) {
					return true;
				}
				return false;
			});
		},

		/**
		 * Remove all selected cells in the selected cell list where the selection
		 * is completely enclosed in the selected cell.
		 */
		removeSelected(): void {
			const { START_COL, END_COL, START_ROW, END_ROW } = SelectedCellValue;

			// set status of selected element area to none for all cells that fail the filter
			this.selectedCellList = this.selectedCellList.filter(
				(selectedCell) =>
					!(
						this.selectedCell[START_ROW] >= selectedCell[START_ROW] &&
						this.selectedCell[END_ROW] <= selectedCell[END_ROW] &&
						this.selectedCell[START_COL] >= selectedCell[START_COL] &&
						this.selectedCell[END_COL] <= selectedCell[END_COL]
					) || this.setSelectedElementArea(selectedCell, CellStatus.NONE)
			);

			// rewrite selected cells to selected element area in all cells where they might be
			// incorrectly removed in previous step
			this.selectedCellList.forEach((selectedCells) => {
				if (this.isSelectionPossiblySelected(selectedCells)) {
					this.setSelectedElementArea(selectedCells, CellStatus.SELECTED);
				}
			});

			this.processActiveCells();
		},

		/**
		 * Add selection to the selected cell list and then initiate processing.
		 */
		addSelected(): void {
			this.selectedCellList.push(this.selectedCell);

			this.setSelectedElementArea(this.selectedCell, CellStatus.SELECTED);

			this.processActiveCells();
		},

		/**
		 * Get a style object to place an absolutely positioned element over the selection.
		 * @param {SelectedCell} selectedCell
		 */
		getSelectedCellStyle(selectedCell: SelectedCell) {
			const { START_ROW, END_ROW, START_COL, END_COL } = SelectedCellValue;

			let top = 0;
			let bottom = 0;
			let left = 0;
			let right = 0;

			for (let i = 0; this.microRowArray[i] <= selectedCell[END_ROW]; i++) {
				if (this.microRowArray[i] < selectedCell[START_ROW]) {
					top = i + 1;
				} else {
					bottom = i;
				}
			}

			for (let i = 0; this.microColArray[i] <= selectedCell[END_COL]; i++) {
				if (this.microColArray[i] < selectedCell[START_COL]) {
					left = i + 1;
				} else {
					right = i;
				}
			}

			const topLeft =
				this.viewport?.toScreen(
					(left / this.microColArray.length) * this.viewport.worldWidth,
					(top / this.microRowArray.length) * this.viewport.worldHeight
				) || EMPTY_POINT;
			const bottomRight =
				this.viewport?.toScreen(
					((right + 1) / this.microColArray.length) * this.viewport.worldWidth,
					((bottom + 1) / this.microRowArray.length) * this.viewport.worldHeight
				) || EMPTY_POINT;

			return {
				left: `${topLeft.x}px`,
				top: `${topLeft.y}px`,
				right: `${this.screenWidth - bottomRight.x}px`,
				bottom: `${this.screenHeight - bottomRight.y}px`
			};
		},

		/**
		 * Return the type of drilldown to use given a selection.
		 * @param {SelectedCell} selectedCell
		 */
		getSelectedDrilldownType(selectedCell: SelectedCell): CellType {
			const { START_COL, END_COL } = SelectedCellValue;

			const startCol = selectedCell[START_COL];
			const endCol = selectedCell[END_COL];
			return startCol === endCol ? CellType.BAR : CellType.LINE;
		},

		/**
		 * Return the color function required given a selection.
		 * @param {SelectedCell} selectedCell
		 */
		getSelectedGraphColorFn(selectedCell: SelectedCell): any {
			switch (this.getSelectedDrilldownType(selectedCell)) {
				case CellType.BAR:
					return this.barColorFn;
				case CellType.LINE:
					return this.lineColorFn;
				default:
					return () => '#FF00FF';
			}
		},

		centerGraph(): any {
			this.viewport?.fit();
			this.viewport?.moveCenter(this.viewport.worldWidth / 2, this.viewport.worldHeight / 2);
		},

		// ///////////////////////////////////////////////////////////////////////////////
		// event handlers

		handleTick() {
			const visibleBounds = this.viewport?.getVisibleBounds();
			this.uniforms.uViewportWorldWidth = visibleBounds?.width || 0;
			this.uniforms.uViewportWorldHeight = visibleBounds?.height || 0;
		},

		handleKey({ shiftKey }: KeyboardEvent) {
			this.enableDrag = shiftKey;
			if (this.viewport) {
				this.viewport.pause = !shiftKey;
			}
		},

		handleScroll(e) {
			if (this.enableDrag) {
				e.preventDefault();
			}
		},

		handleMouseDown(e: FederatedPointerEvent) {
			if (this.enableDrag) {
				return;
			}

			const { START_ROW, START_COL } = SelectedCellValue;

			const world = this.viewport?.toWorld(e.screen.x, e.screen.y) || EMPTY_POINT;
			const matrixUv = { x: world.x / this.worldWidth, y: world.y / this.worldHeight };
			const selectedColId = this.microColArray[Math.floor(matrixUv.x * this.microColArray.length)];
			const selectedRowId = this.microRowArray[Math.floor(matrixUv.y * this.microRowArray.length)];

			this.selectedCell = Array(4) as SelectedCell;
			this.selectedCell[START_ROW] = selectedRowId;
			this.selectedCell[START_COL] = selectedColId;
		},

		handleMouseUp(e: FederatedPointerEvent) {
			if (this.enableDrag) {
				return;
			}

			const { START_ROW, END_ROW, START_COL, END_COL } = SelectedCellValue;

			// get selected cell row and column ids
			const world = this.viewport?.toWorld(e.screen.x, e.screen.y) || EMPTY_POINT;
			const matrixUv = { x: world.x / this.worldWidth, y: world.y / this.worldHeight };
			const selectedColId = this.microColArray[Math.floor(matrixUv.x * this.microColArray.length)];
			const selectedRowId = this.microRowArray[Math.floor(matrixUv.y * this.microRowArray.length)];

			// complete selected cell
			const startRow = Math.min(this.selectedCell[START_ROW], selectedRowId);
			const endRow = Math.max(this.selectedCell[START_ROW], selectedRowId);
			const startCol = Math.min(this.selectedCell[START_COL], selectedColId);
			const endCol = Math.max(this.selectedCell[START_COL], selectedColId);

			this.selectedCell[START_ROW] = startRow;
			this.selectedCell[START_COL] = startCol;
			this.selectedCell[END_ROW] = endRow;
			this.selectedCell[END_COL] = endCol;

			// trigger an action
			if (this.selectedCell.some((v) => Number.isNaN(v))) {
				// selection is out-of-bounds
				this.centerGraph();
				this.incrementUpdate();
			} else if (this.isSelectionSelected(this.selectedCell)) {
				this.removeSelected();
			} else {
				this.addSelected();
			}
		},

		selectedCellClick(selectedCellIndex) {
			this.selectedCellList.splice(selectedCellIndex, 1);
			this.processActiveCells();
		},

		// ///////////////////////////////////////////////////////////////////////////////
		// update handlers

		incrementUpdate() {
			this.update++;
		},

		incrementMove() {
			this.move++;
		}
	}
};
</script>

<style scoped>
main {
	position: relative;
}

.matrix {
	display: grid;
	position: absolute;
	overflow: hidden;
	height: 100%;
	width: 100%;
}
</style>
