<template>
	<div
		v-if="matrix"
		class="p-datatable p-component p-datatable-scrollable p-datatable-responsive-scroll p-datatable-gridlines p-datatable-grouped-header"
	>
		<table class="p-datatable-table p-datatable-scrollable-table editable-cells-table">
			<thead class="p-datatable-thead">
				<tr>
					<th v-for="i in matrix[0].length + 1" :key="i">
						<Dropdown
							v-if="i === 2"
							placeholder="Select a variable"
							v-model="chosenX"
							:options="xDimensions"
						/>
					</th>
				</tr>
			</thead>
			<tbody class="p-datatable-tbody">
				<tr v-for="(row, i) in matrix" :key="i">
					<td class="p-frozen-column">
						<Dropdown
							v-if="i === 0"
							placeholder="Select a variable"
							v-model="chosenY"
							:options="yDimensions"
						/>
					</td>
					<td v-for="(cell, j) in row" :key="j">
						<template v-if="cell?.value?.[chosenX] && cell?.value?.[chosenY]">
							{{ cell?.value?.[chosenX] }}
							{{ cell?.value?.[chosenY] }}
						</template>
						<span class="not-allowed" v-else>N/A</span>
					</td>
				</tr>
			</tbody>
		</table>
	</div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { matrixStrata } from '@/temp/models/matrix_strata';
import { stratify_output } from '@/temp/models/stratify_output';
import {
	extractMapping,
	extractStateMatrixData
} from '@/model-representation/petrinet/petrinet-service';
// import { createMatrix } from '@/utils/pivot';
import Dropdown from 'primevue/dropdown';

const { matrix, xDimensions, yDimensions } = matrixStrata;

const chosenX = ref(xDimensions[0]);
const chosenY = ref(yDimensions[0]);

const stateList = extractMapping(stratify_output, 'S_Rgn_1');
const stateList2 = extractMapping(stratify_output, 'rec_Rgn1_dis');
console.log(stateList, stateList2);

console.log(extractStateMatrixData(stratify_output, stateList));

// console.log(createMatrix(stratify_output.semantics.span, 'S_Rgn_1', 'S_Rgn_2'));

// console.log(create)
</script>

<style scoped>
td,
th {
	text-align: center;
	width: 4rem;
}

.p-frozen-column {
	left: 0px;
	white-space: nowrap;
}

.not-allowed {
	color: var(--text-color-subdued);
}
</style>
