import { AssetType } from '@/types/Types';

export enum ProjectPages {
	OVERVIEW = 'overview'
}

export const isProjectAssetTypes = (type: AssetType | string): boolean =>
	Object.values(AssetType).includes(type as AssetType);
