/**
 * Datasets
 */

import API from '@/api/api';
import { Dataset } from '@/types/Dataset';

/**
 * Get all datasets
 * @return Array<Dataset>|null - the list of all datasets, or null if none returned by API
 */
async function getAll(): Promise<Dataset[] | null> {
	const response = await API.get('/datasets');
	return response?.data ?? null;
}

/**
 * Get all datasets
 * @return Array<Dataset>|null - the list of all datasets, or null if none returned by API
 */
async function getDataset(datasetId: string): Promise<Dataset | null> {
	const response = await API.get(`/datasets/${datasetId}`);
	return response?.data ?? null;
}

export { getAll, getDataset };
