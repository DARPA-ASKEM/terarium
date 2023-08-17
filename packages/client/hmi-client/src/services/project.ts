/**
 * Project
 */

import API from '@/api/api';
import { IProject, ProjectAssets } from '@/types/Project';
import { logger } from '@/utils/logger';
import { Tab } from '@/types/common';
import DatasetIcon from '@/assets/svg/icons/dataset.svg?component';
import { Component } from 'vue';
import useResourcesStore from '@/stores/resources';
import * as EventService from '@/services/event';
import { DocumentAsset, EventType, Project, AssetType } from '@/types/Types';

/**
 * Create a project
 * @param name Project['name']
 * @param [description] Project['description']
 * @param [username] Project['username']
 * @return Project|null - the appropriate project, or null if none returned by API
 */
async function create(
	name: Project['name'],
	description: Project['description'] = '',
	username: Project['username'] = ''
): Promise<Project | null> {
	try {
		const project: Project = {
			name,
			description,
			username,
			active: true,
			assets: {} as Project['assets']
		};
		const response = await API.post(`/projects`, project);
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
		useResourcesStore().setActiveProject(await get(project.id, true));
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
async function getAll(): Promise<Project[] | null> {
	try {
		const response = await API.get('/projects');
		const { status, data } = response;
		if (status !== 200 || !data) return null;
		return (data as Project[]).reverse();
	} catch (error) {
		logger.error(error);
		return null;
	}
}

/**
 * Get project assets for a given project per id
 * @param projectId projet id to get assets for
 * @param types optional list of types. If none are given we assume you want it all
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
		} else {
			Object.values(AssetType).forEach((type, indx) => {
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
 * Get projects publication assets for a given project per id
 * @param projectId projet id to get assets for
 * @return DocumentAsset[] the documents assets for the project
 */
async function getPublicationAssets(projectId: string): Promise<DocumentAsset[]> {
	try {
		const url = `/projects/${projectId}/assets?types=${AssetType.Publications}`;
		const response = await API.get(url);
		const { status, data } = response;
		if (status === 200) {
			return data?.[AssetType.Publications] ?? ([] as DocumentAsset[]);
		}
	} catch (error) {
		logger.error(error);
	}
	return [] as DocumentAsset[];
}

/**
 * Add project asset
 * @projectId string - represents the project id wherein the asset will be added
 * @assetType string - represents the type of asset to be added, e.g., 'documents'
 * @assetId string - represents the id of the asset to be added. This will be the internal id of some asset stored in one of the data service collections
 * @return any|null - some result if success, or null if none returned by API
 */
async function addAsset(projectId: string, assetsType: string, assetId: string) {
	// FIXME: handle cases where assets is already added to the project
	const url = `/projects/${projectId}/assets/${assetsType}/${assetId}`;
	const response = await API.post(url);

	EventService.create(
		EventType.AddResourcesToProject,
		projectId,
		JSON.stringify({
			assetsType,
			assetId
		})
	);

	if (response.data) {
		useResourcesStore().setActiveProject(await get(projectId, true));
	}
	return response?.data ?? null;
}

/**
 * Delete a project asset
 * @projectId IProject["id"] - represents the project id wherein the asset will be added
 * @assetType AssetType - represents the type of asset to be added, e.g., 'documents'
 * @assetId string | number - represents the id of the asset to be added. This will be the internal id of some asset stored in one of the data service collections
 * @return boolean
 */
async function deleteAsset(
	projectId: IProject['id'],
	assetType: AssetType,
	assetId: string | number
): Promise<boolean> {
	try {
		const url = `/projects/${projectId}/assets/${assetType}/${assetId}`;
		const { status } = await API.delete(url);
		if (status >= 200 && status < 300) {
			useResourcesStore().setActiveProject(await get(projectId, true));
		}
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
 * Get the icon associated with an Asset
 */
const icons = new Map<string | AssetType, string | Component>([
	[AssetType.Publications, 'file'],
	[AssetType.Models, 'share-2'],
	[AssetType.Datasets, DatasetIcon],
	[AssetType.Simulations, 'settings'],
	// DVINCE TODO [ProjectAssetTypes.CODE, 'code'],
	[AssetType.Workflows, 'git-merge'],
	['overview', 'layout']
]);

function getAssetIcon(type: AssetType | string | null): string | Component {
	if (type && icons.has(type)) {
		return icons.get(type) ?? 'circle';
	}
	return 'circle';
}

/**
 * Get the xdd_uri of a Project Document
 */
function getDocumentAssetXddUri(project: IProject, assetId: Tab['assetId']): string | null {
	return (
		project.assets?.[AssetType.Publications]?.find(
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
	getAssetIcon,
	getPublicationAssets,
	getDocumentAssetXddUri
};
