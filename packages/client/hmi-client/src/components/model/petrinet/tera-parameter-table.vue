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
		<Column header="Symbol">
			<template #body="slotProps">
				<span class="truncate-text">
					{{ slotProps.data.id }}
				</span>
			</template>
		</Column>
		<Column header="Name">
			<template #body="{ data }">
				<InputText
					size="small"
					v-model.lazy="data.name"
					:disabled="configView || readonly || data.type === ParamType.MATRIX"
					@update:model-value="updateParamValue(data.value, 'name', $event)"
				/>
			</template>
		</Column>

		<Column header="Description" class="w-2">
			<template #body="{ data }">
				<InputText
					size="small"
					v-model.lazy="data.description"
					:disabled="configView || readonly || data.type === ParamType.MATRIX"
					@update:model-value="updateParamValue(data.value, 'description', $event)"
				/>
			</template>
		</Column>

		<Column header="Concept" class="w-1">
			<template #body="{ data }">
				<template v-if="data.concept?.identifiers && !isEmpty(data.concept.identifiers)">
					{{
						getNameOfCurieCached(
							nameOfCurieCache,
							getCurieFromGroudingIdentifier(data.concept.identifiers)
						)
					}}

					<a
						target="_blank"
						rel="noopener noreferrer"
						:href="getCurieUrl(getCurieFromGroudingIdentifier(data.concept.identifiers))"
						@click.stop
						aria-label="Open Concept"
					>
						<i class="pi pi-external-link" />
					</a>
				</template>
				<template v-else>--</template>
			</template>
			<template v-if="!configView && !readonly" #editor="{ data }">
				<AutoComplete
					v-model="conceptSearchTerm.name"
					:suggestions="curies"
					@complete="onSearch"
					@item-select="
						updateParamValue(data.value, 'grounding', {
							identifiers: parseCurie($event.value.curie)
						})
					"
					optionLabel="name"
					:forceSelection="true"
					:inputStyle="{ width: '100%' }"
				/>
			</template>
		</Column>

		<Column header="Unit" class="w-1">
			<template #body="slotProps">
				<InputText
					v-if="slotProps.data.type !== ParamType.MATRIX"
					size="small"
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

		<!-- Value type: Matrix or a Dropdown with: Time varying, Constant, Distribution (with icons) -->
		<Column field="type" header="Value Type" class="w-2">
			<template #body="slotProps">
				<Button
					text
					v-if="slotProps.data.type === ParamType.MATRIX"
					icon="pi pi-table"
					label="Matrix"
					@click="openMatrixModal(slotProps.data)"
					class="p-0"
				/>
				<Dropdown
					v-else
					class="value-type-dropdown w-9"
					:model-value="slotProps.data.type"
					:options="typeOptions"
					optionLabel="label"
					optionValue="value"
					placeholder="Select a parameter type"
					:disabled="readonly"
					@update:model-value="(val) => changeType(slotProps.data.value, val)"
				>
					<template #value="slotProps">
						<span class="flex align-items-center">
							<span
								class="p-dropdown-item-icon mr-2"
								:class="typeOptions[slotProps.value].icon"
							></span>
							<span>{{ typeOptions[slotProps.value].label }}</span>
						</span>
					</template>
					<template #option="slotProps">
						<span class="flex align-items-center">
							<span class="p-dropdown-item-icon mr-2" :class="slotProps.option.icon"></span>
							<span>{{ slotProps.option.label }}</span>
						</span>
					</template>
				</Dropdown>
			</template>
		</Column>

		<!-- Value: the thing we show depends on the type of number -->
		<Column field="value" header="Value" class="w-2 pr-2">
			<template #body="slotProps">
				<!-- Matrix -->
				<span
					v-if="slotProps.data.type === ParamType.MATRIX"
					@click="openMatrixModal(slotProps.data)"
					class="cursor-pointer secondary-text"
					>Click to open</span
				>
				<!-- Distribution -->
				<div
					v-else-if="slotProps.data.type === ParamType.DISTRIBUTION"
					class="distribution-container"
				>
					<InputNumber
						class="distribution-item min-value"
						size="small"
						inputId="numericInput"
						mode="decimal"
						:min-fraction-digits="1"
						:max-fraction-digits="10"
						v-model.lazy="slotProps.data.value.distribution.parameters.minimum"
						:disabled="readonly"
						@update:model-value="emit('update-value', [slotProps.data.value])"
					/>
					<InputNumber
						class="distribution-item max-value"
						size="small"
						inputId="numericInput"
						mode="decimal"
						:min-fraction-digits="1"
						:max-fraction-digits="10"
						v-model.lazy="slotProps.data.value.distribution.parameters.maximum"
						:disabled="readonly"
						@update:model-value="emit('update-value', [slotProps.data.value])"
					/>
				</div>

				<!-- Constant: Includes the value, a button and input box to convert it into a distributions with a customizable range -->
				<span
					v-else-if="slotProps.data.type === ParamType.CONSTANT"
					class="flex align-items-center"
				>
					<InputNumber
						size="small"
						class="constant-number"
						inputId="numericInput"
						mode="decimal"
						:min-fraction-digits="1"
						:max-fraction-digits="10"
						v-model.lazy="slotProps.data.value.value"
						:disabled="readonly"
						@update:model-value="emit('update-value', [slotProps.data.value])"
					/>
					<!-- This is a button with an input field inside it, weird huh?, but it works -->
					<Button
						v-if="!readonly"
						class="ml-2 pt-0 pb-0 w-5"
						text
						@click="changeType(slotProps.data.value, 1)"
						v-tooltip="'Convert to distribution'"
					>
						<span class="white-space-nowrap text-sm">Add ±</span>
						<InputNumber
							v-model="addPlusMinus"
							size="small"
							text
							class="constant-number add-plus-minus w-full"
							inputId="convert-to-distribution"
							suffix="%"
							:min="0"
							:max="100"
							:disabled="readonly"
							@click.stop
						/>
					</Button>
				</span>

				<!-- Time series -->
				<span
					class="timeseries-container mr-2"
					v-else-if="slotProps.data.type === ParamType.TIME_SERIES"
				>
					<InputText
						size="small"
						:placeholder="'step:value, step:value, (e.g., 0:25, 1:26, 2:27 etc.)'"
						v-model.lazy="slotProps.data.timeseries"
						:disabled="readonly"
						@update:model-value="(val) => updateTimeseries(slotProps.data.value.id, val)"
					/>
					<small v-if="errorMessage" class="invalid-message">{{ errorMessage }}</small>
				</span>
			</template>
		</Column>

		<!-- Source  -->
		<Column field="source" header="Source" class="w-2">
			<template #body="{ data }">
				<InputText
					v-if="data.type !== ParamType.MATRIX"
					size="small"
					class="w-full"
					v-model.lazy="data.source"
					:disabled="readonly"
					@update:model-value="(val) => updateSource(data.value.id ?? data.value.target, val)"
				/>
			</template>
		</Column>

		<!-- Suggested Configurations Button -->
		<Column v-if="configView && !readonly">
			<template #body="{ data }">
				<Button
					v-if="data.type !== ParamType.MATRIX"
					text
					size="small"
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
			:open-value-config="matrixModalContext.isOpen"
			@close-modal="matrixModalContext.isOpen = false"
			@update-cell-value="(configToUpdate: any) => updateCellValue(configToUpdate)"
		/>

		<tera-modal
			v-if="suggestedValuesModalContext.isOpen"
			@modal-mask-clicked="onCloseSuggestedValuesModal"
		>
			<template #header
				><h5>Suggested configurations for {{ suggestedValuesModalContext.id }}</h5></template
			>
			<Datatable
				:value="suggestedValues"
				dataKey="index"
				v-model:selection="selectedValue"
				tableStyle="min-width: 50rem"
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
				<Column header="Value Type">
					<template #body="{ data }">
						{{ typeOptions[getParamType(data.parameter, data.configuration.configuration)].label }}
					</template>
				</Column>
				<Column header="Value">
					<template #body="{ data }">
						<span
							v-if="
								getParamType(data.parameter, data.configuration.configuration) ===
								ParamType.CONSTANT
							"
						>
							{{ data.parameter.value }}
						</span>
						<div
							class="distribution-container"
							v-else-if="
								getParamType(data.parameter, data.configuration.configuration) ===
								ParamType.DISTRIBUTION
							"
						>
							<span>Min: {{ data.parameter.distribution.parameters.minimum }}</span>
							<span>Max: {{ data.parameter.distribution.parameters.maximum }}</span>
						</div>
						<span
							v-else-if="
								getParamType(data.parameter, data.configuration.configuration) ===
								ParamType.TIME_SERIES
							"
						>
							{{ data.configuration?.configuration?.metadata?.timeseries?.[data.parameter.id] }}
						</span>
					</template>
				</Column>
				<Column header="Source">
					<template #body="{ data }">
						<span>{{
							data.configuration.configuration.metadata?.parameters?.[data.parameter.id]?.source
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
import { computed, ref } from 'vue';
import { cloneDeep, isEmpty } from 'lodash';
import Button from 'primevue/button';
import InputNumber from 'primevue/inputnumber';
import type { DKG, Model, ModelConfiguration, ModelParameter } from '@/types/Types';
import Dropdown from 'primevue/dropdown';
import InputText from 'primevue/inputtext';
import Datatable from 'primevue/datatable';
import Column from 'primevue/column';
import { StratifiedMatrix } from '@/types/Model';
import TeraStratifiedMatrixModal from '@/components/model/petrinet/model-configurations/tera-stratified-matrix-modal.vue';
import { AMRSchemaNames, ModelConfigTableData, ParamType } from '@/types/common';
import {
	getCurieFromGroudingIdentifier,
	getCurieUrl,
	getNameOfCurieCached,
	parseCurie,
	searchCuriesEntities
} from '@/services/concept';
import AutoComplete, { AutoCompleteCompleteEvent } from 'primevue/autocomplete';

import { getModelType } from '@/services/model';
import { matrixEffect } from '@/utils/easter-eggs';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { isStratifiedModel, collapseParameters } from '@/model-representation/mira/mira';
import { updateVariable } from '@/model-representation/service';
import TeraModal from '@/components/widgets/tera-modal.vue';

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

const typeOptions = [
	{ label: 'Constant', value: ParamType.CONSTANT, icon: 'pi pi-hashtag' },
	{ label: 'Distribution', value: ParamType.DISTRIBUTION, icon: 'custom-icon-distribution' },
	{ label: 'Time varying', value: ParamType.TIME_SERIES, icon: 'pi pi-clock' }
];

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
		if (modelType.value !== AMRSchemaNames.REGNET) {
			configuration.configuration?.semantics?.ode.parameters?.forEach((parameter) => {
				if (parameter.id === suggestedValuesModalContext.value.id) {
					matchingParameters.push({
						parameter,
						configuration,
						index: i
					});
				}
			});
		} else {
			configuration.configuration?.model?.parameters?.forEach((parameter) => {
				if (parameter.id === suggestedValuesModalContext.value.id) {
					matchingParameters.push({
						parameter,
						configuration,
						index: i
					});
				}
			});
		}
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

const getParamType = (param: ModelParameter | undefined, model: Model = props.model) => {
	let type = ParamType.CONSTANT;
	if (!param) return type;
	if (model.metadata?.timeseries?.[param.id] || model.metadata?.timeseries?.[param.id] === '') {
		type = ParamType.TIME_SERIES;
	} else if (param?.distribution) {
		type = ParamType.DISTRIBUTION;
	}
	return type;
};

const tableFormattedParams = computed<ModelConfigTableData[]>(() => {
	const model = props.model;
	const formattedParams: ModelConfigTableData[] = [];

	if (isStratified.value) {
		parameters.value.forEach((vals, init) => {
			const tableFormattedMatrix: ModelConfigTableData[] = vals.map((v) => {
				let param;
				if (modelType.value === AMRSchemaNames.REGNET) {
					param = model.model.parameters.find((i) => i.id === v);
				} else {
					param = model.semantics?.ode?.parameters?.find((i) => i.id === v);
				}
				const paramType = getParamType(param);
				const timeseriesValue = model.metadata?.timeseries?.[param!.id];
				const parametersMetadata = model.metadata?.parameters?.[param!.id];
				const sourceValue = parametersMetadata?.source;
				return {
					id: v,
					name: param.name,
					type: paramType,
					description: param.description,
					concept: param.grounding,
					unit: param.unit?.expression,
					value: param,
					source: sourceValue,
					visibility: false,
					timeseries: timeseriesValue
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
			let param;
			if (modelType.value === AMRSchemaNames.REGNET) {
				param = model.model.parameters.find((i) => i.id === vals[0]);
			} else {
				param = model.semantics?.ode.parameters?.find((i) => i.id === vals[0]);
			}

			const paramType = getParamType(param);

			const timeseriesValue = model.metadata?.timeseries?.[param!.id];
			const parametersMetadata = model.metadata?.parameters?.[param!.id];
			const sourceValue = parametersMetadata?.source;
			formattedParams.push({
				id: init,
				name: param.name,
				type: paramType,
				description: param.description,
				concept: param.grounding,
				unit: param.unit?.expression,
				value: param,
				source: sourceValue,
				visibility: false,
				timeseries: timeseriesValue
			});
		});
	}

	return formattedParams;
});

const conceptSearchTerm = ref({
	curie: '',
	name: ''
});
const nameOfCurieCache = ref(new Map<string, string>());

const curies = ref<DKG[]>([]);

const modelType = computed(() => getModelType(props.model));

const addPlusMinus = ref(10);

const errorMessage = ref('');

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

const updateTimeseries = (id: string, value: string) => {
	if (!validateTimeSeries(value)) return;
	const clonedModel = cloneDeep(props.model);
	clonedModel.metadata ??= {};
	clonedModel.metadata.timeseries ??= {};
	clonedModel.metadata.timeseries[id] = value;
	emit('update-model', clonedModel);
};

const updateSource = (id: string, value: string) => {
	const clonedModel = cloneDeep(props.model);
	if (!clonedModel.metadata?.parameters?.[id]) {
		clonedModel.metadata ??= {};
		clonedModel.metadata.parameters ??= {};
		clonedModel.metadata.parameters[id] = {};
	}
	clonedModel.metadata.parameters[id].source = value;
	emit('update-model', clonedModel);
};

const validateTimeSeries = (values: string) => {
	const message = 'Incorrect format (e.g., 0:500)';
	if (typeof values !== 'string') {
		errorMessage.value = message;
		return false;
	}

	const isPairValid = (pair: string): boolean => /^\d+:\d+(\.\d+)?$/.test(pair.trim());
	const isValid = values.split(',').every(isPairValid);
	errorMessage.value = isValid ? '' : message;
	return isValid;
};

const changeType = (param: ModelParameter, typeIndex: number) => {
	// FIXME: changing between parameter types will delete the previous values of distribution or timeseries, ideally we would want to keep these.
	const type = typeOptions[typeIndex];
	const clonedModel = cloneDeep(props.model);

	let idx;
	if (modelType.value === AMRSchemaNames.PETRINET || modelType.value === AMRSchemaNames.STOCKFLOW) {
		idx = clonedModel.semantics?.ode.parameters?.findIndex((p) => p.id === param.id);
	} else if (modelType.value === AMRSchemaNames.REGNET) {
		idx = clonedModel.model.parameters.findIndex((p) => p.id === param.id);
	}
	switch (type.value) {
		case ParamType.CONSTANT:
			delete clonedModel.metadata?.timeseries?.[param.id];
			replaceParam(clonedModel, param, idx);
			break;
		case ParamType.DISTRIBUTION:
			delete clonedModel.metadata?.timeseries?.[param.id];
			param.distribution = {
				type: 'Uniform1',
				parameters: {
					minimum: 0,
					maximum: 0
				}
			};
			replaceParam(clonedModel, param, idx);
			break;
		case ParamType.TIME_SERIES:
			if (!clonedModel.metadata?.timeseries) {
				clonedModel.metadata ??= {};
				clonedModel.metadata.timeseries = {};
			}
			replaceParam(clonedModel, param, idx);
			clonedModel.metadata.timeseries[param.id] = '';
			break;
		default:
			break;
	}
	emit('update-model', clonedModel);
};

const replaceParam = (model: Model, param: any, index: number) => {
	if (modelType.value === AMRSchemaNames.PETRINET || modelType.value === AMRSchemaNames.STOCKFLOW) {
		if (model.semantics?.ode.parameters) model.semantics.ode.parameters[index] = param;
	} else if (modelType.value === AMRSchemaNames.REGNET) {
		model.model.parameters[index] = param;
	}
};

async function onSearch(event: AutoCompleteCompleteEvent) {
	const query = event.query;
	if (query.length > 2) {
		const response = await searchCuriesEntities(query);
		curies.value = response;
	}
}

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
	const timeseries =
		selectedValue.value.configuration.configuration.metadata?.timeseries?.[
			selectedValue.value.parameter.id
		];
	const metadata =
		selectedValue.value.configuration.configuration.metadata?.parameters?.[
			selectedValue.value.parameter.id
		];

	clonedModel.metadata ??= {};
	clonedModel.metadata.parameters ??= {};
	clonedModel.metadata.timeseries ??= {};
	clonedModel.metadata.parameters[selectedValue.value.parameter.id] = metadata;
	clonedModel.metadata.timeseries[selectedValue.value.parameter.id] = timeseries;

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
	props.modelConfigurations?.filter((configuration) => {
		if (modelType.value !== AMRSchemaNames.REGNET) {
			return configuration.configuration?.semantics?.ode.parameters?.find((p) => p.id === id);
		}

		return configuration.configuration?.model?.parameters?.find((p) => p.id === id);
	}).length ?? 0;
</script>

<style scoped>
.truncate-text {
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}

.p-datatable.p-datatable-sm :deep(.p-datatable-tbody > tr > td) {
	padding: 0;
}

.p-datatable :deep(.p-datatable-tbody > tr.no-expander > td .p-row-toggler) {
	display: none;
}

.p-datatable :deep(.p-datatable-tbody > tr.no-expander) {
	background: var(--surface-highlight);
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

.distribution-item > :deep(input) {
	width: 100%;
	font-feature-settings: 'tnum';
	font-size: var(--font-caption);
	text-align: right;
}

.constant-number > :deep(input) {
	font-feature-settings: 'tnum';
	font-size: var(--font-caption);
	text-align: right;
}

.add-plus-minus > :deep(input) {
	width: 3rem;
	margin-left: var(--gap-xsmall);
}

.tabular-numbers {
	font-feature-settings: 'tnum';
	font-size: var(--font-caption);
	text-align: right;
}

.min-value {
	position: relative;
}
.min-value::before {
	content: 'Min';
	position: relative;
	top: var(--gap-small);
	left: var(--gap-small);
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
	width: 0;
}
.max-value::before {
	content: 'Max';
	position: relative;
	top: var(--gap-small);
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

.timeseries-container {
	display: flex;
	flex-direction: column;
}

.secondary-text {
	color: var(--text-color-subdued);
}

.value-type-dropdown {
	min-width: 10rem;
}
</style>
