import API from '@/api/api';
import { logger } from '@/utils/logger';
import { AssetType } from '@/types/Types';

export async function searchAsset(text: string, assetType: AssetType) {
	try {
		const response = await API.post(`/search-by-asset-type/${assetType}`, { text });
		return response?.data ?? [];
	} catch (error) {
		logger.error(error);
		return [];
	}
}
