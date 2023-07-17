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
							v-model="chosenCol"
							:options="colDimensions"
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
							v-model="chosenRow"
							:options="rowDimensions"
						/>
					</td>
					<td v-for="(cell, j) in row" :key="j">
						<template v-if="cell?.value?.[chosenCol] && cell?.value?.[chosenRow]">
							{{ cell?.value?.[chosenCol] }}
							{{ cell?.value?.[chosenRow] }}
						</template>
						<span class="not-allowed" v-else>N/A</span>
					</td>
				</tr>
			</tbody>
		</table>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import {
	// extractMapping,
	// extractStateMatrixData,
	extractTransitionMatrixData
} from '@/model-representation/petrinet/petrinet-service';
import { createMatrix } from '@/utils/pivot';
import Dropdown from 'primevue/dropdown';
import { Model } from '@/types/Types';

const props = defineProps<{
	model: Model;
}>();

let colDimensions: string[] = [];
let rowDimensions: string[] = [];

const matrix = ref();
const chosenCol = ref(colDimensions[0]);
const chosenRow = ref(rowDimensions[0]);

function configureMatrix() {
	const transitionMatrixData = extractTransitionMatrixData(
		props.model,
		props.model.model.transitions.map(({ id }) => id)
	);
	console.log(transitionMatrixData);

	const matrixAttributes = createMatrix(transitionMatrixData, ['Pop'], ['id']); // colDimensions, rowDimensions);
	matrix.value = matrixAttributes.matrix;
	colDimensions = matrixAttributes.colDimensions;
	rowDimensions = matrixAttributes.rowDimensions;

	chosenCol.value = colDimensions[0];
	chosenRow.value = rowDimensions[0];

	console.log(matrix.value);
}

onMounted(() => {
	configureMatrix();
});

// const stateList = extractMapping(props.model, 'S_Rgn_1');
// const stateList2 = extractMapping(props.model, 'rec_Rgn1_dis');
// console.log(stateList, stateList2);

// const data = extractStateMatrixData(props.model, stateList)
// console.log(data);
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
