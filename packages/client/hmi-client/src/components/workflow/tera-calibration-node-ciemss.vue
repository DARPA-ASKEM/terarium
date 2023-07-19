<template>
	<section v-if="!showSpinner">
		<Accordion
			v-if="datasetColumnNames && modelColumnNames"
			:multiple="true"
			:active-index="[0, 3]"
		>
			<AccordionTab header="Mapping">
				<DataTable class="p-datatable-xsm" :value="mapping">
					<Column field="modelVariable">
						<template #header>
							<span class="column-header">Model variable</span>
						</template>
						<template #body="{ data, field }">
							<div :class="data[field] ? 'mappingVariable' : 'unmappedVariable'">
								{{ data[field] ? data[field] : '-' }}
							</div>
						</template>
					</Column>
					<Column field="datasetVariable">
						<template #header>
							<span class="column-header">Dataset variable</span>
						</template>
						<template #body="{ data, field }">
							<div :class="data[field] ? 'mappingVariable' : 'unmappedVariable'">
								{{ data[field] ? data[field] : 'Not mapped' }}
							</div>
						</template>
					</Column>
				</DataTable>
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
					@click="addChart"
					label="Add Chart"
					icon="pi pi-plus"
				></Button>
			</AccordionTab>
			<AccordionTab header="Calibrated parameter values">
				<table class="p-datatable-table">
					<thead class="p-datatable-thead">
						<th>Parameter</th>
						<th>Value</th>
					</thead>
					<tr v-for="(content, key) in parameterResult" :key="key">
						<td>
							<p>{{ key }}</p>
						</td>
						<td>
							<p>{{ content }}</p>
						</td>
					</tr>
				</table>
			</AccordionTab>
			<AccordionTab header="Extras">
				<div class="extras w-full">
					<div class="flex flex-column gap-2 w-full">
						<label class="extras-label" for="numSamples">Number of samples</label>
						<InputNumber id="numSamples" v-model="numSamples"></InputNumber>
					</div>
					<div class="flex flex-column gap-2 w-full">
						<label class="extras-label" for="numIterations">Number of solver iterations</label>
						<InputNumber id="numIterations" v-model="numIterations"></InputNumber>
					</div>
				</div>
				<div class="flex flex-column gap-2 w-full">
					<label class="extras-label" for="method">Solver method</label>
					<Dropdown id="method" :options="ciemssMethodOptions" v-model="method" />
				</div>
			</AccordionTab>
			<!-- <AccordionTab header="Loss"></AccordionTab>
			<AccordionTab header="Parameters"></AccordionTab>
			<AccordionTab header="Variables"></AccordionTab> -->
		</Accordion>
		<section v-else class="emptyState">
			<img src="@assets/svg/seed.svg" alt="" draggable="false" />
			<p class="helpMessage">
				Connect a model configuration and dataset, then configure in the side panel
			</p>
		</section>
		<Button
			class="p-button-sm run-button"
			label="Run"
			icon="pi pi-play"
			@click="runCalibrate"
			:disabled="disableRunButton"
		/>
	</section>
	<section v-else>
		<div><i class="pi pi-spin pi-spinner"></i> loading...</div>
	</section>
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
import InputNumber from 'primevue/inputnumber';
import { CalibrationRequestCiemss, CsvAsset, Simulation, ModelConfiguration } from '@/types/Types';
import {
	makeCalibrateJobCiemss,
	getSimulation,
	getRunResultCiemss
} from '@/services/models/simulation-service';
import { setupModelInput, setupDatasetInput } from '@/services/calibrate-workflow';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { workflowEventBus } from '@/services/workflow';
import _ from 'lodash';
import {
	CalibrationOperationCiemss,
	CalibrationOperationStateCiemss,
	CalibrateMap
} from './calibrate-operation-ciemss';
import TeraSimulateChart from './tera-simulate-chart.vue';

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
const parameterResult = ref<{ [index: string]: any }>();

const datasetColumnNames = ref<string[]>();
const modelColumnNames = ref<string[] | undefined>();
const runResults = ref<RunResults>({});
const simulationIds: ComputedRef<any | undefined> = computed(
	<any | undefined>(() => props.node.outputs[0]?.value)
);

const mapping = ref<CalibrateMap[]>(props.node.state.mapping);
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);
const showSpinner = ref(false);

// EXTRA section
const numSamples = ref(100);
const numIterations = ref(100);
const method = ref('dopri5');
const ciemssMethodOptions = ref(['dopri5', 'euler']);

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
	// If the user has done any mapping populate formattedMap
	if (mapping.value[0].datasetVariable !== '') {
		mapping.value.forEach((ele) => {
			formattedMap[ele.datasetVariable] = ele.modelVariable;
		});
	}
	// TODO: TS/1225 -> Should not have to rand results
	const initials = modelConfig.value.configuration.semantics.ode.initials.map((d) => d.target);
	const rates = modelConfig.value.configuration.semantics.ode.rates.map((d) => d.target);
	const initialsObj = {};
	const paramsObj = {};

	const state = props.node.state;

	initials.forEach((d) => {
		initialsObj[d] = Math.random() * 100;
	});
	rates.forEach((d) => {
		paramsObj[d] = Math.random() * 0.05;
	});

	const calibrationRequest: CalibrationRequestCiemss = {
		modelConfigId: modelConfigId.value,
		dataset: {
			id: datasetId.value,
			filename: currentDatasetFileName.value,
			mappings: formattedMap
		},
		extra: {
			num_samples: numSamples.value,
			num_iterations: numIterations.value,
			method: method.value
		},
		timespan: {
			start: state.timeSpan.start,
			end: state.timeSpan.end
		},
		engine: 'ciemss'
	};
	const response = await makeCalibrateJobCiemss(calibrationRequest);

	startedRunId.value = response.simulationId;
	getStatus();
	showSpinner.value = true;
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
		showSpinner.value = false;
	} else if (currentSimulation && ongoingStatusList.includes(currentSimulation.status)) {
		// recursively call until all runs retrieved
		setTimeout(getStatus, 3000);
	} else {
		// throw if there are any failed runs for now
		console.error('Failed', startedRunId.value);
		showSpinner.value = false;
		throw Error('Failed Runs');
	}
};

const updateOutputPorts = async (runId) => {
	const portLabel = props.node.inputs[0].label;
	emit('append-output-port', {
		type: CalibrationOperationCiemss.outputs[0].type,
		label: `${portLabel} Result`,
		value: {
			runId
		}
	});
};

// Tom TODO: Make this generic, its copy paste from drilldown
const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state: CalibrationOperationStateCiemss = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const addChart = () => {
	const state: CalibrationOperationStateCiemss = _.cloneDeep(props.node.state);
	state.chartConfigs.push(_.last(state.chartConfigs) as ChartConfig);

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

// Set up model config + dropdown names
// Note: Same as calibrate side panel
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

// Fetch simulation run results whenever output changes
watch(
	() => simulationIds.value,
	async () => {
		if (!simulationIds.value) return;
		// const resultCsv = await getRunResult(simulationIds.value[0].runId, 'simulation.csv');
		// const csvData = csvParse(resultCsv);
		// runResults.value[simulationIds.value[0].runId] = csvData as any;
		// parameterResult.value = await getRunResult(simulationIds.value[0].runId, 'parameters.json');

		const output = await getRunResultCiemss(simulationIds.value[0].runId, 'simulation.csv');
		runResults.value = output.runResults;
	},
	{ immediate: true }
);
</script>

<style scoped>
.emptyState {
	align-self: center;
	display: flex;
	flex-direction: column;
	align-items: center;
	text-align: center;
	margin-bottom: 1rem;
	gap: 0.5rem;
}
.helpMessage {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
	width: 90%;
}
img {
	width: 20%;
}

th {
	text-align: left;
}
.column-header {
	color: var(--text-color-primary);
	font-size: var(--font-caption);
	font-weight: var(--font-semibold);
}
.mappingVariable {
	font-size: var(--font-caption);
}
.unmappedVariable {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
}
.extras {
	display: flex;
	flex-direction: row;
	gap: 1rem;
	margin-bottom: 1rem;
}

.extras-label {
	font-size: var(--font-caption);
}
#numSamples:deep(.p-inputnumber-input),
#numIterations:deep(.p-inputnumber-input),
#method:deep(.p-dropdown-label) {
	width: 100%;
	padding: 0.75rem;
}
.run-button {
	margin-top: 1rem;
	margin-bottom: 0.5rem;
	width: 5rem;
	float: right;
}
</style>
