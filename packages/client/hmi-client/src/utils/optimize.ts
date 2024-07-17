import { ForecastChartOptions } from '@/services/charts';
import { mean } from 'lodash';

const binCount = 5;
const VEGALITE_SCHEMA = 'https://vega.github.io/schema/vega-lite/v5.json';

function formatSuccessChartData(
	riskResults: any,
	targetVariable: string,
	threshold: number,
	isMinimized: boolean
) {
	const targetState = `${targetVariable}_state`;
	const data = riskResults[targetState]?.qoi || [];

	const minValue = Math.min(...data);
	const maxValue = Math.max(...data);
	const stepSize = (maxValue - minValue) / binCount;
	const bins: { range: string; count: number; tag: 'in' | 'out' }[] = [];
	const binLabels: string[] = [];
	for (let i = binCount; i > 0; i--) {
		const rangeStart = minValue + stepSize * (i - 1);
		const rangeEnd = minValue + stepSize * i;
		let tag;
		if (isMinimized) {
			tag = rangeEnd < threshold ? 'in' : 'out';
		} else {
			tag = rangeStart > threshold ? 'in' : 'out';
		}

		bins.push({
			range: `${rangeStart.toFixed(4)}-${rangeEnd.toFixed(4)}`,
			count: 0,
			tag
		});
		binLabels.push(`${rangeStart.toFixed(4)} - ${rangeEnd.toFixed(4)}`);
	}

	const toBinIndex = (value: number) => {
		if (value < minValue || value > maxValue) return -1;
		const index = binCount - 1 - Math.abs(Math.floor((value - minValue) / stepSize));
		return index;
	};

	const avgArray: number[] = [];

	// Fill bins:
	data.forEach((ele) => {
		const index = toBinIndex(ele);
		if (index !== -1) {
			bins[index].count += 1;
			if (bins[index].tag === 'out') {
				avgArray.push(ele);
			}
		}
	});

	const avg = mean(avgArray);

	return { data: bins, avg };
}

export function createOptimizeChart(
	riskResults: any,
	targetVariable: string,
	threshold: number,
	isMinimized: boolean
): any {
	const { data } = formatSuccessChartData(riskResults, targetVariable, threshold, isMinimized);

	return {
		$schema: VEGALITE_SCHEMA,
		width: 400,
		height: 400,
		data: {
			values: data
		},
		transform: [
			{
				calculate: 'split(datum.range, "-")[0]',
				as: 'start'
			},
			{
				calculate: 'split(datum.range, "-")[1]',
				as: 'end'
			}
		],
		layer: [
			{
				mark: {
					type: 'bar',
					stroke: 'black',
					tooltip: true,
					interpolate: 'linear'
				},
				encoding: {
					y: {
						field: 'start',
						type: 'quantitative',
						title: 'Min value at all times'
					},
					y2: { field: 'end' },
					x: {
						aggregate: 'sum',
						field: 'count',
						type: 'quantitative',
						title: 'Count'
					},
					color: {
						field: 'tag',
						type: 'nominal',
						scale: {
							domain: ['out', 'in'],
							range: ['#FFAB00', '#1B8073']
						}
					}
				}
			},
			{
				mark: { type: 'rule', strokeDash: [4, 4], color: 'black' },
				encoding: {
					y: { datum: +threshold }
				}
			},
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
			}
		],
		config: {
			legend: { title: null, orient: 'top', direction: 'horizontal' }
		}
	};
}

function createInterventionChartMarkers(data: { name: string; value: number; time: number }[]) {
	return data.map((ele) => ({
		data: [{}], // Dummy data to ensure the layer is rendered
		mark: { type: 'rule', strokeDash: [4, 4], color: 'black' },
		encoding: {
			x: { datum: ele.time }
		}
	}));
}

function createStatisticLayer(
	data: Record<string, any>[],
	xaxis,
	yaxis,
	options: ForecastChartOptions,
	isPreStatistic: boolean
) {
	const statisticalVariables = options.statisticalVariables?.map((d) => d);
	const tooltipContent = statisticalVariables?.map((d) => ({
		field: d,
		type: 'quantitative',
		format: '.4f'
	}));

	const layerSpec: any = {
		mark: { type: 'line' },
		data: { values: data },
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
					range: [isPreStatistic ? '#AAB3C6' : '#1B8073']
				},
				legend: options.legend
					? {
							title: null,
							orient: 'top',
							direction: 'horizontal',
							labelExpr: isPreStatistic
								? 'datum.value ? "Before Optimization" : ""'
								: 'datum.value ? "After Optimization" : ""'
						}
					: null
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
	options: ForecastChartOptions,
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
		autosize: {
			type: 'fit'
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
