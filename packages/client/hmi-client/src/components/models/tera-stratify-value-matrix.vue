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
	// extractType,
	// extractStateMatrixData,
	extractTransitionMatrixData
} from '@/model-representation/petrinet/petrinet-service';
import { createMatrix } from '@/utils/pivot';
import Dropdown from 'primevue/dropdown';
import { Model } from '@/types/Types';

const props = defineProps<{
	model: Model;
	id: string;
}>();

let colDimensions: string[] = [];
let rowDimensions: string[] = [];

const matrix = ref();
const chosenCol = ref('');
const chosenRow = ref('');

function configureMatrix() {
	const transitionMatrixData = extractTransitionMatrixData(
		props.model,
		props.model.model.transitions.map(({ id }) => id)
	);

	// Get stratified ids from the chosen base id
	const rowAndCol = props.model?.semantics?.span?.[0]?.map
		.filter((id: string) => id[1] === props.id)
		.map((d) => d[0]);

	// Assign dimensions relevant to the ids
	const rowAndColDimensions = props.model.semantics?.typing?.map.filter((id) =>
		rowAndCol.includes(id[0])
	);

	if (rowAndColDimensions) {
		// Assuming column id is the first one
		const colId = rowAndColDimensions[0][0];
		let rowId: string | null = null;

		for (let i = 0; i < rowAndColDimensions?.length; i++) {
			// If other id is recognized it is the row id
			if (!rowId && rowAndColDimensions[i][0] !== colId) {
				rowId = rowAndColDimensions[i][0];
			}

			// Push dimension
			if (colId === rowAndColDimensions[i][0]) {
				colDimensions.push(rowAndColDimensions[i][1]);
			} else if (rowId === rowAndColDimensions[i][0]) {
				rowDimensions.push(rowAndColDimensions[i][1]);
			}
		}
	}

	const matrixAttributes = createMatrix(transitionMatrixData, colDimensions, rowDimensions);
	matrix.value = matrixAttributes.matrix;
	colDimensions = matrixAttributes.colDimensions;
	rowDimensions = matrixAttributes.rowDimensions;

	chosenCol.value = colDimensions[0];
	chosenRow.value = rowDimensions[0];
}

onMounted(() => {
	configureMatrix();
});
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
