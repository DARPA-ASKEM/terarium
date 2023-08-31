/**
 * Datasets
 */

import API from '@/api/api';
import { logger } from '@/utils/logger';
import { AssetType, CsvAsset, Dataset } from '@/types/Types';
import { addAsset } from '@/services/project';
import { Ref } from 'vue';
import { AxiosResponse } from 'axios';
import useResourcesStore from '@/stores/resources';
import * as ProjectService from '@/services/project';

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

/**
 * Update dataset from the dataservice
 * @return Dataset|null - the dataset, or null if none returned by API
 */
async function updateDataset(dataset: Dataset) {
	delete dataset.columns;
	const response = await API.patch(`/datasets/${dataset.id}`, dataset);
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
async function downloadRawFile(
	datasetId: string,
	filename: string,
	limit: number = 100
): Promise<CsvAsset | null> {
	const URL = `/datasets/${datasetId}/downloadCSV?filename=${filename}&limit=${limit}`;
	const response = await API.get(URL).catch((error) => {
		logger.error(`Error: data-service was not able to retrieve the dataset's rawfile ${error}`);
	});
	return response?.data ?? null;
}

/**
 * Creates a new dataset in TDS from the dataset given (required name, url, description).
 * @param dataset the dataset with updated storage ID from tds
 */
async function createDataset(dataset: Dataset): Promise<Dataset | null> {
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
 * @param repoOwnerAndName
 * @param path
 * @param userName
 * @param projectId
 * @param url the source url of the file
 */
async function createNewDatasetFromGithubFile(
	repoOwnerAndName: string,
	path: string,
	userName: string,
	projectId: string,
	url: string
) {
	// Find the file name by removing the path portion
	const fileName: string | undefined = path.split('/').pop();

	if (!fileName) return null;

	// Remove the file extension from the name, if any
	const name: string = fileName?.replace(/\.[^/.]+$/, '');

	// Create a new dataset with the same name as the file, and post the metadata to TDS
	const dataset: Dataset = {
		name,
		datasetUrl: url,
		description: path,
		fileNames: [fileName],
		username: userName
	};

	const newDataset: Dataset | null = await createDataset(dataset);
	if (!newDataset || !newDataset.id) return null;

	const urlResponse = await API.put(
		`/datasets/${newDataset.id}/uploadCSVFromGithub?filename=${fileName}&path=${path}&repoOwnerAndName=${repoOwnerAndName}`,
		{
			timeout: 30000
		}
	);

	if (!urlResponse || urlResponse.status >= 400) {
		return null;
	}

	return addAsset(projectId, AssetType.Datasets, newDataset.id);
}

/**
 * This is a helper function which creates a new dataset and adds a given CSV file to it. The data set will
 * share the same name as the file and can optionally have a description
 * @param progress reference to display in ui
 * @param file the CSV file
 * @param userName uploader of this dataset
 * @param projectId the project ID
 * @param description description of the file. Optional. If not given description will be just the csv name
 */
async function createNewDatasetFromCSV(
	progress: Ref<number>,
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
		description: description || file.name,
		fileNames: [file.name],
		username: userName
	};

	const newDataset: Dataset | null = await createDataset(dataset);
	if (!newDataset || !newDataset.id) return null;

	const formData = new FormData();
	formData.append('file', file);

	const urlResponse = await API.put(`/datasets/${newDataset.id}/uploadCSV`, formData, {
		params: {
			filename: file.name
		},
		headers: {
			'Content-Type': 'multipart/form-data'
		},
		onUploadProgress(progressEvent) {
			progress.value = Math.min(
				90,
				Math.round((progressEvent.loaded * 100) / (progressEvent?.total ?? 100))
			);
		},
		timeout: 30000
	});

	if (!urlResponse || urlResponse.status >= 400) {
		return null;
	}

	await addAsset(projectId, AssetType.Datasets, newDataset.id);

	// Now verify it all works and obtain a preview for the user.
	return downloadRawFile(newDataset.id, file.name);
}

async function createDatasetFromSimulationResult(
	projectId: string,
	simulationId: string,
	datasetName: string | null
): Promise<boolean> {
	try {
		const response: AxiosResponse<Response> = await API.get(
			`/simulations/${simulationId}/add-result-as-dataset-to-project/${projectId}?datasetName=${datasetName}`
		);
		if (response && response.status === 201) {
			return true;
		}
		logger.error(`Unable to create dataset from simulation result ${response.status}`, {
			toastTitle: 'TDS - Simulation'
		});
		return false;
	} catch (error) {
		logger.error(
			`/simulations/{id}/add-result-as-dataset-to-project/{projectId} not responding:  ${error}`,
			{
				toastTitle: 'TDS - Simulation'
			}
		);
		return false;
	}
}

export const saveDataset = async (
	projectId: string,
	simulationId: string | undefined,
	datasetName: string | null
) => {
	if (!simulationId) return;
	if (await createDatasetFromSimulationResult(projectId, simulationId, datasetName)) {
		useResourcesStore().setActiveProject(await ProjectService.get(projectId, true));
	}
};

export {
	getAll,
	getDataset,
	updateDataset,
	getBulkDatasets,
	downloadRawFile,
	createNewDatasetFromCSV,
	createNewDatasetFromGithubFile,
	createDatasetFromSimulationResult
};
