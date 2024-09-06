import { AssetType } from '@/types/Types';

export enum ProjectPages {
	OVERVIEW = 'overview'
}

/**
 * Return the list of AssetTypes that are visible in the project
 * This takes the Types from the AssetType enum and filters out the ones that are not visible
 */
export const listOfVisibleAssetTypes: AssetType[] = Object.values(AssetType).filter(
	(type) =>
		![AssetType.Artifact, AssetType.Simulation, AssetType.ModelConfiguration, AssetType.InterventionPolicy].includes(
			type
		)
);

/**
 * Check if the asset type is a project asset type
 * @param type
 */
export const isProjectAssetTypes = (type: AssetType | string): boolean =>
	Object.values(AssetType).includes(type as AssetType);

/**
 * Check if the asset type is a project visible asset type
 * @param type
 */
export const isVisibleProjectAssetTypes = (type: AssetType | string): boolean =>
	listOfVisibleAssetTypes.includes(type as AssetType);
