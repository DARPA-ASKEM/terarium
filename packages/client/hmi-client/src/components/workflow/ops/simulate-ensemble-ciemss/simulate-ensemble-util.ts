import { EnsembleModelConfigs } from '@/types/Types';
import { SimulateEnsembleMappingRow, SimulateEnsembleWeight } from './simulate-ensemble-ciemss-operation';

export function formatSimulateModelConfigurations(
	rows: SimulateEnsembleMappingRow[],
	weights: SimulateEnsembleWeight[]
): EnsembleModelConfigs[] {
	const ensembleModelConfigMap: { [key: string]: EnsembleModelConfigs } = {};
	// const totalWeight = Object.values(weights).reduce((acc, curr) => acc + curr.value, 0) ?? 1;
	const totalWeight = weights.map((ele) => ele.value).reduce((acc, curr) => acc + curr, 0);
	// 1. map the weights to the ensemble model configs
	weights.forEach((weight) => {
		// return if there is no weight
		if (!weight.value) return;

		const ensembleModelConfig: EnsembleModelConfigs = {
			id: weight.modelConfigurationId,
			solutionMappings: {},
			weight: weight.value / totalWeight
		};

		ensembleModelConfigMap[weight.modelConfigurationId] = ensembleModelConfig;
	});

	// 2. format the solution mappings
	rows.forEach((row) => {
		row.modelConfigurationMappings.forEach((ele) => {
			if (!ensembleModelConfigMap[ele.modelConfigId]) return;
			ensembleModelConfigMap[ele.modelConfigId].solutionMappings[row.newName] = ele.compartmentName;
		});
	});

	return [...Object.values(ensembleModelConfigMap)];
}
