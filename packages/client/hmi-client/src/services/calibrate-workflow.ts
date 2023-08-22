import { ModelConfiguration, Dataset, CsvAsset } from '@/types/Types';
import { getModelConfigurationById } from '@/services/model-configurations';
import { downloadRawFile, getDataset } from '@/services/dataset';

// Used in the setup of calibration node and drill down
// Takes a model config Id and grabs relevant objects
export const setupModelInput = async (modelConfigId: string | undefined) => {
	if (modelConfigId) {
		const modelConfiguration: ModelConfiguration = await getModelConfigurationById(modelConfigId);
		// modelColumnNames.value = modelConfig.value.configuration.model.states.map((state) => state.name);
		const modelColumnNameOptions: string[] = modelConfiguration.configuration.model.states.map(
			(state) => state.id
		);

		// add observables
		if (modelConfiguration.configuration.semantics?.ode?.observables) {
			modelConfiguration.configuration.semantics.ode.observables.forEach((o) => {
				modelColumnNameOptions.push(o.id);
			});
		}

		modelColumnNameOptions.push('timestep');
		modelColumnNameOptions.push('timestamp');
		return { modelConfiguration, modelColumnNameOptions };
	}
	return {};
};

// Used in the setup of calibration node and drill down
// takes a datasetId and grabs relevant objects
export const setupDatasetInput = async (datasetId: string | undefined) => {
	if (datasetId) {
		// Get dataset:
		const dataset: Dataset | null = await getDataset(datasetId);
		const filename = dataset?.fileNames?.[0] ?? '';
		// FIXME: We are setting the limit to -1 (i.e. no limit) on the number of rows returned.
		// This is a temporary fix since the datasets could be very large.
		const limit = -1;

		// We are assuming here there is only a single csv file. This may change in the future as the API allows for it.
		const csv = (await downloadRawFile(datasetId, filename, limit)) as CsvAsset;
		// datasetValue.value = csvAsset.value?.csv.map((row) => row.join(',')).join('\n');
		return { filename, csv };
	}
	return {};
};
