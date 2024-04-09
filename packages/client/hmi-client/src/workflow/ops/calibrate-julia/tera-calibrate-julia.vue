<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<section :tabName="CalibrateTabs.Wizard" class="ml-4 mr-2 pt-3">
			<tera-drilldown-section>
				<div class="form-section">
					<h5>Mapping</h5>
					<DataTable class="mapping-table" :value="mapping">
						<Button
							class="p-button-sm p-button-text"
							label="Delete all mapping"
							@click="deleteAllMapping"
						/>
						<Column field="modelVariable">
							<template #header>
								<span class="column-header">Model variable</span>
							</template>
							<template #body="{ data, field }">
								<Dropdown
									class="w-full p-inputtext-sm"
									placeholder="Select a variable"
									v-model="data[field]"
									:options="modelStateOptions?.map((ele) => ele.id)"
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
									:options="datasetColumns?.map((ele) => ele.name)"
								/>
							</template>
						</Column>
						<Column field="deleteRow">
							<template #header>
								<span class="column-header"></span>
							</template>
							<template #body="{ index }">
								<Button
									class="p-button-sm p-button-text"
									label="Delete"
									@click="deleteMapRow(index)"
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
						<Button
							class="p-button-sm p-button-text"
							icon="pi pi-plus"
							label="Auto map"
							@click="getAutoMapping"
						/>
					</div>
				</div>
				<div class="form-section">
					<h5>Calibration settings</h5>
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
			<h5>Notebook</h5>
		</section>
		<template #preview>
			<tera-drilldown-preview
				title="Preview"
				:options="outputs"
				v-model:output="selectedOutputId"
				@update:selection="onSelection"
				is-selectable
				class="mr-4 ml-2 mt-3 mb-3"
			>
				<div class="form-section">
					<h5>Calibrated parameters</h5>
					<table class="p-datatable-table">
						<thead class="p-datatable-thead">
							<th>Parameter</th>
							<th>Value</th>
						</thead>
						<template v-if="inProgressSimulationId">
							<tr v-for="(content, key) in parameterResult" :key="key">
								<td>{{ key }}</td>
								<td>{{ content }}</td>
							</tr>
						</template>
						<template v-else-if="!inProgressSimulationId && selectedRunId">
							<tr v-for="(content, key) in runResultParams[selectedRunId]" :key="key">
								<td>{{ key }}</td>
								<td>{{ content }}</td>
							</tr>
						</template>
					</table>
				</div>
				<div class="form-section">
					<h5>Loss function</h5>
					<div v-if="inProgressSimulationId || selectedRunId" ref="lossPlot"></div>
				</div>
				<div class="form-section">
					<h5>Variables</h5>
					<div>
						<section v-if="selectedRunId && runResults[selectedRunId]" ref="outputPanel">
							<tera-simulate-chart
								v-for="(cfg, index) of node.state.chartConfigs"
								:key="index"
								:run-results="{ [selectedRunId]: runResults[selectedRunId] }"
								:initial-data="csvAsset"
								:mapping="mapping"
								:run-type="RunType.Julia"
								:chartConfig="{ selectedRun: selectedRunId, selectedVariable: cfg }"
								@configuration-change="chartProxy.configurationChange(index, $event)"
								@remove="chartProxy.removeChart(index)"
								show-remove-button
								:size="chartSize"
							/>
						</section>
						<Button
							class="p-button-sm p-button-text"
							@click="chartProxy.addChart"
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
import { computed, ref, shallowRef, watch } from 'vue';
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
	DatasetColumn,
	ModelConfiguration,
	ScimlStatusUpdate,
	State
} from '@/types/Types';
import {
	setupModelInput,
	setupDatasetInput,
	renderLossGraph,
	CalibrateMap
} from '@/services/calibrate-workflow';
import { autoCalibrationMapping } from '@/services/concept';
import { RunResults, RunType } from '@/types/SimulateConfig';
import { WorkflowNode } from '@/types/workflow';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';

import {
	getRunResultJulia,
	makeCalibrateJobJulia,
	subscribeToUpdateMessages,
	unsubscribeToUpdateMessages
} from '@/services/models/simulation-service';
import { csvParse } from 'd3';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';

import { getTimespan, chartActionsProxy, drilldownChartSize } from '@/workflow/util';
import { useToastService } from '@/services/toast';
import {
	CalibrateExtraJulia,
	CalibrateMethodOptions,
	CalibrationOperationStateJulia
} from './calibrate-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateJulia>;
}>();
const emit = defineEmits(['update-state', 'select-output', 'close']);
const toast = useToastService();

enum CalibrateTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

// Model variables checked in the model configuration will be options in the mapping dropdown
const modelStateOptions = ref<State[] | undefined>();
const datasetColumns = ref<DatasetColumn[]>();

const mapping = ref<CalibrateMap[]>(props.node.state.mapping);
const extra = ref<CalibrateExtraJulia>(props.node.state.extra);
const inProgressSimulationId = ref<string>(props.node.state.inProgressSimulationId);

const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

const modelConfig = ref<ModelConfiguration>();
const modelConfigId = computed<string | undefined>(() => props.node.inputs[0]?.value?.[0]);
const datasetId = computed<string | undefined>(() => props.node.inputs[1]?.value?.[0]);
const currentDatasetFileName = ref<string>();

const outputs = computed(() => {
	if (!_.isEmpty(props.node.outputs)) {
		return [
			{
				label: 'Select outputs to display in operator',
				items: props.node.outputs
			}
		];
	}
	return [];
});
const selectedOutputId = ref<string>();
const selectedRunId = ref<string>();

const lossPlot = ref<HTMLElement>();
let lossValues: { [key: string]: number }[] = [];

// refs to keep track of intermediate states and parameters
const parameterResult = ref<{ [index: string]: any }>();

const runResults = ref<RunResults>({});
const runResultParams = ref<Record<string, Record<string, number>>>({});

const outputPanel = ref(null);
const chartSize = computed(() => drilldownChartSize(outputPanel.value));

const chartProxy = chartActionsProxy(props.node, (state: CalibrationOperationStateJulia) => {
	emit('update-state', state);
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
	// Reset
	lossValues = [];

	const simulationId = await makeCalibrateRequest();
	const state = _.cloneDeep(props.node.state);
	state.inProgressSimulationId = simulationId;
	emit('update-state', state);
};

const makeCalibrateRequest = async () => {
	if (
		!modelConfigId.value ||
		!datasetId.value ||
		!currentDatasetFileName.value ||
		!modelConfig.value
	) {
		throw new Error('Insufficient information to run calibrate operation');
	}

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
		timespan: getTimespan({ dataset: csvAsset.value, mapping: mapping.value })
	};
	const response = await makeCalibrateJobJulia(calibrationRequest);
	return response.simulationId;
};

const messageHandler = (event: ClientEvent<ScimlStatusUpdate>) => {
	// if (runIds.includes(event.data.id)) {
	if (props.node.state.inProgressSimulationId === event.data.id) {
		const { iter, loss, params } = event.data;

		parameterResult.value = filterStateVars(params);

		lossValues.push({ iter, loss });
		if (lossPlot.value) {
			const width = lossPlot.value.offsetWidth;
			renderLossGraph(lossPlot.value, lossValues, { width, height: 150 });
		}
	}
};

const onSelection = (id: string) => {
	emit('select-output', id);
};

// Used from button to add new entry to the mapping object
const addMapping = () => {
	mapping.value.push({
		modelVariable: '',
		datasetVariable: ''
	});

	const state = _.cloneDeep(props.node.state);
	state.mapping = mapping.value;

	emit('update-state', state);
};

function deleteAllMapping() {
	mapping.value = [{ modelVariable: '', datasetVariable: '' }];

	const state = _.cloneDeep(props.node.state);
	state.mapping = mapping.value;

	emit('update-state', state);
}

function deleteMapRow(index: number) {
	mapping.value.splice(index, 1);
	const state = _.cloneDeep(props.node.state);
	state.mapping = mapping.value;

	emit('update-state', state);
}

const lazyLoadCalibrationData = async (runId: string) => {
	if (!runId || runResults.value[runId]) return;

	const result = await getRunResultJulia(runId, 'result.json');
	if (result) {
		const csvData = csvParse(result.csvData);
		runResults.value[runId] = csvData as any;
		runResultParams.value[runId] = result.paramVals;
	}
};

async function getAutoMapping() {
	if (!modelStateOptions.value) {
		toast.error('', 'No model states to map with');
		return;
	}
	if (!datasetColumns.value) {
		toast.error('', 'No dataset columns to map with');
		return;
	}
	mapping.value = (await autoCalibrationMapping(
		modelStateOptions.value,
		datasetColumns.value
	)) as CalibrateMap[];
	const state = _.cloneDeep(props.node.state);
	state.mapping = mapping.value;
	emit('update-state', state);
}

watch(
	() => props.node.state.inProgressSimulationId,
	(id) => {
		if (id === '') {
			unsubscribeToUpdateMessages([id], ClientEventType.SimulationSciml, messageHandler);
		} else {
			subscribeToUpdateMessages([id], ClientEventType.SimulationSciml, messageHandler);
		}
	}
);

watch(
	() => props.node.active,
	() => {
		const node = props.node;
		if (!node.active) return;

		selectedOutputId.value = node.active;
		selectedRunId.value = node.outputs.find((o) => o.id === selectedOutputId.value)?.value?.[0];

		if (!selectedRunId.value) return;
		lazyLoadCalibrationData(selectedRunId.value as string);

		const lossVals = node.state.intermediateLoss;
		if (lossVals && lossPlot) {
			const width = lossPlot.value?.offsetWidth as number;
			renderLossGraph(lossPlot.value as HTMLElement, lossVals, { width, height: 150 });
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
		const { modelConfiguration, modelOptions } = await setupModelInput(modelConfigId.value);
		modelConfig.value = modelConfiguration;
		modelStateOptions.value = modelOptions;
	},
	{ immediate: true }
);

// Set up csv + dropdown names
watch(
	() => datasetId.value,
	async () => {
		const { filename, csv, datasetOptions } = await setupDatasetInput(datasetId.value);
		currentDatasetFileName.value = filename;
		csvAsset.value = csv;
		datasetColumns.value = datasetOptions;
	},
	{ immediate: true }
);
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
