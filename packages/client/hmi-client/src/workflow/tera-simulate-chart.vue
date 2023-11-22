<template>
	<div class="simulate-chart">
		<!-- <div class="multiselect-title">Select variables to plot</div> -->
		<MultiSelect
			v-model="selectedVariable"
			:selection-limit="hasMultiRuns ? 1 : undefined"
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
import _ from 'lodash';
import { ref, computed, watch, onMounted } from 'vue';
import MultiSelect from 'primevue/multiselect';
import Chart from 'primevue/chart';
import { ChartConfig, DataseriesConfig, RunResults, RunType } from '@/types/SimulateConfig';
import { CsvAsset } from '@/types/Types';
import { getGraphDataFromDatasetCSV } from './util';

const emit = defineEmits(['configuration-change']);

const props = defineProps<{
	runResults: RunResults;
	chartConfig: ChartConfig;
	hasMeanLine?: boolean;
	colorByRun?: boolean;
	initialData?: CsvAsset;
	mapping?: { [key: string]: string }[];
	runType?: RunType;
}>();

const renderedRuns = computed<RunResults>(() => {
	if (!props.hasMeanLine) return _.cloneDeep(props.runResults);

	const runResult: RunResults = _.cloneDeep(props.runResults);
	const parsedSimProbData = Object.values(runResult);

	const numRuns = parsedSimProbData.length;
	if (!numRuns) {
		return props.runResults;
	}

	const numTimestamps = (parsedSimProbData as { [key: string]: number }[][])[0].length;
	const aggregateRun: { [key: string]: number }[] = [];

	for (let timestamp = 0; timestamp < numTimestamps; timestamp++) {
		for (let run = 0; run < numRuns; run++) {
			if (!aggregateRun[timestamp]) {
				aggregateRun[timestamp] = parsedSimProbData[run][timestamp];
				Object.keys(aggregateRun[timestamp]).forEach((key) => {
					aggregateRun[timestamp][key] = Number(aggregateRun[timestamp][key]) / numRuns;
				});
			} else {
				const datum = parsedSimProbData[run][timestamp];
				Object.keys(datum).forEach((key) => {
					aggregateRun[timestamp][key] += datum[key] / numRuns;
				});
			}
		}
	}

	return { ...runResult, [numRuns]: aggregateRun };
});

const lineWidthArray = computed(() => {
	// If we have a meanline, make it bigger
	if (props.hasMeanLine) {
		const output = Array(Math.max(Object.keys(renderedRuns.value).length - 1 ?? 0 - 1, 0)).fill(1);
		output.push(3);
		return output;
	}
	// Otherwise all widths are 1
	return Array(Math.max(Object.keys(renderedRuns.value).length ?? 0 - 1, 0)).fill(1);
});

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

// data for rendering ui
let stateVariablesList: string[] = [];
const chartData = ref({});

const selectedVariable = ref<string[]>(props.chartConfig.selectedVariable);

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

const getVariableColorByRunIdx = (runIdx: number) => {
	const runIdList = Object.keys(renderedRuns.value) as string[];
	return VIRIDIS_14[Math.floor((runIdx / runIdList.length) * VIRIDIS_14.length)];
};

const hasMultiRuns = computed(() => {
	const runIdList = Object.keys(renderedRuns.value) as string[];
	return props.colorByRun && runIdList.length > 1;
});

const getLineColor = (variableName: string, runIdx: number) => {
	const runIdList = Object.keys(renderedRuns.value) as string[];
	if (props.hasMeanLine) {
		const lastRun = runIdList.length - 1;
		return runIdx === lastRun
			? getVariableColorByVar(variableName)
			: `${getVariableColorByVar(variableName)}10`;
	}

	return hasMultiRuns.value
		? getVariableColorByRunIdx(runIdx)
		: getVariableColorByVar(variableName);
};

const watchRunResults = async (runResults) => {
	const runIdList = Object.keys(renderedRuns.value) as string[];
	if (!runIdList.length || _.isEmpty(runResults)) {
		return;
	}

	// TODO: pass mappings here for easy variable list genreation

	// assume that the state variables for all runs will be identical
	// take first run and parse it for state variables
	if (!stateVariablesList.length) {
		stateVariablesList = Object.keys(
			renderedRuns.value[Object.keys(renderedRuns.value)[0]][0]
		).filter((key) => key !== 'timestamp');
	}
	renderGraph();
};

const renderGraph = () => {
	const runIdList = Object.keys(renderedRuns.value) as string[];

	if (!runIdList.length || _.isEmpty(renderedRuns.value)) {
		return;
	}

	const datasets: DataseriesConfig[] = [];
	selectedVariable.value.forEach((variable) => {
		runIdList
			.map((runId) => renderedRuns.value[runId])
			.forEach((run, runIdx) => {
				const dataset: DataseriesConfig = {
					data: run.map(
						// - runResults[selectedRun.value][timeIdx][code]
						(datum: { [key: string]: number }) =>
							// return datum[variable];
							({
								y: +datum[variable],
								x: +datum.timestamp
							})
					),
					label: `${runIdList[runIdx]} - ${variable}`,
					fill: false,
					borderColor: getLineColor(variable, runIdx),
					borderWidth: lineWidthArray.value[runIdx]
				};
				datasets.push(dataset);
			});

		if (props.initialData) {
			const dataset: DataseriesConfig | null = getGraphDataFromDatasetCSV(
				props.initialData,
				variable,
				props.mapping,
				props.runType
			);
			if (dataset) {
				datasets.push(dataset);
			}
		}
	});
	console.log(datasets);
	chartData.value = { datasets };
};

const updateSelectedVariable = () => {
	emit('configuration-change', {
		selectedVariable: selectedVariable.value,
		selectedRun: props.chartConfig.selectedRun
	});
};

onMounted(() => {
	// FIXME: Should use deep, need to rewire the dependencies
	watch(() => renderedRuns.value, watchRunResults, { immediate: true, deep: true });

	watch(
		() => props.chartConfig,
		() => {
			selectedVariable.value = props.chartConfig.selectedVariable;
			renderGraph();
		},
		{ immediate: true, deep: true }
	);
});
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
