/**
 * Project
 */

import API from '@/api/api';
import { IProject, ProjectAssets } from '@/types/Project';
import { logger } from '@/utils/logger';
import DatasetIcon from '@/assets/svg/icons/dataset.svg?component';
import { Component } from 'vue';
import * as EventService from '@/services/event';
import {
	AssetType,
	EventType,
	ExternalPublication,
	PermissionRelationships,
	Project
} from '@/types/Types';

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
	userId: Project['userId'] = ''
): Promise<Project | null> {
	try {
		const project: Project = {
			name,
			description,
			userId,
			projectAssets: {} as Project['projectAssets']
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
		const { id, name, description, active, username, assets } = project;
		const response = await API.put(`/projects/${id}`, {
			id,
			name,
			description,
			active,
			username,
			assets
		});
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
async function getAll(): Promise<Project[] | null> {
	try {
		const response = await API.get(`/projects`);
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
 * @return ExternalPublication[] the documents assets for the project
 */
async function getPublicationAssets(projectId: string): Promise<ExternalPublication[]> {
	try {
		const url = `/projects/${projectId}/assets?types=${AssetType.Publication}`;
		const response = await API.get(url);
		const { status, data } = response;
		if (status === 200) {
			return data?.[AssetType.Publication] ?? ([] as ExternalPublication[]);
		}
	} catch (error) {
		logger.error(error);
	}
	return [] as ExternalPublication[];
}

/**
 * Add project asset
 * @projectId string - represents the project id wherein the asset will be added
 * @assetType string - represents the type of asset to be added, e.g., 'documents'
 * @assetId string - represents the id of the asset to be added. This will be the internal id of some asset stored in one of the data service collections
 * @return any|null - some result if success, or null if none returned by API
 */
async function addAsset(projectId: string, assetType: string, assetId: string) {
	// FIXME: handle cases where assets is already added to the project
	const url = `/projects/${projectId}/assets/${assetType}/${assetId}`;
	const response = await API.post(url);

	EventService.create(
		EventType.AddResourcesToProject,
		projectId,
		JSON.stringify({
			assetType,
			assetId
		})
	);
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

async function getPermissions(projectId: IProject['id']): Promise<PermissionRelationships | null> {
	try {
		const { status, data } = await API.get(`projects/${projectId}/permissions`);
		if (status !== 200) {
			return null;
		}
		return data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

async function setPermissions(projectId: IProject['id'], userId: string, relationship: string) {
	try {
		const { status, data } = await API.post(
			`projects/${projectId}/permissions/user/${userId}/${relationship}`
		);
		if (status !== 200) {
			return null;
		}
		return data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

async function removePermissions(projectId: IProject['id'], userId: string, relationship: string) {
	try {
		const { status, data } = await API.delete(
			`projects/${projectId}/permissions/user/${userId}/${relationship}`
		);
		if (status !== 200) {
			return null;
		}
		return data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

async function updatePermissions(
	projectId: IProject['id'],
	userId: string,
	oldRelationship: string,
	to: string
): Promise<PermissionRelationships | null> {
	try {
		const { status, data } = await API.put(
			`projects/${projectId}/permissions/user/${userId}/${oldRelationship}?to=${to}`
		);
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
 * Get the icon associated with an Asset
 */
const icons = new Map<string | AssetType, string | Component>([
	[AssetType.Document, 'file'],
	[AssetType.Model, 'share-2'],
	[AssetType.Dataset, DatasetIcon],
	[AssetType.Simulation, 'settings'],
	[AssetType.Code, 'code'],
	[AssetType.Workflow, 'git-merge'],
	['overview', 'layout']
]);

function getAssetIcon(type: AssetType | string | null): string | Component {
	if (type && icons.has(type)) {
		return icons.get(type) ?? 'circle';
	}
	return 'circle';
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
	getPermissions,
	setPermissions,
	removePermissions,
	updatePermissions
};
