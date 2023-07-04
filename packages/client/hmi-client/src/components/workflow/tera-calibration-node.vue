<template>
	<Button class="p-button-sm" label="Run" @click="runCalibrate" :disabled="disableRunButton" />
	<Accordion :multiple="true" :active-index="[0, 3]">
		<AccordionTab header="Mapping">
			<DataTable class="p-datatable-xsm" :value="mapping">
				<Column field="modelVariable">
					<template #body="{ data, field }">
						<!-- Tom TODO: No v-model -->
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
						<!-- Tom TODO: No v-model -->
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
		<AccordionTab header="Variables">
			<tera-simulate-chart
				v-for="(cfg, index) of node.state.chartConfigs"
				:key="index"
				:run-results="runResults"
				:chartConfig="cfg"
				@configuration-change="chartConfigurationChange(index, $event)"
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
		<!-- <AccordionTab header="Loss"></AccordionTab>
		<AccordionTab header="Parameters"></AccordionTab>
		<AccordionTab header="Variables"></AccordionTab> -->
	</Accordion>
</template>

<script setup lang="ts">
import { computed, shallowRef, watch, ref, ComputedRef } from 'vue';
import { WorkflowNode } from '@/types/workflow';
import DataTable from 'primevue/datatable';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import Column from 'primevue/column';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { CalibrationRequest, CsvAsset, Simulation, ModelConfiguration } from '@/types/Types';
import {
	makeCalibrateJob,
	getSimulation,
	getRunResult
} from '@/services/models/simulation-service';
import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';
import { setupModelInput, setupDatasetInput } from '@/services/calibrate-workflow';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { csvParse } from 'd3';
import { workflowEventBus } from '@/services/workflow';
import _ from 'lodash';
import {
	CalibrationOperation,
	CalibrationOperationState,
	CalibrateMap
} from './calibrate-operation';
import TeraSimulateChart from './tera-simulate-chart.vue';

const props = defineProps<{
	node: WorkflowNode;
}>();

const emit = defineEmits(['append-output-port']);
const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();

const modelConfigId = computed(() => props.node.inputs[0].value?.[0] as string | undefined);
const datasetId = computed(() => props.node.inputs[1].value?.[0] as string | undefined);
const currentDatasetFileName = ref<string>();
const modelConfig = ref<ModelConfiguration>();
const startedRunId = ref<string>();
const completedRunId = ref<string>();

const datasetColumnNames = ref<string[]>();
const modelColumnNames = ref<string[] | undefined>();
const calibrateNumCharts = ref<number>(1);
const runResults = ref<RunResults>({});
const simulationIds: ComputedRef<any | undefined> = computed(
	<any | undefined>(() => props.node.outputs[0]?.value)
);

const mapping = ref<CalibrateMap[]>(props.node.state.mapping);
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

const disableRunButton = computed(
	() =>
		!currentDatasetFileName.value ||
		!modelConfig.value ||
		!csvAsset.value ||
		!modelConfigId.value ||
		!datasetId.value
);

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
			mappings: formattedMap
		},
		extra: {},
		engine: 'sciml'
	};
	const response = await makeCalibrateJob(calibrationRequest);

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
	const portLabel = props.node.inputs[0].label;
	emit('append-output-port', {
		type: CalibrationOperation.outputs[0].type,
		label: `${portLabel} Result`,
		value: {
			runId
		}
	});
};

// Used from button to add new entry to the mapping object
// Tom TODO: Make this generic, its copy paste from drilldown
function addMapping() {
	mapping.value.push({
		modelVariable: '',
		datasetVariable: ''
	});

	const state: CalibrationOperationState = _.cloneDeep(props.node.state);
	state.mapping = mapping.value;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
}

// Tom TODO: Make this generic, its copy paste from drilldown
const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state: CalibrationOperationState = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

watch(
	() => props.node.inputs[1],
	async () => {
		console.log(props.node.inputs[1]);
	},
	{ immediate: true }
);

// Set up model config + dropdown names
// Note: Same as calibrate side panel
watch(
	() => modelConfigId.value,
	async () => {
		console.log(modelConfigId.value);
		const { modelConfiguration, modelColumnNameOptions } = await setupModelInput(
			modelConfigId.value
		);
		modelConfig.value = modelConfiguration;
		modelColumnNames.value = modelColumnNameOptions;
		console.log(modelConfiguration);
	},
	{ immediate: true }
);

// Set up csv + dropdown names
// Note: Same as calibrate side panel
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

watch(
	() => props.node,
	(node) => openedWorkflowNodeStore.setNode(node ?? null),
	{ deep: true }
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
.dropdown-button {
	width: 156px;
	height: 25px;
	border-radius: 6px;
}
</style>
