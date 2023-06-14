/**
 * Datasets
 */

import API from '@/api/api';
import { logger } from '@/utils/logger';
import { CsvAsset, Dataset, PresignedURL } from '@/types/Types';
import axios, { AxiosResponse } from 'axios';
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
async function downloadRawFile(datasetId: string, filename: string): Promise<CsvAsset | null> {
	const URL = `/datasets/${datasetId}/downloadCSV?filename=${filename}`;
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
 * @param userName owner of this project
 * @param projectId the project ID
 * @param description description of the file. Optional. If not given description will be just the csv name
 */
async function createNewDatasetFromCSV(
	file: File,
	userName: string,
	projectId: string,
	description?: string
): Promise<CsvAsset | null> {
	// Remove the file extension from the name, if any
	const name = file.name.replace(/\.[^/.]+$/, '');

	// Create a new dataset with the same name as the file, and post the metadata to TDS
	const dataset: Dataset = {
		name,
		url: '',
		description: description || file.name,
		fileNames: [file.name],
		username: userName
	};

	const newDataSet: Dataset | null = await createNewDataset(dataset);
	if (!newDataSet || !newDataSet.id) return null;

	// Now, get a signed URL from TDS and upload the file to S3
	const urlResponse: AxiosResponse<PresignedURL> = await API.get(
		`/datasets/${newDataSet.id}/upload-url?filename=${file.name}`
	);
	if (!urlResponse || urlResponse.status >= 400 || !urlResponse.data || !urlResponse.data.url) {
		return null;
	}

	// Upload the file to S3
	try {
		const s3response: AxiosResponse<any> = await axios.put(urlResponse.data.url, file, {
			headers: {
				'Content-Type': 'application/octet-stream',
				Bucket: 'askem-staging-data-service',
				Key: `datasets/${newDataSet.id}/${file.name}`
			}
		});

		if (!s3response || s3response.status >= 400) {
			console.log('Unable to post file.');
			return null;
		}
	} catch (e) {
		console.log(e);
		return null;
	}

	await addAsset(projectId, ProjectAssetTypes.DATASETS, newDataSet.id);

	// Now verify it all works and obtain a preview for the user.
	return downloadRawFile(newDataSet.id, file.name);
}

export {
	getAll,
	getDataset,
	getBulkDatasets,
	downloadRawFile,
	createNewDatasetFromCSV,
	createNewDataset
};
