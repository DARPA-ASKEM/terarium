import API from '@/api/api';
import { AssetType, Code, ProgrammingLanguage } from '@/types/Types';
import { Ref } from 'vue';
import { addAsset } from '@/services/project';
import { logger } from '@/utils/logger';

async function getCodeFileAsText(codeAssetId: string, fileName: string): Promise<string | null> {
	const response = await API.get(
		`/code-asset/${codeAssetId}/download-code-as-text?filename=${fileName}`,
		{}
	);

	if (!response || response.status >= 400) {
		logger.error('Error getting code file as text');
		return null;
	}

	return response.data;
}

async function uploadCodeToProject(
	projectId: string,
	file: File,
	progress: Ref<number>
): Promise<Code | null> {
	// Create a new code asset with the same name as the file and post the metadata to TDS
	const codeAsset: Code = {
		name: file.name,
		description: file.name,
		filename: file.name,
		language: getProgrammingLanguage(file.name)
	};

	const newCodeAsset: Code | null = await createNewCodeAsset(codeAsset);
	if (!newCodeAsset || !newCodeAsset.id) return null;

	const successfulUpload = await addFileToCodeAsset(newCodeAsset.id, file, progress);
	if (!successfulUpload) return null;

	const resp = addAsset(projectId, AssetType.Code, newCodeAsset.id);
	if (!resp) return null;

	return newCodeAsset;
}

/**
 * This is a helper function to take an arbitrary file from a github repo and create a new code asset from it
 * @param repoOwnerAndName
 * @param path
 * @param userName
 * @param projectId
 */
async function uploadCodeToProjectFromGithub(
	repoOwnerAndName: string,
	path: string,
	projectId: string,
	url: string
): Promise<Code | null> {
	// Find the file name by removing the path portion
	const fileName: string | undefined = path.split('/').pop();

	if (!fileName) return null;

	// Create a new code asset with the same name as the file and post the metadata to TDS
	const codeAsset: Code = {
		name: fileName,
		description: path,
		filename: fileName,
		language: getProgrammingLanguage(fileName),
		repoUrl: url
	};

	const newCode: Code | null = await createNewCodeAsset(codeAsset);
	if (!newCode || !newCode.id) return null;

	const urlResponse = await API.put(
		`/code-asset/${newCode.id}/uploadCodeFromGithub?filename=${fileName}&path=${path}&repoOwnerAndName=${repoOwnerAndName}`,
		{
			timeout: 30000
		}
	);

	if (!urlResponse || urlResponse.status >= 400) {
		logger.error(`Failed to upload code from github: ${urlResponse}`);
		return null;
	}

	const resp = addAsset(projectId, AssetType.Code, newCode.id);

	if (!resp) return null;

	return newCode;
}

async function createNewCodeAsset(codeAsset: Code): Promise<Code | null> {
	const response = await API.post('/code-asset', codeAsset);
	if (!response || response.status >= 400) return null;
	return response.data;
}

async function addFileToCodeAsset(
	codeAssetId: string,
	file: File,
	progress: Ref<number>
): Promise<boolean> {
	const formData = new FormData();
	formData.append('file', file);

	const response = await API.put(`/code-asset/${codeAssetId}/uploadFile`, formData, {
		params: {
			filename: file.name
		},
		headers: {
			'Content-Type': 'multipart/form-data'
		},
		onUploadProgress(progressEvent) {
			progress.value = Math.min(
				100,
				Math.round((progressEvent.loaded * 100) / (progressEvent?.total ?? 100))
			);
		}
	});

	return response && response.status < 400;
}

function getProgrammingLanguage(fileName: string): ProgrammingLanguage {
	// given the extension of a file, return the programming language
	const fileExtensiopn: string = fileName.split('.').pop() || '';
	switch (fileExtensiopn) {
		case 'py':
			return ProgrammingLanguage.Python;
		case 'jl':
			return ProgrammingLanguage.Julia;
		case 'r':
			return ProgrammingLanguage.R;
		default:
			return ProgrammingLanguage.Python; // TODO do we need an "unknown" language?
	}
}

export { uploadCodeToProject, getCodeFileAsText, uploadCodeToProjectFromGithub };
