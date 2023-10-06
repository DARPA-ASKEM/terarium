<template>
	<section class="tera-simulate">
		<div class="simulate-header">
			<div class="simulate-header p-buttonset">
				<Button
					label="Input"
					severity="secondary"
					icon="pi pi-sign-in"
					size="small"
					:active="activeTab === SimulateTabs.input"
					@click="activeTab = SimulateTabs.input"
				/>
				<Button
					label="Output"
					severity="secondary"
					icon="pi pi-sign-out"
					size="small"
					:active="activeTab === SimulateTabs.output"
					@click="activeTab = SimulateTabs.output"
				/>
			</div>
		</div>
		<div
			v-if="activeTab === SimulateTabs.output && node?.outputs.length"
			class="simulate-container"
		>
			<tera-simulate-chart
				v-for="(cfg, index) of node.state.chartConfigs"
				:key="index"
				:run-results="runResults"
				:chartConfig="cfg"
				has-mean-line
				@configuration-change="configurationChange(index, $event)"
			/>
			<Button
				class="add-chart"
				text
				:outlined="true"
				@click="addChart"
				label="Add chart"
				icon="pi pi-plus"
			/>
			<tera-dataset-datatable :rows="10" :raw-content="rawContent" />
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
		</div>
		<div v-else-if="activeTab === SimulateTabs.input && node" class="simulate-container">
			<div class="simulate-model">
				<Accordion :multiple="true" :active-index="[0, 1, 2]">
					<AccordionTab>
						<template #header> Model </template>
						<tera-model-diagram v-if="model" :model="model" :is-editable="false" />
					</AccordionTab>
					<AccordionTab>
						<template #header> Model configuration </template>
						<tera-model-configurations
							v-if="model"
							:model="model"
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
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, onMounted, computed, watch } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
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
import { workflowEventBus } from '@/services/workflow';
import { saveDataset, createCsvAssetFromRunResults } from '@/services/dataset';
import InputText from 'primevue/inputtext';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import { useProjects } from '@/composables/project';
import { SimulateCiemssOperationState } from './simulate-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateCiemssOperationState>;
}>();

const hasValidDatasetName = computed<boolean>(() => saveAsName.value !== '');

const timespan = ref<TimeSpan>(props.node.state.currentTimespan);
const numSamples = ref<number>(props.node.state.numSamples);

enum SimulateTabs {
	input,
	output
}

const activeTab = ref(SimulateTabs.input);

const model = ref<Model | null>(null);
const modelConfigurations = ref<ModelConfiguration[]>([]);
const parsedRawData = ref<any>();
const runConfigs = ref<any>({});
const runResults = ref<RunResults>({});
const showSaveInput = ref(<boolean>false);
const saveAsName = ref(<string | null>'');
const completedRunId = computed<string | undefined>(() => props?.node?.outputs?.[0]?.value?.[0]);
const rawContent = ref<CsvAsset | null>(null);

const configurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const addChart = () => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs.push(_.last(state.chartConfigs) as ChartConfig);

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

async function saveDatasetToProject() {
	const { activeProject, get } = useProjects();
	if (activeProject.value?.id) {
		if (await saveDataset(activeProject.value.id, completedRunId.value, saveAsName.value)) {
			get();
		}
		showSaveInput.value = false;
	}
}

onMounted(async () => {
	// FIXME: Even though the input is a list of simulation ids, we will assume just a single model for now
	// e.g. just take the first one.
	if (!props.node) return;

	const nodeObj = props.node;

	console.log(nodeObj);

	if (!nodeObj.outputs[0]) return;
	const port = nodeObj.outputs[0];
	if (!port.value) return;
	const simulationId = port.value[0];

	const modelConfigId = nodeObj.inputs[0].value?.[0];
	const modelConfiguration = await getModelConfigurationById(modelConfigId);
	if (modelConfiguration) {
		model.value = await getModel(modelConfiguration.modelId);
		if (model.value) modelConfigurations.value = await getModelConfigurations(model.value.id);
	}

	const output = await getRunResultCiemss(simulationId);
	parsedRawData.value = output.parsedRawData;
	runResults.value = output.runResults;
	runConfigs.value = output.runConfigs;

	rawContent.value = createCsvAssetFromRunResults(runResults.value);
});

watch(
	() => numSamples.value,
	(n) => {
		const state = _.cloneDeep(props.node.state);
		state.numSamples = n;
		workflowEventBus.emitNodeStateChange({
			workflowId: props.node.workflowId,
			nodeId: props.node.id,
			state
		});
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
</style>
