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
					:initial-data="csvAsset"
					:mapping="mapping"
					:chartConfig="cfg"
					has-mean-line
					@configuration-change="chartConfigurationChange(index, $event)"
				/>
				<Button
					class="add-chart"
					text
					:outlined="true"
					@click="addChart"
					label="Add chart"
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
			v-if="modelConfigId && datasetId"
			class="p-button-sm run-button"
			label="Run"
			icon="pi pi-play"
			@click="runCalibrate"
			:disabled="disableRunButton"
		/>
	</section>
	<section v-else>
		<tera-progress-bar :value="progress.value" :status="progress.status" />
	</section>
</template>

<script setup lang="ts">
import { computed, shallowRef, watch, ref, ComputedRef, onMounted, onUnmounted } from 'vue';
import { ProgressState, WorkflowNode } from '@/types/workflow';
import DataTable from 'primevue/datatable';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import Column from 'primevue/column';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import InputNumber from 'primevue/inputnumber';
import {
	CalibrationRequestCiemss,
	ClientEvent,
	ClientEventType,
	CsvAsset,
	ModelConfiguration
} from '@/types/Types';
import {
	makeCalibrateJobCiemss,
	getRunResultCiemss,
	simulationPollAction,
	querySimulationInProgress
} from '@/services/models/simulation-service';
import { setupModelInput, setupDatasetInput, CalibrateMap } from '@/services/calibrate-workflow';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import _ from 'lodash';
import { Poller, PollerState } from '@/api/api';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import TeraProgressBar from '@/workflow/tera-progress-bar.vue';
import { getTimespan } from '@/workflow/util';
import { logger } from '@/utils/logger';
import { subscribe, unsubscribe } from '@/services/ClientEventService';
import { CalibrationOperationCiemss, CalibrationOperationStateCiemss } from './calibrate-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateCiemss>;
}>();

const emit = defineEmits(['append-output-port', 'update-state']);

const modelConfigId = computed(() => props.node.inputs[0].value?.[0] as string | undefined);
const datasetId = computed(() => props.node.inputs[1].value?.[0] as string | undefined);
const currentDatasetFileName = ref<string>();
const modelConfig = ref<ModelConfiguration>();
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
const progress = ref({ status: ProgressState.RETRIEVING, value: 0 });

// EXTRA section
const numSamples = ref(100);
const numIterations = ref(100);
const method = ref('dopri5');
const ciemssMethodOptions = ref(['dopri5', 'euler']);

const poller = new Poller();

const disableRunButton = computed(
	() =>
		!currentDatasetFileName.value ||
		!modelConfig.value ||
		!csvAsset.value ||
		!modelConfigId.value ||
		!datasetId.value
);

onMounted(() => {
	const runIds = querySimulationInProgress(props.node);
	if (runIds.length > 0) {
		getStatus(runIds[0]);
	}
});

onUnmounted(() => {
	poller.stop();
});

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
		timespan: getTimespan(csvAsset.value, mapping.value),
		engine: 'ciemss'
	};
	const response = await makeCalibrateJobCiemss(calibrationRequest);

	if (response?.simulationId) {
		getStatus(response.simulationId);
	}
};

function getMessageHandler(event: ClientEvent<any>) {
	const runIds: string[] = querySimulationInProgress(props.node);
	if (runIds.length === 0) return;

	if (runIds.includes(event.data.id)) {
		// perform some action here
		console.log(`Event received for: ${event.data.id}`);
	}
}

const getStatus = async (simulationId: string) => {
	console.log('Getting status');
	showSpinner.value = true;
	if (!simulationId) {
		console.log('No sim id');
		return;
	}
	console.log(`Simulation Id:${simulationId}`);
	const runIds = [simulationId];

	// open a connection for each run id and handle the messages
	await subscribe(ClientEventType.SimulationPyciemss, getMessageHandler);

	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => simulationPollAction(runIds, props.node, progress, emit));
	console.log('Poller defined');
	const pollerResults = await poller.start();

	// closing event source connections
	await unsubscribe(ClientEventType.SimulationPyciemss, getMessageHandler);

	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		showSpinner.value = false;
		logger.error(`Calibrate: ${simulationId} has failed`, {
			toastTitle: 'Error - Pyciemss'
		});
		throw Error('Failed Runs');
	}
	completedRunId.value = simulationId;
	updateOutputPorts(completedRunId);
	showSpinner.value = false;
};

const updateOutputPorts = async (runId) => {
	const portLabel = props.node.inputs[0].label;
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs.push({
		selectedRun: runId,
		selectedVariable: []
	});
	emit('update-state', state);

	emit('append-output-port', {
		type: CalibrationOperationCiemss.outputs[0].type,
		label: `${portLabel} Result`,
		value: {
			runId
		}
	});
};

const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	emit('update-state', state);
};

const addChart = () => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs.push(_.last(state.chartConfigs) as ChartConfig);

	emit('update-state', state);
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

		const output = await getRunResultCiemss(simulationIds.value[0].runId, 'result.csv');
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
