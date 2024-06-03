<template>
	<Datatable
		:value="data ?? tableFormattedParams"
		v-model:expanded-rows="expandedRows"
		dataKey="id"
		:row-class="rowClass"
		size="small"
		:class="{ 'hide-header': hideHeader }"
		edit-mode="cell"
		@cell-edit-complete="conceptSearchTerm = { curie: '', name: '' }"
	>
		<!-- Row expander, ID and Name columns -->
		<Column expander class="w-3rem" />

		<!-- Symbol -->
		<Column header="Symbol" class="w-2">
			<template #body="slotProps">
				<span class="truncate-text latex-font">
					{{ slotProps.data.id }}
				</span>
			</template>
		</Column>

		<!-- Name -->
		<Column header="Name">
			<template #body="{ data }">
				<InputText
					v-model.lazy="data.name"
					:disabled="configView || readonly || data.type === ParamType.MATRIX"
					@update:model-value="updateParamValue(data.value, 'name', $event)"
				/>
			</template>
		</Column>

		<Column header="Description" class="w-2">
			<template #body="{ data }">
				<InputText
					v-model.lazy="data.description"
					:disabled="configView || readonly || data.type === ParamType.MATRIX"
					@update:model-value="updateParamValue(data.value, 'description', $event)"
				/>
			</template>
		</Column>

		<Column header="Unit" class="w-1">
			<template #body="slotProps">
				<InputText
					v-if="slotProps.data.type !== ParamType.MATRIX"
					class="w-full"
					v-model.lazy="slotProps.data.unit"
					:disabled="readonly"
					@update:model-value="
						updateParamValue(slotProps.data.value, 'unit', {
							...slotProps.data.value.unit,
							expression: $event
						})
					"
				/>
				<template v-else>--</template>
			</template>
		</Column>

		<!-- Value type: Matrix or a Contant -->
		<Column field="type" header="Constant" class="pl-2 pr-6 w-2">
			<template #body="slotProps">
				<Button
					text
					v-if="slotProps.data.type === ParamType.MATRIX"
					icon="pi pi-table"
					label="Matrix"
					@click="openMatrixModal(slotProps.data)"
					class="p-0"
				/>
				<tera-input-number
					v-else
					class="constant-number"
					v-model.lazy="slotProps.data.value.value"
					:min-fraction-digits="1"
					:max-fraction-digits="10"
					:disabled="readonly"
					@update:model-value="emit('update-value', [slotProps.data.value])"
				/>
			</template>
		</Column>

		<!-- Value: the thing we show depends on the type of number -->
		<Column field="value" header="Distribution" class="w-4 pl-2 pr-2">
			<template #body="slotProps">
				<!-- Matrix -->
				<span
					v-if="slotProps.data.type === ParamType.MATRIX"
					@click="openMatrixModal(slotProps.data)"
					class="cursor-pointer secondary-text text-sm"
					>Open matrix</span
				>
				<!-- Distribution -->
				<div v-else class="distribution-container">
					<label>Min</label>
					<tera-input-number
						class="distribution-item min-value"
						v-model.lazy="slotProps.data.value.distribution.parameters.minimum"
						:min-fraction-digits="1"
						:max-fraction-digits="10"
						:disabled="readonly"
						@update:model-value="emit('update-value', [slotProps.data.value])"
					/>
					<label>Max</label>
					<tera-input-number
						class="distribution-item max-value"
						v-model.lazy="slotProps.data.value.distribution.parameters.maximum"
						:min-fraction-digits="1"
						:max-fraction-digits="10"
						:disabled="readonly"
						@update:model-value="emit('update-value', [slotProps.data.value])"
					/>
				</div>
			</template>
		</Column>

		<!-- Suggested Configurations Button -->
		<Column v-if="configView && !readonly">
			<template #body="{ data }">
				<Button
					v-if="data.type !== ParamType.MATRIX"
					text
					:label="`Suggested Configurations (${countSuggestions(data.id)})`"
					@click="openSuggestedValuesModal(data.id)"
				/>
			</template>
		</Column>

		<!-- Hiding for now until functionality is available
		<Column field="visibility" header="Visibility" style="width: 10%">
			<template #body="slotProps">
				<InputSwitch v-model="slotProps.data.visibility" @click.stop />
			</template>
		</Column> -->
		<template #expansion="slotProps">
			<tera-parameter-table
				hide-header
				v-if="slotProps.data.type === ParamType.MATRIX"
				:model="model"
				:model-configurations="modelConfigurations"
				:mmt="mmt"
				:mmt-params="mmtParams"
				:data="slotProps.data.tableFormattedMatrix"
				:readonly="readonly"
				:config-view="configView"
				@update-value="(val: ModelParameter) => emit('update-value', val)"
				@update-model="(model: Model) => emit('update-model', model)"
			/>
		</template>
	</Datatable>
	<Teleport to="body">
		<tera-stratified-matrix-modal
			v-if="matrixModalContext.isOpen && isStratified"
			:id="matrixModalContext.matrixId"
			:mmt="mmt"
			:mmt-params="mmtParams"
			:stratified-matrix-type="StratifiedMatrix.Parameters"
			@close-modal="matrixModalContext.isOpen = false"
			@update-cell-value="(configToUpdate: any) => updateCellValue(configToUpdate)"
		/>

		<tera-modal
			v-if="suggestedValuesModalContext.isOpen"
			@modal-mask-clicked="onCloseSuggestedValuesModal"
			:width="1000"
		>
			<template #header
				><h5>Suggested configurations for {{ suggestedValuesModalContext.id }}</h5></template
			>
			<Datatable
				:value="suggestedValues"
				dataKey="index"
				v-model:selection="selectedValue"
				tableStyle="min-width: 80rem"
			>
				<Column selectionMode="single" class="w-3rem"></Column>
				<Column header="Symbol">
					<template #body="{ data }">
						<span class="truncate-text">
							{{ data.parameter.id }}
						</span>
					</template>
				</Column>
				<Column header="Name">
					<template #body="{ data }">
						<span class="truncate-text">
							{{ data.parameter.name }}
						</span>
					</template>
				</Column>
				<Column header="Configuration">
					<template #body="{ data }">
						{{ data.configuration?.name }}
					</template>
				</Column>
				<Column header="Constant">
					<template #body="{ data }">
						{{ data.parameter.value }}
					</template>
				</Column>
				<Column header="Distribution">
					<template #body="{ data }">
						<div class="distribution-container">
							<span>Min: {{ data.parameter.distribution?.parameters.minimum }}</span>
							<span>Max: {{ data.parameter.distribution?.parameters.maximum }}</span>
						</div>
					</template>
				</Column>
				<Column header="Source">
					<template #body="{ data }">
						<span>{{
							data.configuration.configuration.metadata?.parameters?.[data.parameter.id]?.source ??
							data.configuration.configuration.metadata?.source?.join(', ')
						}}</span>
					</template>
				</Column>
			</Datatable>
			<template #footer>
				<Button
					label="Apply selected configuration"
					:disabled="isEmpty(selectedValue)"
					@click="applySelectedValue"
				/>
				<Button outlined label="Close" @click="onCloseSuggestedValuesModal" />
			</template>
		</tera-modal>
	</Teleport>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { cloneDeep, isEmpty } from 'lodash';
import Button from 'primevue/button';
import type { Model, ModelConfiguration, ModelParameter } from '@/types/Types';
import InputText from 'primevue/inputtext';
import Datatable from 'primevue/datatable';
import Column from 'primevue/column';
import { StratifiedMatrix } from '@/types/Model';
import TeraStratifiedMatrixModal from '@/components/model/petrinet/model-configurations/tera-stratified-matrix-modal.vue';
import { AMRSchemaNames, ModelConfigTableData, ParamType } from '@/types/common';

import { getModelType } from '@/services/model';
import { matrixEffect } from '@/utils/easter-eggs';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { isStratifiedModel, collapseParameters } from '@/model-representation/mira/mira';
import {
	updateVariable,
	getParameterMetadata,
	getParameters
} from '@/model-representation/service';
import TeraModal from '@/components/widgets/tera-modal.vue';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';

const props = defineProps<{
	model: Model;
	modelConfigurations?: ModelConfiguration[];
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
	data?: ModelConfigTableData[]; // we can use our own passed in data or the computed one.  this is for the embedded matrix table
	hideHeader?: boolean;
	readonly?: boolean;
	configView?: boolean; // if the table is in the model config view we have limited functionality
}>();

const emit = defineEmits(['update-value', 'update-model']);

const isStratified = computed(() => isStratifiedModel(props.mmt));

interface SuggestedValue {
	parameter: ModelParameter;
	configuration: ModelConfiguration;
	index: number;
}
const selectedValue = ref<SuggestedValue | null>(null);
const suggestedValues = computed(() => {
	const matchingParameters: SuggestedValue[] = [];
	props.modelConfigurations?.forEach((configuration, i) => {
		getParameters(configuration.configuration).forEach((parameter) => {
			if (parameter.id === suggestedValuesModalContext.value.id) {
				matchingParameters.push({
					parameter,
					configuration,
					index: i
				});
			}
		});
	});

	return matchingParameters;
});

const matrixModalContext = ref({
	isOpen: false,
	matrixId: ''
});

const suggestedValuesModalContext = ref({
	isOpen: false,
	id: ''
});
const parameters = computed<Map<string, string[]>>(() => {
	if (isStratified.value) {
		const collapsedParams = collapseParameters(props.mmt, props.mmtParams);
		return collapsedParams;
	}

	// Non-stratified logic
	const result = new Map<string, string[]>();
	Object.keys(props.mmt.parameters).forEach((key) => {
		const p = props.mmt.parameters[key];
		result.set(p.name, [p.name]);
	});
	return result;
});

const tableFormattedParams = ref<ModelConfigTableData[]>([]);

// FIXME: This method doee not really work in this context, the different types
// of CONSTANT/TIME_SERIES/DISTRIBUTION are not mutually exclusive, a parameter
// can have one or more types
const getParamType = (param: ModelParameter | undefined) => {
	let type = ParamType.CONSTANT;
	if (!param) return type;

	if (param?.distribution) {
		type = ParamType.DISTRIBUTION;
	}
	return type;
};

const buildParameterTable = () => {
	const model = props.model;
	const formattedParams: ModelConfigTableData[] = [];

	if (isStratified.value) {
		parameters.value.forEach((vals, init) => {
			const tableFormattedMatrix: ModelConfigTableData[] = vals.map((v) => {
				const param = getParameters(model).find((i) => i.id === v);
				const paramType = getParamType(param);
				const parametersMetadata = getParameterMetadata(props.model, param!.id);
				const sourceValue = parametersMetadata?.source;
				// If the parameter does not have a distribution, add a default distribution for editing purposes
				if (param && !param.distribution) {
					const lb = '';
					const ub = '';
					param.distribution = {
						type: 'StandardUniform1',
						parameters: {
							minimum: lb,
							maximum: ub
						}
					};
				}
				return {
					id: v,
					name: param?.name ?? '',
					type: paramType,
					description: param?.description ?? '',
					concept: param?.grounding ?? { identifiers: {} },
					units: param?.units?.expression ?? '',
					value: param,
					source: sourceValue,
					visibility: false
				};
			});
			formattedParams.push({
				id: init,
				name: init,
				description: '',
				concept: { identifiers: {} },
				type: ParamType.MATRIX,
				value: 'matrix',
				source: '',
				visibility: false,
				tableFormattedMatrix
			});
		});
	} else {
		parameters.value.forEach((vals, init) => {
			const param = getParameters(model).find((i) => i.id === vals[0]);
			if (!param) return;
			const paramType = getParamType(param);

			const parametersMetadata = getParameterMetadata(props.model, param.id);
			const sourceValue = parametersMetadata?.source;
			if (!param.distribution) {
				const lb = '';
				const ub = '';
				param.distribution = {
					type: 'StandardUniform1',
					parameters: {
						minimum: lb,
						maximum: ub
					}
				};
			}
			formattedParams.push({
				id: init,
				name: param.name ?? '',
				type: paramType,
				description: param.description ?? '',
				concept: param.grounding ?? { identifiers: {} },
				unit: param.units?.expression,
				value: param,
				source: sourceValue,
				visibility: false
			});
		});

		// Stockflow has auxiliaries
		const auxiliaries = model.model?.auxiliaries ?? [];
		auxiliaries.forEach((aux) => {
			const paramType = getParamType(aux);
			const parametersMetadata = model.metadata?.parameters?.[aux.id];
			const sourceValue = parametersMetadata?.source;
			formattedParams.push({
				id: aux.id,
				name: aux.name,
				type: paramType,
				description: aux.description,
				concept: aux.grounding,
				unit: aux.units?.expression,
				value: aux,
				source: sourceValue,
				visibility: false
			});
		});
	}
	tableFormattedParams.value = formattedParams;
};

const conceptSearchTerm = ref({
	curie: '',
	name: ''
});

const modelType = computed(() => getModelType(props.model));

// const addPlusMinus = ref(10);

const expandedRows = ref([]);

const openMatrixModal = (datum: ModelConfigTableData) => {
	// Matrix effect easter egg (shows matrix effect 1 in 10 times a person clicks the Matrix button)
	matrixEffect();

	const id = datum.id;
	if (!datum.tableFormattedMatrix) return;
	matrixModalContext.value = {
		isOpen: true,
		matrixId: id
	};
};

const rowClass = (rowData) => (rowData.type === ParamType.MATRIX ? '' : 'no-expander');

const updateCellValue = (v: any) => {
	const clone = cloneDeep(props.model);
	updateVariable(clone, 'parameters', v.variableName, v.newValue, v.mathml);
	emit('update-model', clone);
};

const updateParamValue = (param: ModelParameter, key: string, value: any) => {
	const clonedParam = cloneDeep(param);
	clonedParam[key] = value;
	emit('update-value', [clonedParam]);
};

const openSuggestedValuesModal = (id: string) => {
	suggestedValuesModalContext.value.isOpen = true;
	suggestedValuesModalContext.value.id = id;
};

const onCloseSuggestedValuesModal = () => {
	suggestedValuesModalContext.value.isOpen = false;
	suggestedValuesModalContext.value.id = '';
	selectedValue.value = null;
};

const applySelectedValue = () => {
	if (!selectedValue.value) return;

	const clonedModel = cloneDeep(props.model);
	const metadata =
		selectedValue.value.configuration.configuration.metadata?.parameters?.[
			selectedValue.value.parameter.id
		] ?? {};

	clonedModel.metadata ??= {};
	clonedModel.metadata.parameters ??= {};
	clonedModel.metadata.parameters[selectedValue.value.parameter.id] = metadata;

	// default source to use confirguration's source if there is no source
	clonedModel.metadata.parameters[selectedValue.value.parameter.id].source =
		metadata?.source ??
		selectedValue.value.configuration.configuration.metadata?.source?.join(', ');

	let parameterIdx;
	if (modelType.value === AMRSchemaNames.REGNET) {
		parameterIdx = clonedModel.model.parameters.findIndex(
			(p) => p.id === selectedValue.value?.parameter.id
		);
		clonedModel.model.parameters[parameterIdx] = selectedValue.value.parameter;
	} else {
		parameterIdx = clonedModel.semantics?.ode.parameters?.findIndex(
			(p) => p.id === selectedValue.value?.parameter.id
		);
		if (clonedModel.semantics?.ode.parameters)
			clonedModel.semantics.ode.parameters[parameterIdx] = selectedValue.value.parameter;
	}

	emit('update-model', clonedModel);
	onCloseSuggestedValuesModal();
};

const countSuggestions = (id): number =>
	props.modelConfigurations?.filter((configuration) =>
		getParameters(configuration.configuration).find((p) => p.id === id)
	).length ?? 0;

watch(
	() => [parameters.value, props.model],
	(val) => {
		if (!val) return;
		buildParameterTable();
	},
	{ deep: true, immediate: true }
);
</script>

<style scoped>
.truncate-text {
	display: flex;
	width: 10rem;
}

.p-datatable.p-datatable-sm :deep(.p-datatable-tbody > tr > td) {
	padding: 0;
}

.p-datatable :deep(.p-datatable-tbody > tr.no-expander > td .p-row-toggler) {
	visibility: hidden;
}

.p-datatable :deep(.p-datatable-tbody > tr.no-expander) {
	background: var(--surface-0);
}

.p-datatable :deep(.p-datatable-tbody > tr.no-expander > td) {
	padding: 0;
}

.hide-header :deep(.p-datatable-thead) {
	display: none;
}

.distribution-container {
	display: flex;
	align-items: center;
	gap: var(--gap-small);
}

.distribution-item {
	width: 100%;
	font-feature-settings: 'tnum';
	font-size: var(--font-caption);
	text-align: right;
}

.constant-number {
	font-feature-settings: 'tnum';
	font-size: var(--font-caption);
	text-align: right;
}

.add-plus-minus {
	width: 3rem;
	margin-left: var(--gap-xsmall);
}

.tabular-numbers {
	font-feature-settings: 'tnum';
	font-size: var(--font-caption);
	text-align: right;
}

/* FIXMEL Reapply these later */
.min-value {
	position: relative;
}
.min-value::before {
	content: 'Min';
	position: relative;
	top: 11px;
	left: var(--gap-small);
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
	width: 0;
}
.max-value::before {
	content: 'Max';
	position: relative;
	top: 11px;
	left: var(--gap-small);
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
	width: 0;
}

.custom-icon-distribution {
	background-image: url('@assets/svg/icons/distribution.svg');
	background-size: contain;
	background-repeat: no-repeat;
	display: inline-block;
	width: 1rem;
	height: 1rem;
}
.custom-icon-expression {
	background-image: url('@assets/svg/icons/expression.svg');
	background-size: contain;
	background-repeat: no-repeat;
	display: inline-block;
	width: 1rem;
	height: 1rem;
}
.invalid-message {
	color: var(--text-color-danger);
}

.secondary-text {
	color: var(--text-color-subdued);
}

.value-type-dropdown {
	min-width: 10rem;
}
</style>
