import { DocumentAsset, Document } from '@/types/Types';
import { Dataset } from '@/types/Dataset';
import { Model } from './Model';

export enum ProjectAssetTypes {
	DOCUMENTS = 'publications',
	MODELS = 'models',
	PLANS = 'plans',
	SIMULATIONS = 'simulations',
	SIMULATION_RUNS = 'simulation_runs',
	SIMULATION_WORKFLOW = 'simulation_workflow',
	DATASETS = 'datasets',
	CODE = 'code'
}

export enum ProjectPages {
	OVERVIEW = 'overview',
	EMPTY = ''
}

export const isProjectAssetTypes = (type: ProjectAssetTypes | string): boolean =>
	Object.values(ProjectAssetTypes).includes(type as ProjectAssetTypes);

export type ProjectAssets = {
	[ProjectAssetTypes.DOCUMENTS]: DocumentAsset[];
	[ProjectAssetTypes.MODELS]: Model[];
	[ProjectAssetTypes.PLANS]: any[]; // FIXME: add proper type
	[ProjectAssetTypes.SIMULATION_RUNS]: any[]; // FIXME: add proper type
	[ProjectAssetTypes.DATASETS]: Dataset[]; // FIXME: add proper type
	[ProjectAssetTypes.CODE]: any[];
};

export interface IProject {
	id: string;
	name: string;
	description: string;
	timestamp: string;
	active: boolean;
	concept: string | null;
	assets: ProjectAssets | null;
	relatedDocuments: Document[];
	username: string;
}
