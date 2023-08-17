<template>
	<section v-if="!showSpinner">
		<Accordion
			v-if="datasetColumnNames && modelColumnNames"
			:multiple="true"
			:active-index="[0, 3]"
		>
			<AccordionTab header="Mapping">
				<DataTable class="mappingTable" :value="mapping">
					<Column field="modelVariable">
						<template #header>
							<span class="column-header">Model variable</span>
						</template>
						<template #body="{ data, field }">
							<div class="mappingVariable">{{ data[field] }}</div>
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
				<span class="extras">
					<label>Num Chains</label>
					<InputNumber v-model="extra.numChains" />
					<label>num_iterations</label>
					<InputNumber v-model="extra.numIterations" />
					<label>odeMethod</label>
					<InputText v-model="extra.odeMethod" />
					<label>calibrate_method</label>
					<Dropdown
						:options="Object.values(CalibrateMethodOptions)"
						v-model="extra.calibrateMethod"
					/>
					<div class="smaller-buttons">
						<label>Start</label>
						<InputNumber v-model="timeSpan.start" />
						<label>End</label>
						<InputNumber v-model="timeSpan.end" />
					</div>
				</span>
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
import Column from 'primevue/column';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { CalibrationRequestJulia, CsvAsset, ModelConfiguration, TimeSpan } from '@/types/Types';
import {
	makeCalibrateJobJulia,
	getRunResultJulia,
	simulationPollAction,
	querySimulationInProgress,
	EventSourceManager
} from '@/services/models/simulation-service';
import { setupModelInput, setupDatasetInput } from '@/services/calibrate-workflow';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { csvParse } from 'd3';
import { workflowEventBus } from '@/services/workflow';
import _ from 'lodash';
import InputNumber from 'primevue/inputnumber';
import InputText from 'primevue/inputtext';
import Dropdown from 'primevue/dropdown';
import { Poller, PollerState } from '@/api/api';
import TeraSimulateChart from './tera-simulate-chart.vue';
import TeraProgressBar from './tera-progress-bar.vue';
import {
	CalibrationOperationJulia,
	CalibrationOperationStateJulia,
	CalibrateMap,
	CalibrateMethodOptions,
	CalibrateExtraJulia
} from './calibrate-operation-julia';

const props = defineProps<{
	node: WorkflowNode;
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
const extra = ref<CalibrateExtraJulia>(props.node.state.extra);
const timeSpan = ref<TimeSpan>(props.node.state.timeSpan);

const csvAsset = shallowRef<CsvAsset | undefined>(undefined);
const showSpinner = ref(false);
const progress = ref({ status: ProgressState.RETRIEVING, value: 0 });

const poller = new Poller();
const eventSourceManager = new EventSourceManager();

onMounted(() => {
	const runIds = querySimulationInProgress(props.node);
	if (runIds.length > 0) {
		getStatus(runIds[0]);
	}
});

onUnmounted(() => {
	poller.stop();
});

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

	initials.forEach((d) => {
		initialsObj[d] = Math.random() * 100;
	});
	rates.forEach((d) => {
		paramsObj[d] = Math.random() * 0.05;
	});

	const calibrationRequest: CalibrationRequestJulia = {
		modelConfigId: modelConfigId.value,
		dataset: {
			id: datasetId.value,
			filename: currentDatasetFileName.value,
			mappings: formattedMap
		},
		extra: extra.value,
		engine: 'sciml',
		timespan: timeSpan.value
	};
	const response = await makeCalibrateJobJulia(calibrationRequest);
	if (response?.simulationId) {
		getStatus(response.simulationId);
	}
};

const getStatus = async (simulationId: string) => {
	showSpinner.value = true;
	if (!simulationId) return;

	const runIds = [simulationId];
	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () =>
			simulationPollAction(runIds, props.node, progress, emit, eventSourceManager)
		);
	const pollerResults = await poller.start();

	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		console.error('Failed', simulationId);
		showSpinner.value = false;
		throw Error('Failed Runs');
	}
	completedRunId.value = simulationId;
	updateOutputPorts(completedRunId);
	showSpinner.value = false;
};

const updateOutputPorts = async (runId) => {
	const portLabel = props.node.inputs[0].label;
	emit('append-output-port', {
		type: CalibrationOperationJulia.outputs[0].type,
		label: `${portLabel} Result`,
		value: {
			runId
		}
	});
};

// Tom TODO: Make this generic, its copy paste from drilldown
const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state: CalibrationOperationStateJulia = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const addChart = () => {
	const state: CalibrationOperationStateJulia = _.cloneDeep(props.node.state);
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
		const resultCsv = (await getRunResultJulia(
			simulationIds.value[0].runId,
			'result.json'
		)) as string;
		const csvData = csvParse(resultCsv);
		runResults.value[simulationIds.value[0].runId] = csvData as any;
		// parameterResult.value = await getRunResult(simulationIds.value[0].runId, 'parameters.json');
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
.p-datatable:deep(td) {
	padding: 0.25rem 0rem !important;
}
.p-datatable:deep(th) {
	padding: 0.25rem 0rem !important;
}

.run-button {
	margin-top: 1rem;
	margin-bottom: 0.5rem;
	width: 5rem;
	float: right;
}
.extras {
	display: grid;
}
.smaller-buttons {
	max-width: 30%;
}
</style>
