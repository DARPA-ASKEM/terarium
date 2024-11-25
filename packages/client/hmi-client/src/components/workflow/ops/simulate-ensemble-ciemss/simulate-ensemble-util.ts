import { EnsembleModelConfigs } from '@/types/Types';
import { SimulateEnsembleMappingRow, SimulateEnsembleWeights } from './simulate-ensemble-ciemss-operation';

export function formatSimulateModelConfigurations(
	rows: SimulateEnsembleMappingRow[],
	weights: SimulateEnsembleWeights
): EnsembleModelConfigs[] {
	const ensembleModelConfigMap: { [key: string]: EnsembleModelConfigs } = {};
	// 1. map the weights to the ensemble model configs
	Object.entries(weights).forEach(([key, value]) => {
		// return if there is no weight
		if (!value) return;

		const ensembleModelConfig: EnsembleModelConfigs = {
			id: key,
			solutionMappings: {},
			weight: value
		};

		ensembleModelConfigMap[key] = ensembleModelConfig;
	});

	// 2. format the solution mappings
	rows.forEach((row) => {
		Object.entries(row.modelConfigurationMappings).forEach(([key, value]) => {
			if (!ensembleModelConfigMap[key]) return;
			ensembleModelConfigMap[key].solutionMappings[row.newName] = value;
		});
	});

	return [...Object.values(ensembleModelConfigMap)];
}
