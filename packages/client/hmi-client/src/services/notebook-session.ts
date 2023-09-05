import API from '@/api/api';
import { NotebookSession } from '@/types/Types';

export const getAllNotebookSessions = async () => {
	const response = await API.get(`/notebook_sessions`);
	return (response?.data as NotebookSession[]) ?? null;
};

export const getNotebookSessionById = async (notebook_id: string) => {
	const response = await API.get(`/notebook_sessions/${notebook_id}`);
	return (response?.data as NotebookSession) ?? null;
};

export const createNotebookSession = async (notebookSession: NotebookSession) => {
	const response = await API.post(`/notebook_sessions`, {
		id: notebookSession.id,
		name: notebookSession.name,
		description: notebookSession.description,
		data: notebookSession.data,
		timestamp: notebookSession.timestamp
	});
	return response?.data ?? null;
};

export const updateNotebookSession = async (notebookSession: NotebookSession) => {
	const response = await API.put(`/notebook_sessions/${notebookSession.id}`, {
		id: notebookSession.id,
		name: notebookSession.name,
		description: notebookSession.description,
		data: notebookSession.data,
		timestamp: notebookSession.timestamp
	});

	return response?.data ?? null;
};

export const deleteNotebookSession = async (notebook_id: string) => {
	const response = await API.delete(`/notebook_sessions/${notebook_id}`);
	return response?.data ?? null;
};
