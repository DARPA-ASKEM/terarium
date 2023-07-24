<template>
	<div
		v-if="matrix"
		class="p-datatable p-component p-datatable-scrollable p-datatable-responsive-scroll p-datatable-gridlines p-datatable-grouped-header stratified-value-matrix"
	>
		<div class="p-datatable-wrapper">
			<table class="p-datatable-table p-datatable-scrollable-table editable-cells-table">
				<thead class="p-datatable-thead">
					<tr>
						<th class="choose-criteria"></th>
						<th class="choose-criteria"></th>
						<th v-for="i in matrix[0].length + 1" :key="i" class="choose-criteria">
							<Dropdown
								v-if="i === 1"
								class="w-full"
								placeholder="Select a variable"
								v-model="chosenCol"
								:options="colDimensions"
							/>
						</th>
					</tr>
					<tr>
						<th class="choose-criteria"></th>
						<th class="choose-criteria"></th>
						<th v-for="(row, i) in matrix[0]" :key="i">
							{{ row.colCriteria?.[chosenCol] }}
						</th>
					</tr>
				</thead>
				<tbody class="p-datatable-tbody">
					<tr v-for="(row, i) in matrix" :key="i">
						<td class="p-frozen-column">
							<Dropdown
								v-if="i === 0"
								class="w-full"
								placeholder="Select a variable"
								v-model="chosenRow"
								:options="rowDimensions"
							/>
						</td>
						<td class="p-frozen-column">{{ row[0].rowCriteria?.[chosenRow] }}</td>
						<td v-for="(cell, j) in row" :key="j">
							<template v-if="cell?.value?.[chosenCol] && cell?.value?.[chosenRow]">
								{{ findMatrixValue(cell?.value?.[chosenCol]) }}
							</template>
							<span class="not-allowed" v-else>N/A</span>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { isEmpty } from 'lodash';
import {
	extractStateMatrixData,
	extractTransitionMatrixData
} from '@/model-representation/petrinet/petrinet-service';
import { createMatrix } from '@/utils/pivot';
import Dropdown from 'primevue/dropdown';
import { Model } from '@/types/Types';
import { NodeType } from '@/model-representation/petrinet/petrinet-renderer';

const props = defineProps<{
	model: Model;
	id: string;
	nodeType: NodeType;
}>();

let colDimensions: string[] = [];
let rowDimensions: string[] = [];

const matrix = ref();
const chosenCol = ref('');
const chosenRow = ref('');

// Makes cell inputs focus once they appear
// const vFocus = {
//     mounted: (el) => el.focus()
// };

function findMatrixValue(variableName: string) {
	const ode = props.model.semantics?.ode;

	if (!ode) return variableName;

	let matrixValue: string = '';
	const odeFields = ['rates', 'initials', 'parameters'];

	for (let i = 0; i < odeFields.length; i++) {
		const odeFieldObject = ode[odeFields[i]].find(
			({ target, id }) => target === variableName || id === variableName
		);

		matrixValue = odeFieldObject?.expression ?? odeFieldObject?.value ?? '';

		if (!isEmpty(matrixValue)) break;
	}
	return matrixValue;
}

function configureMatrix() {
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

	// Get only the states/transitions that are mapped to the base model
	const matrixData =
		props.nodeType === NodeType.State
			? extractStateMatrixData(props.model, rowAndCol, [...colDimensions, ...rowDimensions])
			: extractTransitionMatrixData(props.model, rowAndCol);

	const matrixAttributes = createMatrix(matrixData, colDimensions, rowDimensions);
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
.p-datatable {
	height: 10rem;
}

.p-datatable .p-datatable-thead > tr > th.choose-criteria,
.p-datatable-scrollable .p-frozen-column:first-child {
	padding: 0;
	background: var(--surface-ground);
	border: none;
}

.p-datatable .p-datatable-thead > tr > th.choose-criteria {
	padding-bottom: 1rem;
}

.p-datatable-scrollable .p-frozen-column:first-child {
	padding-right: 1rem;
}

.p-datatable-scrollable .p-frozen-column {
	font-weight: bold;
}

.p-datatable .p-datatable-thead > tr > th {
	padding-bottom: 1rem;
}

/* .p-frozen-column {
	left: 0px;
	white-space: nowrap;
}

.second-frozen {
	left: 12rem;
}

.p-frozen-column,
th {
	background: transparent;
} */

.p-datatable-scrollable .p-frozen-column {
	padding-right: 1rem;
}

.p-dropdown {
	min-width: 11rem;
}

.not-allowed {
	color: var(--text-color-subdued);
}
</style>
