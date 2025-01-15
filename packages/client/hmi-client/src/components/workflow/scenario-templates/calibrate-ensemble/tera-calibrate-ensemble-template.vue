<template>
	<tera-scenario-template :header="header" :scenario-instance="scenario" @save-workflow="emit('save-workflow')">
		<template #inputs>
			<label>Select a dataset (historical)</label>
			<Dropdown
				:model-value="scenario.datasetSpec.id"
				:options="datasets"
				option-label="assetName"
				option-value="assetId"
				placeholder="Select a dataset"
				@update:model-value="scenario.setDatasetSpec($event)"
				class="mb-3"
			/>

			<div class="flex justify-content-between">
				<label>Select models</label>
				<Button icon="pi pi-plus" label="Add model" size="small" text @click="scenario.addTabSpec()" />
			</div>
			<TabView scrollable v-model:activeIndex="activeTab">
				<TabPanel v-for="(tab, index) in scenario.tabSpecs" :header="`Model ${index + 1}`" :key="tab.id">
					<template #header
						><Button
							v-if="scenario.tabSpecs.length > 1"
							icon="pi pi-times"
							class="p-0"
							text
							size="small"
							@click.stop="onRemoveTab(index)"
					/></template>
					<div class="flex flex-column">
						<label>Select a model</label>
						<Dropdown
							:model-value="tab.modelSpec.id"
							:options="models"
							option-label="assetName"
							option-value="assetId"
							placeholder="Select a model"
							@update:model-value="scenario.setModelSpec($event, tab.id)"
						/>

						<label>Select configuration representing best and generous estimates of the initial conditions</label>
						<Dropdown
							:model-value="tab.modelConfigSpec.id"
							placeholder="Select a configuration"
							:options="modelAssets.get(tab.modelSpec.id)?.modelConfigurations"
							option-label="name"
							option-value="id"
							@update:model-value="scenario.setModelConfigSpec($event, tab.id)"
							:disabled="isEmpty(modelAssets.get(tab.modelSpec.id)?.modelConfigurations) || isFetchingModelInformation"
							:loading="isFetchingModelInformation"
						/>
					</div>
				</TabPanel>
			</TabView>

			<label>Map dataset to models</label>
			<div class="overflow-x-scroll">
				<table>
					<thead>
						<tr>
							<th v-for="(header, i) in tableHeaders" :key="i">
								{{ header }}
							</th>
						</tr>
					</thead>
					<tbody>
						<!-- Timestamp selection-->
						<tr>
							<td>Timestamp</td>
							<td>
								<Dropdown
									:model-value="scenario.timestampColName"
									@update:model-value="scenario.setTimeStepColName($event)"
									placeholder="Select"
									:options="datasetColumnNames"
								/>
							</td>
						</tr>
						<tr v-for="(config, i) in scenario.calibrateMappings" :key="i">
							<td>
								<tera-input-text v-model="config.newName" placeholder="Variable name" />
							</td>
							<td>
								<Dropdown v-model="config.datasetMapping" placeholder="Select" :options="datasetColumnNames" />
							</td>
							<td v-for="(configuration, index) in selectedModelConfigurations" :key="configuration.id">
								<Dropdown
									v-if="configuration?.id"
									v-model="config.modelConfigurationMappings[configuration.id]"
									placeholder="Select"
									:options="
										modelAssets
											.get(scenario.tabSpecs[index].modelSpec.id)
											?.modelStates.map((ele) => ele.referenceId ?? ele.id)
									"
								/>
							</td>
							<td>
								<Button
									v-if="scenario.calibrateMappings.length > 1"
									icon="pi pi-trash"
									text
									@click="scenario.removeMappingRow(i)"
								/>
							</td>
						</tr>
					</tbody>
				</table>
			</div>

			<div>
				<Button size="small" text icon="pi pi-plus" label="Add mapping" @click="scenario.addMappingRow()" />
			</div>
		</template>

		<template #outputs>
			<label>Select an output metric</label>
			<MultiSelect
				:disabled="isEmpty(modelStateOptions) || isFetchingModelInformation"
				:model-value="scenario.simulateSpec.ids"
				placeholder="Select output metrics"
				option-label="id"
				option-value="id"
				:options="modelStateOptions"
				@update:model-value="scenario.setSimulateSpec($event)"
				:loading="isFetchingModelInformation"
				filter
			/>
			<img :src="CalibrateEnsembleImg" alt="Calibrate ensemble model" />
		</template>
	</tera-scenario-template>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { AssetType, Dataset, ModelConfiguration } from '@/types/Types';
import { useProjects } from '@/composables/project';
import { getModel, getModelConfigurationsForModel } from '@/services/model';
import { isEmpty } from 'lodash';
import Dropdown from 'primevue/dropdown';
import MultiSelect from 'primevue/multiselect';
import Button from 'primevue/button';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import { getFileName } from '@/services/calibrate-workflow';
import { getDataset } from '@/services/dataset';
import CalibrateEnsembleImg from '@/assets/svg/template-images/calibrate-ensemble-model-thumbnail.svg';
import { ScenarioHeader } from '../base-scenario';
import { CalibrateEnsembleScenario } from './calibrate-ensemble-scenario';
import TeraScenarioTemplate from '../tera-scenario-template.vue';

const header: ScenarioHeader = Object.freeze({
	title: 'Calibrate an ensemble model template',
	question: 'Create a more accurate model by combining multiple models in an ensemble.',
	description: 'Simulate and calibrate each model individually, then calibrate the ensemble.',
	examples: ['How can I leverage the strengths of each model to make the most accurate model possible?']
});

const datasets = computed(() => useProjects().getActiveProjectAssets(AssetType.Dataset));
const models = computed(() => useProjects().getActiveProjectAssets(AssetType.Model));
const isFetchingModelInformation = ref(false);

const modelAssets = ref<Map<string, { modelConfigurations: ModelConfiguration[]; modelStates: any }>>(new Map());

const modelStateOptions = ref<any[]>([]);

const selectedModelConfigurations = computed<ModelConfiguration[]>(() =>
	// get the model configuration ids from the tab specs and return the already fetched model configurations from the model assets
	props.scenario.tabSpecs
		.map((tab) =>
			modelAssets.value
				.get(tab.modelSpec.id)
				?.modelConfigurations.find((config) => config.id === tab.modelConfigSpec.id)
		)
		.filter((config) => config !== undefined)
);

const dataset = ref<Dataset | null>();

const tableHeaders = computed(() => {
	const headers = ['Ensemble model'];

	headers.push(dataset.value ? getFileName(dataset.value) : '');

	selectedModelConfigurations.value.forEach((config) => {
		headers.push(config.name ?? '');
	});
	return headers;
});

const datasetColumnNames = computed(() => dataset.value?.columns?.map((col) => col.name) ?? ([] as string[]));

const activeTab = ref(0);
const props = defineProps<{
	scenario: CalibrateEnsembleScenario;
}>();

const allModelsIds = computed(() => props.scenario.tabSpecs.map((tab) => tab.modelSpec.id));

const emit = defineEmits(['save-workflow']);

const onRemoveTab = (index: number) => {
	props.scenario.removeTabSpec(index);
	if (activeTab.value > 0) {
		activeTab.value--;
	}
};

watch(
	() => props.scenario.datasetSpec.id,
	async (datasetId) => {
		if (!datasetId) return;
		dataset.value = await getDataset(datasetId);
	},
	{ immediate: true }
);

watch(
	() => allModelsIds.value,
	async (newModelIds) => {
		if (!newModelIds) return;
		isFetchingModelInformation.value = true;
		const validIds = newModelIds.filter((id) => !!id);
		await Promise.all(
			validIds.map(async (modelId) => {
				const [model, modelConfigurations] = await Promise.all([
					getModel(modelId),
					getModelConfigurationsForModel(modelId)
				]);

				return { modelId, model, modelConfigurations };
			})
		).then((assets) => {
			assets.forEach(({ modelId, model, modelConfigurations }) => {
				if (model?.id) {
					const modelOptions = model.model.states || [];
					model.semantics?.ode.observables?.forEach((o) => {
						modelOptions.push(o);
					});

					modelAssets.value.set(modelId, {
						modelConfigurations,
						modelStates: modelOptions
					});
				}
			});
		});

		isFetchingModelInformation.value = false;
	},
	{ immediate: true }
);
</script>
