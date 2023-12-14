<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section :tabName="CalibrateTabs.Wizard">
			<tera-drilldown-section>
				<div class="form-section">
					<h4>Mapping</h4>
					<DataTable class="mapping-table" :value="mapping">
						<Column field="modelVariable">
							<template #header>
								<span class="column-header">Model variable</span>
							</template>
							<template #body="{ data, field }">
								<Dropdown
									class="w-full p-inputtext-sm"
									placeholder="Select a variable"
									v-model="data[field]"
									:options="modelColumnNames"
								/>
							</template>
						</Column>
						<Column field="datasetVariable">
							<template #header>
								<span class="column-header">Dataset variable</span>
							</template>
							<template #body="{ data, field }">
								<Dropdown
									class="w-full p-inputtext-sm"
									placeholder="Select a variable"
									v-model="data[field]"
									:options="datasetColumnNames"
								/>
							</template>
						</Column>
					</DataTable>
					<div>
						<Button
							class="p-button-sm p-button-text"
							icon="pi pi-plus"
							label="Add mapping"
							@click="addMapping"
						/>
					</div>
				</div>
				<div class="form-section">
					<h4>Calibration settings</h4>
					<div class="input-row">
						<div class="label-and-input">
							<label for="chains">Chains</label>
							<InputNumber
								class="p-inputtext-sm"
								inputId="integeronly"
								v-model="extra.numChains"
								@update:model-value="updateStateExtras"
							/>
						</div>
						<div class="label-and-input">
							<label for="iterations">Iterations</label>
							<InputNumber
								class="p-inputtext-sm"
								inputId="integeronly"
								v-model="extra.numIterations"
								@update:model-value="updateStateExtras"
							/>
						</div>
						<div class="label-and-input">
							<label for="ode-method">ODE method</label>
							<InputText
								class="p-inputtext-sm"
								v-model="extra.odeMethod"
								@update:model-value="updateStateExtras"
							/>
						</div>
						<div class="label-and-input">
							<label for="calibrate-method">Calibrate method</label>
							<Dropdown
								:options="Object.values(CalibrateMethodOptions)"
								v-model="extra.calibrateMethod"
								@update:model-value="updateStateExtras"
							/>
						</div>
					</div>
				</div>
			</tera-drilldown-section>
		</section>
		<section :tabName="CalibrateTabs.Notebook">
			<h4>Notebook</h4>
		</section>
		<template #preview>
			<tera-drilldown-preview
				title="Preview"
				:options="outputs"
				v-model:output="selectedOutputId"
				@update:output="onUpdateOutput"
				@update:selection="onUpdateSelection"
				is-selectable
			>
				<div class="form-section">
					<h4>Calibrated parameters</h4>
					<table class="p-datatable-table">
						<thead class="p-datatable-thead">
							<th>Parameter</th>
							<th>Value</th>
						</thead>
						<template v-if="runInProgress">
							<tr v-for="(content, key) in parameterResult" :key="key">
								<td>
									<p>{{ key }}</p>
								</td>
								<td>
									<p>{{ content }}</p>
								</td>
							</tr>
						</template>
						<template v-else-if="!runInProgress && selectedRunId">
							<tr v-for="(content, key) in runResultParams[selectedRunId]" :key="key">
								<td>
									<p>{{ key }}</p>
								</td>
								<td>
									<p>{{ content }}</p>
								</td>
							</tr>
						</template>
					</table>
				</div>
				<div class="form-section">
					<h4>Loss function</h4>
					<div v-if="runInProgress" ref="drilldownLossPlot"></div>
					<div v-else ref="staticLossPlotRef"></div>
				</div>
				<div class="form-section">
					<h4>Variables</h4>
					<div>
						<template v-if="runInProgress">
							<tera-calibrate-chart
								v-for="(cfg, index) of node.state.chartConfigs"
								:key="index"
								:initial-data="csvAsset"
								:intermediate-data="currentIntermediateVals"
								:mapping="mapping"
								:chartConfig="{ selectedRun: runInProgress, selectedVariable: cfg }"
								@configuration-change="chartConfigurationChange(index, $event)"
							/>
						</template>
						<template v-else-if="!runInProgress && selectedRunId && runResults[selectedRunId]">
							<tera-simulate-chart
								v-for="(cfg, index) of node.state.chartConfigs"
								:key="index"
								:run-results="{ [selectedRunId]: runResults[selectedRunId] }"
								:initial-data="csvAsset"
								:mapping="mapping"
								:run-type="RunType.Julia"
								:chartConfig="{ selectedRun: selectedRunId, selectedVariable: cfg }"
								@configuration-change="chartConfigurationChange(index, $event)"
							/>
						</template>
						<Button
							class="p-button-sm p-button-text"
							@click="addChart"
							label="Add chart"
							icon="pi pi-plus"
						></Button>
					</div>
				</div>
			</tera-drilldown-preview>
		</template>
		<template #footer>
			<Button
				outlined
				:style="{ marginRight: 'auto' }"
				label="Run"
				icon="pi pi-play"
				@click="runCalibrate"
				:disabled="disableRunButton"
			/>
			<Button label="Close" @click="emit('close')" />
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, onMounted, onUnmounted, ref, shallowRef, watch } from 'vue';
import Button from 'primevue/button';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import Dropdown from 'primevue/dropdown';
import InputNumber from 'primevue/inputnumber';
import InputText from 'primevue/inputtext';
import {
	CalibrationRequestJulia,
	ClientEvent,
	ClientEventType,
	CsvAsset,
	ModelConfiguration,
	ScimlStatusUpdate
} from '@/types/Types';
import { setupModelInput, setupDatasetInput, renderLossGraph } from '@/services/calibrate-workflow';
import { ChartConfig, RunResults, RunType } from '@/types/SimulateConfig';
import { ProgressState, WorkflowNode } from '@/types/workflow';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import TeraCalibrateChart from '@/workflow/tera-calibrate-chart.vue';
import {
	getRunResultJulia,
	makeCalibrateJobJulia,
	simulationPollAction,
	subscribeToUpdateMessages,
	querySimulationInProgress,
	unsubscribeToUpdateMessages
} from '@/services/models/simulation-service';
import { csvParse } from 'd3';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import { getTimespan } from '@/workflow/util';
import { Poller, PollerState } from '@/api/api';
import { logger } from '@/utils/logger';
import { getGroupedOutputs } from '@/services/workflow';
import {
	CalibrateExtraJulia,
	CalibrateMap,
	CalibrateMethodOptions,
	CalibrationOperationJulia,
	CalibrationOperationStateJulia
} from './calibrate-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateJulia>;
}>();
const emit = defineEmits([
	'append-output-port',
	'update-state',
	'select-output',
	'update-output-port',
	'close'
]);

enum CalibrateTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

// Model variables checked in the model configuration will be options in the mapping dropdown
const modelColumnNames = ref<string[] | undefined>();
const datasetColumnNames = ref<string[]>();

const mapping = ref<CalibrateMap[]>(props.node.state.mapping);
const extra = ref<CalibrateExtraJulia>(props.node.state.extra);

const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

const modelConfig = ref<ModelConfiguration>();
const modelConfigId = computed<string | undefined>(() => props.node.inputs[0]?.value?.[0]);
const datasetId = computed<string | undefined>(() => props.node.inputs[1]?.value?.[0]);
const currentDatasetFileName = ref<string>();

const outputs = computed(() =>
	getGroupedOutputs<CalibrationOperationStateJulia>(props.node, {
		unsaved: 'Select outputs to display in operator'
	})
);
const selectedOutputId = ref<string>();
const selectedRunId = computed(
	() => props.node.outputs.find((o) => o.id === selectedOutputId.value)?.value?.[0]
);

const drilldownLossPlot = ref<HTMLElement>();
const staticLossPlotRef = ref<HTMLElement>();
let lossValues: { [key: string]: number }[] = [];

// refs to keep track of intermediate states and parameters
const currentIntermediateVals = ref<{ [key: string]: any }>({ timesteps: [], solData: {} });
const parameterResult = ref<{ [index: string]: any }>();

const progress = ref({ status: ProgressState.RETRIEVING, value: 0 });
const runInProgress = ref<string>();

const completedRunIdList = ref<string[]>([]);
const runResults = ref<RunResults>({});
const runResultParams = ref<Record<string, Record<string, number>>>({});

const poller = new Poller();

onMounted(() => {
	const runIds = querySimulationInProgress(props.node);
	if (runIds.length > 0) {
		getStatus(runIds);
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

const updateStateExtras = () => {
	const state = _.cloneDeep(props.node.state);
	state.extra = extra.value;
	emit('update-state', state);
};

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

const getMessageHandler = (event: ClientEvent<ScimlStatusUpdate>) => {
	const runIds: string[] = querySimulationInProgress(props.node);
	if (runIds.length === 0) return;

	if (runIds.includes(event.data.id)) {
		const { iter, loss, params, solData, timesteps } = event.data;

		parameterResult.value = filterStateVars(params);

		lossValues.push({ iter, loss });
		if (drilldownLossPlot.value) {
			const width = drilldownLossPlot.value.offsetWidth;
			renderLossGraph(drilldownLossPlot.value, lossValues, { width, height: 150 });
		}

		if (iter % 100 === 0) {
			currentIntermediateVals.value = { timesteps, solData };
		}
	}
};

const getStatus = async (simulationIds: string[]) => {
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

	if (pollerResults.state === PollerState.Cancelled) {
		return;
	}
	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		logger.error(`Calibrate: ${simulationIds} has failed`, {
			toastTitle: 'Error - Julia'
		});
		throw Error('Failed Runs');
	}

	completedRunIdList.value = simulationIds;
	runInProgress.value = undefined;
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

	const state = _.cloneDeep(props.node.state);
	state.intermediateLoss = lossValues;

	emit('append-output-port', {
		type: CalibrationOperationJulia.outputs[0].type,
		label: 'Output',
		value: runIdList,
		isSelected: false,
		state: {
			extra: state.extra,
			simulationsInProgress: state.simulationsInProgress,
			intermediateLoss: state.intermediateLoss
		}
	});

	// clear out intermediate values for next run
	lossValues = [];
	parameterResult.value = {};
};

const onUpdateOutput = (id) => {
	emit('select-output', id);
};

const onUpdateSelection = (id) => {
	const outputPort = props.node.outputs?.find((port) => port.id === id);
	if (!outputPort) return;
	outputPort.isSelected = !outputPort?.isSelected;
	emit('update-output-port', outputPort);
};

const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config.selectedVariable;

	emit('update-state', state);
};

const addChart = () => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs.push([]);

	emit('update-state', state);
};

// Used from button to add new entry to the mapping object
function addMapping() {
	mapping.value.push({
		modelVariable: '',
		datasetVariable: ''
	});

	const state = _.cloneDeep(props.node.state);
	state.mapping = mapping.value;

	emit('update-state', state);
}

watch(
	() => props.node.active,
	() => {
		// Update selected output
		if (props.node.active) {
			selectedOutputId.value = props.node.active;
		}
		// Update Wizard form fields with current selected output state extras
		extra.value = props.node.state.extra;
	},
	{ immediate: true }
);

// Set up model config + dropdown names
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

const lazyLoadCalibrationData = async (runId?: string) => {
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
		lazyLoadCalibrationData(selectedRunId.value);
	},
	{ immediate: true }
);

// Plot loss values if available on mount or on selectedRun change
watch([() => selectedRunId.value, () => staticLossPlotRef.value], () => {
	if (selectedRunId.value) {
		const lossVals = props.node.state.intermediateLoss;
		if (lossVals && staticLossPlotRef.value) {
			const width = staticLossPlotRef.value.offsetWidth;
			renderLossGraph(staticLossPlotRef.value, lossVals, { width, height: 300 });
		}
	}
});
</script>

<style scoped>
.mapping-table:deep(td) {
	padding: 0rem 0.25rem 0.5rem 0rem !important;
	border: none !important;
}

.mapping-table:deep(th) {
	padding: 0rem 0.25rem 0.5rem 0.25rem !important;
	border: none !important;
	width: 50%;
}

th {
	text-align: left;
}

.column-header {
	color: var(--text-color-primary);
	font-size: var(--font-body-small);
	font-weight: var(--font-weight-semibold);
}

.form-section {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

.label-and-input {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

.input-row {
	width: 100%;
	display: flex;
	flex-direction: row;
	flex-wrap: wrap;
	align-items: center;
	gap: 0.5rem;

	& > * {
		flex: 1;
	}
}
</style>
