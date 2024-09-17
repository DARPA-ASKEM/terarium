/**
 * @fileoverview This file contains the utils for the tera-dataset components.
 */

import type { Dataset } from '@/types/Types';

/**
 * Enriches the dataset with additional information from the data-card.
 * @param dataset
 * @returns the enriched dataset
 */
function enrichDataset(dataset: Dataset): Dataset {
	// Set a default metadata and data-card if it does not exist
	if (!dataset?.metadata) {
		dataset.metadata = {};
	}
	if (!dataset.metadata?.dataCard) {
		dataset.metadata.dataCard = {};
	}

	// Properties that should be set to a default value if they do not exist
	const propertiesToSetDefault = [
		'DESCRIPTION',
		'AUTHOR_NAME',
		'AUTHOR_EMAIL',
		'DATE',
		'PROVENANCE',
		'SENSITIVITY',
		'LICENSE',
		'DATASET_TYPE'
	];

	// Set the default value for the properties
	propertiesToSetDefault.forEach((property) => {
		if (!dataset.metadata) {
			dataset.metadata = {};
		}
		if (!dataset?.metadata?.dataCard) {
			dataset.metadata.dataCard = {};
		}
		if (!dataset?.metadata?.dataCard?.[property]) {
			dataset.metadata.dataCard[property] = '-';
		}
	});

	return dataset;
}

function isSaveDatasetDisabled(id, projectid): boolean {
	if (id === undefined || id === '' || !projectid) {
		return true;
	}
	return false;
}
export { enrichDataset, isSaveDatasetDisabled };
