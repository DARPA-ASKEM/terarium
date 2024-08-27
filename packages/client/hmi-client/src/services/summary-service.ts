import API, { getProjectIdFromUrl } from '@/api/api';
import type { Summary } from '@/types/Types';
import { activeProjectId } from '@/composables/activeProject';

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
	const projectId = activeProjectId.value || getProjectIdFromUrl();
	const response = await API.post(`/summary?project-id=${projectId}`, summary);
	return response.data;
};

export const updateSummary = async (summary: Summary) => {
	const response = await API.put(`/summary/${summary.id}`, summary);
	return response.data;
};

export const getSummaries = async (ids: string[]): Promise<{ [key: string]: Summary }> => {
	const projectId = activeProjectId.value || getProjectIdFromUrl();
	const response = await API.post(`/summary/search?project-id=${projectId}`, ids);
	return response.data;
};
