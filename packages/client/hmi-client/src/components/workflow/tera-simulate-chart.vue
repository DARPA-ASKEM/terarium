<template>
	<div class="simulate-chart">
		<div class="multiselect-title">Select variables to plot</div>
		<MultiSelect
			v-model="selectedVariable"
			:options="stateVariablesList"
			optionLabel="code"
			placeholder="Select a State Variable"
		>
			<template v-slot:value>
				<span
					class="selected-label-item"
					v-for="variable in selectedVariable"
					:key="variable.code"
					:style="{ color: getVariableColor(variable.code) }"
				>
					{{ variable.code }}
				</span>
			</template>
		</MultiSelect>
		<Chart type="line" :data="chartData" :options="CHART_OPTIONS" />
	</div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { isEmpty } from 'lodash';

import MultiSelect from 'primevue/multiselect';
import Chart from 'primevue/chart';

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
}>();

// data for rendering ui
let stateVariablesList: { code: string }[] = [];
const selectedVariable = ref<{ code: string }[]>([]);
let runList = [] as any[];
const selectedRun = ref<null | { code: string }>(null);
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
	];
	const codeIdx = selectedVariable.value.findIndex(({ code }) => code === variableCode);
	return VIRIDIS_14[Math.floor((codeIdx / selectedVariable.value.length) * VIRIDIS_14.length)];
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
	selectedVariable.value = [stateVariablesList[0]];
	runList = runIdList.map((runId, index) => ({ code: runId, index }));
	selectedRun.value = runList[0];
};
watch(() => props.runResults, watchRunResults, { immediate: true });

const renderGraph = () => {
	const { runResults, runIdList } = props;

	if (!runIdList.length || isEmpty(runResults)) {
		return;
	}

	const datasets: DatasetType[] = [];
	selectedVariable.value.forEach(({ code }) =>
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
watch(() => [selectedVariable.value, selectedRun.value], renderGraph, { immediate: true });
</script>

<style scoped>
.multiselect-title {
	font-size: smaller;
	font-weight: 700;
}

.selected-label-item::after {
	color: black;
	content: ', ';
}
.selected-label-item:last-child::after {
	content: '';
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
