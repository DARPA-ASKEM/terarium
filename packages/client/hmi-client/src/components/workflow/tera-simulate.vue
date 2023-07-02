<template>
	<section class="tera-simulate">
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
			<span class="simulate-header-label">Simulate (deterministic)</span>
		</div>
		<div
			v-if="activeTab === SimulateTabs.output && node?.outputs.length"
			class="simulate-container"
		>
			<simulate-chart
				v-for="(cfg, index) of node.state.chartConfigs"
				:key="index"
				:run-results="runResults"
				:chartConfig="cfg"
			/>
			<!--
			<Button
				class="add-chart"
				text
				:outlined="true"
				@click="openedWorkflowNodeStore.appendChart"
				label="Add Chart"
				icon="pi pi-plus"
			></Button>
			-->
		</div>
		<div v-else-if="activeTab === SimulateTabs.input && node" class="simulate-container">
			<div class="simulate-model">
				<Accordion :multiple="true" :active-index="[0, 1, 2]">
					<AccordionTab>
						<template #header> {{ modelConfiguration?.configuration.name }} </template>
						<model-diagram v-if="model" :model="model" :is-editable="false" />
					</AccordionTab>
					<AccordionTab>
						<template #header> Simulation Time Range </template>
						<div class="sim-tspan-container">
							<!--
							<div class="sim-tspan-group">
								<label for="1">Units</label>
								<Dropdown
									id="1"
									class="p-inputtext-sm"
									v-model="openedWorkflowNodeStore.tspanUnit"
									:options="TspanUnitList"
								/>
							</div>
							-->
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
import { ref, onMounted } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
// import Column from 'primevue/column';
// import Row from 'primevue/row';
// import ColumnGroup from 'primevue/columngroup';
// import DataTable from 'primevue/datatable';
// import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import InputNumber from 'primevue/inputnumber';
import { ModelConfiguration, Model, TimeSpan } from '@/types/Types';
import { RunResults } from '@/types/SimulateConfig';

import { getModelConfigurationById } from '@/services/model-configurations';
import ModelDiagram from '@/components/models/tera-model-diagram.vue';

import { getSimulation, getRunResult } from '@/services/models/simulation-service';
import { getModel } from '@/services/model';
import { csvParse } from 'd3';
import { WorkflowNode } from '@/types/workflow';
import SimulateChart from './tera-simulate-chart.vue';

const props = defineProps<{
	node: WorkflowNode;
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

// const TspanUnitList = computed(() =>
// 	Object.values(TspanUnits).filter((v) => Number.isNaN(Number(v)))
// );

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

	console.log('execution payload', executionPayload);
	console.log('simulation', simulationObj);
	console.log('modelConfigId', modelConfigurationId);
	console.log('modelConfig', modelConfigurationObj);
	const modelId = modelConfigurationObj.modelId;
	model.value = await getModel(modelId);

	// Fetch run results
	await Promise.all(
		port.value.map(async (runId) => {
			const resultCsv = await getRunResult(runId, 'result.csv');
			const csvData = csvParse(resultCsv);
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
	margin: 1em;
}

.simulate-header-label {
	display: flex;
	align-items: center;
	margin: 0 1em;
	font-weight: 700;
	font-size: 1.75em;
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
