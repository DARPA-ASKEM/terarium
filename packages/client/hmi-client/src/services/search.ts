import API from '@/api/api';
import { AssetType, TerariumAssetThatSupportsAdditionalProperties } from '@/types/Types';
import { logger } from '@/utils/logger';

export async function searchByAssetType(
	text: string,
	assetType: AssetType
): Promise<TerariumAssetThatSupportsAdditionalProperties[]> {
	try {
		const response = await API.get(`/search-by-asset-type/${assetType}?text=${encodeURIComponent(text)}`);
		return response?.data ?? [];
	} catch (error) {
		logger.error(`Unable to fetch results from the question: ${text}`);
		return [];
	}
}
