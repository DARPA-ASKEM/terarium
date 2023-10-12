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
			const projectAssetType = type as AssetType;
			const typeAssets = projectAssets[projectAssetType]
				.map((asset) => {
					let assetName = (asset?.name || asset?.title || asset?.id)?.toString();
					if (asset.header?.name) assetName = asset.header.name; // FIXME should unify upstream via a summary endpoint

					const pageType = asset?.type ?? projectAssetType;
					const assetId = asset?.id?.toString() ?? '';
					return { assetName, pageType, assetId };
				})
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
