import { EnsembleModelConfigs } from '@/types/Types';
import { SimulateEnsembleMappingRow, SimulateEnsembleWeights } from './simulate-ensemble-ciemss-operation';

// Note that the order of the result matters as it will help us understand the output's ordering
export function formatSimulateModelConfigurations(
	inputModelConfigsOrder: string[],
	rows: SimulateEnsembleMappingRow[],
	weights: SimulateEnsembleWeights
): EnsembleModelConfigs[] {
	const ensembleModelConfigList: EnsembleModelConfigs[] = [];

	inputModelConfigsOrder.forEach((configId) => {
		const newConfig: EnsembleModelConfigs = {
			id: configId,
			solutionMappings: {},
			weight: 0
		};
		ensembleModelConfigList.push(newConfig);
	});

	// 1. map the weights to the ensemble model configs
	Object.entries(weights).forEach(([key, value]) => {
		// return if there is no weight
		if (!value) return;
		const index = ensembleModelConfigList.findIndex((ele) => ele.id === key) as number;
		ensembleModelConfigList[index].weight = value;
	});

	// 2. format the solution mappings
	rows.forEach((row) => {
		Object.entries(row.modelConfigurationMappings).forEach(([key, value]) => {
			const index = ensembleModelConfigList.findIndex((ele) => ele.id === key) as number;
			ensembleModelConfigList[index].solutionMappings = {
				[row.newName]: value
			};
		});
	});

	return ensembleModelConfigList;
}
