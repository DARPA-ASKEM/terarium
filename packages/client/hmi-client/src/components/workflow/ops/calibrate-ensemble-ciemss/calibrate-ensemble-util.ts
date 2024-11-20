import { createForecastChart, AUTOSIZE } from '@/services/charts';
import { getSimulation } from '@/services/models/simulation-service';
import { EnsembleModelConfigs } from '@/types/Types';
import { CalibrateEnsembleMappingRow, CalibrateEnsembleWeights } from './calibrate-ensemble-ciemss-operation';

export async function getLossValuesFromSimulation(calibrationId: string) {
	if (!calibrationId) return [];
	const simulationObj = await getSimulation(calibrationId);
	if (simulationObj?.updates) {
		const lossValues = simulationObj?.updates
			.sort((a, b) => a.data.progress - b.data.progress)
			.map((d, i) => ({
				iter: i,
				loss: d.data.loss
			}));
		return lossValues;
	}
	return [];
}

export const updateLossChartSpec = (data: string | Record<string, any>[], size: { width: number; height: number }) =>
	createForecastChart(
		null,
		{
			data: Array.isArray(data) ? data : { name: data },
			variables: ['loss'],
			timeField: 'iter'
		},
		null,
		{
			title: '',
			width: size.width,
			height: 100,
			xAxisTitle: 'Solver iterations',
			yAxisTitle: 'Loss',
			autosize: AUTOSIZE.FIT,
			fitYDomain: true
		}
	);

export function formatCalibrateModelConfigurations(
	rows: CalibrateEnsembleMappingRow[],
	weights: CalibrateEnsembleWeights
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
			ensembleModelConfigMap[key].solutionMappings[row.datasetMapping] = value;
		});
	});

	console.log([...Object.values(ensembleModelConfigMap)]);
	return [...Object.values(ensembleModelConfigMap)];
}
