import API from '@/api/api';

export async function getEsgf(id: string) {
	const response = await API.get(`/climatedata/queries/fetch-esgf/${id}`);
	return response?.data ?? null;
}

export async function previewEsgf(id: string) {
	const response = await API.get(`/climatedata/queries/preview-esgf/${id}`);
	return response?.data ?? null;
}

export async function searchEsgf(query: string) {
	const response = await API.get(
		`/climatedata/queries/search-esgf?query=${encodeURIComponent(query)}`
	);
	return response?.data ?? null;
}

export async function getSubsetStatus(id: string) {
	const response = await API.get(`/climatedata/queries/status/${id}`);
	return response?.data ?? null;
}

export async function getEsgfSubset(id: string) {
	const response = await API.get(`/climatedata/queries/subset-esgf/${id}`);
	return response?.data ?? null;
}
