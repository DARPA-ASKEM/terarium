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
	const response = await API.get('/datasets').catch((error) => {
		console.log('Error: ', error);
	});
	return response?.data ?? null;
}

/**
 * Get Dataset from the data service
 * @return Dataset|null - the dataset, or null if none returned by API
 */
async function getDataset(datasetId: string): Promise<Dataset | null> {
	const response = await API.get(`/datasets/${datasetId}`);
	return response?.data ?? null;
}

//
// Retrieve multiple datasets by their IDs
// FIXME: the backend does not support bulk fetch
//        so for now we are fetching by issueing multiple API calls
async function getBulkDatasets(datasetIDs: string[]) {
	const result: Dataset[] = [];
	const promiseList = [] as Promise<Dataset | null>[];
	datasetIDs.forEach((datasetId) => {
		promiseList.push(getDataset(datasetId));
	});
	const responsesRaw = await Promise.all(promiseList);
	responsesRaw.forEach((r) => {
		if (r) {
			result.push(r);
		}
	});
	return result;
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

export { getAll, getDataset, getBulkDatasets, downloadRawFile };
