import { DocumentType, DocumentAsset } from '@/types/Document';
import { AssetType } from '@/types/common';
import { Model } from './Model';

export type ProjectAssets = {
	[AssetType.DOCUMENT]: DocumentAsset[];
	[AssetType.INTERMEDIATE]: any[]; // FIXME: add proper type
	[AssetType.MODEL]: Model[];
	[AssetType.SIMULATION_PLAN]: any[]; // FIXME: add proper type
	[AssetType.SIMULATION_RUN]: any[]; // FIXME: add proper type
	[AssetType.DATASET]: any[]; // FIXME: add proper type
	[AssetType.CODE]: any[];
};

export type SimpleProjectAssets = {
	[AssetType.DOCUMENT]: (string | number)[];
	[AssetType.INTERMEDIATE]: (string | number)[];
	[AssetType.MODEL]: (string | number)[];
	[AssetType.SIMULATION_PLAN]: (string | number)[];
	[AssetType.SIMULATION_RUN]: (string | number)[];
	[AssetType.DATASET]: (string | number)[];
};

export interface IProject {
	id: string;
	name: string;
	title?: string;
	description: string;
	timestamp: string;
	active: boolean;
	concept: string | null;
	assets: SimpleProjectAssets;
	relatedDocuments: DocumentType[];
	username: string;
}
