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
import { createMatrix2D } from '@/utils/pivot';
import { amr } from '@/temp/catlab-strata-x2';
import {
	getCatlabTransitionsMatrixData,
	getCatlabStatesMatrixData
} from '@/model-representation/petrinet/petrinet-service';

const statesMatrixData = getCatlabStatesMatrixData(amr as any);
const transitionsMatrixData = getCatlabTransitionsMatrixData(amr as any);

console.log('states', JSON.stringify(statesMatrixData));
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
