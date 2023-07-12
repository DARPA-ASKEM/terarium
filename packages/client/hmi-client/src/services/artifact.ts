import { Artifact, PresignedURL } from '@/types/Types';
import API from '@/api/api';
import { ProjectAssetTypes } from '@/types/Project';
import { addAsset } from '@/services/project';
import { Ref } from 'vue';
import { logger } from '@/utils/logger';

/**
 * This is a helper function to take an arbitrary file from a github repo and create a new artifact from it
 * @param repoOwnerAndName
 * @param path
 * @param userName
 * @param projectId
 */
async function createNewArtifactFromGithubFile(
	repoOwnerAndName: string,
	path: string,
	userName: string,
	projectId: string
) {
	// Find the file name by removing the path portion
	const fileName: string | undefined = path.split('/').pop();

	if (!fileName) return null;

	// Create a new artifact with the same name as the file, and post the metadata to TDS
	const artifact: Artifact = {
		name: fileName,
		description: path,
		fileNames: [fileName],
		username: userName
	};

	const newArtifact: Artifact | null = await createNewArtifact(artifact);
	if (!newArtifact || !newArtifact.id) return null;

	const urlResponse = await API.put(
		`/artifacts/${newArtifact.id}/uploadArtifactFromGithub?filename=${fileName}&path=${path}&repoOwnerAndName=${repoOwnerAndName}`,
		{
			timeout: 30000
		}
	);

	if (!urlResponse || urlResponse.status >= 400) {
		logger.error(`Failed to upload artifact from github: ${urlResponse}`);
		return null;
	}

	return addAsset(projectId, ProjectAssetTypes.ARTIFACTS, newArtifact.id);
}

/**
 * This is a helper function which uploads an arbitrary document to TDS and creates a new artifact from it.
 * @param progress reference to display in ui
 * @param file the file to upload
 * @param userName owner of this project
 * @param projectId the project ID
 * @param description? description of the file. Optional. If not given description will be just the file name
 */
async function uploadArtifactToProject(
	progress: Ref<number>,
	file: File,
	userName: string,
	projectId: string,
	description?: string
): Promise<any> {
	// Create a new artifact with the same name as the file, and post the metadata to TDS
	const artifact: Artifact = {
		name: file.name,
		description: description || file.name,
		fileNames: [file.name],
		username: userName
	};

	const newArtifact: Artifact | null = await createNewArtifact(artifact);
	if (!newArtifact || !newArtifact.id) return null;

	const successfulUpload = await addFileToArtifact(newArtifact.id, file, progress);
	if (!successfulUpload) return null;

	return addAsset(projectId, ProjectAssetTypes.ARTIFACTS, newArtifact.id);
}

/**
 * Creates a new artifact in TDS and returns the new artifact object id
 * @param artifact the artifact to create
 */
async function createNewArtifact(artifact: Artifact): Promise<Artifact | null> {
	const response = await API.put('/artifacts', artifact);
	if (!response || response.status >= 400) return null;
	return response.data;
}

/**
 * Adds a file to an artifact in TDS
 * @param artifactId the artifact to add the file to
 * @param file the file to upload
 */
async function addFileToArtifact(
	artifactId: string,
	file: File,
	progress: Ref<number>
): Promise<boolean> {
	const formData = new FormData();
	formData.append('file', file);

	const response = await API.put(`/artifacts/${artifactId}/uploadFile`, formData, {
		params: {
			filename: file.name
		},
		headers: {
			'Content-Type': 'multipart/form-data'
		},
		onUploadProgress(progressEvent) {
			progress.value = Math.min(
				90,
				Math.round((progressEvent.loaded * 100) / (progressEvent?.total ?? 100))
			);
		},
		timeout: 30000
	});

	return response && response.status < 400;
}

async function getArtifactFileAsText(artifactId: string, fileName: string): Promise<string | null> {
	const response = await API.get(
		`/artifacts/${artifactId}/download-file-as-text?filename=${fileName}`,
		{}
	);

	if (!response || response.status >= 400) {
		logger.error('Error getting artifact file as text');
		return null;
	}

	return response.data;
}

async function getPresignedDownloadURL(
	artifactId: string,
	fileName: string
): Promise<PresignedURL | null> {
	const response = await API.get(`/artifacts/${artifactId}/download-url?filename=${fileName}`, {});

	if (!response || response.status >= 400) {
		logger.error('Error getting presigned download url');
		return null;
	}

	return response.data;
}

export {
	uploadArtifactToProject,
	createNewArtifactFromGithubFile,
	getArtifactFileAsText,
	getPresignedDownloadURL
};
