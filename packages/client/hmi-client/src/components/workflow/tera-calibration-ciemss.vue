<template>
	<!--Probably rename tera-asset to something even more abstract-->
	<tera-asset :name="'Calibrate'" is-editable stretch-content>
		<template #nav>
			<tera-asset-nav :show-header-links="false">
				<template #viewing-mode>
					<span class="p-buttonset">
						<Button
							class="p-button-secondary p-button-sm"
							label="Input"
							icon="pi pi-sign-in"
							@click="calibrationView = CalibrationView.INPUT"
							:active="calibrationView === CalibrationView.INPUT"
						/>
						<Button
							class="p-button-secondary p-button-sm"
							label="Output"
							icon="pi pi-sign-out"
							@click="calibrationView = CalibrationView.OUTPUT"
							:active="calibrationView === CalibrationView.OUTPUT"
						/>
					</span>
				</template>
			</tera-asset-nav>
		</template>
		<Accordion
			v-if="calibrationView === CalibrationView.INPUT && modelConfig"
			:multiple="true"
			:active-index="[0, 1, 2, 3, 4]"
		>
			<AccordionTab :header="modelConfig.configuration.name">
				<tera-model-diagram :model="modelConfig.configuration" :is-editable="false" />
			</AccordionTab>
			<AccordionTab header="Mapping">
				<DataTable class="p-datatable-xsm" :value="mapping">
					<Column field="modelVariable">
						<template #body="{ data, field }">
							<!-- Tom TODO: No v-model -->
							<Dropdown
								class="w-full"
								placeholder="Select a variable"
								v-model="data[field]"
								:options="modelColumnNames"
							/>
						</template>
					</Column>
					<Column field="datasetVariable">
						<template #body="{ data, field }">
							<!-- Tom TODO: No v-model -->
							<Dropdown
								class="w-full"
								placeholder="Select a variable"
								v-model="data[field]"
								:options="datasetColumnNames"
							/>
						</template>
					</Column>
				</DataTable>
				<div>
					<Button
						class="p-button-sm p-button-outlined"
						icon="pi pi-plus"
						label="Add mapping"
						@click="addMapping"
					/>
				</div>
			</AccordionTab>
			<AccordionTab v-if="datasetId" :header="currentDatasetFileName">
				<tera-dataset-datatable preview-mode :raw-content="csvAsset ?? null" />
			</AccordionTab>
			<AccordionTab header="Train / Test ratio">
				<section class="train-test-ratio">
					<InputNumber v-model="trainTestValue" />
					<Slider v-model="trainTestValue" />
				</section>
			</AccordionTab>
		</Accordion>
		<Accordion
			v-if="calibrationView === CalibrationView.OUTPUT && modelConfig"
			:multiple="true"
			:active-index="[0, 1]"
		>
			<AccordionTab header="Variables">
				<tera-simulate-chart
					v-for="(cfg, index) of node.state.chartConfigs"
					:key="index"
					:run-results="runResults"
					:chartConfig="cfg"
					@configuration-change="chartConfigurationChange(index, $event)"
				/>
				<Button
					class="add-chart"
					text
					:outlined="true"
					@click="addChart"
					label="Add Chart"
					icon="pi pi-plus"
				></Button>
			</AccordionTab>
			<AccordionTab header="Calibrated parameter values">
				<table class="p-datatable-table">
					<thead class="p-datatable-thead">
						<th>Parameter</th>
						<th>Value</th>
					</thead>
					<tr v-for="(content, key) in parameterResult" :key="key">
						<td>
							<p>{{ key }}</p>
						</td>
						<td>
							<p>{{ content }}</p>
						</td>
					</tr>
				</table>
			</AccordionTab>
		</Accordion>
	</tera-asset>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ref, shallowRef, watch } from 'vue';
import { csvParse } from 'd3';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Dropdown from 'primevue/dropdown';
import Column from 'primevue/column';
import { getRunResult } from '@/services/models/simulation-service';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraAssetNav from '@/components/asset/tera-asset-nav.vue';
import TeraModelDiagram from '@/components/models/tera-model-diagram.vue';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import { CsvAsset, ModelConfiguration } from '@/types/Types';
import Slider from 'primevue/slider';
import InputNumber from 'primevue/inputnumber';
import { setupModelInputJulia, setupDatasetInputJulia } from '@/services/calibrate-workflow';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { WorkflowNode } from '@/types/workflow';
import { workflowEventBus } from '@/services/workflow';
import TeraSimulateChart from './tera-simulate-chart.vue';
import { CalibrationOperationStateJulia, CalibrateMap } from './calibrate-operation-julia';

const props = defineProps<{
	node: WorkflowNode;
}>();

enum CalibrationView {
	INPUT = 'input',
	OUTPUT = 'output'
}

// Model variables checked in the model configuration will be options in the mapping dropdown
const modelColumnNames = ref<string[] | undefined>();

const calibrationView = ref(CalibrationView.INPUT);

const trainTestValue = ref(80);

const datasetColumnNames = ref<string[]>();
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

const modelConfig = ref<ModelConfiguration>();

const modelConfigId = computed<string | undefined>(() => props.node.inputs[0]?.value?.[0]);
const datasetId = computed<string | undefined>(() => props.node.inputs[1]?.value?.[0]);
const currentDatasetFileName = ref<string>();
const simulationIds = computed<any | undefined>(() => props.node.outputs[0]?.value);
const runResults = ref<RunResults>({});
const parameterResult = ref<{ [index: string]: any }[]>();
const mapping = ref<CalibrateMap[]>(props.node.state.mapping);

// Tom TODO: Make this generic... its copy paste from node.
const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state: CalibrationOperationStateJulia = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const addChart = () => {
	const state: CalibrationOperationStateJulia = _.cloneDeep(props.node.state);
	state.chartConfigs.push(_.last(state.chartConfigs) as ChartConfig);

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

// Used from button to add new entry to the mapping object
// Tom TODO: Make this generic... its copy paste from node.
function addMapping() {
	mapping.value.push({
		modelVariable: '',
		datasetVariable: ''
	});

	const state: CalibrationOperationStateJulia = _.cloneDeep(props.node.state);
	state.mapping = mapping.value;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
}
// Set up model config + dropdown names
// Note: Same as calibrate-node
watch(
	() => modelConfigId.value,
	async () => {
		const { modelConfiguration, modelColumnNameOptions } = await setupModelInputJulia(
			modelConfigId.value
		);
		modelConfig.value = modelConfiguration;
		modelColumnNames.value = modelColumnNameOptions;
	},
	{ immediate: true }
);

// Set up csv + dropdown names
// Note: Same as calibrate-node
watch(
	() => datasetId.value,
	async () => {
		const { filename, csv } = await setupDatasetInputJulia(datasetId.value);
		currentDatasetFileName.value = filename;
		csvAsset.value = csv;
		datasetColumnNames.value = csv?.headers;
	},
	{ immediate: true }
);

// Fetch simulation run results whenever output changes
watch(
	() => simulationIds.value,
	async () => {
		if (!simulationIds.value) return;
		const resultCsv = await getRunResult(simulationIds.value[0].runId, 'simulation.csv');
		const csvData = csvParse(resultCsv);
		runResults.value[simulationIds.value[0].runId] = csvData as any;
		parameterResult.value = await getRunResult(simulationIds.value[0].runId, 'parameters.json');
	},
	{ immediate: true }
);
</script>

<style scoped>
.p-accordion {
	padding-top: 1rem;
}

.dropdown-button {
	width: 156px;
	height: 25px;
	border-radius: 6px;
}

.train-test-ratio {
	display: flex;
	gap: 1rem;
	margin: 0.5rem 0;
}

.train-test-ratio > .p-slider {
	margin-top: 1rem;
	width: 100%;
}

th {
	text-align: left;
}
</style>
