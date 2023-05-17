/**
 * Datasets
 */

import API from '@/api/api';
import { logger } from '@/utils/logger';
import { CsvAsset, Dataset } from '@/types/Types';
import { addAsset } from '@/services/project';
import { ProjectAssetTypes } from '@/types/Project';

/**
 * Get all datasets
 * @return Array<Dataset>|null - the list of all datasets, or null if none returned by API
 */
async function getAll(): Promise<Dataset[] | null> {
	const response = await API.get('/datasets').catch((error) => {
		logger.error(`Error: ${error}`);
	});
	return response?.data ?? null;
}

/**
 * Get Dataset from the data service
 * @return Dataset|null - the dataset, or null if none returned by API
 */
async function getDataset(datasetId: string): Promise<Dataset | null> {
	const response = await API.get(`/datasets/${datasetId}`).catch((error) => {
		logger.error(`Error: data-service was not able to retreive the dataset ${datasetId} ${error}`);
	});
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
async function downloadRawFile(datasetId: string, binCount?: number): Promise<CsvAsset | null> {
	let URL = `/datasets/${datasetId}/files?row_limit=50`;
	if (binCount) URL += `&binCount=${binCount}`;
	const response = await API.get(URL).catch((error) => {
		logger.error(`Error: data-service was not able to retrieve the dataset's rawfile ${error}`);
	});
	return response?.data ?? null;
}

/**
 * Creates a new dataset in TDS from the dataset given (required name, url, description).
 * @param dataset the dataset with updated storage ID from tds
 */
async function createNewDataset(dataset: Dataset): Promise<Dataset | null> {
	const resp = await API.post('/datasets', dataset);
	if (resp && resp.status < 400 && resp.data) {
		return resp.data;
	}
	console.log(`Error creating new dataset ${resp?.status}`);
	return null;
}

/**
 * This is a helper function which creates a new dataset and adds a given CSV file to it. The data set will
 * share the same name as the file and can optionally have a description
 * @param file the CSV file
 * @param projectId the current project ID to add this dataset to
 * @param description description of the file. Optional. If not given description will be just the csv name
 */
async function createNewDatasetFromCSV(
	file: File,
	projectId: string,
	description?: string
): Promise<CsvAsset | null> {
	// Remove the file extension from the name, if any
	const name = file.name.substring(
		0,
		file.name.lastIndexOf('.') > 0 ? file.name.lastIndexOf('.') : file.name.length
	);

	const dataset: Dataset = {
		name,
		url: '',
		description: description || file.name
	};

	const newDataSet: Dataset | null = await createNewDataset(dataset);
	if (!newDataSet) return null;

	const formData = new FormData();
	formData.append('file', file);
	let resp = await API.post(`/datasets/${newDataSet.id}/files?filename=${file.name}`, formData);
	if (resp && resp.status < 400 && resp.data) {
		resp = await addAsset(projectId, ProjectAssetTypes.DATASETS, newDataSet.id);
	} else {
		console.log(`Error adding asset ${resp?.status}`);
		return null;
	}

	if (!newDataSet.id) return null;

	return downloadRawFile(newDataSet.id.toString());
}

export {
	getAll,
	getDataset,
	getBulkDatasets,
	downloadRawFile,
	createNewDatasetFromCSV,
	createNewDataset
};
