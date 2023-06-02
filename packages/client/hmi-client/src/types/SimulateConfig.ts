export enum TspanUnits {
	'Units',
	'Date'
}

export type ChartConfig = {
	selectedVariable: string[];
	selectedRun: number;
};

export type RunResults = {
	[runId: string]: { [key: string]: number }[];
};
