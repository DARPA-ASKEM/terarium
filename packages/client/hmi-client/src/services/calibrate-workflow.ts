import type { Dataset, CsvAsset } from '@/types/Types';
import { getModelConfigurationById, getObservables } from '@/services/model-configurations';
import { downloadRawFile, getDataset } from '@/services/dataset';
import { getUnitsFromModelParts, getModelByModelConfigurationId, getTypesFromModelParts } from '@/services/model';

export interface CalibrateMap {
	modelVariable: string;
	datasetVariable: string;
}

// Used in the setup of calibration node and drill down
// Takes a model config Id and grabs relevant objects
export const setupModelInput = async (modelConfigId: string | undefined) => {
	if (modelConfigId) {
		const [modelConfiguration, model] = await Promise.all([
			getModelConfigurationById(modelConfigId),
			getModelByModelConfigurationId(modelConfigId)
		]);

		const modelPartUnits = !model ? {} : getUnitsFromModelParts(model);
		const modelPartTypes = !model ? {} : getTypesFromModelParts(model);

		const modelOptions: any[] = model?.model.states;

		getObservables(modelConfiguration).forEach((o) => {
			modelOptions.push(o);
		});

		modelOptions.push({ id: 'timestamp' });

		return { modelConfiguration, modelOptions, modelPartUnits, modelPartTypes };
	}
	return {};
};

// Used in the setup of calibration node and drill down
// takes a datasetId and grabs relevant objects
export const setupDatasetInput = async (datasetId: string | undefined) => {
	if (datasetId) {
		// Get dataset:
		const dataset: Dataset | null = await getDataset(datasetId);
		if (dataset === undefined || !dataset) {
			console.log(`Dataset with id:${datasetId} not found`);
			return {};
		}
		const datasetOptions = dataset.columns;
		const filename = dataset?.fileNames?.[0] ?? '';
		// FIXME: We are setting the limit to -1 (i.e. no limit) on the number of rows returned.
		// This is a temporary fix since the datasets could be very large.
		const limit = -1;

		// We are assuming here there is only a single csv file. This may change in the future as the API allows for it.
		if (dataset.metadata?.format !== 'netcdf' || !dataset.esgfId) {
			const csv = (await downloadRawFile(datasetId, filename, limit)) as CsvAsset;
			// datasetValue.value = csvAsset.value?.csv.map((row) => row.join(',')).join('\n');

			if (csv?.headers) {
				csv.headers = csv.headers.map((header) => header.trim());
			}

			return { filename, csv, datasetOptions };
		}
		return {};
	}
	return {};
};
