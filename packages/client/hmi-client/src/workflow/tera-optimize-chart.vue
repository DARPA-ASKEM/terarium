<template>
	<div class="simulate-chart">
		<div class="row">
			<Chart
				type="scatter"
				:width="chartSize.width"
				:height="chartSize.height"
				:data="chartData"
				:options="chartOptions"
			/>
		</div>
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
			ticks: {
				color: '#aaa',
				maxTicksLimit: 3,
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
	data.sort();
	const minValue = data[0]; // data.getMin
	const maxValue = data[data.length - 1]; // data.getMax
	const stepSize = (maxValue - minValue) / (binCount - 1);
	const bins: number[] = Array<number>(binCount).fill(0);
	// Fill bins:
	data.forEach((ele) => {
		const index = Math.abs(Math.floor((ele - minValue) / stepSize));
		bins[index] += 1;
	});

	return bins;
};

const setChartData = () => {
	if (!props.riskResults)
		return {
			labels: [],
			datasets: [
				{
					label: '',
					data: [],
					borderColor: '#440154',
					borderWidth: 1
				}
			]
		};

	// TODO: risk.json has _state appended to all states. This is an ugly but fast fix.
	const targetState = `${props.targetVariable}_state`;
	const riskValue = props.riskResults[targetState].risk[0];
	const qoiData = props.riskResults[targetState].qoi;
	const riskLine: any[] = [];
	for (let i = 0; i < binCount; i++) {
		riskLine.push({ x: i, y: riskValue });
	}
	return {
		labels: [],
		datasets: [
			{
				label: '',
				data: getBinData(qoiData).map((ele, index) => ({ x: ele, y: index })),
				borderColor: '#440154',
				borderWidth: 1
			},
			// Risk:
			{
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
.multiselect-title {
	font-size: smaller;
	font-weight: 700;
}

.p-chart {
	width: 100%;
	/* height: 200px; */
	margin-top: 0.5em;
}

.p-multiselect {
	width: 100%;
	border-color: lightgrey;
}

.row {
	display: flex;
}
</style>
