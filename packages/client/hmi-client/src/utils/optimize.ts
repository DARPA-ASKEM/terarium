import { mean } from 'lodash';

const binCount = 5;

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
		$schema: 'https://vega.github.io/schema/vega-lite/v5.json',
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

export function createInterventionsChart(
	data: { name: string; value: number; time: number; phase: string }[],
	endTime: number
): any {
	const interventionName = data[0].name;
	return {
		$schema: 'https://vega.github.io/schema/vega-lite/v5.json',
		description: 'A line chart with annotations.',
		data: {
			values: data
		},
		config: {
			legend: { title: null, orient: 'top', direction: 'horizontal' }
		},
		layer: [
			{
				mark: { type: 'line', interpolate: 'step-after' },
				width: 400,
				height: 400,
				encoding: {
					x: {
						field: 'time',
						type: 'quantitative',
						axis: { title: 'Time (days)', grid: false },
						scale: { domain: [0, endTime] }
					},
					y: {
						field: 'value',
						type: 'quantitative',
						axis: {
							title: interventionName,
							grid: true,
							gridColor: { condition: { test: 'datum.value === 0', value: 'black' }, value: null }
						},
						scale: { padding: 10 }
					},
					color: {
						field: 'phase',
						type: 'nominal',
						scale: { range: ['#a3a3a3', '#1B8073'] },
						legend: { title: null },
						sort: ['Before optimization', 'After optimization']
					}
				}
			},
			...createInterventionChartMarkers(data)
		]
	};
}

function createInterventionChartMarkers(
	data: { name: string; value: number; time: number; phase: string }[]
) {
	return data.map((ele) => ({
		mark: { type: 'rule', strokeDash: [4, 4], color: 'black' },
		encoding: {
			x: { datum: ele.time }
		}
	}));
}
