import { createForecastChart, AUTOSIZE } from '@/services/charts';
import { getSimulation } from '@/services/models/simulation-service';

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