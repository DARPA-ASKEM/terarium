import { Model } from './Model';
import { PublicationAsset } from './XDD';

export const PUBLICATIONS = 'publications';
export const INTERMEDIATES = 'intermediates';
export const MODELS = 'models';
export const PLANS = 'plans';
export const SIMULATION_RUNS = 'simulation_runs';
export const DATASETS = 'datasets';

export type ProjectAssets = {
	[PUBLICATIONS]: PublicationAsset[];
	[INTERMEDIATES]: any[]; // FIXME: add proper type
	[MODELS]: Model[];
	[PLANS]: any[]; // FIXME: add proper type
	[SIMULATION_RUNS]: any[]; // FIXME: add proper type
	[DATASETS]: any[]; // FIXME: add proper type
};

export type SimpleProjectAssets = {
	[PUBLICATIONS]: (string | number)[];
	[INTERMEDIATES]: (string | number)[];
	[MODELS]: (string | number)[];
	[PLANS]: (string | number)[];
	[SIMULATION_RUNS]: (string | number)[];
	[DATASETS]: (string | number)[];
};

export type Project = {
	id: string;
	name: string;
	description: string;
	timestamp: string;
	active: boolean;
	concept: string | null;
	assets: SimpleProjectAssets;
};
