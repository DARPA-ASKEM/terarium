<template>
	<main v-if="isConfigurationVisible">
		<div
			class="p-datatable p-component p-datatable-scrollable p-datatable-responsive-scroll p-datatable-gridlines p-datatable-grouped-header model-configuration"
		>
			<div class="p-datatable-wrapper">
				<table class="p-datatable-table p-datatable-scrollable-table editable-cells-table">
					<thead class="p-datatable-thead">
						<!-- Table header 1st row: Initials, Parameters -->
						<tr v-if="!featureConfig.isPreview">
							<th class="p-frozen-column"></th>
							<th class="p-frozen-column second-frozen"></th>
							<th v-for="({ name, colspan }, i) in tableHeaders" :colspan="colspan" :key="i">
								{{ name }}
							</th>
						</tr>
						<!-- Table header 2nd row: Actual column headers -->
						<tr>
							<th class="p-frozen-column" />
							<th class="p-frozen-column second-frozen">Select all</th>
							<th v-for="(variableName, i) in headerInitialsAndParameters" :key="i">
								{{ variableName }}
							</th>
						</tr>
					</thead>
					<!--Different tbody depending on model-->
					<component
						:is="
							stratifiedModelType
								? TeraStratifiedModelConfigurations
								: TeraRegularModelConfigurations
						"
						v-model:editValue="editValue"
						:model-configurations="modelConfigurations"
						:cell-edit-states="cellEditStates"
						:base-initials-and-parameters="headerInitialsAndParameters"
						@update-value="updateValue"
						@update-name="updateName"
						@enter-name-cell="onEnterNameCell"
						@enter-value-cell="onEnterValueCell"
						v-on="{ 'open-modal': stratifiedModelType ? openMatrixModal : openValueModal }"
					/>
				</table>
			</div>
		</div>
		<SplitButton
			outlined
			label="Add configuration"
			size="small"
			icon="pi pi-plus"
			:model="addConfigurationItems"
		/>
		<!-- 
			For viewing transition matrices without clicking nodes
			<tera-transition-matrices
			v-if="stratifiedModelType"
			:model-configuration="modelConfigurations[0]"
		/> -->
		<Teleport to="body">
			<!--
			TODO: Not sure if these modals should be in the child config components or if they should
			be in their own since they are dealing with a different set of variables. Will deal with
			this in another PR.
			-->

			<!-- <tera-modal
				v-if="openValueConfig && stratifiedModelType && modalAttributes.id"
				@modal-mask-clicked="openValueConfig = false"
			>
				<template #header>
					<h4>{{ modalAttributes.id }}</h4>
					<span>Configure the matrix values</span>
					<div class="flex align-items-center">
						<Checkbox
							inputId="matrixShouldEval"
							v-model="matrixShouldEval"
							:binary="true"
							label="Evaluate expressions?"
						/>
						<label for="matrixShouldEval" class="ml-2"> Evaluate Expressions? </label>
					</div>
				</template>
				<template #default>
					<tera-stratified-matrix
						:model-configuration="modelConfigurations[modalAttributes.configIndex]"
						:id="modalAttributes.id"
						:stratified-model-type="stratifiedModelType"
						:stratified-matrix-type="modalAttributes.stratifiedMatrixType"
						:should-eval="matrixShouldEval"
						@update-configuration="(configToUpdate: ModelConfiguration) => updateConfiguration(configToUpdate)"
					/>
				</template>
				<template #footer>
					<Button label="OK" @click="openValueConfig = false" />
					<Button class="p-button-outlined" label="Cancel" @click="openValueConfig = false" />
				</template>
			</tera-modal> -->
			<tera-stratified-matrix-modal
				v-if="openValueConfig && stratifiedModelType && modalAttributes.id"
				:model-configuration="modelConfigurations[modalAttributes.configIndex]"
				:id="modalAttributes.id"
				:stratified-model-type="stratifiedModelType"
				:stratified-matrix-type="modalAttributes.stratifiedMatrixType"
				:open-value-config="openValueConfig"
				@close-modal="openValueConfig = false"
				@update-configuration="(configToUpdate: ModelConfiguration) => updateConfiguration(configToUpdate)"
			/>
			<tera-modal
				v-else-if="
					openValueConfig &&
					modalAttributes.stratifiedMatrixType &&
					isNumber(modalAttributes.odeObjIndex)
				"
				@modal-mask-clicked="openValueConfig = false"
				@modal-enter-press="setModelParameters"
			>
				<template #header>
					<h4>
						{{
							modelConfigurations[modalAttributes.configIndex].configuration.semantics.ode[
								modalAttributes.stratifiedMatrixType
							][modalAttributes.odeObjIndex]['id'] ??
							modelConfigurations[modalAttributes.configIndex].configuration.semantics.ode[
								modalAttributes.stratifiedMatrixType
							][modalAttributes.odeObjIndex]['target']
						}}
					</h4>
					<span>Select a value for this configuration</span>
				</template>
				<template #default>
					<TabView v-model:activeIndex="activeIndex">
						<TabPanel v-for="(extraction, i) in extractions" :key="i">
							<template #header>
								<span>{{ extraction.name }}</span>
							</template>
							<div>
								<label for="name">Name</label>
								<InputText class="p-inputtext-sm" :key="'name' + i" v-model="extraction.name" />
							</div>
							<div v-if="modalAttributes.stratifiedMatrixType === 'parameters'">
								<label for="type">Type</label>
								<Dropdown
									v-model="extraction.type"
									:options="typeOptions"
									optionLabel="label"
									optionValue="value"
									placeholder="Select a parameter type"
								/>
							</div>
							<div>
								<label for="name">Value</label>
								<InputText
									class="p-inputtext-sm"
									:class="{ 'p-invalid': errorMessage }"
									:key="'value' + i"
									v-model="extraction.value"
									:placeholder="getValuePlaceholder(extraction.type)"
									@keydown="clearError()"
								/>
								<small v-if="errorMessage" class="invalid-message">{{ errorMessage }}</small>
							</div>
							<div v-if="modalAttributes.stratifiedMatrixType === 'parameters'">
								<div v-if="extraction.type === ParamType.DISTRIBUTION">
									<label for="name">Min</label>
									<InputText
										class="p-inputtext-sm"
										:key="'min' + i"
										v-model="extraction.distribution.parameters.minimum"
									/>
									<label for="name">Max</label>
									<InputText
										class="p-inputtext-sm"
										:key="'max' + i"
										v-model="extraction.distribution.parameters.maximum"
									/>
								</div>
							</div>
							<label for="equation">Equation</label>
							<tera-math-editor
								:is-editing-eq="true"
								:latex-equation="''"
								:keep-open="true"
								@equation-updated="console.log('equation udpated from configuration')"
							/>
						</TabPanel>
					</TabView>
				</template>
				<template #footer>
					<Button label="OK" @click="setModelParameters" />
					<Button class="p-button-outlined" label="Cancel" @click="openValueConfig = false" />
				</template>
			</tera-modal>
		</Teleport>
	</main>
</template>

<script setup lang="ts">
import { watch, ref, computed } from 'vue';
import { isEmpty, cloneDeep, isNumber } from 'lodash';
import {
	StratifiedModel,
	getStratificationType
} from '@/model-representation/petrinet/petrinet-service';
import { ModelConfiguration, Model } from '@/types/Types';
import SplitButton from 'primevue/splitbutton';
import { getCatlabAMRPresentationData } from '@/model-representation/petrinet/catlab-petri';
import {
	getMiraAMRPresentationData,
	getUnstratifiedParameters
} from '@/model-representation/petrinet/mira-petri';
import { FeatureConfig, ParamType } from '@/types/common';
import { StratifiedMatrix } from '@/types/Model';
import TabView from 'primevue/tabview';
import TeraModal from '@/components/widgets/tera-modal.vue';
import TabPanel from 'primevue/tabpanel';
import Dropdown from 'primevue/dropdown';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
// import TeraTransitionMatrices from '@/temp/tera-transition-matrices.vue';
import TeraStratifiedMatrixModal from './model-configurations/tera-stratified-matrix-modal.vue';
import TeraRegularModelConfigurations from './model-configurations/tera-regular-model-configurations.vue';
import TeraStratifiedModelConfigurations from './model-configurations/tera-stratified-model-configurations.vue';
// import TabPanel from 'primevue/tabpanel';
// import TabView from 'primevue/tabview';

const props = defineProps<{
	model: Model;
	modelConfigurations: ModelConfiguration[];
	featureConfig: FeatureConfig;
	calibrationConfig?: boolean;
}>();

const emit = defineEmits(['update-model', 'update-configuration', 'add-configuration']);

const cellEditStates = ref<any[]>([]);
const extractions = ref<any[]>([]);
const openValueConfig = ref(false);
const editValue = ref<string>('');
const activeIndex = ref(0);
const errorMessage = ref('');

const addConfigurationItems = computed(() =>
	props.modelConfigurations.map((config) => ({
		label: config.name,
		command: () => {
			emit('add-configuration', config);
		}
	}))
);

const configurations = computed<Model[]>(
	() => props.modelConfigurations.map((m) => m.configuration) ?? []
);

const stratifiedModelType = computed(() => props.model && getStratificationType(props.model));
// Dependent on stratifiedModelType which is computed
const modalAttributes = ref(
	stratifiedModelType.value
		? { id: '', configIndex: 0, stratifiedMatrixType: StratifiedMatrix.Initials }
		: { stratifiedMatrixType: '', valueName: '', configIndex: 0, odeObjIndex: 0 }
);

const baseModel = computed<any>(() => {
	if (stratifiedModelType.value === StratifiedModel.Catlab) {
		return getCatlabAMRPresentationData(props.model).compactModel;
	}
	if (stratifiedModelType.value === StratifiedModel.Mira) {
		return getMiraAMRPresentationData(props.model).compactModel.model;
	}
	return props.model.model;
});
const headerInitials = computed<any[]>(() =>
	stratifiedModelType.value
		? baseModel.value.states.map(({ id }) => id)
		: configurations.value[0]?.semantics?.ode.initials?.map(({ target }) => target) ?? []
);
const headerParameters = computed<any[]>(() =>
	stratifiedModelType.value
		? // ? baseModel.value.transitions.map(({ id }) => id)
		  [...getUnstratifiedParameters(props.model).keys()]
		: configurations.value[0]?.semantics?.ode.parameters?.map(({ id }) => id) ?? []
);
const headerInitialsAndParameters = computed(() => [
	...headerInitials.value,
	...headerParameters.value
]);

// Determines names of headers and how many columns they'll span eg. initials, parameters, observables
const tableHeaders = computed<{ name: string; colspan: number }[]>(() => [
	{ name: 'initials', colspan: headerInitials.value.length },
	{ name: 'parameters', colspan: headerParameters.value.length }
]);
// Decide if we should display the whole configuration table
const isConfigurationVisible = computed(
	() =>
		!isEmpty(props.modelConfigurations) &&
		!isEmpty(tableHeaders.value) &&
		!isEmpty(cellEditStates.value)
);

function onEnterNameCell(configIndex: number) {
	editValue.value = cloneDeep(props.modelConfigurations[configIndex].name);
	cellEditStates.value[configIndex].name = true;
}

function onEnterValueCell(
	stratifiedMatrixType: string,
	valueName: string,
	configIndex: number,
	odeObjIndex: number
) {
	editValue.value = cloneDeep(
		props.modelConfigurations[configIndex].configuration.semantics.ode[stratifiedMatrixType][
			odeObjIndex
		][valueName]
	);
	cellEditStates.value[configIndex][stratifiedMatrixType][odeObjIndex] = true;
}

// Modal
function getValuePlaceholder(parameterType) {
	if (parameterType === ParamType.TIME_SERIES) {
		return 'Enter values here as a list of time:value pairs (e.g., 0:500, 10:550, 25:700 etc)';
	}
	return '';
}

const typeOptions = ref([
	{ label: 'A constant', value: ParamType.CONSTANT },
	{ label: 'A distribution', value: ParamType.DISTRIBUTION },
	{ label: 'A value that changes over time', value: ParamType.TIME_SERIES }
]);

function clearError() {
	errorMessage.value = '';
}

function getParameterValue(parameter, valueName, timeseries) {
	if (parameter.id in timeseries) {
		return timeseries[parameter.id];
	}
	return parameter[valueName];
}
function getParameterType(parameter, timeseries) {
	if (parameter.id in timeseries) {
		return ParamType.TIME_SERIES;
	}
	if (parameter.distribution) {
		return ParamType.DISTRIBUTION;
	}
	return ParamType.CONSTANT;
}

function openMatrixModal(configIndex: number, id: string) {
	if (!props.featureConfig.isPreview) {
		activeIndex.value = 0;
		openValueConfig.value = true;
		const stratifiedMatrixType = headerInitials.value.includes(id)
			? StratifiedMatrix.Initials
			: StratifiedMatrix.Parameters;
		modalAttributes.value = { id, configIndex, stratifiedMatrixType };
	}
}

function openValueModal(
	stratifiedMatrixType: string,
	valueName: string,
	configIndex: number,
	odeObjIndex: number
) {
	if (!props.featureConfig.isPreview) {
		activeIndex.value = 0;
		openValueConfig.value = true;
		clearError();
		modalAttributes.value = { stratifiedMatrixType, valueName, configIndex, odeObjIndex };
		const modelParameter = cloneDeep(
			props.modelConfigurations[configIndex].configuration.semantics.ode[stratifiedMatrixType][
				odeObjIndex
			]
		);

		// sticking the timeseries values on the metadata, temporary solution for now
		const modelTimeSeries =
			cloneDeep(props.modelConfigurations[configIndex].configuration?.metadata?.timeseries) ?? {};

		extractions.value[0].value = getParameterValue(modelParameter, valueName, modelTimeSeries);
		extractions.value[0].name = modelParameter.name ?? 'Default';
		extractions.value[0].type = getParameterType(modelParameter, modelTimeSeries);
		// we are only adding the ability to add one type of distribution for now...
		extractions.value[0].distribution = modelParameter.distribution ?? {
			type: 'Uniform1',
			parameters: { minimum: null, maximum: null }
		};
	}
}

function validateTimeSeries(values) {
	let isValid = true;
	if (typeof values !== 'string') {
		isValid = false;
		errorMessage.value = 'Incorrect Format (e.g., 0:500, 10:550, 25:700 etc)';
		return isValid;
	}
	const timeValuePairs = values.split(',');

	timeValuePairs.forEach((pair) => {
		const [time, value] = pair.trim().split(/\s*:\s*/);
		if (!time || !value) {
			isValid = false;
		}
	});

	clearError();
	if (!isValid) {
		errorMessage.value = 'Incorrect Format (e.g., 0:500, 10:550, 25:700 etc)';
	}
	return isValid;
}

// to validate input
function checkModelParameters() {
	const { type, value } = extractions.value[activeIndex.value];

	if (type === ParamType.TIME_SERIES) {
		return validateTimeSeries(value);
	}
	clearError();
	return true;
}

function updateConfiguration(configToUpdate: ModelConfiguration) {
	emit('update-configuration', configToUpdate);
}

function updateName(index: number) {
	const configToUpdate = cloneDeep(props.modelConfigurations[index]);
	cellEditStates.value[index].name = false;
	if (configToUpdate.name !== editValue.value && !isEmpty(editValue.value)) {
		configToUpdate.name = editValue.value;
		updateConfiguration(configToUpdate);
	}
}

function updateValue(
	stratifiedMatrixType: string,
	valueName: string,
	index: number,
	odeObjIndex: number
) {
	const configToUpdate = cloneDeep(props.modelConfigurations[index]);
	cellEditStates.value[index][stratifiedMatrixType][odeObjIndex] = false;
	if (
		configToUpdate.configuration.semantics.ode[stratifiedMatrixType][odeObjIndex][
			valueName
		].toString() !== editValue.value &&
		!isEmpty(editValue.value)
	) {
		configToUpdate.configuration.semantics.ode[stratifiedMatrixType][odeObjIndex][valueName] =
			editValue.value;
		updateConfiguration(configToUpdate);
	}
}

// function to set the provided values from the modal
function setModelParameters() {
	if (
		checkModelParameters() &&
		modalAttributes.value.stratifiedMatrixType &&
		modalAttributes.value.valueName &&
		isNumber(modalAttributes.value.odeObjIndex)
	) {
		const { stratifiedMatrixType, valueName, configIndex, odeObjIndex } = modalAttributes.value;
		const configToUpdate = cloneDeep(props.modelConfigurations[configIndex]);

		const modelParameter =
			configToUpdate.configuration.semantics.ode[stratifiedMatrixType][odeObjIndex];
		modelParameter[valueName] = extractions.value[activeIndex.value].value;

		if (!configToUpdate.configuration.metadata) {
			configToUpdate.configuration.metadata = {};
		}
		const modelMetadata = configToUpdate.configuration.metadata;

		modelParameter.name = extractions.value[activeIndex.value].name;

		if (extractions.value[activeIndex.value].type === ParamType.TIME_SERIES) {
			if (!modelMetadata.timeseries) modelMetadata.timeseries = {};
			if (!modelMetadata.timeseries[modelParameter.id])
				modelMetadata.timeseries[modelParameter.id] = {};
			modelMetadata.timeseries[modelParameter.id] = extractions.value[activeIndex.value].value;
			delete modelParameter.distribution;
		} else if (extractions.value[activeIndex.value].type === ParamType.DISTRIBUTION) {
			modelParameter.distribution = extractions.value[activeIndex.value].distribution;
			delete modelMetadata.timeseries?.[modelParameter.id];
		} else {
			// A constant
			delete modelParameter.distribution;
			delete modelMetadata.timeseries?.[modelParameter.id];
		}
		emit('update-configuration', configToUpdate);
		openValueConfig.value = false;
	}
}

function resetCellEditing() {
	const row = { name: false };

	for (let i = 0; i < tableHeaders.value.length; i++) {
		const { name, colspan } = tableHeaders.value[i];
		row[name] = Array(colspan).fill(false);
	}

	// Can't use fill here because the same row object would be referenced throughout the array
	const cellEditStatesArr = new Array(props.modelConfigurations.length);
	for (let i = 0; i < props.modelConfigurations.length; i++) cellEditStatesArr[i] = cloneDeep(row);
	cellEditStates.value = cellEditStatesArr;
}

async function setupConfigurations() {
	resetCellEditing();

	openValueConfig.value = false;
	modalAttributes.value = stratifiedModelType.value
		? { id: '', configIndex: 0, stratifiedMatrixType: StratifiedMatrix.Initials }
		: { stratifiedMatrixType: '', valueName: '', configIndex: 0, odeObjIndex: 0 };
	extractions.value = [{ name: '', value: '' }];
}

watch(
	() => tableHeaders.value,
	() => {
		resetCellEditing();
	}
);

watch(
	() => [props.model.id],
	() => {
		setupConfigurations();
	},
	{ immediate: true, deep: true }
);
</script>

<style scoped>
.p-splitbutton {
	margin-top: 1rem;
}

.invalid-message {
	color: var(--text-color-danger);
	font-size: var(--font-caption);
}

thead > tr:first-child {
	text-transform: capitalize;
	font-size: var(--font-body-medium);
}

.model-configuration:deep(.p-column-header-content) {
	color: var(--text-color-subdued);
}

.p-datatable-thead th {
	/** TODO: Apply to all tables in theme or create second table rules?  */
	font-size: var(--font-size-small) !important;
	padding-left: 1rem !important;
}

.model-configuration:deep(.p-datatable-tbody > tr > td:empty:before) {
	content: '--';
}

.p-datatable:deep(td) {
	cursor: pointer;
}

.p-datatable:deep(td:focus) {
	background-color: var(--primary-color-lighter);
}

/**Applies tbodys */
.model-configuration:deep(.cell-modal-button) {
	visibility: hidden;
}
.model-configuration:deep(th:hover > .editable-cell > .cell-modal-button),
.model-configuration:deep(td:hover > .editable-cell > .cell-modal-button) {
	visibility: visible;
}

.model-configuration:deep(.p-frozen-column) {
	left: 0px;
	white-space: nowrap;
}

.model-configuration:deep(.second-frozen) {
	left: 48px;
}

.model-configuration:deep(.cell-input) {
	height: 4rem;
	width: 100%;
}

.model-configuration:deep(td:has(.cell-input)) {
	padding: 2px !important;
	max-width: 4rem;
}

/**Modal */

.p-tabview {
	display: flex;
	gap: 1rem;
	justify-content: space-between;
}

.p-tabview:deep(> *) {
	width: 50vw;
	height: 65vh;
	overflow: auto;
}

.p-tabview:deep(.p-tabview-nav) {
	flex-direction: column;
}

.p-tabview:deep(label) {
	display: block;
	font-size: var(--font-caption);
	margin-bottom: 0.25rem;
	width: 20%;
}

.p-tabview:deep(.p-tabview-nav-container, .p-tabview-nav-content) {
	width: 20%;
}

.p-tabview:deep(.p-tabview-panels) {
	border-radius: var(--border-radius);
	border: 1px solid var(--surface-border-light);
	background-color: var(--surface-ground);
	width: 100%;
	height: 100%;
}

.p-tabview:deep(.p-tabview-panel) {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.p-tabview:deep(.p-tabview-nav li) {
	border-left: 3px solid transparent;
}

.p-tabview:deep(.p-tabview-nav .p-tabview-header:nth-last-child(n + 3)) {
	border-bottom: 1px solid var(--surface-border-light);
}

.p-tabview:deep(.p-tabview-nav li.p-highlight) {
	border-left: 3px solid var(--primary-color);
	background: var(--surface-highlight);
}

.p-tabview:deep(.p-tabview-nav li.p-highlight .p-tabview-nav-link) {
	background: none;
}

.p-tabview:deep(.p-inputtext) {
	width: 100%;
}

.p-tabview:deep(.p-tabview-nav .p-tabview-ink-bar) {
	display: none;
}

.modal-input-container {
	display: flex;
	flex-direction: column;
	flex-grow: 1;
}

.modal-input {
	height: 25px;
	padding-left: 5px;
	margin: 5px;
	align-items: baseline;
}

.modal-input-label {
	margin-left: 5px;
	padding-top: 5px;
	padding-bottom: 5px;
	align-items: baseline;
}
</style>
