<template>
	<tera-scenario-template :header="header" :scenario-instance="scenario" @save-workflow="emit('save-workflow')">
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
				:disabled="!isModelSelected"
				:model-value="props.scenario.getModelConfigId()"
				:options="allModelConfigOptions"
				option-label="name"
				option-value="id"
				placeholder="Select a model configuration"
				@update:model-value="props.scenario.setModelConfigId($event)"
				class="mb-3"
				:loading="isFetchingModelData"
			/>

			<label>Select an intervention</label>
			<Dropdown
				:disabled="!isModelSelected"
				:model-value="props.scenario.getInterventionPolicyId()"
				:options="allInterventionPolicyOptions"
				option-label="name"
				option-value="id"
				placeholder="Select an intervention for this model"
				@update:model-value="props.scenario.setInterventionPolicyId($event, modelConfiguration)"
				class="mb-3"
				:loading="isFetchingModelData"
			/>

			<label>Select a dataset (optional)</label>
			<Dropdown
				:model-value="props.scenario.getDatasetId()"
				:options="allDatasetOptions"
				option-label="assetName"
				option-value="assetId"
				placeholder="Select a dataset (optional)"
				@update:model-value="props.scenario.setDatasetId($event)"
				class="mb-3"
			/>
		</template>
		<template #outputs>
			<label :class="{ 'disabled-label': _.isEmpty(modelStateAndObsOptions) || isFetchingModelData }"
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
			<img :src="horizon" alt="Horizon scanning chart" class="" />
		</template>
	</tera-scenario-template>
</template>

<script setup lang="ts">
import Dropdown from 'primevue/dropdown';
import { computed, ref, watch } from 'vue';
import { useProjects } from '@/composables/project';
import { AssetType, ModelConfiguration, InterventionPolicy, Model } from '@/types/Types';
import { getInterventionPoliciesForModel, getModel, getModelConfigurationsForModel } from '@/services/model';
import _ from 'lodash';
import { getModelConfigurationById } from '@/services/model-configurations';
import horizon from '@/assets/svg/template-images/horizon-thumbnail.svg';
import MultiSelect from 'primevue/multiselect';
import TeraScenarioTemplate from '../tera-scenario-template.vue';
import { ScenarioHeader } from '../base-scenario';
import { PolicyDesignScenario } from './policy-design-scenario';

const props = defineProps<{
	scenario: PolicyDesignScenario;
}>();

const header: ScenarioHeader = Object.freeze({
	title: 'Policy design template',
	question: 'Search for a policy/intervention that achieves a desired outcome',
	description:
		'Requires a desired future outcome state and a variable to intervene on. The system will attempt to optimize the variable to achieve the desired outcome state and provide an intervention for that variable (if possible). If there is data to calibrate against, the suggested intervention will be more accurate. This is a type of counterfactual analysis.',
	examples: [
		'What percentage vaccination is required to have hospitalizations below 5% of the population in three months?',
		'How much would gas taxes need to raise to reduce driving by 5% by October?',
		'How much would property taxes need to lower to increase investment by %5 in one year?'
	]
});

const allModelOptions = computed<ProjectAsset[]>(() => useProjects().getActiveProjectAssets(AssetType.Model));
const allDatasetOptions = computed(() => useProjects().getActiveProjectAssets(AssetType.Dataset));
const selectedModelId = computed(() => props.scenario.getModelId());
const selectedModelConfigurationId = computed(() => props.scenario.getModelConfigId());
const isModelSelected = computed(() => !_.isEmpty(selectedModelId.value));
const model = ref<Model | null>(null);
const modelConfiguration = ref<ModelConfiguration>();
const allModelConfigOptions = ref<ModelConfiguration[]>([]);
const allInterventionPolicyOptions = ref<InterventionPolicy[]>([]);
const isFetchingModelData = ref<boolean>(false);
const modelStateAndObsOptions = ref<string[]>([]);
const selectedOutputSettings = computed(() => props.scenario.getOptimizeOutputSettings());

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

const emit = defineEmits(['save-workflow']);
</script>
