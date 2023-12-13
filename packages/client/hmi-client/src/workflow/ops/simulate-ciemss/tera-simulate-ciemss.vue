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
						<div class="label-and-input">
							<label for="4">Number of samples</label>
							<InputNumber
								id="4"
								class="p-inputtext-sm"
								v-model="numSamples"
								inputId="integeronly"
								:min="2"
							/>
						</div>
						<div class="label-and-input">
							<label for="5">Method</label>
							<Dropdown
								id="5"
								class="p-inputtext-sm"
								v-model="method"
								:options="ciemssMethodOptions"
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
			<tera-drilldown-preview title="Simulation output">
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
				<Dropdown
					v-if="runList.length > 0"
					:options="runList"
					v-model="selectedRun"
					option-label="label"
					placeholder="Select a simulation run"
					@update:model-value="handleSelectedRunChange"
				/>
				<template v-if="runResults[selectedRun?.runId]">
					<div v-if="view === OutputView.Charts">
						<!-- <ul class="metadata-container">
							<li><span>Run ID:</span> {{ selectedRun.runId }}</li>
							<li>
								<span>Configuration name:</span>
								{{ node.state.simConfigs.runConfigs[selectedRun.runId].configName }}
							</li>
							<li>
								<span>Method selected:</span>
								{{ node.state.simConfigs.runConfigs[selectedRun.runId].method }}
							</li>
							<li>
								<span>Number of samples:</span>
								{{ node.state.simConfigs.runConfigs[selectedRun.runId].numSamples }}
							</li>
							<li>
								<span>Start step:</span>
								{{ node.state.simConfigs.runConfigs[selectedRun.runId].timeSpan?.start }}
								<span>End step:</span>
								{{ node.state.simConfigs.runConfigs[selectedRun.runId].timeSpan?.end }}
							</li>
						</ul> -->
						<tera-simulate-chart
							v-for="(cfg, idx) in node.state.simConfigs.chartConfigs"
							:key="idx"
							:run-results="runResults[selectedRun.runId]"
							:chartConfig="{ selectedRun: selectedRun.runId, selectedVariable: cfg }"
							has-mean-line
							@configuration-change="configurationChange(idx, $event)"
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
							v-if="rawContent[selectedRun?.runId]"
							:rows="10"
							:raw-content="rawContent[selectedRun.runId]"
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
				<InputText v-model="saveAsName" class="post-fix" placeholder="New dataset name" />
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
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import InputNumber from 'primevue/inputnumber';
import InputText from 'primevue/inputtext';
import { CsvAsset, Model, ModelConfiguration, SimulationRequest, TimeSpan } from '@/types/Types';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { getModel, getModelConfigurations } from '@/services/model';
import { getModelConfigurationById } from '@/services/model-configurations';
import {
	makeForecastJobCiemss as makeForecastJob,
	getRunResultCiemss,
	simulationPollAction,
	querySimulationInProgress
	// getSimulation
} from '@/services/models/simulation-service';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import { ProgressState, WorkflowNode } from '@/types/workflow';
import { saveDataset, createCsvAssetFromRunResults } from '@/services/dataset';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import { useProjects } from '@/composables/project';
import SelectButton from 'primevue/selectbutton';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import { Poller, PollerState } from '@/api/api';
// import TeraProgressBar from '@/workflow/tera-progress-bar.vue';
import { logger } from '@/utils/logger';
import {
	// SimulateCiemssOperation,
	SimulateCiemssOperationState
} from './simulate-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateCiemssOperationState>;
}>();
const emit = defineEmits(['append-output-port', 'update-state', 'close']);

const hasValidDatasetName = computed<boolean>(() => saveAsName.value !== '');

const timespan = ref<TimeSpan>(props.node.state.currentTimespan);
const numSamples = ref<number>(props.node.state.numSamples);
const method = ref(props.node.state.method);
const ciemssMethodOptions = ref(['dopri5', 'euler']);

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
const modelConfigurations = ref<ModelConfiguration[]>([]);

const showSpinner = ref(false);
const completedRunIdList = ref<string[]>([]);
const runResults = ref<{ [runId: string]: RunResults }>({});
const progress = ref({ status: ProgressState.RETRIEVING, value: 0 });

const showSaveInput = ref(<boolean>false);
const saveAsName = ref(<string | null>'');
const rawContent = ref<{ [runId: string]: CsvAsset | null }>({});

const runList = computed(() =>
	Object.keys(props.node.state.simConfigs.runConfigs).map((runId: string, idx: number) => ({
		label: `Output ${idx + 1} - ${runId}`,
		runId
	}))
);
const selectedRun = ref();

const poller = new Poller();

onMounted(() => {
	const runIds = querySimulationInProgress(props.node);
	if (runIds.length > 0) {
		getStatus(runIds);
	}

	const runId = Object.values(props.node.state.simConfigs.runConfigs).find(
		(metadata) => metadata.active
	)?.runId;
	if (runId) {
		selectedRun.value = runList.value.find((run) => run.runId === runId);
	} else {
		selectedRun.value = runList.value.length > 0 ? runList.value[0] : undefined;
	}

	if (selectedRun.value?.runId) {
		lazyLoadSimulationData(selectedRun.value.runId);
	}
});

onUnmounted(() => {
	poller.stop();
});

const runSimulate = async () => {
	const modelConfigurationList = props.node.inputs[0].value;
	if (!modelConfigurationList?.length) return;

	// Since we've disabled multiple configs to a simulation node, we can assume only one config
	const modelConfigId = modelConfigurationList[0];

	const state = props.node.state;

	const payload: SimulationRequest = {
		modelConfigId,
		timespan: {
			start: state.currentTimespan.start,
			end: state.currentTimespan.end
		},
		extra: {
			num_samples: state.numSamples,
			method: state.method
		},
		engine: 'ciemss'
	};
	const response = await makeForecastJob(payload);
	getStatus([response.id]);
};

const getStatus = async (runIds: string[]) => {
	showSpinner.value = true;
	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => simulationPollAction(runIds, props.node, progress, emit));
	const pollerResults = await poller.start();

	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		showSpinner.value = false;
		logger.error(`Simulation: ${runIds} has failed`, {
			toastTitle: 'Error - Pyciemss'
		});
		throw Error('Failed Runs');
	}
	completedRunIdList.value = runIds;
	showSpinner.value = false;
};

const configurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.simConfigs.chartConfigs[index] = config.selectedVariable;

	emit('update-state', state);
};

const handleSelectedRunChange = () => {
	if (!selectedRun.value) return;

	lazyLoadSimulationData(selectedRun.value.runId);

	const state = _.cloneDeep(props.node.state);
	// set the active status for the selected run in the run configs
	Object.keys(state.simConfigs.runConfigs).forEach((runId) => {
		state.simConfigs.runConfigs[runId].active = runId === selectedRun.value?.runId;
	});

	emit('update-state', state);
};

const addChart = () => {
	const state = _.cloneDeep(props.node.state);
	state.simConfigs.chartConfigs.push([]);

	emit('update-state', state);
};

async function saveDatasetToProject() {
	const { activeProject, refresh } = useProjects();
	if (activeProject.value?.id) {
		if (await saveDataset(activeProject.value.id, selectedRun.value.runId, saveAsName.value)) {
			refresh();
		}
		showSaveInput.value = false;
	}
}

const lazyLoadSimulationData = async (runId: string) => {
	if (runResults.value[runId]) return;

	// there's only a single input config
	const modelConfigId = props.node.inputs[0].value?.[0];
	const modelConfiguration = await getModelConfigurationById(modelConfigId);
	if (modelConfiguration) {
		model.value[runId] = await getModel(modelConfiguration.modelId);
		if (model.value[runId]) {
			modelConfigurations.value = await getModelConfigurations(model.value[runId]!.id);
		}
	}

	const output = await getRunResultCiemss(runId);
	runResults.value[runId] = output.runResults;
	rawContent.value[runId] = createCsvAssetFromRunResults(runResults.value[runId]);
};

watch(
	() => numSamples.value,
	(n) => {
		const state = _.cloneDeep(props.node.state);
		state.numSamples = n;
		emit('update-state', state);
	}
);

watch(
	() => method.value,
	() => {
		const state = _.cloneDeep(props.node.state);
		state.method = method.value;

		emit('update-state', state);
	}
);
</script>

<style scoped>
.simulate-chart {
	margin: 2em 1.5em;
}

.metadata-container {
	padding: 1rem;
	display: flex;
	flex-direction: column;
	gap: 1em;
	list-style: none;
}

li > span {
	font-weight: bold;
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
