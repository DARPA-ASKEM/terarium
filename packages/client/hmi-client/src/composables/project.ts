import { IProject } from '@/types/Project';
import { readonly, shallowRef } from 'vue';
import * as ProjectService from '@/services/project';
import { AssetType } from '@/types/Types';

const TIMEOUT_MS = 1000;

const activeProject = shallowRef<IProject | null>(null);
const allProjects = shallowRef<IProject[]>([]);

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

	return {
		activeProject: readonly(activeProject),
		allProjects,
		getActiveProject,
		getAllProjects,
		addAsset,
		deleteAsset
	};
}
