import API, { Poller } from '@/api/api';
import { AxiosError } from 'axios';
import { Model } from '@/types/Types';
import { logger } from '@/utils/logger';

/**
 * Fetch information from the extraction service via the Poller utility
 * @param id
 * @return {Promise<T | void>}
 */
async function fetchExtraction(id: string): Promise<T | void> {
	const poller = new Poller<object>()
		.setInterval(2000)
		.setThreshold(90)
		.setPollAction(async (): Promise<T | null> => {
			const response = await API.get(`/extract/status/${id}`);
			if (response?.status === 200 && response?.data?.status === 'finished') {
				return response.data.result as T;
			}
			return null;
		});
	return poller.start();
}

/**
 * Transform a MathML list of strings to an AMR
 */
const mathmlToAMR = async (mathml: string[], framework = 'petrinet'): Promise<Model | null> => {
	try {
		const response = await API.post(`/extract/mathml-to-amr?framework=${framework}`, mathml);
		if (response && response?.status === 200) {
			const { id, status } = response.data;
			if (status === 'queue') {
				return (await fetchExtraction(id)) as Model | null;
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
