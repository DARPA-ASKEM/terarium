<script setup lang="ts">
import { watch } from 'vue';
import { useRoute } from 'vue-router';
import { NumberValue, scaleTime } from 'd3';
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
import run10 from './simulation-run-data/10/output.json';
import run11 from './simulation-run-data/11/output.json';
import run12 from './simulation-run-data/12/output.json';
import run13 from './simulation-run-data/13/output.json';
import run14 from './simulation-run-data/14/output.json';

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

function parseSimData(input) {
	if (input.constructor !== Array) {
		input = [input];
	}

	// assume timesteps are identical in all runs
	const maxTimesteps = 20;
	// eslint-disable-next-line no-underscore-dangle
	const cellLabelCol = input[0]._time.value.map((v) => new Date(v)).slice(0, maxTimesteps);
	const cellLabelRow = [
		'run1',
		'run2',
		'run3',
		'run4',
		'run5',
		'run6',
		'run7',
		'run8',
		'run9',
		'run10',
		'run11',
		'run12',
		'run13',
		'run14'
	];
	const numTimesteps = maxTimesteps || cellLabelCol.length;

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

	// find max diverging max/min
	const baseRow = 0;
	cellLabelRow[baseRow] = 'Base'; // change label of base row to base
	let divergingMaxMin = 0;
	data.forEach((row) =>
		row.forEach((cell: any, j) => {
			const cellBase: any = data[baseRow][j];
			const cellBaseDiff = Math.abs(cell.Infected - cellBase.Infected);
			divergingMaxMin = Math.max(divergingMaxMin, cellBaseDiff);
		})
	);

	const fillColorFn = (datum: CellData) => {
		const colorExtremePos = '#4d9221';
		const colorExtremeNeg = '#c51b7d';
		const colorMid = '#f7f7f7';
		const datumBase: any = data[baseRow][datum.col];

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

	const labelColFormatFn = scaleTime().tickFormat() as (value: NumberValue) => string;

	return {
		data,
		cellLabelCol,
		cellLabelRow,
		fillColorFn,
		labelColFormatFn
	};
}

const simData = parseSimData([
	run1,
	run2,
	run3,
	run4,
	run5,
	run6,
	run7,
	run8,
	run9,
	run10,
	run11,
	run12,
	run13,
	run14
]);

const { data, cellLabelRow, cellLabelCol, labelColFormatFn, fillColorFn } = simData;
</script>

<template>
	<section>
		<h3>Simulation Results</h3>
		<div class="result-container">
			<ResponsiveMatrix
				:data="data"
				:fillColorFn="fillColorFn"
				:labelColFormatFn="labelColFormatFn"
				:cellLabelRow="cellLabelRow"
				:cellLabelCol="cellLabelCol"
				:style="{ flex: '1' }"
			/>
		</div>
	</section>
</template>

<style scoped>
.result-container {
	width: 25rem;
	height: 25rem;
	display: flex;
	flex-direction: row;
}

h3 {
	font: var(--un-font-h3);
	margin-bottom: 10px;
}
</style>
