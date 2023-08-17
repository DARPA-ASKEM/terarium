import { DocumentAsset, Document, Dataset, Model, Artifact, AssetType } from '@/types/Types';
import { Workflow } from '@/types/workflow';

export enum ProjectPages {
	OVERVIEW = 'overview',
	CALIBRATE = 'calibrate',
	STRATIFY = 'stratify',
	EMPTY = ''
}

export const isProjectAssetTypes = (type: AssetType | string): boolean =>
	Object.values(AssetType).includes(type as AssetType);

export type ProjectAssets = {
	[AssetType.Publications]: DocumentAsset[];
	[AssetType.Models]: Model[];
	[AssetType.Datasets]: Dataset[];
	// DVINCE TODO[ProjectAssetTypes.CODE]: any[];
	[AssetType.Artifacts]: Artifact[];
	[AssetType.Workflows]: Workflow[];
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
