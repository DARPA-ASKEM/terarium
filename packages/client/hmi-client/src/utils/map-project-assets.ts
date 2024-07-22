import _, { isEmpty } from 'lodash';
import { AssetItem } from '@/types/common';
import { useProjects } from '@/composables/project';
import { isVisibleProjectAssetTypes, listOfVisibleAssetTypes } from '@/types/Project';
import { formatShort, sortDatesAsc } from '@/utils/date';

// Map of asset types to their respective asset items
type ProjectAssetItems = Map<string, Array<AssetItem>>;

/**
 * Generate a map of all the visible asset types and their respective asset items
 * @param searchAsset - the search term to filter the assets
 */
export const generateProjectAssetsMap = (searchAsset: string): ProjectAssetItems => {
	// Create a map of all the visible asset types
	const assetItemsMap: ProjectAssetItems = new Map<string, Array<AssetItem>>();
	listOfVisibleAssetTypes.forEach((type) => assetItemsMap.set(type, new Array<AssetItem>()));

	// Check if the project has any assets
	const projectAssets = useProjects().activeProject?.value?.projectAssets;
	if (!projectAssets) return assetItemsMap;

	// Run through all the assets type within the project
	const cleanAssets = projectAssets
		.filter((asset) => isVisibleProjectAssetTypes(asset.assetType))
		.map(
			(asset) =>
				({
					assetId: asset.assetId.toString(),
					assetName:
						asset.assetName ??
						`${asset.assetId.substring(0, 3)} - ${formatShort(asset?.updatedOn ?? asset?.createdOn)}`,
					assetCreatedOn: asset.createdOn,
					pageType: asset.assetType
				}) as AssetItem
		)
		.filter((asset) => {
			if (!searchAsset.trim()) return true;
			if (!asset.assetName) return false;
			const searchTermLower = searchAsset.trim().toLowerCase();
			return asset.assetName.toLowerCase().includes(searchTermLower);
		});

	// Assign the assets to the map
	Object.entries(_.groupBy(cleanAssets, 'pageType')).forEach(([type, assetList]) =>
		assetItemsMap.set(
			type,
			[...new Set(assetList as AssetItem[])].sort((a, b) => sortDatesAsc(a.assetCreatedOn, b.assetCreatedOn))
		)
	);

	return assetItemsMap;
};

// Function to filter out keys with non-null sets
export function getNonNullSetOfVisibleItems(map: ProjectAssetItems): number[] {
	const nonNullSet: number[] = [];
	map.forEach((value, key) => {
		if (!isEmpty(value)) {
			nonNullSet.push(listOfVisibleAssetTypes.findIndex((type) => type === key));
		}
	});
	return nonNullSet;
}
