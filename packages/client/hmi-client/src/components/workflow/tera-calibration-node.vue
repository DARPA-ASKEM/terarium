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
import { CalibrationOperation } from './calibrate-operation';

const props = defineProps<{
	node: WorkflowNode;
}>();

const emit = defineEmits(['append-output-port']);

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

// Used to setup modelConfig ref, as well as modelColumnNames which is used for mapping dropdown
const setupModelInput = async () => {
	if (modelConfigId.value) {
		modelConfig.value = await getModelConfigurationById(modelConfigId.value);
		// modelColumnNames.value = modelConfig.value.configuration.model.states.map((state) => state.name);
		modelColumnNames.value = modelConfig.value.configuration.S.map((state) => state.sname);
		modelColumnNames.value?.push('timestep');
	}
};

const setupDatasetInput = async () => {
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
};

const runCalibrate = async () => {
	if (
		!modelConfigId.value ||
		!datasetId.value ||
		!currentDatasetFileName.value ||
		!modelConfig.value
	)
		return;

	const formattedMap: { [index: string]: string } = {};
	mapping.value.forEach((ele) => {
		formattedMap[ele.datasetVariable] = ele.modelVariable;
	});
	// TODO: TS/1225 -> Should not have to rand results
	// console.log(modelConfig.value as any);
	// const initials = (modelConfig.value as any).semantics.ode.initials.map((d) => d.target);
	// const rates = (modelConfig.value as any).semantics.ode.rates.map((d) => d.target);
	// const initialsObj = {};
	// const paramsObj = {};

	// initials.forEach((d) => {
	// 	initialsObj[d] = Math.random() * 100;
	// });
	// rates.forEach((d) => {
	// 	paramsObj[d] = Math.random() * 0.05;
	// });

	const calibrationRequest: CalibrationRequest = {
		modelConfigId: modelConfigId.value,
		dataset: {
			id: datasetId.value,
			filename: currentDatasetFileName.value,
			mappings: formattedMap
		},
		extra: {
			initials: {
				S: 0.49457800495224524,
				I: 0.4497387877393193,
				R: 0.32807705995998604
			},
			params: {
				inf: 0.16207166221196045,
				rec: 0.7009195813964052
			}
		},
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
		updateOutputPorts(completedRunId);
		// showSpinner.value = false;
	} else if (currentSimulation && ongoingStatusList.includes(currentSimulation.status)) {
		// recursively call until all runs retrieved
		setTimeout(getStatus, 3000);
	} else {
		// throw if there are any failed runs for now
		console.error('Failed', startedRunId.value);
		throw Error('Failed Runs');
	}
};

const updateOutputPorts = async (runId) => {
	const port = props.node.inputs[0];
	emit('append-output-port', {
		type: CalibrationOperation.outputs[0].type,
		label: `${port.label} Results`,
		value: {
			runId
		}
	});
};

// Used from button to add new entry to the mapping object
function addMapping() {
	mapping.value.push({
		modelVariable: '',
		datasetVariable: ''
	});
	console.log(mapping.value);
	console.log(modelColumnNames.value);
}

watch(() => modelConfigId.value, setupModelInput, { immediate: true });

watch(() => datasetId.value, setupDatasetInput, { immediate: true });
</script>

<style scoped>
.dropdown-button {
	width: 156px;
	height: 25px;
	border-radius: 6px;
}
</style>
