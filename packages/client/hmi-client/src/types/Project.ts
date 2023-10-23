import {
	DocumentAsset,
	Document,
	Dataset,
	Model,
	Artifact,
	AssetType,
	Code,
	ExternalPublication
} from '@/types/Types';
import { Workflow } from '@/types/workflow';

export enum ProjectPages {
	OVERVIEW = 'overview',
	CALIBRATE = 'calibrate',
	STRATIFY = 'stratify',
	EMPTY = ''
}

export const isProjectAssetTypes = (type: AssetType | string): boolean =>
	Object.values(AssetType).includes(type as AssetType);

// TODO this is essentially the same as Assets from Types.tx, however for some reason the
// Workflows class referenced here is only implemented on the front end and not
// driven by the TypeScrypt generation on the backend. This should be fixed.
export type ProjectAssets = {
	[AssetType.Publications]: ExternalPublication[];
	[AssetType.Models]: Model[];
	[AssetType.Datasets]: Dataset[];
	[AssetType.Code]: Code[];
	[AssetType.Artifacts]: Artifact[];
	[AssetType.Workflows]: Workflow[];
	[AssetType.Documents]: DocumentAsset[];
};

// TODO this is essentially the same as Project from Types.ts, however it references
// the above ProjectAssets type instead of the Assets type. This should be fixed.
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
	metadata?: { [index: string]: string };
	publicProject?: boolean;
	userPermission?: string;
}
