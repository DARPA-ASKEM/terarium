import API from '@/api/api';
import { AxiosError } from 'axios';
import { Model } from '@/types/Types';
import { logger } from '@/utils/logger';

// Transform a MathML list of strings to an AMR
const mathmlToAMR = async (mathml: string[]): Promise<Model | null> => {
	try {
		const { status, data } = await API.post('/transforms/mathml-to-amr', mathml);
		if (status && status === 200) {
			return (data as Model) ?? null;
		}
		logger.error('MathML to AMR Error (skema-rs): Server did not provide a correct response', {
			showToast: false
		});
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
