/**
 * Project
 */

import API from '@/api/api';
import { Project, ProjectAssets, PUBLICATIONS } from '@/types/Project';
import { getRelatedDocuments } from '@/services/data';
import { XDDArticle } from '@/types/XDD';

/**
 * Get a project per id
 * @return Project|null - the appropriate project, or null if none returned by API
 */
async function get(projectId: string): Promise<Project | null> {
	try {
		const response = await API.get(`/projects/${projectId}`);
		const { status, data } = response;
		if (status !== 200) return null;
		return data ?? null;
	} catch (error) {
		console.error(error);
		return null;
	}
}

/**
 * Get all projects
 * @return Array<Project>|null - the list of all projects, or null if none returned by API
 */
async function getAll(): Promise<Project[] | null> {
	const response = await API.get('/projects');
	return response?.data ?? null;
}

/**
 * Get project assets for a given project per id
 * @return ProjectAssets|null - the appropriate project, or null if none returned by API
 */
async function getAssets(projectId: string): Promise<ProjectAssets | null> {
	try {
		const response = await API.get(`/projects/${projectId}/assets`);
		const { status, data } = response;
		if (status !== 200) return null;
		return data ?? null;
	} catch (error) {
		console.error(error);
		return null;
	}
}

/**
 * add project asset
 * @projectId string - represents the project id wherein the asset will be added
 * @assetType string - represents the type of asset to be added, e.g., 'publications'
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
 * delete a project asset
 * @projectId string - represents the project id wherein the asset will be added
 * @assetType string - represents the type of asset to be added, e.g., 'publications'
 * @assetId string - represents the id of the asset to be added. This will be the internal id of some asset stored in one of the data service collections
 * @return any|null - some result if success, or null if none returned by API
 */
async function deleteAsset(projectId: string, assetsType: string, assetId) {
	// FIXME: handle cases where asset does not exist
	const url = `/projects/${projectId}/assets/${assetsType}/${assetId}`;
	const response = await API.delete(url);
	return response?.data ?? null;
}

// project id -> project assets -> publication list (Which will give you xdd id + url + title)  -> now you can use getRelatedDocuments given a xdd_uri (docid)
// TODO: Remove hardcoded dataset
async function getRelatedArticles(aProject: Project): Promise<XDDArticle[]> {
	const resp = await getAssets(aProject.id);
	try {
		const listOfRelatedArticles = await getRelatedDocuments(
			String(resp?.[PUBLICATIONS][0].xdd_uri),
			'xdd-covid-19'
		);
		return listOfRelatedArticles;
	} catch (error) {
		// If resp = null (project has no assets or cannot be found)
		return [];
	}
}

export { get, getAll, addAsset, deleteAsset, getAssets, getRelatedArticles };
