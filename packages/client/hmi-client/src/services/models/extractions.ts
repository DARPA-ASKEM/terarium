import API, { Poller, PollerState } from '@/api/api';
import { AxiosError } from 'axios';
import { Model } from '@/types/Types';
import { logger } from '@/utils/logger';

/**
 * Fetch information from the extraction service via the Poller utility
 * @param id
 * @return {Promise<PollerResult>}
 */
async function fetchExtraction(id: string) {
	const poller = new Poller<object>().setPollAction(async (): Promise<T | null> => {
		const response = await API.get(`/extract/status/${id}`);

		// Finished
		if (response?.status === 200 && response?.data?.status === 'finished') {
			return {
				data: response.data.result as T,
				progress: null,
				error: null
			};
		}

		// Failed
		if (response?.status === 200 && response?.data?.status === 'failed') {
			return {
				data: null,
				progress: null,
				error: true
			};
		}

		// Queued
		return {
			data: null,
			progress: null,
			error: null
		};
	});
	return poller.start();
}

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
				const result = (await fetchExtraction(id)) as PollResponse<Model | null>;
				if (result?.state === PollerState.Done) {
					return result.data as Model;
				}
			}
			if (status === 'finished') {
				return response.data.result as Model;
			}
		}
		logger.error(
			`MathML to AMR: Extraction-service Server did not provide a correct response [HTTP ${response?.status}]`,
			{ showToast: false }
		);
	} catch (error: unknown) {
		if ((error as AxiosError).isAxiosError) {
			const axiosError = error as AxiosError;
			logger.error(
				'MathML to AMR Error (skema-rs): ',
				axiosError.response?.data || axiosError.message,
				{
					showToast: false
				}
			);
		} else {
			logger.error(error, { showToast: false });
		}
	}
	return null;
};

export { mathmlToAMR };
