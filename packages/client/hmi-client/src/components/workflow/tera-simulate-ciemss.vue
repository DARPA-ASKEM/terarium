<template>
	<section class="tera-simulate">
		<div class="simulate-header">
			<span class="simulate-header-label">Simulate (probabilistic)</span>
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
			<div class="datatable-header">
				<div class="datatable-header-title">
					{{ `${rawDataKeys.length} columns | ${parsedRawData.length} rows` }}
				</div>
				<div class="datatable-header-select-container">
					<MultiSelect v-model="selectedCols" :options="rawDataKeys" placeholder="Select columns" />
				</div>
			</div>
			<div
				class="p-datatable p-component p-datatable-scrollable p-datatable-responsive-scroll p-datatable-gridlines p-datatable-grouped-header"
			>
				<div class="p-datatable-wrapper">
					<table class="p-datatable-table p-datatable-scrollable-table editable-cells-table">
						<thead class="p-datatable-thead">
							<tr>
								<th
									v-for="(header, i) of selectedCols.length ? selectedCols : rawDataKeys"
									:key="i"
									class="p-frozen-column"
								>
									{{ header }}
								</th>
							</tr>
						</thead>
						<tbody class="p-datatable-tbody">
							<tr v-for="(data, i) of rawDataRenderedRows" :key="i">
								<td v-for="(key, j) of selectedCols.length ? selectedCols : rawDataKeys" :key="j">
									{{ data[key] }}
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
			<Paginator
				v-model:first="paginatorFirst"
				v-model:rows="paginatorRows"
				:totalRecords="parsedRawData.length"
				:rowsPerPageOptions="[5, 10, 20, 50]"
			></Paginator>
			<simulate-chart
				v-for="(cfg, index) of node.state.chartConfigs"
				:key="index"
				:run-results="renderedRuns"
				:chartConfig="cfg"
				:line-color-array="lineColorArray"
				:line-width-array="lineWidthArray"
				@configuration-change="configurationChange(index, $event)"
			/>
			<Button
				class="add-chart"
				text
				:outlined="true"
				@click="addChart"
				label="Add Chart"
				icon="pi pi-plus"
			/>
			<Button
				class="add-chart"
				text
				:outlined="true"
				@click="saveDataset"
				label="Save as Dataset"
				icon="pi pi-save"
			/>
		</div>
		<div v-else-if="activeTab === SimulateTabs.input && node" class="simulate-container">
			<div class="simulate-model">
				<Accordion :multiple="true" :active-index="[0, 1, 2]">
					<AccordionTab>
						<template #header> Model </template>
						<model-diagram v-if="model" :model="model" :is-editable="false" />
					</AccordionTab>
					<AccordionTab>
						<template #header> Model configuration </template>
						<tera-model-configuration v-if="model" :model="model" :is-editable="false" />
					</AccordionTab>
					<AccordionTab>
						<template #header> Simulation time range </template>
						<div class="sim-tspan-container">
							<!--
							<div class="sim-tspan-group">
								<label for="1">Units</label>
								<Dropdown
									id="1"
									class="p-inputtext-sm"
									v-model=""
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
import MultiSelect from 'primevue/multiselect';
import * as ProjectService from '@/services/project';
// import Column from 'primevue/column';
// import Row from 'primevue/row';
// import ColumnGroup from 'primevue/columngroup';
// import DataTable from 'primevue/datatable';
// import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import InputNumber from 'primevue/inputnumber';
import Paginator from 'primevue/paginator';
import { Model, TimeSpan } from '@/types/Types';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';

import { getModel } from '@/services/model';
import { getModelConfigurationById } from '@/services/model-configurations';
import { getRunResultCiemss } from '@/services/models/simulation-service';
import ModelDiagram from '@/components/models/tera-model-diagram.vue';
import TeraModelConfiguration from '@/components/models/tera-model-configuration.vue';
import SimulateChart from '@/components/workflow/tera-simulate-chart.vue';
import { SimulateCiemssOperationState } from '@/components/workflow/simulate-ciemss-operation';

import { WorkflowNode } from '@/types/workflow';
import { workflowEventBus } from '@/services/workflow';
import { createDatasetFromSimulationResult } from '@/services/dataset';
import { IProject } from '@/types/Project';
import useResourcesStore from '@/stores/resources';

const props = defineProps<{
	node: WorkflowNode;
	project: IProject;
}>();

const timespan = ref<TimeSpan>(props.node.state.currentTimespan);
const numSamples = ref<number>(props.node.state.numSamples);

enum SimulateTabs {
	input,
	output
}

const activeTab = ref(SimulateTabs.input);

const model = ref<Model | null>(null);
const parsedRawData = ref<any>();
const runConfigs = ref<any>({});
const runResults = ref<RunResults>({});
const renderedRuns = ref<RunResults>({});

const selectedCols = ref<string[]>([]);
const paginatorRows = ref(10);
const paginatorFirst = ref(0);

// const TspanUnitList = computed(() =>
// 	Object.values(TspanUnits).filter((v) => Number.isNaN(Number(v)))
// );

const configurationChange = (index: number, config: ChartConfig) => {
	const state: SimulateCiemssOperationState = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const addChart = () => {
	const state: SimulateCiemssOperationState = _.cloneDeep(props.node.state);
	state.chartConfigs.push(_.last(state.chartConfigs) as ChartConfig);

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const saveDataset = async () => {
	if (!props.node) return;
	// @ts-ignore: Object is possibly 'null'.
	await createDatasetFromSimulationResult(props.project.id, props.node!.outputs[0].value[0]);
	// TODO: See about getting rid of this - this refresh should preferably be within a service
	useResourcesStore().setActiveProject(await ProjectService.get(props.project.id, true));
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

	const modelConfigId = nodeObj.inputs[0].value?.[0];
	const modelConfiguration = await getModelConfigurationById(modelConfigId);
	if (modelConfiguration) {
		model.value = await getModel(modelConfiguration.modelId);
	}

	const output = await getRunResultCiemss(simulationId);
	parsedRawData.value = output.parsedRawData;
	runResults.value = output.runResults;
	runConfigs.value = output.runConfigs;
});

const lineColorArray = computed(() => {
	const output = Array(Math.max(Object.keys(runResults.value).length ?? 0 - 1, 0)).fill(
		'#00000020'
	);
	output.push('#1b8073');
	return output;
});

const lineWidthArray = computed(() => {
	const output = Array(Math.max(Object.keys(runResults.value).length ?? 0 - 1, 0)).fill(1);
	output.push(2);
	return output;
});

// process run result data to create mean run line
watch(
	() => runResults.value,
	(input) => {
		const runResult: RunResults = JSON.parse(JSON.stringify(input));

		// convert to array from array-like object
		const parsedSimProbData = Object.values(runResult);

		const numRuns = parsedSimProbData.length;
		if (!numRuns) {
			renderedRuns.value = runResult;
			return;
		}

		const numTimestamps = (parsedSimProbData as { [key: string]: number }[][])[0].length;
		const aggregateRun: { [key: string]: number }[] = [];

		for (let timestamp = 0; timestamp < numTimestamps; timestamp++) {
			for (let run = 0; run < numRuns; run++) {
				if (!aggregateRun[timestamp]) {
					aggregateRun[timestamp] = parsedSimProbData[run][timestamp];
					Object.keys(aggregateRun[timestamp]).forEach((key) => {
						aggregateRun[timestamp][key] = Number(aggregateRun[timestamp][key]) / numRuns;
					});
				} else {
					const datum = parsedSimProbData[run][timestamp];
					Object.keys(datum).forEach((key) => {
						aggregateRun[timestamp][key] += datum[key] / numRuns;
					});
				}
			}
		}

		renderedRuns.value = { ...runResult, [numRuns]: aggregateRun };
	},
	{ immediate: true, deep: true }
);

watch(
	() => numSamples.value,
	(n) => {
		const state: SimulateCiemssOperationState = _.cloneDeep(props.node.state);
		state.numSamples = n;
		workflowEventBus.emitNodeStateChange({
			workflowId: props.node.workflowId,
			nodeId: props.node.id,
			state
		});
	}
);

const rawDataKeys = computed(() => Object.keys(parsedRawData.value[0]));

const rawDataRenderedRows = computed(() =>
	parsedRawData.value.slice(paginatorFirst.value, paginatorFirst.value + paginatorRows.value)
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
