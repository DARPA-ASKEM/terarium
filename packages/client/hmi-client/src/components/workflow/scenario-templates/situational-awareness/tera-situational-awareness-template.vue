<template>
	<div>
		<h6>What's likely to happen next?</h6>
		<p>
			Calibrates the model to historical data to obtain the best estimate of parameters for the present, then forecasts
			into the near future.
		</p>
	</div>
	<div>
		<h6>Examples</h6>
		<ul>
			<li>Some example</li>
		</ul>
	</div>

	<div>
		<label>What would you like to call this workflow?</label>
		<tera-input-text :model-value="scenario.workflowName" @update:model-value="scenario.setWorkflowName($event)" />
	</div>

	<div class="grid">
		<div class="col-6 flex flex-column gap-2">
			<h6>Inputs</h6>
			<label>Select a model</label>
			<Dropdown
				:model-value="scenario.modelSpec.id"
				:options="models"
				option-label="assetName"
				option-value="assetId"
				placeholder="Select a model"
				@update:model-value="scenario.setModelSpec($event)"
			/>

			<label>Select a dataset</label>
			<Dropdown
				:model-value="scenario.datasetSpec.id"
				:options="datasets"
				option-label="assetName"
				option-value="assetId"
				placeholder="Select a dataset"
				@update:model-value="scenario.setDatasetSpec($event)"
			/>

			<label>Select an intervention policy (historical)</label>
			<Dropdown placeholder="Optional" :disabled="isEmpty(interventionPolicies) || isFetchingModelInformation" />

			<label>Select an intervention policy (known future)</label>
			<Dropdown placeholder="Optional" :disabled="isEmpty(interventionPolicies) || isFetchingModelInformation" />

			<label>Select configuration representing best and generous estimates of the initial conditions</label>
			<Dropdown
				:model-value="scenario.modelConfigSpec.id"
				placeholder="Select a configuration"
				:options="modelConfigurations"
				option-label="name"
				option-value="id"
				@update:model-value="scenario.setModelConfigSpec($event)"
				:disabled="isEmpty(modelConfigurations) || isFetchingModelInformation"
			/>
		</div>
		<div class="col-6 flex flex-column gap-2">
			<h6>Outputs</h6>
			<label>Select an output metric</label>
			<MultiSelect
				:disabled="isEmpty(modelStateOptions) || isFetchingModelInformation"
				:model-value="scenario.calibrateSpec.ids"
				placeholder="Select output metrics"
				option-label="name"
				option-value="id"
				:options="modelStateOptions"
				@update:model-value="scenario.setCalibrateSpec($event)"
				filter
			/>
		</div>
	</div>
</template>

<script setup lang="ts">
import Dropdown from 'primevue/dropdown';
import { computed, ref, watch } from 'vue';
import { useProjects } from '@/composables/project';
import { AssetType, InterventionPolicy, ModelConfiguration } from '@/types/Types';
import { getInterventionPoliciesForModel, getModel, getModelConfigurationsForModel } from '@/services/model';
import { isEmpty } from 'lodash';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import MultiSelect from 'primevue/multiselect';
import { SituationalAwarenessScenario } from './situational-awareness-template';

const isFetchingModelInformation = ref(false);
const models = computed(() => useProjects().getActiveProjectAssets(AssetType.Model));
const datasets = computed(() => useProjects().getActiveProjectAssets(AssetType.Dataset));

const modelConfigurations = ref<ModelConfiguration[]>([]);
const interventionPolicies = ref<InterventionPolicy[]>([]);
const modelStateOptions = ref<any[]>([]);

const props = defineProps<{
	scenario: SituationalAwarenessScenario;
}>();

watch(
	() => props.scenario.modelSpec.id,
	async (modelId) => {
		if (!modelId) return;
		isFetchingModelInformation.value = true;
		const model = await getModel(modelId);
		if (!model) return;
		modelConfigurations.value = await getModelConfigurationsForModel(modelId);
		interventionPolicies.value = await getInterventionPoliciesForModel(modelId);

		const modelOptions: any[] = model.model.states;

		model.semantics?.ode.observables?.forEach((o) => {
			modelOptions.push(o);
		});
		modelStateOptions.value = modelOptions;
		isFetchingModelInformation.value = false;
	},
	{ immediate: true }
);
</script>
