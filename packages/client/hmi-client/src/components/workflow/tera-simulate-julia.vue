<template>
	<section class="tera-simulate">
		<div class="simulate-header">
			<span class="simulate-header-label">Simulate (deterministic)</span>
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
					label="Ouput"
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
				@configuration-change="configurationChange(index, $event)"
				color-by-run
			/>
			<Button
				class="add-chart"
				text
				:outlined="true"
				@click="addChart"
				label="Add chart"
				icon="pi pi-plus"
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
					v-if="project?.id"
					class="pi pi-check i"
					:class="{ save: hasValidDatasetName }"
					@click="
						saveDataset(project.id, completedRunId, saveAsName);
						showSaveInput = false;
					"
				></i>
			</span>
		</div>
		<div v-else-if="activeTab === SimulateTabs.input && node" class="simulate-container">
			<div class="simulate-model">
				<Accordion :multiple="true" :active-index="[0, 1, 2]">
					<AccordionTab>
						<template #header> {{ modelConfiguration?.configuration.name }} </template>
						<model-diagram v-if="model" :model="model" :is-editable="false" />
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
				</Accordion>
			</div>
		</div>
	</section>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, onMounted, computed } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import InputNumber from 'primevue/inputnumber';
import { ModelConfiguration, Model, TimeSpan } from '@/types/Types';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';

import { getModelConfigurationById } from '@/services/model-configurations';
import ModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';

import { getSimulation, getRunResult } from '@/services/models/simulation-service';
import { getModel } from '@/services/model';
import { saveDataset } from '@/services/dataset';
import { csvParse } from 'd3';
import { WorkflowNode } from '@/types/workflow';
import { workflowEventBus } from '@/services/workflow';
import { IProject } from '@/types/Project';
import InputText from 'primevue/inputtext';
import { SimulateJuliaOperationState } from './simulate-julia-operation';
import TeraSimulateChart from './tera-simulate-chart.vue';

const props = defineProps<{
	node: WorkflowNode;
	project?: IProject;
}>();

const timespan = ref<TimeSpan>(props.node.state.currentTimespan);

enum SimulateTabs {
	input,
	output
}

const activeTab = ref(SimulateTabs.input);

const model = ref<Model | null>(null);
const runResults = ref<RunResults>({});
const modelConfiguration = ref<ModelConfiguration | null>(null);
const completedRunId = computed<string | undefined>(() => props?.node?.outputs?.[0]?.value?.[0]);
const hasValidDatasetName = computed<boolean>(() => saveAsName.value !== '');
const showSaveInput = ref(<boolean>false);
const saveAsName = ref(<string | null>'');

const configurationChange = (index: number, config: ChartConfig) => {
	const state: SimulateJuliaOperationState = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const addChart = () => {
	const state: SimulateJuliaOperationState = _.cloneDeep(props.node.state);
	state.chartConfigs.push(_.last(state.chartConfigs) as ChartConfig);

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

onMounted(async () => {
	// FIXME: Even though the input is a list of simulation ids, we will assume just a single model for now
	// e.g. just take the first one.
	if (!props.node) return;

	const nodeObj = props.node;

	if (!nodeObj.outputs[0]) return;
	const port = nodeObj.outputs[0];
	if (!port.value) return;
	const simulationId = port.value[0];

	const simulationObj = await getSimulation(simulationId as string);
	if (!simulationObj) return;

	const executionPayload = simulationObj.executionPayload;

	if (!executionPayload) return;

	const modelConfigurationId = (simulationObj.executionPayload as any).model_config_id;
	const modelConfigurationObj = await getModelConfigurationById(modelConfigurationId);
	const modelId = modelConfigurationObj.modelId;
	model.value = await getModel(modelId);

	// Fetch run results
	await Promise.all(
		port.value.map(async (runId) => {
			const resultCsv = await getRunResult(runId, 'result.csv');
			const csvData = csvParse(resultCsv);
			if (modelConfigurationObj) {
				const parameters = modelConfigurationObj.configuration.semantics.ode.parameters;
				csvData.forEach((row) =>
					parameters.forEach((parameter) => {
						row[parameter.id] = parameter.value;
					})
				);
			}
			runResults.value[runId] = csvData as any;
		})
	);
});
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
	margin: 2em 1em;
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
</style>
