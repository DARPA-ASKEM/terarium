<template>
	<div style="padding: 2rem">
		<vega-chart :visualization-spec="spec" @chart-click="handleChartClick($event)" />
	</div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import VegaChart from './VegaChart.vue';

const handleChartClick = (event: any) => {
	console.log('!!', event);
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
			}
		}
	]
});
</script>
