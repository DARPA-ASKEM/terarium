<template>
	<!--Probably rename tera-asset to something even more abstract-->
	<tera-asset :name="'Calibrate'" is-editable stretch-content>
		<template #nav>
			<tera-asset-nav :show-header-links="false">
				<template #viewing-mode>
					<span class="p-buttonset">
						<Button
							class="p-button-secondary p-button-sm"
							label="Input"
							icon="pi pi-sign-in"
							@click="calibrationView = CalibrationView.INPUT"
							:active="calibrationView === CalibrationView.INPUT"
						/>
						<Button
							class="p-button-secondary p-button-sm"
							label="Output"
							icon="pi pi-sign-out"
							@click="calibrationView = CalibrationView.OUTPUT"
							:active="calibrationView === CalibrationView.OUTPUT"
						/>
					</span>
				</template>
			</tera-asset-nav>
		</template>
		<Accordion
			v-if="calibrationView === CalibrationView.INPUT && modelConfig"
			:multiple="true"
			:active-index="[0, 1, 2, 3, 4]"
		>
			<AccordionTab :header="modelConfig.amrConfiguration.name">
				<tera-model-diagram :model="modelConfig.amrConfiguration" :is-editable="false" />
			</AccordionTab>
			<AccordionTab v-if="datasetId" :header="currentDatasetFileName">
				<tera-dataset-datatable preview-mode :raw-content="csvAsset ?? null" />
			</AccordionTab>
			<AccordionTab header="Train / Test ratio">
				<section class="train-test-ratio">
					<InputNumber v-model="trainTestValue" />
					<Slider v-model="trainTestValue" />
				</section>
			</AccordionTab>
		</Accordion>
		<Accordion
			v-if="calibrationView === CalibrationView.OUTPUT && modelConfig"
			:multiple="true"
			:active-index="[0, 1]"
		>
			<AccordionTab header="Variables">
				<tera-simulate-chart
					v-for="index in calibrateNumCharts"
					:key="index"
					:run-results="runResults"
					:run-id-list="simulationIds"
					:chart-idx="index"
				/>
				<Button
					class="add-chart"
					text
					:outlined="true"
					@click="calibrateNumCharts++"
					label="Add Chart"
					icon="pi pi-plus"
				></Button>
			</AccordionTab>
			<AccordionTab header="Calibrated parameter values">
				<!-- <tera-model-configuration :model="modelConfig.model" :is-editable="false"
					:model-config-node-input="calibratedModelConfig" /> -->
			</AccordionTab>
		</Accordion>
	</tera-asset>
</template>

<script setup lang="ts">
import { computed, ref, shallowRef, watch } from 'vue';
import { csvParse } from 'd3';
import Button from 'primevue/button';
import { getRunResult } from '@/services/models/simulation-service';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraAssetNav from '@/components/asset/tera-asset-nav.vue';
import TeraModelDiagram from '@/components/models/tera-model-diagram.vue';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';
import { CsvAsset, ModelConfiguration } from '@/types/Types';
import Slider from 'primevue/slider';
import InputNumber from 'primevue/inputnumber';
import { setupModelInput, setupDatasetInput } from '@/services/calibrate-workflow';
import { RunResults } from '@/types/SimulateConfig';
import TeraSimulateChart from './tera-simulate-chart.vue';

const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();
const node = ref(openedWorkflowNodeStore.node);

enum CalibrationView {
	INPUT = 'input',
	OUTPUT = 'output'
}

// Model variables checked in the model configuration will be options in the mapping dropdown
const modelColumnNames = ref<string[] | undefined>();

const calibrationView = ref(CalibrationView.INPUT);
const calibrateNumCharts = ref<number>(1);

const trainTestValue = ref(80);

const datasetColumnNames = ref<string[]>();
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

const modelConfig = ref<ModelConfiguration>();

const modelConfigId = computed<string | undefined>(() => node.value?.inputs[0]?.value?.[0]);
const datasetId = computed<string | undefined>(() => node.value?.inputs[1]?.value?.[0]);
const currentDatasetFileName = ref<string>();
const simulationIds = computed<any | undefined>(() => node.value?.outputs[0]?.value);
const runResults = ref<RunResults>({});

// Set up model config + dropdown names
// Note: Same as calibrate-node
watch(
	() => modelConfigId.value,
	async () => {
		const { modelConfiguration, modelColumnNameOptions } = await setupModelInput(
			modelConfigId.value
		);
		modelConfig.value = modelConfiguration;
		modelColumnNames.value = modelColumnNameOptions;
	},
	{ immediate: true }
);

// Set up csv + dropdown names
// Note: Same as calibrate-node
watch(
	() => datasetId.value,
	async () => {
		const { filename, csv } = await setupDatasetInput(datasetId.value);
		currentDatasetFileName.value = filename;
		csvAsset.value = csv;
		datasetColumnNames.value = csv?.headers;
	},
	{ immediate: true }
);

// Fetch simulation run results whenever output changes
watch(
	() => simulationIds.value,
	async () => {
		if (!simulationIds.value) return;
		const resultCsv = await getRunResult(simulationIds.value[0].runId, 'simulation.csv');
		const csvData = csvParse(resultCsv);
		runResults.value[simulationIds.value[0].runId] = csvData as any;
	},
	{ immediate: true }
);
</script>

<style scoped>
.p-accordion {
	padding-top: 1rem;
}

.dropdown-button {
	width: 156px;
	height: 25px;
	border-radius: 6px;
}

.train-test-ratio {
	display: flex;
	gap: 1rem;
	margin: 0.5rem 0;
}

.train-test-ratio > .p-slider {
	margin-top: 1rem;
	width: 100%;
}
</style>
