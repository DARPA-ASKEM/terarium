import API from '@/api/api';
import type { NotebookSession } from '@/types/Types';

export const getAllNotebookSessions = async () => {
	const response = await API.get(`/sessions`);
	return (response?.data as NotebookSession[]) ?? null;
};

export const getNotebookSessionById = async (notebook_id: string, projectId: string) => {
	const response = await API.get(`/sessions/${notebook_id}?project-id=${projectId}`);
	return (response?.data as NotebookSession) ?? null;
};

export const createNotebookSession = async (
	notebookSession: NotebookSession,
	projectId: string
) => {
	const response = await API.post(`/sessions`, {
		notebookSession,
		projectId
	});
	return response?.data ?? null;
};

export const updateNotebookSession = async (
	notebookSession: NotebookSession,
	projectId: string
) => {
	const response = await API.put(`/sessions/${notebookSession.id}`, {
		notebookSession,
		projectId
	});

	return response?.data ?? null;
};

export const deleteNotebookSession = async (notebook_id: string, projectId: string) => {
	const response = await API.delete(`/sessions/${notebook_id}?project-id=${projectId}`);
	return response?.data ?? null;
};

export const cloneNoteBookSession = async (notebookId: string, projectId: string) => {
	const response = await API.post(`/sessions/${notebookId}/clone?project-id=${projectId}`);
	return response?.data ?? null;
};
