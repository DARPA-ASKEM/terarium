<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { csvParse } from 'd3';
import { isEqual } from 'lodash';

import Dropdown from 'primevue/dropdown';
import Chart from 'primevue/chart';

import { PollerState } from '@/api/api';
import { getRunStatus, getRunResult } from '@/services/models/simulation-service';

const route = useRoute();

let runIdList = ref([] as number[]);
let runResults = {};

// data for rendering ui
let stateVariablesList = [] as any[];
let selectedVariable = ref(null as null | any);
let runStatus = [] as boolean[];
let runList = [] as any[];
let selectedRun = ref(null as null | any);

const chartData = ref({});
const chartOptions = {
	maintainAspectRatio: false,
	plugins: {
		legend: {
			labels: {
				color: '#000'
			}
		}
	},
	scales: {
		x: {
			ticks: {
				color: '#000'
			},
			grid: {
				color: '#AAA'
			}
		},
		y: {
			ticks: {
				color: '#000'
			},
			grid: {
				color: '#AAA'
			}
		}
	}
};

// retrieve run ids
const getStatus = async () => {
	const { assetId } = route.params;
	const assetString = sessionStorage.getItem(assetId as string);
	const assetObj = JSON.parse(assetString as string);
	const newRunIdList = assetObj.map((asset) => asset.id);
	const newRunStatus = await Promise.all(newRunIdList.map(getRunStatus));

	if (!isEqual(newRunStatus, runStatus)) {
		runIdList.value = newRunStatus.reduce((finishedRuns, runStatus, runIdx) => {
			if (runStatus.status === PollerState.Done) {
				finishedRuns.push(newRunIdList[runIdx]);
			}
			return finishedRuns;
		}, []);
		runStatus = newRunStatus;
	}

	// recursively call until all runs retrieved
	if (runStatus.some((status) => status === false)) {
		setTimeout(getStatus, 3000);
	}
};
getStatus();

watch(
	() => runIdList.value,
	async (runIdList) => {
		const newRunResults = {};
		await Promise.all(
			runIdList.map(async (runId) => {
				if (runResults[runId]) {
					newRunResults[runId] = runResults[runId];
				} else {
					const resultCsv = await getRunResult(runId);
					newRunResults[runId] = csvParse(resultCsv);
				}
			})
		);
		runResults = newRunResults;

		// process data retrieved

		// assume that the state variables for all runs will be identical
		// take first run and parse it for state variables
		stateVariablesList = Object.keys(runResults[Object.keys(runResults)[0]][0])
			.filter((key) => key !== 'timestamp')
			.map((key) => ({ code: key }));
		selectedVariable.value = stateVariablesList[0];
		runList = runIdList.map((runId, index) => ({ code: runId, index }));
		selectedRun.value = runList[0];
	}
);

const renderGraph = ([{ code }]) => {
	chartData.value = {
		labels: runResults[Object.keys(runResults)[0]].map((datum) => Number(datum.timestamp)),
		datasets: runIdList.value
			.map((runId) => runResults[runId])
			.map((run, runIdx) => {
				const dataset = {
					data: run.map(
						(datum: any, timeIdx) => datum[code] // - runResults[selectedRun.value.code][timeIdx][code]
					),
					label: runIdList.value[runIdx],
					fill: false,
					tension: 0.4
				};
				return dataset;
			})
	};
};
watch(() => [selectedVariable.value, selectedRun.value] as any, renderGraph);
</script>

<template>
	<section class="result-container">
		<div class="options">
			<div class="dropdown-group">
				<span>State Variable: </span>
				<Dropdown
					v-model="selectedVariable"
					:options="stateVariablesList"
					optionLabel="code"
					placeholder="Select a State Variable"
				/>
			</div>
			<div class="dropdown-group">
				<span>Baseline Run: </span>
				<Dropdown
					v-model="selectedRun"
					:options="runList"
					optionLabel="code"
					placeholder="Select a Base Run"
				/>
			</div>
		</div>
		<h3>Simulation Results - Difference from Baseline Scenario</h3>
		<div class="result">
			<Chart type="line" :data="chartData" :options="chartOptions" />
		</div>
	</section>
</template>

<style scoped>
.legend-container {
	display: flex;
	justify-content: space-between;
}

.legend-label {
	display: flex;
	justify-content: center;
	width: 370px;
}

.result-container {
	display: flex;
	flex-direction: column;
	width: 100%;
	padding: 10px;
	background: var(--surface-overlay);
}

.result {
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: row;
}

.p-chart {
	width: 100%;
}

.options {
	display: flex;
	margin-bottom: 10px;
}

.dropdown-group {
	flex-grow: 1;
	flex-basis: 0%;
}

.p-dropdown {
	width: 50%;
}

h3 {
	margin-bottom: 10px;
}
</style>
