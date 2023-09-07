<template>
	<div
		v-if="!isEmpty(matrix)"
		class="p-datatable p-component p-datatable-scrollable p-datatable-responsive-scroll p-datatable-gridlines p-datatable-grouped-header stratified-value-matrix"
	>
		<div class="p-datatable-wrapper">
			<table class="p-datatable-table p-datatable-scrollable-table editable-cells-table">
				<thead v-if="nodeType === NodeType.Transition" class="p-datatable-thead">
					<tr>
						<th class="choose-criteria"></th>
						<th v-for="(row, i) in matrix[0]" :key="i">
							{{ Object.values(row.colCriteria).join(' / ') }}
						</th>
					</tr>
				</thead>
				<tbody class="p-datatable-tbody">
					<tr v-for="(row, i) in matrix" :key="i">
						<td class="p-frozen-column">{{ Object.values(row[0].rowCriteria).join(' / ') }}</td>
						<td
							v-for="(cell, j) in row"
							:key="j"
							tabindex="0"
							@keyup.enter="onEnterValueCell(cell?.value?.id, i, j)"
							@click="onEnterValueCell(cell?.value?.id, i, j)"
						>
							<template v-if="cell?.value?.id">
								<InputText
									v-if="editableCellStates[i][j]"
									class="cell-input"
									v-model.lazy="valueToEdit"
									v-focus
									@focusout="updateModelConfigValue(cell.value.id, i, j)"
									@keyup.stop.enter="updateModelConfigValue(cell.value.id, i, j)"
								/>
								<div
									v-else
									class="mathml-container"
									v-html="matrixExpressionsList[i]?.[j] ?? '...'"
								></div>
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
import { ref, onMounted, computed, watch } from 'vue';
import { cloneDeep, isEmpty } from 'lodash';
import { StratifiedModelType } from '@/model-representation/petrinet/petrinet-service';
import { getCatlabAMRPresentationData } from '@/model-representation/petrinet/catlab-petri';
import { getMiraAMRPresentationData } from '@/model-representation/petrinet/mira-petri';
import { createMatrix1D, createMatrix2D } from '@/utils/pivot';
import { Initial, ModelConfiguration, ModelParameter, Rate, Model } from '@/types/Types';
import { NodeType } from '@/model-representation/petrinet/petrinet-renderer';
import InputText from 'primevue/inputtext';
import { updateModelConfiguration } from '@/services/model-configurations';

import { pythonInstance } from '@/python/PyodideController';

const props = defineProps<{
	modelConfiguration: ModelConfiguration;
	id: string;
	stratifiedModelType: StratifiedModelType;
	nodeType: NodeType;
	shouldEval: boolean;
}>();

const colDimensions: string[] = [];
const rowDimensions: string[] = [];

const matrix = ref<any>([]);
const chosenCol = ref('');
const chosenRow = ref('');
const valueToEdit = ref('');
const editableCellStates = ref<boolean[][]>([]);

const matrixExpressionsList = ref<string[][]>([]);

const parametersValueList = computed(() =>
	props.modelConfiguration.configuration?.semantics.ode.parameters.reduce((acc, val) => {
		acc[val.id] = val.value;
		return acc;
	}, {})
);

watch(
	() => [matrix.value, props.shouldEval],
	async () => {
		const output: string[][] = [];
		await Promise.all(
			matrix.value
				.map((row) =>
					row.map(async (cell) => {
						if (cell.value?.id) {
							if (!output[cell.row]) {
								output[cell.row] = [];
							}
							output[cell.row][cell.col] = await getMatrixValue(cell.value.id, props.shouldEval);
						}
					})
				)
				.flat()
		);
		matrixExpressionsList.value = output;
	}
);

// Makes cell inputs focus once they appear
const vFocus = {
	mounted: (el) => el.focus()
};

function onEnterValueCell(variableName: string, rowIdx: number, colIdx: number) {
	valueToEdit.value = getMatrixExpression(variableName);
	editableCellStates.value[rowIdx][colIdx] = true;
}

// Finds where to get the value within the AMR based on the variable name
function findOdeObjectLocation(variableName: string): {
	odeFieldObject: Rate & Initial & ModelParameter;
	fieldName: string;
	fieldIndex: number;
} | null {
	const ode = props.modelConfiguration.configuration?.semantics?.ode;
	if (!ode) return null;

	const fieldNames = ['rates', 'initials', 'parameters'];

	for (let i = 0; i < fieldNames.length; i++) {
		const fieldIndex = ode[fieldNames[i]].findIndex(
			({ target, id }) => target === variableName || id === variableName
		);
		if (fieldIndex === -1) continue;

		return {
			odeFieldObject: ode[fieldNames[i]][fieldIndex],
			fieldName: fieldNames[i],
			fieldIndex
		};
	}
	return null;
}

function getMatrixExpression(variableName: string) {
	const odeObjectLocation = findOdeObjectLocation(variableName);
	if (odeObjectLocation) {
		const { odeFieldObject } = odeObjectLocation;
		return odeFieldObject?.expression ?? odeFieldObject?.value;
	}
	return variableName;
}

// Returns the presentation mathml
async function getMatrixValue(variableName: string, shouldEvaluate: boolean) {
	const expressionBase = getMatrixExpression(variableName);

	if (shouldEvaluate) {
		const expressionEval = await pythonInstance.evaluateExpression(
			expressionBase,
			parametersValueList.value
		);
		return (await pythonInstance.parseExpression(expressionEval)).pmathml;
	}

	return (await pythonInstance.parseExpression(expressionBase)).pmathml;
}

async function updateModelConfigValue(variableName: string, rowIdx: number, colIdx: number) {
	editableCellStates.value[rowIdx][colIdx] = false;
	const newValue = valueToEdit.value;
	const odeObjectLocation = findOdeObjectLocation(variableName);

	if (odeObjectLocation) {
		const { odeFieldObject, fieldName, fieldIndex } = odeObjectLocation;

		// Update if the value is different
		if (odeFieldObject.expression) {
			if (odeFieldObject.expression === newValue) return;

			// If expression changed, we want to update the the twin fields
			// - expression
			// - expression_mathml
			odeFieldObject.expression = newValue;
			const mathml = (await pythonInstance.parseExpression(newValue)).mathml;
			odeFieldObject.expression_mathml = mathml;
		} else if (odeFieldObject.value) {
			if (odeFieldObject.value === Number(newValue)) return;
			odeFieldObject.value = Number(newValue);
		}

		const modelConfigurationClone = cloneDeep(props.modelConfiguration);
		modelConfigurationClone.configuration.semantics.ode[fieldName][fieldIndex] = odeFieldObject;

		await updateModelConfiguration(modelConfigurationClone);
		generateMatrix();
	}
}

function generateMatrix(populateDimensions = false) {
	const amr: Model = props.modelConfiguration.configuration;

	const result =
		props.stratifiedModelType === StratifiedModelType.Catlab
			? getCatlabAMRPresentationData(amr)
			: getMiraAMRPresentationData(amr);

	// Get only the states/transitions that are mapped to the base model
	const matrixData =
		props.nodeType === NodeType.State
			? result.stateMatrixData.filter(({ base }) => base === props.id)
			: result.transitionMatrixData.filter(({ base }) => base === props.id);

	if (isEmpty(matrixData)) return matrixData;

	if (populateDimensions) {
		const dimensions = [cloneDeep(matrixData)[0]].map((d) => {
			delete d.id;
			delete d.base;
			return Object.keys(d);
		})[0];

		rowDimensions.push(...dimensions);
		colDimensions.push(...dimensions);
	}

	const matrixAttributes =
		props.nodeType === NodeType.State
			? createMatrix1D(matrixData)
			: createMatrix2D(matrixData, colDimensions, rowDimensions);

	matrix.value = matrixAttributes.matrix;

	return matrixData;
}

function configureMatrix() {
	const matrixData = generateMatrix(true);
	if (isEmpty(matrixData)) return;

	chosenCol.value = colDimensions[0];
	chosenRow.value = rowDimensions[0];

	// Matrix for editable cell states
	matrix.value.forEach((m) => editableCellStates.value.push(Array(m.length).fill(false)));
}

onMounted(() => {
	configureMatrix();
});
</script>

<style scoped>
.p-datatable {
	max-width: 80vw;
}

.p-datatable .p-datatable-thead > tr > th.choose-criteria {
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
	min-width: 8rem;
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
