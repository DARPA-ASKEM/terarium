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
						<th v-for="(row, i) in matrix[0]" :key="i">{{ row.colCriteria }}</th>
					</tr>
				</thead>
				<tbody class="p-datatable-tbody">
					<tr v-for="(row, i) in matrix" :key="i">
						<td class="p-frozen-column">
							<template v-if="nodeType === NodeType.State">
								{{ Object.values(row[0].rowCriteria).join(' / ') }}
							</template>
							<template v-else>
								{{ row[0].rowCriteria }}
							</template>
						</td>

						<td
							v-for="(cell, j) in row"
							:key="j"
							tabindex="0"
							@keyup.enter="onEnterValueCell(cell?.value?.id, i, j)"
							@click="onEnterValueCell(cell?.value?.id, i, j)"
						>
							<template v-if="cell?.value">
								<InputText
									v-if="editableCellStates[i][j]"
									class="cell-input"
									v-model.lazy="valueToEdit"
									v-focus
									@focusout="updateModelConfigValue(cell.value.id, i, j)"
									@keyup.stop.enter="updateModelConfigValue(cell.value.id, i, j)"
								/>
								<div
									class="mathml-container"
									v-html="matrixExpressionsList[i]?.[j] ?? cell.value ?? '...'"
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
import {
	getMiraAMRPresentationData,
	getUnstratifiedParameters
} from '@/model-representation/petrinet/mira-petri';
import { createMatrix1D, createMatrix2D } from '@/utils/pivot';
import { Initial, ModelConfiguration, ModelParameter, Rate, Model } from '@/types/Types';
import { NodeType } from '@/model-representation/petrinet/petrinet-renderer';
import InputText from 'primevue/inputtext';
import { pythonInstance } from '@/python/PyodideController';

const props = defineProps<{
	modelConfiguration: ModelConfiguration;
	id: string;
	stratifiedModelType: StratifiedModelType;
	nodeType: NodeType;
	shouldEval: boolean;
}>();

const emit = defineEmits(['update-configuration']);

const colDimensions: string[] = [];
const rowDimensions: string[] = [];

const matrix = ref<any>([]);
const valueToEdit = ref('');
const editableCellStates = ref<boolean[][]>([]);

const matrixExpressionsList = ref<string[][]>([]);

const parametersValueMap = computed(() =>
	props.modelConfiguration.configuration?.semantics.ode.parameters.reduce((acc, val) => {
		acc[val.id] = val.value;
		return acc;
	}, {})
);

// const initialsList = computed(() => {
// 	const model = props.modelConfiguration.configuration as Model;
// 	const initials = model.semantics?.ode.initials;
// 	return initials?.map((initial) => initial.target);
// });

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

// See ES2_2a_start in "Eval do not touch"
// Returns the presentation mathml
async function getMatrixValue(variableName: string, shouldEvaluate: boolean) {
	const expressionBase = getMatrixExpression(variableName);
	// const expressionEvalTest = await pythonInstance.parseExpression(expressionBase);
	// console.log('evaluted', expressionBase, expressionEvalTest.freeSymbols);

	// Strip out initials the free symbols
	// const freeParamSymbols = expressionEvalTest.freeSymbols.filter(
	// 	(s) => initialsList.value?.includes(s) === false
	// );
	// if (freeParamSymbols.length === 1) {
	// 	return freeParamSymbols[0];
	// }

	/*
	console.log('debug', initialsList.value);
	let simplifiedStr = await pythonInstance.removeExpressions(expressionBase, initialsList.value as string[]);
	console.log('\tSimplified expr', expressionBase, simplifiedStr);
	*/

	if (shouldEvaluate) {
		const expressionEval = await pythonInstance.evaluateExpression(
			expressionBase,
			parametersValueMap.value
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

		emit('update-configuration', modelConfigurationClone);
		generateMatrix();
	}
}

function generateMatrix(populateDimensions = false) {
	const amr: Model = props.modelConfiguration.configuration;

	const { stateMatrixData, transitionMatrixData } =
		props.stratifiedModelType === StratifiedModelType.Catlab
			? getCatlabAMRPresentationData(amr)
			: getMiraAMRPresentationData(amr);

	// Get only the states/transitions that are mapped to the base model
	let matrixData: any[] = [];
	let inputs: string[] = [];
	let outputs: string[] = [];
	if (props.nodeType === NodeType.State) {
		matrixData = stateMatrixData.filter(({ base }) => base === props.id);
	} else {
		const paramsMap = getUnstratifiedParameters(amr);
		if (!paramsMap.has(props.id)) return [];

		// IDs to find within the rates
		const childParmaterIds = paramsMap.get(props.id) as string[];

		// ;

		// Holds all points that have the parameter
		matrixData = transitionMatrixData.filter((d) => {
			// Check if the transition's expression include the usage
			const rate = amr.semantics?.ode.rates.find((r) => r.target === d.id);
			if (!rate) return false;

			// FIXME: should check through sympy to be more accurate
			if (rate.expression.includes(props.id)) return true;
			for (let i = 0; i < childParmaterIds.length; i++) {
				if (rate.expression.includes(childParmaterIds[i])) return true;
			}
			return false;
		});
		console.log(childParmaterIds);
		console.log('matrix data', matrixData);

		// Get inputs and outputs
		for (let i = 0; i < matrixData.length; i++) {
			const id = matrixData[i].id;
			const { input, output } = amr.model.transitions.find((t) => t.id === id);
			inputs.push(...input);
			outputs.push(...output);
		}

		// Get unique inputs and outputs and sort names alphabetically
		inputs = [...new Set(inputs)].sort();
		outputs = [...new Set(outputs)].sort();

		const rows: any[] = [];
		for (let rowIdx = 0; rowIdx < inputs.length; rowIdx++) {
			const row: any[] = [];
			for (let colIdx = 0; colIdx < outputs.length; colIdx++) {
				// If there is an input output pair that matches then a parameter belongs in this cell
				let value: any = null;

				// Get inputs and outputs
				for (let i = 0; i < matrixData.length; i++) {
					const id = matrixData[i].id;
					const { input, output } = amr.model.transitions.find((t) => t.id === id);

					if (input.includes(inputs[rowIdx]) && output.includes(outputs[colIdx])) {
						const rate = amr.semantics?.ode.rates.find((r) => r.target === id);
						if (!rate) {
							value = id;
							break;
						}
						for (let j = 0; j < childParmaterIds.length; j++) {
							if (rate.expression.includes(childParmaterIds[j])) {
								value =
									amr.semantics?.ode.parameters?.find((p) => p.id === childParmaterIds[j])?.value ??
									null;
								break;
							}
						}
					}
				}

				row.push({
					row: rowIdx,
					col: colIdx,
					rowCriteria: inputs[rowIdx],
					colCriteria: outputs[colIdx],
					value
				});
			}
			rows.push(row);
		}

		console.log(inputs, outputs, rows);

		matrix.value = rows;
		return matrixData;
	}

	if (isEmpty(matrixData)) return matrixData;

	if (populateDimensions) {
		const dimensions = [cloneDeep(matrixData)[0]].map((d) => {
			delete d.id;
			delete d.base;
			return Object.keys(d);
		})[0];

		console.log(dimensions);

		rowDimensions.push(...dimensions);
		colDimensions.push(...dimensions);
	}

	const matrixAttributes =
		props.nodeType === NodeType.State
			? createMatrix1D(matrixData)
			: createMatrix2D(matrixData, colDimensions, rowDimensions);

	matrix.value = matrixAttributes.matrix;
	console.log(matrix.value);

	return matrixData;
}

function configureMatrix() {
	const matrixData = generateMatrix(true);
	if (isEmpty(matrixData)) return;

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
