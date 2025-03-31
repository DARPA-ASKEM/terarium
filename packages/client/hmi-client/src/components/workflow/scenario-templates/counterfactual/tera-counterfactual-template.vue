<template>
	<tera-scenario-template
		ref="blankTemplate"
		:header="header"
		:scenario-instance="scenario"
		@save-workflow="emit('save-workflow')"
	>
		<template #inputs>
			<label>Select a model</label>
			<Dropdown
				:model-value="props.scenario.getModelId()"
				:options="allModelOptions"
				option-label="assetName"
				option-value="assetId"
				placeholder="Select a model"
				@update:model-value="props.scenario.setModelId($event)"
				class="mb-3"
			/>

			<label>Select a model configuration</label>
			<Dropdown
				:model-value="props.scenario.getModelConfigId()"
				:loading="isFetchingModelData"
				:disabled="!isModelSelected"
				:options="allModelOptions"
				option-label="assetName"
				option-value="assetId"
				placeholder="Select a model"
				@update:model-value="props.scenario.setModelConfigId($event)"
				class="mb-3"
			/>

			<label>Select a dataset</label>
			<Dropdown
				:model-value="props.scenario.getDatasetId()"
				:disabled="!isModelSelected"
				:options="allDatasetOptions"
				option-label="assetName"
				option-value="assetId"
				placeholder="Select a dataset"
				@update:model-value="props.scenario.setDatasetId($event)"
				class="mb-3"
			/>

			<label>Select an intervention</label>
			<Dropdown
				:disabled="!isModelSelected"
				:model-value="props.scenario.getInterventionPolicyId()"
				:options="allInterventionPolicyOptions"
				option-label="name"
				option-value="id"
				placeholder="Select an intervention for this model"
				@update:model-value="props.scenario.setInterventionPolicyId($event)"
				class="mb-3"
				:loading="isFetchingModelData"
			/>
		</template>
		<template #outputs>
			<!-- <label :class="{ 'disabled-label': _.isEmpty(modelStateAndObsOptions) || isFetchingModelData }"
				>Select an output metric</label
			>
			<MultiSelect
				:disabled="_.isEmpty(modelStateAndObsOptions) || isFetchingModelData"
				:model-value="selectedOutputSettings"
				placeholder="Select output metrics"
				:options="modelStateAndObsOptions"
				@update:model-value="scenario.setOptimizeOutputSettings($event)"
				:loading="isFetchingModelData"
				filter
			/>
			<img :src="horizon" alt="Horizon scanning chart" class="" /> -->
		</template>
	</tera-scenario-template>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { AssetType, InterventionPolicy, Model, ModelConfiguration, ProjectAsset } from '@/types/Types';
import { useProjects } from '@/composables/project';
import Dropdown from 'primevue/dropdown';
import _ from 'lodash';
import { getModelConfigurationById } from '@/services/model-configurations';
import { getInterventionPoliciesForModel, getModel, getModelConfigurationsForModel } from '@/services/model';
import { ScenarioHeader } from '../base-scenario';
import { CounterfactualScenario } from './counterfactual-scenario';
import TeraScenarioTemplate from '../tera-scenario-template.vue';

const props = defineProps<{
	scenario: CounterfactualScenario;
}>();
const emit = defineEmits(['save-workflow']);

const header: ScenarioHeader = Object.freeze({
	title: 'Necessary or sufficient analysis template',
	question: 'How likely would we arrive in a different state, if specific choices had been made differently?',
	description:
		'Requires data to calibrate against, intervention points in the past and binary test about the end state. This is a type of counterfactual analysis.',
	examples: [
		'Would we have 10% fewer cases now if vaccinations had been 5% higher a month ago?',
		'Would the storm winds have been at least 10mph less if the ocean temperature were 1 degree lower?'
	]
});

const allModelOptions = computed<ProjectAsset[]>(() => useProjects().getActiveProjectAssets(AssetType.Model));
const allDatasetOptions = computed<ProjectAsset[]>(() => useProjects().getActiveProjectAssets(AssetType.Dataset));
const selectedModelId = computed<string>(() => props.scenario.getModelId());
const selectedModelConfigurationId = computed<string>(() => props.scenario.getModelConfigId());
const isModelSelected = computed<boolean>(() => !_.isEmpty(selectedModelId.value));
const model = ref<Model | null>(null);
const modelConfiguration = ref<ModelConfiguration>();
const allModelConfigOptions = ref<ModelConfiguration[]>([]);
const allInterventionPolicyOptions = ref<InterventionPolicy[]>([]);
const isFetchingModelData = ref<boolean>(false);
const modelStateAndObsOptions = ref<string[]>([]);

watch(
	() => selectedModelId,
	async () => {
		if (selectedModelId.value) {
			isFetchingModelData.value = true;
			model.value = await getModel(selectedModelId.value);
			allModelConfigOptions.value = await getModelConfigurationsForModel(selectedModelId.value);
			allInterventionPolicyOptions.value = await getInterventionPoliciesForModel(selectedModelId.value);

			// Set model state and obs:
			if (model.value) {
				// States:
				const modelStates = model.value.model.states;
				modelStates.forEach((state) => modelStateAndObsOptions.value.push(state.id));
				// Obs:
				const modelObs = model.value.semantics?.ode.observables;
				if (modelObs) {
					modelObs.forEach((obs) => modelStateAndObsOptions.value.push(obs.id));
				}
			}
		}

		isFetchingModelData.value = false;
	},
	{ deep: true }
);

watch(
	() => selectedModelConfigurationId,
	async () => {
		if (selectedModelConfigurationId.value) {
			modelConfiguration.value = await getModelConfigurationById(selectedModelConfigurationId.value);
		}
	},
	{ deep: true }
);
</script>
