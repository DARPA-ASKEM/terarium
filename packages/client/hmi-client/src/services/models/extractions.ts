import API, { Poller, PollerState, PollResponse } from '@/api/api';
import { AxiosError } from 'axios';
import { Model } from '@/types/Types';
import { logger } from '@/utils/logger';

/**
 * Fetch information from the extraction service via the Poller utility
 * @param id
 * @return {Promise<PollerResult>}
 */
async function fetchExtraction(id: string) {
	const pollerResult: PollResponse<any> = { data: null, progress: null, error: null };
	const poller = new Poller<object>().setPollAction(async () => {
		const response = await API.get(`/extract/status/${id}`);

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
	});
	return poller.start();
}

/**
 * Transform a list of LaTeX strings to an AMR
 * @param latex string[] - list of LaTeX strings representing a model
 * @param framework [string] - the framework to use for the extraction, default to 'petrinet'
 * @return {Promise<Model | null>}
 */
const latexToAMR = async (latex: string[], framework = 'petrinet'): Promise<Model | null> => {
	try {
		const response = await API.post(`/extract/latex-to-amr/${framework}`, latex);
		if (response && response?.status === 200) {
			const model = response.data as Model;
			if (model) return model;
			logger.error(`LaTeX to AMR request failed`, { toastTitle: 'Error - SKEMA Unified' });
			return null;
		}
	} catch (error: unknown) {
		logger.error(error, { showToast: false, toastTitle: 'Error - SKEMA Unified' });
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
		const response = await API.post(`/extract/mathml-to-amr?framework=${framework}`, mathml);
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
		logger.error(`MathML to AMR request failed`, { toastTitle: 'Error - extraction-service' });
	} catch (error: unknown) {
		if ((error as AxiosError).isAxiosError) {
			const axiosError = error as AxiosError;
			logger.error('[extraction-service]', axiosError.response?.data || axiosError.message, {
				showToast: false,
				toastTitle: 'Error - extraction-service'
			});
		} else {
			logger.error(error, { showToast: false, toastTitle: 'Error - extraction-service' });
		}
	}
	return null;
};

export { mathmlToAMR, latexToAMR };
