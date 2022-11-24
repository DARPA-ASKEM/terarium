export type ProjectAssets = {
	publications: string[];
	intermediates: string[];
	models: string[];
	plans: string[];
	simulation_runs: string[];
	datasets: string[];
};

export type Project = {
	id: string;
	name: string;
	description: string;
	timestamp: string;
	active: boolean;
	concept: string | null;
	assets: ProjectAssets;
};
