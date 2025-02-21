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
