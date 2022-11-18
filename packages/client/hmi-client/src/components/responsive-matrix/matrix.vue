<template>
	<div class="matrix-container" ref="matrix_container">
		<div class="matrix" ref="matrix">
			<component
				v-for="(selectedCell, idx) in selectedCellList" :key="idx"
				:is="getSelectedDrilldownType(selectedCell)"
				:getSelectedCellStyle="getSelectedCellStyle"
				:update="update"
				:move="move"
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
			/>
		</div>
	</div>
</template>

<script lang="ts">
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
	} from 'pixi.js';
	import {
		SelectedCell,
		SelectedCellValue,
		CellStatus,
		CellType,
		Uniforms,
	} from '@/types/ResponsiveMatrix';
	import { uint32ArrayToRedIntTex } from './pixi-utils';

	import ResponsiveCellBarContainer from './cell-bar-container.vue';
	import ResponsiveCellLineContainer from './cell-line-container.vue';

	import matrixVS from './matrix.vs.glsl';
	import matrixFS from './matrix.fs.glsl';
	import matrixGridFS from './matrix-grid.fs.glsl';



	export default {

		// ---------------------------------------------------------------------------- //
		// components                                                                   //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		components: {
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
				type: Array as PropType< number[] | string[]>,
				default() {
					return [];
				},
			},
			cellLabelCol: {
				type: Array as PropType< number[] | string[]>,
				default() {
					return [];
				},
			},
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
				dataCellList: [] as object[],
				dataRowList: [] as object[][], // e.g. [[row1col1Obj, row1col2Obj]]
				dataColList: [] as object[][], // e.g. [[row1col1Obj, row2col1Obj]]
				dataParameters: new Set() as Set<string>, // e.g. ["age", "height", "weight"]
				dataParametersMin: {}, // e.g. {param1: 0, param2: 3}
				dataParametersMax: {}, // e.g. {param1: 10, param2: 17}
				labelRowList: [] as number[] | string[],
				labelColList: [] as number[] | string[],
				numRows: 0,
				numCols: 0,

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
				selectedCell: Array(4) as SelectedCell, // e.g. [startrow, startcol, endrow, endcol]

				update: 0,
				move: 0,
				enableDrag: false,
				resizeObserver: null as unknown as ResizeObserver,
			};
		},



		// ---------------------------------------------------------------------------- //
		// computed                                                                     //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		computed: {
			screenAspectRatio(): number {
				return this.screenHeight / this.screenWidth;
			},

			dataParametersArray(): string[] {
				return [...this.dataParameters];
			},

			numSelectedRows(): number {
				return this.selectedRows.reduce((acc: number, val: CellStatus) =>
					acc + Number(val !== CellStatus.NONE), 0);
			},

			numSelectedCols(): number {
				return this.selectedCols.reduce((acc: number, val: CellStatus) =>
					acc + Number(val !== CellStatus.NONE), 0);
			},

			microRowSettings(): number[] {
				const percentageSelectedRows = this.numSelectedRows
					? 0.5 + (this.numSelectedRows / this.numRows * 0.5)
					: 0;
				const microRowSettings = Array(2).fill(0);
				microRowSettings[CellStatus.NONE] = percentageSelectedRows
					? (1 - percentageSelectedRows) * this.numRows / (this.numRows - this.numSelectedRows)
					: 1;
				microRowSettings[CellStatus.SELECTED] = percentageSelectedRows
					? percentageSelectedRows * this.numRows / this.numSelectedRows
					: 1;
				const microRowSettingsMin =  Math.min(...microRowSettings);
				if(microRowSettingsMin < 1) {
					for(let i = 0; i < microRowSettings.length; i++) {
						microRowSettings[i] /= microRowSettingsMin;
					}
				}

				return microRowSettings;
			},
			microColSettings(): number[] {
				const percentageSelectedCols = this.numSelectedCols
					? 0.5 + (this.numSelectedCols / this.numCols * 0.5)
					: 0;
				const microColSettings = Array(2).fill(0);
				microColSettings[CellStatus.NONE] = percentageSelectedCols
					? (1 - percentageSelectedCols) * this.numCols / (this.numCols - this.numSelectedCols)
					: 1;
				microColSettings[CellStatus.SELECTED] = percentageSelectedCols
					? percentageSelectedCols * this.numCols / this.numSelectedCols
					: 1;
				const microColSettingsMin =  Math.min(...microColSettings);
				if(microColSettingsMin < 1) {
					for(let i = 0; i < microColSettings.length; i++) {
						microColSettings[i] /= microColSettingsMin;
					}
				}

				return microColSettings;
			},
		},



		// ---------------------------------------------------------------------------- //
		// beforeMount                                                                  //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		async beforeMount() {
			this.processData();
			this.processLabels();
			this.processActiveCells();
		},



		// ---------------------------------------------------------------------------- //
		// mounted                                                                      //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		async mounted() {
			const matrix = this.$refs.matrix as HTMLElement;
			const matrixContainer = this.$refs.matrix_container as HTMLElement;

			// ///////////////////////////////////////////////////////////////////////////////

			// initialize screen height/width
			this.screenHeight = matrix.offsetHeight;
			this.screenWidth = matrix.offsetWidth;

			// start the resize observer
			this.resizeObserver = new ResizeObserver(entries => {
				this.screenHeight = entries[0].contentRect.height;
				this.screenWidth = entries[0].contentRect.width;
			});
			this.resizeObserver.observe(matrixContainer);

			// start event listeners
			window.addEventListener("keydown", this.handleKey);
			window.addEventListener("keyup", this.handleKey);
			matrixContainer.addEventListener('wheel', this.handleScroll);

			// ///////////////////////////////////////////////////////////////////////////////

			// initialize pixi.js
			const app = new Application({
				resizeTo: matrixContainer,
				backgroundColor: this.backgroundColor,
			});
			matrix.appendChild(app.view as unknown as Node);

			// create viewport
			const screenHeight = this.screenHeight;
			const screenWidth = this.screenWidth;
			const cellAspectRatio = 1 || this.microRowArray.length / this.microColArray.length;
			const quadAspectRatio = this.screenAspectRatio * cellAspectRatio; // height / width
			const vertexPosition = quadAspectRatio > 1 // is height long?
				? [-1 / quadAspectRatio, -1, // height long
					1 / quadAspectRatio, -1, // x, y
					1 / quadAspectRatio, 1,
					-1 / quadAspectRatio, 1]
				: [-1, -1 * quadAspectRatio, // width long
					1, -1 * quadAspectRatio, // x, y
					1, 1 * quadAspectRatio,
					-1, 1 * quadAspectRatio];
			this.worldHeight = vertexPosition[5] - vertexPosition[3];
			this.worldWidth = vertexPosition[2] - vertexPosition[0];
			(this as any).$viewport = new Viewport({
				screenWidth: this.screenWidth,
				screenHeight: this.screenHeight,
				worldWidth: this.worldWidth,
				worldHeight: this.worldHeight,
				divWheel: matrix as HTMLElement,
				interaction: app.renderer.plugins.interaction // the interaction module is important for wheel to work properly when renderer.view is placed or scaled
			});

			// add the viewport to the stage
			app.stage.addChild((this as any).$viewport as unknown as DisplayObject);

			// activate camera interaction plugins
			(this as any).$viewport
				.drag()
				.pinch()
				.wheel();
			(this as any).$viewport.pause = true;

			// clamp camera
			(this as any).$viewport.clampZoom({
				maxWidth: this.worldWidth * 2,
				maxHeight: this.worldHeight * 2,
			});

			// build quad
			const geometry = new Geometry()
				.addAttribute('aVertexPosition',
					vertexPosition,
					2) // the size of the attribute
				.addAttribute('aUvs',
					[0, 0, // u, v
						1, 0,
						1, 1,
						0, 1], // u, v
					2) // the size of the attribute
				.addIndex([0, 1, 2, 0, 2, 3]);

			// build color array
			const color = this.createColorArray();

			// build uniforms
			this.uniforms = {
				// screen data
				uScreenWidth: screenWidth,
				uScreenHeight: screenHeight,

				// geometry/viewport data
				uWorldWidth: (this as any).$viewport.worldWidth,
				uWorldHeight: (this as any).$viewport.worldHeight,
				uViewportWorldWidth: (this as any).$viewport.worldWidth,
				uViewportWorldHeight: (this as any).$viewport.worldHeight,

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
				uColor: Texture.fromBuffer(color, this.numCols, this.numRows),

				uTime: 0,
			};

			// set callback to update uniforms every render call
			app.ticker.add(this.handleTick);

			// run shader on quad
			const quadShader = Shader.from(matrixVS, matrixFS, this.uniforms);
			const quad = new Mesh(geometry, quadShader);

			// center quad in world
			quad.position.set((this as any).$viewport.worldWidth / 2, (this as any).$viewport.worldHeight / 2);

			// add quad to viewport
			(this as any).$viewport.addChild(quad as any);

			// run shader on grid
			const gridShader = Shader.from(matrixVS, matrixGridFS, this.uniforms);
			const grid = new Mesh(geometry, gridShader);

			// center grid in world
			grid.position.set((this as any).$viewport.worldWidth / 2, (this as any).$viewport.worldHeight / 2);

			// add grid to viewport
			(this as any).$viewport.addChild(grid as any);

			// center and zoom camera to world
			this.centerGraph();

			// add pixi interaction handlers
			(this as any).$viewport.on('pointerdown', this.handleMouseDown);
			(this as any).$viewport.on('pointerup', this.handleMouseUp);
			(this as any).$viewport.on('moved' as any, this.incrementMove);
			(this as any).$viewport.on('zoomed-end' as any, this.incrementUpdate);
		},



		// ---------------------------------------------------------------------------- //
		// watch                                                                        //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

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
			},
		},



		// ---------------------------------------------------------------------------- //
		// unmounted                                                                    //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		unmounted() {
			this.resizeObserver.disconnect();
			window.removeEventListener("keydown", this.handleKey);
			window.removeEventListener("keyup", this.handleKey);
		},



		// ---------------------------------------------------------------------------- //
		// methods                                                                      //
		// ---------------------------------------------------------------------------- //

		// ///////////////////////////////////////////////////////////////////////////////

		methods: {

			// ///////////////////////////////////////////////////////////////////////////////
			// data methods

			/**
			 * Determines if data object array provided is structured correctly (as a 2D matrix).
			 * @param {object[][]} data
			 */
			isDataValid(data: object[][]) {
				if (data?.constructor !== Array || data[0]?.constructor !== Array) {
					return false;
				}

				return !data.some((row: any) => row.constructor !== Array || row.length !== data[0].length);
			},

			/**
			 * Ingests data, process and stores it in internal component state stores.
			 * @param {object[][]} data
			 */
			processData() {
				if(this.isDataValid(this.data)) {
					this.numRows = this.data.length;
					this.numCols = this.data[0]?.length;

					// find more efficient way to initialize 2D array?
					this.dataRowList = Array(this.numRows).fill(0).map(() => []);
					this.dataColList = Array(this.numCols).fill(0).map(() => []);

					// iterate through all the data and push the cell objects to 
					this.data.forEach((row: any, indexRow: number) => row.forEach((cell: any, indexCol: number) => {
						if(cell) {
							this.extractParams(cell);
							// find better solution than using reserved properties
							// for cell identification
							const cellData = {
								...cell,
								_row: indexRow,
								_col: indexCol,
								_idx: this.dataCellList.length,
							};

							this.dataCellList.push(cellData);
							this.dataRowList[indexRow].push(cellData);
							this.dataColList[indexCol].push(cellData);
						}
					}));
				} else {
					console.error('Data Invalid');
				}
			},

			/**
			 * Ingests labels data, process and stores it in internal component state stores.
			 * Assumes that processData has already been called.
			 */
			processLabels() {
				if(this.cellLabelRow.length) {
					this.labelRowList = this.cellLabelRow;
				} else { 
					this.labelRowList = Array(this.numRows).fill(0).map((v, i) => i);
				}

				if(this.cellLabelCol.length) {
					this.labelColList = this.cellLabelCol;
				} else { 
					this.labelColList = Array(this.numCols).fill(0).map((v, i) => i);
				}
			},

			/**
			 * Processes a single cell object and extract the parameters from it.
			 * As well update the parameters min and max state objects.
			* @param {object} cellObject
			 */
			extractParams(cellObject: object) {
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

			/**
			 * Processes the dataCellList to generate the color array.
			 */
			createColorArray(): Uint8Array {
				const data = this.dataCellList;
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
			},

			/**
			 * Creates a Uint32Array representing the row or column asignments for each micro row or column,
			 * using the status of each row or column and the number of micro rows or columns associated with
			 * each status.
			* @param {CellStatus[]} elStatusArray
			* @param {number[]} elStatusArray
			 */
			createMicroArray(elStatusArray: CellStatus[], statusSettingsArray: number[]): Uint32Array {
				const microArray: number[] = [];
				let i = 0;

				for(let elIdx = 0; elIdx < elStatusArray.length; elIdx++) {
					for(let fillIdx = 0; fillIdx < statusSettingsArray[elStatusArray[elIdx]]; fillIdx++) {
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
				const {
					START_ROW,
					END_ROW,
					START_COL,
					END_COL
				} = SelectedCellValue;

				this.selectedRows = Array(this.numRows).fill(CellStatus.NONE);
				this.selectedCols = Array(this.numCols).fill(CellStatus.NONE);

				this.selectedCellList.forEach(selectedCell => {
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
				this.uniforms.uMicroRow = uint32ArrayToRedIntTex(this.microRowArray, this.microRowArray.length, 1);

				this.microColArray = this.createMicroArray(this.selectedCols, this.microColSettings);
				// TODO: switch microRows to use 2D textures to avoid running into texture size limits
				this.uniforms.uMicroCol = uint32ArrayToRedIntTex(this.microColArray, this.microColArray.length, 1);

				// wait for the grid recalculation to complete before triggering children update
				await nextTick();
				this.incrementUpdate();
			},

			/**
			 * Given a selected cell array, determines if selection only consists of cells which
			 * have already been selected.
			 */
			isSelectionAlreadySelected(): boolean {
				const {
					START_COL,
					END_COL,
					START_ROW,
					END_ROW,
				} = SelectedCellValue;
				const { selectedCell } = this;
				const startCol = selectedCell[START_COL];
				const endCol = selectedCell[END_COL];
				const startRow = selectedCell[START_ROW];
				const endRow = selectedCell[END_ROW];

				for(let i = startRow; i <= endRow; i++) {
					if(this.selectedRows[i] === CellStatus.NONE) {
						return false;
					}
				}
				for(let i = startCol; i <= endCol; i++) {
					if(this.selectedCols[i] === CellStatus.NONE) {
						return false;
					}
				}
				return true;
			},

			/**
			 * Remove all selected cells in the selected cell list where the selection
			 * is completely enclosed in the selected cell.
			 */
			removeSelected(): void {
				const {
					START_COL,
					END_COL,
					START_ROW,
					END_ROW,
				} = SelectedCellValue;
				this.selectedCellList = this.selectedCellList.filter(selectedCell =>
					!(this.selectedCell[START_ROW] >= selectedCell[START_ROW]
						&& this.selectedCell[END_ROW] <= selectedCell[END_ROW]
						&& this.selectedCell[START_COL] >= selectedCell[START_COL]
						&& this.selectedCell[END_COL] <= selectedCell[END_COL])
				);
				this.processActiveCells();
			},

			/**
			 * Add selection to the selected cell list and then initiate processing.
			 */
			addSelected(): void {
				this.selectedCellList.push(this.selectedCell);
				this.processActiveCells();
			},

			/**
			 * Get a style object to place an absolutely positioned element over the selection.
			 * @param {SelectedCell} selectedCell
			 */
			getSelectedCellStyle(selectedCell: SelectedCell): object {
				const {
					START_ROW,
					END_ROW,
					START_COL,
					END_COL
				} = SelectedCellValue;

				let top = 0;
				let bottom = 0;
				let left = 0;
				let right = 0;

				for(let i = 0; this.microRowArray[i] <= selectedCell[END_ROW]; i++) {
					if(this.microRowArray[i] < selectedCell[START_ROW]) {
						top = i + 1;
					} else {
						bottom = i;
					}
				}

				for(let i = 0; this.microColArray[i] <= selectedCell[END_COL]; i++) {
					if(this.microColArray[i] < selectedCell[START_COL]) {
						left = i + 1;
					} else {
						right = i;
					}
				}

				const topLeft = (this as any).$viewport.toScreen(
					left / this.microColArray.length * (this as any).$viewport.worldWidth,
					top / this.microRowArray.length * (this as any).$viewport.worldHeight,
				);
				const bottomRight = (this as any).$viewport.toScreen(
					(right + 1) / this.microColArray.length * (this as any).$viewport.worldWidth,
					(bottom + 1) / this.microRowArray.length  * (this as any).$viewport.worldHeight,
				);

				return {
					left: `${topLeft.x}px`,
					top: `${topLeft.y}px`,
					right: `${this.screenWidth - bottomRight.x}px`,
					bottom: `${this.screenHeight - bottomRight.y}px`,
				};
			},

			/**
			 * Return the type of drilldown to use given a selection.
			 * @param {SelectedCell} selectedCell
			 */
			getSelectedDrilldownType(selectedCell: SelectedCell): CellType {
				const {
					START_COL,
					END_COL
				} = SelectedCellValue;

				const startCol = selectedCell[START_COL];
				const endCol = selectedCell[END_COL];
				return startCol === endCol ? CellType.BAR : CellType.LINE;
			},

			/**
			 * Return the color function required given a selection.
			 * @param {SelectedCell} selectedCell
			 */
			getSelectedGraphColorFn(selectedCell: SelectedCell): any {
				switch(this.getSelectedDrilldownType(selectedCell)) {
					case CellType.BAR:
						return this.barColorFn;
					case CellType.LINE:
						return this.lineColorFn;
					default:
						return () => '#FF00FF';
				}
			},

			centerGraph(): any {
				(this as any).$viewport.fit();
				(this as any).$viewport.moveCenter((this as any).$viewport.worldWidth / 2, (this as any).$viewport.worldHeight / 2);
			},

			// ///////////////////////////////////////////////////////////////////////////////
			// event handlers

			handleTick(delta) {
				const visibleBounds = (this as any).$viewport.getVisibleBounds();
				this.uniforms.uViewportWorldWidth = visibleBounds.width;
				this.uniforms.uViewportWorldHeight = visibleBounds.height;

				this.uniforms.uTime += delta;
			},

			handleKey({shiftKey}: KeyboardEvent) {
				this.enableDrag = shiftKey;
				(this as any).$viewport.pause = !shiftKey;
			},

			handleScroll(e) {
				if(this.enableDrag) {
					e.preventDefault();
				}
			},

			handleMouseDown(e: FederatedPointerEvent) {
				if(this.enableDrag) {
					return;
				}

				const {
					START_ROW,
					START_COL,
				} = SelectedCellValue;

				const world = (this as any).$viewport.toWorld(e.screen.x, e.screen.y);
				const matrixUv = { x: world.x / this.worldWidth, y: world.y / this.worldHeight };
				const selectedColId = this.microColArray[Math.floor(matrixUv.x * this.microColArray.length)];
				const selectedRowId = this.microRowArray[Math.floor(matrixUv.y * this.microRowArray.length)];

				this.selectedCell = Array(4) as SelectedCell;
				this.selectedCell[START_ROW] = selectedRowId;
				this.selectedCell[START_COL] = selectedColId;
			},

			handleMouseUp(e: FederatedPointerEvent) {
				if(this.enableDrag) {
					return;
				}

				const {
					START_ROW,
					END_ROW,
					START_COL,
					END_COL
				} = SelectedCellValue;

				// get selected cell row and column ids
				const world = (this as any).$viewport.toWorld(e.screen.x, e.screen.y);
				const matrixUv = { x: world.x / this.worldWidth, y: world.y / this.worldHeight };
				const selectedColId = this.microColArray[Math.floor(matrixUv.x * this.microColArray.length)];
				const selectedRowId = this.microRowArray[Math.floor(matrixUv.y * this.microRowArray.length)];

				// complete selected cell
				const startRow = Math.min(
					this.selectedCell[START_ROW],
					selectedRowId
				);
				const endRow = Math.max(
					this.selectedCell[START_ROW],
					selectedRowId
				);
				const startCol = Math.min(
					this.selectedCell[START_COL],
					selectedColId,
				);
				const endCol = Math.max(
					this.selectedCell[START_COL],
					selectedColId,
				);

				this.selectedCell[START_ROW] = startRow;
				this.selectedCell[START_COL] = startCol;
				this.selectedCell[END_ROW] = endRow;
				this.selectedCell[END_COL] = endCol;

				// trigger an action
				if(this.selectedCell.some(x => Number.isNaN(x))) { // selection is out-of-bounds
					this.centerGraph();
					this.incrementUpdate();
				} else if(this.isSelectionAlreadySelected()) {
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
		},
	};
</script>

<style scoped>
	.matrix-container {
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