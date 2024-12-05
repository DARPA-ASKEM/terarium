import { AssetType, ProjectSearchResult } from '@/types/Types';
import type { Project } from '@/types/Types';

export enum ProjectPages {
	OVERVIEW = 'overview'
}

/* Amalgamation of the ProjectSearchResult and Project types,
this allows us to display in the same table normal project and search results */
export interface ProjectWithKnnData extends ProjectSearchResult, Project {}

/**
 * Return the list of AssetTypes that are visible in the project
 * This takes the Types from the AssetType enum and filters out the ones that are not visible
 */
export const listOfVisibleAssetTypes: AssetType[] = Object.values(AssetType).filter(
	(type) =>
		![
			AssetType.Artifact,
			AssetType.Code,
			AssetType.Simulation,
			AssetType.ModelConfiguration,
			AssetType.InterventionPolicy,
			AssetType.NotebookSession,
			AssetType.Project
		].includes(type)
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
