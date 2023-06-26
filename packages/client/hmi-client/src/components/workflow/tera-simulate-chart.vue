<template>
	<div class="simulate-chart">
		<div class="multiselect-title">Select variables to plot</div>
		<MultiSelect
			v-if="openedWorkflowNodeStore.chartConfigs[props.chartIdx]"
			v-model="openedWorkflowNodeStore.chartConfigs[props.chartIdx].selectedVariable"
			:selection-limit="hasMultiRuns ? 1 : undefined"
			:options="stateVariablesList"
			placeholder="Select a State Variable"
		>
			<template v-slot:value>
				<template
					v-for="(variable, index) in openedWorkflowNodeStore.chartConfigs[props.chartIdx]
						.selectedVariable"
					:key="index"
				>
					<template v-if="index > 0">,&nbsp;</template>
					<span
						class="selected-label-item"
						:style="{ color: hasMultiRuns ? 'black' : getVariableColorByVar(variable) }"
						>{{ variable }}</span
					>
				</template>
			</template>
		</MultiSelect>
		<MultiSelect v-else placeholder="No Data" :disabled="true" />
		<Chart type="line" :data="chartData" :options="CHART_OPTIONS" />
	</div>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from 'vue';
import { isEmpty } from 'lodash';

import MultiSelect from 'primevue/multiselect';
import Chart from 'primevue/chart';

import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';

import { RunResults } from '@/types/SimulateConfig';

type DatasetType = {
	data: number[];
	label: string;
	fill: boolean;
	tension: number;
};

const CHART_OPTIONS = {
	devicePixelRatio: 4,
	maintainAspectRatio: false,
	pointStyle: false,
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
				includeBounds: true
			},
			grid: {
				color: '#fff',
				borderColor: '#fff'
			}
		}
	}
};

const props = defineProps<{
	runResults: RunResults;
	chartIdx: number;
}>();
const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();

// data for rendering ui
let stateVariablesList: string[] = [];
const chartData = ref({});

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
	const { selectedVariable } = openedWorkflowNodeStore.chartConfigs[props.chartIdx];
	const codeIdx = selectedVariable.findIndex((variable) => variable === variableName);
	return VIRIDIS_14[Math.floor((codeIdx / selectedVariable.length) * VIRIDIS_14.length)];
};

const getVariableColorByRunIdx = (runIdx: number) => {
	const runIdList = Object.keys(props.runResults) as string[];
	return VIRIDIS_14[Math.floor((runIdx / runIdList.length) * VIRIDIS_14.length)];
};

const hasMultiRuns = computed(() => {
	const runIdList = Object.keys(props.runResults) as string[];
	return runIdList.length > 1;
});

const watchRunResults = async (runResults) => {
	const runIdList = Object.keys(props.runResults) as string[];
	if (!runIdList.length || isEmpty(runResults)) {
		return;
	}

	// TODO: pass mappings here for easy variable list genreation

	// assume that the state variables for all runs will be identical
	// take first run and parse it for state variables
	if (!stateVariablesList.length) {
		stateVariablesList = Object.keys(props.runResults[Object.keys(props.runResults)[0]][0]).filter(
			(key) => key !== 'timestep' && key !== 'timestamp' && key !== 'date'
		);
	}

	// grab variable columns here?
	// console.log(stateVariablesList)
	// console.log(props.runResults)
	// console.log(Object.keys(props.runResults[Object.keys(props.runResults)[0]][0]).filter(
	// 	(key) => (key !== 'timestep' && key !== 'timestamp' && key !== 'date')
	// ))

	if (!openedWorkflowNodeStore.chartConfigs[props.chartIdx]) {
		openedWorkflowNodeStore.setChartConfig(props.chartIdx, {
			selectedVariable: [stateVariablesList[0]],
			selectedRun: runIdList[0]
		});
	}
	renderGraph();
};

const renderGraph = () => {
	const { runResults } = props;
	const runIdList = Object.keys(props.runResults) as string[];

	console.log('renderGraph', runResults, runIdList);

	if (!runIdList.length || isEmpty(runResults)) {
		return;
	}

	const datasets: DatasetType[] = [];
	openedWorkflowNodeStore.chartConfigs[props.chartIdx].selectedVariable.forEach((variable) =>
		runIdList
			.map((runId) => runResults[runId])
			.forEach((run, runIdx) => {
				const dataset = {
					data: run.map(
						(datum: { [key: string]: number }) => datum[variable] // - runResults[selectedRun.value][timeIdx][code]
					),
					label: `${runIdList[runIdx]} - ${variable}`,
					fill: false,
					tension: 0.4,
					borderColor: hasMultiRuns.value
						? getVariableColorByRunIdx(runIdx)
						: getVariableColorByVar(variable)
				};
				datasets.push(dataset);
			})
	);
	chartData.value = {
		labels: runResults[Object.keys(runResults)[0]].map((datum) => Number(datum.timestamp)),
		datasets
	};
};

onMounted(() => {
	// FIXME: Should use deep, need to rewire the dependencies
	watch(() => props.runResults, watchRunResults, { immediate: true, deep: true });

	if (openedWorkflowNodeStore.chartConfigs[props.chartIdx]) {
		watch(() => openedWorkflowNodeStore.chartConfigs[props.chartIdx].selectedVariable, renderGraph);
	}
});
</script>

<style scoped>
.multiselect-title {
	font-size: smaller;
	font-weight: 700;
}

.selected-label-item {
	font-weight: bold;
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
