export const PUBLICATIONS = 'publications';
export const INTERMEDIATES = 'intermediates';
export const MODELS = 'models';
export const PLANS = 'plans';
export const SIMULATION_RUNS = 'simulation_runs';
export const DATASETS = 'datasets';

export type ProjectAssets = {
	[PUBLICATIONS]: string[];
	[INTERMEDIATES]: string[];
	[MODELS]: string[];
	[PLANS]: string[];
	[SIMULATION_RUNS]: string[];
	[DATASETS]: string[];
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
