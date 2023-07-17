export enum TspanUnits {
	'Units',
	'Date'
}

export type ChartConfig = {
	selectedVariable: string[];
	selectedRun: string;
};

export type RunResults = {
	[runId: string]: { [key: string]: number }[];
};
