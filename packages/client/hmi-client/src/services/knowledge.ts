import API, { Poller, PollerState, PollResponse, PollerResult } from '@/api/api';
import { AxiosError, AxiosResponse } from 'axios';
import { ExtractionResponse } from '@/types/Types';
import { logger } from '@/utils/logger';

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
 * Transform a list of LaTeX strings to an AMR
 * @param latex string[] - list of LaTeX strings representing a model
 * @param framework string= - the framework to use for the extraction, default to 'petrinet'
 * @param modelId string= - the model id to use for the extraction
 * @return {Promise<Boolean>}
 */
export const latexToAMR = async (
	equations: string[],
	framework: string = 'petrinet',
	modelId?: string
): Promise<Boolean> => {
	try {
		const response: AxiosResponse<ExtractionResponse> = await API.post(
			`/knowledge/equations-to-model`,
			{ format: 'latex', framework, modelId, equations }
		);
		if (response && response?.status === 200) {
			const { id, status } = response.data;
			if (status === 'queued') {
				const result = await fetchExtraction(id);
				if (result?.state === PollerState.Done && result?.data?.job_result?.status_code === 200) {
					return true;
				}
			}
			if (status === 'finished' && response.data.result.job_result?.status_code === 200) {
				return true;
			}
		}
		logger.error(`LaTeX to AMR request failed`, { toastTitle: 'Error - Knowledge Middleware' });
	} catch (error: unknown) {
		logger.error(error, { showToast: false });
	}
	return false;
};

/**
 * Given a model, enrich its metadata
 * Returns a runId used to poll for result
 */
export const profileModel = async (modelId: string, documentId: string | null = null) => {
	let response: any = null;
	if (documentId) {
		response = await API.post(`/knowledge/profile-model/${modelId}?document_id=${documentId}`);
	} else {
		response = await API.post(`/knowledge/profile-model/${modelId}`);
	}
	console.log('model profile response', response.data);
	return response.data.id;
};

export const alignModel = async (modelId: string, documentId: string): Promise<string | null> => {
	const response = await API.post(
		`/knowledge/link-amr?document_id=${documentId}&model_id=${modelId}`
	);
	return response.data?.id ?? null;
};
/**
 * Given a dataset, enrich its metadata
 * Returns a runId used to poll for result
 */
export const profileDataset = async (datasetId: string, documentId: string | null = null) => {
	let response: any = null;
	if (documentId) {
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
	dynamicsOnly: boolean = false
) {
	const response = await API.post(
		`/knowledge/code-to-amr?code_id=${codeId}&name=${name}&description=${description}&dynamics_only=${dynamicsOnly}`
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
