<template>
	<main v-if="isConfigurationVisible">
		<div v-if="stratifiedModelType">Stratified configs (WIP)</div>
		<tera-stratified-model-configuration
			v-if="stratifiedModelType"
			:stratified-model-type="stratifiedModelType"
			:model="model"
			:cell-edit-states="cellEditStates"
			:feature-config="featureConfig"
			:table-headers="tableHeaders"
			:base-states="baseModelStates"
			:base-transitions="baseModelTransitions"
			@new-model-configuration="emit('new-model-configuration')"
		/>
		<div v-if="stratifiedModelType"><br />All values</div>
		<tera-regular-model-configuration
			:model="model"
			:feature-config="featureConfig"
			:cell-edit-states="cellEditStates"
			:table-headers="tableHeaders"
			:configurations="configurations"
			@new-model-configuration="emit('new-model-configuration')"
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
	// updateModelConfiguration,
	addDefaultConfiguration
} from '@/services/model-configurations';
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
// const modalVal = ref({ id: '', configIndex: 0, nodeType: NodeType.State });
// const modelConfigInputValue = ref<string>('');
// const modelConfigurations = ref<ModelConfiguration[]>([]);
// const cellEditStates = ref<any[]>([]);
// const extractions = ref<any[]>([]);
// const openValueConfig = ref(false);

// const activeIndex = ref(0);
const configItems = ref<any[]>([]);

const configurations = computed<Model[]>(
	() => modelConfigurations.value?.map((m) => m.configuration) ?? []
);

const stratifiedModelType = computed(() => props.model && getStratificationType(props.model));
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
const baseModelStates = computed<any>(() => baseModel.value.states.map(({ id }) => id));
const baseModelTransitions = computed<any>(() => baseModel.value.transitions.map(({ id }) => id));

// Determines names of headers and how many columns they'll span eg. initials, parameters, observables
const tableHeaders = computed<{ name: string; colspan: number }[]>(() => {
	if (stratifiedModelType.value) {
		return [
			{ name: 'Initials', colspan: baseModelStates.value.length },
			{ name: 'Parameters', colspan: baseModelTransitions.value.length }
		];
	}
	if (configurations.value?.[0]?.semantics) {
		return ['initials', 'parameters'].map((name) => {
			const colspan = configurations.value?.[0]?.semantics?.ode?.[name].length ?? 0;
			return { name, colspan };
		});
	}
	return [];
});
// Decide if we should display the whole configuration table
const isConfigurationVisible = computed(
	() => !isEmpty(configurations) && !isEmpty(tableHeaders) && !isEmpty(cellEditStates)
);

// const emit = defineEmits(['new-model-configuration', 'update-model-configuration']);

async function addModelConfiguration(config: ModelConfiguration) {
	await createModelConfiguration(
		props.model.id,
		`Copy of ${config.name}`,
		config.description as string,
		config.configuration
	);
	setTimeout(() => {
		emit('new-model-configuration');
		initializeConfigSpace();
	}, 800);
}

function resetCellEditing() {
	const row = { name: false };

	if (stratifiedModelType.value) {
		for (let i = 0; i < tableHeaders.value.length; i++) {
			const { name, colspan } = tableHeaders.value[i];
			row[name] = Array(colspan).fill(false);
		}
	}

	// Can't use fill here because the same row object would be referenced throughout the array
	const cellEditStatesArr = new Array(modelConfigurations.value.length);
	for (let i = 0; i < modelConfigurations.value.length; i++) cellEditStatesArr[i] = cloneDeep(row);
	cellEditStates.value = cellEditStatesArr;
}

async function initializeConfigSpace() {
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
	() => initializeConfigSpace()
);

onMounted(() => {
	initializeConfigSpace();
});
</script>

<style scoped></style>
