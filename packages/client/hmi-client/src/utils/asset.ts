import { Dataset, DocumentAsset, Model } from '@/types/Types';

export type Asset = Model | Dataset | DocumentAsset;

// TEMP FUNCTIONS
export function isModel(item: Asset): item is Model {
	return (<Model>item)?.header?.model_version !== undefined;
}

export function isDataset(item: Asset): item is Dataset {
	return (<Dataset>item).columns !== undefined || (<Dataset>item).esgfId !== undefined;
}

export function isDocumentAsset(item: Asset): item is DocumentAsset {
	return (<DocumentAsset>item).fileNames !== undefined;
}
