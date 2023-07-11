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
			<span class="simulate-header-label">Simulate (probabilistic)</span>
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
					<MultiSelect
						v-model="selectedCols"
						:options="rawDataKeys"
						placeholder="Select Colummns"
					/>
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
				:rowsPerPageOptions="[10, 20, 30]"
			></Paginator>
			<simulate-chart
				v-for="(cfg, index) of node.state.chartConfigs"
				:key="index"
				:run-results="renderedRuns"
				:chartConfig="cfg"
				:line-color-array="lineColorArray"
				@configuration-change="configurationChange(index, $event)"
			/>
			<Button
				class="add-chart"
				text
				:outlined="true"
				@click="addChart"
				label="Add Chart"
				icon="pi pi-plus"
			></Button>
		</div>
		<div v-else-if="activeTab === SimulateTabs.input && node" class="simulate-container">
			<div class="simulate-model">
				<Accordion :multiple="true" :active-index="[0, 1, 2]">
					<AccordionTab>
						<template #header> Model </template>
						<model-diagram v-if="model" :model="model" :is-editable="false" />
					</AccordionTab>
					<AccordionTab>
						<template #header> Working Title </template>
						<div class="uncertainty-container">
							<input-switch v-model="uncertainty" />
							<div class="uncertainty">
								<div>Add uncertainty</div>
								<div>
									This option turns the values without ranges into distributions with a width of +/-
									a set amount
								</div>
								<div class="uncertainty-input">
									<label for="uncertaintyAmt"> Uncertainty Amount </label>
									<InputNumber
										inputId="uncertaintyAmt"
										v-model="uncertaintyPercentage"
										suffix=" %"
									/>
								</div>
							</div>
						</div>
						<tera-model-configuration v-if="model" :model="model" :run-configs="runConfigs" />
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
// import Column from 'primevue/column';
// import Row from 'primevue/row';
// import ColumnGroup from 'primevue/columngroup';
// import DataTable from 'primevue/datatable';
// import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import InputSwitch from 'primevue/inputswitch';
import InputNumber from 'primevue/inputnumber';
import Paginator from 'primevue/paginator';
import { ModelConfiguration, Model, TimeSpan } from '@/types/Types';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';

import { getModelConfigurationById } from '@/services/model-configurations';
import ModelDiagram from '@/components/models/tera-model-diagram.vue';
import TeraModelConfiguration from '@/components/models/tera-model-configuration-ciemss.vue';

import { getModel } from '@/services/model';
import { csvParse } from 'd3';
import { WorkflowNode } from '@/types/workflow';
import { workflowEventBus } from '@/services/workflow';
import SimulateChart from './tera-simulate-chart.vue';
import { SimulateOperationState } from './simulate-ciemss-operation';

import SimulateProbabilisticData from './simulate-prob-data';

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
const parsedRawData = ref<any>();
const runConfigs = ref<any>({});
const runResults = ref<RunResults>({});
const renderedRuns = ref<RunResults>({});

const modelConfiguration = ref<ModelConfiguration | null>(null);

const uncertainty = ref<boolean>(true);
const uncertaintyPercentage = ref<number>(10);

const selectedCols = ref<string[]>([]);
const paginatorRows = ref(10);
const paginatorFirst = ref(0);

// const TspanUnitList = computed(() =>
// 	Object.values(TspanUnits).filter((v) => Number.isNaN(Number(v)))
// );

const configurationChange = (index: number, config: ChartConfig) => {
	const state: SimulateOperationState = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const addChart = () => {
	const state: SimulateOperationState = _.cloneDeep(props.node.state);
	state.chartConfigs.push(_.last(state.chartConfigs) as ChartConfig);

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

onMounted(async () => {
	const modelConfigId = props.node.inputs[0].value?.[0];
	modelConfiguration.value = await getModelConfigurationById(modelConfigId);
	if (modelConfiguration.value) {
		model.value = await getModel(modelConfiguration.value.modelId);
	}

	// Fetch run results
	parsedRawData.value = csvParse(SimulateProbabilisticData);
	const parsedSimProbData = parsedRawData.value;

	// populate completedRunIdList
	const completedRunIdList = new Array(
		Number(parsedSimProbData[parsedSimProbData.length - 1].sample_id) + 1
	)
		.fill('0')
		.map((_x, i) => i.toString());

	// initialize runResults
	for (let i = 0; i < completedRunIdList.length; i++) {
		runResults.value[i.toString()] = [];
	}

	// populate runResults
	parsedSimProbData.forEach((inputRow) => {
		const outputRowRunResults = { timestamp: inputRow.timepoint_id };
		Object.keys(inputRow).forEach((key) => {
			if (key.includes('_sol')) {
				outputRowRunResults[key.replace('_sol', '')] = inputRow[key];
			} else if (key.includes('_param')) {
				const paramKey = key.replace('_param', '');
				if (!runConfigs.value[paramKey]) {
					runConfigs.value[paramKey] = [];
				}
				runConfigs.value[paramKey].push(Number(inputRow[key]));
			}
		});
		runResults.value[inputRow.sample_id as string].push(outputRowRunResults as any);
	});

	Object.keys(runConfigs.value).forEach((key) => {
		runConfigs.value[key] = runConfigs.value[key].sort();
	});
});

const lineColorArray = computed(() => {
	const output = Array(Math.max(Object.keys(runResults.value).length ?? 0 - 1, 0)).fill(
		'#00000020'
	);
	output.push('#1b8073');
	return output;
});

watch(
	() => runResults.value,
	(input) => {
		const runResult = JSON.parse(JSON.stringify(input));
		// renderedRuns.value = runResults.value;

		// convert to array from array-like object
		const parsedSimProbData = Object.values(runResult);

		const numRuns = parsedSimProbData.length;
		if (!numRuns) {
			renderedRuns.value = runResult;
			return;
		}

		const numTimestamps = (parsedSimProbData[0] as any[]).length;
		const aggregateRun: any = [];

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

.uncertainty-container {
	display: flex;
	gap: 1em;
}
.uncertainty {
	flex-grow: 1;
	flex-basis: 0;
	margin: 1em;
	margin-top: 0;
}
.uncertainty-input {
	margin-top: 1em;
	font-weight: bolder;
}
</style>
