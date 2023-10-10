/**
 * @fileoverview This file contains the utils for the tera-dataset components.
 */

import { Dataset } from '@/types/Types';

/**
 * Enriches the dataset with additional information from the data-card.
 * @param dataset
 * @returns the enriched dataset
 */
function enrichDataset(dataset: Dataset): Dataset {
	console.group('enrichDataset');
	console.log(dataset);
	if (!dataset?.metadata?.dataCard) return dataset;

	const dataCard = dataset.metadata.dataCard;
	console.table(dataCard);

	if (!dataset?.metadata?.dataCard?.DESCRIPTION) {
		dataset.metadata.dataCard.DESCRIPTION = '-';
	}
	if (!dataset?.metadata?.dataCard?.AUTHOR_NAME) {
		dataset.metadata.dataCard.AUTHOR_NAME = '-';
	}
	if (!dataset?.metadata?.dataCard?.AUTHOR_EMAIL) {
		dataset.metadata.dataCard.AUTHOR_EMAIL = '-';
	}
	if (!dataset?.metadata?.dataCard?.DATE) {
		dataset.metadata.dataCard.DATE = '-';
	}
	if (!dataset?.metadata?.dataCard?.PROVENANCE) {
		dataset.metadata.dataCard.PROVENANCE = '-';
	}
	if (!dataset?.metadata?.dataCard?.SENSITIVITY) {
		dataset.metadata.dataCard.SENSITIVITY = '-';
	}
	if (!dataset?.metadata?.dataCard?.LICENSE) {
		dataset.metadata.dataCard.LICENSE = '-';
	}
	if (!dataset?.metadata?.dataCard?.DATASET_TYPE) {
		dataset.metadata.dataCard.DATASET_TYPE = '-';
	}

	return dataset;
}

export { enrichDataset };
