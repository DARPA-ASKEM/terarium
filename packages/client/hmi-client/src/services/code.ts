import API, { getProjectIdFromUrl } from '@/api/api';
import type { Code, ProjectAsset } from '@/types/Types';
import { ProgrammingLanguage } from '@/types/Types';
import { Ref } from 'vue';
import { activeProjectId } from '@/composables/activeProject';

async function getCodeAsset(codeAssetId: string): Promise<Code | null> {
	const response = await API.get(`/code-asset/${codeAssetId}`);

	if (!response) {
		return null;
	}

	return response.data as Code;
}

async function updateCodeAsset(code: Code, file?: File, progress?: Ref<number>): Promise<Code | null> {
	if (!code.id) {
		return null;
	}

	if (file && progress) {
		const successfulUpload = await addFileToCodeAsset(code.id, file, progress);
		if (!successfulUpload) {
			return null;
		}
	}
	const response = await API.put(`/code-asset/${code.id}`, code);

	if (!response) {
		return null;
	}

	return response.data;
}

async function getCodeFileAsText(codeAssetId: string, fileName: string): Promise<string | null> {
	const response = await API.get(`/code-asset/${codeAssetId}/download-code-as-text?filename=${fileName}`, {});

	if (!response) {
		return null;
	}

	return response.data;
}

async function uploadCodeToProject(file: File, progress: Ref<number>): Promise<ProjectAsset | null> {
	const projectId = activeProjectId.value || getProjectIdFromUrl();

	const formData = new FormData();
	formData.append('file', file);

	const response = await API.post(`/code-asset/upload-code?project-id=${projectId}`, formData, {
		params: {
			name: file.name,
			description: file.name,
			filename: file.name
		},
		headers: {
			'Content-Type': 'multipart/form-data'
		},
		onUploadProgress(progressEvent) {
			progress.value = Math.min(100, Math.round((progressEvent.loaded * 100) / (progressEvent?.total ?? 100)));
		}
	});

	if (response && response.status < 400 && response.data) {
		return response.data;
	}
	return null;
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
	url: string
): Promise<Code | null> {
	// Find the file name by removing the path portion
	const fileName: string | undefined = path.split('/').pop();

	if (!fileName) return null;

	// Create a new code asset with the same name as the file and post the metadata to TDS
	const codeAsset: Code = {
		name: fileName,
		description: path,
		repoUrl: url
	};

	const newCode: Code | null = await createNewCodeAsset(codeAsset);
	if (!newCode || !newCode.id) return null;

	const urlResponse = await API.put(
		`/code-asset/${newCode.id}/upload-code-from-github?filename=${fileName}&path=${path}&repo-owner-and-name=${repoOwnerAndName}`,
		{
			timeout: 3600000
		}
	);

	if (!urlResponse) {
		return null;
	}

	return newCode;
}

async function uploadCodeFromGithubRepo(repoOwnerAndName: string, repoUrl: string): Promise<Code | null> {
	// Create a new code asset with the same name as the file and post the metadata to TDS
	const repoName = `${repoOwnerAndName.split('/')[1]}.zip`;

	if (!repoName) return null;

	const codeAsset: Code = {
		name: repoName,
		description: repoName,
		repoUrl
	};

	const newCode: Code | null = await createNewCodeAsset(codeAsset);
	if (!newCode || !newCode.id) return null;

	const urlResponse = await API.put(
		`/code-asset/${newCode.id}/upload-code-from-github-repo?repo-owner-and-name=${repoOwnerAndName}&repo-name=${repoName}`,
		{
			timeout: 3600000
		}
	);

	if (!urlResponse) {
		return null;
	}

	return newCode;
}

async function createNewCodeAsset(codeAsset: Code): Promise<Code | null> {
	const projectId = activeProjectId.value || getProjectIdFromUrl();
	const response = await API.post(`/code-asset?project-id=${projectId}`, codeAsset);
	if (!response || response.status >= 400) return null;
	return response.data;
}

async function addFileToCodeAsset(codeAssetId: string, file: File, progress: Ref<number>): Promise<boolean> {
	const formData = new FormData();
	formData.append('file', file);

	const response = await API.put(`/code-asset/${codeAssetId}/upload-code`, formData, {
		params: {
			filename: file.name
		},
		headers: {
			'Content-Type': 'multipart/form-data'
		},
		onUploadProgress(progressEvent) {
			progress.value = Math.min(100, Math.round((progressEvent.loaded * 100) / (progressEvent?.total ?? 100)));
		}
	});

	return response && response.status < 400;
}

function getProgrammingLanguage(fileName: string): ProgrammingLanguage {
	// given the extension of a file, return the programming language
	const fileExtension: string = fileName.split('.').pop() || '';
	switch (fileExtension) {
		case 'py':
			return ProgrammingLanguage.Python;
		case 'jl':
			return ProgrammingLanguage.Julia;
		case 'r':
			return ProgrammingLanguage.R;
		case 'zip':
			return ProgrammingLanguage.Zip;
		default:
			return ProgrammingLanguage.Python; // TODO do we need an "unknown" language?
	}
}

function getFileExtension(language: ProgrammingLanguage): string {
	switch (language) {
		case ProgrammingLanguage.Python:
			return 'py';
		case ProgrammingLanguage.Julia:
			return 'jl';
		case ProgrammingLanguage.R:
			return 'r';
		case ProgrammingLanguage.Zip:
			return 'zip';
		default:
			return '';
	}
}

function setFileExtension(fileName: string, language: ProgrammingLanguage) {
	// Find the last '.' to find the file extension index.  Anything before this is the filename.
	if (fileName.includes('.')) {
		const fileExtensionIndex = fileName.lastIndexOf('.');
		fileName = fileName.slice(0, fileExtensionIndex);
	}

	// Add the appropriate extension based on the selected language
	return fileName.concat('.').concat(getFileExtension(language));
}

export {
	uploadCodeToProject,
	getCodeFileAsText,
	uploadCodeToProjectFromGithub,
	getCodeAsset,
	updateCodeAsset,
	addFileToCodeAsset,
	getFileExtension,
	getProgrammingLanguage,
	setFileExtension,
	uploadCodeFromGithubRepo
};
