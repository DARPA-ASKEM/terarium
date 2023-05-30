<template>
	<div class="simulate-chart">
		<div class="multiselect-title">Select variables to plot</div>
		<MultiSelect
			v-if="openedWorkflowNodeStore.chartConfigs[props.chartIdx]"
			v-model="openedWorkflowNodeStore.chartConfigs[props.chartIdx].selectedVariable"
			:options="stateVariablesList"
			optionLabel="code"
			placeholder="Select a State Variable"
		>
			<template v-slot:value>
				<span
					class="selected-label-item"
					v-for="variable in openedWorkflowNodeStore.chartConfigs[props.chartIdx].selectedVariable"
					:key="variable.code"
					:style="{ background: getVariableColor(variable.code) }"
				>
					{{ variable.code }}
				</span>
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
				includeBounds: true
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
	runResults: any;
	runIdList: any;
	chartIdx: number;
}>();
const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();

// data for rendering ui
let stateVariablesList: { code: string }[] = [];
let runList = [] as any[];
const chartData = ref({});

const getVariableColor = (variableCode: string) => {
	// temp
	const VIRIDIS_14 = [
		'#fde725',
		'#cde11d',
		'#98d83e',
		'#67cc5c',
		'#40bd72',
		'#25ac82',
		'#1f998a',
		'#24878e',
		'#2b748e',
		'#34618d',
		'#3d4d8a',
		'#453581',
		'#481c6e',
		'#440154'
	].reverse();
	const { selectedVariable } = openedWorkflowNodeStore.chartConfigs[props.chartIdx];
	const codeIdx = selectedVariable.findIndex(({ code }) => code === variableCode);
	return VIRIDIS_14[Math.floor((codeIdx / selectedVariable.length) * VIRIDIS_14.length)];
};

const watchRunResults = async (runResults) => {
	const { runIdList } = props;
	if (!runIdList.length || isEmpty(runResults)) {
		return;
	}

	// assume that the state variables for all runs will be identical
	// take first run and parse it for state variables
	if (!stateVariablesList.length) {
		stateVariablesList = Object.keys(props.runResults[Object.keys(props.runResults)[0]][0])
			.filter((key) => key !== 'timestep')
			.map((key) => ({ code: key }));
	}

	runList = runIdList.map((runId, index) => ({ code: runId, index }));
	if (!openedWorkflowNodeStore.chartConfigs[props.chartIdx]) {
		openedWorkflowNodeStore.setChartConfig(props.chartIdx, {
			selectedVariable: [stateVariablesList[0]],
			selectedRun: runList[0]
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
	openedWorkflowNodeStore.chartConfigs[props.chartIdx].selectedVariable.forEach(({ code }) =>
		runIdList
			.map((runId) => runResults[runId])
			.forEach((run, runIdx) => {
				const dataset = {
					data: run.map(
						(datum: { [key: string]: number }) => datum[code] // - runResults[selectedRun.value.code][timeIdx][code]
					),
					label: `${runIdList[runIdx]} - ${code}`,
					fill: false,
					tension: 0.4,
					borderColor: getVariableColor(code)
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
	padding: 0em 0.5em;
	margin: 0em 0.25em;
	border-radius: 2px;
	text-shadow: 0 0 0.15em white, 0 0 0.15em white, 0 0 0.15em white, 0 0 0.15em white;
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
