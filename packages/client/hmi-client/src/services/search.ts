import API from '@/api/api';
import { AssetType } from '@/types/Types';

export async function search(text: string, assetType: AssetType) {
	const response = await API.get(`/search-by-assset-type/${assetType}?text=${text}`);
	return response?.data ?? null;
}
