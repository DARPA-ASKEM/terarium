<template>
	<div style="padding: 2rem; display: flex; flex-direction: row">
		<vega-chart
			:interval-selection-signal-names="['brush']"
			:visualization-spec="spec"
			@chart-click="handleChartClick($event)"
			@update-interval-selection="handleIntervalSelect"
		/>
		<vega-chart :visualization-spec="spec2" />
	</div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import VegaChart from './VegaChart.vue';

const rand = (v: number) => Math.round(Math.random() * v);

const handleChartClick = (event: any) => {
	console.log('!!', event);
};

const handleIntervalSelect = (name: any, valueRange: any) => {
	console.log(name, valueRange);
};

const numPoints = 10;
const numSamples = 20;
const valueRange = 20;
const trueValues: any[] = [];
const dataChart1: any[] = [];
const dataChart2: any[] = [];

for (let j = 0; j < numPoints; j++) {
	trueValues.push(rand(valueRange));
}

for (let i = 0; i < numSamples; i++) {
	let error = 0;
	for (let j = 0; j < numPoints; j++) {
		const v = rand(valueRange);
		dataChart2.push({ sample: i, timestep: j, value: v });
		error += Math.abs(trueValues[j] - v);
	}
	dataChart1.push({ sample: i, error: error });
}

const spec = ref<any>({
	$schema: 'https://vega.github.io/schema/vega-lite/v5.json',
	data: { values: dataChart1 },
	transform: [
		{
			aggregate: [{ op: 'count', field: '*', as: 'count' }],
			groupby: ['error']
		},
		{ calculate: 'random()', as: 'jitter' }
	],
	vconcat: [
		{
			mark: 'area',
			encoding: {
				x: {
					field: 'error',
					type: 'quantitative'
				},
				y: {
					field: 'count',
					type: 'quantitative'
				}
			}
		},
		{
			mark: 'point',
			encoding: {
				data: { value: dataChart1 },
				color: { value: '#f80' },
				opacity: { value: 0.8 },
				size: { value: 15 },
				x: { field: 'error', type: 'quantitative', title: '' },
				y: { field: 'jitter', type: 'quantitative', title: '' }
			},
			params: [{ name: 'brush', select: 'interval' }]
		}
	]
});

const spec2 = ref<any>({
	$schema: 'https://vega.github.io/schema/vega-lite/v5.json',
	description: 'Stock prices of 5 Tech Companies over Time.',
	// data: { url: 'https://vega.github.io/vega-lite/data/stocks.csv' },
	data: { values: dataChart2 },
	mark: 'line',
	encoding: {
		x: { field: 'timestep', type: 'quantitative' },
		y: { field: 'value', type: 'quantitative' },
		color: {
			field: 'sample',
			type: 'nominal',
			legend: null
		}
	}
});

/*
const spec = ref<any>({
	$schema: 'https://vega.github.io/schema/vega-lite/v5.json',
	data: { url: 'https://vega.github.io/editor/data/unemployment-across-industries.json' },
	transform: [{ calculate: 'random()', as: 'jitter' }],
	vconcat: [
		{
			width: 500,
			height: 100,
			mark: 'area',
			transform: [{ filter: { param: 'brush' } }],
			encoding: {
				color: { value: '#0d4' },
				x: {
					timeUnit: 'yearmonth',
					field: 'date',
					axis: { format: '%Y' }
				},
				y: {
					aggregate: 'sum',
					field: 'count',
					title: 'count'
				}
			}
		},
		{
			width: 500,
			height: 50,
			mark: 'boxplot',
			transform: [{ filter: { param: 'brush' } }],
			encoding: {
				color: { value: '#a52' },
				x: {
					title: '',
					field: 'count',
					type: 'quantitative',
					scale: { zero: false }
				}
			}
		},
		{
			width: 500,
			height: 100,
			mark: 'point',
			encoding: {
				color: { value: '#f80' },
				opacity: { value: 0.4 },
				size: { value: 5 },
				x: { field: 'count', type: 'quantitative', title: '' },
				y: { field: 'jitter', type: 'quantitative', title: '' }
			},
			params: [{ name: 'brush', select: 'interval' }]
		}
	]
});
*/
</script>
