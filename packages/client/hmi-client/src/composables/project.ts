/*
	Use `activeProject` to get the active project in your component. It is read only and should not be directly modified.
	`activeProject` can be refreshed by calling `getProject`
	Use the functions in this composable to make modifications to the project and to add/remove assets from it.
	Using these functions guarantees that such changes propogate to all components using `activeProject`.
	Using the resource store for project data is no longer needed.
*/

import { IProject } from '@/types/Project';
import { computed, shallowRef } from 'vue';
import * as ProjectService from '@/services/project';
import type { AssetType, PermissionRelationships } from '@/types/Types';
import useAuthStore from '@/stores/auth';

const TIMEOUT_MS = 100;

const activeProject = shallowRef<IProject | null>(null);
const projectLoading = shallowRef<boolean>(false);
const allProjects = shallowRef<IProject[] | null>(null);
const activeProjectId = computed<string>(() => activeProject.value?.id ?? '');

export function useProjects() {
	/**
	 * Refreshes the active project from backend if `projectId` is not defined.
	 * Otherwise get and set the active project to the one specified by `projectId`.
	 *
	 * @param {string} projectId Id of the project to set as the active project.
	 * @returns {Promise<IProject | null>} Active project.
	 */
	async function get(projectId: IProject['id']): Promise<IProject | null> {
		if (projectId) {
			projectLoading.value = true;
			activeProject.value = await ProjectService.get(projectId, true);
			projectLoading.value = false;
		} else {
			activeProject.value = null;
		}
		return activeProject.value;
	}

	async function refresh(): Promise<IProject | null> {
		const projectId: IProject['id'] = activeProjectId.value;
		if (projectId) {
			activeProject.value = await ProjectService.get(projectId, true);
		}
		return activeProject.value;
	}

	/**
	 * Refreshes the list of all projects from backend.
	 * @returns {Promise<IProject[]>} List of all projects.
	 */
	async function getAll(): Promise<IProject[]> {
		allProjects.value = (await ProjectService.getAll()) as unknown as IProject[];
		return allProjects.value;
	}

	/**
	 * If `projectId` is defined, add an asset to that project.
	 * Otherwise, add an asset to the active project and refresh it.
	 *
	 * @param {string} assetType Type of asset to be added, e.g., 'documents'.
	 * @param {string} assetId Id of the asset to be added. This will be the internal id of some asset stored in one of the data service collections.
	 * @param {string} [projectId] Id of the project to add the asset to.
	 * @returns {Promise<string|null>} Id of the added asset, if successful. Null, otherwise.
	 */
	async function addAsset(assetType: string, assetId: string, projectId?: string) {
		const newAssetId = await ProjectService.addAsset(
			projectId ?? activeProjectId.value,
			assetType,
			assetId
		);
		if (!projectId || projectId === activeProjectId.value) {
			setTimeout(async () => {
				activeProject.value = await ProjectService.get(activeProjectId.value, true);
			}, TIMEOUT_MS);
		}
		return newAssetId;
	}

	/**
	 * If `projectId` is defined, delete an asset from that project.
	 * Otherwise, delete an asset from the active project and refresh it.
	 *
	 * @param {string} assetType Type of asset to be deleted, e.g., 'documents'.
	 * @param {string} assetId Id of the asset to be deleted. This will be the internal id of some asset stored in one of the data service collections.
	 * @param {string} [projectId] Id of the project to delete the asset from.
	 * @returns {Promise<boolean>} True if the asset was successfuly deleted. False, otherwise.
	 */
	async function deleteAsset(assetType: AssetType, assetId: string, projectId?: string) {
		const deleted = await ProjectService.deleteAsset(
			projectId ?? activeProjectId.value,
			assetType,
			assetId
		);
		if (!projectId || projectId === activeProjectId.value) {
			setTimeout(async () => {
				activeProject.value = await ProjectService.get(activeProjectId.value, true);
			}, TIMEOUT_MS);
		}
		return deleted;
	}

	/**
	 * Create a new project and refresh the list of all projects.
	 *
	 * @param {string} name Name of the project.
	 * @param {string} description Short description.
	 * @param {string} username Username of the owner of the project.
	 * @returns {Promise<Project|null>} The created project, or null if none returned by the API.
	 */
	async function create(name: string, description: string, username: string) {
		const created = await ProjectService.create(name, description, username);
		setTimeout(async () => {
			getAll();
		}, TIMEOUT_MS);
		return created;
	}

	/**
	 * Update a project. If updated project is the active project, refresh it.
	 *
	 * @param {Iproject} project Project to update.
	 * @returns {Promise<string>} Id of the updated project.
	 */
	async function update(project: IProject) {
		const updated = await ProjectService.update(project);
		if (project.id === activeProjectId.value) {
			setTimeout(async () => {
				activeProject.value = await ProjectService.get(project.id, true);
			}, 1000);
		}
		return updated;
	}

	/**
	 * Remove a project and refresh the list of all projects. Sets the active project to null.
	 *
	 * @param {string} projectId Id of the project to remove.
	 * @returns {Promise<string>} Id of the project to remove.
	 */
	async function remove(projectId: IProject['id']) {
		const removed = await ProjectService.remove(projectId);
		setTimeout(async () => {
			getAll();
		}, TIMEOUT_MS);
		// `toString()` shouldn't be necessary but for some reason `activeProjectId.value` evaluates to a number
		if (removed && projectId === activeProjectId.value.toString()) {
			// removed project was the active project; set active project to null
			activeProject.value = null;
		}
		return removed;
	}

	/**
	 * Get projects publication assets for a given project per id.
	 *
	 * @param {string} projectId Project id to get assets for.
	 * @return {Promise<DocumentAsset[]>} The documents assets for the project.
	 */
	async function getPublicationAssets(projectId: IProject['id']) {
		return ProjectService.getPublicationAssets(projectId);
	}

	async function getPermissions(
		projectId: IProject['id']
	): Promise<PermissionRelationships | null> {
		return ProjectService.getPermissions(projectId);
	}

	async function setPermissions(projectId: IProject['id'], userId: string, relationship: string) {
		return ProjectService.setPermissions(projectId, userId, relationship);
	}

	async function removePermissions(
		projectId: IProject['id'],
		userId: string,
		relationship: string
	) {
		return ProjectService.removePermissions(projectId, userId, relationship);
	}

	async function updatePermissions(
		projectId: IProject['id'],
		userId: string,
		oldRelationship: string,
		to: string
	) {
		return ProjectService.updatePermissions(projectId, userId, oldRelationship, to);
	}

	async function clone(projectId: IProject['id']) {
		const username = useAuthStore().user?.name;
		if (!username) {
			return null;
		}
		const projectToClone = await ProjectService.get(projectId, true);
		if (!projectToClone || !projectToClone.assets) {
			return null;
		}
		const created = await ProjectService.create(
			`Copy of ${projectToClone.name}`,
			projectToClone.description,
			username
		);
		if (!created || !created.id) {
			return null;
		}
		// There doesn't seem to be a way to add multiple assets in one call yet
		Object.entries(projectToClone.assets).forEach(async (projectAsset) => {
			const [assetType, assets] = projectAsset;
			if (assets.length) {
				await Promise.all(
					assets.map(async (asset) => {
						await ProjectService.addAsset(created.id!, assetType, asset.id);
					})
				);
			}
		});
		return created;
	}

	return {
		activeProject,
		allProjects,
		projectLoading,
		get,
		getAll,
		addAsset,
		deleteAsset,
		create,
		update,
		remove,
		refresh,
		getPublicationAssets,
		getPermissions,
		setPermissions,
		removePermissions,
		updatePermissions,
		clone
	};
}
