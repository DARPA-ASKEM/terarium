import type { InjectionKey } from 'vue';
import { AssetType } from './types/Types';

export const addAssetKey: InjectionKey<
	(projectId: string, assetType: string, assetId: string) => Promise<any>
> = Symbol('addAsset');

export const deleteAssetKey: InjectionKey<
	(projectId: string, assetType: AssetType, assetId: string) => Promise<any>
> = Symbol('deleteAsset');
