<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { select, scaleOrdinal } from 'd3';
import { mix } from 'chroma-js';
import ResponsiveMatrix from '@/components/responsive-matrix/matrix.vue';
import { CellData } from '@/types/ResponsiveMatrix';

import run1 from './simulation-run-data/1/output.json';
import run2 from './simulation-run-data/2/output.json';
import run3 from './simulation-run-data/3/output.json';
import run4 from './simulation-run-data/4/output.json';
import run5 from './simulation-run-data/5/output.json';
import run6 from './simulation-run-data/6/output.json';
import run7 from './simulation-run-data/7/output.json';
import run8 from './simulation-run-data/8/output.json';
import run9 from './simulation-run-data/9/output.json';

import run1Description from './simulation-run-data/1/description.json';
import run2Description from './simulation-run-data/2/description.json';
import run3Description from './simulation-run-data/3/description.json';
import run4Description from './simulation-run-data/4/description.json';
import run5Description from './simulation-run-data/5/description.json';
import run6Description from './simulation-run-data/6/description.json';
import run7Description from './simulation-run-data/7/description.json';
import run8Description from './simulation-run-data/8/description.json';
import run9Description from './simulation-run-data/9/description.json';

const route = useRoute();

watch(
	() => route.params.simulationRunId,
	async (simulationRunId) => {
		if (!simulationRunId) return;

		// FIXME: siwtch to different simulation run result
		console.log('simulation run id changed to', simulationRunId);
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
	cellLabelRow[baseRow] = 'Base'; // change label of base row to base

	let divergingMaxMin = 0;
	const dataParameters = new Set();
	const dataParametersMax: any = {};
	const dataParametersMin: any = {};
	data.forEach((row) =>
		row.forEach((cell: any, j) => {
			// diverging max/min
			const cellBase: any = data[baseRow][j];
			const cellBaseDiff = Math.abs(cell.Infected - cellBase.Infected);
			divergingMaxMin = Math.max(divergingMaxMin, cellBaseDiff);

			// data parameters max/min
			Object.keys(cell).forEach((param) => {
				if (!dataParameters.has(param)) {
					dataParametersMin[param] = cell[param];
					dataParametersMax[param] = cell[param];
				} else {
					dataParametersMin[param] = Math.min(dataParametersMin[param], cell[param]);
					dataParametersMax[param] = Math.max(dataParametersMax[param], cell[param]);
				}
				dataParameters.add(param);
			});
		})
	);

	const fillColorFn = (datum: CellData, parametersMin: any, parametersMax: any) => {
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
		dataParametersMax,
		dataParametersMin,
		cellLabelCol,
		cellLabelAltCol,
		cellLabelRow,
		fillColorFn,
		lineColorFn: drilldownColorFn,
		barColorFn: drilldownColorFn,
		labelColFormatFn,
		divergingMaxMin
	};
}

const simData = parseSimData([run1, run2, run3, run4, run5, run6, run7, run8, run9]);
const scenarioDescriptionData = [
	run1Description,
	run2Description,
	run3Description,
	run4Description,
	run5Description,
	run6Description,
	run7Description,
	run8Description,
	run9Description
].map((o) => o.description);

const {
	data,
	cellLabelRow,
	cellLabelCol,
	cellLabelAltCol,
	fillColorFn,
	lineColorFn,
	barColorFn,
	labelColFormatFn
} = simData;

// ///////////////////////////////////////////////////////////////////////////////
// generate legend

const legendDiff = ref(null);
const legendBase = ref(null);
// FIXME: Render legend test
onMounted(() => {
	const legendCellW = 50;

	// diff legend
	const svgDiff = select(legendDiff.value as any);
	const maxDiff = simData.divergingMaxMin;
	const minDiff = -simData.divergingMaxMin;
	let cntDiff = 0;
	svgDiff.append('text').attr('x', 10).attr('y', 12).text(String(minDiff).slice(0, 7));
	const colorMapDiff = ['#c51b7d', 'mix', '#f7f7f7', 'mix', '#4d9221'];
	colorMapDiff[1] = mix(colorMapDiff[0], colorMapDiff[2], 0.5, 'lab');
	colorMapDiff[3] = mix(colorMapDiff[2], colorMapDiff[4], 0.5, 'lab');
	for (let i = 0; i < 5; i++) {
		svgDiff
			.append('rect')
			.attr('x', 63 + cntDiff * legendCellW)
			.attr('y', 0)
			.attr('width', legendCellW)
			.attr('height', 15)
			.style('fill', colorMapDiff[i]);
		cntDiff++;
	}
	svgDiff
		.append('text')
		.attr('x', 65 + legendCellW * cntDiff)
		.attr('y', 12)
		.text(String(maxDiff).slice(0, 6));

	// base legend
	const svgBase = select(legendBase.value as any);
	const maxBase = simData.dataParametersMax.Infected;
	const minBase = simData.dataParametersMin.Infected;
	let cntBase = 0;
	svgBase.append('text').attr('x', 10).attr('y', 12).text(String(minBase).slice(0, 7));
	const colorMapBase = ['#f7f7f7', 'mix', 'mix', 'mix', '#252525'];
	colorMapBase[1] = mix(colorMapBase[0], colorMapBase[4], 1 / 4, 'lab');
	colorMapBase[2] = mix(colorMapBase[0], colorMapBase[4], 2 / 4, 'lab');
	colorMapBase[3] = mix(colorMapBase[0], colorMapBase[4], 3 / 4, 'lab');
	for (let i = 0; i < 5; i++) {
		svgBase
			.append('rect')
			.attr('x', 63 + cntBase * legendCellW)
			.attr('y', 0)
			.attr('width', legendCellW)
			.attr('height', 15)
			.style('fill', colorMapBase[i]);
		cntBase++;
	}
	svgBase
		.append('text')
		.attr('x', 65 + legendCellW * cntBase)
		.attr('y', 12)
		.text(String(maxBase).slice(0, 6));
});
</script>

<template>
	<section class="result-container">
		<h3>Simulation Results - Difference from Baseline Scenario</h3>

		<div class="legend-container">
			<div class="legend-label">Difference w.r.t. Base (% Total Population Infected)</div>
			<div class="legend-label">Infected (% Total Population)</div>
		</div>
		<div class="legend-container">
			<svg ref="legendDiff" height="20px" width="370px"></svg>
			<svg ref="legendBase" height="20px" width="370px"></svg>
		</div>

		<div class="result">
			<ResponsiveMatrix
				:data="data"
				:fillColorFn="fillColorFn"
				:lineColorFn="lineColorFn"
				:barColorFn="barColorFn"
				:labelColFormatFn="labelColFormatFn"
				:cellLabelRow="cellLabelRow"
				:cellLabelAltRow="scenarioDescriptionData"
				:cellLabelCol="cellLabelCol"
				:cellLabelAltCol="cellLabelAltCol"
				:margin="40"
				:style="{ flex: '1' }"
			/>
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
	background: var(--un-color-body-surface-primary);
	margin: 10px;
}

.result {
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: row;
	padding: 20px;
}

h3 {
	font: var(--un-font-h3);
	margin-bottom: 10px;
}
</style>
