import API from '@/api/api';
import { logger } from '@/utils/logger';
import { AssetType, TerariumAssetThatSupportsAdditionalProperties } from '@/types/Types';

export async function searchByAssetType(
	text: string,
	assetType: AssetType
): Promise<TerariumAssetThatSupportsAdditionalProperties[]> {
	try {
		const response = await API.post(`/search-by-asset-type/${assetType}`, { text });
		return response?.data ?? [];
	} catch (error) {
		logger.error(error);
		return [];
	}
}
