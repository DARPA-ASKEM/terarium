const VEGALITE_SCHEMA = 'https://vega.github.io/schema/vega-lite/v5.json';

const CATEGORICAL_SCHEME = [
	'#5F9E3E',
	'#4375B0',
	'#8F69B9',
	'#D67DBF',
	'#E18547',
	'#D2C446',
	'#84594D'
];

interface ForecastChartOptions {
	variables?: string[];
	statisticalVariables?: string[];
	groundTruthVariables?: string[];

	legend: boolean;
	colorscheme?: string[];
	timeField: string;
	groupField: string;

	title?: string;
	xAxisTitle: string;
	yAxisTitle: string;

	width: number;
	height: number;
}

/**
 * Generate Vegalite specs for simulation/forecast charts
 *
 * Data comes in as a list of multi-variate objects:
 *   [ { time: 1, var1: 0.2, var2: 0.5, var3: 0.1 }, ... ]
 *
 * This then transformed by the fold-transform to be something like:
 *   [
 *     { time: 1, var1: 0.2, var2: 0.5, var3: 0.1, var: 'var1', value: 0.2 },
 *     { time: 1, var1: 0.2, var2: 0.5, var3: 0.1, var: 'var2', value: 0.5 },
 *     { time: 1, var1: 0.2, var2: 0.5, var3: 0.1, var: 'var3', value: 0.1 },
 *     ...
 *   ]
 *
 * Then we use the new 'var' and 'value' columns to render timeseries
 * */
export const createForecastChart = (
	sampleRunData: Record<string, any>[],
	statisticData: Record<string, any>[],
	groundTruthData: Record<string, any>[],
	options: ForecastChartOptions
) => {
	const axisColor = '#EEE';
	const labelColor = '#667085';
	const labelFontWeight = 'normal'; // Adjust font weight here
	const titleObj = options.title
		? {
				text: options.title,
				anchor: 'start',
				subtitle: ' ',
				subtitlePadding: 4
			}
		: null;

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
		title: titleObj,
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
		const sampleVariables = options.variables?.map((d) => d);

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
						range: options.colorscheme || CATEGORICAL_SCHEME
					},
					legend: false // No legend for sampling-layer, too noisy
				},
				detail: { field: options.groupField, type: 'nominal' },
				strokeWidth: { value: 1 },
				opacity: { value: 0.1 }
			}
		});
	}

	// Build statistical layer
	if (statisticData && statisticData.length > 0) {
		const statisticalVariables = options.statisticalVariables?.map((d) => d);
		const tooltipContent = statisticalVariables?.map((d) => ({
			field: d,
			type: 'quantitative',
			format: '.4f'
		}));

		const layerSpec: any = {
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
						range: options.colorscheme || CATEGORICAL_SCHEME
					},
					legend: false
				},
				opacity: { value: 1.0 },
				strokeWidth: { value: 3.5 },
				tooltip: [{ field: options.timeField, type: 'quantitative' }, ...(tooltipContent || [])]
			}
		};

		if (options.legend === true) {
			layerSpec.encoding.color.legend = {
				strokeColor: null,
				padding: { value: 5 }
			};
		}
		spec.layer.push(layerSpec);
	}

	// Build ground truth layer
	if (groundTruthData && groundTruthData.length > 0) {
		const groundTruthVariables = options.groundTruthVariables?.map((d) => d);

		const layerSpec: any = {
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
						range: options.colorscheme || CATEGORICAL_SCHEME
					},
					legend: false
				}
			}
		};

		if (options.legend === true) {
			layerSpec.encoding.color.legend = {
				strokeColor: null,
				padding: { value: 5 }
			};
		}
		spec.layer.push(layerSpec);
	}
	return spec;
};
