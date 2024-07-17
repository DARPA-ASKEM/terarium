const VEGALITE_SCHEMA = 'https://vega.github.io/schema/vega-lite/v5.json';

export const CATEGORICAL_SCHEME = [
	'#5F9E3E',
	'#4375B0',
	'#8F69B9',
	'#D67DBF',
	'#E18547',
	'#D2C446',
	'#84594D'
];

export interface ForecastChartOptions {
	legend: boolean;
	translationMap?: Record<string, string>;
	colorscheme?: string[];

	title?: string;
	xAxisTitle: string;
	yAxisTitle: string;

	width: number;
	height: number;
}

export interface ForecastChartLayer {
	dataset: Record<string, any>[];
	variables: string[];
	timeField: string;
	groupField?: string;
}

/**
 * Generate Vegalite specs for simulation/forecast charts. The chart can contain:
 *  - sampling layer: multiple forecast runsk
 *  - statistics layer: statistical aggregate of the sampling layer
 *  - ground truth layer: any grounding data
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
	samplingLayer: ForecastChartLayer | null,
	statisticsLayer: ForecastChartLayer | null,
	groundTruthLayer: ForecastChartLayer | null,
	options: ForecastChartOptions
) => {
	const axisColor = '#EEE';
	const labelColor = '#667085';
	const labelFontWeight = 'normal';
	const globalFont = 'Figtree';
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

	const translationMap = options.translationMap;
	let labelExpr = '';
	if (translationMap) {
		Object.keys(translationMap).forEach((key) => {
			labelExpr += `datum.value === '${key}' ? '${translationMap[key]}' : `;
		});
		labelExpr += " 'other'";
	}

	const spec: any = {
		$schema: VEGALITE_SCHEMA,
		title: titleObj,
		description: '',
		width: options.width,
		height: options.height,
		autosize: {
			type: 'fit'
		},
		config: {
			font: globalFont
		},

		// layers
		layer: [],

		// Make layers independent
		resolve: {
			legend: { color: 'independent' },
			scale: { color: 'independent' }
		}
	};

	// Helper function to capture common layer structure
	const newLayer = (layer: ForecastChartLayer, markType: string) => {
		const header = {
			mark: { type: markType },
			data: { values: layer.dataset },
			transform: [
				{
					fold: layer.variables,
					as: ['variableField', 'valueField']
				}
			]
		};
		const encoding = {
			x: { field: layer.timeField, type: 'quantitative', axis: xaxis },
			y: { field: 'valueField', type: 'quantitative', axis: yaxis },
			color: {
				field: 'variableField',
				type: 'nominal',
				scale: {
					domain: layer.variables,
					range: options.colorscheme || CATEGORICAL_SCHEME
				},
				legend: false
			}
		};

		return {
			...header,
			encoding
		} as any;
	};

	// Build sample layer
	if (samplingLayer) {
		const layerSpec = newLayer(samplingLayer, 'line');

		Object.assign(layerSpec.encoding, {
			detail: { field: samplingLayer.groupField, type: 'nominal' },
			strokeWidth: { value: 1 },
			opacity: { value: 0.1 }
		});

		spec.layer.push(layerSpec);
	}

	// Build statistical layer
	if (statisticsLayer) {
		const layerSpec = newLayer(statisticsLayer, 'line');
		Object.assign(layerSpec.encoding, {
			opacity: { value: 1.0 },
			strokeWidth: { value: 2 }
		});

		if (options.legend === true) {
			layerSpec.encoding.color.legend = {
				title: null,
				padding: { value: 0 },
				strokeColor: null,
				orient: 'top',
				direction: 'horizontal',
				columns: Math.floor(options.width / 100),
				symbolStrokeWidth: 4,
				symbolSize: 200,
				labelFontSize: 12,
				labelOffset: 4
			};

			if (labelExpr.length > 0) {
				layerSpec.encoding.color.legend.labelExpr = labelExpr;
			}
		}
		spec.layer.push(layerSpec);
	}

	// Build ground truth layer
	if (groundTruthLayer) {
		const layerSpec = newLayer(groundTruthLayer, 'point');

		if (options.legend === true) {
			layerSpec.encoding.color.legend = {
				title: null,
				padding: { value: 0 },
				strokeColor: null,
				orient: 'top',
				direction: 'horizontal',
				columns: Math.floor(options.width / 100),
				symbolStrokeWidth: 4,
				symbolSize: 200,
				labelFontSize: 12,
				labelOffset: 4
			};

			if (labelExpr.length > 0) {
				layerSpec.encoding.color.legend.labelExpr = labelExpr;
			}
		}
		spec.layer.push(layerSpec);
	}

	// Build a transparent layer with fat lines as a better hover target for tooltips
	// Re-Build statistical layer
	if (statisticsLayer) {
		const tooltipContent = statisticsLayer.variables?.map((d) => {
			const tip: any = {
				field: d,
				type: 'quantitative',
				format: '.4f'
			};

			if (options.translationMap && options.translationMap[d]) {
				tip.title = options.translationMap[d];
			}

			return tip;
		});

		const layerSpec = newLayer(statisticsLayer, 'line');
		Object.assign(layerSpec.encoding, {
			opacity: { value: 0 },
			strokeWidth: { value: 16 },
			tooltip: [
				{ field: statisticsLayer.timeField, type: 'quantitative' },
				...(tooltipContent || [])
			]
		});
		spec.layer.push(layerSpec);
	}

	return spec;
};
