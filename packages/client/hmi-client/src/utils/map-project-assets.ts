import { AssetItem } from '@/types/common';
import { AssetType } from '@/types/Types';
import { useProjects } from '@/composables/project';
import { isEmpty } from 'lodash';
import { isProjectAssetTypes } from '@/types/Project';

type IProjectAssetItems = Map<AssetType, Set<AssetItem>>;

export const generateProjectAssetsMap = (searchAsset: string): IProjectAssetItems => {
	const assetItemsMap = new Map<AssetType, Set<AssetItem>>();

	const projectAssets = useProjects().activeProject?.value?.assets;
	if (!projectAssets) return assetItemsMap;

	// Run through all the assets type within the project
	Object.keys(projectAssets).forEach((type) => {
		if (isProjectAssetTypes(type) && !isEmpty(projectAssets[type])) {
			const projectAssetType = type;
			const typeAssets = projectAssets[projectAssetType]
				.map((asset) => ({
					assetName: (
						asset?.name ||
						asset?.header?.name || // FIXME should unify upstream via a summary endpoint
						asset?.title ||
						asset?.id
					)?.toString(),
					pageType: asset?.type ?? projectAssetType,
					assetId: asset?.id?.toString() ?? ''
				}))
				.filter((asset) => {
					// filter assets
					if (!searchAsset.trim()) {
						return true;
					}
					const searchTermLower = searchAsset.trim().toLowerCase();
					return asset.assetName.toLowerCase().includes(searchTermLower);
				}) as AssetItem[];
			if (!isEmpty(typeAssets)) {
				assetItemsMap.set(projectAssetType, new Set(typeAssets));
			}
		}
	});
	return assetItemsMap;
};
