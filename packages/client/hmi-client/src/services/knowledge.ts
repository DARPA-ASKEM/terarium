import API, { Poller, PollerState, PollResponse, PollerResult } from '@/api/api';
import { AxiosError, AxiosResponse } from 'axios';
import { Artifact, ExtractionResponse, Model } from '@/types/Types';
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
				return pollerResult;
			}

			// Queued
			return pollerResult;
		})
		.setThreshold(30);
	return poller.start();
}

/**
 * Transform a list of LaTeX strings to an AMR
 * @param latex string[] - list of LaTeX strings representing a model
 * @param modelId string - the model id to use for the extraction
 * @param framework [string] - the framework to use for the extraction, default to 'petrinet'
 * @return {Promise<Model | null>}
 */
const latexToAMR = async (
	latex: string[],
	modelId?: string,
	framework = 'petrinet'
): Promise<Model | null> => {
	try {
		const response: AxiosResponse<ExtractionResponse> = await API.post(
			`/knowledge/latex-to-amr/${framework}?modelId=${modelId}`,
			latex
		);
		if (response && response?.status === 200) {
			const { id, status } = response.data;
			if (status === 'queued') {
				const result = await fetchExtraction(id);
				if (result?.state === PollerState.Done) {
					if (result?.data?.job_result?.status_code === 200) {
						return result.data.job_result.amr as Model;
					}
				}
			}
			if (status === 'finished' && response.data.result.job_result.status_code === 200) {
				return response.data.result.job_result.amr as Model;
			}
		}
		logger.error(`LaTeX to AMR request failed`, { toastTitle: 'Error - Knowledge Middleware' });
	} catch (error: unknown) {
		logger.error(error, { showToast: false, toastTitle: 'Error - Knowledge Middleware' });
	}
	return null;
};

/**
 * Transform a MathML list of strings to an AMR
 * @param mathml string[] - list of MathML strings representing a model
 * @param framework [string] - the framework to use for the extraction, default to 'petrinet'
 * @return {Promise<Model | null>}
 */
const mathmlToAMR = async (mathml: string[], framework = 'petrinet'): Promise<Model | null> => {
	try {
		const response = await API.post(`/knowledge/mathml-to-amr?framework=${framework}`, mathml);
		if (response && response?.status === 200) {
			const { id, status } = response.data;
			if (status === 'queued') {
				const result = await fetchExtraction(id);
				if (result?.state === PollerState.Done) {
					return result.data as Model;
				}
			}
			if (status === 'finished') {
				return response.data.result as Model;
			}
		}
		logger.error(`MathML to AMR request failed`, { toastTitle: 'Error - ta1-service' });
	} catch (error: unknown) {
		if ((error as AxiosError).isAxiosError) {
			const axiosError = error as AxiosError;
			logger.error('[ta1-service]', axiosError.response?.data || axiosError.message, {
				showToast: false,
				toastTitle: 'Error - ta1-service'
			});
		} else {
			logger.error(error, { showToast: false, toastTitle: 'Error - ta1-service' });
		}
	}
	return null;
};

/**
 * Given a dataset, enrich its metadata
 * Returns a runId used to poll for result
 */
export const profileDataset = async (datasetId: string, artifactId: string | null = null) => {
	let response: any = null;
	if (artifactId) {
		response = await API.post(`/knowledge/profile-dataset/${datasetId}?artifact_id=${artifactId}`);
	} else {
		response = await API.post(`/knowledge/profile-dataset/${datasetId}`);
	}
	console.log('data profile response', response.data);
	return response.data.id;
};

const extractTextFromPDFArtifact = async (artifactId: string): Promise<string | null> => {
	try {
		const response = await API.post(`/knowledge/pdf-to-text?artifact_id=${artifactId}`);
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

const pdfExtractions = async (
	artifactId: string,
	pdfName?: string,
	description?: string
): Promise<string | null> => {
	// I've purposefully excluded the MIT and SKEMA options here, so they're always
	// defaulted to true.

	let url = `/knowledge/pdf-extractions?artifact_id=${artifactId}`;
	if (pdfName) {
		url += `&pdf_name=${pdfName}`;
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

export const extractPDF = async (artifact: Artifact) => {
	if (artifact.id) {
		const resp: string | null = await extractTextFromPDFArtifact(artifact.id);
		if (resp) {
			const pollResult = await fetchExtraction(resp);
			if (pollResult?.state === PollerState.Done) {
				await pdfExtractions(artifact.id); // we don't care now. fire and forget.
			}
		}
	}
};

export { mathmlToAMR, latexToAMR };
