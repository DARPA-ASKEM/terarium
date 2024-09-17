import API from '@/api/api';
import type { Summary } from '@/types/Types';

type SummarizationMode = 'ASYNC' | 'SYNC';
export const createLLMSummary = async (prompt: string, mode: SummarizationMode = 'ASYNC') => {
	const response = await API.post(`/gollm/generate-summary?mode=${mode}`, prompt, {
		headers: {
			'Content-Type': 'application/json'
		}
	});
	if (response.data) {
		return { id: response.data.additionalProperties.summaryId };
	}
	return null;
};

type NewSummary = Omit<Summary, 'id'>;
export const createSummary = async (summary: NewSummary) => {
	const response = await API.post('/summary', summary);
	return response.data;
};

export const updateSummary = async (summary: Summary) => {
	const response = await API.put(`/summary/${summary.id}`, summary);
	return response.data;
};

export const getSummaries = async (ids: string[]): Promise<{ [key: string]: Summary }> => {
	const response = await API.post('/summary/search', ids);
	return response.data;
};
