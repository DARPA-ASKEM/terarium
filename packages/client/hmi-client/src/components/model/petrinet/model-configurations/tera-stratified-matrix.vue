<template>
	<div
		v-if="!isEmpty(matrix)"
		class="p-datatable p-component p-datatable-scrollable p-datatable-responsive-scroll p-datatable-gridlines p-datatable-grouped-header stratified-value-matrix"
	>
		<div class="p-datatable-wrapper">
			<table class="p-datatable-table p-datatable-scrollable-table editable-cells-table">
				<thead v-if="matrix[0].length > 1" class="p-datatable-thead">
					<tr>
						<th v-if="matrix.length > 1" class="choose-criteria">&nbsp;</th>
						<th v-for="(row, rowIdx) in matrix[0]" :key="rowIdx">{{ row.colCriteria }}</th>
					</tr>
				</thead>
				<tbody class="p-datatable-tbody">
					<tr v-for="(row, rowIdx) in matrix" :key="rowIdx">
						<td v-if="matrix.length > 1" class="p-frozen-column">
							<template v-if="stratifiedMatrixType === StratifiedMatrix.Initials">
								{{ Object.values(row[0].rowCriteria).join(' / ') }}
							</template>
							<template v-else>
								{{ row[0].rowCriteria }}
							</template>
						</td>
						<td
							v-for="(cell, colIdx) in row"
							:key="colIdx"
							tabindex="0"
							:class="editableCellStates[rowIdx][colIdx] && 'is-editing'"
							@keyup.enter="onEnterValueCell(cell.content.id, rowIdx, colIdx)"
							@click="onEnterValueCell(cell.content.id, rowIdx, colIdx)"
						>
							<template v-if="cell.content.id">
								<InputText
									v-if="editableCellStates[rowIdx][colIdx]"
									class="cell-input"
									v-model.lazy="valueToEdit"
									v-focus
									@focusout="updateModelConfigValue(cell.content.id, rowIdx, colIdx)"
									@keyup.stop.enter="updateModelConfigValue(cell.content.id, rowIdx, colIdx)"
								/>
								<section v-else>
									<div>
										<div
											class="mathml-container"
											v-html="matrixExpressionsList?.[rowIdx]?.[colIdx] ?? '...'"
										/>
										<template v-if="cell?.content?.controllers">
											controllers: {{ cell?.content?.controllers }}
										</template>
									</div>
									<div class="subdue">
										{{ cell?.content.id }}
									</div>
								</section>
							</template>
							<span v-else class="subdue">N/A</span>
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
import { StratifiedModel } from '@/model-representation/petrinet/petrinet-service';
import { generateMatrix } from '@/model-representation/petrinet/mira-petri';
import { Initial, ModelConfiguration, ModelParameter, Rate } from '@/types/Types';
import InputText from 'primevue/inputtext';
import { pythonInstance } from '@/python/PyodideController';
import { StratifiedMatrix } from '@/types/Model';

const props = defineProps<{
	modelConfiguration: ModelConfiguration;
	id: string;
	stratifiedModelType: StratifiedModel;
	stratifiedMatrixType: StratifiedMatrix;
	shouldEval: boolean;
}>();

const emit = defineEmits(['update-configuration']);

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

// Makes cell inputs focus once they appear
const vFocus = {
	mounted: (el) => el.focus()
};

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

function renderMatrix() {
	matrix.value = generateMatrix(
		props.modelConfiguration.configuration,
		props.id,
		props.stratifiedMatrixType
	);
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

.subdue {
	color: var(--text-color-subdued);
}

section {
	display: flex;
	justify-content: space-between;
}
</style>
