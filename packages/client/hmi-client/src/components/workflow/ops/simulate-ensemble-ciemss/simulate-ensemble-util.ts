import { getSimulation } from '@/services/models/simulation-service';
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

// The result files from ensemble simulate have column headers such as model_#/column_name
// This can be used to trace what model configuration is what model_# in a given run.
export async function getResultModelConfigMap(runId: string) {
	interface EnsembleModelConfigsSnakeCase {
		id: string;
		solution_mappings: { [index: string]: string };
		weight: number;
	}
	const resultMap: { [key: string]: string } = {};

	// Get Simulation Execution Run:
	const simulationRun = await getSimulation(runId);
	if (!simulationRun) {
		console.error(`Could not find simulation ${runId}`);
		return null;
	}
	const modelConfigs: EnsembleModelConfigsSnakeCase[] = simulationRun.executionPayload.model_configs;
	for (let i = 0; i < modelConfigs.length; i++) {
		resultMap[`model_${i}`] = modelConfigs[i].id;
	}
	return resultMap;
}
