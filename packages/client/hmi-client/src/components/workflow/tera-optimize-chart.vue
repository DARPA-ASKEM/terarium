<template>
	<div class="row">
		<vega-chart v-if="!isEmpty(spec)" :visualization-spec="spec" />
	</div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

import { isEmpty, mean } from 'lodash';
import VegaChart from '../widgets/VegaChart.vue';

const props = defineProps<{
	riskResults: any;
	targetVariable?: string;
	size?: { width: number; height: number };
	threshold: number;
	isMinimized: boolean;
}>();

const spec = computed<any>(() => {
	const { data } = getChartData(qoiData.value);
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
					y: { datum: +props.threshold }
				}
			},
			{
				mark: {
					type: 'text',
					align: 'left',
					text: `Threshold = ${props.threshold}`,
					baseline: 'line-bottom'
				},
				encoding: {
					y: { datum: +props.threshold }
				}
			}
		],
		config: {
			legend: { title: null, orient: 'top', direction: 'horizontal' }
		}
	};
});

const binCount = 5;

const getChartData = (data: number[]) => {
	const minValue = Math.min(...data);
	const maxValue = Math.max(...data);
	const stepSize = (maxValue - minValue) / binCount;
	const bins: { range: string; count: number; tag: 'in' | 'out' }[] = [];
	const binLabels: string[] = [];
	for (let i = binCount; i > 0; i--) {
		const rangeStart = minValue + stepSize * (i - 1);
		const rangeEnd = minValue + stepSize * i;
		const threshold = props.threshold;
		let tag;
		if (props.isMinimized) {
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
};

const targetState = computed(() => `${props.targetVariable}_state`);
const qoiData = computed(() => props.riskResults?.[targetState.value]?.qoi || []);
</script>

<style scoped>
.row {
	display: flex;
}
</style>
