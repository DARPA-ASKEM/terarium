<template>
	<div style="display: flex; flex-direction: column">
		<p style="font-size: 100%; background: #ffc; padding: 5px"></p>
		<br />
		<table class="table" style="border: 1px solid; cellpadding: 0; cellspacing: 0">
			<tr v-for="(row, rowIdx) of rows" :key="rowIdx">
				<td v-for="(val, colIdx) of row" :key="colIdx" style="border: 1px solid">
					({{ val.row }} , {{ val.col }}) : {{ val.value?.id ?? 'n/a' }}
				</td>
			</tr>
		</table>
	</div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { Model } from '@/types/Types';
import { createMatrix2D } from '@/utils/pivot';
// import { extractMapping } from '@/model-representation/petrinet/petrinet-service';

import { amr } from '@/temp/catlab-strata-x2';

const stateIds = amr.model.states.map((d) => d.id);
const transitions = amr.model.transitions;

const getCatlabStratasForId = (amr: Model, id: string) => {
	let span: any = amr.semantics?.span;
	let key = id;

	let c = 0;
	const result = {};
	while (true) {
		c++;
		const dimension = span[1].system.name;
		const term = span[1].map.find((d) => d[0] === key)[1];

		// console.log(dimension, term);
		result[dimension] = term;

		key = span[0].map.find((d) => d[0] === key)[1];
		span = span[0].system.semantics.span;
		if (!span || c > 10) {
			result['id'] = id;
			result['@base'] = key;
			break;
		}
	}
	return result;

	/*
	let key = id;

	const result = {};
	let span: any = amr.semantics?.span;
	while (true) {
		const tokens = key.split('_');
		const term = tokens.pop();
		const dimension = span[1].system.name;
		result[dimension] = term;

		if (tokens.length < 2) {
			result['@id'] = id;
			result['@base'] = tokens[0];
			break;
		}
		key = tokens.join('_');
		span = span[0].system.semantics.span;
	}
	return result;
	*/
};

const getCatlabStatesMatrixData = (amr: Model) => {
	const results: any[] = [];
	for (let i = 0; i < stateIds.length; i++) {
		const result = getCatlabStratasForId(amr as any, stateIds[i]);
		results.push(result);
	}
	return results;
};

const getCatlabTransitionsMatrixData = (amr: Model) => {
	const results: any[] = [];
	for (let i = 0; i < transitions.length; i++) {
		const transition = transitions[i];
		let result = {};

		transition.input.forEach((stateId) => {
			result = Object.assign(result, getCatlabStratasForId(amr as any, stateId));
		});
		transition.output.forEach((stateId) => {
			result = Object.assign(result, getCatlabStratasForId(amr as any, stateId));
		});

		let c = 0;
		let key = transition.id;
		let span: any = amr.semantics?.span;

		while (true) {
			c++;
			key = span[0].map.find((d) => d[0] === key)[1];
			span = span[0].system.semantics.span;
			if (!span || c > 10) {
				result['@base'] = key;
				result['id'] = transition.id;
				break;
			}
		}
		results.push(result);
	}
	return results;
};

const statesMatrixData = getCatlabStatesMatrixData(amr as any);
const transitionsMatrixData = getCatlabTransitionsMatrixData(amr as any);

console.log('states', statesMatrixData);
console.log('transitions', transitionsMatrixData);

const xDim = ['Age-contact strata model', 'Location-travel strata model'];
const yDim = ['Age-contact strata model', 'Location-travel strata model'];
const result = createMatrix2D(
	transitionsMatrixData.filter((d) => d['@base'] === 'inf'),
	xDim,
	yDim
);
const rows = ref<any>(result.matrix);

console.log(result);

// Given a state, extract all state of the same type
// const stateIds = amr.semantics.span[0].map.filter(d => d[1] === 'S_L1_A1').map(d => d[0]);
// const stateIds = amr.semantics.span[0].map;
// console.log(stateIds);

// const stateIds = amr.model.transitions.map(d => d.id);
// const transitionIds = amr.model.transitions.map(d => d.id);
// console.log(transitionIds);

/*
const testData = extractTransitionMatrixData(amr, testT);

console.log(testData);
console.log(testT);


const xDim = ['Disease', 'Disease', 'Disease'];
const yDim = ['Disease', 'Disease', 'Disease'];
const result = createMatrix(testData, xDim, yDim);
const rows = ref<any>(result.matrix);
*/

// const rows = ref<any>([]);
</script>

<style scoped>
table {
	border-spacing: 0px;
}
th,
td {
	padding: 5px;
}
</style>
