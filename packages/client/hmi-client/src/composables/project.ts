import { IProject } from '@/types/Project';
import { Component, Ref, readonly, shallowRef } from 'vue';
import * as ProjectService from '@/services/project';
import * as CodeService from '@/services/code';
import { AssetType } from '@/types/Types';
import DatasetIcon from '@/assets/svg/icons/dataset.svg?component';

const TIMEOUT_MS = 1000;

const activeProject = shallowRef<IProject | null>(null);
const allProjects = shallowRef<IProject[] | null>(null);

/**
 * Get the icon associated with an Asset
 */
const icons = new Map<string | AssetType, string | Component>([
	[AssetType.Publications, 'file'],
	[AssetType.Models, 'share-2'],
	[AssetType.Datasets, DatasetIcon],
	[AssetType.Simulations, 'settings'],
	[AssetType.Code, 'code'],
	[AssetType.Workflows, 'git-merge'],
	['overview', 'layout']
]);

export function useProjects() {
	async function getActiveProject(projectId: IProject['id']): Promise<IProject | null> {
		if (projectId && !!projectId) {
			activeProject.value = await ProjectService.get(projectId, true);
		} else {
			activeProject.value = null;
		}
		return activeProject.value;
	}

	async function getAllProjects(): Promise<IProject[]> {
		allProjects.value = (await ProjectService.getAll()) as unknown as IProject[];
		return allProjects.value;
	}

	async function addAsset(projectId: string, assetType: string, assetId: string) {
		const newAssetId = await ProjectService.addAsset(projectId, assetType, assetId);
		setTimeout(async () => {
			activeProject.value = await ProjectService.get(projectId as IProject['id'], true);
		}, TIMEOUT_MS);
		return newAssetId;
	}

	async function deleteAsset(projectId: string, assetType: AssetType, assetId: string) {
		const deleted = ProjectService.deleteAsset(projectId, assetType, assetId);
		setTimeout(async () => {
			activeProject.value = await ProjectService.get(projectId as IProject['id'], true);
		}, 1000);
		return deleted;
	}

	async function create(name: string, description: string, username: string) {
		return ProjectService.create(name, description, username);
	}

	async function update(project: IProject) {
		return ProjectService.update(project);
	}

	async function remove(projectId: IProject['id']) {
		return ProjectService.remove(projectId);
	}

	async function getPublicationAssets(projectId: IProject['id']) {
		return ProjectService.getPublicationAssets(projectId);
	}

	function getAssetIcon(type: AssetType | string | null): string | Component {
		if (type && icons.has(type)) {
			return icons.get(type) ?? 'circle';
		}
		return 'circle';
	}

	async function uploadCodeToProject(projectId: IProject['id'], file: File, progress: Ref<number>) {
		const code = await CodeService.uploadCodeToProject(file, progress);
		if (code && code.id) {
			await addAsset(projectId, AssetType.Code, code.id);
		}
		return code;
	}

	async function uploadCodeToProjectFromGithub(
		projectId: IProject['id'],
		repoOwnerAndName: string,
		path: string,
		url: string
	) {
		const code = await CodeService.uploadCodeToProjectFromGithub(repoOwnerAndName, path, url);
		if (code && code.id) {
			await addAsset(projectId, AssetType.Code, code.id);
		}
		return code;
	}

	return {
		activeProject: readonly(activeProject),
		allProjects: readonly(allProjects),
		getActiveProject,
		getAllProjects,
		addAsset,
		deleteAsset,
		create,
		update,
		remove,
		getPublicationAssets,
		getAssetIcon,
		uploadCodeToProject,
		uploadCodeToProjectFromGithub
	};
}
