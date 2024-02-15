<template>
	<Datatable
		:value="data"
		v-model:expanded-rows="expandedRows"
		dataKey="id"
		:row-class="rowClass"
		size="small"
		:class="{ 'hide-header': hideHeader }"
	>
		<Column expander style="width: 5rem" />
		<Column field="id" header="ID" style="width: 10%"></Column>
		<Column field="name" header="Name" style="width: 15%"></Column>
		<Column field="type" header="Value type" style="width: 10%">
			<template #body="slotProps">
				<Button
					text
					v-if="slotProps.data.type === ParamType.MATRIX"
					icon="pi pi-table"
					label="Matrix"
					@click="openMatrixModal(slotProps.data)"
					class="p-0"
				/>
				<span v-else-if="slotProps.data.type === ParamType.EXPRESSION">Expression</span>
				<Dropdown
					v-else
					:model-value="slotProps.data.type"
					:options="typeOptions"
					optionLabel="label"
					optionValue="value"
					placeholder="Select a parameter type"
					@update:model-value="(val) => changeType(slotProps.data.value, val)"
				/>
			</template>
		</Column>
		<Column field="value" header="Value" style="width: 15%">
			<template #body="slotProps">
				<span
					v-if="slotProps.data.type === ParamType.MATRIX"
					@click="openMatrixModal(slotProps.data)"
					class="cursor-pointer secondary-text"
					>Click to view</span
				>
				<span v-else-if="slotProps.data.type === ParamType.EXPRESSION">
					<InputText
						size="small"
						v-model.lazy="slotProps.data.value.expression"
						@update:model-value="updateExpression(slotProps.data.value)"
					/>
				</span>
				<div
					v-else-if="slotProps.data.type === ParamType.DISTRIBUTION"
					class="distribution-container"
				>
					<label>Min</label>
					<InputNumber
						class="distribution-item"
						size="small"
						inputId="numericInput"
						mode="decimal"
						:min-fraction-digits="1"
						:max-fraction-digits="10"
						v-model.lazy="slotProps.data.value.distribution.parameters.minimum"
						@update:model-value="emit('update-value', [slotProps.data.value])"
					/>
					<label>Max</label>
					<InputNumber
						class="distribution-item"
						size="small"
						inputId="numericInput"
						mode="decimal"
						:min-fraction-digits="1"
						:max-fraction-digits="10"
						v-model.lazy="slotProps.data.value.distribution.parameters.maximum"
						@update:model-value="emit('update-value', [slotProps.data.value])"
					/>
				</div>
				<span v-else-if="slotProps.data.type === ParamType.CONSTANT">
					<InputNumber
						size="small"
						inputId="numericInput"
						mode="decimal"
						:min-fraction-digits="1"
						:max-fraction-digits="10"
						v-model.lazy="slotProps.data.value.value"
						@update:model-value="emit('update-value', [slotProps.data.value])"
					/>
				</span>
				<span
					class="timeseries-container"
					v-else-if="slotProps.data.type === ParamType.TIME_SERIES"
				>
					<InputText
						size="small"
						:placeholder="'(e.g., 0:500, 10:550, 25:700 etc)'"
						v-model.lazy="slotProps.data.timeseries"
						@update:model-value="(val) => updateTimeseries(slotProps.data.value.id, val)"
					/>
					<small v-if="errorMessage" class="invalid-message">{{ errorMessage }}</small>
				</span>
			</template>
		</Column>
		<Column field="source" header="Source" style="width: 35%"></Column>
		<Column field="visibility" header="Visibility" style="width: 10%">
			<template #body="slotProps">
				<InputSwitch v-model="slotProps.data.visibility" @click.stop />
			</template>
		</Column>
		<template #expansion="slotProps">
			<tera-model-config-table
				hide-header
				v-if="slotProps.data.type === ParamType.MATRIX"
				:model-configuration="modelConfiguration"
				:data="slotProps.data.tableFormattedMatrix"
				@update-value="(val: ModelParameter | Initial) => emit('update-value', [val])"
				@update-configuration="(config: ModelConfiguration) => emit('update-configuration', config)"
			/>
		</template>
	</Datatable>
	<Teleport to="body">
		<tera-stratified-matrix-modal
			v-if="matrixModalContext.isOpen && stratifiedModelType"
			:id="matrixModalContext.matrixId"
			:model-configuration="modelConfiguration"
			:stratified-model-type="stratifiedModelType"
			:stratified-matrix-type="matrixModalContext.stratifiedMatrixType"
			:open-value-config="matrixModalContext.isOpen"
			@close-modal="matrixModalContext.isOpen = false"
			@update-configuration="
				(configToUpdate: ModelConfiguration) => emit('update-configuration', configToUpdate)
			"
		/>
	</Teleport>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import Button from 'primevue/button';
import InputNumber from 'primevue/inputnumber';
import type { ModelConfiguration, ModelParameter, Initial } from '@/types/Types';
import { getStratificationType } from '@/model-representation/petrinet/petrinet-service';
import { StratifiedMatrix } from '@/types/Model';
import Datatable from 'primevue/datatable';
import Column from 'primevue/column';
import TeraStratifiedMatrixModal from '@/components/model/petrinet/model-configurations/tera-stratified-matrix-modal.vue';
import { ModelConfigTableData, ParamType } from '@/types/common';
import Dropdown from 'primevue/dropdown';
import InputSwitch from 'primevue/inputswitch';
import { pythonInstance } from '@/python/PyodideController';
import InputText from 'primevue/inputtext';
import { cloneDeep } from 'lodash';

const typeOptions = [
	{ label: 'Constant', value: ParamType.CONSTANT },
	{ label: 'Distribution', value: ParamType.DISTRIBUTION },
	{ label: 'Time Varying', value: ParamType.TIME_SERIES }
];
const props = defineProps<{
	modelConfiguration: ModelConfiguration;
	data: ModelConfigTableData[];
	hideHeader?: boolean;
}>();

const emit = defineEmits(['update-value', 'update-configuration']);

const matrixModalContext = ref({
	isOpen: false,
	stratifiedMatrixType: StratifiedMatrix.Initials,
	matrixId: ''
});

const errorMessage = ref('');

const expandedRows = ref([]);
const isInitial = (obj: Initial | ModelParameter): obj is Initial => 'target' in obj;

const openMatrixModal = (datum: ModelConfigTableData) => {
	const id = datum.id;
	if (!datum.tableFormattedMatrix) return;
	const type = isInitial(datum.tableFormattedMatrix[0].value)
		? StratifiedMatrix.Initials
		: StratifiedMatrix.Parameters;
	matrixModalContext.value = {
		isOpen: true,
		stratifiedMatrixType: type,
		matrixId: id
	};
};

const rowClass = (rowData) => (rowData.type === ParamType.MATRIX ? '' : 'no-expander');

const updateTimeseries = (id: string, value: string) => {
	if (!validateTimeSeries(value)) return;
	const clonedConfig = cloneDeep(props.modelConfiguration);
	clonedConfig.configuration.metadata.timeseries[id] = value;
	emit('update-configuration', clonedConfig);
};

const validateTimeSeries = (values: string) => {
	const message = 'Incorrect Format (e.g., 0:500, 10:550, 25:700 etc)';
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
	const clonedConfig = cloneDeep(props.modelConfiguration);
	const idx = clonedConfig.configuration.semantics.ode.parameters.findIndex(
		(p) => p.id === param.id
	);
	switch (type.value) {
		case ParamType.CONSTANT:
			delete param.distribution;
			delete clonedConfig.configuration.metadata?.timeseries?.[param.id];
			clonedConfig.configuration.semantics.ode.parameters[idx] = param;
			break;
		case ParamType.DISTRIBUTION:
			delete clonedConfig.configuration.metadata?.timeseries?.[param.id];
			param.distribution = {
				type: 'Uniform1',
				parameters: {
					minimum: 0,
					maximum: 0
				}
			};
			clonedConfig.configuration.semantics.ode.parameters[idx] = param;
			break;
		case ParamType.TIME_SERIES:
			delete param.distribution;
			if (!clonedConfig.configuration.metadata?.timeseries) {
				clonedConfig.configuration.metadata.timeseries = {};
			}
			clonedConfig.configuration.semantics.ode.parameters[idx] = param;
			clonedConfig.configuration.metadata.timeseries[param.id] = '';
			break;
		default:
			break;
	}
	emit('update-configuration', clonedConfig);
};

const stratifiedModelType = computed(() =>
	getStratificationType(props.modelConfiguration.configuration)
);

const updateExpression = async (value: Initial) => {
	const mathml = (await pythonInstance.parseExpression(value.expression)).mathml;
	value.expression_mathml = mathml;
	emit('update-value', [value]);
};
</script>

<style scoped>
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
	width: 4rem;
}
.invalid-message {
	color: var(--text-color-danger);
	font-size: var(--font-caption);
}

.timeseries-container {
	display: flex;
	flex-direction: column;
}

.secondary-text {
	color: var(--text-color-subdued);
}
</style>
