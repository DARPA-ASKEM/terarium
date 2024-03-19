<template>
	<div class="row">
		<canvas ref="chartCanvas"></canvas>
	</div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
// eslint-disable-next-line import/no-extraneous-dependencies
import ChartAnnotation from 'chartjs-plugin-annotation';
import { ChartConfig } from '@/types/SimulateConfig';
import { Chart } from 'chart.js';

// Note: There's seems to be a problem with vueprime/chart, chart.js and chartjs-plugin-annotation where the annotation plugin is not working properly throwing errors unexpectedly.
// In order to work around this issue, we are using the chart.js directly and importing the annotation plugin from chartjs-plugin-annotation instead of using the vueprime/chart component.
// Also there's weird issue with eslint complaining about the import of ChartAnnotation from 'chartjs-plugin-annotation' even though it's in the package.json and being used in the code.
// To workaround this, we are disabling the eslint rule for this line.

Chart.register(ChartAnnotation);

const chartCanvas = ref(null);
const chart = ref<any>(null);

const renderChart = () => {
	if (chart.value !== null) {
		chart.value.destroy();
	}
	// eslint-disable-next-line no-new
	chart.value = new Chart(chartCanvas.value, {
		type: 'bar',
		data: chartData.value,
		options: chartOptions.value
	});
	chart.value.resize(chartSize.value.width, chartSize.value.height);
};

const props = defineProps<{
	riskResults: any;
	targetVariable: string;
	chartConfig: ChartConfig;
	size?: { width: number; height: number };
	threshold: number;
}>();

const chartSize = computed(() => {
	if (props.size) return props.size;
	return { width: 520, height: 190 };
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
		},
		annotation: {
			annotations: {
				line1: {
					type: 'line',
					yMin: props.threshold,
					yMax: props.threshold,
					borderColor: 'rgb(255, 99, 132)',
					borderWidth: 2
				}
			}
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
	for (let i = binCount - 1; i >= 0; i--) {
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
			}
		]
	};
};

watch(
	[() => props.riskResults, () => props.threshold],
	async () => {
		chartOptions.value = setChartOptions();
		chartData.value = setChartData();
		renderChart();
	},
	{ immediate: true }
);
</script>

<style scoped>
.row {
	display: flex;
}
</style>
