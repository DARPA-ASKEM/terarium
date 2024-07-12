const VEGALITE_SCHEMA = 'https://vega.github.io/schema/vega-lite/v5.json';

interface ForecastChartOptions {
	variables: string[];
	statisticalVariables: string[];
	groundTruthVariables: string[];

	colorscheme: string[];
	timeField: string;

	xAxisTitle: string;
	yAxisTitle: string;

	width: number;
	height: number;
}

/**
 * Generate Vegalite specs for simulation/forecast charts
 * */
export const createForecastChart = (
	sampleRunData: any[],
	statisticData: any[],
	groundTruthData: any[],
	options: ForecastChartOptions
) => {
	const axisColor = '#EEE';
	const labelColor = '#667085';
	const labelFontWeight = 'normal'; // Adjust font weight here

	const xaxis = {
		domainColor: axisColor,
		tickColor: { value: axisColor },
		labelColor: { value: labelColor },
		labelFontWeight,
		title: options.xAxisTitle,
		gridColor: '#EEE',
		gridOpacity: 1.0
	};
	const yaxis = structuredClone(xaxis);
	yaxis.title = options.yAxisTitle;

	const spec: any = {
		$schema: VEGALITE_SCHEMA,
		title: {
			text: 'Simulation chart',
			anchor: 'start',
			subtitle: ' ',
			subtitlePadding: 4
		},
		description: '',
		width: options.width,
		height: options.height,

		// layers
		layer: [],

		// Make layers independent
		resolve: {
			legend: { color: 'independent' },
			scale: { color: 'independent' }
		}
	};

	// Build sample layer
	if (sampleRunData && sampleRunData.length > 0) {
		const sampleVariables = options.variables.map((d) => d);

		spec.layer.push({
			mark: { type: 'line' },
			data: { values: sampleRunData },
			transform: [
				{
					fold: sampleVariables,
					as: ['sample_variable', 'sample_value']
				}
			],
			encoding: {
				x: { field: options.timeField, type: 'quantitative', axis: xaxis },
				y: { field: 'sample_value', type: 'quantitative', axis: yaxis },
				color: {
					field: 'sample_variable',
					type: 'nominal',
					scale: {
						domain: sampleVariables,
						range: options.colorscheme
					},
					legend: false
				},
				detail: { field: 'sample_variable', type: 'nominal' },
				strokeWidth: { value: 1 },
				opacity: { value: 0.1 }
			}
		});
	}

	// Build statistical layer (mean)
	if (statisticData && statisticData.length > 0) {
		const statisticalVariables = options.statisticalVariables.map((d) => d);
		const tooltipContent = statisticalVariables.map((d) => ({
			field: d,
			type: 'quantitative',
			format: '.4f'
		}));

		spec.layer.push({
			mark: { type: 'line' },
			data: { values: statisticData },
			transform: [
				{
					fold: statisticalVariables,
					as: ['stat_variable', 'stat_value']
				}
			],
			encoding: {
				x: { field: options.timeField, type: 'quantitative', axis: xaxis },
				y: { field: 'stat_value', type: 'quantitative', axis: yaxis },
				color: {
					field: 'stat_variable',
					type: 'nominal',
					scale: {
						domain: statisticalVariables,
						range: options.colorscheme
					},
					legend: {
						strokeColor: null,
						padding: { value: 5 }
					}
				},
				opacity: { value: 1.0 },
				strokeWidth: { value: 3.5 },
				tooltip: [{ field: options.timeField, type: 'quantitative' }, ...tooltipContent]
			}
		});
	}

	// Build ground truth layer
	if (groundTruthData && groundTruthData.length > 0) {
		const groundTruthVariables = options.groundTruthVariables.map((d) => d);
		spec.layer.push({
			mark: { type: 'point' },
			data: { values: groundTruthData },
			transform: [
				{
					fold: groundTruthVariables,
					as: ['ground_variable', 'ground_value']
				}
			],
			encoding: {
				x: { field: options.timeField, type: 'quantitative', axis: xaxis },
				y: { field: 'ground_value', type: 'quantitative', axis: yaxis },
				color: {
					field: 'ground_variable',
					type: 'nominal',
					scale: {
						domain: groundTruthVariables,
						range: options.colorscheme
					},
					legend: {
						strokeColor: null,
						padding: { value: 5 }
					}
				}
			}
		});
	}
	return spec;
};
