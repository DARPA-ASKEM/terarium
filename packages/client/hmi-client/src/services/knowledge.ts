import API from '@/api/api';
import { AxiosResponse } from 'axios';
import { extractionStatusUpdateHandler, subscribe } from '@/services/ClientEventService';
import { type Dataset, type DocumentAsset, type Model, ClientEventType } from '@/types/Types';
import { logger } from '@/utils/logger';

/**
 * Define the request type
 * @param equations string[] - list of LaTeX or mathml strings representing a model
 * @param framework string= - the framework to use for the extraction, default to 'petrinet'
 * @param modelId string= - the model id to use for the extraction
 * @param documentId string= - the document id source of the equations
 */
export interface EquationsToAMRRequest {
	equations: string[];
	framework?: string;
	modelId?: Model['id'];
	documentId?: DocumentAsset['id'];
}

/**
 * Transform a list of LaTeX or mathml strings to an AMR
 * @param request EquationsToAMRRequest
 * @return {Promise<any>}
 */
export const equationsToAMR = async (request: EquationsToAMRRequest): Promise<string | null> => {
	const { equations, framework: model = 'petrinet', modelId, documentId } = request;
	try {
		const response: AxiosResponse<string> = await API.post(`/knowledge/equations-to-model`, {
			model,
			modelId,
			documentId,
			equations
		});
		return response.data;
	} catch (error: unknown) {
		logger.error(error, { showToast: false });
	}
	return null;
};

/**
 * Given a dataset, enrich its metadata
 * Returns a runId used to poll for result
 */
export const profileDataset = async (datasetId: Dataset['id'], documentId: DocumentAsset['id'] = '') => {
	let response: any;
	if (documentId) {
		response = await API.post(`/knowledge/profile-dataset/${datasetId}?document-id=${documentId}`);
	} else {
		response = await API.post(`/knowledge/profile-dataset/${datasetId}`);
	}
	return response.data.id;
};

/** Extract text and artifacts from a PDF document */
export const extractPDF = async (documentId: DocumentAsset['id']) => {
	if (documentId) {
		const response = await API.post(`/knowledge/pdf-extractions?document-id=${documentId}`);
		if (response?.status === 202) {
			await subscribe(ClientEventType.Extraction, extractionStatusUpdateHandler);
		}
	}
};

/** Extract variables from a text document */
export const extractVariables = async (documentId: DocumentAsset['id'], modelIds: Array<Model['id']>) => {
	if (documentId) {
		const url = `/knowledge/variable-extractions?document-id=${documentId}&model-ids=${modelIds}`;
		const response = await API.post(url);
		if (response?.status === 202) {
			await subscribe(ClientEventType.Extraction, extractionStatusUpdateHandler);
		}
	}
};
