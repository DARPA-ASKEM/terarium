import API from '@/api/api';
import { AxiosError } from 'axios';
import { Model } from '@/types/Types';
import { logger } from '@/utils/logger';

// Transform a MathML list of strings to an AMR
const mathmlToAMR = async (model_id: Model['id'], mathml: string[]): Promise<Model | null> => {
	try {
		const response = await API.post(`/extract/mathml-to-amr?model=${model_id}`, mathml);
		if (response && response?.status === 200) {
			return (response?.data as Model) ?? null;
		}
		logger.error(
			`MathML to AMR: Extraction-service Server did not provide a correct response [HTTP ${response?.status}]`,
			{
				showToast: false
			}
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
