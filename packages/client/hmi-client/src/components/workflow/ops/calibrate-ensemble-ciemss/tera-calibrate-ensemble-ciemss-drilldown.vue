<template>
	<tera-drilldown
		:node="node"
		@update:selection="onSelection"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<section :tabName="CalibrateEnsembleTabs.Wizard">
			<tera-drilldown-section class="ml-3 mr-2 pt-3">
				<template #header-controls-right>
					<Button :disabled="isRunDisabled" label="Run" icon="pi pi-play" @click="runEnsemble" />
					<tera-pyciemss-cancel-button class="mr-auto" :simulation-run-id="cancelRunId" />
				</template>
				<Accordion :multiple="true" :active-index="[0, 1, 2]">
					<AccordionTab header="Model weights">
						<div class="model-weights">
							<!-- Turn this into a horizontal bar chart -->
							<section>
								<table class="p-datatable-table">
									<thead class="p-datatable-thead">
										<tr>
											<th>Model config ID</th>
											<th>Weight</th>
										</tr>
									</thead>
									<tbody class="p-datatable-tbody">
										<!-- Index matching listModelLabels and ensembleConfigs-->
										<tr v-for="(id, i) in listModelLabels" :key="i">
											<td>
												{{ id }}
											</td>
											<td>
												<tera-input-number v-model="knobs.ensembleConfigs[i].weight" />
											</td>
										</tr>
									</tbody>
								</table>
								<Button
									label="Set weights to be equal"
									class="p-button-sm p-button-outlined ml-2 mt-2"
									outlined
									severity="secondary"
									@click="calculateEvenWeights()"
								/>
							</section>
						</div>
					</AccordionTab>
					<AccordionTab header="Mapping">
						<label> Dataset timestamp column </label>
						<Dropdown
							v-model="knobs.timestampColName"
							:options="datasetColumnNames"
							placeholder="Timestamp column"
							class="ml-2"
						/>
						<template v-if="knobs.ensembleConfigs.length > 0">
							<table class="w-full mt-3">
								<tbody>
									<tr>
										<th class="w-4">Ensemble variables</th>
										<!-- Index matching listModelLabels and ensembleConfigs-->
										<th v-for="(element, i) in listModelLabels" :key="i">
											{{ element }}
										</th>
									</tr>
									<tr>
										<td v-for="(element, i) in Object.keys(knobs.ensembleConfigs[0].solutionMappings)" :key="i">
											{{ element }}
										</td>
										<td v-for="i in knobs.ensembleConfigs.length" :key="i">
											<template
												v-for="element in Object.keys(knobs.ensembleConfigs[i - 1].solutionMappings)"
												:key="element"
											>
												<Dropdown
													v-model="knobs.ensembleConfigs[i - 1].solutionMappings[element]"
													:options="allModelOptions[i - 1]?.map((ele) => ele.referenceId ?? ele.id)"
													class="w-full mb-2 mt-2"
												/>
											</template>
										</td>
									</tr>
								</tbody>
							</table>
						</template>
						<Dropdown
							class="mr-2"
							v-model="newSolutionMappingKey"
							:options="datasetColumnNames"
							placeholder="Variable name"
						/>
						<Button class="p-button-sm p-button-outlined" icon="pi pi-plus" label="Add mapping" @click="addMapping" />
					</AccordionTab>
					<AccordionTab header="Additional fields">
						<table>
							<thead class="p-datatable-thead">
								<tr>
									<th>Units</th>
									<th>Number of particles</th>
									<th>Number of iterations</th>
									<th>Solver method</th>
								</tr>
							</thead>
							<tbody class="p-datatable-tbody">
								<tr>
									<td>Steps</td>
									<td>
										<tera-input-number v-model="knobs.extra.numParticles" />
									</td>
									<td>
										<tera-input-number v-model="knobs.extra.numIterations" />
									</td>
									<td>
										<Dropdown
											class="p-inputtext-sm"
											:options="['dopri5', 'euler']"
											v-model="knobs.extra.solverMethod"
											placeholder="Select"
										/>
									</td>
								</tr>
							</tbody>
						</table>
					</AccordionTab>
				</Accordion>
			</tera-drilldown-section>
		</section>
		<section :tabName="CalibrateEnsembleTabs.Notebook">
			<h4>Notebook</h4>
		</section>
		<template #preview>
			<tera-drilldown-preview
				title="Calibrate ensemble"
				:options="outputs"
				v-model:output="selectedOutputId"
				@update:selection="onSelection"
				:is-loading="showSpinner"
				is-selectable
			>
				<section v-if="!inProgressCalibrationId && !inProgressForecastId" ref="outputPanel">
					<tera-simulate-chart
						v-for="(cfg, index) of node.state.chartConfigs"
						:key="index"
						:run-results="runResults"
						:chartConfig="{
							selectedRun: props.node.state.forecastRunId,
							selectedVariable: cfg
						}"
						has-mean-line
						@configuration-change="chartProxy.configurationChange(index, $event)"
						@remove="chartProxy.removeChart(index)"
						show-remove-button
						:size="chartSize"
						class="mb-2"
					/>
					<Button
						class="p-button-sm p-button-text"
						@click="chartProxy.addChart()"
						label="Add chart"
						icon="pi pi-plus"
					/>
				</section>
				<tera-progress-spinner
					v-if="inProgressCalibrationId || inProgressForecastId"
					:font-size="2"
					is-centered
					style="height: 100%"
				/>
			</tera-drilldown-preview>
		</template>
	</tera-drilldown>
	<tera-save-dataset-from-simulation
		:simulation-run-id="knobs.forecastRunId"
		:showDialog="showSaveDataDialog"
		@dialog-hide="showSaveDataDialog = false"
	/>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, shallowRef, computed, watch, onMounted } from 'vue';
import { getRunResultCiemss, makeEnsembleCiemssCalibration } from '@/services/models/simulation-service';
import Button from 'primevue/button';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import AccordionTab from 'primevue/accordiontab';
import Accordion from 'primevue/accordion';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import Dropdown from 'primevue/dropdown';
import { setupDatasetInput, setupCsvAsset, setupModelInput } from '@/services/calibrate-workflow';
import TeraSimulateChart from '@/components/workflow/tera-simulate-chart.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraSaveDatasetFromSimulation from '@/components/dataset/tera-save-dataset-from-simulation.vue';
import TeraPyciemssCancelButton from '@/components/pyciemss/tera-pyciemss-cancel-button.vue';

import { chartActionsProxy, drilldownChartSize, getTimespan, nodeMetadata } from '@/components/workflow/util';
import type {
	CsvAsset,
	EnsembleModelConfigs,
	EnsembleCalibrationCiemssRequest,
	ModelConfiguration,
	Dataset
} from '@/types/Types';
import { RunResults } from '@/types/SimulateConfig';
import { WorkflowNode } from '@/types/workflow';
import { getDataset } from '@/services/dataset';
import {
	CalibrateEnsembleCiemssOperationState,
	EnsembleCalibrateExtraCiemss
} from './calibrate-ensemble-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrateEnsembleCiemssOperationState>;
}>();
const showSaveDataDialog = ref<boolean>(false);
const emit = defineEmits(['append-output', 'update-state', 'close', 'select-output']);

enum CalibrateEnsembleTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

interface BasicKnobs {
	ensembleConfigs: EnsembleModelConfigs[];
	extra: EnsembleCalibrateExtraCiemss;
	forecastRunId: string;
	timestampColName: string;
}

const knobs = ref<BasicKnobs>({
	ensembleConfigs: props.node.state.ensembleConfigs ?? [],
	extra: props.node.state.extra ?? {},
	forecastRunId: props.node.state.forecastRunId,
	timestampColName: props.node.state.timestampColName ?? ''
});

const outputs = computed(() => {
	if (!_.isEmpty(props.node.outputs)) {
		return [
			{
				label: 'Select an output',
				items: props.node.outputs
			}
		];
	}
	return [];
});
const selectedOutputId = ref<string>();
const showSpinner = ref(false);
const isRunDisabled = computed(() => !knobs.value.ensembleConfigs[0]?.weight || !datasetId.value);
const cancelRunId = computed(() => props.node.state.inProgressForecastId || props.node.state.inProgressCalibrationId);
const inProgressCalibrationId = computed(() => props.node.state.inProgressCalibrationId);
const inProgressForecastId = computed(() => props.node.state.inProgressForecastId);

const datasetId = computed(() => props.node.inputs[0].value?.[0] as string | undefined);
const currentDatasetFileName = ref<string>();
const datasetColumnNames = ref<string[]>();

const listModelLabels = ref<string[]>([]);
const allModelConfigurations = ref<ModelConfiguration[]>([]);
// List of each observible + state for each model.
const allModelOptions = ref<any[][]>([]);

const newSolutionMappingKey = ref<string>('');
const runResults = ref<RunResults>({});

const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

const outputPanel = ref(null);
const chartSize = computed(() => drilldownChartSize(outputPanel.value));
const chartProxy = chartActionsProxy(props.node, (state: CalibrateEnsembleCiemssOperationState) => {
	emit('update-state', state);
});

const calculateEvenWeights = () => {
	if (!knobs.value.ensembleConfigs) return;
	const percent = 1 / knobs.value.ensembleConfigs.length;
	for (let i = 0; i < knobs.value.ensembleConfigs.length; i++) {
		knobs.value.ensembleConfigs[i].weight = percent;
	}
};

const onSelection = (id: string) => {
	emit('select-output', id);
};

function addMapping() {
	for (let i = 0; i < knobs.value.ensembleConfigs.length; i++) {
		knobs.value.ensembleConfigs[i].solutionMappings[newSolutionMappingKey.value] = '';
	}

	const state = _.cloneDeep(props.node.state);
	state.ensembleConfigs = knobs.value.ensembleConfigs;
	emit('update-state', state);
}

const runEnsemble = async () => {
	if (!datasetId.value || !currentDatasetFileName.value) return;
	const datasetMapping: { [index: string]: string } = {};
	datasetMapping[knobs.value.timestampColName] = 'timestamp';
	// Each key used in the ensemble configs is a dataset column.
	// add these columns used to the datasetMapping
	Object.keys(knobs.value.ensembleConfigs[0].solutionMappings).forEach((key) => {
		datasetMapping[key] = key;
	});

	const calibratePayload: EnsembleCalibrationCiemssRequest = {
		modelConfigs: knobs.value.ensembleConfigs,
		timespan: getTimespan({
			dataset: csvAsset.value,
			timestampColName: knobs.value.timestampColName
		}),
		dataset: {
			id: datasetId.value,
			filename: currentDatasetFileName.value,
			mappings: datasetMapping
		},
		engine: 'ciemss',
		extra: {
			num_particles: knobs.value.extra.numParticles,
			num_iterations: knobs.value.extra.numIterations,
			solver_method: knobs.value.extra.solverMethod
		}
	};
	const response = await makeEnsembleCiemssCalibration(calibratePayload, nodeMetadata(props.node));
	if (response?.simulationId) {
		const state = _.cloneDeep(props.node.state);
		state.inProgressCalibrationId = response?.simulationId;
		state.inProgressForecastId = '';

		emit('update-state', state);
	}
};

onMounted(async () => {
	allModelConfigurations.value = [];
	const modelConfigurationIds: string[] = [];
	props.node.inputs.forEach((ele) => {
		if (ele.value && ele.type === 'modelConfigId') modelConfigurationIds.push(ele.value[0]);
	});
	if (!modelConfigurationIds) return;

	// Model configuration input
	await Promise.all(
		modelConfigurationIds.map(async (id) => {
			const { modelConfiguration, modelOptions } = await setupModelInput(id);
			if (modelConfiguration) allModelConfigurations.value.push(modelConfiguration);
			if (modelOptions) allModelOptions.value.push(modelOptions);
		})
	);

	// dataset input
	if (datasetId.value) {
		// Get dataset
		const dataset: Dataset | null = await getDataset(datasetId.value);
		if (dataset) {
			const { filename, datasetOptions } = await setupDatasetInput(dataset);
			currentDatasetFileName.value = filename;
			datasetColumnNames.value = datasetOptions?.map((ele) => ele.name);
			setupCsvAsset(dataset).then((csv) => {
				csvAsset.value = csv;
			});
		}
	}

	listModelLabels.value = allModelConfigurations.value.map((ele) => ele.name ?? '');

	// initalize ensembleConfigs when its length is less than the amount of models provided to node (- 1 due to dataset, -1 due to last empty )
	if (knobs.value.ensembleConfigs.length < props.node.inputs.length - 2) {
		knobs.value.ensembleConfigs = [];
		for (let i = 0; i < allModelConfigurations.value.length; i++) {
			knobs.value.ensembleConfigs.push({
				id: allModelConfigurations.value[i].id as string,
				solutionMappings: {},
				weight: 0
			});
		}
	}

	if (knobs.value.ensembleConfigs.some((ele) => ele.weight === 0)) {
		calculateEvenWeights();
	}
});

watch(
	() => props.node.active,
	async () => {
		// Update selected output
		if (props.node.active) {
			selectedOutputId.value = props.node.active;

			const state = props.node.state;
			const output = await getRunResultCiemss(state.forecastRunId, 'result.csv');
			runResults.value = output.runResults;
		}
	},
	{ immediate: true }
);

watch(
	() => knobs.value,
	async () => {
		const state = _.cloneDeep(props.node.state);
		state.timestampColName = knobs.value.timestampColName;
		state.extra = knobs.value.extra;
		state.ensembleConfigs = knobs.value.ensembleConfigs;
		emit('update-state', state);
	},
	{ deep: true }
);
</script>

<style scoped>
.tera-ensemble {
	background: white;
	z-index: 1;
}

.model-weights {
	display: flex;
}

.ensemble-header {
	display: flex;
	margin: 1em;
}

th {
	text-align: left;
}

th,
td {
	padding-left: 0;
}

.ensemble-header-label {
	display: flex;
	align-items: center;
	margin: 0 1em;
	font-weight: 700;
	font-size: 1.75em;
}

:deep(.p-inputnumber-input, .p-inputwrapper) {
	width: 100%;
}
</style>
