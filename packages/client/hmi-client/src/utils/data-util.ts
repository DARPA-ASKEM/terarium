import { Dataset, DocumentAsset, Model, ProgrammingLanguage } from '@/types/Types';

export type ResultType = Model | Dataset | DocumentAsset;

// TEMP FUNCTIONS
export function isModel(item: ResultType): item is Model {
	return (<Model>item)?.header?.model_version !== undefined;
}

export function isDataset(item: ResultType): item is Dataset {
	return (<Dataset>item).columns !== undefined || (<Dataset>item).esgfId !== undefined;
}

export function isDocumentAsset(item: ResultType): item is DocumentAsset {
	return (<DocumentAsset>item).fileNames !== undefined;
}
export function getResourceID(item: ResultType) {
	return item.id;
}

//

export function pdfNameFromUrl(url: string): string | null {
	const urlWithoutParams = url.split('?')[0]; // Remove query parameters
	const regex = /\/([^/]+\.pdf)$/i;
	const match = urlWithoutParams.match(regex);

	if (match && match[1]) {
		return match[1];
	}
	return null;
}

export function extensionFromProgrammingLanguage(lang: ProgrammingLanguage): string | null {
	switch (lang) {
		case ProgrammingLanguage.Python:
			return 'py';
		case ProgrammingLanguage.Julia:
			return 'jl';
		case ProgrammingLanguage.R:
			return 'r';
		case ProgrammingLanguage.Zip:
			return 'zip';
		default:
			return null;
	}
}
