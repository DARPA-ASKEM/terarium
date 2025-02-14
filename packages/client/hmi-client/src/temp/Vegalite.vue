<template>
	<div style="padding: 2rem; display: flex; flex-direction: col">
		<div>
			<vega-chart expandable :visualization-spec="forecastChartSpec" />
			<div>
				<div v-for="a in forecastAnnotations" :key="a.id">
					{{ a.layerSpec.description }} <i class="pi pi-trash pi-trash" @click="removeAnnotation(a.id ?? '')" />
				</div>
			</div>
			<div style="display: flex; flex-direction: col">
				<tera-input-text
					class="input"
					style="flex-grow: 1"
					ref="inputElement"
					v-model="questionString"
					:disabled="waitingForForecastChartAnnotation"
					:placeholder="waitingForForecastChartAnnotation ? 'Please wait...' : 'What do you want to annotate?'"
					@keydown.enter="generateAndAddAnnotation"
				/>
				<i v-if="waitingForForecastChartAnnotation" class="pi pi-spin pi-spinner" />
				<Button v-else severity="secondary" icon="pi pi-send" @click="generateAndAddAnnotation" />
			</div>
		</div>
		<div>
			<vega-chart
				:expandable="onExpandErrorChart"
				:interval-selection-signal-names="['brush']"
				:visualization-spec="spec"
				@chart-click="handleChartClick($event)"
				@update-interval-selection="debounceHandleIntervalSelect"
			/>
			<vega-chart expandable :visualization-spec="spec2" style="" />
		</div>
		<vega-chart expandable :visualization-spec="specHistogram" />
	</div>
</template>

<script setup lang="ts">
import { v4 as uuidv4 } from 'uuid';
import { debounce } from 'lodash';
import { ref, onMounted, computed } from 'vue';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import Button from 'primevue/button';
import VegaChart from '@/components/widgets/VegaChart.vue';
// import unchartedVegaTheme from './vega-theme';
import {
	createForecastChart,
	createHistogramChart,
	createErrorChart,
	createForecastChartAnnotation,
	applyForecastChartAnnotations
} from '@/services/charts';
import { generateChartAnnotation } from '@/services/chart-annotation';
import { ChartAnnotation, ChartAnnotationType } from '@/types/Types';
// import { createLLMSummary, getSummaries } from '@/services/summary-service';

const rand = (v: number) => Math.round(Math.random() * v);

const numPoints = 10;
const numSamples = 40;
const valueRange = 20;
const trueValues: any[] = [];
const dataChart1: any[] = [];
const dataChart2: any[] = [];

// Fake data generation
for (let j = 0; j < numPoints; j++) {
	trueValues.push(rand(valueRange));
}

for (let i = 0; i < numSamples; i++) {
	let error = 0;
	let error2 = 0;
	for (let j = 0; j < numPoints; j++) {
		const v = rand(valueRange);
		const v2 = rand(valueRange);
		dataChart2.push({ sample: i, timestep: j, value: v, value2: v2 });
		error += Math.abs(trueValues[j] - v);
		error2 += Math.abs(trueValues[j] - v2);
	}
	dataChart1.push({ sample: i, error, error2 });
}

const makeLineChart = (data: any[]) => {
	return {
		$schema: 'https://vega.github.io/schema/vega-lite/v5.json',
		description: 'Stock prices of 5 Tech Companies over Time.',
		// data: { url: 'https://vega.github.io/vega-lite/data/stocks.csv' },
		data: { values: data },
		mark: {
			type: 'line',
			strokeWidth: 0.5
		},
		encoding: {
			x: {
				field: 'timestep',
				type: 'quantitative'
			},
			y: {
				field: 'value',
				type: 'quantitative',
				scale: { domain: [-20, 40] }
			},
			color: {
				field: 'sample',
				type: 'nominal',
				// scale: { range: ['#f00'] },
				legend: null
			}
		}
	};
};

const spec = ref<any>(
	createErrorChart(dataChart1, {
		title: '',
		width: 500,
		variables: [{ field: 'error' }, { field: 'error2', label: 'e2' }],
		xAxisTitle: 'Error'
	})
);
const onExpandErrorChart = () => {
	// Customize the chart size by modifying the spec before expanding the chart
	const spec = createErrorChart(dataChart1, {
		title: '',
		width: window.innerWidth / 1.5,
		height: 230,
		boxPlotHeight: 50,
		areaChartHeight: 150,
		variables: [{ field: 'error' }, { field: 'error2', label: 'e2' }],
		xAxisTitle: 'Error'
	});
	return spec as any;
};

const spec2 = ref<any>(makeLineChart(dataChart2));

const handleChartClick = (event: any) => {
	console.log('!!', event);
};

const handleIntervalSelect = (name: any, valueRange: any) => {
	console.log('>>', name, valueRange);
	let samples = dataChart1
		.filter((d) => {
			return d.error >= valueRange._value[0] && d.error <= valueRange._value[1];
		})
		.map((d) => d.sample);

	if (!samples || samples.length === 0) {
		spec2.value = makeLineChart(dataChart2);
		return;
	}

	spec2.value = makeLineChart(
		dataChart2.filter((d) => {
			return samples.includes(d.sample);
		})
	);
};

const debounceHandleIntervalSelect = debounce(handleIntervalSelect, 200);

// Generate time series data
const dataNew = generateSimulateData();
const forecastAnnotations = ref<ChartAnnotation[]>([createForecastChartAnnotation('x', 60, 'test label')]);

const questionString = ref('');
const waitingForForecastChartAnnotation = ref(false);
const generateAndAddAnnotation = async () => {
	if (!questionString.value) return;
	waitingForForecastChartAnnotation.value = true;
	const timeField = 'time';
	const variables = ['alphaMean', 'betaMean'];
	const axisTitle = { xAxisTitle: 'Day', yAxisTitle: 'Value' };
	const { request, layerSpec } = await generateChartAnnotation(
		questionString.value,
		ChartAnnotationType.ForecastChart,
		timeField,
		variables,
		axisTitle
	);
	forecastAnnotations.value.push({
		id: uuidv4(),
		description: request,
		nodeId: '',
		outputId: '',
		chartType: ChartAnnotationType.ForecastChart,
		chartId: 'forecastchart',
		layerSpec,
		llmGenerated: true,
		metadata: {}
	});
	waitingForForecastChartAnnotation.value = false;
};
const removeAnnotation = async (id: string) => {
	forecastAnnotations.value = forecastAnnotations.value.filter((a) => a.id !== id);
};

const forecastChartSpec = computed(() =>
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
const generateSampleData = (numSamples = 100) => {
	const data: { varA: number; varB: number }[] = [];

	for (let i = 0; i < numSamples; i++) {
		const point = {
			varA: Math.random() * 0.7 + 0.2,
			varB: Math.random() * 0.7 + 0.2
		};
		data.push(point);
	}
	return data;
};

const specHistogram = ref<any>(
	createHistogramChart(generateSampleData(), {
		title: 'Kappa',
		width: 720,
		height: 250,
		xAxisTitle: 'Kappa',
		yAxisTitle: 'Count',
		maxBins: 10,
		variables: [
			{ field: 'varA', label: 'Before calibration', width: 54, color: '#AAB3C6' },
			{ field: 'varB', label: 'After calibration', width: 24, color: '#1B8073' }
		]
	})
);

onMounted(async () => {
	// Test
	// const abc = await createLLMSummary('what is 1 + 2 + 3');
	// console.log('abc', abc.additionalProperties.summaryId);
	// const summaries = await getSummaries(
	// 	['754f7222-35a6-47f2-8487-06e16330a237', 'd61f4a4e-577a-4f98-aa64-8ba2e0f15f24', 'c98a9a61-461b-41c2-965b-833a93d63f86']
	// );
	// console.log('!!', summaries);
});
</script>
