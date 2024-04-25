import API from '@/api/api';
import type { NotebookSession } from '@/types/Types';

async function getAll() {
	const response = await API.get(`/sessions`);
	return (response?.data as NotebookSession[]) ?? null;
}

async function get(id: NotebookSession['id']) {
	const response = await API.get(`/sessions/${id}`);
	return (response?.data as NotebookSession) ?? null;
}

async function create(notebookSession: NotebookSession) {
	const response = await API.post(`/sessions`, notebookSession);
	return response?.data ?? null;
}

async function update(notebookSession: NotebookSession) {
	const response = await API.put(`/sessions/${notebookSession.id}`, notebookSession);
	return response?.data ?? null;
}

async function deleteAsset(id: NotebookSession['id']) {
	const response = await API.delete(`/sessions/${id}`);
	return response?.data ?? null;
}

async function clone(id: NotebookSession['id']) {
	const response = await API.post(`/sessions/${id}/clone`);
	return response?.data ?? null;
}

export default {
	clone,
	create,
	delete: deleteAsset,
	get,
	getAll,
	update
};
