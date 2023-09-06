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
							<th v-for="(variableName, i) in [...headerStates, ...headerTransitions]" :key="i">
								{{ variableName }}
							</th>
						</tr>
					</thead>
					<!-- <div v-if="stratifiedModelType">Stratified configs (WIP)</div> -->
					<tera-stratified-model-configuration
						v-if="stratifiedModelType"
						:stratified-model-type="stratifiedModelType"
						:model-configurations="modelConfigurations"
						:cell-edit-states="cellEditStates"
						:feature-config="featureConfig"
						:table-headers="tableHeaders"
						:base-states="headerStates"
						:base-transitions="headerTransitions"
						@new-model-configuration="emit('new-model-configuration')"
						@update-value="updateValue"
						@update-name="updateName"
						@enter-value-cell="onEnterValueCell"
					/>
					<!-- <div v-if="stratifiedModelType"><br />All values</div> -->
					<tera-regular-model-configuration
						:model-configurations="modelConfigurations"
						:feature-config="featureConfig"
						:cell-edit-states="cellEditStates"
						:table-headers="tableHeaders"
						@new-model-configuration="emit('new-model-configuration')"
						@update-value="updateValue"
						@update-name="updateName"
						@enter-value-cell="onEnterValueCell"
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
import { FeatureConfig } from '@/types/common';
import TeraStratifiedModelConfiguration from './model-configurations/tera-stratified-model-configuration.vue';
import TeraRegularModelConfiguration from './model-configurations/tera-regular-model-configuration.vue';

const props = defineProps<{
	featureConfig: FeatureConfig;
	model: Model;
	calibrationConfig?: boolean;
}>();

const emit = defineEmits(['new-model-configuration', 'update-model-configuration']);

// const modelConfigInputValue = ref<string>('');
const modelConfigurations = ref<ModelConfiguration[]>([]);
const cellEditStates = ref<any[]>([]);
const extractions = ref<any[]>([]);
const openValueConfig = ref(false);
const modelConfigInputValue = ref<string>('');

// const activeIndex = ref(0);
const configItems = ref<any[]>([]);

const configurations = computed<Model[]>(
	() => modelConfigurations.value.map((m) => m.configuration) ?? []
);

const stratifiedModelType = computed(() => props.model && getStratificationType(props.model));
// Dependent on stratifiedModelType which is computed
const modalVal = ref(
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

function onEnterValueCell(
	odeType: string,
	valueName: string,
	configIndex: number,
	odeObjIndex: number
) {
	modelConfigInputValue.value = cloneDeep(
		modelConfigurations.value[configIndex].configuration.semantics.ode[odeType][odeObjIndex][
			valueName
		]
	);
	cellEditStates.value[configIndex][odeType][odeObjIndex] = true;
}

function updateModelConfig(configIndex: number = modalVal.value.configIndex) {
	const configToUpdate = modelConfigurations.value[configIndex];
	updateModelConfiguration(configToUpdate);
	openValueConfig.value = false;
	setTimeout(() => {
		emit('update-model-configuration');
	}, 800);
}

function updateValue(odeType: string, valueName: string, configIndex: number, odeObjIndex: number) {
	cellEditStates.value[configIndex][odeType][odeObjIndex] = false;
	modelConfigurations.value[configIndex].configuration.semantics.ode[odeType][odeObjIndex][
		valueName
	] = modelConfigInputValue.value;
	updateModelConfig(configIndex);
}

function updateName(configIndex: number) {
	cellEditStates.value[configIndex].name = false;
	modelConfigurations.value[configIndex].name = modelConfigInputValue.value;
	updateModelConfig(configIndex);
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
	console.log(modelConfigurations.value);
	resetCellEditing();

	openValueConfig.value = false;
	modalVal.value = stratifiedModelType.value
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
</style>
