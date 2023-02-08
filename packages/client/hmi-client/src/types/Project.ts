import { XDDArticle, DocumentAsset } from '@/types/XDD';
import { Model } from './Model';

export enum ProjectAssetTypes {
	DOCUMENTS = 'publications',
	INTERMEDIATES = 'intermediates',
	MODELS = 'models',
	PLANS = 'plans',
	SIMULATION_RUNS = 'simulation_runs',
	DATASETS = 'datasets',
	CODE = 'code'
}

export type ProjectAssets = {
	[ProjectAssetTypes.DOCUMENTS]: DocumentAsset[];
	[ProjectAssetTypes.INTERMEDIATES]: any[]; // FIXME: add proper type
	[ProjectAssetTypes.MODELS]: Model[];
	[ProjectAssetTypes.PLANS]: any[]; // FIXME: add proper type
	[ProjectAssetTypes.SIMULATION_RUNS]: any[]; // FIXME: add proper type
	[ProjectAssetTypes.DATASETS]: any[]; // FIXME: add proper type
	[ProjectAssetTypes.CODE]: any[];
};

export type SimpleProjectAssets = {
	[ProjectAssetTypes.DOCUMENTS]: (string | number)[];
	[ProjectAssetTypes.INTERMEDIATES]: (string | number)[];
	[ProjectAssetTypes.MODELS]: (string | number)[];
	[ProjectAssetTypes.PLANS]: (string | number)[];
	[ProjectAssetTypes.SIMULATION_RUNS]: (string | number)[];
	[ProjectAssetTypes.DATASETS]: (string | number)[];
};

export type Project = {
	id: string;
	name: string;
	title?: string;
	description: string;
	timestamp: string;
	active: boolean;
	concept: string | null;
	assets: SimpleProjectAssets;
	relatedArticles: XDDArticle[];
	username: string;
};
