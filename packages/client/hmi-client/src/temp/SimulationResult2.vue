<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { scaleOrdinal } from 'd3';
import { mix } from 'chroma-js';
import { CellData } from '@/types/ResponsiveMatrix';

import Dropdown from 'primevue/dropdown';
import Chart from 'primevue/chart';

import run1 from './simulation-run-data/1/output.json';
import run2 from './simulation-run-data/2/output.json';
import run3 from './simulation-run-data/3/output.json';
import run4 from './simulation-run-data/4/output.json';
import run5 from './simulation-run-data/5/output.json';
import run6 from './simulation-run-data/6/output.json';
import run7 from './simulation-run-data/7/output.json';
import run8 from './simulation-run-data/8/output.json';
import run9 from './simulation-run-data/9/output.json';

const route = useRoute();

watch(
	() => route.params.assetId,
	async (assetId) => {
		if (!assetId) return;

		// FIXME: siwtch to different simulation run result
		console.log(`simulation run id changed to ${assetId}`);
	},
	{ immediate: true }
);

// ///////////////////////////////////////////////////////////////////////////////
// parse raw data into responsive matrix input

function parseSimData(input) {
	if (input.constructor !== Array) {
		input = [input];
	}

	// assume timesteps are identical in all runs
	// eslint-disable-next-line no-underscore-dangle
	const cellLabelCol = input[0]._time.value.map((v) => new Date(v));
	const cellLabelAltCol = cellLabelCol.map((d) => d.toLocaleDateString());
	const cellLabelRow = ['Run1', 'Run2', 'Run3', 'Run4', 'Run5', 'Run6', 'Run7', 'Run8', 'Run9'];
	const numTimesteps = cellLabelCol.length;

	// assume _time exists and that the number of timesteps equals number of values
	const data: object[][] = [];
	input.forEach((run) => {
		const runData: object[] = [];
		const runStateVars = Object.keys(run).filter((k) => ![`_time`].includes(k));
		for (let i = 0; i < numTimesteps; i++) {
			runData.push(
				runStateVars
					.map((k) => run[k].value[i])
					.reduce((acc, v, j) => ({ ...acc, [runStateVars[j]]: v }), {})
			);
		}
		data.push(runData);
	});

	// find max diverging max/min and data parameters max/min
	const baseRow = 0;
	// cellLabelRow[baseRow] = 'Base'; // change label of base row to base

	let divergingMaxMin = 0;
	const dataParameters = new Set();
	const parametersMax: any = {};
	const parametersMin: any = {};
	const parametersDiffMax: any = {};
	const parametersDiffMin: any = {};
	data.forEach((row) =>
		row.forEach((cell: any, j) => {
			// diverging max/min
			const cellBase: any = data[baseRow][j];
			const cellBaseDiff = Math.abs(cell.Infected - cellBase.Infected);
			divergingMaxMin = Math.max(divergingMaxMin, cellBaseDiff);

			// data parameters max/min
			Object.keys(cell).forEach((param) => {
				if (!dataParameters.has(param)) {
					parametersMin[param] = cell[param];
					parametersMax[param] = cell[param];
					parametersDiffMin[param] = cell[param] - cellBase[param];
					parametersDiffMax[param] = cell[param] - cellBase[param];
				} else {
					parametersMin[param] = Math.min(parametersMin[param], cell[param]);
					parametersMax[param] = Math.max(parametersMax[param], cell[param]);
					parametersDiffMin[param] = Math.min(
						parametersDiffMin[param],
						cell[param] - cellBase[param]
					);
					parametersDiffMax[param] = Math.max(
						parametersDiffMax[param],
						cell[param] - cellBase[param]
					);
				}
				dataParameters.add(param);
			});
		})
	);

	const selectorFn = (datum: CellData, param: number | string) => {
		const datumBase: any = data[baseRow][datum.col];
		return datum[param] - datumBase[param];
	};

	const fillColorFn = (datum: CellData) => {
		const colorExtremePos = '#4d9221';
		const colorExtremeNeg = '#c51b7d';
		const colorMid = '#f7f7f7';
		const datumBase: any = data[baseRow][datum.col];

		if (datum.row === baseRow) {
			const v =
				(datum.Infected - parametersMin.Infected) /
				(parametersMax.Infected - parametersMin.Infected);
			return mix('#F7F7F7', '#252525', v, 'lab');
		}

		// return midpoint color if the range of Infected is 0 to avoid divide-by-0 error
		if (!divergingMaxMin) {
			return colorMid;
		}

		const diff = datum.Infected - datumBase.Infected;

		// map ratio value to a hex colour
		if (diff >= 0) {
			return mix(colorMid, colorExtremePos, diff / divergingMaxMin, 'lab');
		}
		return mix(colorMid, colorExtremeNeg, -diff / divergingMaxMin, 'lab');
	};

	const variableColorScale = scaleOrdinal([
		'#1f77b4',
		'#ff7f0e',
		'#d62728',
		'#9467bd',
		'#8c564b',
		'#7f7f7f',
		'#bcbd22',
		'#17becf'
	]).domain(Object.keys(data[0][0]));

	// const scale = scaleOrdinal(schemeAccent).domain(Object.keys(data[0][0]));
	const drilldownColorFn = (parameter: string) => variableColorScale(parameter);

	const labelColFormatFn = (date) => {
		const month = (1 + date.getMonth()).toString().padStart(2, '0');
		const day = date.getDate().toString().padStart(2, '0');

		return `${month}/${day}`;
	};

	return {
		data,
		parametersMax,
		parametersMin,
		parametersDiffMax,
		parametersDiffMin,
		cellLabelCol,
		cellLabelAltCol,
		cellLabelRow,
		selectorFn,
		fillColorFn,
		lineColorFn: drilldownColorFn,
		barColorFn: drilldownColorFn,
		labelColFormatFn,
		divergingMaxMin
	};
}

const simData = parseSimData([run1, run2, run3, run4, run5, run6, run7, run8, run9]);

// transform simData into a suitable format for primecharts
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

const stateVariablesList = Object.keys(simData.parametersMin).map((selectedVar) => ({
	code: selectedVar
}));
const selectedVariable = ref(stateVariablesList[0]);

const runList = simData.cellLabelRow.map((label, index) => ({ code: label, index }));
const selectedRun = ref(runList[0]);

const renderGraph = ([{ code }]) => {
	chartData.value = {
		labels: simData.cellLabelAltCol,
		datasets: simData.data.map((row, rowIdx) => {
			const dataset = {
				data: row.map(
					(datum: any, colIdx) => datum[code] - simData.data[selectedRun.value.index][colIdx][code]
				),
				label: simData.cellLabelRow[rowIdx],
				fill: false,
				tension: 0.4
			};
			return dataset;
		})
	};
};

watch(() => [selectedVariable.value, selectedRun.value] as any, renderGraph, { immediate: true });
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
