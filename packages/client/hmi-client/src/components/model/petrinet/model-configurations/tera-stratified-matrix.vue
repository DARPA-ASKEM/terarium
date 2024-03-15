<template>
	<div
		v-if="!isEmpty(matrix)"
		class="p-datatable p-component p-datatable-scrollable p-datatable-responsive-scroll p-datatable-gridlines p-datatable-grouped-header stratified-value-matrix"
	>
		<div class="p-datatable-wrapper">
			<table class="p-datatable-table p-datatable-scrollable-table editable-cells-table">
				<thead v-if="matrix[0].length > 0" class="p-datatable-thead">
					<tr>
						<th v-if="matrix.length > 0" class="choose-criteria">&nbsp;</th>
						<th v-for="(row, rowIdx) in matrix[0]" :key="rowIdx">{{ row.colCriteria }}</th>
					</tr>
				</thead>
				<tbody class="p-datatable-tbody">
					<tr v-for="(row, rowIdx) in matrix" :key="rowIdx">
						<td v-if="matrix.length > 0" class="p-frozen-column">
							<template v-if="stratifiedMatrixType === StratifiedMatrix.Initials">
								{{ Object.values(row[0].rowCriteria)[0] }}
							</template>
							<template v-else>
								{{ row[0].rowCriteria }}
							</template>
						</td>
						<td
							v-for="(cell, colIdx) in row"
							:key="colIdx"
							tabindex="0"
							:class="{
								'is-editing': editableCellStates[rowIdx][colIdx],
								'n-a-cell': !cell.content.id
							}"
							@keyup.enter="onEnterValueCell(cell.content.id, rowIdx, colIdx)"
							@click="onEnterValueCell(cell.content.id, rowIdx, colIdx)"
						>
							<template v-if="cell.content.id">
								<section class="flex flex-column">
									<InputText
										v-if="editableCellStates[rowIdx][colIdx]"
										class="cell-input"
										:class="stratifiedMatrixType !== StratifiedMatrix.Initials && 'big-cell-input'"
										v-model.lazy="valueToEdit"
										v-focus
										@focusout="updateModelConfigValue(cell.content.id, rowIdx, colIdx)"
										@keyup.stop.enter="updateModelConfigValue(cell.content.id, rowIdx, colIdx)"
									/>
									<div
										class="w-full"
										:class="{ 'hide-content': editableCellStates[rowIdx][colIdx] }"
									>
										<div
											class="subdue mb-1 flex align-items-center gap-1 w-full justify-content-between"
											v-if="stratifiedMatrixType !== StratifiedMatrix.Initials"
										>
											{{ cell?.content.id }}
											<span
												v-if="cell?.content?.controllers"
												class="pi pi-info-circle"
												v-tooltip.top="{
													value: `Controllers:\n ${cell?.content?.controllers}`,
													pt: 'grid col'
												}"
											/>
										</div>
										<div>
											<div
												class="mathml-container"
												v-html="matrixExpressionsList?.[rowIdx]?.[colIdx] ?? '...'"
											/>
										</div>
									</div>
								</section>
							</template>
							<span v-else class="subdue">n/a</span>
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
import { generateMatrix } from '@/model-representation/petrinet/mira-petri';
import type { Initial, ModelConfiguration, ModelParameter, Rate } from '@/types/Types';
import InputText from 'primevue/inputtext';
import { pythonInstance } from '@/python/PyodideController';
import { StratifiedMatrix } from '@/types/Model';
import type { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import {} from '@/model-representation/mira/mira-util';
import { collapseParameters, createParameterMatrix } from '@/model-representation/mira/mira';

const props = defineProps<{
	modelConfiguration: ModelConfiguration;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
	id: string;
	stratifiedMatrixType: StratifiedMatrix;
	shouldEval: boolean;
}>();

const emit = defineEmits(['update-configuration']);

const matrix = ref<any>([]);
const valueToEdit = ref('');
const editableCellStates = ref<boolean[][]>([]);

const matrixExpressionsList = ref<string[][]>([]);

const parametersValueMap = computed(
	() =>
		props.modelConfiguration.configuration?.semantics.ode.parameters.reduce((acc, val) => {
			acc[val.id] = val.value;
			return acc;
		}, {})
);

// Makes cell inputs focus once they appear
const vFocus = {
	mounted: (el) => el.focus()
};

watch(
	() => [matrix.value, props.shouldEval],
	async () => {
		if (!matrix.value) return;
		const output: string[][] = [];
		await Promise.all(
			matrix.value
				.map((row) =>
					row.map(async (cell) => {
						if (cell.content?.id) {
							if (!output[cell.row]) {
								output[cell.row] = [];
							}
							output[cell.row][cell.col] = await getMatrixValue(cell.content.id);
						}
					})
				)
				.flat()
		);
		matrixExpressionsList.value = output;
	}
);

// Finds where to get the value within the AMR based on the variable name
function findOdeObjectLocation(variableName: string): {
	odeFieldObject: Rate & Initial & ModelParameter;
	fieldName: string;
	fieldIndex: number;
} | null {
	const ode = props.modelConfiguration.configuration?.semantics?.ode;
	if (!ode) return null;

	const fieldNames = [
		StratifiedMatrix.Rates,
		StratifiedMatrix.Initials,
		StratifiedMatrix.Parameters
	];

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

function onEnterValueCell(variableName: string, rowIdx: number, colIdx: number) {
	if (!variableName) return;
	valueToEdit.value = getMatrixExpression(variableName);
	editableCellStates.value[rowIdx][colIdx] = true;
}

// See ES2_2a_start in "Eval do not touch"
// Returns the presentation mathml
async function getMatrixValue(variableName: string) {
	const expressionBase = getMatrixExpression(variableName);

	if (props.shouldEval) {
		const expressionEval = await pythonInstance.evaluateExpression(
			expressionBase,
			parametersValueMap.value
		);
		return (await pythonInstance.parseExpression(expressionEval)).pmathml;
	}

	return (await pythonInstance.parseExpression(expressionBase)).pmathml;
}

function renderMatrix() {
	matrix.value = generateMatrix(
		props.modelConfiguration.configuration,
		props.id,
		props.stratifiedMatrixType
	);

	const paramsMap = collapseParameters(props.mmt, props.mmtParams);
	const childrenParams = paramsMap.get(props.id);
	const matrices = createParameterMatrix(props.mmt, props.mmtParams, props.id);

	console.group('matrix gen');
	console.log('props.id', props.id);
	console.log('children', childrenParams);
	console.log('matrix data', matrices);
	console.groupEnd();

	matrix.value = matrices.outcomeControllers.matrix;
	// matrix.value = matrices.subjectControllers.matrix;
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
		renderMatrix();
	}
}

function configureMatrix() {
	renderMatrix();

	if (!isEmpty(matrix.value)) {
		for (let i = 0; i < matrix.value.length; i++) {
			editableCellStates.value.push(Array(matrix.value[0].length).fill(false));
		}
	}
}

watch([() => props.id, () => props.modelConfiguration.configuration], () => {
	configureMatrix();
});

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

.p-datatable .p-datatable-tbody > tr > td.is-editing {
	padding: 0;
	padding-bottom: 0;
}

.editable-cell {
	display: flex;
	justify-content: space-between;
	align-items: center;
}
.n-a-cell {
	background-color: var(--surface-b);
}
.hide-content {
	visibility: hidden;
	height: 0px;
	padding-left: 2rem;
}
.cell-input {
	padding-left: var(--gap-small);
	padding-right: var(--gap);
	margin-bottom: 0 !important;
	font-feature-settings: 'tnum';
	text-align: right;
	height: 44px;
}

.big-cell-input {
	height: 66px;
}

.mathml-container {
	width: 100%;
	text-align: right;
}
.mathml-container:deep(mn) {
	font-family: var(--font-family);
	font-feature-settings: 'tnum';
	text-align: right !important;
}
.p-datatable-scrollable .p-frozen-column {
	padding-right: 1rem;
}

.p-dropdown {
	min-width: 11rem;
}

.subdue {
	color: var(--text-color-subdued);
}

.controllers {
	font-size: var(--font-caption);
}
section {
	display: flex;
	justify-content: space-between;
}
</style>
