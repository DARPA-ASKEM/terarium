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
	// FIXME: add flag to fetch simulation vs. non-simulation datasets
	const response = await API.get('/datasets?is_simulation=true');
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

/**
 * Get the raw (CSV) file content for a given dataset
 * @return Array<string>|null - the dataset raw content, or null if none returned by API
 */
async function downloadRawFile(datasetId: string): Promise<string | null> {
	// FIXME: review exposing the "wide_format" and "data_annotation_flag" later
	const response = await API.get(
		`/datasets/${datasetId}/download/rawfile?wide_format=true&data_annotation_flag=false`
	);
	return response?.data ?? null;
}

export { getAll, getDataset, downloadRawFile };
