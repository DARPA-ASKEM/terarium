<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<template #sidebar>
			<tera-slider-panel
				class="input-config"
				v-model:is-open="isInputSettingsOpen"
				header="Compare settings"
				content-width="440px"
			>
				<template #content>
					<tera-drilldown-section class="px-2">
						<Button class="ml-auto" size="small" label="Run" @click="onRun" />
						<label>What do you want to compare?</label>
						<Dropdown
							v-model="selectedCompareOption"
							:options="compareOptions"
							option-label="label"
							option-value="value"
						></Dropdown>
						<tera-checkbox class="pt-2" v-model="isSimulationsFromSameModel" label="Show results in a new tab" />

						<label> Select simulation to use as a baseline (optional) </label>
						<Dropdown
							v-model="selectedDataset"
							:options="datasets"
							option-label="name"
							option-value="id"
							:loading="isFetchingDatasets"
							placeholder="Optional"
						/>

						<label>Comparison tables</label>
						<tera-checkbox
							v-model="isATESelected"
							label="Average treatment effect (ATE)"
							subtext="Description for ATE."
						/>
					</tera-drilldown-section>
				</template>
			</tera-slider-panel>
		</template>

		<tera-drilldown-section :tabName="DrilldownTabs.Wizard"> </tera-drilldown-section>

		<tera-drilldown-section :tabName="DrilldownTabs.Notebook"> </tera-drilldown-section>

		<template #sidebar-right>
			<tera-slider-panel
				class="input-config"
				v-model:is-open="isOutputSettingsOpen"
				header="Output settings"
				content-width="360px"
			>
				<template #content>
					<tera-drilldown-section class="px-2">
						<label>What values do you want to plot?</label>
						<div v-for="option in plotOptions" class="flex align-items-center" :key="option.value">
							<RadioButton v-model="selectedPlotValue" :value="option.value" name="plotValues" />
							<label class="pl-2 py-1" :for="option.value">{{ option.label }}</label>
						</div>
					</tera-drilldown-section>
				</template>
			</tera-slider-panel>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { DrilldownTabs } from '@/types/common';
import { onMounted, ref } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import { Dataset } from '@/types/Types';
import { getDataset } from '@/services/dataset';
import TeraCheckbox from '@/components/widgets/tera-checkbox.vue';
import RadioButton from 'primevue/radiobutton';
import { CompareDatasetsState } from './compare-datasets-operation';
// const props =
const props = defineProps<{
	node: WorkflowNode<CompareDatasetsState>;
}>();

const compareOptions: { label: string; value: string }[] = [
	{ label: 'Compare the impact of interventions', value: 'interventions_impact' }
];
const selectedCompareOption = ref(compareOptions[0].value);

const datasets = ref<Dataset[]>([]);
const selectedDataset = ref<Dataset | null>(null);

const plotOptions = [
	{ label: 'Percent change', value: 'percentage' },
	{ label: 'Absolute difference', value: 'value' }
];
const selectedPlotValue = ref('percentage');

const isInputSettingsOpen = ref(true);
const isOutputSettingsOpen = ref(true);
const isFetchingDatasets = ref(false);
const isSimulationsFromSameModel = ref(true);
const isATESelected = ref(false);

const onRun = () => {
	console.log('run');
};

const emit = defineEmits(['update-state', 'update-status', 'close']);

onMounted(async () => {
	const inputs = props.node.inputs;
	const datasetInputs = inputs.filter(
		(input) => input.type === 'datasetId' && input.status === WorkflowPortStatus.CONNECTED
	);
	console.log(datasetInputs);
	const promises = datasetInputs.map((input) => getDataset(input.value![0]));

	isFetchingDatasets.value = true;
	await Promise.all(promises).then((ds) => {
		const filteredDatasets: Dataset[] = ds.filter((dataset) => dataset !== null);
		datasets.value.push(...filteredDatasets);
	});
	isFetchingDatasets.value = false;
});
</script>

<style scoped>
label {
	padding: var(--gap-2) 0;
}
</style>
