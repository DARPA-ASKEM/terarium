<template>
	<div class="row">
		<Chart
			type="bar"
			:width="chartSize.width"
			:height="chartSize.height"
			:data="chartData"
			:options="chartOptions"
		/>
	</div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import Chart from 'primevue/chart';
import { ChartConfig } from '@/types/SimulateConfig';

const props = defineProps<{
	riskResults: any;
	targetVariable: string;
	chartConfig: ChartConfig;
	size?: { width: number; height: number };
}>();

const chartSize = computed(() => {
	if (props.size) return props.size;
	return { width: 390, height: 190 };
});

const chartOptions = ref();
const chartData = ref();
const binCount = 6;

const setChartOptions = () => ({
	indexAxis: 'y',
	responsive: false,
	devicePixelRatio: 4,
	maintainAspectRatio: false,
	pointStyle: false,
	animation: {
		duration: 0
	},
	showLine: true,
	plugins: {
		legend: {
			display: false
		}
	},
	scales: {
		x: {
			title: {
				display: true,
				text: 'Number of Samples'
			},
			ticks: {
				color: '#aaa',
				maxTicksLimit: 5,
				includeBounds: true,
				// this rounds the tick label to nearest int
				callback: (num) => num
			},
			grid: {
				color: '#fff',
				borderColor: '#fff'
			}
		},
		y: {
			title: {
				display: true,
				text: props.targetVariable
			},
			ticks: {
				color: '#aaa',
				includeBounds: true,
				precision: 4
			},
			grid: {
				color: '#fff',
				borderColor: '#fff'
			}
		}
	}
});

const getBinData = (data: number[]) => {
	const minValue = Math.min(...data);
	const maxValue = Math.max(...data);
	const stepSize = (maxValue - minValue) / (binCount - 1);
	const bins: number[] = Array<number>(binCount).fill(0);
	const binLabels: string[] = [];
	for (let i = 0; i < binCount; i++) {
		binLabels.push(
			`${(minValue + stepSize * i).toFixed(4)} - ${(minValue + stepSize * (i + 1)).toFixed(4)}`
		);
	}
	// Fill bins:
	data.forEach((ele) => {
		const index = Math.abs(Math.floor((ele - minValue) / stepSize));
		bins[index] += 1;
	});

	return { binValues: bins, binLabes: binLabels };
};

const setChartData = () => {
	if (!props.riskResults) return {};

	// TODO: risk.json has _state appended to all states. This is an ugly but fast fix.
	const targetState = `${props.targetVariable}_state`;
	const riskValue = props.riskResults[targetState].risk[0];
	const qoiData = props.riskResults[targetState].qoi;
	const binData = getBinData(qoiData);
	const binLabels = binData.binLabes;
	const binValues = binData.binValues.map((ele, index) => ({ x: ele, y: index }));
	const riskLine: any[] = [];
	for (let i = 0; i < binCount; i++) {
		riskLine.push({ x: riskValue, y: i });
	}
	return {
		labels: binLabels,
		datasets: [
			{
				type: 'bar',
				label: '',
				data: binValues,
				borderColor: '#440154',
				borderWidth: 1
			},
			// Risk:
			{
				type: 'line',
				label: '',
				data: riskLine,
				borderColor: '#FF0000',
				borderWidth: 1
			}
		]
	};
};

watch(
	() => props.riskResults,
	async () => {
		chartOptions.value = setChartOptions();
		chartData.value = setChartData();
	},
	{ immediate: true }
);
</script>

<style scoped>
.row {
	display: flex;
}
</style>
