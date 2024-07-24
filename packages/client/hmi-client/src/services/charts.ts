import { percentile } from '@/utils/math';
import { isEmpty } from 'lodash';

const VEGALITE_SCHEMA = 'https://vega.github.io/schema/vega-lite/v5.json';

export const CATEGORICAL_SCHEME = ['#1B8073', '#6495E8', '#8F69B9', '#D67DBF', '#E18547', '#D2C446', '#84594D'];

export const NUMBER_FORMAT = '.3~s';

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

	const xaxis: any = {
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
	yaxis.format = NUMBER_FORMAT;

	const translationMap = options.translationMap;
	let labelExpr = '';
	if (translationMap) {
		Object.keys(translationMap).forEach((key) => {
			labelExpr += `datum.value === '${key}' ? '${translationMap[key]}' : `;
		});
		labelExpr += " 'other'";
	}

	const isCompact = options.width < 200;

	const legendProperties = {
		title: null,
		padding: { value: 0 },
		strokeColor: null,
		orient: 'top',
		direction: isCompact ? 'vertical' : 'horizontal',
		columns: Math.floor(options.width / 100),
		symbolStrokeWidth: isCompact ? 2 : 4,
		symbolSize: 200,
		labelFontSize: isCompact ? 8 : 12,
		labelOffset: isCompact ? 2 : 4,
		labelLimit: isCompact ? 50 : 150
	};

	// Start building
	const spec: any = {
		$schema: VEGALITE_SCHEMA,
		title: titleObj,
		description: '',
		width: options.width,
		height: options.height,
		autosize: {
			type: 'fit-x'
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
	if (samplingLayer && !isEmpty(samplingLayer.variables)) {
		const layerSpec = newLayer(samplingLayer, 'line');

		Object.assign(layerSpec.encoding, {
			detail: { field: samplingLayer.groupField, type: 'nominal' },
			strokeWidth: { value: 1 },
			opacity: { value: 0.1 }
		});

		spec.layer.push(layerSpec);
	}

	// Build statistical layer
	if (statisticsLayer && !isEmpty(statisticsLayer.variables)) {
		const layerSpec = newLayer(statisticsLayer, 'line');
		Object.assign(layerSpec.encoding, {
			opacity: { value: 1.0 },
			strokeWidth: { value: 2 }
		});

		if (options.legend === true) {
			layerSpec.encoding.color.legend = {
				...legendProperties
			};

			if (labelExpr.length > 0) {
				layerSpec.encoding.color.legend.labelExpr = labelExpr;
			}
		}
		spec.layer.push(layerSpec);
	}

	// Build ground truth layer
	if (groundTruthLayer && !isEmpty(groundTruthLayer.variables)) {
		const layerSpec = newLayer(groundTruthLayer, 'point');

		// FIXME: variables not aligned, set unique color for now
		layerSpec.encoding.color.scale.range = ['#1B8073'];
		// layerSpec.encoding.color.scale.range = options.colorscheme || CATEGORICAL_SCHEME;

		if (options.legend === true) {
			layerSpec.encoding.color.legend = {
				...legendProperties
			};

			if (labelExpr.length > 0) {
				layerSpec.encoding.color.legend.labelExpr = labelExpr;
			}
		}
		spec.layer.push(layerSpec);
	}

	// Build a transparent layer with fat lines as a better hover target for tooltips
	// Re-Build statistical layer
	if (statisticsLayer && !isEmpty(statisticsLayer.variables)) {
		const tooltipContent = statisticsLayer.variables?.map((d) => {
			const tip: any = {
				field: d,
				type: 'quantitative',
				format: NUMBER_FORMAT
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
			tooltip: [{ field: statisticsLayer.timeField, type: 'quantitative' }, ...(tooltipContent || [])]
		});
		spec.layer.push(layerSpec);
	}

	return spec;
};

/// /////////////////////////////////////////////////////////////////////////////
// Optimize charts
/// /////////////////////////////////////////////////////////////////////////////

export interface BaseChartOptions {
	title?: string;
	width: number;
	height: number;
	xAxisTitle: string;
	yAxisTitle: string;
	legend: boolean;
}
export interface OptimizeChartOptions extends BaseChartOptions {
	variables?: string[];
	statisticalVariables?: string[];
	timeField: string;
	groupField: string;
}

export interface SuccessCriteriaChartOptions extends BaseChartOptions {}

export function createSuccessCriteriaChart(
	riskResults: any,
	targetVariable: string,
	threshold: number,
	isMinimized: boolean,
	alpha: number,
	options: SuccessCriteriaChartOptions
): any {
	const targetState = `${targetVariable}_state`;
	const data = riskResults[targetState]?.qoi || [];
	const risk = riskResults[targetState]?.risk?.[0] || 0;
	const binCount = Math.floor(Math.sqrt(data.length)) ?? 1;
	const alphaPercentile = percentile(data, alpha);

	const determineTag = () => {
		if (isMinimized) {
			// If target variable is below
			// Colour those whose lower y-coordinates are above the threshold to be dark orange or red and label them as "Fail"
			// Colour the horizontal bars whose upper y-coordinates are below the alpha-percentile to be green and label them as "Best <alpha*100>%"
			// Colour those whose lower y-coordinates are above the alpha-percentile to be yellow/orange and label them as "Pass"
			return `datum.binned_value > ${+threshold} ? "fail" : datum.binned_value_end < ${alphaPercentile} ? "best" : "pass"`;
		}
		// If target variable is above
		// Colour those whose upper y-coordinates are below the threshold to be dark orange or red and label them as "Fail"
		// Colour the horizontal bars whose lower y-coordinates are above the alpha-percentile to be green and label them as "Best <alpha*100>%"
		// Colour those whose upper y-coordinates are below the alpha-percentile to be yellow/orange and label them as "Pass"
		return `datum.binned_value_end < ${+threshold} ? "fail" : datum.binned_value > ${alphaPercentile} ? "best" : "pass"`;
	};

	const xaxis: any = {
		title: options.xAxisTitle,
		gridColor: '#EEE',
		gridOpacity: 1.0
	};
	const yaxis = structuredClone(xaxis);
	yaxis.title = options.yAxisTitle;
	yaxis.format = NUMBER_FORMAT;

	return {
		$schema: VEGALITE_SCHEMA,
		width: options.width,
		height: options.height,
		autosize: {
			type: 'fit-x'
		},
		data: {
			values: data
		},
		transform: [
			{
				calculate: 'datum.data',
				as: 'value'
			},
			{
				bin: { maxbins: binCount },
				field: 'value',
				as: 'binned_value'
			},
			{
				calculate: determineTag(),
				as: 'tag'
			}
		],
		layer: [
			{
				mark: {
					type: 'bar',
					tooltip: true
				},
				encoding: {
					y: {
						bin: { binned: true },
						field: 'binned_value',
						title: yaxis.title,
						axis: yaxis
					},
					y2: { field: 'binned_value_end' },
					x: {
						aggregate: 'count',
						axis: xaxis
					},
					color: {
						field: 'tag',
						type: 'nominal',
						scale: {
							domain: ['fail', 'pass', 'best'],
							range: ['#B00020', '#FFAB00', '#1B8073']
						},
						legend: options.legend
							? {
									title: null,
									orient: 'top',
									direction: 'horizontal',
									labelExpr: `datum.label === "fail" ? "Failing" : datum.label === "pass" ? "Passing" : "Best ${alpha}%"`
								}
							: null
					}
				}
			},
			// Threshold line
			{
				mark: { type: 'rule', strokeDash: [4, 4], color: 'black' },
				encoding: {
					y: { datum: +threshold }
				}
			},
			// Threshold label
			{
				mark: {
					type: 'text',
					align: 'left',
					text: `Threshold = ${+threshold}`,
					baseline: 'line-bottom'
				},
				encoding: {
					y: { datum: +threshold }
				}
			},
			// Average of worst line
			{
				mark: { type: 'rule', strokeDash: [4, 4], color: 'black' },
				encoding: {
					y: { datum: +risk }
				}
			},
			// Average of worst label
			{
				mark: {
					type: 'text',
					align: 'left',
					text: `Average of worst ${100 - alpha}% = ${risk.toFixed(4)}`,
					baseline: 'line-bottom'
				},
				encoding: {
					y: { datum: +risk }
				}
			}
		]
	};
}

function createInterventionChartMarkers(data: { name: string; value: number; time: number }[]) {
	const markers = data.map((ele) => ({
		data: { values: data },
		mark: { type: 'rule', strokeDash: [4, 4], color: 'black' },
		encoding: {
			x: { datum: ele.time }
		}
	}));

	const labelsSpec = {
		data: { values: data },
		mark: {
			type: 'text',
			align: 'left',
			angle: 90,
			dx: 5,
			dy: -10
		},
		encoding: {
			x: { field: 'time', type: 'quantitative' },
			y: { field: 'value', type: 'quantitative' },
			text: { field: 'name', type: 'nominal' }
		}
	};

	return [...markers, labelsSpec];
}

function createStatisticLayer(
	data: Record<string, any>[],
	xaxis,
	yaxis,
	options: OptimizeChartOptions,
	isPreStatistic: boolean
) {
	const statisticalVariables = options.statisticalVariables?.map((d) => d);
	const tooltipContent = statisticalVariables?.map((d) => ({
		field: d,
		type: 'quantitative',
		format: NUMBER_FORMAT
	}));

	const isCompact = options.width < 200;
	const legendProperties = {
		title: null,
		padding: { value: 0 },
		strokeColor: null,
		orient: 'top',
		direction: isCompact ? 'vertical' : 'horizontal',
		columns: Math.floor(options.width / 100),
		symbolStrokeWidth: isCompact ? 2 : 4,
		symbolSize: 200,
		labelFontSize: isCompact ? 8 : 12,
		labelOffset: isCompact ? 2 : 4,
		labelExpr: "datum.label === 'before' ? 'Before optimization' : 'After optimization'"
	};

	const layerSpec: any = {
		mark: { type: 'line' },
		data: { values: data },
		transform: [
			{
				calculate: isPreStatistic ? '"before"' : '"after"',
				as: 'type'
			},
			{
				fold: statisticalVariables,
				as: ['stat_variable', 'stat_value']
			}
		],
		encoding: {
			x: { field: options.timeField, type: 'quantitative', axis: xaxis },
			y: { field: 'stat_value', type: 'quantitative', axis: yaxis },
			color: {
				field: 'type',
				type: 'nominal',
				scale: {
					domain: ['before', 'after'],
					range: ['#AAB3C6', '#1B8073']
				},
				// we only want to render a legend for the first statistic layer
				legend: options.legend && isPreStatistic ? legendProperties : null
			},
			opacity: { value: 1.0 },
			strokeWidth: { value: 3.5 },
			tooltip: [{ field: options.timeField, type: 'quantitative' }, ...(tooltipContent || [])]
		}
	};

	return layerSpec;
}

function createSampleLayer(
	data: Record<string, any>[],
	xaxis,
	yaxis,
	options: OptimizeChartOptions,
	isPreSample: boolean
) {
	const sampleVariables = options.variables?.map((d) => d);

	return {
		mark: { type: 'line' },
		data: { values: data },
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
					range: [isPreSample ? '#AAB3C6' : '#1B8073']
				},
				legend: false // No legend for sampling-layer, too noisy
			},
			detail: { field: options.groupField, type: 'nominal' },
			strokeWidth: { value: 1 },
			opacity: { value: 0.1 }
		}
	};
}

export const createOptimizeForecastChart = (
	preSampleRunData: Record<string, any>[],
	preStatisticData: Record<string, any>[],
	postSampleRunData: Record<string, any>[],
	postStatisticData: Record<string, any>[],
	interventionsData: { name: string; value: number; time: number }[],
	options: OptimizeChartOptions
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

	const xaxis: any = {
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
	yaxis.format = NUMBER_FORMAT;

	const spec: any = {
		$schema: VEGALITE_SCHEMA,
		title: titleObj,
		description: '',
		width: options.width,
		height: options.height,
		autosize: {
			type: 'fit-x'
		},

		// layers
		layer: [],

		// Make layers independent
		resolve: {
			legend: { color: 'independent' },
			scale: { color: 'independent' }
		}
	};

	// Build pre sample layer
	if (preSampleRunData && preSampleRunData.length > 0) {
		spec.layer.push(createSampleLayer(preSampleRunData, xaxis, yaxis, options, true));
	}

	// Build post sample layer
	if (postSampleRunData && postSampleRunData.length > 0) {
		spec.layer.push(createSampleLayer(postSampleRunData, xaxis, yaxis, options, false));
	}

	// Build pre statistical layer
	if (preStatisticData && preStatisticData.length > 0) {
		spec.layer.push(createStatisticLayer(preStatisticData, xaxis, yaxis, options, true));
	}

	if (postStatisticData && postStatisticData.length > 0) {
		spec.layer.push(createStatisticLayer(postStatisticData, xaxis, yaxis, options, false));
	}

	if (interventionsData && interventionsData.length > 0) {
		createInterventionChartMarkers(interventionsData).forEach((marker) => {
			spec.layer.push(marker);
		});
	}

	return spec;
};

export const createInterventionChart = (interventionsData: { name: string; value: number; time: number }[]) => {
	const spec: any = {
		$schema: VEGALITE_SCHEMA,
		width: 400,
		autosize: {
			type: 'fit-x'
		},
		layer: []
	};
	if (interventionsData && interventionsData.length > 0) {
		// markers
		createInterventionChartMarkers(interventionsData).forEach((marker) => {
			spec.layer.push(marker);
		});
		// chart
		spec.layer.push({
			data: { values: interventionsData },
			mark: 'point',
			encoding: {
				x: { field: 'time', type: 'quantitative' },
				y: { field: 'value', type: 'quantitative' }
			}
		});
	}
	return spec;
};
