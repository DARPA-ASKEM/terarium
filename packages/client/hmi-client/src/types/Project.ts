export type Project = {
	id: string;
	name: string;
	description: string;
	timestamp: string;
	assets: {
		publications: number[];
		intermediates: number[];
		models: number[];
		plans: number[];
		simulation_runs: number[];
	};
};
