<template>
	<main>
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
										val: getMatrixValue(cell?.value?.id),
										rowIdx: i,
										colIdx: j
									}
								"
							>
								<template v-if="cell?.value?.id">
									<InputText
										v-if="valueToEdit.rowIdx === i && valueToEdit.colIdx === j"
										class="cell-input"
										v-model.lazy="valueToEdit.val"
										v-focus
										@focusout="valueToEdit = { val: '', rowIdx: -1, colIdx: -1 }"
										@keyup.stop.enter="updateModelConfigValue(cell?.value?.id)"
									/>
									<span v-else class="editable-cell">
										{{ getMatrixValue(cell?.value?.id) }}
									</span>
								</template>
								<span v-else class="not-allowed">N/A</span>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
		<!--
			Temporary - just showing the mira dimensions and terms that are generated. 
			Will put these with matrix once the matrix is generated (don't want to modify table logic above)
		-->
		<div v-if="stratifiedModelType === StratifiedModelType.Mira">
			Column dimensions
			<Dropdown
				class="w-full"
				placeholder="Select a variable"
				v-model="chosenCol"
				:options="colDimensions"
			/>
			Row dimensions
			<Dropdown
				class="w-full"
				placeholder="Select a variable"
				v-model="chosenRow"
				:options="rowDimensions"
			/>
			Terms / Headers
			<br />
			{{ miraHeaders }}
		</div>
	</main>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { cloneDeep, isEmpty, isEqual, union } from 'lodash';
import {
	StratifiedModelType,
	getCatlabStatesMatrixData,
	getCatlabTransitionsMatrixData
} from '@/model-representation/petrinet/petrinet-service';
import { createMatrix1D, createMatrix2D } from '@/utils/pivot';
import Dropdown from 'primevue/dropdown';
import { Initial, ModelConfiguration, ModelParameter, Rate } from '@/types/Types';
import { NodeType } from '@/model-representation/petrinet/petrinet-renderer';
import InputText from 'primevue/inputtext';
import { updateModelConfiguration } from '@/services/model-configurations';

const props = defineProps<{
	modelConfiguration: ModelConfiguration;
	id: string;
	stratifiedModelType: StratifiedModelType;
	nodeType: NodeType;
}>();

const colDimensions: string[] = [];
const rowDimensions: string[] = [];

const matrix = ref();
const chosenCol = ref('');
const chosenRow = ref('');
const valueToEdit = ref({ val: '', rowIdx: -1, colIdx: -1 });

const miraHeaders = ref(); // temp

// Makes cell inputs focus once they appear
const vFocus = {
	mounted: (el) => el.focus()
};

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

function getMatrixValue(variableName: string) {
	const odeObjectLocation = findOdeObjectLocation(variableName);
	if (odeObjectLocation) {
		const { odeFieldObject } = odeObjectLocation;
		return odeFieldObject?.expression ?? odeFieldObject?.value;
	}
	return variableName;
}

function updateModelConfigValue(variableName: string) {
	const odeObjectLocation = findOdeObjectLocation(variableName);
	if (odeObjectLocation) {
		const { odeFieldObject, fieldName, fieldIndex } = odeObjectLocation;

		if (odeFieldObject.expression) odeFieldObject.expression = valueToEdit.value.val;
		else if (odeFieldObject.value) odeFieldObject.value = Number(valueToEdit.value.val);

		const modelConfigurationClone = cloneDeep(props.modelConfiguration);
		modelConfigurationClone.configuration.semantics.ode[fieldName][fieldIndex] = odeFieldObject;
		updateModelConfiguration(modelConfigurationClone);

		valueToEdit.value = { val: '', rowIdx: -1, colIdx: -1 };
	}
}

function configureMatrix() {
	if (props.stratifiedModelType === StratifiedModelType.Catlab) {
		// Get only the states/transitions that are mapped to the base model
		const matrixData =
			props.nodeType === NodeType.State
				? getCatlabStatesMatrixData(props.modelConfiguration.configuration).filter(
						(d) => d['@base'] === props.id
				  )
				: getCatlabTransitionsMatrixData(props.modelConfiguration.configuration).filter(
						(d) => d['@base'] === props.id
				  );

		if (isEmpty(matrixData)) return;

		// Grab dimension names from the first matrix row
		const dimensions = [cloneDeep(matrixData)[0]].map((d) => {
			delete d.id;
			delete d['@base'];
			return Object.keys(d);
		})[0];
		rowDimensions.push(...dimensions);
		colDimensions.push(...dimensions);

		const matrixAttributes =
			props.nodeType === NodeType.State
				? createMatrix1D(matrixData)
				: createMatrix2D(matrixData, colDimensions, rowDimensions);

		matrix.value = matrixAttributes.matrix;
	} else if (props.stratifiedModelType === StratifiedModelType.Mira) {
		const modifiers = props.modelConfiguration.configuration.model.states.map(
			({ grounding }) => grounding.modifiers
		);

		const dimensionsAndTerms = {};
		for (let i = 0; i < modifiers.length; i++) {
			const dimensions = Object.keys(dimensionsAndTerms);
			const modifierDims = Object.keys(modifiers[i]);

			// Update dimensions
			const newDimensions = union(dimensions, modifierDims);
			if (!isEqual(dimensions, newDimensions)) {
				for (let j = 0; j < modifierDims.length; j++) dimensionsAndTerms[newDimensions[j]] = [];
			}

			// Append terms
			for (let j = 0; j < modifierDims.length; j++) {
				const dimension = modifierDims[j];
				const term = modifiers[i][dimension];

				if (!dimensionsAndTerms[dimension].includes(term)) {
					dimensionsAndTerms[dimension].push(term);
				}
			}
		}

		miraHeaders.value = dimensionsAndTerms;
		const dimensions = Object.keys(dimensionsAndTerms);
		rowDimensions.push(...dimensions);
		colDimensions.push(...dimensions);
	}
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
