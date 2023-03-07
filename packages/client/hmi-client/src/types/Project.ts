import { DocumentType, DocumentAsset } from '@/types/Document';
import { IAsset } from '@/types/common';
import { Model } from './Model';

export type ProjectAssets = {
	[IAsset.DOCUMENT]: DocumentAsset[];
	[IAsset.INTERMEDIATE]: any[]; // FIXME: add proper type
	[IAsset.MODEL]: Model[];
	[IAsset.SIMULATION_PLAN]: any[]; // FIXME: add proper type
	[IAsset.SIMULATION_RUN]: any[]; // FIXME: add proper type
	[IAsset.DATASET]: any[]; // FIXME: add proper type
	[IAsset.CODE]: any[];
};

export type SimpleProjectAssets = {
	[IAsset.DOCUMENT]: (string | number)[];
	[IAsset.INTERMEDIATE]: (string | number)[];
	[IAsset.MODEL]: (string | number)[];
	[IAsset.SIMULATION_PLAN]: (string | number)[];
	[IAsset.SIMULATION_RUN]: (string | number)[];
	[IAsset.DATASET]: (string | number)[];
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
