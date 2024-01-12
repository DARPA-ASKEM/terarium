import { AssetItem } from '@/types/common';
import { useProjects } from '@/composables/project';
import { isProjectAssetTypes } from '@/types/Project';

type ProjectAssetItems = Map<string, Set<AssetItem>>;

export const generateProjectAssetsMap = (searchAsset: string): ProjectAssetItems => {
	const assetItemsMap = new Map<string, Set<AssetItem>>();

	const projectAssets = useProjects().activeProject?.value?.projectAssets;
	if (!projectAssets) return assetItemsMap;

	// Run through all the assets type within the project
	const cleanAssets = projectAssets
		.filter((asset) => isProjectAssetTypes(asset.assetType))
		.map(
			(asset) =>
				({
					assetId: asset.id.toString(),
					assetName: asset.assetName ?? asset.assetId,
					pageType: asset.assetType
				}) as AssetItem
		)
		.filter((asset) => {
			if (!searchAsset.trim()) return true;
			if (!asset.assetName) return false;
			const searchTermLower = searchAsset.trim().toLowerCase();
			return asset.assetName.toLowerCase().includes(searchTermLower);
		});

	Object.entries(Object.groupBy(cleanAssets, (asset) => asset.pageType)).forEach(
		([type, assetList]) => assetItemsMap.set(type, new Set(assetList))
	);

	console.log(assetItemsMap);
	return assetItemsMap;
};
