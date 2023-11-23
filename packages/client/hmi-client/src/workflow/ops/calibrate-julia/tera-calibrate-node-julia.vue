<template>
	<section v-if="!showSpinner">
		<Accordion
			v-if="datasetColumnNames && modelColumnNames"
			:multiple="true"
			:active-index="[1, 2, 3]"
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
			<AccordionTab header="Loss">
				<div ref="staticLossPlotRef"></div>
			</AccordionTab>
			<AccordionTab header="Calibrated parameter values">
				<table class="p-datatable-table" v-if="selectedRunId">
					<thead class="p-datatable-thead">
						<th>Parameter</th>
						<th>Value</th>
					</thead>
					<tr v-for="(content, key) in runResultParams[selectedRunId]" :key="key">
						<td>
							<p>{{ key }}</p>
						</td>
						<td>
							<p>{{ content }}</p>
						</td>
					</tr>
				</table>
			</AccordionTab>
			<AccordionTab header="Variables">
				<div v-if="selectedRunId && runResults[selectedRunId]">
					<tera-simulate-chart
						v-for="(cfg, index) of node.state.calibrateConfigs.chartConfigs"
						:key="index"
						:run-results="{ [selectedRunId]: runResults[selectedRunId] }"
						:initial-data="csvAsset"
						:mapping="mapping"
						:run-type="RunType.Julia"
						:chartConfig="{ selectedRun: selectedRunId, selectedVariable: cfg }"
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
				</div>
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
				</span>
			</AccordionTab>
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
		<!-- TODO: cleanup duplicate code below -->
		<Accordion
			v-if="datasetColumnNames && modelColumnNames"
			:multiple="true"
			:active-index="[1, 2, 3]"
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
			<AccordionTab header="Loss">
				<div ref="lossPlotRef"></div>
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
			<AccordionTab header="Variables">
				<tera-calibrate-chart
					v-for="(cfg, index) of node.state.calibrateConfigs.chartConfigs"
					:key="index"
					:initial-data="csvAsset"
					:intermediate-data="currentIntermediateVals"
					:mapping="mapping"
					:chartConfig="{ selectedRun: runInProgress!, selectedVariable: cfg }"
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
		</Accordion>
	</section>
</template>

<script setup lang="ts">
import { computed, shallowRef, watch, ref, onMounted, onUnmounted } from 'vue';
import { ProgressState, WorkflowNode } from '@/types/workflow';
import DataTable from 'primevue/datatable';
import Button from 'primevue/button';
import Column from 'primevue/column';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import {
	CalibrationRequestJulia,
	ClientEvent,
	ClientEventType,
	CsvAsset,
	ModelConfiguration,
	ScimlStatusUpdate
} from '@/types/Types';
import {
	makeCalibrateJobJulia,
	getRunResultJulia,
	simulationPollAction,
	querySimulationInProgress,
	subscribeToUpdateMessages,
	unsubscribeToUpdateMessages
} from '@/services/models/simulation-service';
import { setupModelInput, setupDatasetInput, renderLossGraph } from '@/services/calibrate-workflow';
import { ChartConfig, RunResults, RunType } from '@/types/SimulateConfig';
import { csvParse } from 'd3';
import _ from 'lodash';
import InputNumber from 'primevue/inputnumber';
import InputText from 'primevue/inputtext';
import Dropdown from 'primevue/dropdown';
import { Poller, PollerState } from '@/api/api';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import TeraCalibrateChart from '@/workflow/tera-calibrate-chart.vue';
import TeraProgressBar from '@/workflow/tera-progress-bar.vue';
import { getTimespan } from '@/workflow/util';
import { logger } from '@/utils/logger';
import {
	CalibrationOperationJulia,
	CalibrationOperationStateJulia,
	CalibrateMap,
	CalibrateMethodOptions,
	CalibrateExtraJulia
} from './calibrate-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateJulia>;
}>();

const emit = defineEmits(['append-output-port', 'update-state']);

const modelConfigId = computed(() => props.node.inputs[0].value?.[0] as string | undefined);
const datasetId = computed(() => props.node.inputs[1].value?.[0] as string | undefined);
const currentDatasetFileName = ref<string>();
const modelConfig = ref<ModelConfiguration>();
const completedRunIdList = ref<string[]>([]);
const parameterResult = ref<{ [index: string]: any }>();

const datasetColumnNames = ref<string[]>();
const modelColumnNames = ref<string[] | undefined>();
const runResults = ref<RunResults>({});
const runResultParams = ref<Record<string, Record<string, number>>>({});

const mapping = ref<CalibrateMap[]>(props.node.state.mapping);
const extra = ref<CalibrateExtraJulia>(props.node.state.extra);

const csvAsset = shallowRef<CsvAsset | undefined>(undefined);
const showSpinner = ref(false);
const progress = ref({ status: ProgressState.RETRIEVING, value: 0 });

const runList = computed(() =>
	Object.keys(props.node.state.calibrateConfigs.runConfigs).map((runId: string, idx: number) => ({
		label: `Output ${idx + 1} - ${runId}`,
		runId
	}))
);
const selectedRun = ref(); // used to select a run from the dropdown for this component
const selectedRunId = computed(
	() =>
		// if selected run changes from the drilldown component, then it should change in this component as well
		Object.values(props.node.state.calibrateConfigs.runConfigs).find((metadata) => metadata.active)
			?.runId
);
const runInProgress = ref<string>();

const lossPlotRef = ref<HTMLElement>();
const staticLossPlotRef = ref<HTMLElement>();
let lossValues: { [key: string]: number }[] = [];

const currentIntermediateVals = ref<{ [key: string]: any }>({ timesteps: [], solData: {} });

const poller = new Poller();

onMounted(() => {
	const runIds = querySimulationInProgress(props.node);
	if (runIds.length > 0) {
		getStatus(runIds);
	}

	const runId = Object.values(props.node.state.calibrateConfigs.runConfigs).find(
		(metadata) => metadata.active
	)?.runId;
	if (runId) {
		selectedRun.value = runList.value.find((run) => run.runId === runId);
	} else {
		selectedRun.value = runList.value.length > 0 ? runList.value[0] : undefined;
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

const filterStateVars = (params) => {
	const initialStates =
		modelConfig.value?.configuration.semantics.ode.initials.map((d) => d.expression) ?? [];
	return Object.keys(params).reduce((acc, key) => {
		if (!initialStates.includes(key)) {
			acc[key] = params[key];
		}
		return acc;
	}, {});
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
		timespan: getTimespan(csvAsset.value, mapping.value)
	};
	const response = await makeCalibrateJobJulia(calibrationRequest);
	if (response?.simulationId) {
		getStatus([response.simulationId]);
	}
};

function getMessageHandler(event: ClientEvent<ScimlStatusUpdate>) {
	const runIds: string[] = querySimulationInProgress(props.node);
	if (runIds.length === 0) return;

	if (runIds.includes(event.data.id)) {
		const { iter, loss, params, solData, timesteps } = event.data;

		parameterResult.value = filterStateVars(params);

		lossValues.push({ iter, loss });
		if (lossPlotRef.value) {
			const width = lossPlotRef.value.offsetWidth;
			renderLossGraph(lossPlotRef.value, lossValues, { width, height: 150 });
		}

		if (iter % 100 === 0) {
			currentIntermediateVals.value = { timesteps, solData };
		}
	}
}

const getStatus = async (simulationIds: string[]) => {
	showSpinner.value = true;
	runInProgress.value = simulationIds[0];

	await subscribeToUpdateMessages(
		simulationIds,
		ClientEventType.SimulationSciml,
		getMessageHandler
	);

	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => simulationPollAction(simulationIds, props.node, progress, emit));
	const pollerResults = await poller.start();

	await unsubscribeToUpdateMessages(
		simulationIds,
		ClientEventType.SimulationSciml,
		getMessageHandler
	);

	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		showSpinner.value = false;
		logger.error(`Calibrate: ${simulationIds} has failed`, {
			toastTitle: 'Error - Julia'
		});
		throw Error('Failed Runs');
	}
	completedRunIdList.value = simulationIds;

	runInProgress.value = undefined;
	showSpinner.value = false;
};

const watchCompletedRunList = async (runIdList: string[]) => {
	if (runIdList.length === 0) return;

	const newRunResults = {};
	const newRunResultParams = {};
	await Promise.all(
		runIdList.map(async (runId) => {
			if (runResults.value[runId] && runResultParams.value[runId]) {
				newRunResults[runId] = runResults.value[runId];
				newRunResultParams[runId] = runResultParams.value[runId];
			} else {
				const result = await getRunResultJulia(runId, 'result.json');
				if (result) {
					const csvData = csvParse(result.csvData);
					newRunResults[runId] = csvData;
					newRunResultParams[runId] = result.paramVals;
				}
			}
		})
	);
	runResults.value = newRunResults;
	runResultParams.value = newRunResultParams;

	const port = props.node.inputs[0];

	const state = _.cloneDeep(props.node.state);

	state.calibrateConfigs.runConfigs[runIdList[0]] = {
		runId: runIdList[0],
		active: true,
		loss: lossValues
	};
	emit('update-state', state);

	// clear out intermediate values for next run
	lossValues = [];
	parameterResult.value = {};

	emit('append-output-port', {
		type: CalibrationOperationJulia.outputs[0].type,
		label: `${port.label} - Output ${runList.value.length}`,
		value: runIdList
	});

	// show the latest run in the dropdown
	selectedRun.value = runList.value[runList.value.length - 1];
};

// Tom TODO: Make this generic, its copy paste from drilldown
const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.calibrateConfigs.chartConfigs[index] = config.selectedVariable;

	emit('update-state', state);
};

const addChart = () => {
	const state = _.cloneDeep(props.node.state);
	state.calibrateConfigs.chartConfigs.push([]);

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
watch(() => completedRunIdList.value, watchCompletedRunList, { immediate: true });

const handleSelectedRunChange = () => {
	if (!selectedRun.value) return;

	const state = _.cloneDeep(props.node.state);
	// set the active status for the selected run in the run configs
	Object.keys(state.calibrateConfigs.runConfigs).forEach((runId) => {
		state.calibrateConfigs.runConfigs[runId].active = runId === selectedRun.value.runId;
	});

	emit('update-state', state);
};
watch(() => selectedRun.value, handleSelectedRunChange, { immediate: true });

const lazyLoadRunResults = async (runId?: string) => {
	if (!runId || runResults.value[runId]) return;

	const result = await getRunResultJulia(runId, 'result.json');
	if (result) {
		const csvData = csvParse(result.csvData);
		runResults.value[runId] = csvData as any;
		runResultParams.value[runId] = result.paramVals;
	}
};
watch(
	() => selectedRunId.value,
	() => {
		lazyLoadRunResults(selectedRunId.value);
	},
	{ immediate: true }
);

// Plot loss values if available on mount or on selectedRun change
watch([() => selectedRunId.value, () => staticLossPlotRef.value], () => {
	if (selectedRunId.value) {
		const lossVals = props.node.state.calibrateConfigs.runConfigs[selectedRunId.value]?.loss;
		if (lossVals && staticLossPlotRef.value) {
			const width = staticLossPlotRef.value.offsetWidth;
			renderLossGraph(staticLossPlotRef.value, lossVals, { width, height: 150 });
		}
	}
});
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
