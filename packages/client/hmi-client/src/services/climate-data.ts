import API from '@/api/api';

export async function getSubsetStatus(id: string) {
	const response = await API.get(`/climatedata/queries/status/${id}`);
	return response?.data ?? null;
}

export async function getEsgfSubset(id: string) {
	const response = await API.get(`/climatedata/queries/subset-esgf/${id}`);
	return response?.data ?? null;
}
