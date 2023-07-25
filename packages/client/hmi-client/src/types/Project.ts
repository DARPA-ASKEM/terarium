import { DocumentAsset, Document, Dataset, Model, Artifact } from '@/types/Types';

// TODO: this should be an enum driven by our back end.
export enum ProjectAssetTypes {
	DOCUMENTS = 'publications',
	MODELS = 'models',
	SIMULATIONS = 'simulations',
	SIMULATION_WORKFLOW = 'workflows',
	DATASETS = 'datasets',
	CODE = 'code',
	ARTIFACTS = 'artifacts'
}

export enum ProjectPages {
	OVERVIEW = 'overview',
	CALIBRATE = 'calibrate',
	STRATIFY = 'stratify',
	EMPTY = ''
}

export const isProjectAssetTypes = (type: ProjectAssetTypes | string): boolean =>
	Object.values(ProjectAssetTypes).includes(type as ProjectAssetTypes);

export type ProjectAssets = {
	[ProjectAssetTypes.DOCUMENTS]: DocumentAsset[];
	[ProjectAssetTypes.MODELS]: Model[];
	[ProjectAssetTypes.DATASETS]: Dataset[];
	[ProjectAssetTypes.CODE]: any[];
	[ProjectAssetTypes.ARTIFACTS]: Artifact[];
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
