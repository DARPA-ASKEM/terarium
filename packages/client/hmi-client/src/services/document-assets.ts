/**
 * Documents Asset
 */

import API, { getProjectIdFromUrl } from '@/api/api';
import { DocumentAsset, ProjectAsset } from '@/types/Types';
import { logger } from '@/utils/logger';
import { Ref } from 'vue';
import { activeProjectId } from '@/composables/activeProject';

/**
 * Get all documents
 * @return Array<DocumentAsset>|null - the list of all document assets, or null if none returned by API
 */
async function getAll(): Promise<DocumentAsset[] | null> {
	const response = await API.get('/document-asset').catch((error) => {
		logger.error(`Error: ${error}`);
	});
	return response?.data ?? null;
}

/**
 * Get DocumentAsset from the data service
 * Note that projectId is optional as projectId is assigned by the axios API interceptor if value is available from activeProjectId. If the method is call from place where activeProjectId is not available, projectId should be passed as an argument as all endpoints requires projectId as a parameter.
 * @return DocumentAsset|null - the dataset, or null if none returned by API
 */
async function getDocumentAsset(documentId: string, projectId?: string): Promise<DocumentAsset | null> {
	const response = await API.get(`/document-asset/${documentId}`, {
		params: { 'project-id': projectId }
	}).catch((error) => {
		logger.error(`Error: data-service was not able to retreive the document asset ${documentId} ${error}`);
	});
	return response?.data ?? null;
}

/**
 * This is a helper function which uploads an arbitrary document to TDS and creates a new document asset from it.
 * @param file the file to upload
 * @param userName owner of this project
 * @param projectId the project ID
 * @param description? description of the file. Optional. If not given description will be just the file name
 * @param progress? reference to display in ui
 */
async function uploadDocumentAssetToProject(
	file: File,
	description?: string,
	progress?: Ref<number>
): Promise<ProjectAsset | null> {
	const projectId = activeProjectId.value || getProjectIdFromUrl();

	const formData = new FormData();
	formData.append('file', file);

	const response = await API.post(`/document-asset/upload-document?project-id=${projectId}`, formData, {
		params: {
			name: file.name,
			description: description || file.name,
			filename: file.name
		},
		headers: {
			'Content-Type': 'multipart/form-data'
		},
		onUploadProgress(progressEvent) {
			if (progress) {
				progress.value = Math.min(90, Math.round((progressEvent.loaded * 100) / (progressEvent?.total ?? 100)));
			}
		},
		timeout: 3600000
	});

	if (response && response.status < 400 && response.data) {
		return response.data;
	}
	return null;
}

async function createNewDocumentFromGithubFile(
	repoOwnerAndName: string,
	path: string,
	userId: string,
	description?: string
) {
	// Find the file name by removing the path portion
	const fileName: string | undefined = path.split('/').pop();

	if (!fileName) return null;

	// Create a new document with the same name as the file, and post the metadata to TDS
	const documentAsset: DocumentAsset = {
		name: fileName,
		description: description || fileName,
		fileNames: [fileName],
		userId
	};

	const newDocument: DocumentAsset | null = await createNewDocumentAsset(documentAsset);
	if (!newDocument || !newDocument.id) return null;

	const urlResponse = await API.put(
		`/document-asset/${newDocument.id}/upload-document-from-github?filename=${fileName}&path=${path}&repo-owner-and-name=${repoOwnerAndName}`,
		{
			timeout: 3600000
		}
	);

	if (!urlResponse || urlResponse.status >= 400) {
		return null;
	}

	return newDocument;
}

/**
 * Creates a new document asset in TDS and returns the new document asset object id
 * @param document the document asset to create
 */
async function createNewDocumentAsset(documentAsset: DocumentAsset): Promise<DocumentAsset | null> {
	const projectId = activeProjectId.value || getProjectIdFromUrl();
	const response = await API.post(`/document-asset?project-id=${projectId}`, documentAsset);
	if (!response || response.status >= 400) return null;
	return response.data;
}

async function downloadDocumentAsset(documentId: string, fileName: string): Promise<string | null> {
	try {
		const response = await API.get(`document-asset/${documentId}/download-document?filename=${fileName}`, {
			responseType: 'arraybuffer'
		});
		const blob = new Blob([response?.data], { type: 'application/pdf' });
		const pdfLink = window.URL.createObjectURL(blob);
		return pdfLink ?? null;
	} catch (error) {
		logger.error(`Unable to download PDF file for document asset ${documentId}: ${error}`);
		return null;
	}
}

async function getDocumentFileAsText(documentId: string, fileName: string): Promise<string | null> {
	const response = await API.get(`/document-asset/${documentId}/download-document-as-text?filename=${fileName}`, {});

	if (!response) {
		return null;
	}

	return response.data;
}

async function getEquationFromImageUrl(documentId: string, filename: string): Promise<string | null> {
	const response = await API.get(`/document-asset/${documentId}/image-to-equation?filename=${filename}`, {});

	if (!response) {
		return null;
	}

	return response.data;
}

async function getBulkDocumentAssets(docIDs: string[]) {
	const result: DocumentAsset[] = [];
	const promiseList = [] as Promise<DocumentAsset | null>[];
	docIDs.forEach((docId) => {
		promiseList.push(getDocumentAsset(docId));
	});
	const responsesRaw = await Promise.all(promiseList);
	responsesRaw.forEach((r) => {
		if (r) {
			result.push(r);
		}
	});
	return result;
}

export {
	createNewDocumentAsset,
	createNewDocumentFromGithubFile,
	downloadDocumentAsset,
	getAll,
	getBulkDocumentAssets,
	getDocumentAsset,
	getDocumentFileAsText,
	getEquationFromImageUrl,
	uploadDocumentAssetToProject
};
