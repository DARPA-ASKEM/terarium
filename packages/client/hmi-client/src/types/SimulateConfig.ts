export const enum RunType {
	Julia = 'julia',
	Ciemss = 'ciemss'
}

export type ChartConfig = {
	selectedVariable: string[];
	selectedRun: string;
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
