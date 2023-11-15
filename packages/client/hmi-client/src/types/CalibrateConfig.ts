export type CalibrateConfig = {
	runConfigs: { [runId: string]: CalibrateStore };
	chartConfigs: string[][];
};

export type CalibrateStore = {
	runId: string;
	active: boolean;
	loss?: { [key: string]: number }[];
	params?: { [key: string]: number };
};
