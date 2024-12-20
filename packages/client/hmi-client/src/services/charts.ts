import * as d3 from 'd3';
import { isEmpty, pick } from 'lodash';
import { percentile } from '@/utils/math';
import { VisualizationSpec } from 'vega-embed';
import { v4 as uuidv4 } from 'uuid';
import type { ChartAnnotation, FunmanInterval } from '@/types/Types';
import { CalendarDateType } from '@/types/common';
import { countDigits, fixPrecisionError } from '@/utils/number';
import { format } from 'd3';
import { flattenInterventionData } from './intervention-policy';
import type { FunmanBox, FunmanConstraintsResponse } from './models/funman-service';

const VEGALITE_SCHEMA = 'https://vega.github.io/schema/vega-lite/v5.json';

const NUMBER_FORMAT = '.3~s';
export const expressionFunctions = {
	// chartNumberFormatter is a custom number format that will display numbers in a more readable format
	chartNumberFormatter: (value: number) => {
		const correctedValue = fixPrecisionError(value);
		if (value > -1 && value < 1) {
			return countDigits(correctedValue) > 6 ? correctedValue.toExponential(3) : correctedValue.toString();
		}
		return format(NUMBER_FORMAT)(correctedValue);
	},
	// Just show full value in tooltip
	tooltipFormatter: (value) => {
		if (value === undefined) return 'N/A';
		return fixPrecisionError(value);
	}
};

export const CATEGORICAL_SCHEME = ['#1B8073', '#6495E8', '#8F69B9', '#D67DBF', '#E18547', '#D2C446', '#84594D'];

// diverging categorical colour scheme for the sensitivity chart (from a deep blue -> a deep red)
export const SENSITIVITY_COLOUR_SCHEME = ['#4575B4', '#91BFDB', '#E0F3F8', '#FFFFBF', '#FEE090', '#FC8D59', '#D73027'];
export enum AUTOSIZE {
	FIT = 'fit',
	FIT_X = 'fit-x',
	FIT_Y = 'fit-y',
	PAD = 'pad',
	NONE = 'none'
}

interface BaseChartOptions {
	title?: string;
	width: number;
	height: number;
	xAxisTitle: string;
	yAxisTitle: string;
	legend?: boolean;
	autosize?: AUTOSIZE;
	dateOptions?: DateOptions;
	scale?: string;
}

export interface DateOptions {
	dateFormat: CalendarDateType;
	startDate: Date;
}
export interface ForecastChartOptions extends BaseChartOptions {
	translationMap?: Record<string, string>;
	colorscheme?: string[];
	fitYDomain?: boolean;
	legendProperties?: Record<string, any>;
	bins?: Map<string, number[]>;
}

export interface ForecastChartLayer {
	data: Record<string, any>[] | { name: string } | { url: string };
	variables: string[];
	timeField: string;
	groupField?: string;
}

export interface SensitivityChartLayer {
	data: Record<string, any>[];
	inputVariables: string[];
	outputVariable: string;
}

export interface HistogramChartOptions extends BaseChartOptions {
	maxBins?: number;
	variables: { field: string; label?: string; width: number; color: string }[];
	legendProperties?: Record<string, any>;
}

export interface ErrorChartOptions extends Omit<BaseChartOptions, 'height' | 'yAxisTitle' | 'legend'> {
	height?: number;
	areaChartHeight?: number;
	boxPlotHeight?: number;
	color?: string;
	variables: { field: string; label?: string }[];
}

export interface InterventionMarkerOptions {
	hideLabels?: boolean;
	labelXOffset?: number;
	dateOptions?: DateOptions;
}

export interface ChartEncoding {
	field: string;
	type: string;
	axis: any;
	scale?: any;
}

function formatDateLabelFn(date: Date, datum: string, type: CalendarDateType): string {
	switch (type) {
		case CalendarDateType.YEAR:
			return `timeFormat(datetime(${date.getFullYear()} + ${datum}, ${date.getMonth()}, ${date.getDate()}), '%Y')`;
		case CalendarDateType.MONTH:
			return `timeFormat(datetime(${date.getFullYear()} + floor(${datum} / 12), ${date.getMonth()} + (${datum} % 12) , ${date.getDate()}), '%b %Y')`;
		case CalendarDateType.DATE:
		default:
			return `timeFormat(datetime(${date.getFullYear()}, ${date.getMonth()}, ${date.getDate()} + ${datum}), '%b %d, %Y')`;
	}
}

export function createErrorChart(dataset: Record<string, any>[], options: ErrorChartOptions) {
	const axisColor = '#EEE';
	const labelColor = '#667085';
	const labelFontWeight = 'normal';
	const globalFont = 'Figtree';

	const areaChartColor = options.color ?? '#1B8073';
	const dotColor = options.color ?? '#1B8073';
	const boxPlotColor = '#000';

	const width = options.width;
	const height = options.height ?? 100;
	const boxPlotHeight = options.boxPlotHeight ?? 16;
	const areaChartHeight = options.areaChartHeight ?? 44;
	const gap = 15;

	const areaChartRange = [areaChartHeight, 0];
	const dotChartRange = [areaChartHeight + gap, areaChartHeight + gap + boxPlotHeight];
	const boxPlotYPosition = (dotChartRange[0] + dotChartRange[1]) / 2;

	const variablesOptions = options.variables.map(({ field, label }) => ({ field, label: label ?? field }));

	const variables = variablesOptions.map((v) => v.field);

	const titleObj = options.title
		? {
				text: options.title,
				anchor: 'start',
				subtitle: ' ',
				subtitlePadding: 4
			}
		: null;

	const brushParamName = 'brush';

	const config = {
		facet: { spacing: 2 },
		font: globalFont,
		mark: { opacity: 1 },
		view: { stroke: 'transparent' },
		axis: {
			tickCount: 5,
			ticks: false,
			grid: false,
			domain: false,
			gridDash: [2, 3],
			domainColor: axisColor,
			tickColor: { value: axisColor },
			labelColor: { value: labelColor },
			labelFontWeight,
			labels: false
		},
		area: {
			line: true,
			fillOpacity: 0.33
		},
		point: {
			color: dotColor,
			opacity: 0.7,
			filled: true
		},
		boxplot: {
			size: boxPlotHeight,
			median: { color: boxPlotColor },
			box: {
				fill: 'transparent',
				stroke: boxPlotColor,
				strokeWidth: 1
			}
		}
	};

	return {
		$schema: VEGALITE_SCHEMA,
		config,
		title: titleObj,
		data: { values: dataset },
		transform: [
			{ fold: variables, as: ['variable', '_value'] },
			{ extent: '_value', param: '_valueExtent' },
			{
				lookup: 'variable',
				from: { data: { values: variablesOptions }, key: 'field', fields: ['label'] },
				as: 'Variable Label'
			}
		],
		facet: { row: { field: 'variable', title: '', header: { labels: null } } },
		resolve: { scale: { y: 'independent' } },
		spec: {
			width,
			height,
			encoding: {
				x: {
					field: '_value',
					type: 'quantitative',
					title: options.xAxisTitle,
					axis: { labels: true, domain: true, ticks: true }
				},
				y: {
					title: ''
				}
			},
			layer: [
				{
					transform: [{ density: '_value', as: ['val', 'density'], extent: { signal: '_valueExtent' } }],
					mark: {
						type: 'area',
						tooltip: true
					},
					encoding: {
						x: {
							field: 'val',
							type: 'quantitative'
						},
						y: {
							field: 'density',
							type: 'quantitative',
							scale: { range: areaChartRange }
						},
						color: {
							value: areaChartColor
						}
					}
				},
				{
					mark: {
						type: 'boxplot'
					},
					encoding: {
						y: {
							field: 'Variable Label',
							scale: { range: [boxPlotYPosition, boxPlotYPosition] },
							axis: {
								grid: true,
								labels: true,
								orient: 'left',
								offset: 5,
								labelAngle: -90,
								labelLimit: areaChartHeight + boxPlotHeight + gap
							}
						}
					}
				},
				{
					mark: {
						type: 'point'
					},
					transform: [{ calculate: 'random()', as: 'jitter' }],
					encoding: {
						y: {
							field: 'jitter',
							type: 'quantitative',
							scale: { range: dotChartRange }
						},
						color: {
							condition: { param: brushParamName },
							value: 'lightgray'
						},
						tooltip: [{ field: '_value', title: options.xAxisTitle }]
					},
					params: [
						{
							name: brushParamName,
							select: { type: 'interval', encodings: ['x'], resolve: 'global' }
						}
					]
				}
			]
		}
	} as any;
}

export function createHistogramChart(dataset: Record<string, any>[], options: HistogramChartOptions) {
	const maxBins = options.maxBins ?? 10;
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

	const barMinGapWidth = 4;
	const xDiff = 32; // Diff between inner chart content width and the outer box width
	const maxBarWidth = Math.max(...options.variables.map((v) => v.width));
	const reaminingXSpace = options.width - xDiff - maxBins * (maxBarWidth + barMinGapWidth);
	const xPadding = reaminingXSpace < 0 ? barMinGapWidth : reaminingXSpace / 2;

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
	yaxis.title = options.yAxisTitle || '';

	const legendProperties = {
		title: null,
		padding: { value: 0 },
		strokeColor: null,
		orient: 'top',
		direction: 'horizontal',
		symbolStrokeWidth: 4,
		symbolSize: 200,
		labelFontSize: 12,
		labelOffset: 4,
		...options.legendProperties
	};

	const spec: VisualizationSpec = {
		$schema: VEGALITE_SCHEMA,
		title: titleObj as any,
		width: options.width,
		height: options.height,
		autosize: { type: AUTOSIZE.FIT },
		data: {
			values: []
		},
		layer: [],
		config: {
			font: globalFont
		}
	};

	const data = dataset.map((d) =>
		pick(
			d,
			options.variables.map((v) => v.field)
		)
	);

	if (isEmpty(data?.[0])) return spec;

	spec.data = { values: data };

	// Create an extent from the min max of the data across all variables, this is used to set the bin extent and let multiple histograms from different layers to share the same bin extent
	const extent = [Infinity, -Infinity];
	data.forEach((d) => {
		extent[0] = Math.min(extent[0], Math.min(...Object.values(d)));
		extent[1] = Math.max(extent[1], Math.max(...Object.values(d)));
	});

	const createLayers = (opts) => {
		const colorScale = {
			domain: opts.variables.map((v) => v.label ?? v.field),
			range: opts.variables.map((v) => v.color)
		};
		const bin = { maxbins: maxBins, extent };
		const aggregate = 'count';
		return opts.variables.map((varOption) => ({
			mark: { type: 'bar', width: varOption.width, tooltip: true },
			encoding: {
				x: { bin, field: varOption.field, axis: xaxis, scale: { padding: xPadding } },
				y: { aggregate, axis: yaxis },
				color: {
					legend: { ...legendProperties },
					type: 'nominal',
					datum: varOption.label ?? varOption.field,
					scale: colorScale
				},
				tooltip: [
					{ bin, field: varOption.field, title: varOption.field },
					{ aggregate, type: 'quantitative', title: yaxis.title }
				]
			}
		}));
	};

	// Add layers
	spec.layer = createLayers(options);

	return spec;
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
export function createForecastChart(
	samplingLayer: ForecastChartLayer | null,
	statisticsLayer: ForecastChartLayer | null,
	groundTruthLayer: ForecastChartLayer | null,
	options: ForecastChartOptions
) {
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
		symbolStrokeWidth: isCompact ? 2 : 4,
		symbolSize: 200,
		labelFontSize: isCompact ? 8 : 12,
		labelOffset: isCompact ? 2 : 4,
		labelLimit: isCompact ? 50 : 150,
		...options.legendProperties
	};

	// Start building
	const spec: any = {
		$schema: VEGALITE_SCHEMA,
		title: titleObj,
		description: '',
		width: options.width,
		height: options.height,
		autosize: {
			type: options.autosize || AUTOSIZE.FIT_X
		},
		config: {
			font: globalFont,
			legend: {
				layout: {
					direction: legendProperties.direction,
					anchor: 'start'
				}
			}
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
		const selectedFields = layer.variables.concat([layer.timeField]);
		if (layer.groupField) selectedFields.push(layer.groupField);

		const data = Array.isArray(layer.data) ? { values: layer.data.map((d) => pick(d, selectedFields)) } : layer.data;
		const header = {
			data,
			transform: [
				{
					fold: layer.variables,
					as: ['variableField', 'valueField']
				}
			]
		};

		// group by bins id if present
		if (options.bins) {
			let calculateExpr = '';
			options.bins?.forEach((sampleIds, bin) => {
				calculateExpr += `indexof([${sampleIds}], datum.sample_id) >= 0 ? '${bin}' : `;
			});
			calculateExpr += '0';

			header.transform.push({
				// @ts-ignore
				calculate: calculateExpr,
				as: ['group']
			});
		}

		let dateExpression;
		if (options.dateOptions) {
			dateExpression = formatDateLabelFn(options.dateOptions.startDate, 'datum.value', options.dateOptions.dateFormat);
		}
		const encodingX: ChartEncoding = {
			field: layer.timeField,
			type: 'quantitative',
			axis: {
				...xaxis,
				labelExpr: dateExpression
			}
		};
		const encodingY: ChartEncoding = {
			field: 'valueField',
			type: 'quantitative',
			axis: yaxis
		};

		if (options.scale === 'log') {
			encodingY.scale = { type: 'symlog' };
		}

		if (options.fitYDomain && layer.data[0]) {
			// gets the other fieldname
			const yField = Object.keys(layer.data[0]).find((elem) => elem !== layer.timeField);
			if (yField && Array.isArray(layer.data)) {
				const yValues = [...layer.data].map((datum) => datum[yField]);
				const domainMin = Math.min(...yValues);
				const domainMax = Math.max(...yValues);
				encodingY.scale = {
					domain: [domainMin, domainMax]
				};
			}
		}

		const encoding = {
			x: encodingX,
			y: encodingY,
			color: {
				field: options.bins ? 'group' : 'variableField',
				type: 'nominal',
				scale: {
					domain: options.bins ? Array.from(options.bins.keys()) : layer.variables,
					range: options.colorscheme || CATEGORICAL_SCHEME
				},
				legend: options.bins ? { ...legendProperties } : null
			}
		};

		return {
			...header,
			layer: [
				{
					mark: { type: markType },
					encoding
				}
			]
		} as any;
	};

	// Build sample layer
	if (samplingLayer && !isEmpty(samplingLayer.variables) && !isEmpty(samplingLayer.data)) {
		const layerSpec = newLayer(samplingLayer, 'line');
		const encoding = layerSpec.layer[0].encoding;
		Object.assign(encoding, {
			detail: { field: samplingLayer.groupField, type: 'nominal' },
			strokeWidth: { value: 1 },
			opacity: { value: options.bins ? 1.0 : 0.1 }
		});

		spec.layer.push(layerSpec);
	}

	// Build statistical layer
	if (statisticsLayer && !isEmpty(statisticsLayer.variables) && !isEmpty(statisticsLayer.data)) {
		const layerSpec = newLayer(statisticsLayer, 'line');
		const lineSubLayer = layerSpec.layer[0];
		const tooltipSubLayer = structuredClone(lineSubLayer);
		Object.assign(lineSubLayer.encoding, {
			opacity: { value: 1.0 },
			strokeWidth: { value: 2 }
		});

		if (options.legend === true) {
			lineSubLayer.encoding.color.legend = {
				...legendProperties
			};

			if (labelExpr.length > 0) {
				lineSubLayer.encoding.color.legend.labelExpr = labelExpr;
			}
		}

		// Build a transparent layer with fat lines as a better hover target for tooltips
		const tooltipContent = statisticsLayer.variables?.map((d) => {
			const tip: any = {
				field: d,
				type: 'quantitative'
			};

			if (options.translationMap && options.translationMap[d]) {
				tip.title = options.translationMap[d];
			}

			return tip;
		});

		Object.assign(tooltipSubLayer.encoding, {
			opacity: { value: 0.00000001 },
			strokeWidth: { value: 16 },
			tooltip: [
				{
					field: statisticsLayer.timeField,
					type: 'quantitative'
				},
				...(tooltipContent || [])
			]
		});
		layerSpec.layer.push(tooltipSubLayer);

		spec.layer.push(layerSpec);
	}

	// Build ground truth layer
	if (groundTruthLayer && !isEmpty(groundTruthLayer.variables) && !isEmpty(groundTruthLayer.data)) {
		const layerSpec = newLayer(groundTruthLayer, 'point');
		const encoding = layerSpec.layer[0].encoding;

		encoding.color.scale.range = options.colorscheme
			? structuredClone(options.colorscheme).reverse()
			: CATEGORICAL_SCHEME;

		if (options.legend === true) {
			encoding.color.legend = {
				...legendProperties
			};

			if (labelExpr.length > 0) {
				encoding.color.legend.labelExpr = labelExpr;
			}
		}
		spec.layer.push(layerSpec);
	}
	return spec;
}

/**
 * e.g. [{variable1: [1, 2, 3], variable2: [4, 5, 6]}, ...] where each item in the variable array is a sample value. Sample values must be sorted in ascending order.
 */
export type GroupedDataArray = Record<string, number[]>[];

const buildQuantileChartData = (data: GroupedDataArray, selectVariables: string[], quantiles: number[]) => {
	const result: {
		x: number;
		lower: number;
		upper: number;
		variable: string;
		quantile: number;
	}[] = [];
	data.forEach((d, index) => {
		selectVariables.forEach((variable) => {
			const values = d[variable] ?? [];
			[...quantiles]
				.sort((a, b) => b - a) // Sort in descending order so that data with higher quantiles are drawn first
				.forEach((q) => {
					result.push({
						x: index,
						lower: d3.quantile(values, 1 - q) ?? NaN,
						upper: d3.quantile(values, q) ?? NaN,
						variable,
						quantile: q
					});
				});
		});
	});
	return result;
};

export function createQuantilesForecastChart(
	data: GroupedDataArray,
	variables: string[],
	quantiles: number[],
	options: ForecastChartOptions
) {
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

	const translationMap = options.translationMap;
	let labelExpr = '';
	let varDisplayNameExpr = '';
	if (translationMap) {
		Object.keys(translationMap)
			.filter((key) => variables.includes(key))
			.forEach((key) => {
				labelExpr += `datum.value === '${key}' ? '${translationMap[key]}' : `;
				varDisplayNameExpr += `datum.variable === '${key}' ? '${translationMap[key]}' : `;
			});
		labelExpr += " 'other'";
		varDisplayNameExpr += " 'other'";
	}

	const isCompact = options.width < 200;

	const legendProperties = {
		title: null,
		padding: { value: 0 },
		strokeColor: null,
		orient: 'top',
		direction: isCompact ? 'vertical' : 'horizontal',
		symbolStrokeWidth: isCompact ? 2 : 4,
		symbolSize: 200,
		labelFontSize: isCompact ? 8 : 12,
		labelOffset: isCompact ? 2 : 4,
		labelLimit: isCompact ? 50 : 150,
		...options.legendProperties
	};

	const yScale = { type: options.scale === 'log' ? 'symlog' : 'linear' };

	const spec: any = {
		$schema: VEGALITE_SCHEMA,
		title: titleObj,
		description: '',
		width: options.width,
		height: options.height,
		autosize: {
			type: options.autosize || AUTOSIZE.FIT_X
		},
		config: {
			font: globalFont,
			legend: {
				layout: {
					direction: legendProperties.direction,
					anchor: 'start'
				}
			}
		},
		data: { values: buildQuantileChartData(data, variables, quantiles) },
		transform: [
			{
				calculate: varDisplayNameExpr,
				as: 'varDisplayName'
			}
		],
		layer: [
			{
				mark: {
					type: 'errorband',
					extent: 'ci',
					borders: true
				},
				encoding: {
					x: { field: 'x', type: 'quantitative', axis: { ...xaxis } },
					y: { field: 'lower', type: 'quantitative', axis: { ...yaxis }, scale: yScale },
					y2: { field: 'upper', type: 'quantitative' },
					color: {
						field: 'variable',
						type: 'nominal',
						scale: {
							domain: variables,
							range: options.colorscheme || CATEGORICAL_SCHEME
						},
						legend: options.legend
							? {
									...legendProperties,
									labelExpr: labelExpr.length && labelExpr
								}
							: false
					},
					opacity: {
						field: 'quantile',
						type: 'quantitative',
						scale: { domain: [0.5, 1], range: [1, 0.1] },
						legend: false
					},
					tooltip: [
						{ field: 'varDisplayName', title: ' ' },
						{ field: 'quantile', title: 'Quantile', format: '.0%' },
						{ field: 'lower', title: 'Lower Bound' },
						{ field: 'upper', title: 'Upper Bound' }
					]
				}
			}
		]
	};
	return spec;
}

/**
 * FIXME: The design calls for combinations of different types of charts
 * in the grid, which we don't know how to achieve currently with vegalite
 * */
export function createSimulateSensitivityScatter(samplingLayer: SensitivityChartLayer, options: ForecastChartOptions) {
	// Start building
	let calculateExpr = '';
	options.bins?.forEach((sampleIds, quantile) => {
		calculateExpr += `indexof([${sampleIds}], datum.sample_id) >= 0 ? '${quantile}' : `;
	});
	calculateExpr += '0';
	const spec: any = {
		$schema: VEGALITE_SCHEMA,
		description: '',
		repeat: {
			row: samplingLayer.inputVariables,
			column: samplingLayer.inputVariables
		},
		data: { values: samplingLayer.data },
		transform: [
			{
				calculate: calculateExpr,
				as: 'quantile'
			}
		],
		spec: {
			width: options.width,
			height: options.height,
			mark: { type: 'point', filled: true },
			encoding: {
				x: {
					field: { repeat: 'row' },
					type: 'quantitative',
					axis: {
						gridColor: '#EEE'
					},
					scale: {
						zero: false,
						nice: false
					}
				},
				y: {
					field: { repeat: 'column' },
					type: 'quantitative',
					axis: {
						gridColor: '#EEE'
					},
					scale: {
						zero: false,
						nice: false
					}
				},
				color: {
					field: 'quantile',
					type: 'nominal',
					scale: {
						domain: options.bins ? Array.from(options.bins.keys()) : samplingLayer.outputVariable,
						range: options.colorscheme || CATEGORICAL_SCHEME
					},
					legend: null
				},
				detail: { field: 'sample_id', type: 'nominal' },
				size: { value: 80 }
			}
		}
	};

	return spec;
}

export function applyForecastChartAnnotations(chartSpec: any, annotations: ChartAnnotation[]) {
	if (isEmpty(annotations)) return chartSpec;
	const targetLayerIndex = 1; // Assume the target layer is the second layer which is the statistic layer
	const layerSpecs = annotations.map((a) => a.layerSpec);
	if (!chartSpec.layer[targetLayerIndex]) return chartSpec;
	chartSpec.layer[targetLayerIndex].layer.push(...layerSpecs);
	return chartSpec;
}

export function createForecastChartAnnotation(axis: 'x' | 'y', datum: number, label: string) {
	const layerSpec = {
		description: `At ${axis} ${datum}, add a label '${label}'.`,
		encoding: {
			[axis]: { datum }
		},
		layer: [
			{
				mark: {
					type: 'rule',
					strokeDash: [4, 4]
				}
			},
			{
				mark: {
					type: 'text',
					align: 'left',
					dx: 5,
					dy: -5
				},
				encoding: {
					text: { value: label }
				}
			}
		]
	};
	const annotation: ChartAnnotation = {
		id: uuidv4(),
		nodeId: '',
		outputId: '',
		chartId: '',
		layerSpec,
		llmGenerated: false,
		metadata: { axis, datum, label }
	};
	return annotation;
}

/// /////////////////////////////////////////////////////////////////////////////
// Optimize charts
/// /////////////////////////////////////////////////////////////////////////////

export function createSuccessCriteriaChart(
	riskResults: any,
	threshold: number,
	isMinimized: boolean,
	alpha: number,
	options: BaseChartOptions
): any {
	// FIXME: risk results can be null/undefined sometimes
	const data = riskResults.data;
	const risk = riskResults.risk;
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

	return {
		$schema: VEGALITE_SCHEMA,
		width: options.width,
		height: options.height,
		autosize: {
			type: AUTOSIZE.FIT_X
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

export function createInterventionChartMarkers(
	data: ReturnType<typeof flattenInterventionData>,
	options: InterventionMarkerOptions = { hideLabels: false, labelXOffset: 5 }
): any[] {
	const markerSpec = {
		data: { values: data },
		mark: { type: 'rule', strokeDash: [4, 4], color: 'black' },
		encoding: {
			x: { field: 'time', type: 'quantitative' }
		}
	};
	if (options.hideLabels) return [markerSpec];
	const labelSpec = {
		data: { values: data },
		mark: {
			type: 'text',
			align: 'left',
			angle: 90,
			dx: options.labelXOffset || 0 - 45,
			dy: -10,
			limit: 140,
			ellipsis: '...'
		},
		encoding: {
			x: { field: 'time', type: 'quantitative' },
			y: 0,
			text: { field: 'name', type: 'nominal' }
		}
	};

	return [markerSpec, labelSpec];
}

interface InterventionChartOptions extends Omit<BaseChartOptions, 'legend'> {
	hideLabels?: boolean;
}

export function createInterventionChart(
	interventions: ReturnType<typeof flattenInterventionData>,
	chartOptions: InterventionChartOptions
) {
	const titleObj = chartOptions.title
		? {
				text: chartOptions.title,
				anchor: 'start',
				subtitle: ' ',
				subtitlePadding: 4
			}
		: null;
	const spec: any = {
		$schema: VEGALITE_SCHEMA,
		width: chartOptions.width,
		title: titleObj,
		height: chartOptions.height,
		autosize: {
			type: AUTOSIZE.FIT_X
		},
		layer: []
	};
	if (!isEmpty(interventions)) {
		// markers
		createInterventionChartMarkers(interventions, { hideLabels: chartOptions.hideLabels }).forEach((marker) => {
			spec.layer.push(marker);
		});
		// chart
		spec.layer.push({
			data: { values: interventions },
			mark: 'point',
			encoding: {
				x: { field: 'time', type: 'quantitative', title: chartOptions.xAxisTitle },
				y: { field: 'value', type: 'quantitative', title: chartOptions.yAxisTitle }
			}
		});
	}
	return spec;
}

/// /////////////////////////////////////////////////////////////////////////////
// Funman charts
/// /////////////////////////////////////////////////////////////////////////////

enum FunmanChartLegend {
	Satisfactory = 'Satisfactory',
	Unsatisfactory = 'Unsatisfactory',
	Ambiguous = 'Ambiguous',
	ModelChecks = 'Model checks'
}

export function getBoundType(label: string): string {
	switch (label) {
		case 'true':
			return FunmanChartLegend.Satisfactory;
		case 'false':
			return FunmanChartLegend.Unsatisfactory;
		default:
			return FunmanChartLegend.Ambiguous;
	}
}

export function createFunmanStateChart(
	trajectories: any[],
	constraints: FunmanConstraintsResponse[],
	stateId: string,
	focusOnModelChecks: boolean,
	selectedBoxId: number = -1
) {
	if (isEmpty(trajectories)) return null;

	const threshold = 1e25;
	const globalFont = 'Figtree';

	const stateIdConstraints = constraints.filter((c) => c.variables.includes(stateId));
	// Find min/max values to set an appropriate viewing range for y-axis
	// Limit by threshold as very large numbers cause the chart to have NaN in the y domain
	const lowerBounds = stateIdConstraints.map((c) => {
		let lb = c.additive_bounds.lb;
		const ub = c.additive_bounds.ub;
		if (lb && ub && lb < -threshold) lb = -Math.abs(ub) * 0.5 + ub;
		return lb;
	});
	const upperBounds = stateIdConstraints.map((c) => {
		const lb = c.additive_bounds.lb;
		let ub = c.additive_bounds.ub;
		if (ub && lb && ub > threshold) ub = Math.abs(lb) * 0.5 + lb;
		return ub;
	});
	const yPoints = trajectories.map((t) => t.values[stateId]);

	const potentialMinYs = focusOnModelChecks && !isEmpty(lowerBounds) ? [...yPoints, ...lowerBounds] : yPoints;
	const potentialMaxYs = focusOnModelChecks && !isEmpty(upperBounds) ? [...yPoints, ...upperBounds] : yPoints;
	const minY = Math.floor(Math.min(...potentialMinYs));
	const maxY = Math.ceil(Math.max(...potentialMaxYs));

	const modelChecks = stateIdConstraints.map((c) => {
		let startY = c.additive_bounds.lb ?? minY;
		let endY = c.additive_bounds.ub ?? maxY;
		// Fallback to min/max if the bounds exceed the threshold
		if (startY < -threshold) startY = minY;
		if (endY > threshold) endY = maxY;
		return {
			legendItem: FunmanChartLegend.ModelChecks,
			startX: c.timepoints.lb,
			endX: c.timepoints.ub,
			startY,
			endY
		};
	});

	return {
		$schema: VEGALITE_SCHEMA,
		id: stateId,
		config: { font: globalFont },
		width: 600,
		height: 300,
		title: {
			text: `${stateId} (persons)`,
			anchor: 'start',
			frame: 'group',
			offset: 10,
			fontSize: 14
		},
		params: [
			{
				name: 'selectedBoxId',
				value: selectedBoxId
			}
		],
		layer: [
			{
				mark: {
					type: 'rect',
					clip: true,
					stroke: '#A4CEFF',
					strokeWidth: 1
				},
				data: { values: modelChecks },
				encoding: {
					x: { field: 'startX', type: 'quantitative' },
					x2: { field: 'endX', type: 'quantitative' },
					y: { field: 'startY', type: 'quantitative' },
					y2: { field: 'endY', type: 'quantitative' }
				}
			},
			{
				mark: {
					type: 'line',
					point: true
				},
				data: { values: trajectories },
				encoding: {
					x: { field: 'timepoint', type: 'quantitative' },
					y: { field: `values[${stateId}]`, type: 'quantitative' },
					opacity: {
						condition: {
							test: 'selectedBoxId == datum.boxId || selectedBoxId == -1', // -1 is the default value (shows all boxes)
							value: 1
						},
						value: 0.2
					}
				}
			}
		],
		encoding: {
			x: { title: 'Timepoints' },
			y: {
				title: `${stateId} (persons)`,
				scale: { domain: [minY, maxY] }
			},
			color: {
				field: 'legendItem',
				legend: { orient: 'top', direction: 'horizontal', title: null },
				scale: {
					domain: [
						FunmanChartLegend.Satisfactory,
						FunmanChartLegend.Unsatisfactory,
						FunmanChartLegend.Ambiguous,
						FunmanChartLegend.ModelChecks
					],
					range: ['#1B8073', '#FFAB00', '#CCC569', '#A4CEFF54'] // Specify colors for each legend item
				}
			},
			detail: { field: 'boxId' }
		}
	};
}

export function createFunmanParameterCharts(
	distributionParameters: { label: string; name: string; interval: FunmanInterval }[],
	boxes: FunmanBox[]
) {
	const parameterRanges: {
		boxId: number;
		parameterId: string;
		boundType: string;
		lb?: number;
		ub?: number;
		tick?: number;
	}[] = [];
	const distributionParameterIds: string[] = [];

	// Widest range (model configuration ranges)
	distributionParameters.forEach(({ name, interval }) => {
		parameterRanges.push({
			boxId: -1,
			parameterId: name,
			boundType: 'length',
			lb: interval.lb,
			ub: interval.ub
		});
		distributionParameterIds.push(name);
	});

	// Ranges determined by the true/false boxes
	boxes.forEach(({ boxId, label, parameters }) => {
		Object.keys(parameters).forEach((key) => {
			if (!distributionParameterIds.includes(key)) return;
			parameterRanges.push({
				boxId,
				parameterId: key,
				boundType: getBoundType(label),
				lb: parameters[key].lb,
				ub: parameters[key].ub,
				tick: parameters[key].point
			});
		});
	});

	const globalFont = 'Figtree';
	return {
		$schema: VEGALITE_SCHEMA,
		config: {
			font: globalFont,
			tick: { thickness: 2 }
		},
		width: 600,
		height: 50, // Height per facet
		data: {
			values: parameterRanges
		},
		// This determines the range of the whole x-axis
		transform: [
			{
				joinaggregate: [
					{ field: 'lb', op: 'min', as: 'minX' },
					{ field: 'ub', op: 'max', as: 'maxX' }
				],
				groupby: ['parameterId']
			}
		],
		params: [
			{ name: 'minX', expr: 'minX' },
			{ name: 'maxX', expr: 'maxX' }
		],
		facet: {
			row: {
				field: 'parameterId',
				type: 'nominal',
				header: { labelAngle: 0, title: '', labelAlign: 'left' }
			}
		},
		resolve: {
			scale: {
				x: 'independent' // Ensure each facet has its own x-axis scale
			}
		},
		spec: {
			width: 600,
			layer: [
				{
					mark: {
						type: 'bar', // Use a bar to represent ranges
						opacity: 0.4 // FIXME: This opacity shouldn't be applied to the legend
					},
					encoding: {
						x: {
							field: 'lb',
							type: 'quantitative',
							scale: {
								zero: false,
								// Doesn't work with regular domain setting
								domainMin: { expr: 'minX' },
								domainMax: { expr: 'maxX' }
							},
							title: null
						},
						x2: {
							field: 'ub'
						},
						color: {
							condition: {
								param: 'tickSelection',
								field: 'boundType',
								type: 'nominal',
								legend: { orient: 'top', direction: 'horizontal', title: null },
								scale: {
									domain: [
										FunmanChartLegend.Satisfactory,
										FunmanChartLegend.Unsatisfactory,
										FunmanChartLegend.Ambiguous
									],
									range: ['#1B8073', '#FFAB00', '#CCC569']
								}
							},
							value: 'rgba(190,190,190,0.1)'
						}
					}
				},
				{
					mark: {
						type: 'tick',
						thickness: 4,
						stroke: 'white',
						strokeWidth: 1 // Optional: Adjust the border width
					},
					params: [
						{
							name: 'tickSelection',
							select: { type: 'point', fields: ['boxId'] },
							on: [
								{
									events: 'click',
									update: 'datum.boxId'
								}
							]
						}
					],
					encoding: {
						x: {
							field: 'tick',
							type: 'quantitative',
							title: null
						},
						color: { field: 'boundType' },
						size: {
							condition: { param: 'tickSelection', value: 25, empty: false },
							value: 15
						}
					}
				},
				// Selected bound square brackets for lb, ub
				{
					mark: {
						type: 'text',
						size: 30,
						dy: 2
					},
					encoding: {
						x: {
							field: 'lb',
							type: 'quantitative'
						},
						text: { value: '[' },
						opacity: {
							condition: {
								test: 'tickSelection.boxId == datum.boxId',
								value: 1
							},
							value: 0
						}
					}
				},
				{
					mark: {
						type: 'text',
						size: 30,
						dy: 2
					},
					encoding: {
						x: {
							field: 'ub',
							type: 'quantitative'
						},
						text: { value: ']' },
						opacity: {
							condition: {
								test: 'tickSelection.boxId == datum.boxId',
								value: 1
							},
							value: 0
						}
					}
				}
			]
		}
	};
}
