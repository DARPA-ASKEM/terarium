<template>
	<div
		v-if="!isEmpty(matrix)"
		class="p-datatable p-component p-datatable-scrollable p-datatable-responsive-scroll p-datatable-gridlines p-datatable-grouped-header stratified-value-matrix"
	>
		<div class="p-datatable-wrapper">
			<table class="p-datatable-table p-datatable-scrollable-table editable-cells-table">
				<thead v-if="matrix[0].length > 1" class="p-datatable-thead">
					<tr>
						<th v-if="matrix.length > 1" class="choose-criteria"></th>
						<th v-for="(row, i) in matrix[0]" :key="i">{{ row.colCriteria }}</th>
					</tr>
				</thead>
				<tbody class="p-datatable-tbody">
					<template v-for="(row, i) in matrix" :key="i">
						<tr v-for="(controller, j) in controllers" :key="j">
							<td v-if="matrix.length > 1" class="p-frozen-column">
								<template v-if="nodeType === NodeType.State">
									{{ Object.values(row[0].rowCriteria).join(' / ') }}
								</template>
								<template v-else>
									{{ row[0].rowCriteria
									}}<template v-if="controller !== ''">, {{ controller }} </template>
								</template>
							</td>
							<td
								v-for="(cell, k) in row"
								:key="k"
								tabindex="0"
								@keyup.enter="onEnterValueCell(cell?.content?.id, i, k)"
								@click="onEnterValueCell(cell?.content?.id, i, k)"
							>
								<template v-if="cell.content.id">
									<InputText
										v-if="editableCellStates[i][k]"
										class="cell-input"
										v-model.lazy="valueToEdit"
										v-focus
										@focusout="updateModelConfigValue(cell.content.id, i, k)"
										@keyup.stop.enter="updateModelConfigValue(cell.content.id, i, k)"
									/>
									<div
										v-else-if="nodeType === NodeType.State"
										class="mathml-container"
										v-html="matrixExpressionsList?.[i]?.[k] ?? '...'"
									/>
									<div v-else>
										{{ shouldEval ? cell?.content.value : cell?.content.id ?? '...' }}
									</div>
								</template>
								<span v-else class="not-allowed">N/A</span>
							</td>
						</tr>
					</template>
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
	getUnstratifiedParameters,
	filterParameterLocations
} from '@/model-representation/petrinet/mira-petri';
import { createMatrix1D, createParameterMatrix } from '@/utils/pivot';
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
const controllers = ref<string[]>([]);
const valueToEdit = ref('');
const editableCellStates = ref<boolean[][]>([]);

const matrixExpressionsList = ref<string[][]>([]);

const parametersValueMap = computed(() =>
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
						if (cell.content?.id) {
							if (!output[cell.row]) {
								output[cell.row] = [];
							}
							output[cell.row][cell.col] = await getMatrixValue(cell.content.id, props.shouldEval);
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
	let childParameterIds: string[] = [];

	if (props.nodeType === NodeType.State) {
		matrixData = stateMatrixData.filter(({ base }) => base === props.id);
	} else {
		const paramsMap = getUnstratifiedParameters(amr);
		if (!paramsMap.has(props.id)) return [];

		// IDs to find within the rates
		childParameterIds = paramsMap.get(props.id) as string[];
		// Holds all points that have the parameter
		matrixData = filterParameterLocations(amr, transitionMatrixData, [
			...childParameterIds,
			props.id
		]);
	}

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

	const matrixAttributes: any =
		props.nodeType === NodeType.State
			? createMatrix1D(matrixData)
			: createParameterMatrix(
					amr,
					cloneDeep(
						matrixData.map((t) => amr.model.transitions.filter(({ id }) => t.id === id)).flat()
					),
					childParameterIds
			  );

	matrix.value = matrixAttributes.matrix;
	controllers.value = matrixAttributes.controllers ? matrixAttributes.controllers : [''];

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
