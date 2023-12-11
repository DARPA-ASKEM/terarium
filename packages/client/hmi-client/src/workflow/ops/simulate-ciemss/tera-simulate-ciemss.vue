<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section>
			<section class="tera-simulate" stretch-content>
				<div class="simulate-header">
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
				</div>
				<div v-if="view === SimulateView.Output && node?.outputs.length" class="simulate-container">
					<Dropdown
						v-if="runList.length > 0"
						:options="runList"
						v-model="selectedRun"
						option-label="label"
						placeholder="Select a simulation run"
						@update:model-value="handleSelectedRunChange"
					/>
					<template v-if="runResults[selectedRun?.runId]">
						<ul class="metadata-container">
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
						</ul>
						<tera-simulate-chart
							v-for="(cfg, idx) in node.state.simConfigs.chartConfigs"
							:key="idx"
							:run-results="runResults[selectedRun.runId]"
							:chartConfig="{ selectedRun: selectedRun.runId, selectedVariable: cfg }"
							has-mean-line
							@configuration-change="configurationChange(idx, $event)"
						/>
						<Button
							class="add-chart"
							text
							:outlined="true"
							@click="addChart"
							label="Add chart"
							icon="pi pi-plus"
						>
						</Button>
						<tera-dataset-datatable
							v-if="rawContent[selectedRun?.runId]"
							:rows="10"
							:raw-content="rawContent[selectedRun.runId]"
						/>
						<Button
							class="add-chart"
							title="Saves the current version of the model as a new Terarium asset"
							@click="showSaveInput = !showSaveInput"
						>
							<span class="pi pi-save p-button-icon p-button-icon-left"></span>
							<span class="p-button-text">Save as</span>
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
					</template>
				</div>
				<div v-else-if="view === SimulateView.Input && node" class="simulate-container">
					<div class="simulate-model">
						<Accordion :multiple="true" :active-index="[0, 1, 2]">
							<AccordionTab>
								<template #header> Model </template>
								<tera-model-diagram
									v-if="model[selectedRun?.runId]"
									:model="model[selectedRun.runId]!"
									:is-editable="false"
								/>
							</AccordionTab>
							<AccordionTab>
								<template #header> Model configuration </template>
								<tera-model-configurations
									v-if="model[selectedRun?.runId]"
									:model="model[selectedRun.runId]!"
									:model-configurations="modelConfigurations"
									:feature-config="{ isPreview: true }"
								/>
							</AccordionTab>
							<AccordionTab>
								<template #header> Simulation time range </template>
								<div class="sim-tspan-container">
									<div class="sim-tspan-group">
										<label for="2">Start date</label>
										<InputNumber
											id="2"
											class="p-inputtext-sm"
											v-model="timespan.start"
											inputId="integeronly"
										/>
									</div>
									<div class="sim-tspan-group">
										<label for="3">End date</label>
										<InputNumber
											id="3"
											class="p-inputtext-sm"
											v-model="timespan.end"
											inputId="integeronly"
										/>
									</div>
								</div>
							</AccordionTab>
							<AccordionTab>
								<template #header> Other options </template>
								<div class="sim-tspan-container">
									<div class="sim-tspan-group">
										<label for="4">Number of stochastic samples</label>
										<InputNumber
											id="4"
											class="p-inputtext-sm"
											v-model="numSamples"
											inputId="integeronly"
											:min="2"
										/>
									</div>
								</div>
							</AccordionTab>
						</Accordion>
					</div>
				</div>
			</section>
		</section>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, onMounted, computed, watch } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import InputNumber from 'primevue/inputnumber';
import { CsvAsset, Model, TimeSpan, ModelConfiguration } from '@/types/Types';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { getModel, getModelConfigurations } from '@/services/model';
import { getModelConfigurationById } from '@/services/model-configurations';
import { getRunResultCiemss } from '@/services/models/simulation-service';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraModelConfigurations from '@/components/model/petrinet/tera-model-configurations.vue';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import { WorkflowNode } from '@/types/workflow';
import { saveDataset, createCsvAssetFromRunResults } from '@/services/dataset';
import InputText from 'primevue/inputtext';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import { useProjects } from '@/composables/project';
import SelectButton from 'primevue/selectbutton';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import { SimulateCiemssOperationState } from './simulate-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateCiemssOperationState>;
}>();
const emit = defineEmits(['append-output-port', 'update-state', 'close']);

const hasValidDatasetName = computed<boolean>(() => saveAsName.value !== '');

const timespan = ref<TimeSpan>(props.node.state.currentTimespan);
const numSamples = ref<number>(props.node.state.numSamples);

enum SimulateView {
	Input = 'Input',
	Output = 'Output'
}

const view = ref(SimulateView.Input);
const viewOptions = ref([
	{ value: SimulateView.Input, icon: 'pi pi-sign-in' },
	{ value: SimulateView.Output, icon: 'pi pi-sign-out' }
]);

const model = ref<{ [runId: string]: Model | null }>({});
const modelConfigurations = ref<ModelConfiguration[]>([]);
const runResults = ref<{ [runId: string]: RunResults }>({});
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

onMounted(() => {
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

watch(
	() => numSamples.value,
	(n) => {
		const state = _.cloneDeep(props.node.state);
		state.numSamples = n;
		emit('update-state', state);
	}
);
</script>

<style scoped>
.add-chart {
	width: 9em;
	margin: 0em 1em;
	margin-bottom: 1em;
}

.tera-simulate {
	background: white;
	z-index: 1;
}

.simulate-header {
	display: flex;
	margin: 0.5rem;
}

.simulate-header-label {
	display: flex;
	align-items: center;
	font-weight: var(--font-weight-semibold);
	font-size: 20px;
	margin-right: 1rem;
}

.simulate-container {
	height: calc(100vh - 150px);
	overflow-y: scroll;
}

.simulate-chart {
	margin: 2em 1.5em;
}

.sim-tspan-container {
	display: flex;
	gap: 1em;
}

.sim-tspan-group {
	display: flex;
	flex-direction: column;
	flex-grow: 1;
	flex-basis: 0;
}

::v-deep .p-inputnumber-input,
.p-inputwrapper {
	width: 100%;
}

.datatable-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin: 0.5em;
	position: relative;
}

.datatable-header-select-container {
	min-width: 0;
}

.datatable-header-title {
	white-space: nowrap;
	margin-right: 1em;
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
</style>
