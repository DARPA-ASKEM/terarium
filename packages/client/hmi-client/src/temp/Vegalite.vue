<template>
	<vega-chart :visualization-spec="spec" />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import VegaChart from './VegaChart.vue';

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
				x: { field: 'count', type: 'quantitative' },
				y: { field: 'jitter', type: 'quantitative' }
			}
		}
	]
});
</script>
