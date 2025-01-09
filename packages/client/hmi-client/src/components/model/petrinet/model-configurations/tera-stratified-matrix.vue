<template>
	<div class="matrix-toolbar">
		<Dropdown
			v-if="matrixMap && Object.keys(matrixMap).length > 0"
			:model-value="matrixType"
			:options="matrixTypes"
			placeholder="Select matrix type"
			@update:model-value="(v) => changeMatrix(v)"
		/>

		<Button
			@click="clipboardBuffer(clipboardText)"
			label="Paste"
			severity="secondary"
			size="small"
			:disabled="clipboardText === ''"
		/>
	</div>
	<div class="p-datatable-wrapper stratified-matrix">
		<div
			v-if="!isEmpty(matrix)"
			class="p-datatable p-component p-datatable-scrollable p-datatable-responsive-scroll p-datatable-gridlines p-datatable-grouped-header stratified-value-matrix"
		>
			<table class="p-datatable-table p-datatable-scrollable-table editable-cells-table">
				<thead v-if="matrix[0].length > 0" class="p-datatable-thead sticky-table-header">
					<tr>
						<th v-if="matrix.length > 0" class="choose-criteria">&nbsp;</th>
						<th v-for="(row, rowIdx) in matrix[0]" :key="rowIdx">{{ row.colCriteria }}</th>
					</tr>
				</thead>
				<tbody class="p-datatable-tbody">
					<tr v-for="(row, rowIdx) in matrix" :key="rowIdx">
						<!-- Row label -->
						<td v-if="matrix.length > 0" class="p-frozen-column" style="position: sticky; left: 0; background: white">
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
							<section v-if="cell.content.id" class="flex flex-column">
								<InputText
									v-if="editableCellStates[rowIdx][colIdx]"
									class="cell-input"
									:class="stratifiedMatrixType !== StratifiedMatrix.Initials && 'big-cell-input'"
									v-model.lazy="valueToEdit"
									v-focus
									@focusout="updateCellValue(cell.content.id, rowIdx, colIdx)"
									@keyup.stop.enter="updateCellValue(cell.content.id, rowIdx, colIdx)"
								/>
								<div class="w-full" :class="{ 'hide-content': editableCellStates[rowIdx][colIdx] }">
									<div
										class="subdue mb-1 flex align-items-center gap-1 w-full justify-content-between"
										v-if="stratifiedMatrixType !== StratifiedMatrix.Initials"
									>
										{{ cell?.content.id }}
										<!--
											<span
												v-if="cell?.content?.controllers"
												class="pi pi-info-circle"
												v-tooltip.top="{
													value: `Controllers:\n ${cell?.content?.controllers}`,
													pt: 'grid col'
												}"
											/>
											-->
									</div>
									<div
										v-if="!isReadOnly"
										class="mathml-container"
										v-html="expressionMap[rowIdx + ':' + colIdx] ?? '...'"
									/>
								</div>
							</section>
							<span v-else class="subdue">n/a</span>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
	<!-- these patches mask the parts of the datatable we want to hide while scrolling -->
	<div class="patch-top"></div>
	<div class="patch-top-right"></div>
	<div class="patch-left-side"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { isEmpty, isNumber } from 'lodash';
import { pythonInstance } from '@/python/PyodideController';
import InputText from 'primevue/inputtext';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import { StratifiedMatrix } from '@/types/Model';
import type { MiraMatrix, MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { createParameterMatrix, createInitialMatrix, collapseTemplates } from '@/model-representation/mira/mira';
import { getVariable } from '@/model-representation/service';
import { extractTemplateMatrix } from '@/model-representation/mira/mira-util';
import { logger } from '@/utils/logger';
import { getClipboardText, pasteEventGenerator } from '@/utils/clipboard';
import { dsvParse } from '@/utils/dsv';

const props = defineProps<{
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
	id: string;
	stratifiedMatrixType: StratifiedMatrix;
	isReadOnly?: boolean;
	shouldEval: boolean;
	matrixType?: string;
}>();

const emit = defineEmits(['update-cell-values']);

const matrixTypes = ['subjectOutcome', 'subjectControllers', 'outcomeControllers', 'other'];
const matrixType = ref(props.matrixType || 'subjectOutcome');
const matrixMap = ref<{ [key: string]: MiraMatrix }>({});
const matrix = ref<MiraMatrix>([]);

const currentMatrixtype = ref(props.matrixType || '');

const valueToEdit = ref('');
const editableCellStates = ref<boolean[][]>([]);
const expressionMap = ref<{ [key: string]: string }>({});

const parametersValueMap = computed(() => {
	const keys = Object.keys(props.mmt.parameters);
	const result: any = {};
	keys.forEach((key) => {
		result[key] = props.mmt.parameters[key].value;
	});
	return result;
});

// Copy and paste utilities
let timerId = -1;
const clipboardText = ref('');
const updateByMatrixBulk = (matrixToUpdate: MiraMatrix, text: string) => {
	const parseResult = dsvParse(text);
	const updateList: { id: string; value: number }[] = [];

	matrixToUpdate.forEach((row) => {
		row.forEach(async (matrixEntry) => {
			// If we have label information, use them as they may be more accurate, otherwise use indices
			if (parseResult.hasColLabels && parseResult.hasRowLabels) {
				const match = parseResult.entries.find(
					(entry) => entry.rowLabel === matrixEntry.rowCriteria && entry.colLabel === matrixEntry.colCriteria
				);
				if (match) {
					updateList.push({
						id: matrixEntry.content.id,
						value: match.value
					});
				}
			} else {
				const match = parseResult.entries.find(
					(entry) => entry.rowIdx === matrixEntry.row && entry.colIdx === matrixEntry.col
				);
				if (match) {
					updateList.push({
						id: matrixEntry.content.id,
						value: match.value
					});
				}
			}
		});
	});

	emit('update-cell-values', updateList);
};

const pasteItemProcessor = async (item: DataTransferItem) => {
	if (item.kind !== 'string') return;
	if (item.type !== 'text/plain') return;

	// Presume this is a full matrix, with row/column labels
	item.getAsString(async (text) => {
		updateByMatrixBulk(matrix.value, text);
	});
};
const processPasteEvent = pasteEventGenerator(pasteItemProcessor);
const clipboardBuffer = (text: string) => {
	updateByMatrixBulk(matrix.value, text);
};

const changeMatrix = (v: string) => {
	matrixType.value = v;
	matrix.value = matrixMap.value[v];
};

// Makes cell inputs focus once they appear
const vFocus = {
	mounted: (el) => el.focus()
};

function onEnterValueCell(variableName: string, rowIdx: number, colIdx: number) {
	if (!variableName || props.isReadOnly) return;
	valueToEdit.value = getVariable(props.mmt, variableName).value;
	editableCellStates.value[rowIdx][colIdx] = true;
}

// See ES2_2a_start in "Eval do not touch"
// Returns the presentation mathml
async function getMatrixValue(variableName: string) {
	let expressionBase = getVariable(props.mmt, variableName).value;

	if (isNumber(expressionBase) && +expressionBase === 0) {
		expressionBase = '0.0'; // just to ensure we don't trigger falsy checks
	}

	if (props.shouldEval) {
		const expressionEval = await pythonInstance.evaluateExpression(expressionBase, parametersValueMap.value);
		return (await pythonInstance.parseExpression(expressionEval)).pmathml;
	}
	return (await pythonInstance.parseExpression(expressionBase)).pmathml;
}

function generateMatrix() {
	const stratifiedType = props.stratifiedMatrixType;

	if (stratifiedType === StratifiedMatrix.Initials) {
		matrix.value = createInitialMatrix(props.mmt, props.id);
	} else if (stratifiedType === StratifiedMatrix.Parameters) {
		const matrices = createParameterMatrix(props.mmt, props.mmtParams, props.id);

		matrixMap.value = {
			subjectOutcome: matrices.subjectOutcome.matrix,
			subjectControllers: matrices.subjectControllers.matrix,
			outcomeControllers: matrices.outcomeControllers.matrix,
			other: matrices.other
		};

		// Find a default
		if (currentMatrixtype.value) {
			matrix.value = matrixMap.value[currentMatrixtype.value];
			matrixType.value = currentMatrixtype.value;
			return;
		}

		for (let i = 0; i < matrixTypes.length; i++) {
			const typeStr = matrixTypes[i];

			if (matrixMap.value[typeStr] && matrixMap.value[typeStr].length > 0) {
				matrix.value = matrixMap.value[typeStr];
				matrixType.value = typeStr;
				break;
			}
		}
	} else if (stratifiedType === StratifiedMatrix.Rates) {
		const templatesMap = collapseTemplates(props.mmt).matrixMap;

		const transitionMatrix = templatesMap.get(props.id);
		if (!transitionMatrix) {
			logger.error('Failed to generate transition matrix');
			return;
		}
		matrix.value = extractTemplateMatrix(transitionMatrix).matrix;
	}
}

async function updateCellValue(variableName: string, rowIdx: number, colIdx: number) {
	editableCellStates.value[rowIdx][colIdx] = false;
	const newValue = valueToEdit.value;
	const mathml = (await pythonInstance.parseExpression(newValue)).mathml;

	currentMatrixtype.value = matrixType.value;
	emit('update-cell-values', [{ id: variableName, value: newValue, mathml }]);
}

function resetEditState() {
	editableCellStates.value = [];

	if (!isEmpty(matrix.value)) {
		for (let i = 0; i < matrix.value.length; i++) {
			editableCellStates.value.push(Array(matrix.value[0].length).fill(false));
		}
	}
}

onMounted(() => {
	document.addEventListener('paste', processPasteEvent);
	timerId = window.setInterval(async () => {
		const x = await getClipboardText();
		if (x !== clipboardText.value) {
			clipboardText.value = x;
		}
	}, 1000);
});

onUnmounted(() => {
	document.removeEventListener('paste', processPasteEvent);
	window.clearInterval(timerId);
});

watch(
	() => [matrix.value, props.shouldEval],
	async () => {
		if (!matrix.value) return;
		resetEditState();
		expressionMap.value = {};

		const evalList: Promise<{ row: number; col: number; val: string } | undefined>[] = [];

		const assignExpression = async (id: string, row: number, col: number) => {
			if (!id) return { row: -1, col: -1, val: '' };
			const val = await getMatrixValue(id);
			return {
				row,
				col,
				val
			};
		};

		matrix.value.forEach((matrixRow: any) => {
			matrixRow.forEach((cell: any) => {
				evalList.push(assignExpression(cell.content.id, cell.row, cell.col));
			});
		});

		const values = await Promise.all(evalList);
		values.forEach((v) => {
			if (v) {
				expressionMap.value[`${v.row}:${v.col}`] = v.val;
			}
		});
	}
);

watch(
	() => [props.id, props.mmt],
	() => {
		generateMatrix();
		resetEditState();
	},
	{ immediate: true }
);
</script>

<style scoped>
.stratified-matrix {
	position: relative;
}

.p-datatable {
	max-width: 80vw;
}

.p-datatable .p-datatable-thead > tr > th.choose-criteria {
	padding: 0;
	background: var(--surface-0);
	border-top: none;
	border-left: none;
	position: sticky;
	left: 0;
	z-index: 2;
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
	padding-left: var(--gap-2);
	padding-right: var(--gap-4);
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
	margin-bottom: var(--gap-4);
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

.matrix-toolbar {
	display: flex;
	justify-content: space-between;
	position: sticky;
	z-index: 1;
	background: var(--surface-0);
	padding-bottom: var(--gap-1);
	top: 0;
	left: 0;
}
.matrix-toolbar Button {
	margin-bottom: var(--gap-4);
}
.sticky-table-header {
	position: sticky;
	top: 3rem;
	background: white;
}
/* Patches to mask the parts of the datatable we want to hide while scrolling */
.patch-top {
	position: absolute;
	top: 62px;
	left: 0;
	width: 100%;
	height: 2px;
	background: var(--surface-0);
	z-index: 1;
}
.patch-top-right {
	position: absolute;
	top: 4rem;
	right: 0;
	width: 100px;
	height: 50px;
	background: var(--surface-0);
	z-index: 1;
}
.patch-left-side {
	position: absolute;
	top: 4rem;
	left: 0;
	width: 2rem;
	height: calc(100% - 4rem);
	background: var(--surface-0);
	z-index: 1;
}
</style>
