<template>
	<!--Probably rename tera-asset to something even more abstract-->
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section>
			<tera-asset stretch-content>
				<template #edit-buttons>
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
				</template>
				<Accordion
					v-if="view === CalibrationView.Input && modelConfig"
					:multiple="true"
					:active-index="[0, 1, 2, 3, 4]"
				>
					<AccordionTab :header="modelConfig.configuration.name">
						<tera-model-diagram :model="modelConfig.configuration" :is-editable="false" />
					</AccordionTab>
					<AccordionTab header="Mapping">
						<DataTable class="mapping-table" :value="mapping">
							<Column field="modelVariable">
								<template #header>
									<span class="column-header">Model variable</span>
								</template>
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
								<template #header>
									<span class="column-header">Dataset variable</span>
								</template>
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
					v-if="view === CalibrationView.Output && modelConfig"
					:multiple="true"
					:active-index="[0, 1]"
				>
					<AccordionTab header="Variables">
						<tera-simulate-chart
							v-for="(cfg, index) of node.state.chartConfigs"
							:key="index"
							:run-results="runResults"
							:chartConfig="cfg"
							:initial-data="csvAsset"
							:mapping="mapping"
							has-mean-line
							@configuration-change="chartConfigurationChange(index, $event)"
						/>
						<Button
							class="add-chart"
							text
							:outlined="true"
							@click="addChart"
							label="Add chart"
							icon="pi pi-plus"
						></Button>
					</AccordionTab>
				</Accordion>
				<section v-else-if="!modelConfig" class="emptyState">
					<img src="@assets/svg/seed.svg" alt="" draggable="false" />
					<p class="helpMessage">Connect a model configuration and dataset</p>
				</section>
			</tera-asset>
		</section>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ref, shallowRef, watch } from 'vue';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Dropdown from 'primevue/dropdown';
import Column from 'primevue/column';
import { getRunResult, getRunResultCiemss } from '@/services/models/simulation-service';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import { CsvAsset, ModelConfiguration } from '@/types/Types';
import Slider from 'primevue/slider';
import InputNumber from 'primevue/inputnumber';
import { setupModelInput, setupDatasetInput, CalibrateMap } from '@/services/calibrate-workflow';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { WorkflowNode } from '@/types/workflow';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import SelectButton from 'primevue/selectbutton';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import { CalibrationOperationStateCiemss } from './calibrate-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateCiemss>;
}>();
const emit = defineEmits(['append-output-port', 'update-state', 'close']);

enum CalibrationView {
	Input = 'Input',
	Output = 'Output'
}

// Model variables checked in the model configuration will be options in the mapping dropdown
const modelColumnNames = ref<string[] | undefined>();

const view = ref(CalibrationView.Input);
const viewOptions = ref([
	{ value: CalibrationView.Input, icon: 'pi pi-sign-in' },
	{ value: CalibrationView.Output, icon: 'pi pi-sign-out' }
]);

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

const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	emit('update-state', state);
};

const addChart = () => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs.push(_.last(state.chartConfigs) as ChartConfig);

	emit('update-state', state);
};

// Used from button to add new entry to the mapping object
function addMapping() {
	mapping.value.push({
		modelVariable: '',
		datasetVariable: ''
	});

	const state = _.cloneDeep(props.node.state);
	state.mapping = mapping.value;

	emit('update-state', state);
}

async function getAutoMapping() {
	if (!modelConfigId.value) {
		console.log('no model config id'); // should be toast when jami is done
		return;
	}
	if (!datasetId.value) {
		console.log('no dataset id');
	}
	// mapping.value = await autoCalibrationMapping(modelConfigId.value, datasetId.value);
}

// Set up model config + dropdown names
// Note: Same as calibrate-node
watch(
	() => modelConfigId.value,
	async () => {
		const { modelConfiguration, modelColumnNameOptions } = await setupModelInput(
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
		const { filename, csv } = await setupDatasetInput(datasetId.value);
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

		const output = await getRunResultCiemss(simulationIds.value[0].runId, 'result.csv');
		runResults.value = output.runResults;
		parameterResult.value = await getRunResult(simulationIds.value[0].runId, 'visualization.json');
	},
	{ immediate: true }
);
</script>

<style scoped>
.p-accordion {
	padding-top: 1rem;
}

.mapping-table:deep(td) {
	padding: 0rem 0.25rem 0.5rem 0rem !important;
	border: none !important;
}

.mapping-table:deep(th) {
	padding: 0rem 0.25rem 0.5rem 0.25rem !important;
	border: none !important;
	width: 50%;
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

.column-header {
	color: var(--text-color-primary);
	font-size: var(--font-body-small);
	font-weight: var(--font-weight-semibold);
}

.emptyState {
	align-self: center;
	display: flex;
	flex-direction: column;
	align-items: center;
	text-align: center;
	margin-top: 15rem;
	gap: 0.5rem;
}

.helpMessage {
	color: var(--text-color-subdued);
	font-size: var(--font-body-small);
	width: 90%;
	margin-top: 1rem;
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

img {
	width: 20%;
}
</style>
