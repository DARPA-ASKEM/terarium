<template>
	<div class="simulate-chart">
		<div class="multiselect-title">Select variables to plot</div>
		<MultiSelect
			v-model="selectedVariable"
			:options="stateVariablesList"
			placeholder="Select a state variable"
			@update:model-value="updateSelectedVariable"
		>
			<template v-slot:value>
				<template v-for="(variable, index) in selectedVariable" :key="index">
					<template v-if="index > 0">,&nbsp;</template>
					<span :style="{ color: getVariableColorByVar(variable) }">
						{{ variable }}
					</span>
				</template>
			</template>
		</MultiSelect>
		<Chart type="scatter" :data="chartData" :options="CHART_OPTIONS" />
	</div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import MultiSelect from 'primevue/multiselect';
import Chart from 'primevue/chart';
import { ChartConfig, DataseriesConfig, RunResults, RunType } from '@/types/SimulateConfig';
import type { CsvAsset } from '@/types/Types';
import { getGraphDataFromDatasetCSV } from '@/components/workflow//util';

const emit = defineEmits(['configuration-change']);

const props = defineProps<{
	intermediateData: any;
	chartConfig: ChartConfig;
	runResults?: RunResults;
	initialData?: CsvAsset;
	mapping?: { [key: string]: string }[];
	selectedRun?: string;
	runInProgress?: boolean;
}>();

const stateVariablesList = computed(() => {
	// TODO: handle mapping
	if (props.initialData) {
		return props.initialData.headers.reduce((acc: string[], header: string) => {
			if (header !== 'timestamp') {
				acc.push(`${header.trim()}(t)`);
			}
			return acc;
		}, []);
	}
	return [];
});
const selectedVariable = ref<string[]>(props.chartConfig.selectedVariable);

const chartData = ref({});
const tempChartData: { [key: string]: { [key: string]: DataseriesConfig } } = {
	intermediateData: {},
	initialData: {}
};

// const currentRunResults = computed(() => {
//     if (props.selectedRun && props.runResults[props.selectedRun]) {
//         return props.runResults[props.selectedRun];
//     }
//     return null;
// });

const CHART_OPTIONS = {
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
};

// temp
const VIRIDIS_14 = [
	'#440154',
	'#481c6e',
	'#453581',
	'#3d4d8a',
	'#34618d',
	'#2b748e',
	'#24878e',
	'#1f998a',
	'#25ac82',
	'#40bd72',
	'#67cc5c',
	'#98d83e',
	'#cde11d',
	'#fde725'
];

const getVariableColorByVar = (variableName: string) => {
	const codeIdx = selectedVariable.value.findIndex((variable) => variable === variableName);
	return VIRIDIS_14[Math.floor((codeIdx / selectedVariable.value.length) * VIRIDIS_14.length)];
};

const updateSelectedVariable = () => {
	emit('configuration-change', {
		selectedVariable: selectedVariable.value,
		selectedRun: props.chartConfig.selectedRun
	});
};

watch(
	() => selectedVariable.value,
	() => {
		const datasets: DataseriesConfig[] = [];
		if (selectedVariable.value.length > 0 && props.initialData) {
			selectedVariable.value.forEach((variable) => {
				const dataset: DataseriesConfig | null = getGraphDataFromDatasetCSV(
					props.initialData!,
					variable,
					props.mapping,
					RunType.Julia
				);
				if (dataset) {
					tempChartData.initialData[variable] = dataset;
					datasets.push(dataset);
				}
				if (tempChartData.intermediateData[variable]) {
					datasets.push(tempChartData.intermediateData[variable]);
				}
			});
		}
		chartData.value = { datasets };
	},
	{ immediate: true }
);

watch(
	() => props.intermediateData,
	() => {
		const datasets: DataseriesConfig[] = [];
		if (selectedVariable.value.length > 0) {
			const { timesteps, solData } = props.intermediateData;
			selectedVariable.value.forEach((variable) => {
				if (solData[variable]) {
					const dataset: DataseriesConfig = {
						data: solData[variable].map((val, idx) => ({
							x: timesteps[idx],
							y: val
						})),
						label: `${variable} - intermediate`,
						fill: false,
						borderColor: getVariableColorByVar(variable),
						borderWidth: 1
					};

					tempChartData.intermediateData[variable] = dataset;
					datasets.push(dataset);
					if (tempChartData.initialData[variable]) {
						datasets.push(tempChartData.initialData[variable]);
					}
				}
			});
		}
		chartData.value = { datasets };
	},
	{ immediate: true }
);

/**
 * TODO: If we want to use this component to handle both intermediate results as well as
 * final runResults, then we might make use of the following code.
 */
// watch(() => currentRunResults.value, () => {
//     const datasets: DataseriesConfig[] = [];
//     if (currentRunResults.value) {
//         updateTempChartData(currentRunResults.value);
//         selectedVariable.value.forEach((variable) => {
//             if (tempChartData.intermediateData[variable]) {
//                 datasets.push(tempChartData.intermediateData[variable]);
//             }
//             if (tempChartData.initialData[variable]) {
//                 datasets.push(tempChartData.initialData[variable]);
//             }
//         });
//     }
//     chartData.value = { datasets };
// });

// const updateTempChartData = (run) => {
//     stateVariablesList.value.forEach((variable) => {
//         const dataset: DataseriesConfig = {
//             data: run.map(
//                 (datum: { [key: string]: number }) =>
//                     ({
//                         y: +datum[variable],
//                         x: +datum.timestamp
//                     })
//             ),
//             label: `${props.selectedRun} - ${variable}`,
//             fill: false,
//             borderColor: getVariableColorByVar(variable),
//             borderWidth: 1
//         };
//         tempChartData.intermediateData[variable] = dataset;
//         console.log(tempChartData);
//     });
// };
</script>

<style scoped>
.multiselect-title {
	font-size: smaller;
	font-weight: 700;
}

.p-chart {
	width: 100%;
	height: 200px;
	margin-top: 0.5em;
}

.p-multiselect {
	width: 100%;
	border-color: lightgrey;
}
</style>
