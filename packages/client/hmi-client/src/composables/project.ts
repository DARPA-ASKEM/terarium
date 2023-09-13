import { IProject } from '@/types/Project';
import { Component, Ref, readonly, shallowRef } from 'vue';
import * as ProjectService from '@/services/project';
import * as CodeService from '@/services/code';
import * as ArtifactService from '@/services/artifact';
import * as DatasetService from '@/services/dataset';
import * as ModelService from '@/services/model';
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
		const updated = ProjectService.update(project);
		setTimeout(async () => {
			activeProject.value = await ProjectService.get(project.id, true);
		}, 1000);
		return updated;
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

	async function uploadArtifactToProject(
		progress: Ref<number>,
		file: File,
		userName: string,
		projectId: IProject['id'],
		description?: string
	) {
		const artifact = await ArtifactService.uploadArtifactToProject(
			progress,
			file,
			userName,
			description
		);
		if (artifact && artifact.id) {
			await addAsset(projectId, AssetType.Artifacts, artifact.id);
		}
		return artifact;
	}

	async function createNewArtifactFromGithubFile(
		repoOwnerAndName: string,
		path: string,
		userName: string,
		projectId: IProject['id']
	) {
		const artifact = await ArtifactService.createNewArtifactFromGithubFile(
			repoOwnerAndName,
			path,
			userName
		);
		if (artifact && artifact.id) {
			await addAsset(projectId, AssetType.Artifacts, artifact.id);
		}
		return artifact;
	}

	async function createNewDatasetFromCSV(
		progress: Ref<number>,
		file: File,
		userName: string,
		projectId: IProject['id'],
		description?: string
	) {
		const dataset = await DatasetService.createNewDatasetFromCSV(
			progress,
			file,
			userName,
			description
		);
		if (dataset && dataset.id) {
			await addAsset(projectId, AssetType.Datasets, dataset.id);
		}
		return dataset;
	}

	async function createNewDatasetFromGithubFile(
		repoOwnerAndName: string,
		path: string,
		userName: string,
		projectId: IProject['id'],
		url: string
	) {
		const dataset = await DatasetService.createNewDatasetFromGithubFile(
			repoOwnerAndName,
			path,
			userName,
			url
		);
		if (dataset && dataset.id) {
			await addAsset(projectId, AssetType.Datasets, dataset.id);
		}
		return dataset;
	}

	async function addNewModelToProject(modelName: string, projectId: IProject['id']) {
		const modelId = await ModelService.addNewModelToProject(modelName);
		if (modelId) {
			await addAsset(projectId, AssetType.Models, modelId);
		}
		return modelId;
	}

	async function saveDatasetFromSimulationResultToProject(
		projectId: IProject['id'],
		simulationId: string | undefined,
		datasetName: string | null
	) {
		if (!simulationId) return null;
		const dataset = await DatasetService.createDatasetFromSimulationResult(
			projectId,
			simulationId,
			datasetName
		);
		if (dataset) {
			await getActiveProject(projectId);
		}
		return dataset;
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
		uploadCodeToProjectFromGithub,
		uploadArtifactToProject,
		createNewArtifactFromGithubFile,
		createNewDatasetFromCSV,
		createNewDatasetFromGithubFile,
		addNewModelToProject,
		saveDatasetFromSimulationResultToProject
	};
}
