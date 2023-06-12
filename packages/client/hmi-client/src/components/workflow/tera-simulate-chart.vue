<template>
	<div class="simulate-chart">
		<div class="multiselect-title">Select variables to plot</div>
		<MultiSelect
			v-if="openedWorkflowNodeStore.chartConfigs[props.chartIdx]"
			v-model="openedWorkflowNodeStore.chartConfigs[props.chartIdx].selectedVariable"
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
					<span class="selected-label-item" :style="{ color: getVariableColor(variable, 0) }">{{
						variable
					}}</span>
				</template>
			</template>
		</MultiSelect>
		<MultiSelect v-else placeholder="No Data" :disabled="true" />
		<Chart type="line" :data="chartData" :options="CHART_OPTIONS" />
	</div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
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
	runIdList: number[];
	chartIdx: number;
}>();
const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();

// data for rendering ui
let stateVariablesList: string[] = [];
const chartData = ref({});

const lightenHexStr = (hex, v) => {
	let r = parseInt(hex.substring(1, 3), 16);
	let g = parseInt(hex.substring(3, 5), 16);
	let b = parseInt(hex.substring(5, 7), 16);

	r += Math.floor((255 - r) * v);
	g += Math.floor((255 - g) * v);
	b += Math.floor((255 - b) * v);

	return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b
		.toString(16)
		.padStart(2, '0')}`;
};

const getVariableColor = (variableName: string, runIdx: number) => {
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
	const { selectedVariable } = openedWorkflowNodeStore.chartConfigs[props.chartIdx];
	const codeIdx = selectedVariable.findIndex((variable) => variable === variableName);
	const baseColor = VIRIDIS_14[Math.floor((codeIdx / selectedVariable.length) * VIRIDIS_14.length)];
	return lightenHexStr(baseColor, runIdx / props.runIdList.length);
};

const watchRunResults = async (runResults) => {
	const { runIdList } = props;
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
};
watch(() => props.runResults, watchRunResults, { immediate: true });

const renderGraph = () => {
	const { runResults, runIdList } = props;

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
					borderColor: getVariableColor(variable, runIdx)
				};
				datasets.push(dataset);
			})
	);
	chartData.value = {
		labels: runResults[Object.keys(runResults)[0]].map((datum) => Number(datum.timestep)),
		datasets
	};
};
watch(() => openedWorkflowNodeStore.chartConfigs[props.chartIdx], renderGraph, {
	immediate: true,
	deep: true
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
