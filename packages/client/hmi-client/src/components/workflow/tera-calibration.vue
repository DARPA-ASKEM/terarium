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
		<template #edit-buttons>
			<Button
				icon="pi pi-play"
				label="Run"
				class="p-button-sm"
				:disabled="disableRunButton"
				@click="calibrate"
			/>
		</template>
		<Accordion
			v-if="calibrationView === CalibrationView.INPUT && modelConfig"
			:multiple="true"
			:active-index="[0, 1, 2, 3, 4]"
		>
			<AccordionTab :header="modelConfig.amrConfiguration.name">
				<tera-model-diagram :model="modelConfig.amrConfiguration" :is-editable="false" />
			</AccordionTab>
			<AccordionTab header="Model configuation">
				<!-- <tera-model-configuration
					ref="modelConfig"
					:model-configuration="modelConfig"
					calibration-config
				/> -->
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
			<AccordionTab header="Mapping">
				<section class="mapping">
					<div>
						Select target variables from the model and the corresponding data column you want to
						match them to.
					</div>
					<DataTable :value="mapping" v-model:expandedRows="expandedRows">
						<ColumnGroup type="header">
							<Row>
								<Column />
								<Column header="Model variable" />
								<Column header="Dataset variable" />
							</Row>
							<Row>
								<Column />
								<Column header="Timestep" style="font-weight: normal" />
								<Column>
									<template #header>
										<Dropdown
											class="w-full"
											placeholder="Timestep column"
											v-model="timestepColumn"
											:options="datasetColumnNames"
										/>
									</template>
								</Column>
							</Row>
						</ColumnGroup>
						<Column expander style="width: 5rem" />
						<Column field="modelVariable">
							<template #body="{ data, field }">
								<Dropdown
									class="w-full"
									placeholder="Select a variable"
									v-model="data[field].label"
									:options="modelColumnNames"
								/>
							</template>
						</Column>
						<Column field="datasetVariable">
							<template #body="{ data, field }">
								<Dropdown
									class="w-full"
									placeholder="Select a variable"
									v-model="data[field].label"
									:options="datasetColumnNames"
								/>
							</template>
						</Column>
						<template #expansion="slotProps">
							<DataTable
								class="p-datatable-sm"
								:value="[slotProps.data.modelVariable, slotProps.data.datasetVariable]"
							>
								<Column field="label" header="Label" />
								<Column field="name" header="Name" />
								<Column field="units" header="Units" />
								<Column field="concept" header="Concept" />
								<Column field="definition" header="Definition" />
							</DataTable>
						</template>
					</DataTable>
					<div>
						<Button
							class="p-button-sm p-button-outlined"
							icon="pi pi-plus"
							label="Add mapping"
							@click="addMapping"
						/>
					</div>
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
import { Poller } from '@/api/api';
import {
	makeCalibrateJob,
	getSimulation,
	getRunResult
} from '@/services/models/simulation-service';
import Dropdown from 'primevue/dropdown';
import DataTable from 'primevue/datatable';
import Row from 'primevue/row';
import ColumnGroup from 'primevue/columngroup';
import Column from 'primevue/column';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
// import { WorkflowNode } from '@/types/workflow';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraAssetNav from '@/components/asset/tera-asset-nav.vue';
import TeraModelDiagram from '@/components/models/tera-model-diagram.vue';
// import TeraModelConfiguration from '@/components/models/tera-model-configuration.vue';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';
import { CalibrationRequest, CsvAsset, ModelConfiguration } from '@/types/Types';
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
const expandedRows = ref([]);

const runId = ref<number>(0);
const trainTestValue = ref(80);

const timestepColumn = ref('');
const mapping = ref<any[]>([
	{
		modelVariable: { label: null, name: null, units: null, concept: null, definition: null },
		datasetVariable: { label: null, name: null, units: null, concept: null, definition: null }
	}
]);
const datasetColumnNames = ref<string[]>();
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

const modelConfig = ref<ModelConfiguration>();

const modelConfigId = computed<string | undefined>(() => node.value?.inputs[0]?.value?.[0]);
const datasetId = computed<string | undefined>(() => node.value?.inputs[1]?.value?.[0]);
const currentDatasetFileName = ref<string>();
const simulationIds = computed<any | undefined>(() => node.value?.outputs[0]?.value);
const runResults = ref<RunResults>({});

const disableRunButton = computed(
	() =>
		!currentDatasetFileName.value ||
		!modelConfig.value ||
		!csvAsset.value ||
		!modelConfigId.value ||
		!datasetId.value
);

const calibrate = async () => {
	// Make calibration job.
	if (
		!currentDatasetFileName.value ||
		!modelConfig.value ||
		!csvAsset.value ||
		!modelConfigId.value ||
		!datasetId.value
	)
		return;
	const featureMappings: { [index: string]: string } = {};
	// Go from 2D array to a index: value like they want
	// was just easier to work with 2D array for user input
	for (let i = 0; i < mapping.value.length; i++) {
		if (mapping.value[i].modelVariable.label && mapping.value[i].datasetVariable.label) {
			featureMappings[mapping.value[i].datasetVariable.label] =
				mapping.value[i].modelVariable.label;
		}
	}
	// TODO: TS/1225 -> Should not have to rand results
	const initials = modelConfig.value.amrConfiguration.semantics.ode.initials.map((d) => d.target);
	const rates = modelConfig.value.amrConfiguration.semantics.ode.rates.map((d) => d.target);
	const initialsObj = {};
	const paramsObj = {};

	initials.forEach((d) => {
		initialsObj[d] = Math.random() * 100;
	});
	rates.forEach((d) => {
		paramsObj[d] = Math.random() * 0.05;
	});

	const calibrationRequest: CalibrationRequest = {
		modelConfigId: modelConfigId.value,
		dataset: {
			id: datasetId.value,
			filename: currentDatasetFileName.value,
			mappings: featureMappings
		},
		extra: {
			initials: initialsObj,
			params: paramsObj
		},
		engine: 'sciml'
	};
	const results = await makeCalibrateJob(calibrationRequest);
	runId.value = results.id;

	const calibratePoller = new Poller<object>()
		.setInterval(2000)
		.setThreshold(90)
		.setPollAction(async () => {
			const statusResponse = await getSimulation(results.id);

			if (statusResponse && statusResponse.status === 'done') {
				return {
					data: statusResponse,
					progress: null,
					error: null
				};
			}
			if (statusResponse && statusResponse.status !== 'running') {
				throw Error('failed calibrate');
			}

			return {
				data: null,
				progress: null,
				error: null
			};
		});
	await calibratePoller.start();

	const calibratedParams = await getRunResult(results.id, 'result.csv');
	const result = csvParse(calibratedParams);
	console.log(`TODO: Use this result for node's output${result}`);
	// openedWorkflowNodeStore.setCalibrateResults(
	// 	csvAsset.value,
	// 	result as any,
	// 	indexOfTimestep,
	// 	featureMappings,
	// 	mappingSimplified.value
	// );
};

function addMapping() {
	mapping.value.push({
		modelVariable: { label: null, name: null, units: null, concept: null, definition: null },
		datasetVariable: { label: null, name: null, units: null, concept: null, definition: null }
	});
}

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

.mapping {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}
</style>
