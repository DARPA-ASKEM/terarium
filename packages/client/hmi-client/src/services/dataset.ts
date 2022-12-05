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

const getDataset = async (datasetId: string) => API.get(`/datasets/${datasetId}`);

export { getAll, getDataset };
