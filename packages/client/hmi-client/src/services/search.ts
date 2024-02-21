import API from '@/api/api';
import { AssetType } from '@/types/Types';

export async function searchAsset(text: string, assetType: AssetType) {
	const response = await API.get(`/search-by-asset-type/${assetType}?text=${text}`);
	return response?.data ?? [];
}
