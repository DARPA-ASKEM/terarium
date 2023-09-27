/**
 * Documents Asset
 */

import API from '@/api/api';
import { logger } from '@/utils/logger';
/**
 * Get all documents
 * @return Array<Dataset>|null - the list of all datasets, or null if none returned by API
 */
async function getAll(): Promise<any | null> {
	const response = await API.get('/document-asset').catch((error) => {
		logger.error(`Error: ${error}`);
	});
	return response?.data ?? null;
}

/**
 * Get Dataset from the data service
 * @return Dataset|null - the dataset, or null if none returned by API
 */
async function getDocumentAsset(documentId: string): Promise<any | null> {
	const response = await API.get(`/document-asset/${documentId}`).catch((error) => {
		logger.error(`Error: data-service was not able to retreive the dataset ${documentId} ${error}`);
	});
	return response?.data ?? null;
}

export { getAll, getDocumentAsset };
