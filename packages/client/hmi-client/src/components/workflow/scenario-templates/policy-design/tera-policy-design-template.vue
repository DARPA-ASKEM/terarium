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
			<label>Select Success Criteria: </label>
			<tera-optimize-criterion-group-form
				v-for="(cfg, index) in props.scenario.getOptimizeState().constraintGroups"
				:key="index"
				:index="index"
				:criterion="cfg"
				:model-state-and-obs-options="modelStateAndObsOptions"
				@update-self="(config) => props.scenario.setOptimizeCriteriaForm(index, config)"
				@delete-self="() => props.scenario.deleteOptimizeCriterionGroupForm(index)"
			/>
			<Button
				icon="pi pi-plus"
				class="p-button-sm p-button-text w-max"
				label="Add new criterion"
				@click="props.scenario.addOptimizeCriterionGroupForm()"
			/>
			<Label>Intervention policy </Label>
			<section v-for="(cfg, idx) in interventionPolicyGroups" :key="idx">
				<tera-static-intervention-policy-group
					v-if="isInterventionStatic(cfg.individualIntervention) && modelConfiguration && model"
					:model="model"
					:model-configuration="modelConfiguration"
					:key="cfg.id || '' + idx"
					:config="cfg as StaticInterventionPolicyGroupForm"
					@update-self="(config) => props.scenario.updateInterventionPolicyGroupForm(idx, config)"
				/>
				<tera-dynamic-intervention-policy-group
					v-if="!isInterventionStatic(cfg.individualIntervention)"
					:key="idx"
					:config="cfg as DynamicInterventionPolicyGroupForm"
					@update-self="(config) => props.scenario.updateInterventionPolicyGroupForm(idx, config)"
				/>
			</section>
			<section v-if="interventionPolicyGroups.length === 0">
				<p class="mt-1">No intervention policies have been added.</p>
			</section>
		</template>
	</tera-scenario-template>
</template>

<script setup lang="ts">
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import { computed, ref, watch } from 'vue';
import { useProjects } from '@/composables/project';
import { AssetType, ModelConfiguration, InterventionPolicy, Model } from '@/types/Types';
import { getInterventionPoliciesForModel, getModel, getModelConfigurationsForModel } from '@/services/model';
import _ from 'lodash';
import teraOptimizeCriterionGroupForm from '@/components/workflow/ops/optimize-ciemss/tera-optimize-criterion-group-form.vue';
import teraStaticInterventionPolicyGroup, {
	StaticInterventionPolicyGroupForm
} from '@/components/workflow/ops/optimize-ciemss/tera-static-intervention-policy-group.vue';
import teraDynamicInterventionPolicyGroup, {
	DynamicInterventionPolicyGroupForm
} from '@/components/workflow/ops/optimize-ciemss/tera-dynamic-intervention-policy-group.vue';
import { isInterventionStatic } from '@/services/intervention-policy';
import { getModelConfigurationById } from '@/services/model-configurations';
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

const allModelOptions = computed(() => useProjects().getActiveProjectAssets(AssetType.Model));
const allDatasetOptions = computed(() => useProjects().getActiveProjectAssets(AssetType.Dataset));
const selectedModelId = computed(() => props.scenario.getModelId());
const selectedModelConfigurationId = computed(() => props.scenario.getModelConfigId());
const interventionPolicyGroups = computed(() => props.scenario.getOptimizeState().interventionPolicyGroups);
const isModelSelected = computed(() => !_.isEmpty(selectedModelId.value));
const model = ref<Model | null>(null);
const modelConfiguration = ref<ModelConfiguration>();
const allModelConfigOptions = ref<ModelConfiguration[]>([]);
const allInterventionPolicyOptions = ref<InterventionPolicy[]>([]);
const isFetchingModelData = ref<boolean>(false);
const modelStateAndObsOptions = ref<{ label: string; value: string }[]>([]);

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
				modelStateAndObsOptions.value = modelStates.map((state: any) => ({
					label: state.id,
					value: `${state.id}_state`
				}));
				// Obs:
				const modelObs = model.value.semantics?.ode.observables;
				if (modelObs) {
					modelStateAndObsOptions.value.push(
						...modelObs.map((observable: any) => ({
							label: observable.id,
							value: `${observable.id}_observable`
						}))
					);
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
