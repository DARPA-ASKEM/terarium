<template>
	<Button @click="addTemp"> click </Button>
	<div style="padding: 2rem; display: flex; flex-direction: col">
		<div v-for="(spec, idx) of forecastChartSpec" :key="idx">
			<vega-chart expandable :visualization-spec="spec" :key="idx" />
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import _ from 'lodash';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { createForecastChart, createForecastChartAnnotation, applyForecastChartAnnotations } from '@/services/charts';
import { ChartAnnotation } from '@/types/Types';

// Generate time series data
const dataNew = generateSimulateData();
const forecastAnnotations = ref<ChartAnnotation[]>([createForecastChartAnnotation('x', 60, 'test label')]);

const tempTest = ref<any[]>([]);
const addTemp = () => {
	tempTest.value.push(
		applyForecastChartAnnotations(
			createForecastChart(
				{
					data: dataNew.data,
					variables: ['alpha', 'beta'],
					timeField: 'time',
					groupField: 'sample'
				},
				{
					data: dataNew.summary,
					variables: ['alphaMean', 'betaMean'],
					timeField: 'time'
				},
				null,
				{
					width: 400,
					height: 200,
					legend: true,
					colorscheme: ['#F00', '#0F0', '#00F'],
					xAxisTitle: 'Day',
					yAxisTitle: 'Value'
				}
			),
			forecastAnnotations.value
		)
	);
};

const forecastChartSpec = computed(() => {
	return tempTest.value;
});

function generateSimulateData() {
	const data: any[] = [];
	const summary: any[] = [];
	const truth: any[] = [];

	const numSamples = 30;
	const numSteps = 200;

	for (let j = 0; j < numSteps; j++) {
		let alphaMean = 0;
		let betaMean = 0;
		let gammaMean = 0;

		let alphaTrue = 0.1 * (j % 3);
		let betaTrue = 0.2 * (j % 3) - 0.1;
		let gammaTrue = 0.1 * (j % 5);

		for (let i = 0; i < numSamples; i++) {
			const alpha = Math.sin((5 * j * Math.PI) / 180) + 0.2 * Math.random() + 0.06 * i;
			const beta = Math.cos((5 * j * Math.PI) / 180) + 0.2 * Math.random() - 0.07 * i;
			const gamma = 0.07 * (-0.5 * (j % 25) + Math.random() * (j % 25));

			data.push({
				sample: i,
				time: j,
				alpha: alpha,
				beta: beta,
				gamma: gamma
			});

			// Summary
			alphaMean += alpha;
			betaMean += beta;
			gammaMean += gamma;
		}
		alphaMean /= numSamples;
		betaMean /= numSamples;
		gammaMean /= numSamples;

		truth.push({
			time: j,
			alphaTrue: alphaTrue,
			betaTrue: betaTrue,
			gammaTrue: gammaTrue
		});

		summary.push({
			time: j,
			alphaMean: alphaMean,
			betaMean: betaMean,
			gammaMean: gammaMean
		});
	}
	return { data, summary, truth };
}

onMounted(async () => {});
</script>
