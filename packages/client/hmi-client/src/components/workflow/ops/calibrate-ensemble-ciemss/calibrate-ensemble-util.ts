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

// Note that the order of the result matters as it will help us understand the output's ordering
export function formatCalibrateModelConfigurations(
	inputModelConfigsOrder: string[],
	rows: CalibrateEnsembleMappingRow[],
	weights: CalibrateEnsembleWeights
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
				[row.datasetMapping]: value
			};
		});
	});

	return ensembleModelConfigList;
}
