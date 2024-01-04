<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section>
			<section class="tera-ensemble">
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
				<div v-if="view === CalibrateView.Output && runResults" class="simulate-container">
					<tera-simulate-chart
						v-for="(cfg, index) of node.state.chartConfigs"
						:key="index"
						:run-results="runResults"
						:initial-data="csvAsset"
						:chartConfig="cfg"
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
				</div>

				<div v-else-if="view === CalibrateView.Input && node" class="simulate-container">
					<Accordion :multiple="true" :active-index="[0, 1, 2]">
						<AccordionTab header="Model Weights">
							<div class="model-weights">
								<section class="ensemble-calibration-mode">
									<label>
										<input
											type="radio"
											v-model="ensembleCalibrationMode"
											:value="EnsembleCalibrationMode.EQUALWEIGHTS"
										/>
										{{ EnsembleCalibrationMode.EQUALWEIGHTS }}
									</label>
									<label>
										<input
											type="radio"
											v-model="ensembleCalibrationMode"
											:value="EnsembleCalibrationMode.CALIBRATIONWEIGHTS"
											:disabled="disabledCalibrationWeights"
										/>
										{{ EnsembleCalibrationMode.CALIBRATIONWEIGHTS }}
									</label>
									<label>
										<input
											type="radio"
											v-model="ensembleCalibrationMode"
											:value="EnsembleCalibrationMode.CUSTOM"
										/>
										{{ EnsembleCalibrationMode.CUSTOM }}
									</label>
								</section>
								<!-- Turn this into a horizontal bar chart -->
								<section class="ensemble-calibration-graph">
									<Chart
										v-if="
											ensembleCalibrationMode === EnsembleCalibrationMode.CALIBRATIONWEIGHTS ||
											ensembleCalibrationMode === EnsembleCalibrationMode.EQUALWEIGHTS
										"
										type="bar"
										:height="200"
										:data="setBarChartData()"
										:options="setChartOptions()"
										:plugins="dataLabelPlugin"
									/>
									<table v-else class="p-datatable-table">
										<thead class="p-datatable-thead">
											<th>Model Config ID</th>
											<th>Weight</th>
										</thead>
										<tbody class="p-datatable-tbody">
											<tr v-for="(id, i) in listModelIds" :key="i">
												<td>
													{{ id }}
												</td>
												<td v-if="customWeights === false">
													{{ ensembleConfigs[i].weight }}
												</td>
												<td v-else>
													<InputNumber
														mode="decimal"
														:min-fraction-digits="0"
														:max-fraction-digits="7"
														v-model="ensembleConfigs[i].weight"
													/>
												</td>
											</tr>
										</tbody>
									</table>
								</section>
							</div>
						</AccordionTab>
						<AccordionTab header="Mapping">
							<template v-if="ensembleConfigs.length > 0">
								<table>
									<tr>
										<th>Ensemble Variables</th>
										<th v-for="(element, i) in ensembleConfigs" :key="i">
											{{ element.id }}
										</th>
									</tr>
									<tr>
										<div class="row-header">
											<td
												v-for="(element, i) in Object.keys(ensembleConfigs[0].solutionMappings)"
												:key="i"
											>
												{{ element }}
											</td>
										</div>
										<td v-for="i in ensembleConfigs.length" :key="i">
											<template
												v-for="element in Object.keys(ensembleConfigs[i - 1].solutionMappings)"
												:key="element"
											>
												<Dropdown
													v-model="ensembleConfigs[i - 1].solutionMappings[element]"
													:options="allModelOptions[i - 1]"
												/>
											</template>
										</td>
									</tr>
								</table>
							</template>

							<Dropdown
								style="width: 50%"
								v-model="newSolutionMappingKey"
								:options="datasetColumnNames"
								placeholder="Variable Name"
							/>
							<Button
								class="p-button-sm p-button-outlined"
								icon="pi pi-plus"
								label="Add mapping"
								@click="addMapping"
							/>
						</AccordionTab>
						<AccordionTab header="Time Span">
							<table>
								<thead class="p-datatable-thead">
									<th>Units</th>
									<th>Number of Samples</th>
									<th>Number of iterations</th>
									<th>Total Population</th>
								</thead>
								<tbody class="p-datatable-tbody">
									<td>Steps</td>
									<td>
										<InputNumber v-model="extra.numSamples" />
									</td>
									<td>
										<InputNumber v-model="extra.numIterations" />
									</td>
									<td>
										<InputNumber v-model="extra.totalPopulation" />
									</td>
								</tbody>
							</table>
						</AccordionTab>
					</Accordion>
				</div>
			</section>
		</section>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, shallowRef, computed, watch } from 'vue';
import { getRunResultCiemss } from '@/services/models/simulation-service';
import { getModelConfigurationById } from '@/services/model-configurations';
import { WorkflowNode } from '@/types/workflow';
import Button from 'primevue/button';
import AccordionTab from 'primevue/accordiontab';
import Accordion from 'primevue/accordion';
import InputNumber from 'primevue/inputnumber';
import { CsvAsset, ModelConfiguration, EnsembleModelConfigs } from '@/types/Types';
import Dropdown from 'primevue/dropdown';
import Chart from 'primevue/chart';
import ChartDataLabels from 'chartjs-plugin-datalabels';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { setupDatasetInput } from '@/services/calibrate-workflow';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import { saveDataset } from '@/services/dataset';
import { useProjects } from '@/composables/project';
import SelectButton from 'primevue/selectbutton';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import {
	CalibrateEnsembleCiemssOperationState,
	EnsembleCalibrateExtraCiemss
} from './calibrate-ensemble-ciemss-operation';

const dataLabelPlugin = [ChartDataLabels];

const props = defineProps<{
	node: WorkflowNode<CalibrateEnsembleCiemssOperationState>;
}>();
const emit = defineEmits(['append-output-port', 'update-state', 'close']);

enum CalibrateView {
	Input = 'Input',
	Output = 'Output'
}
enum EnsembleCalibrationMode {
	EQUALWEIGHTS = 'equalWeights',
	CALIBRATIONWEIGHTS = 'calibrationWeights',
	CUSTOM = 'custom'
}

const CATEGORYPERCENTAGE = 0.9;
const BARPERCENTAGE = 0.6;
const MINBARLENGTH = 1;

const showSaveInput = ref(<boolean>false);
const saveAsName = ref(<string | null>'');
const hasValidDatasetName = computed<boolean>(() => saveAsName.value !== '');

const view = ref(CalibrateView.Input);
const viewOptions = ref([
	{ value: CalibrateView.Input, icon: 'pi pi-sign-in' },
	{ value: CalibrateView.Output, icon: 'pi pi-sign-out' }
]);
const listModelIds = computed<string[]>(() => props.node.state.modelConfigIds);
const datasetId = computed(() => props.node.inputs[1].value?.[0] as string | undefined);
const currentDatasetFileName = ref<string>();
const datasetColumnNames = ref<string[]>();

const listModelLabels = ref<string[]>([]);
const ensembleCalibrationMode = ref<string>(EnsembleCalibrationMode.EQUALWEIGHTS);
const allModelConfigurations = ref<ModelConfiguration[]>([]);
// List of each observible + state for each model.
const allModelOptions = ref<string[][]>([]);
const ensembleConfigs = ref<EnsembleModelConfigs[]>(props.node.state.mapping);

const extra = ref<EnsembleCalibrateExtraCiemss>(props.node.state.extra);

const completedRunId = computed<string>(
	() => props?.node?.outputs?.[0]?.value?.[0].runId as string
);

const customWeights = ref<boolean>(false);
// TODO: Does AMR contain weights? Can i check all inputs have the weights parameter filled in or the calibration boolean checked off?
const disabledCalibrationWeights = computed(() => true);
const newSolutionMappingKey = ref<string>('');
const runResults = ref<RunResults>({});

const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

// Tom TODO: Make this generic... its copy paste from node.
const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	emit('update-state', state);
};

const calculateWeights = () => {
	if (!ensembleConfigs.value) return;
	if (ensembleCalibrationMode.value === EnsembleCalibrationMode.EQUALWEIGHTS) {
		customWeights.value = false;
		const percent = 1 / ensembleConfigs.value.length;
		for (let i = 0; i < ensembleConfigs.value.length; i++) {
			ensembleConfigs.value[i].weight = percent;
		}
	}
	if (ensembleCalibrationMode.value === EnsembleCalibrationMode.CUSTOM) {
		customWeights.value = true;
	} else if (ensembleCalibrationMode.value === EnsembleCalibrationMode.CALIBRATIONWEIGHTS) {
		customWeights.value = false;
		// TODO: Get weights from AMR
	}
};

function addMapping() {
	for (let i = 0; i < ensembleConfigs.value.length; i++) {
		ensembleConfigs.value[i].solutionMappings[newSolutionMappingKey.value] = '';
	}

	const state = _.cloneDeep(props.node.state);
	state.mapping = ensembleConfigs.value;

	emit('update-state', state);
}

const setBarChartData = () => {
	const documentStyle = getComputedStyle(document.documentElement);
	const weights = ensembleConfigs.value.map((element) => element.weight);
	return {
		labels: listModelLabels.value,
		datasets: [
			{
				backgroundColor: documentStyle.getPropertyValue('--text-color-secondary'),
				borderColor: documentStyle.getPropertyValue('--text-color-secondary'),
				data: weights,
				categoryPercentage: CATEGORYPERCENTAGE,
				barPercentage: BARPERCENTAGE,
				minBarLength: MINBARLENGTH
			}
		]
	};
};

const setChartOptions = () => {
	const documentStyle = getComputedStyle(document.documentElement);
	return {
		indexAxis: 'y',
		maintainAspectRatio: false,
		aspectRatio: 0.8,
		plugins: {
			legend: {
				display: false
			},
			datalabels: {
				anchor: 'end',
				align: 'right',
				formatter: (n: number) => `${Math.round(n * 100)}%`,
				labels: {
					value: {
						font: {
							size: 12
						}
					}
				}
			}
		},
		scales: {
			x: {
				display: false
			},
			y: {
				ticks: {
					color: documentStyle.getPropertyValue('--text-color-primary')
				},
				grid: {
					display: false,
					drawBorder: false
				}
			}
		}
	};
};

const addChart = () => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs.push({ selectedVariable: [], selectedRun: '' } as ChartConfig);

	emit('update-state', state);
};

// assume only one run for now
const watchCompletedRunList = async () => {
	if (!completedRunId.value) return;

	const output = await getRunResultCiemss(completedRunId.value, 'result.csv');
	runResults.value = output.runResults;
};

async function saveDatasetToProject() {
	const { activeProject, refresh } = useProjects();
	if (activeProject.value?.id) {
		if (await saveDataset(activeProject.value.id, completedRunId.value, saveAsName.value)) {
			refresh();
		}
		showSaveInput.value = false;
	}
}

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

watch(() => completedRunId.value, watchCompletedRunList, { immediate: true });

watch(
	[() => ensembleCalibrationMode.value, listModelIds.value],
	async () => {
		calculateWeights();
	},
	{ immediate: true }
);

watch(
	() => listModelIds,
	async () => {
		allModelConfigurations.value = [];
		// Fetch Model Configurations
		await Promise.all(
			listModelIds.value.map(async (id) => {
				const result = await getModelConfigurationById(id);
				allModelConfigurations.value.push(result);
			})
		);
		allModelOptions.value = [];
		for (let i = 0; i < allModelConfigurations.value.length; i++) {
			const tempList: string[] = [];
			allModelConfigurations.value[i].configuration.model.states?.forEach((element) => {
				tempList.push(element.id);
			});
			allModelConfigurations.value[i].configuration.semantics.ode.observables?.forEach((element) =>
				tempList.push(element.id)
			);
			allModelOptions.value.push(tempList);
		}
		calculateWeights();
		listModelLabels.value = allModelConfigurations.value.map((ele) => ele.name);

		const state = _.cloneDeep(props.node.state);
		state.mapping = ensembleConfigs.value;

		emit('update-state', state);
	},
	{ immediate: true }
);

watch(
	() => extra.value,
	async () => {
		const state = _.cloneDeep(props.node.state);
		state.extra = extra.value;

		emit('update-state', state);
	},
	{ immediate: true }
);
</script>

<style scoped>
.add-chart {
	width: 9em;
	margin: 0em 1em;
	margin-bottom: 1em;
}

.row-header {
	display: flex;
	flex-direction: column;
}

.row-header td {
	margin: 1rem 0;
}

.tera-ensemble {
	background: white;
	z-index: 1;
}

.ensemble-calibration-mode {
	display: flex;
	flex-direction: column;
	justify-content: center;
	gap: 1rem;
	margin-left: 1rem;
	min-width: fit-content;
	padding-right: 3rem;
}

.ensemble-calibration-graph {
	/* margin-left: 1rem; */
	height: 200px;
	/* width: 80%; */
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
	padding-left: 15px;
}

.ensemble-header-label {
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
