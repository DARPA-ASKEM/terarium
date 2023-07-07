import { Artifact } from '@/types/Types';
import API from '@/api/api';
import { ProjectAssetTypes } from '@/types/Project';
import { addAsset } from '@/services/project';
import { Ref } from 'vue';

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

	// Remove the file extension from the name, if any
	const name: string = fileName?.replace(/\.[^/.]+$/, '');

	// Create a new artifact with the same name as the file, and post the metadata to TDS
	const artifact: Artifact = {
		name,
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
	// Remove the file extension from the name, if any
	const name = file.name.replace(/\.[^/.]+$/, '');

	// Create a new artifact with the same name as the file, and post the metadata to TDS
	const artifact: Artifact = {
		name,
		description: description || file.name,
		fileNames: [file.name],
		username: userName
	};

	const newArtifact: Artifact | null = await createNewArtifact(artifact);
	if (!newArtifact || !newArtifact.id) return null;

	const successfulUpload = await addFileToArtifact(newArtifact.id, file);
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
async function addFileToArtifact(artifactId: string, file: File): Promise<boolean> {
	const formData = new FormData();
	formData.append('file', file);

	const response = await API.put(`/artifacts/${artifactId}/uploadFile`, formData, {
		params: {
			filename: file.name
		},
		headers: {
			'Content-Type': 'multipart/form-data'
		}
	});

	return response && response.status < 400;
}

export { uploadArtifactToProject, createNewArtifactFromGithubFile };
