import {
	Artifact,
	AssetType,
	Code,
	Dataset,
	DocumentAsset,
	ExternalPublication,
	Model,
	Project
} from '@/types/Types';
import { Workflow } from '@/types/workflow';

export enum ProjectPages {
	OVERVIEW = 'overview'
}

export const isProjectAssetTypes = (type: AssetType | string): boolean =>
	Object.values(AssetType).includes(type.toUpperCase() as AssetType);

// TODO this is essentially the same as Assets from Types.tx, however for some reason the
// Workflows class referenced here is only implemented on the front end and not
// driven by the TypeScrypt generation on the backend. This should be fixed.
export type ProjectAssets = {
	[AssetType.Publication]: ExternalPublication[];
	[AssetType.Model]: Model[];
	[AssetType.Dataset]: Dataset[];
	[AssetType.Code]: Code[];
	[AssetType.Artifact]: Artifact[];
	[AssetType.Workflow]: Workflow[];
	[AssetType.Document]: DocumentAsset[];
};

// TODO this is essentially the same as Project from Types.ts, however it references
// the above ProjectAssets type instead of the Assets type. This should be fixed.
export interface IProject extends Project {
	assets: ProjectAssets | null;
}
