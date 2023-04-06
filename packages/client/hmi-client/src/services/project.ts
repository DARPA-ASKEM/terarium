/**
 * Project
 */

import API from '@/api/api';
import { IProject, ProjectAssets, ProjectAssetTypes } from '@/types/Project';
import { logger } from '@/utils/logger';
import { Tab } from '@/types/common';

/**
 * Create a project
 * @param name Project['name']
 * @param [description] Project['description']
 * @return Project|null - the appropriate project, or null if none returned by API
 */
async function create(
	name: IProject['name'],
	description: IProject['description'] = '',
	username: IProject['username'] = ''
): Promise<IProject | null> {
	try {
		const response = await API.post(`/projects`, { name, description, username });
		const { status, data } = response;
		if (status !== 201) return null;
		return data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

async function update(project: IProject): Promise<IProject | null> {
	try {
		const { id, name, description, active, username } = project;
		const response = await API.put(`/projects/${id}`, { id, name, description, active, username });
		const { status, data } = response;
		if (status !== 200) {
			return null;
		}
		return data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

/**
 * Remove a project (soft-delete)
 * @param projectId {IProject["id"]} - the id of the project to be removed
 * @return boolean - if the removal was succesful
 */
async function remove(projectId: IProject['id']): Promise<boolean> {
	try {
		const { status } = await API.delete(`/projects/${projectId}`);
		return status === 200;
	} catch (error) {
		logger.error(error);
		return false;
	}
}

/**
 * Get all projects
 * @return Array<Project>|null - the list of all projects, or null if none returned by API
 */
async function getAll(): Promise<IProject[] | null> {
	try {
		const response = await API.get('/projects');
		const { status, data } = response;
		if (status !== 200 || !data) return null;
		return data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

/**
 * Get project assets for a given project per id
 * @return ProjectAssets|null - the appropriate project, or null if none returned by API
 */
async function getAssets(projectId: string, types?: string[]): Promise<ProjectAssets | null> {
	try {
		let url = `/projects/${projectId}/assets`;
		if (types) {
			types.forEach((type, indx) => {
				// add URL with format: ...?types=A&types=B&types=C
				url += `${indx === 0 ? '?' : '&'}types=${type}`;
			});
		}
		const response = await API.get(url);
		const { status, data } = response;
		if (status !== 200) return null;
		return data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

/**
 * Add project asset
 * @projectId string - represents the project id wherein the asset will be added
 * @assetType string - represents the type of asset to be added, e.g., 'documents'
 * @assetId string - represents the id of the asset to be added. This will be the internal id of some asset stored in one of the data service collections
 * @return any|null - some result if success, or null if none returned by API
 */
async function addAsset(projectId: string, assetsType: string, assetId) {
	// FIXME: handle cases where assets is already added to the project
	const url = `/projects/${projectId}/assets/${assetsType}/${assetId}`;
	const response = await API.post(url);
	return response?.data ?? null;
}

/**
 * Delete a project asset
 * @projectId IProject["id"] - represents the project id wherein the asset will be added
 * @assetType ProjectAssetTypes - represents the type of asset to be added, e.g., 'documents'
 * @assetId string | number - represents the id of the asset to be added. This will be the internal id of some asset stored in one of the data service collections
 * @return boolean
 */
async function deleteAsset(
	projectId: IProject['id'],
	assetsType: ProjectAssetTypes,
	assetId: string | number
): Promise<boolean> {
	try {
		const url = `/projects/${projectId}/assets/${assetsType}/${assetId}`;
		const { status } = await API.delete(url);
		return status >= 200 && status < 300;
	} catch (error) {
		logger.error(error);
		return false;
	}
}

/**
 * Get a project per id
 * @param projectId - string
 * @param containingAssetsInformation - boolean - Add the assets information during the same call
 * @return Project|null - the appropriate project, or null if none returned by API
 */
async function get(
	projectId: string,
	containingAssetsInformation: boolean = false
): Promise<IProject | null> {
	try {
		const { status, data } = await API.get(`/projects/${projectId}`);
		if (status !== 200) return null;
		const project = data as IProject;

		if (project && containingAssetsInformation) {
			const assets = await getAssets(projectId);
			if (assets) {
				project.assets = assets;
			}
		}

		return project ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

/**
 * Get all informations for the homepage
 */
async function home(): Promise<IProject[] | null> {
	try {
		const { status, data } = await API.get('/home');
		if (status !== 200 || !data) return null;
		return data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

/**
 * Get the icon associated with an Asset
 */
const icons = new Map([
	[ProjectAssetTypes.DOCUMENTS, 'pi-file'],
	[ProjectAssetTypes.MODELS, 'pi-share-alt'],
	[ProjectAssetTypes.DATASETS, 'ci-dataset'],
	[ProjectAssetTypes.SIMULATIONS, 'pi-cog'],
	[ProjectAssetTypes.SIMULATION_RUNS, 'pi-eye'],
	[ProjectAssetTypes.CODE, 'pi-code'],
	['overview', 'ci-overview']
]);
function iconClassname(type: ProjectAssetTypes | string | null): string {
	if (type && icons.has(type)) {
		return `pi ${icons.get(type)}`;
	}
	return 'pi pi-circle';
}

/**
 * Get the xdd_uri of a Project Document
 */
function getDocumentAssetXddUri(project: IProject, assetId: Tab['assetId']): string | null {
	return (
		project.assets?.[ProjectAssetTypes.DOCUMENTS]?.find(
			(document) => document?.id === Number.parseInt(assetId ?? '', 10)
		)?.xdd_uri ?? null
	);
}

export {
	create,
	update,
	get,
	remove,
	getAll,
	addAsset,
	deleteAsset,
	getAssets,
	home,
	iconClassname,
	getDocumentAssetXddUri
};
