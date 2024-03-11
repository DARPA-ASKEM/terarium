<template>
	<Datatable
		:value="data"
		v-model:expanded-rows="expandedRows"
		dataKey="id"
		:row-class="rowClass"
		size="small"
		:class="{ 'hide-header': hideHeader }"
	>
		<!-- Row expander, ID and Name columns -->
		<Column expander class="w-3rem" />
		<Column header="ID">
			<template #body="slotProps">
				<span class="truncate-text" :title="slotProps.data.yourFieldName">
					{{ slotProps.data.id }}
				</span>
			</template>
		</Column>
		<Column header="Name">
			<template #body="slotProps">
				<span class="truncate-text" :title="slotProps.data.yourFieldName">
					{{ slotProps.data.name }}
				</span>
			</template>
		</Column>

		<!-- Value type: Matrix or Expression, or a Dropdown with: Time varying, Constant, Distribution (with icons) -->
		<Column field="type" header="Type" class="w-2">
			<template #body="slotProps">
				<!-- Matrix -->
				<Button
					v-if="slotProps.data.type === ParamType.MATRIX"
					text
					icon="pi pi-table"
					label="Matrix"
					@click="openMatrixModal(slotProps.data)"
					class="p-0"
				/>

				<!-- Expression -->
				<span
					v-else-if="slotProps.data.type === ParamType.EXPRESSION"
					class="flex align-items-center"
				>
					<span class="custom-icon-expression mr-2" />
					Expression
				</span>

				<!-- Constant, Distribution, and Time Varying -->
				<Dropdown
					v-else
					class="value-type-dropdown w-9"
					:model-value="slotProps.data.type"
					:options="typeOptions"
					optionLabel="label"
					@update:model-value="(val) => changeType(slotProps.data, val.value)"
					optionValue="paramType"
				>
					<template #value="slotProps">
						<span class="flex align-items-center">
							<span class="p-dropdown-item-icon mr-2" :class="typeOptions[slotProps.value].icon" />
							{{ typeOptions[slotProps.value].label }}
						</span>
					</template>
					<template #option="slotProps">
						<span class="flex align-items-center">
							<span class="p-dropdown-item-icon mr-2" :class="slotProps.option.icon" />
							{{ slotProps.option.label }}
						</span>
					</template>
				</Dropdown>
			</template>
		</Column>

		<!-- Value: the thing we show depends on the type of number -->
		<Column field="value" header="Value" class="w-3 pr-2">
			<template #body="slotProps">
				<!-- Matrix -->
				<span
					v-if="slotProps.data.type === ParamType.MATRIX"
					@click="openMatrixModal(slotProps.data)"
					class="cursor-pointer secondary-text"
					>Click to open</span
				>

				<!-- Expression -->
				<span v-else-if="slotProps.data.type === ParamType.EXPRESSION">
					<InputText
						size="small"
						class="tabular-numbers w-full"
						v-model.lazy="slotProps.data.value.expression"
						@update:model-value="updateExpression(slotProps.data.value)"
					/>
				</span>

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
						@update:model-value="emit('update-value', [slotProps.data.value])"
					/>
					<!-- This is a button with an input field inside it, weird huh?, but it works -->
					<Button
						class="ml-2 py-0 w-5"
						text
						@click="constantToDistribution(slotProps.data)"
						v-tooltip.top="'Convert to distribution'"
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
						@update:model-value="(val) => updateTimeseries(slotProps.data.value.id, val)"
					/>
					<small v-if="errorMessage" class="invalid-message">{{ errorMessage }}</small>
				</span>
			</template>
		</Column>

		<!-- Source  -->
		<Column field="source" header="Source" class="w-3">
			<template #body="{ data }">
				<InputText
					v-if="data.type !== ParamType.MATRIX"
					size="small"
					class="w-full"
					v-model.lazy="data.source"
					@update:model-value="(val) => updateSource(data.value.id ?? data.value.target, val)"
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

	<canvas id="matrix-canvas" />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import Button from 'primevue/button';
import InputNumber from 'primevue/inputnumber';
import type {
	Initial,
	ModelConfiguration,
	ModelDistribution,
	ModelMetadata,
	ModelParameter
} from '@/types/Types';
import { getStratificationType } from '@/model-representation/petrinet/petrinet-service';
import { StratifiedMatrix } from '@/types/Model';
import Datatable from 'primevue/datatable';
import Column from 'primevue/column';
import TeraStratifiedMatrixModal from '@/components/model/petrinet/model-configurations/tera-stratified-matrix-modal.vue';
import { AMRSchemaNames, ModelConfigTableData, ParamType } from '@/types/common';
import Dropdown from 'primevue/dropdown';
import { pythonInstance } from '@/python/PyodideController';
import InputText from 'primevue/inputtext';
import { getModelType } from '@/services/model';
import { cloneDeep } from 'lodash';

interface Option {
	label: string;
	paramType: ParamType;
	icon: string;
}

const typeOptions: Option[] = [
	{ label: 'Constant', paramType: ParamType.CONSTANT, icon: 'pi pi-hashtag' },
	{ label: 'Distribution', paramType: ParamType.DISTRIBUTION, icon: 'custom-icon-distribution' },
	{ label: 'Time varying', paramType: ParamType.TIME_SERIES, icon: 'pi pi-clock' }
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

const modelType = computed(() => getModelType(props.modelConfiguration.configuration));
const stratifiedModelType = computed(() =>
	getStratificationType(props.modelConfiguration.configuration)
);

const addPlusMinus = ref(10);

const errorMessage = ref('');

const expandedRows = ref([]);

const isInitial = (obj: Initial | ModelParameter): obj is Initial => 'target' in obj;

const openMatrixModal = (datum: ModelConfigTableData) => {
	// Matrix effect Easter egg (shows matrix effect 1 in 10 times a person clicks the Matrix button)
	matrixEffect();

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

const updateSource = (id: string, value: string) => {
	const clonedConfig = cloneDeep(props.modelConfiguration);
	clonedConfig.configuration.metadata.sources[id] = value;
	emit('update-configuration', clonedConfig);
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

/**
 * Change the type of the parameter
 * @param data The parameter to change
 * @param newParamType The new ParamType
 */
const changeType = (data: ModelConfigTableData, newParamType: ParamType) => {
	console.debug('Changing type', data, newParamType);
	// Keep the previous value for the previous ParamType
	if (!data.values) data.values = new Map();
	data.values.set(data.type, data.value);

	data.type = newParamType;

	// Define default values for different ParamTypes
	let defaultValue: any;
	if (newParamType === ParamType.DISTRIBUTION) {
		defaultValue = {
			type: 'Uniform1',
			parameters: {
				minimum: 0,
				maximum: 0
			}
		} as ModelDistribution;
	} else if (newParamType === ParamType.TIME_SERIES) {
		defaultValue = { [data.id]: '' } as ModelMetadata['timeseries'];
	} else {
		// ParamType.CONSTANT
		defaultValue = 0;
	}

	// Set the value based on the new ParamType
	if (data.values.has(newParamType)) {
		data.value = data.values.get(newParamType) ?? defaultValue;
	} else {
		data.value = defaultValue;
	}

	// Update the configuration
	replaceParameter(data);
};

function constantToDistribution(data: ModelConfigTableData) {
	const constant = data.value as number;

	// Save the current value into the values map
	if (!data.values) data.values = new Map();
	data.values.set(ParamType.CONSTANT, constant);

	// Create the distribution
	const newDistribution: ModelDistribution = {
		type: 'Uniform1',
		parameters: {
			minimum: constant - (constant * addPlusMinus.value) / 100,
			maximum: constant + (constant * addPlusMinus.value) / 100
		}
	};

	// Save the distribution as value and into the values map
	data.type = ParamType.DISTRIBUTION;
	data.value = newDistribution;
	data.values.set(ParamType.DISTRIBUTION, newDistribution);

	// Update the configuration
	replaceParameter(data);
}

/**
 * Replace the parameter in the model configuration with the new data
 * @param data The new data
 */
const replaceParameter = (data: ModelConfigTableData) => {
	// Clone the model configuration
	const clonedConfig = cloneDeep(props.modelConfiguration);

	// Fetch the parameter to be updated
	const isPetrinetOrStockflow =
		modelType.value === AMRSchemaNames.PETRINET || modelType.value === AMRSchemaNames.STOCKFLOW;
	const parameters = isPetrinetOrStockflow
		? clonedConfig.configuration.semantics.ode.parameters
		: clonedConfig.configuration.model.parameters;
	const parameter = parameters.find((p) => p.id === data.id);

	// Make sure we have a parameter to update
	if (!parameter) {
		console.warn(`Parameter ${data.id} not found in the configuration`);
		return;
	}

	// Delete the old parameter values from the configuration
	delete parameter?.value; // Constant
	delete parameter?.distribution; // Distribution
	delete clonedConfig.configuration.metadata?.timeseries?.[parameter.id]; // Timeseries

	// Update the parameter with the new value
	if (data.type === ParamType.CONSTANT) {
		parameter.value = data.value;
	} else if (data.type === ParamType.DISTRIBUTION) {
		parameter.distribution = data.value as ModelDistribution;
	} else if (data.type === ParamType.TIME_SERIES) {
		if (!clonedConfig.configuration?.metadata?.timeseries) {
			clonedConfig.configuration.metadata.timeseries = {};
		}
		clonedConfig.configuration.metadata.timeseries[parameter.id] =
			data.value as ModelMetadata['timeseries'];
	}

	emit('update-configuration', clonedConfig);
};

const updateExpression = async (value: Initial) => {
	value.expression_mathml = (await pythonInstance.parseExpression(value.expression)).mathml;
	emit('update-value', [value]);
};

/* Matrix effect Easter egg: This gets triggered 1 in 10 times a person clicks the Matrix button */
const matrixEffect = () => {
	if (Math.random() > 0.1) return;
	const canvas = document.getElementById('matrix-canvas') as HTMLCanvasElement | null;
	if (!canvas) return;
	const ctx = (canvas as HTMLCanvasElement)?.getContext('2d');

	// eslint-disable-next-line no-multi-assign
	const w = (canvas.width = document.body.offsetWidth);
	// eslint-disable-next-line no-multi-assign
	const h = (canvas.height = document.body.offsetHeight);
	const cols = Math.floor(w / 20) + 1;
	const ypos = Array(cols).fill(0);

	if (ctx) {
		ctx.fillStyle = '#FFF';
		ctx.fillRect(0, 0, w, h);
	}

	function matrix() {
		if (ctx) {
			ctx.fillStyle = '#FFF1';
			ctx.fillRect(0, 0, w, h);

			ctx.fillStyle = '#1B8073';
			ctx.font = '15pt monospace';

			ypos.forEach((y, ind) => {
				const text = String.fromCharCode(Math.random() * 128);
				const x = ind * 20;
				ctx.fillText(text, x, y);
				if (y > 100 + Math.random() * 10000) ypos[ind] = 0;
				else ypos[ind] = y + 20;
			});
		}
	}

	const intervalId = setInterval(matrix, 33);

	// after 4 seconds begin the fade out
	setTimeout(() => {
		if (canvas) {
			canvas.style.opacity = '0';
		}
	}, 3000);

	// after 5 seconds clear the canvas, stop the interval, and reset the opacity
	setTimeout(() => {
		clearInterval(intervalId);
		if (ctx) {
			ctx.clearRect(0, 0, w, h);
		}
		if (canvas) {
			canvas.style.opacity = '1';
		}
	}, 4000);
};
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

#matrix-canvas {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	z-index: 1000;
	pointer-events: none;
	mix-blend-mode: darken;
	opacity: 1;
	transition: opacity 1s;
}
</style>
