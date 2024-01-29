/*
	Use `activeProject` to get the active project in your component. It is read only and should not be directly modified.
	`activeProject` can be refreshed by calling `getProject`
	Use the functions in this composable to make modifications to the project and to add/remove assets from it.
	Using these functions guarantees that such changes propogate to all components using `activeProject`.
	Using the resource store for project data is no longer needed.
*/

import { computed, shallowRef } from 'vue';
import * as ProjectService from '@/services/project';
import type { PermissionRelationships, Project, ProjectAsset } from '@/types/Types';
import { AssetType } from '@/types/Types';
import useAuthStore from '@/stores/auth';

const TIMEOUT_MS = 100;

const activeProject = shallowRef<Project | null>(null);
const projectLoading = shallowRef<boolean>(false);
const allProjects = shallowRef<Project[] | null>(null);
const activeProjectId = computed<string>(() => activeProject.value?.id ?? '');

export function useProjects() {
	/**
	 * Refreshes the active project from backend if `projectId` is not defined.
	 * Otherwise, get and set the active project to the one specified by `projectId`.
	 *
	 * @param {Project['id']} projectId Id of the project to set as the active project.
	 * @returns {Promise<Project | null>} Active project.
	 */
	async function get(projectId: Project['id']): Promise<Project | null> {
		if (projectId) {
			projectLoading.value = true;
			activeProject.value = await ProjectService.get(projectId);
			projectLoading.value = false;
		} else {
			activeProject.value = null;
		}
		return activeProject.value;
	}

	async function refresh(): Promise<Project | null> {
		if (activeProjectId.value) {
			activeProject.value = await ProjectService.get(activeProjectId.value);
		}
		return activeProject.value;
	}

	/**
	 * Refreshes the list of all projects from backend.
	 * @returns {Promise<Project[]>} List of all projects.
	 */
	async function getAll(): Promise<Project[]> {
		allProjects.value = (await ProjectService.getAll()) as Project[];
		return allProjects.value;
	}

	/**
	 * Return all the asset of a certain AssetType from the active project.
	 * @param assetType
	 * @returns ProjectAsset[]
	 */
	function getActiveProjectAssets(assetType: AssetType) {
		return (
			activeProject.value?.projectAssets.filter((asset) => asset.assetType === assetType) ??
			([] as ProjectAsset[])
		);
	}

	/**
	 * If `projectId` is defined, add an asset to that project.
	 * Otherwise, add an asset to the active project and refresh it.
	 *
	 * @param {string} assetType Type of asset to be added, e.g., 'documents'.
	 * @param {string} assetId Id of the asset to be added. This will be the internal id of some asset stored in one of the data service collections.
	 * @param {Project['id']} [projectId] Id of the project to add the asset to.
	 * @returns {Promise<string|null>} Id of the added asset, if successful. Null, otherwise.
	 */
	async function addAsset(
		assetType: string,
		assetId: ProjectAsset['id'],
		projectId?: Project['id']
	): Promise<ProjectAsset['id']> {
		if (!assetId) return undefined;
		const newAssetId = await ProjectService.addAsset(
			projectId ?? activeProjectId.value,
			assetType,
			assetId
		);
		if (!projectId || projectId === activeProjectId.value) {
			setTimeout(async () => {
				activeProject.value = await ProjectService.get(activeProjectId.value);
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
	 * @param {Project['id']} [projectId] Id of the project to delete the asset from.
	 * @returns {Promise<boolean>} True if the asset was successfully deleted. False, otherwise.
	 */
	async function deleteAsset(assetType: AssetType, assetId: string, projectId?: Project['id']) {
		const deleted = await ProjectService.deleteAsset(
			projectId ?? activeProjectId.value,
			assetType,
			assetId
		);
		if (!projectId || projectId === activeProjectId.value) {
			setTimeout(async () => {
				activeProject.value = await ProjectService.get(activeProjectId.value);
			}, TIMEOUT_MS);
		}
		return deleted;
	}

	/**
	 * Create a new project and refresh the list of all projects.
	 *
	 * @param {string} name Name of the project.
	 * @param {string} description Short description.
	 * @param {string} userId ID of the owner of the project.
	 * @returns {Promise<Project|null>} The created project, or null if none returned by the API.
	 */
	async function create(name: string, description: string, userId: string) {
		const created = await ProjectService.create(name, description, userId);
		setTimeout(async () => {
			getAll();
		}, TIMEOUT_MS);
		return created;
	}

	/**
	 * Update a project. If updated project is the active project, refresh it.
	 *
	 * @param {Project} project Project to update.
	 * @returns {Promise<string>} Id of the updated project.
	 */
	async function update(project: Project) {
		const updated = await ProjectService.update(project);
		if (project.id === activeProjectId.value) {
			setTimeout(async () => {
				activeProject.value = await ProjectService.get(project.id);
			}, 1000);
		}
		return updated;
	}

	/**
	 * Remove a project and refresh the list of all projects. Sets the active project to null.
	 *
	 * @param projectId Id of the project to remove.
	 * @returns Id of the project to remove.
	 */
	async function remove(projectId: Project['id']) {
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

	async function getPermissions(projectId: Project['id']): Promise<PermissionRelationships | null> {
		return ProjectService.getPermissions(projectId);
	}

	async function setPermissions(projectId: Project['id'], userId: string, relationship: string) {
		return ProjectService.setPermissions(projectId, userId, relationship);
	}

	async function removePermissions(projectId: Project['id'], userId: string, relationship: string) {
		return ProjectService.removePermissions(projectId, userId, relationship);
	}

	async function updatePermissions(
		projectId: Project['id'],
		userId: string,
		oldRelationship: string,
		to: string
	) {
		return ProjectService.updatePermissions(projectId, userId, oldRelationship, to);
	}

	async function clone(projectId: Project['id']) {
		const userId = useAuthStore().user?.id;
		if (!userId) {
			return null;
		}
		const projectToClone = await ProjectService.get(projectId);
		if (!projectToClone) {
			return null;
		}
		const created = await ProjectService.create(
			`Copy of ${projectToClone.name}`,
			projectToClone.description,
			userId
		);
		if (!created || !created.id) {
			return null;
		}
		// There doesn't seem to be a way to add multiple assets in one call yet
		// Object.entries(projectToClone.assets).forEach(async (projectAsset) => {
		// 	const [assetType, assets] = projectAsset;
		// 	if (assets.length) {
		// 		await Promise.all(
		// 			assets.map(async (asset) => {
		// 				await ProjectService.addAsset(created.id!, assetType, asset.id);
		// 			})
		// 		);
		// 	}
		// });
		return created;
	}

	return {
		activeProject,
		allProjects,
		projectLoading,
		get,
		getAll,
		getActiveProjectAssets,
		addAsset,
		deleteAsset,
		create,
		update,
		remove,
		refresh,
		getPermissions,
		setPermissions,
		removePermissions,
		updatePermissions,
		clone
	};
}
