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
						<!-- Row label -->
						<td v-if="matrix.length > 0" class="p-frozen-column">
							{{ row[0].rowCriteria }}
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
import { pythonInstance } from '@/python/PyodideController';
import type { ModelConfiguration } from '@/types/Types';
import InputText from 'primevue/inputtext';
import { StratifiedMatrix } from '@/types/Model';
import type { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import {
	collapseParameters,
	createParameterMatrix,
	collapseInitials
} from '@/model-representation/mira/mira';
import { getVariable, updateVariable } from '@/model-representation/service';

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

const parametersValueMap = computed(() => {
	const keys = Object.keys(props.mmt.parameters);
	const result: any = {};
	keys.forEach((key) => {
		result[key] = props.mmt.parameters[key].value;
	});
	return result;
});

// Makes cell inputs focus once they appear
const vFocus = {
	mounted: (el) => el.focus()
};

function onEnterValueCell(variableName: string, rowIdx: number, colIdx: number) {
	if (!variableName) return;
	valueToEdit.value = getVariable(props.mmt, variableName).value;
	editableCellStates.value[rowIdx][colIdx] = true;
}

// See ES2_2a_start in "Eval do not touch"
// Returns the presentation mathml
async function getMatrixValue(variableName: string) {
	const expressionBase = getVariable(props.mmt, variableName).value;

	console.log('debugging', variableName, expressionBase);

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
	const matrixType = props.stratifiedMatrixType;

	if (matrixType === StratifiedMatrix.Initials) {
		const initialsMap = collapseInitials(props.mmt);
		const childrenInitials = initialsMap.get(props.id);

		const m2: any[] = [];
		childrenInitials?.forEach((name, idx) => {
			const row: any[] = [];
			row.push({
				row: idx,
				col: 0,
				rowCriteria: name,
				colCriteria: '',
				content: {
					id: name,
					value: props.mmt.initials[name].expression
				}
			});
			m2.push(row);
		});

		console.group('initials matrix gen');
		console.log('props.id', props.id);
		console.log('children', childrenInitials);
		console.log('matrix', m2);
		matrix.value = m2;
	} else if (matrixType === StratifiedMatrix.Parameters) {
		const paramsMap = collapseParameters(props.mmt, props.mmtParams);
		const childrenParams = paramsMap.get(props.id);
		const matrices = createParameterMatrix(props.mmt, props.mmtParams, props.id);

		console.group('param matrix gen');
		console.log('props.id', props.id);
		console.log('children', childrenParams);
		console.log('matrix data', matrices);
		console.groupEnd();
		// FIXME: should be matrices
		matrix.value = matrices.outcomeControllers.matrix;
	} else {
		console.log('TODO template!!!');
	}
}

async function updateModelConfigValue(variableName: string, rowIdx: number, colIdx: number) {
	editableCellStates.value[rowIdx][colIdx] = false;
	const newValue = valueToEdit.value;
	const mathml = (await pythonInstance.parseExpression(newValue)).mathml;

	const clone = cloneDeep(props.modelConfiguration);
	updateVariable(
		clone.configuration,
		props.stratifiedMatrixType.toString(),
		variableName,
		newValue,
		mathml
	);

	emit('update-configuration', clone);
	renderMatrix();
}

function configureMatrix() {
	renderMatrix();

	if (!isEmpty(matrix.value)) {
		for (let i = 0; i < matrix.value.length; i++) {
			editableCellStates.value.push(Array(matrix.value[0].length).fill(false));
		}
	}
}

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
