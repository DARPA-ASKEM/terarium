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
			<AccordionTab :header="modelConfig.model.name">
				<tera-model-diagram :model="modelConfig.model" :is-editable="false" />
			</AccordionTab>
			<AccordionTab header="Model configuation">
				<tera-model-configuration
					ref="modelConfigurationRef"
					:model="modelConfig.model"
					:is-editable="false"
					:model-config-node-input="modelConfig"
					calibration-config
				/>
			</AccordionTab>
			<AccordionTab :header="datasetName">
				<tera-dataset-datatable preview-mode :raw-content="csvAsset ?? null" />
			</AccordionTab>
			<AccordionTab header="Train / Test ratio"></AccordionTab>
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
											:options="datasetVariables"
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
									:options="modelVariables"
								/>
							</template>
						</Column>
						<Column field="datasetVariable">
							<template #body="{ data, field }">
								<Dropdown
									class="w-full"
									placeholder="Select a variable"
									v-model="data[field].label"
									:options="datasetVariables"
								/>
							</template>
						</Column>
						<template #expansion="slotProps">
							<div>
								Model: {{ slotProps.data.modelVariable }}
								<br />
								Dataset: {{ slotProps.data.datasetVariable }}
								<!-- <DataTable> put above in table -->
							</div>
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
					v-for="index in openedWorkflowNodeStore.calibrateNumCharts"
					:key="index"
					:run-results="openedWorkflowNodeStore.calibrateRunResults"
					:run-id-list="openedWorkflowNodeStore.calibrateRunIdList"
					:chart-idx="index"
				/>
				<Button
					class="add-chart"
					text
					:outlined="true"
					@click="openedWorkflowNodeStore.calibrateNumCharts++"
					label="Add Chart"
					icon="pi pi-plus"
				></Button>
			</AccordionTab>
			<AccordionTab header="Calibrated parameter values">
				<tera-model-configuration
					:model="modelConfig.model"
					:is-editable="false"
					:model-config-node-input="modelConfig"
					calibration-config
				/>
			</AccordionTab>
		</Accordion>
		<div>Run ID: {{ runId }}</div>
		<Button @click="getCalibrationStatus"> Get Run Status </Button>
		<Button @click="getCalibrationResults"> Get Run Results </Button>
	</tera-asset>
</template>

<script setup lang="ts">
import { computed, ref, shallowRef, watch } from 'vue';
import { csvParse } from 'd3';
import Button from 'primevue/button';
import { Poller } from '@/api/api';
import {
	makeCalibrateJob,
	makeForecast,
	getRunStatus,
	getRunResult
} from '@/services/models/simulation-service';
import { shimPetriModel } from '@/services/models/petri-shim';
import { CalibrationParams, CsvAsset } from '@/types/Types';
import { ModelConfig } from '@/types/ModelConfig';
import Dropdown from 'primevue/dropdown';
import DataTable from 'primevue/datatable';
import Row from 'primevue/row';
import ColumnGroup from 'primevue/columngroup';
import Column from 'primevue/column';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { downloadRawFile } from '@/services/dataset';
import { WorkflowNode } from '@/types/workflow';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraAssetNav from '@/components/asset/tera-asset-nav.vue';
import TeraModelDiagram from '@/components/models/tera-model-diagram.vue';
import TeraModelConfiguration from '@/components/models/tera-model-configuration.vue';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';
import TeraSimulateChart from './tera-simulate-chart.vue';

const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();

enum CalibrationView {
	INPUT = 'input',
	OUTPUT = 'output'
}

const props = defineProps<{
	node: WorkflowNode;
}>();

const modelConfigurationRef = ref();
const calibrationView = ref(CalibrationView.INPUT);
const expandedRows = ref([]);

const runId = ref(props.node.outputs?.[0]?.value ?? undefined);

const datasetVariables = ref<string[]>();
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

const timestepColumn = ref('');

const datasetId = computed<string | undefined>(() => props.node.inputs[1].value?.[0]);
const datasetName = computed(() => props.node.inputs[1].label?.[0]);
const modelConfig = computed<ModelConfig | undefined>(() => props.node.inputs[0].value?.[0]);

// Model variables checked in the model configuration will be options in the mapping dropdown
const modelVariables = computed(() => {
	if (modelConfigurationRef.value) {
		const filteredVariables = modelConfig.value?.model.content.S.filter((state) =>
			modelConfigurationRef.value.selectedModelVariables.includes(state.sname)
		);

		return filteredVariables.map((state) => state.sname);
	}
	return modelConfig.value?.model.content.S.map((state) => state.sname);
});

const disableRunButton = computed(
	() => !mapping.value?.[0].modelVariable.label || !mapping.value?.[0].datasetVariable.label
);

const mapping = ref<any[]>([
	{
		modelVariable: { label: null, name: null, units: null, concept: null, definition: null },
		datasetVariable: { label: null, name: null, units: null, concept: null, definition: null }
	}
]);

const calibrate = async () => {
	// Make calibration job.
	if (modelConfig.value && csvAsset.value) {
		console.log(modelConfig.value);

		const featureMappings: { [index: string]: string } = {};
		// Go from 2D array to a index: value like they want
		// was just easier to work with 2D array for user input
		for (let i = 0; i < mapping.value.length; i++) {
			if (mapping.value[i].modelVariable.label && mapping.value[i].datasetVariable.label) {
				featureMappings[mapping.value[i].datasetVariable.label] =
					mapping.value[i].modelVariable.label;
			}
		}

		const calibrationParam: CalibrationParams = {
			model: shimPetriModel(modelConfig.value.model), // FIXME: current need to strip out metadata, should do serverside
			initials: modelConfig.value.initialValues,
			params: modelConfig.value.parameterValues,
			timesteps_column: timestepColumn.value,
			feature_mappings: featureMappings,
			dataset: csvAsset.value.csv.map((row) => row.join(',')).join('\n')
		};
		const results = await makeCalibrateJob(calibrationParam);
		runId.value = results.id;

		// do polling and retrieve calibration result

		// use fake calibration params for SIR model
		const calibratedParams = {
			t2: 0.11923213709379274,
			t1: 0.28589833658579455
		};

		const { csv, headers } = csvAsset.value;
		const indexOfTimestep = headers.indexOf(timestepColumn.value);
		const payload = {
			model: shimPetriModel(modelConfig.value.model),
			initials: modelConfig.value.initialValues,
			params: calibratedParams,
			tspan: [Number(csv[1][indexOfTimestep]), Number(csv[csv.length - 1][indexOfTimestep])]
		};

		const forecastResponse = await makeForecast(payload);
		const poller = new Poller<object>()
			.setInterval(2000)
			.setThreshold(90)
			.setPollAction(async () => {
				const statusResponse = await getRunStatus(forecastResponse.id);

				if (statusResponse.status === 'done') {
					return {
						data: statusResponse,
						progress: null,
						error: null
					};
				}
				if (statusResponse.status !== 'running') {
					throw Error('failed forecast');
				}

				return {
					data: null,
					progress: null,
					error: null
				};
			});
		await poller.start();

		const resultCsv = await getRunResult(forecastResponse.id);
		const result = csvParse(resultCsv);

		openedWorkflowNodeStore.setCalibrateResults(
			csvAsset.value,
			result as any,
			indexOfTimestep,
			featureMappings
		);
	}
};

function addMapping() {
	mapping.value.push({
		modelVariable: { label: null, name: null, units: null, concept: null, definition: null },
		datasetVariable: { label: null, name: null, units: null, concept: null, definition: null }
	});

	console.log(mapping.value);
}

const getCalibrationStatus = async () => {
	console.log('Getting status of run');
	const results = await getRunStatus(Number(runId.value));
	console.log(results);
};

const getCalibrationResults = async () => {
	console.log('Getting results of run');
	const results = await getRunResult(Number(runId.value));
	console.log(results);
};

async function updateDataset() {
	if (datasetId.value) {
		csvAsset.value = (await downloadRawFile(datasetId.value)) as CsvAsset;
		datasetVariables.value = csvAsset.value?.headers;

		// Reset mapping on update for now
		mapping.value = [
			{
				modelVariable: { label: null, name: null, units: null, concept: null, definition: null },
				datasetVariable: { label: null, name: null, units: null, concept: null, definition: null }
			}
		];
	}
}

watch(
	() => datasetId.value,
	() => updateDataset(),
	{
		immediate: true
	}
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

.mapping {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}
</style>
