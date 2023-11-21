<template>
	<!--Probably rename tera-asset to something even more abstract-->
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
		<div v-if="view === CalibrationView.Output && modelConfig">
			<Dropdown
				v-if="runList.length > 0"
				:options="runList"
				v-model="selectedRun"
				option-label="label"
				placeholder="Select a calibration run"
			/>
			<Accordion :multiple="true" :active-index="[0, 1, 2]">
				<AccordionTab header="Loss">
					<div ref="drilldownLossPlot"></div>
				</AccordionTab>
				<AccordionTab header="Calibrated parameter values">
					<table class="p-datatable-table" v-if="selectedRunId">
						<thead class="p-datatable-thead">
							<th>Parameter</th>
							<th>Value</th>
						</thead>
						<tr v-for="(content, key) in runResultParams[selectedRunId]" :key="key">
							<td>
								<p>{{ key }}</p>
							</td>
							<td>
								<p>{{ content }}</p>
							</td>
						</tr>
					</table>
				</AccordionTab>
				<AccordionTab header="Variables">
					<div v-if="selectedRunId && runResults[selectedRunId]">
						<tera-simulate-chart
							v-for="(cfg, index) of node.state.calibrateConfigs.chartConfigs"
							:key="index"
							:run-results="{ [selectedRunId]: runResults[selectedRunId] }"
							:initial-data="csvAsset"
							:mapping="mapping"
							:run-type="RunType.Julia"
							:chartConfig="{ selectedRun: selectedRunId, selectedVariable: cfg }"
							@configuration-change="chartConfigurationChange(index, $event)"
						/>
						<Button
							class="p-button-sm p-button-text"
							@click="addChart"
							label="Add chart"
							icon="pi pi-plus"
						></Button>
					</div>
				</AccordionTab>
			</Accordion>
		</div>
		<section v-else-if="!modelConfig" class="emptyState">
			<img src="@assets/svg/seed.svg" alt="" draggable="false" />
			<p class="helpMessage">Connect a model configuration and dataset</p>
		</section>
	</tera-asset>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, onMounted, ref, shallowRef, watch } from 'vue';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Dropdown from 'primevue/dropdown';
import Column from 'primevue/column';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import { CsvAsset, ModelConfiguration } from '@/types/Types';
import Slider from 'primevue/slider';
import InputNumber from 'primevue/inputnumber';
import { setupModelInput, setupDatasetInput, renderLossGraph } from '@/services/calibrate-workflow';
import { ChartConfig, RunResults, RunType } from '@/types/SimulateConfig';
import { WorkflowNode } from '@/types/workflow';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import SelectButton from 'primevue/selectbutton';
import { getRunResultJulia } from '@/services/models/simulation-service';
import { csvParse } from 'd3';
import { CalibrationOperationStateJulia, CalibrateMap } from './calibrate-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateJulia>;
}>();
const emit = defineEmits(['append-output-port', 'update-state']);

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
const runResults = ref<RunResults>({});
const runResultParams = ref<Record<string, Record<string, number>>>({});
const mapping = ref<CalibrateMap[]>(props.node.state.mapping);

const runList = computed(() =>
	Object.keys(props.node.state.calibrateConfigs.runConfigs).map((runId: string, idx: number) => ({
		label: `Output ${idx + 1} - ${runId}`,
		runId
	}))
);
const selectedRun = ref(); // used to select a run from the dropdown for this component
const selectedRunId = computed(
	() =>
		// if selected run changes from the workflow node component, then it should change in this component as well
		Object.values(props.node.state.calibrateConfigs.runConfigs).find((metadata) => metadata.active)
			?.runId
);
const drilldownLossPlot = ref<HTMLElement>();

// Tom TODO: Make this generic... its copy paste from node.
const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.calibrateConfigs.chartConfigs[index] = config.selectedVariable;

	emit('update-state', state);
};

onMounted(() => {
	const runId = Object.values(props.node.state.calibrateConfigs.runConfigs).find(
		(metadata) => metadata.active
	)?.runId;
	if (runId) {
		selectedRun.value = runList.value.find((run) => run.runId === runId);
	} else {
		selectedRun.value = runList.value.length > 0 ? runList.value[0] : undefined;
	}
});

const addChart = () => {
	const state = _.cloneDeep(props.node.state);
	state.calibrateConfigs.chartConfigs.push([]);

	emit('update-state', state);
};

// Used from button to add new entry to the mapping object
// Tom TODO: Make this generic... its copy paste from node.
function addMapping() {
	mapping.value.push({
		modelVariable: '',
		datasetVariable: ''
	});

	const state = _.cloneDeep(props.node.state);
	state.mapping = mapping.value;

	emit('update-state', state);
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

const handleSelectedRunChange = () => {
	if (!selectedRun.value) return;

	const state = _.cloneDeep(props.node.state);
	// set the active status for the selected run in the run configs
	Object.keys(state.calibrateConfigs.runConfigs).forEach((runId) => {
		state.calibrateConfigs.runConfigs[runId].active = runId === selectedRun.value.runId;
	});

	emit('update-state', state);
};
watch(() => selectedRun.value, handleSelectedRunChange, { immediate: true });

const lazyLoadCalibrationData = async (runId?: string) => {
	if (!runId || runResults.value[runId]) return;

	const result = await getRunResultJulia(runId, 'result.json');
	if (result) {
		const csvData = csvParse(result.csvData);
		runResults.value[runId] = csvData as any;
		runResultParams.value[runId] = result.paramVals;
	}
};
watch(
	() => selectedRunId.value,
	() => {
		lazyLoadCalibrationData(selectedRunId.value);
	},
	{ immediate: true }
);

// Plot loss values if available on mount or on selectedRun change
watch([() => selectedRunId.value, () => drilldownLossPlot.value], () => {
	if (selectedRunId.value) {
		const lossVals = props.node.state.calibrateConfigs.runConfigs[selectedRunId.value]?.loss;
		if (lossVals && drilldownLossPlot.value) {
			const width = drilldownLossPlot.value.offsetWidth;
			renderLossGraph(drilldownLossPlot.value, lossVals, { width, height: 300 });
		}
	}
});
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

img {
	width: 20%;
}
</style>
