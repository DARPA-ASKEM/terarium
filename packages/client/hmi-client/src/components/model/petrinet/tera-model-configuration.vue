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
								<span class="capitalize">{{ name }}</span>
							</th>
						</tr>
						<!-- Table header 2nd row: Actual column headers -->
						<tr>
							<th class="p-frozen-column" />
							<th class="p-frozen-column second-frozen">Select all</th>
							<th v-for="(variableName, i) in headerStatesAndTransitions" :key="i">
								{{ variableName }}
							</th>
						</tr>
					</thead>
					<!--Different tbody depending on model-->
					<component
						:is="
							stratifiedModelType ? TeraStratifiedModelConfiguration : TeraRegularModelConfiguration
						"
						v-model:editValue="editValue"
						:model-configurations="modelConfigurations"
						:cell-edit-states="cellEditStates"
						:base-states-and-transitions="headerStatesAndTransitions"
						@new-model-configuration="emit('new-model-configuration')"
						@update-value="updateValue"
						@update-name="updateName"
						@enter-name-cell="onEnterNameCell"
						@enter-value-cell="onEnterValueCell"
						@open-matrix-modal="openMatrixModal"
						@open-value-modal="openValueModal"
					/>
				</table>
			</div>
		</div>
		<SplitButton
			outlined
			label="Add configuration"
			size="small"
			icon="pi pi-plus"
			:model="configItems"
		/>
		<Teleport to="body">
			<tera-modal
				v-if="openValueConfig && stratifiedModelType && modalAttributes.id"
				@modal-mask-clicked="openValueConfig = false"
				@model-submit="openValueConfig = false"
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
					<!-- TODO: Implement value tabs for the modal once we are ready
					<TabView v-model:activeIndex="activeIndex">
					<TabPanel v-for="(extraction, i) in extractions" :key="i">
						<template #header>
							<span>{{ extraction.name }}</span>
						</template>
						<div>
							<label for="name">Name</label>
							<InputText class="p-inputtext-sm" :key="'name' + i" v-model="extraction.name" />
						</div>
						<div>
							<label for="name">Matrix</label>
							<tera-stratified-value-matrix
								:model-configuration="modelConfigurations[modalAttributes.configIndex]"
								:id="modalAttributes.id"
								:stratified-model-type="stratifiedModelType"
								:node-type="modalAttributes.nodeType"
							/>
						</div>
					</TabPanel>
				</TabView> -->
					<tera-stratified-value-matrix
						:model-configuration="modelConfigurations[modalAttributes.configIndex]"
						:id="modalAttributes.id"
						:stratified-model-type="stratifiedModelType"
						:node-type="modalAttributes.nodeType"
						:should-eval="matrixShouldEval"
					/>
				</template>
				<template #footer>
					<Button label="OK" @click="openValueConfig = false" />
					<Button class="p-button-outlined" label="Cancel" @click="openValueConfig = false" />
				</template>
			</tera-modal>
			<tera-modal
				v-else-if="openValueConfig && modalAttributes.odeType"
				@modal-mask-clicked="openValueConfig = false"
				@modal-enter-press="setModelParameters"
			>
				<template #header>
					<h4>
						{{
							modelConfigurations[modalAttributes.configIndex].configuration.semantics.ode[
								modalAttributes.odeType
							][modalAttributes.odeObjIndex]['id'] ??
							modelConfigurations[modalAttributes.configIndex].configuration.semantics.ode[
								modalAttributes.odeType
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
							<div v-if="modalAttributes.odeType === 'parameters'">
								<label for="type">Type</label>
								<Dropdown
									v-model="extraction.type"
									:options="typeOptions"
									optionLabel="label"
									optionValue="value"
									placeholder="Select a parameter type"
								></Dropdown>
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
							<div v-if="modalAttributes.odeType === 'parameters'">
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
							>
							</tera-math-editor>
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
import { watch, ref, onMounted, computed } from 'vue';
import { isEmpty, cloneDeep } from 'lodash';
import {
	StratifiedModelType,
	getStratificationType
} from '@/model-representation/petrinet/petrinet-service';
import { ModelConfiguration, Model } from '@/types/Types';
import {
	createModelConfiguration,
	updateModelConfiguration,
	addDefaultConfiguration
} from '@/services/model-configurations';
import SplitButton from 'primevue/splitbutton';
import { NodeType } from '@/model-representation/petrinet/petrinet-renderer';
import { getCatlabAMRPresentationData } from '@/model-representation/petrinet/catlab-petri';
import { getMiraAMRPresentationData } from '@/model-representation/petrinet/mira-petri';
import { getModelConfigurations } from '@/services/model';
import { FeatureConfig, ParamType } from '@/types/common';

import TabView from 'primevue/tabview';
import TeraModal from '@/components/widgets/tera-modal.vue';
import TabPanel from 'primevue/tabpanel';
import Dropdown from 'primevue/dropdown';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import Button from 'primevue/button';
// st
import Checkbox from 'primevue/checkbox';
import TeraStratifiedValueMatrix from '@/components/models/tera-stratified-value-matrix.vue';
import InputText from 'primevue/inputtext';
import TeraRegularModelConfiguration from './model-configurations/tera-regular-model-configuration.vue';
import TeraStratifiedModelConfiguration from './model-configurations/tera-stratified-model-configuration.vue';
// import TabPanel from 'primevue/tabpanel';
// import TabView from 'primevue/tabview';

const props = defineProps<{
	featureConfig: FeatureConfig;
	model: Model;
	calibrationConfig?: boolean;
}>();

const emit = defineEmits(['new-model-configuration', 'update-model-configuration']);

const modelConfigurations = ref<ModelConfiguration[]>([]);
const cellEditStates = ref<any[]>([]);
const extractions = ref<any[]>([]);
const openValueConfig = ref(false);
const editValue = ref<string>('');
const activeIndex = ref(0);
const errorMessage = ref('');
const configItems = ref<any[]>([]);

const matrixShouldEval = ref(true);

const configurations = computed<Model[]>(
	() => modelConfigurations.value.map((m) => m.configuration) ?? []
);

const stratifiedModelType = computed(() => props.model && getStratificationType(props.model));
// Dependent on stratifiedModelType which is computed
const modalAttributes = ref(
	stratifiedModelType.value
		? { id: '', configIndex: 0, nodeType: NodeType.State }
		: { odeType: '', valueName: '', configIndex: 0, odeObjIndex: 0 }
);

const baseModel = computed<any>(() => {
	if (stratifiedModelType.value === StratifiedModelType.Catlab) {
		return getCatlabAMRPresentationData(props.model).compactModel;
	}
	if (stratifiedModelType.value === StratifiedModelType.Mira) {
		return getMiraAMRPresentationData(props.model).compactModel.model;
	}
	return props.model.model;
});
const headerStates = computed<any[]>(() =>
	stratifiedModelType.value
		? baseModel.value.states.map(({ id }) => id)
		: configurations.value[0]?.semantics?.ode.initials?.map(({ target }) => target) ?? []
);
const headerTransitions = computed<any[]>(() =>
	stratifiedModelType.value
		? baseModel.value.transitions.map(({ id }) => id)
		: configurations.value[0]?.semantics?.ode.parameters?.map(({ id }) => id) ?? []
);
const headerStatesAndTransitions = computed(() => [
	...headerStates.value,
	...headerTransitions.value
]);

// Determines names of headers and how many columns they'll span eg. initials, parameters, observables
const tableHeaders = computed<{ name: string; colspan: number }[]>(() => [
	{ name: 'initials', colspan: headerStates.value.length },
	{ name: 'parameters', colspan: headerTransitions.value.length }
]);
// Decide if we should display the whole configuration table
const isConfigurationVisible = computed(
	() =>
		!isEmpty(modelConfigurations.value) &&
		!isEmpty(tableHeaders.value) &&
		!isEmpty(cellEditStates.value)
);

function onEnterNameCell(configIndex: number) {
	editValue.value = cloneDeep(modelConfigurations.value[configIndex].name);
	cellEditStates.value[configIndex].name = true;
}

function onEnterValueCell(
	odeType: string,
	valueName: string,
	configIndex: number,
	odeObjIndex: number
) {
	editValue.value = cloneDeep(
		modelConfigurations.value[configIndex].configuration.semantics.ode[odeType][odeObjIndex][
			valueName
		]
	);
	cellEditStates.value[configIndex][odeType][odeObjIndex] = true;
}

function updateModelConfig(configIndex: number = modalAttributes.value.configIndex) {
	const configToUpdate = modelConfigurations.value[configIndex];
	updateModelConfiguration(configToUpdate);
	openValueConfig.value = false;
	setTimeout(() => {
		emit('update-model-configuration');
	}, 800);
}

function updateName(configIndex: number) {
	cellEditStates.value[configIndex].name = false;
	modelConfigurations.value[configIndex].name = editValue.value;
	updateModelConfig(configIndex);
}

function updateValue(odeType: string, valueName: string, configIndex: number, odeObjIndex: number) {
	cellEditStates.value[configIndex][odeType][odeObjIndex] = false;
	modelConfigurations.value[configIndex].configuration.semantics.ode[odeType][odeObjIndex][
		valueName
	] = editValue.value;
	updateModelConfig(configIndex);
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
	{ label: 'A distibution', value: ParamType.DISTRIBUTION },
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
		const nodeType = headerStates.value.includes(id) ? NodeType.State : NodeType.Transition;
		modalAttributes.value = { id, configIndex, nodeType };
	}
}

function openValueModal(
	odeType: string,
	valueName: string,
	configIndex: number,
	odeObjIndex: number
) {
	if (!props.featureConfig.isPreview) {
		activeIndex.value = 0;
		openValueConfig.value = true;
		clearError();
		modalAttributes.value = { odeType, valueName, configIndex, odeObjIndex };
		const modelParameter = cloneDeep(
			modelConfigurations.value[configIndex].configuration.semantics.ode[odeType][odeObjIndex]
		);

		// sticking the timeseries values on the metadata, temporary solution for now
		const modelTimeSeries =
			cloneDeep(modelConfigurations.value[configIndex].configuration?.metadata?.timeseries) ?? {};

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

// function to set the provided values from the modal
function setModelParameters() {
	if (checkModelParameters() && modalAttributes.value.odeType) {
		const { odeType, valueName, configIndex, odeObjIndex } = modalAttributes.value;
		const modelParameter =
			modelConfigurations.value[configIndex].configuration.semantics.ode[odeType][odeObjIndex];
		modelParameter[valueName] = extractions.value[activeIndex.value].value;
		if (!modelConfigurations.value[configIndex].configuration.metadata) {
			modelConfigurations.value[configIndex].configuration.metadata = {};
		}
		const modelMetadata = modelConfigurations.value[configIndex].configuration.metadata;
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
		updateModelConfig();
	}
}

async function addModelConfiguration(config: ModelConfiguration) {
	await createModelConfiguration(
		props.model.id,
		`Copy of ${config.name}`,
		config.description as string,
		config.configuration
	);
	setTimeout(() => {
		emit('new-model-configuration');
		setupConfigurations();
	}, 800);
}

function resetCellEditing() {
	const row = { name: false };

	for (let i = 0; i < tableHeaders.value.length; i++) {
		const { name, colspan } = tableHeaders.value[i];
		row[name] = Array(colspan).fill(false);
	}

	// Can't use fill here because the same row object would be referenced throughout the array
	const cellEditStatesArr = new Array(modelConfigurations.value.length);
	for (let i = 0; i < modelConfigurations.value.length; i++) cellEditStatesArr[i] = cloneDeep(row);
	cellEditStates.value = cellEditStatesArr;
}

async function setupConfigurations() {
	let tempConfigurations = await getModelConfigurations(props.model.id);

	configItems.value = tempConfigurations.map((config) => ({
		label: config.name,
		command: () => {
			addModelConfiguration(config);
		}
	}));

	// Ensure that we always have a "default config" model configuration
	if (isEmpty(tempConfigurations) || !tempConfigurations.find((d) => d.name === 'Default config')) {
		await addDefaultConfiguration(props.model);
		tempConfigurations = await getModelConfigurations(props.model.id);
	}

	modelConfigurations.value = tempConfigurations;
	resetCellEditing();

	openValueConfig.value = false;
	modalAttributes.value = stratifiedModelType.value
		? { id: '', configIndex: 0, nodeType: NodeType.State }
		: { odeType: '', valueName: '', configIndex: 0, odeObjIndex: 0 };
	extractions.value = [{ name: '', value: '' }];
}

watch(
	() => tableHeaders.value,
	() => {
		resetCellEditing();
	}
);

watch(
	() => props.model.id,
	() => setupConfigurations()
);

onMounted(() => {
	setupConfigurations();
});
</script>

<style scoped>
.model-configuration {
	margin-bottom: 1rem;
}

.capitalize {
	font-size: var(--font-body-medium);
}

.model-configuration:deep(.p-column-header-content) {
	color: var(--text-color-subdued);
}

/** TODO: Apply to all tables in theme or create second table rules?  */
.p-datatable-thead th {
	text-transform: none !important;
	color: var(--text-color-primary) !important;
	font-size: var(--font-size-small) !important;
	padding-left: 1rem !important;
}

.model-configuration:deep(.p-datatable-tbody > tr > td:empty:before) {
	content: '--';
}

/**Modal */

.p-tabview {
	display: flex;
	gap: 1rem;
	margin-bottom: 1rem;
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
