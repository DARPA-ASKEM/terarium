import { ResourceType, ResultType } from '@/types/common';
import { Filters } from '@/types/Filter';
import { isEmpty } from 'lodash';
import { FACET_FIELDS as MODEL_FACET_FIELDS } from '@/types/Model';
import { Dataset, DocumentAsset, Model, ProgrammingLanguage } from '@/types/Types';
import IconDocument20 from '@carbon/icons-vue/es/document/20';
import IconMachineLearningModel20 from '@carbon/icons-vue/es/machine-learning-model/20';
import IconTableSplit20 from '@carbon/icons-vue/es/table--split/20';
import { FACET_FIELDS as DATASET_FACET_FIELDS } from '@/types/Dataset';

export const applyFacetFilters = <T>(results: T[], filters: Filters, resourceType: ResourceType) => {
	if (isEmpty(filters) || isEmpty(results)) {
		return;
	}

	const { clauses } = filters;
	const ASSET_FACET_FIELDS: string[] = resourceType === ResourceType.MODEL ? MODEL_FACET_FIELDS : DATASET_FACET_FIELDS;

	clauses.forEach((clause) => {
		const filterField: string = clause.field; // the field to filter on
		// "filters" may include fields that belong to different types of artifacts
		//  thus make sure to only filter models using Model fields
		if (ASSET_FACET_FIELDS.includes(filterField)) {
			const filterValues = clause.values.map((v) => v.toString()); // array of values to filter upon
			const isNot = !clause.isNot; // is the filter reversed?

			results.splice(
				0,
				results.length,
				...results.filter((asset) => {
					const assetAttribute: any = asset[filterField as keyof T];
					return filterValues.includes(assetAttribute.toString()) === isNot;
				})
			);
		}
	});
};

export const getResourceTypeIcon = (type: string) => {
	switch (type) {
		case ResourceType.MODEL:
			return IconMachineLearningModel20;
		case ResourceType.DATASET:
			return IconTableSplit20;
		default:
			return IconDocument20;
	}
};

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
