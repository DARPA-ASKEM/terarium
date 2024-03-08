import API, { Poller, PollerResult, PollerState, PollResponse } from '@/api/api';
import { AxiosError, AxiosResponse } from 'axios';
import type { Code, Dataset, ExtractionResponse, Model } from '@/types/Types';
import { logger } from '@/utils/logger';
import { modelCard } from './goLLM';

/**
 * Fetch information from the extraction service via the Poller utility
 * @param id
 * @return {Promise<PollerResult>}
 */
export async function fetchExtraction(id: string): Promise<PollerResult<any>> {
	const pollerResult: PollResponse<any> = { data: null, progress: null, error: null };
	const poller = new Poller<object>()
		.setPollAction(async () => {
			const response = await API.get(`/knowledge/status/${id}`);

			// Finished
			if (response?.status === 200 && response?.data?.status === 'finished') {
				pollerResult.data = response.data.result;
				return pollerResult;
			}

			// Failed
			if (response?.status === 200 && response?.data?.status === 'failed') {
				pollerResult.error = true;
				logger.error(`Extraction failed, Service not responding`, {
					toastTitle: 'Error - knowledge-middleware'
				});
				return pollerResult;
			}

			// Queued
			return pollerResult;
		})
		.setThreshold(3000);
	return poller.start();
}

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
): Promise<any> => {
	try {
		const response: AxiosResponse<ExtractionResponse> = await API.post(
			`/knowledge/equations-to-model`,
			{ model: framework, modelId, equations }
		);
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
		response = await API.post(`/knowledge/profile-model/${modelId}?document_id=${documentId}`);
	} else {
		response = await API.post(`/knowledge/profile-model/${modelId}`);
	}
	console.log('model profile response', response.data);
	return response.data.id;
};

export const alignModel = async (
	modelId: Model['id'],
	documentId: string
): Promise<string | null> => {
	const response = await API.post(
		`/knowledge/link-amr?document_id=${documentId}&model_id=${modelId}`
	);
	return response.data?.id ?? null;
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
		response = await API.post(`/knowledge/profile-dataset/${datasetId}?document_id=${documentId}`);
	} else {
		response = await API.post(`/knowledge/profile-dataset/${datasetId}`);
	}
	console.log('data profile response', response.data);
	return response.data.id;
};

export const extractTextFromPDFDocument = async (documentId: string): Promise<string | null> => {
	try {
		const response = await API.post(`/knowledge/pdf-to-cosmos?document_id=${documentId}`);
		if (response?.status === 200 && response?.data?.id) return response.data.id;
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
			logger.error(error, { showToast: false, toastTitle: 'Error - pdf text extraction' });
		}
	}

	return null;
};

export enum Extractor {
	SKEMA = 'SKEMA',
	MIT = 'MIT'
}
export const pdfExtractions = async (
	documentId: string,
	extractor: Extractor,
	pdfName?: string,
	description?: string
): Promise<string | null> => {
	let url = `/knowledge/pdf-extractions?document_id=${documentId}`;

	if (extractor === Extractor.SKEMA) {
		url += '&annotate_skema=true&annotate_mit=false';
	} else if (extractor === Extractor.MIT) {
		url += '&annotate_skema=false&annotate_mit=true';
	}

	if (pdfName) {
		url += `&name=${pdfName}`;
	}
	if (description) {
		url += `&description=${description}`;
	}

	try {
		const response = await API.post(url);
		if (response?.status === 200 && response?.data?.id) return response.data.id;
		logger.error('pdf Extractions request failed', {
			showToast: false,
			toastTitle: 'Error - pdfExtractions'
		});
	} catch (error: unknown) {
		if ((error as AxiosError).isAxiosError) {
			const axiosError = error as AxiosError;
			logger.error('[pdfExtractions]', axiosError.response?.data || axiosError.message, {
				showToast: false,
				toastTitle: 'Error - pdfExtractions'
			});
		} else {
			logger.error(error, { showToast: false, toastTitle: 'Error - pdfExtractions' });
		}
	}

	return null;
};

export const extractPDF = async (documentId: string) => {
	if (documentId) {
		const resp: string | null = await extractTextFromPDFDocument(documentId);
		if (resp) {
			const pollResult = await fetchExtraction(resp);
			if (pollResult?.state === PollerState.Done) {
				modelCard(documentId);
				pdfExtractions(documentId, Extractor.SKEMA);
				pdfExtractions(documentId, Extractor.MIT);
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
) {
	const response = await API.post(
		`/knowledge/code-to-amr?code_id=${codeId}&name=${name}&description=${description}&dynamics_only=${dynamicsOnly}&llm_assisted=${llmAssisted}`
	);
	if (response?.status === 200) {
		const { id, status } = response.data;
		if (status === 'queued') {
			const extraction = await fetchExtraction(id);
			if (extraction?.state === PollerState.Done) {
				const data = extraction.data as any; // fix linting
				return data?.job_result.tds_model_id;
			}
		}
		if (status === 'finished') {
			return response.data.result?.job_result.tds_model.id;
		}
	}
	logger.error(`Code to AMR request failed`, { toastTitle: 'Error - knowledge-middleware' });
	return null;
}

export async function codeBlocksToAmr(code: Code, file: File): Promise<string | null> {
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
		const { id, status } = response.data;
		if (status === 'queued') {
			const extraction = await fetchExtraction(id);
			if (extraction?.state === PollerState.Done) {
				const data = extraction.data as any; // fix linting
				return data?.job_result.tds_model_id;
			}
		}
		if (status === 'finished') {
			return response.data.result?.job_result.tds_model.id;
		}
	}
	logger.error(`Code to AMR request failed`, { toastTitle: 'Error - knowledge-middleware' });
	return null;
}
