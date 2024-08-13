import API from '@/api/api';
import { b64DecodeUnicode } from '@/utils/binary';
import { percentile } from '@/utils/math';
import { isEmpty } from 'lodash';
import { VisualizationSpec } from 'vega-embed';
import { v4 as uuidv4 } from 'uuid';

const VEGALITE_SCHEMA = 'https://vega.github.io/schema/vega-lite/v5.json';

export const CATEGORICAL_SCHEME = ['#1B8073', '#6495E8', '#8F69B9', '#D67DBF', '#E18547', '#D2C446', '#84594D'];

export const NUMBER_FORMAT = '.3~s';
export const LABEL_EXPR = `
datum.value > -1 && datum.value < 1 ? format(datum.value, '.3~f') : format(datum.value, '${NUMBER_FORMAT}')
`;

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

export interface ForecastChartAnnotation {
	id: string;
	layerSpec: any;
	isLLMGenerated: boolean;
	metadata: any;
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
	};
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

	const createLayers = (opts) => {
		const colorScale = {
			domain: opts.variables.map((v) => v.label ?? v.field),
			range: opts.variables.map((v) => v.color)
		};
		const bin = { maxbins: maxBins };
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

	const spec: VisualizationSpec = {
		$schema: VEGALITE_SCHEMA,
		title: titleObj as any,
		width: options.width,
		height: options.height,
		autosize: { type: 'fit' },
		data: { values: dataset },
		layer: createLayers(options),
		config: {
			font: globalFont
		}
	};
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
	options: ForecastChartOptions,
	summaryLayer?: ForecastChartLayer | null
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
	yaxis.labelExpr = LABEL_EXPR;

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
		width: options.width - 25,
		height: options.height - 25,
		padding: { left: 20, right: 5, top: 5, bottom: 20 },
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
			data: Array.isArray(layer.data) ? { values: layer.data } : layer.data,
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

	// Build summary layer
	if (summaryLayer && !isEmpty(summaryLayer.variables)) {
		const layerSpec = newLayer(summaryLayer, 'line');
		const encoding = layerSpec.layer[0].encoding;
		Object.assign(encoding, {
			opacity: { value: 1.0 },
			strokeWidth: { value: 2 }
		});

		// if (options.legend === true) {
		// 	encoding.color.legend = {
		// 		...legendProperties
		// 	};

		// 	if (labelExpr.length > 0) {
		// 		encoding.color.legend.labelExpr = labelExpr;
		// 	}
		// }

		spec.layer.push(layerSpec);
	}

	return spec;
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
	const annotation: ForecastChartAnnotation = {
		id: uuidv4(),
		layerSpec,
		isLLMGenerated: false,
		metadata: { axis, datum, label }
	};
	return annotation;
}

export async function generateForecastChartAnnotation(
	request: string,
	timeField: string,
	variables: string[],
	axisTitle: { x: string; y: string }
) {
	const prompt = `
	  You are an agent who is an expert in Vega-Lite chart specs. Provide a Vega-Lite layer JSON object for the annotation that can be added to an existing chart spec to satisfy the provided user request.

    - The Vega-Lite schema version you must use is https://vega.github.io/schema/vega-lite/v5.json.
    - Assume that you don’t know the exact data points from the data.
    - You must give me the single layer object that renders all the necessary drawing objects, including multiple layers within the top layer object if needed.
    - When adding a label, also draw a constant line perpendicular to the axis to which you are adding the label.

    Assuming you are adding the annotations to the following chart spec,
    ---- Example Chart Spec Start -----
    {
      "data": {"url": "data/samples.csv"},
      "transform": [
        {
          "fold": ["price", "cost", "tax", "profit"],
          "as": ["variableField", "valueField"]
        }
      ],
      "layer": [
        {
          "mark": "line",
          "encoding": {
            "x": {"field": "date", "type": "quantitative", "axis": {"title": "Day"}},
            "y": {"field": "valueField", "type": "quantitative", "axis": {"title": "Dollars"}}
          }
        }
      ]
    }
    ---- Example Chart Spec End -----
    Here are some example requests and the answers:

    Request:
    At day 200, add a label 'important'
    Answer:
    {
      "description": "At day 200, add a label 'important'",
      "layer": [
        {
          "mark": {
            "type": "rule",
            "strokeDash": [4, 4]
          },
					"encoding": {
						"x": { "datum": 200, "axis": { "title": ""} }
					}
        },
        {
          "mark": {
            "type": "text",
            "align": "left",
            "dx": 5,
            "dy": -5
          },
          "encoding": {
						"x": { "datum": 200, "axis": { "title": ""} }
            "text": {"value": "important"}
          }
        }
      ]
    }

    Request:
    Add a label 'expensive' at price 20
    Answer:
    {
      "description": "Add a label 'expensive' at price 20",
      "layer": [
        {
          "mark": {
            "type": "rule",
            "strokeDash": [4, 4]
          },
          "encoding": {
            "y": { "datum": 20, "axis": { "title": ""} }
          }
        },
        {
          "mark": {
            "type": "text",
            "align": "left",
            "dx": 5,
            "dy": -5
          },
          "encoding": {
            "y": { "datum": 20, "axis": { "title": ""} },
            "text": {"value": "expensive"}
          }
        }
      ]
    }

    Request:
    Add a vertical line for the day where the price exceeds 100.
    Answer:
    {
      "description": "Add a vertical line for the day where the price exceeds 100.",
      "transform": [
        {"filter": "datum.valueField > 100"},
        {"aggregate": [{"op": "min", "field": "date", "as": "min_date"}]}
      ],
      "layer": [
        {
          "mark": {
            "type": "rule",
            "strokeDash": [4, 4]
          },
          "encoding": {
            "x": {"field": "min_date", "type": "quantitative", "axis": { "title": ""}}
          }
        },
        {
          "mark": {
            "type": "text",
            "align": "left",
            "dx": 5,
            "dy": -10
          },
          "encoding": {
            "x": {"field": "min_date", "type": "quantitative", "axis": { "title": ""}},
            "text": {"value": "Price > 100"}
          }
        }
      ]
    }

    Here is the information of the existing target chart spec where you need to add the annotations:
    - The existing chart follows a similar pattern as the above Example Chart Spec like:
        {
          ...
          "transform": [
            {
              "fold": ${JSON.stringify(variables)},
              "as": ["variableField', "valueField"]
            }
          ],
          "layer": [
            {
              ...
              "encoding": {
                "x": {"field": "${timeField}", "type": "quantitative", "axis": {"title": "${axisTitle.x}"}},
                "y": {"field": "valueField", "type": "quantitative", "axis": {"title": "${axisTitle.y}"}}
              }
            }
            ...
          ]
        }
    - Assume all unknown variables except the time field are for the y-axis and are renamed to the valueField.

     Give me the layer object to be added to the existing chart spec based on the following user request.
		 Please return only a JSON object as a response. Make sure to return plain JSON object that can be parsed as JSON. Do not include code block.

		Request:
    ${request}
    Answer
    {
	`;
	// FIXME: Use dedicated endpoint for annotation generation that's configured with JSON response_format instead of using the summary endpoint which is for text output
	const { data } = await API.post(`/gollm/generate-summary?mode=SYNC`, prompt, {
		headers: {
			'Content-Type': 'application/json'
		}
	});
	const str = b64DecodeUnicode(data.output);
	const result = JSON.parse(str);
	const layerSpec = JSON.parse(result.response);
	const annotation: ForecastChartAnnotation = {
		id: uuidv4(),
		layerSpec,
		isLLMGenerated: true,
		metadata: { llmRequest: request }
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
	yaxis.labelExpr = LABEL_EXPR;

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

export function createInterventionChartMarkers(data: { name: string; value: number; time: number }[]): any[] {
	const markerSpec = {
		data: { values: data },
		mark: { type: 'rule', strokeDash: [4, 4], color: 'black' },
		encoding: {
			x: { field: 'time', type: 'quantitative' }
		}
	};

	const labelSpec = {
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

	return [markerSpec, labelSpec];
}

export function createInterventionChart(interventionsData: { name: string; value: number; time: number }[]) {
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
}
