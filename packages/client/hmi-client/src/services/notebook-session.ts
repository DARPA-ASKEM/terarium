import API from '@/api/api';
import type { NotebookSession } from '@/types/Types';

export const getAllNotebookSessions = async () => {
	const response = await API.get(`/sessions`);
	return (response?.data as NotebookSession[]) ?? null;
};

export const getNotebookSessionById = async (notebook_id: string) => {
	const response = await API.get(`/sessions/${notebook_id}`);
	return (response?.data as NotebookSession) ?? null;
};

export const createNotebookSession = async (notebookSession: NotebookSession) => {
	const response = await API.post(`/sessions`, {
		id: notebookSession.id,
		description: notebookSession.description,
		data: notebookSession.data
	} as NotebookSession);
	return response?.data ?? null;
};

export const updateNotebookSession = async (notebookSession: NotebookSession) => {
	const response = await API.put(`/sessions/${notebookSession.id}`, {
		id: notebookSession.id,
		description: notebookSession.description,
		data: notebookSession.data
	});

	return response?.data ?? null;
};

export const deleteNotebookSession = async (notebook_id: string) => {
	const response = await API.delete(`/sessions/${notebook_id}`);
	return response?.data ?? null;
};

export const cloneNoteBookSession = async (notebookId: string) => {
	const response = await API.post(`/sessions/${notebookId}/clone`);
	return response?.data ?? null;
};
