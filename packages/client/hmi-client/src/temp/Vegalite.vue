<template>
	<div style="padding: 2rem">
		<vega-chart
			:interval-selection-signal-names="['brush']"
			:visualization-spec="spec"
			@chart-click="handleChartClick($event)"
			@update-interval-selection="handleIntervalSelect"
		/>
	</div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import VegaChart from './VegaChart.vue';

const handleChartClick = (event: any) => {
	console.log('!!', event);
};

const handleIntervalSelect = (name: any, valueRange: any) => {
	console.log(name, valueRange);
};

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
</script>
