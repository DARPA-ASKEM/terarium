<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section :tabName="SimulateTabs.Wizard">
			<tera-drilldown-section>
				<div class="form-section">
					<h4>Set simulation parameters</h4>
					<div class="input-row">
						<div class="label-and-input">
							<label for="2">Start time</label>
							<InputNumber
								id="2"
								class="p-inputtext-sm"
								v-model="timespan.start"
								inputId="integeronly"
							/>
						</div>
						<div class="label-and-input">
							<label for="3">End time</label>
							<InputNumber
								id="3"
								class="p-inputtext-sm"
								v-model="timespan.end"
								inputId="integeronly"
							/>
						</div>
					</div>
				</div>
			</tera-drilldown-section>
		</section>
		<section :tabName="SimulateTabs.Notebook">
			<h4>Notebook</h4>
		</section>
		<template #preview>
			<tera-drilldown-preview
				title="Simulation output"
				:options="outputs"
				v-model:output="selectedOutputId"
				@update:output="onUpdateOutput"
				@update:selection="onUpdateSelection"
				:is-loading="showSpinner"
				is-selectable
			>
				<SelectButton
					:model-value="view"
					@change="if ($event.value) view = $event.value;"
					:options="viewOptions"
					option-value="value"
				>
					<template #option="{ option }">
						<i :class="`${option.icon} p-button-icon-left`" />
						<span class="p-button-label">{{ option.value }}</span>
					</template>
				</SelectButton>
				<template v-if="runResults[selectedRunId]">
					<div v-if="view === OutputView.Charts">
						<tera-simulate-chart
							v-for="(cfg, idx) in node.state.chartConfigs"
							:key="idx"
							:run-results="{ [selectedRunId]: runResults[selectedRunId] }"
							:chartConfig="{ selectedRun: selectedRunId, selectedVariable: cfg }"
							@configuration-change="configurationChange(idx, $event)"
							color-by-run
						/>
						<Button
							class="p-button-sm p-button-text"
							@click="addChart"
							label="Add chart"
							icon="pi pi-plus"
						/>
					</div>
					<div v-else-if="view === OutputView.Data">
						<tera-dataset-datatable
							v-if="rawContent[selectedRunId]"
							:rows="10"
							:raw-content="rawContent[selectedRunId]"
						/>
					</div>
				</template>
			</tera-drilldown-preview>
		</template>
		<template #footer>
			<Button
				outlined
				:style="{ marginRight: 'auto' }"
				label="Run"
				icon="pi pi-play"
				@click="runSimulate"
				:disabled="showSpinner"
			/>
			<Button
				outlined
				title="Saves the current version of the model as a new Terarium asset"
				@click="showSaveInput = !showSaveInput"
			>
				<span class="pi pi-save p-button-icon p-button-icon-left"></span>
				<span class="p-button-text">Save as new dataset</span>
			</Button>
			<span v-if="showSaveInput" style="padding-left: 1em; padding-right: 2em">
				<InputText v-model="saveAsName" placeholder="New dataset name" />
				<i
					class="pi pi-times i"
					:class="{ clear: hasValidDatasetName }"
					@click="saveAsName = ''"
				></i>
				<i
					v-if="useProjects().activeProject.value?.id"
					class="pi pi-check i"
					:class="{ save: hasValidDatasetName }"
					@click="saveDatasetToProject"
				></i>
			</span>
			<Button label="Close" @click="emit('close')" />
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ref, onMounted, onUnmounted, watch } from 'vue';
import Button from 'primevue/button';
import InputNumber from 'primevue/inputnumber';
import { CsvAsset, Model, ModelConfiguration, SimulationRequest, TimeSpan } from '@/types/Types';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';

import { getModelConfigurationById } from '@/services/model-configurations';
import { Poller, PollerState } from '@/api/api';

import {
	getRunResult,
	getSimulation,
	makeForecastJob,
	simulationPollAction,
	querySimulationInProgress
} from '@/services/models/simulation-service';
import { getModel } from '@/services/model';
import { saveDataset, createCsvAssetFromRunResults } from '@/services/dataset';
import { csvParse } from 'd3';
import { ProgressState, WorkflowNode } from '@/types/workflow';
import InputText from 'primevue/inputtext';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import { useProjects } from '@/composables/project';
import SelectButton from 'primevue/selectbutton';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import { logger } from '@/utils/logger';
import { SimulateJuliaOperation, SimulateJuliaOperationState } from './simulate-julia-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateJuliaOperationState>;
}>();
const emit = defineEmits([
	'append-output-port',
	'update-state',
	'select-output',
	'update-output-port',
	'close'
]);

const timespan = ref<TimeSpan>(props.node.state.currentTimespan);

enum SimulateTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

enum OutputView {
	Charts = 'Charts',
	Data = 'Data'
}

const view = ref(OutputView.Charts);
const viewOptions = ref([
	{ value: OutputView.Charts, icon: 'pi pi-image' },
	{ value: OutputView.Data, icon: 'pi pi-list' }
]);

const model = ref<{ [runId: string]: Model | null }>({});
const modelConfigurations = ref<{ [runId: string]: ModelConfiguration | null }>({});
const hasValidDatasetName = computed<boolean>(() => saveAsName.value !== '');

const showSpinner = ref(false);
const completedRunId = ref<string>('');
const runResults = ref<RunResults>({});
const progress = ref({ status: ProgressState.RETRIEVING, value: 0 });

const showSaveInput = ref(<boolean>false);
const saveAsName = ref(<string | null>'');
const rawContent = ref<{ [runId: string]: CsvAsset | null }>({});

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
const selectedRunId = computed(
	() => props.node.outputs.find((o) => o.id === selectedOutputId.value)?.value?.[0]
);

const poller = new Poller();

onMounted(() => {
	const runIds = querySimulationInProgress(props.node);
	if (runIds.length === 1) {
		// there should only be one run happening at a time
		getStatus(runIds[0]);
	}
});

onUnmounted(() => {
	poller.stop();
});

const runSimulate = async () => {
	const modelConfigurationList = props.node.inputs[0].value;
	if (!modelConfigurationList?.length) return;

	// Since we've disabled multiple configs to a simulation node, we can assume only one config
	const configId = modelConfigurationList[0];

	const state = props.node.state;

	const payload: SimulationRequest = {
		modelConfigId: configId,
		timespan: {
			start: state.currentTimespan.start,
			end: state.currentTimespan.end
		},
		extra: {},
		engine: 'sciml'
	};
	const response = await makeForecastJob(payload);
	getStatus(response.id);
};

const getStatus = async (runId: string) => {
	showSpinner.value = true;
	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => simulationPollAction([runId], props.node, progress, emit));
	const pollerResults = await poller.start();

	if (pollerResults.state === PollerState.Cancelled) {
		showSpinner.value = false;
		return;
	}
	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		showSpinner.value = false;
		logger.error(`Simulate: ${runId} has failed`, {
			toastTitle: 'Error - Julia'
		});
		throw Error('Failed Runs');
	}

	completedRunId.value = runId;
	showSpinner.value = false;
};

const watchCompletedRunId = async (runId: string) => {
	if (!runId) return;

	const state = _.cloneDeep(props.node.state);
	if (state.chartConfigs.length === 0) {
		addChart();
	}

	const sim = await getSimulation(runId);

	emit('append-output-port', {
		type: SimulateJuliaOperation.outputs[0].type,
		label: `Output - ${new Date().toLocaleString()}`,
		value: runId,
		state: {
			currentTimespan: sim?.executionPayload.timespan ?? timespan.value,
			simulationsInProgress: state.simulationsInProgress
		},
		isSelected: false
	});
};

const lazyLoadSimulationData = async (runId: string) => {
	if (runResults.value[runId] && rawContent.value[runId]) return;

	// there's only a single input config
	const modelConfigId = props.node.inputs[0].value?.[0];
	const modelConfiguration = await getModelConfigurationById(modelConfigId);
	modelConfigurations.value[runId] = modelConfiguration;

	const resultCsv = await getRunResult(runId, 'result.csv');
	const csvData = csvParse(resultCsv);

	if (modelConfiguration) {
		model.value[runId] = await getModel(modelConfiguration.modelId);

		const parameters = modelConfiguration.configuration.semantics.ode.parameters;
		csvData.forEach((row) =>
			parameters.forEach((parameter) => {
				row[parameter.id] = parameter.value;
			})
		);
	}

	runResults.value[runId] = csvData as any;
	rawContent.value[runId] = createCsvAssetFromRunResults(runResults.value, runId);
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

const configurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config.selectedVariable;

	emit('update-state', state);
};

const addChart = () => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs.push([]);

	emit('update-state', state);
};

async function saveDatasetToProject() {
	const { activeProject, refresh } = useProjects();
	if (activeProject.value?.id) {
		if (await saveDataset(activeProject.value.id, selectedRunId.value, saveAsName.value)) {
			refresh();
		}
		showSaveInput.value = false;
	}
}

watch(() => completedRunId.value, watchCompletedRunId, { immediate: true });

watch(
	() => selectedRunId.value,
	() => {
		lazyLoadSimulationData(selectedRunId.value);
	},
	{ immediate: true }
);

watch(
	() => props.node.active,
	() => {
		// Update selected output
		if (props.node.active) {
			selectedOutputId.value = props.node.active;
		}

		// Update Wizard form fields with current selected output state timespan
		timespan.value = props.node.state.currentTimespan;
	},
	{ immediate: true }
);
</script>

<style scoped>
.simulate-chart {
	margin: 2em 1.5em;
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
