<template>
	<div
		v-if="matrix"
		class="p-datatable p-component p-datatable-scrollable p-datatable-responsive-scroll p-datatable-gridlines p-datatable-grouped-header stratified-value-matrix"
	>
		<div class="p-datatable-wrapper">
			<table class="p-datatable-table p-datatable-scrollable-table editable-cells-table">
				<thead v-if="nodeType === NodeType.Transition" class="p-datatable-thead">
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
						<td
							v-for="(cell, j) in row"
							:key="j"
							@click="
								valueToEdit = {
									value: findMatrixValue(cell?.value?.[chosenCol]),
									rowIdx: i,
									colIdx: j
								}
							"
						>
							<template v-if="cell?.value?.[chosenCol] && cell?.value?.[chosenRow]">
								<InputText
									v-if="valueToEdit.rowIdx === i && valueToEdit.colIdx === j"
									class="cell-input"
									v-model.lazy="valueToEdit.value"
									v-focus
									@focusout="valueToEdit = { value: '', rowIdx: -1, colIdx: -1 }"
									@keyup.stop.enter="updateModelConfigValue(cell?.value?.[chosenRow])"
								/>
								<span v-else class="editable-cell">
									{{ findMatrixValue(cell?.value?.[chosenCol]) }}
								</span>
							</template>
							<span v-else class="not-allowed">N/A</span>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { isEmpty, cloneDeep } from 'lodash';
import {
	extractStateMatrixData,
	extractTransitionMatrixData
} from '@/model-representation/petrinet/petrinet-service';
import { createStateMatrix, createTransitionMatrix } from '@/utils/pivot';
import Dropdown from 'primevue/dropdown';
import { ModelConfiguration } from '@/types/Types';
import { NodeType } from '@/model-representation/petrinet/petrinet-renderer';
import InputText from 'primevue/inputtext';
import { updateModelConfiguration } from '@/services/model-configurations';

const props = defineProps<{
	modelConfiguration: ModelConfiguration;
	id: string;
	nodeType: NodeType;
}>();

const colDimensions: string[] = [];
const rowDimensions: string[] = [];

const matrix = ref();
const chosenCol = ref('');
const chosenRow = ref('');
const valueToEdit = ref({ value: '', rowIdx: -1, colIdx: -1 });

// Makes cell inputs focus once they appear
const vFocus = {
	mounted: (el) => el.focus()
};

function findMatrixValue(variableName: string) {
	const ode = props.modelConfiguration.configuration?.semantics?.ode;

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

function updateModelConfigValue(variableName: string) {
	const ode = props.modelConfiguration.configuration?.semantics?.ode;

	if (!ode) return;

	const odeFields = ['rates', 'initials', 'parameters'];

	for (let i = 0; i < odeFields.length; i++) {
		const odeFieldObject = ode[odeFields[i]].find(
			({ target, id }) => target === variableName || id === variableName
		);

		const indexToChange = ode[odeFields[i]].findIndex(
			({ target, id }) => target === variableName || id === variableName
		);

		if (!odeFieldObject) continue;

		if (odeFieldObject.expression) odeFieldObject.expression = valueToEdit.value.value;
		else if (odeFieldObject.value) odeFieldObject.value = valueToEdit.value.value;

		const modelConfigurationClone = cloneDeep(props.modelConfiguration);

		modelConfigurationClone.configuration.semantics.ode[odeFields[i]][indexToChange] =
			odeFieldObject;

		updateModelConfiguration(modelConfigurationClone);

		console.log(props.modelConfiguration, modelConfigurationClone);

		break;
	}

	valueToEdit.value = { value: '', rowIdx: -1, colIdx: -1 };
}

function configureMatrix() {
	// Get stratified ids from the chosen base id
	const rowAndCol = props.modelConfiguration.configuration?.semantics?.span?.[0]?.map
		.filter((id: string) => id[1] === props.id)
		.map((d) => d[0]);

	// Assign dimensions relevant to the ids
	const rowAndColDimensions = props.modelConfiguration.configuration.semantics?.typing?.map.filter(
		(id) => rowAndCol.includes(id[0])
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
			? extractStateMatrixData(props.modelConfiguration.configuration, rowAndCol, [
					...colDimensions,
					...rowDimensions
			  ])
			: extractTransitionMatrixData(props.modelConfiguration.configuration, rowAndCol);

	const matrixAttributes =
		props.nodeType === NodeType.State
			? createStateMatrix(matrixData)
			: createTransitionMatrix(matrixData, colDimensions, rowDimensions);

	matrix.value = matrixAttributes.matrix;

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

.editable-cell {
	display: flex;
	justify-content: space-between;
	align-items: center;
	visibility: visible;
	width: 100%;
}

.cell-input {
	height: 4rem;
	width: 100%;
	padding-left: 12px;
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
