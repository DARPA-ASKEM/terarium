import { TimeSpan } from './Types';

export const enum RunType {
	Julia = 'julia',
	Ciemss = 'ciemss'
}

export enum TspanUnits {
	'Units',
	'Date'
}

export type ChartConfig = {
	selectedVariable: string[];
	selectedRun: string;
};

export type SimulationConfig = {
	runConfigs: { [runId: string]: InputMetadata };
	chartConfigs: string[][];
};

export type InputMetadata = {
	runId: string;
	active: boolean;
	configName?: string;
	numSamples?: number;
	method?: string;
	timeSpan?: TimeSpan;
};

export type DataseriesConfig = {
	data: { x: number; y: number }[];
	label: string;
	fill: boolean;
	borderColor?: string;
	borderWidth?: number;
	borderDash?: number[];
};

export type RunResults = {
	[runId: string]: { [key: string]: number }[];
};
