import API from '@/api/api';
import { subscribe, unsubscribe } from '@/services/ClientEventService';
import type { ClientEvent, Code, Dataset, ExtractionStatusUpdate, Model } from '@/types/Types';
import { ClientEventType } from '@/types/Types';
import { logger } from '@/utils/logger';
import { AxiosError, AxiosResponse } from 'axios';

/**
 * Transform a list of LaTeX or mathml strings to an AMR
 * @param format string - the type of equations used to define an AMR ('mathml' or 'latex')
 * @param equations string[] - list of LaTeX or mathml strings representing a model
 * @param framework string= - the framework to use for the extraction, default to 'petrinet'
 * @param modelId string= - the model id to use for the extraction
 * @return {Promise<any>}
 */
export const equationsToAMR = async (
	equations: string[],
	framework: string = 'petrinet',
	modelId?: string
): Promise<string | null> => {
	try {
		const response: AxiosResponse<string> = await API.post(`/knowledge/equations-to-model`, {
			model: framework,
			modelId,
			equations
		});
		return response.data;
	} catch (error: unknown) {
		logger.error(error, { showToast: false });
	}
	return null;
};

/**
 * Given a model, enrich its metadata
 * Returns a runId used to poll for result
 */
export const profileModel = async (modelId: Model['id'], documentId: string | null = null) => {
	let response: any = null;
	if (documentId && modelId) {
		response = await API.post(`/knowledge/profile-model/${modelId}?document-id=${documentId}`);
	} else {
		response = await API.post(`/knowledge/profile-model/${modelId}`);
	}
	console.log('model profile response', response.data);
	return response.data.id;
};

export const alignModel = async (
	modelId: Model['id'],
	documentId: string
): Promise<Model | null> => {
	const response = await API.post(
		`/knowledge/link-amr?document-id=${documentId}&model-id=${modelId}`
	);
	return response.data ?? null;
};
/**
 * Given a dataset, enrich its metadata
 * Returns a runId used to poll for result
 */
export const profileDataset = async (
	datasetId: Dataset['id'],
	documentId: string | null = null
) => {
	let response: any = null;
	if (documentId && datasetId) {
		response = await API.post(
			`/knowledge/profile-dataset/${datasetId}?document-id=${documentId}`
		);
	} else {
		response = await API.post(`/knowledge/profile-dataset/${datasetId}`);
	}
	console.log('data profile response', response.data);
	return response.data.id;
};

const messageHandler = async (event: ClientEvent<ExtractionStatusUpdate>) => {
	if (event.data.error) {
		logger.error(
			`Extraction client-event: ERROR [${event.data.step}/${event.data.totalSteps}]: ${event.data.error}`
		);
		await unsubscribe(ClientEventType.Extraction, messageHandler);
		return;
	}
	if (event.data.message) {
		console.debug(
			`Extraction client-event: [${event.data.step}/${event.data.totalSteps}]: ${event.data.message}`
		);
	} else {
		logger.success(`Extraction client-event: [${event.data.step}/${event.data.totalSteps}]`);
	}
	if (event.data.step === event.data.totalSteps) {
		await unsubscribe(ClientEventType.Extraction, messageHandler);
	}
};

export const extractPDF = async (documentId: string) => {
	if (documentId) {
		try {
			const response = await API.post(`/knowledge/pdf-to-cosmos?document-id=${documentId}`);
			if (response?.status === 202) {
				await subscribe(ClientEventType.Extraction, messageHandler);
				return;
			}
			logger.error('pdf text extraction request failed', {
				showToast: false,
				toastTitle: 'Error - pdfExtractions'
			});
		} catch (error: unknown) {
			if ((error as AxiosError).isAxiosError) {
				const axiosError = error as AxiosError;
				logger.error('[pdfExtractions]', axiosError.response?.data || axiosError.message, {
					showToast: false,
					toastTitle: 'Error - pdf text extraction'
				});
			} else {
				logger.error(error, {
					showToast: false,
					toastTitle: 'Error - pdf text extraction'
				});
			}
		}
	}
};

export async function codeToAMR(
	codeId: string,
	name: string = '',
	description: string = '',
	dynamicsOnly: boolean = false,
	llmAssisted: boolean = false
): Promise<Model | null> {
	const response = await API.post(
		`/knowledge/code-to-amr?code_id=${codeId}&name=${name}&description=${description}&dynamics_only=${dynamicsOnly}&llm_assisted=${llmAssisted}`
	);
	if (response?.status === 200) {
		return response.data;
	}
	logger.error(`Code to AMR request failed`, { toastTitle: 'Error - knowledge-middleware' });
	return null;
}

export async function codeBlocksToAmr(code: Code, file: File): Promise<Model | null> {
	const formData = new FormData();
	const blob = new Blob([JSON.stringify(code)], {
		type: 'application/json'
	});
	formData.append('code', blob);
	formData.append('file', file);
	const response = await API.post(`/knowledge/code-blocks-to-model`, formData, {
		headers: {
			Accept: 'application/json',
			'Content-Type': 'multipart/form-data'
		}
	});
	if (response?.status === 200) {
		return response.data;
	}
	logger.error(`Code to AMR request failed`, { toastTitle: 'Error - knowledge-middleware' });
	return null;
}
