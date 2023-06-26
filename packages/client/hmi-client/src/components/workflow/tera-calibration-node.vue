<template>
	<Button class="p-button-sm" label="Run" @click="runCalibrate" />
	<Accordion :multiple="true" :active-index="[0, 3]">
		<AccordionTab header="Mapping">
			<DataTable class="p-datatable-xsm" :value="mapping">
				<Column field="modelVariable">
					<template #body="{ data, field }">
						<Dropdown
							class="w-full"
							placeholder="Select a variable"
							v-model="data[field]"
							:options="modelColumnNames"
						/>
					</template>
				</Column>
				<Column field="datasetVariable">
					<template #body="{ data, field }">
						<Dropdown
							class="w-full"
							placeholder="Select a variable"
							v-model="data[field]"
							:options="datasetColumnNames"
						/>
					</template>
				</Column>
			</DataTable>
			<div>
				<Button
					class="p-button-sm p-button-outlined"
					icon="pi pi-plus"
					label="Add mapping"
					@click="addMapping"
				/>
			</div>
		</AccordionTab>
		<AccordionTab header="Loss"></AccordionTab>
		<AccordionTab header="Parameters"></AccordionTab>
		<AccordionTab header="Variables">
			<!-- <tera-simulate-chart
				v-for="index in calibrateNumCharts"
				:key="index"
				:run-results="calibrateRunResults"
				:run-id-list="calibrateRunIdList"
				:chart-idx="index"
			/> -->
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import { computed, shallowRef, watch, ref } from 'vue';
import { downloadRawFile, getDataset } from '@/services/dataset';
import { getModelConfigurationById } from '@/services/model-configurations';
import { WorkflowNode } from '@/types/workflow';
import DataTable from 'primevue/datatable';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import Column from 'primevue/column';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import {
	CalibrationRequest,
	CsvAsset,
	Dataset,
	Simulation,
	ModelConfiguration
} from '@/types/Types';
// import TeraSimulateChart from './tera-simulate-chart.vue';
import { makeCalibrateJob, getSimulation } from '@/services/models/simulation-service';

const props = defineProps<{
	node: WorkflowNode;
}>();

// const emit = defineEmits(['append-output-port']);

const modelConfigId = computed(() => props.node.inputs[0].value?.[0] as string | undefined);
const datasetId = computed(() => props.node.inputs[1].value?.[0] as string | undefined);
const currentDatasetFileName = ref<string>();
const modelConfig = ref<ModelConfiguration>();
const startedRunId = ref<string>();
const completedRunId = ref<string>();

const datasetColumnNames = ref<string[]>();
const modelColumnNames = ref<string[]>();

const mapping = ref<any[]>([
	{
		modelVariable: '',
		datasetVariable: ''
	}
]);

const csvAsset = shallowRef<CsvAsset | undefined>(undefined);
// const datasetValue = ref();

// const emit = defineEmits(['append-output-port']);
// 			emit('append-output-port', {
// 				type: 'string',
// 				label: 'Calibration Job ID',
// 				value: runId.value
// 			});

watch(
	() => modelConfigId.value,
	async () => {
		if (modelConfigId.value) {
			modelConfig.value = await getModelConfigurationById(modelConfigId.value);
			console.log(modelConfig.value);
			// modelColumnNames.value = modelConfig.value.configuration.model.states.map((state) => state.name);
			modelColumnNames.value = modelConfig.value.configuration.S.map((state) => state.sname);
			modelColumnNames.value?.push('timestep');
		}
	}
);

watch(
	() => datasetId.value, // When dataset ID changes, update datasetColumnNames
	async () => {
		if (datasetId.value) {
			// Get dataset:
			const dataset: Dataset | null = await getDataset(datasetId.value.toString());
			currentDatasetFileName.value = dataset?.fileNames?.[0] ?? '';
			// We are assuming here there is only a single csv file. This may change in the future as the API allows for it.
			csvAsset.value = (await downloadRawFile(
				datasetId.value.toString(),
				currentDatasetFileName.value
			)) as CsvAsset;
			datasetColumnNames.value = csvAsset.value?.headers;
			// datasetValue.value = csvAsset.value?.csv.map((row) => row.join(',')).join('\n');
		}
	}
);

const runCalibrate = async () => {
	if (!modelConfigId.value || !datasetId.value || !currentDatasetFileName.value) return;

	const calibrationRequest: CalibrationRequest = {
		modelConfigId: modelConfigId.value,
		dataset: {
			id: datasetId.value,
			filename: currentDatasetFileName.value,
			mappings: mapping.value
		},
		extra: {},
		engine: 'sciml'
	};
	const response = await makeCalibrateJob(calibrationRequest);
	console.log(response);
	console.log(calibrationRequest);

	startedRunId.value = response.id;
	getStatus();
	// showSpinner.value = true;s
};
// Retrieve run ids
// FIXME: Replace with API.poller
const getStatus = async () => {
	if (!startedRunId.value) return;

	const currentSimulation: Simulation | null = await getSimulation(startedRunId.value); // get TDS's simulation object
	const ongoingStatusList = ['running', 'queued'];

	if (currentSimulation && currentSimulation.status === 'complete') {
		completedRunId.value = startedRunId.value;
		console.log(completedRunId.value); // TOM TODO: Just console for testing:
		// showSpinner.value = false;
	} else if (currentSimulation && currentSimulation.status in ongoingStatusList) {
		// recursively call until all runs retrieved
		setTimeout(getStatus, 3000);
	} else {
		// throw if there are any failed runs for now
		console.error('Failed', startedRunId.value);
		throw Error('Failed Runs');
	}
};

function addMapping() {
	mapping.value.push({
		modelVariable: '',
		datasetVariable: ''
	});
	console.log(mapping.value);
	console.log(modelColumnNames.value);
}
</script>

<style scoped>
.dropdown-button {
	width: 156px;
	height: 25px;
	border-radius: 6px;
}
</style>
