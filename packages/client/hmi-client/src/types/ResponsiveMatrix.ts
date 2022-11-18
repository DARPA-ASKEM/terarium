import { Texture, Resource } from 'pixi.js';

export type SelectedCell = [number, number, number, number];

export enum SelectedCellValue {
	START_ROW,
	START_COL,
	END_ROW,
	END_COL
}

export enum CellType {
	BAR = 'ResponsiveCellBarContainer',
	LINE = 'ResponsiveCellLineContainer'
}

export enum CellStatus {
	NONE,
	SELECTED
}

export type Uniforms = {
	// screen data
	uScreenWidth: number;
	uScreenHeight: number;

	// geometry/viewport data
	uWorldWidth: number;
	uWorldHeight: number;
	uViewportWorldWidth: number;
	uViewportWorldHeight: number;

	// grid settings
	uGridDisplayBorder: boolean;
	uGridRowDisplayLim: number;
	uGridRowDisplayTransition: number;
	uGridColDisplayLim: number;
	uGridColDisplayTransition: number;

	// row/col data
	uNumRow: number;
	uNumCol: number;
	uMicroRow: Texture<Resource>;
	uMicroCol: Texture<Resource>;

	// cell element color data
	uColor: Texture;

	uTime: number;
};
