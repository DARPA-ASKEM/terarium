import { percentile } from '@/utils/math';
import { isEmpty, pick } from 'lodash';
import { VisualizationSpec } from 'vega-embed';
import { v4 as uuidv4 } from 'uuid';
import type { ChartAnnotation, FunmanInterval } from '@/types/Types';
import { flattenInterventionData } from './intervention-policy';
import type { ProcessedFunmanResult, FunmanBox, FunmanConstraintsResponse } from './models/funman-service';

const VEGALITE_SCHEMA = 'https://vega.github.io/schema/vega-lite/v5.json';

export const CATEGORICAL_SCHEME = ['#1B8073', '#6495E8', '#8F69B9', '#D67DBF', '#E18547', '#D2C446', '#84594D'];

interface BaseChartOptions {
	title?: string;
	width: number;
	height: number;
	xAxisTitle: string;
	yAxisTitle: string;
	legend?: boolean;
}
export interface ForecastChartOptions extends BaseChartOptions {
	translationMap?: Record<string, string>;
	colorscheme?: string[];
}

export interface ForecastChartLayer {
	data: Record<string, any>[] | { name: string } | { url: string };
	variables: string[];
	timeField: string;
	groupField?: string;
}

export interface HistogramChartOptions extends BaseChartOptions {
	maxBins?: number;
	variables: { field: string; label?: string; width: number; color: string }[];
}

export interface ErrorChartOptions extends Omit<BaseChartOptions, 'height' | 'yAxisTitle' | 'legend'> {
	height?: number;
	areaChartHeight?: number;
	boxPlotHeight?: number;
	variables: { field: string; label?: string }[];
}

export function createErrorChart(dataset: Record<string, any>[], options: ErrorChartOptions) {
	const axisColor = '#EEE';
	const labelColor = '#667085';
	const labelFontWeight = 'normal';
	const globalFont = 'Figtree';

	const areaChartColor = '#1B8073';
	const dotColor = '#67B5AC';
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
							axis: { grid: true, labels: true, orient: 'left', offset: 5 }
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
		labelOffset: 4
	};

	const spec: VisualizationSpec = {
		$schema: VEGALITE_SCHEMA,
		title: titleObj as any,
		width: options.width,
		height: options.height,
		autosize: { type: 'fit' },
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
			layer: [
				{
					mark: { type: markType },
					encoding
				}
			]
		} as any;
	};

	// Build sample layer
	if (samplingLayer && !isEmpty(samplingLayer.variables)) {
		const layerSpec = newLayer(samplingLayer, 'line');
		const encoding = layerSpec.layer[0].encoding;
		Object.assign(encoding, {
			detail: { field: samplingLayer.groupField, type: 'nominal' },
			strokeWidth: { value: 1 },
			opacity: { value: 0.1 }
		});

		spec.layer.push(layerSpec);
	}

	// Build statistical layer
	if (statisticsLayer && !isEmpty(statisticsLayer.variables)) {
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
			tooltip: [{ field: statisticsLayer.timeField, type: 'quantitative' }, ...(tooltipContent || [])]
		});
		layerSpec.layer.push(tooltipSubLayer);

		spec.layer.push(layerSpec);
	}

	// Build ground truth layer
	if (groundTruthLayer && !isEmpty(groundTruthLayer.variables)) {
		const layerSpec = newLayer(groundTruthLayer, 'point');
		const encoding = layerSpec.layer[0].encoding;

		// FIXME: variables not aligned, set unique color for now
		encoding.color.scale.range = ['#1B8073'];
		// encoding.color.scale.range = options.colorscheme || CATEGORICAL_SCHEME;

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

export function applyForecastChartAnnotations(chartSpec: any, annotations: ChartAnnotation[]) {
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
	targetVariable: string,
	threshold: number,
	isMinimized: boolean,
	alpha: number,
	options: BaseChartOptions
): any {
	// FIXME: risk results can be null/undefined sometimes
	const data = riskResults?.[targetVariable]?.qoi || [];
	const risk = riskResults?.[targetVariable]?.risk?.[0] || 0;
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

export function createInterventionChartMarkers(
	data: ReturnType<typeof flattenInterventionData>,
	hideLabels = false,
	labelXOffset = 5
): any[] {
	const markerSpec = {
		data: { values: data },
		mark: { type: 'rule', strokeDash: [4, 4], color: 'black' },
		encoding: {
			x: { field: 'time', type: 'quantitative' }
		}
	};
	if (hideLabels) return [markerSpec];
	const labelSpec = {
		data: { values: data },
		mark: {
			type: 'text',
			align: 'left',
			angle: 90,
			dx: labelXOffset,
			dy: -10
		},
		encoding: {
			x: { field: 'time', type: 'quantitative' },
			y: { field: 'value', type: 'quantitative' },
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
			type: 'fit-x'
		},
		layer: []
	};
	if (!isEmpty(interventions)) {
		// markers
		createInterventionChartMarkers(interventions, chartOptions.hideLabels).forEach((marker) => {
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

function getBoundType(label: string): string {
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
	data: ProcessedFunmanResult,
	constraints: FunmanConstraintsResponse[],
	stateId: string,
	focusOnModelChecks: boolean
) {
	if (isEmpty(data.trajs)) return null;

	const globalFont = 'Figtree';

	const boxLines = data.trajs.map((traj) => {
		const legendItem = getBoundType(traj.label);
		return { timepoints: traj.timestep, value: traj[stateId], legendItem };
	});

	// Find min/max values to set an appropriate viewing range for y-axis
	const minY = Math.floor(Math.min(...boxLines.map((d) => d.value)));
	const maxY = Math.ceil(Math.max(...boxLines.map((d) => d.value)));

	// Show checks for the selected state
	const stateIdConstraints = constraints.filter((c) => c.variables.includes(stateId));
	const modelChecks = stateIdConstraints.map((c) => ({
		legendItem: FunmanChartLegend.ModelChecks,
		startX: c.timepoints.lb,
		endX: c.timepoints.ub,
		// If the interval bounds are within the min/max values of the line plot use them, otherwise use the min/max values
		startY: focusOnModelChecks ? c.additive_bounds.lb : Math.max(c.additive_bounds.lb ?? minY, minY),
		endY: focusOnModelChecks ? c.additive_bounds.ub : Math.min(c.additive_bounds.ub ?? maxY, maxY)
	}));

	return {
		$schema: VEGALITE_SCHEMA,
		config: { font: globalFont },
		width: 600,
		height: 300,
		layer: [
			{
				mark: {
					type: 'rect',
					clip: true
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
				data: { values: boxLines },
				encoding: {
					x: { field: 'timepoints', type: 'quantitative' },
					y: { field: 'value', type: 'quantitative' }
				}
			}
		],
		encoding: {
			x: { title: 'Timepoints' },
			y: {
				title: `${stateId} (persons)`,
				scale: focusOnModelChecks ? {} : { domain: [minY, maxY] }
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
			}
		}
	};
}

export function createFunmanParameterCharts(
	distributionParameters: { label: string; name: string; interval: FunmanInterval }[],
	boxes: FunmanBox[]
) {
	const parameterRanges: { parameterId: string; boundType: string; lb?: number; ub?: number; tick?: number }[] = [];
	const distributionParameterIds: string[] = [];

	// Widest range (model configuration ranges)
	distributionParameters.forEach(({ name, interval }) => {
		parameterRanges.push({
			parameterId: name,
			boundType: 'length',
			lb: interval.lb,
			ub: interval.ub
		});
		distributionParameterIds.push(name);
	});

	// Ranges determined by the true/false boxes
	boxes.forEach(({ label, parameters }) => {
		Object.keys(parameters).forEach((key) => {
			if (!distributionParameterIds.includes(key)) return;
			parameterRanges.push({
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
								// FIXME: If the domain is something like lb: 0.002, ub: 0.002 (same numbers close to 0), the only number on the number line will be 0
							},
							title: null
						},
						x2: {
							field: 'ub'
						},
						color: {
							field: 'boundType',
							type: 'nominal',
							legend: { orient: 'top', direction: 'horizontal', title: null },
							scale: {
								domain: [FunmanChartLegend.Satisfactory, FunmanChartLegend.Unsatisfactory, FunmanChartLegend.Ambiguous],
								range: ['#1B8073', '#FFAB00', '#CCC569']
							}
						}
					}
				},
				{
					mark: {
						type: 'tick',
						size: 20
					},
					encoding: {
						x: {
							field: 'tick',
							type: 'quantitative',
							title: null
						}
					}
				}
			]
		}
	};
}
