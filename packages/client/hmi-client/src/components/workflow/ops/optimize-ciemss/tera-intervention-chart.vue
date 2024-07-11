<template>
	<vega-chart :visualization-spec="lineSpec" />
</template>

<script setup lang="ts">
import VegaChart from '@/components/widgets/VegaChart.vue';
import { computed } from 'vue';

const props = defineProps<{
	data: { name: string; value: number; time: number; phase: string }[];
	endTime: number;
	size?: { width: number; height: number };
}>();

const interventionName = computed(() => props.data[0]?.name);

const interventionMarks = computed<any>(() =>
	props.data.map((ele) => ({
		mark: { type: 'rule', strokeDash: [4, 4], color: 'black' },
		encoding: {
			x: { datum: ele.time }
		}
	}))
);

const lineSpec = computed<any>(() => ({
	$schema: 'https://vega.github.io/schema/vega-lite/v5.json',
	description: 'A line chart with annotations.',
	data: {
		values: props.data
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
					scale: { domain: [0, props.endTime] }
				},
				y: {
					field: 'value',
					type: 'quantitative',
					axis: {
						title: interventionName.value,
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
		...interventionMarks.value
	]
}));
</script>
