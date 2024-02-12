<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section :tabName="Tabs.Wizard">
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
									:value="EnsembleCalibrationMode.CUSTOM"
								/>
								{{ EnsembleCalibrationMode.CUSTOM }}
							</label>
						</section>
						<section class="ensemble-calibration-graph">
							<Chart
								v-if="
									ensembleCalibrationMode === EnsembleCalibrationMode.CALIBRATIONWEIGHTS ||
									ensembleCalibrationMode === EnsembleCalibrationMode.EQUALWEIGHTS
								"
								type="bar"
								:height="100"
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

					<InputText v-model="newSolutionMappingKey" placeholder="Variable Name" />
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
							<th>Start Step</th>
							<th>End Step</th>
							<th>Number of Samples</th>
						</thead>
						<tbody class="p-datatable-tbody">
							<td>Steps</td>
							<td>
								<InputNumber v-model="timeSpan.start" />
							</td>
							<td>
								<InputNumber v-model="timeSpan.end" />
							</td>
							<td>
								<InputNumber v-model="numSamples" />
							</td>
						</tbody>
					</table>
				</AccordionTab>
			</Accordion>
		</section>
		<section :tabName="Tabs.Notebook"></section>
		<template #preview> preview pane </template>
		<template #footer>
			<Button
				outlined
				:style="{ marginRight: 'auto' }"
				label="Run"
				icon="pi pi-play"
				@click="true"
				:disabled="false"
			/>
		</template>
	</tera-drilldown>

	<!--
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section tabName="Wizard">
			<section class="tera-ensemble">
				<div class="ensemble-header">
				</div>
				<div v-if="view === SimulateView.Output && runResults" class="simulate-container">
					<tera-simulate-chart
						v-for="(cfg, index) of node.state.chartConfigs"
						:key="index"
						:run-results="runResults"
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
			</section>
		</section>
	</tera-drilldown>
	-->
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, computed, watch } from 'vue';
import { getRunResultCiemss } from '@/services/models/simulation-service';
import { getModelConfigurationById } from '@/services/model-configurations';
import { WorkflowNode } from '@/types/workflow';
import Button from 'primevue/button';
import AccordionTab from 'primevue/accordiontab';
import Accordion from 'primevue/accordion';
import InputNumber from 'primevue/inputnumber';
import type { ModelConfiguration, TimeSpan, EnsembleModelConfigs } from '@/types/Types';
import Dropdown from 'primevue/dropdown';
import Chart from 'primevue/chart';
import ChartDataLabels from 'chartjs-plugin-datalabels';
import InputText from 'primevue/inputtext';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
// import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
// import { saveDataset } from '@/services/dataset';
// import { useProjects } from '@/composables/project';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import { SimulateEnsembleCiemssOperationState } from './simulate-ensemble-ciemss-operation';

console.log('1' as any as ChartConfig);

const dataLabelPlugin = [ChartDataLabels];

const props = defineProps<{
	node: WorkflowNode<SimulateEnsembleCiemssOperationState>;
}>();
const emit = defineEmits(['append-output-port', 'update-state', 'close']);

enum Tabs {
	Wizard = 'wizasrd',
	Notebook = 'notebook'
}

enum EnsembleCalibrationMode {
	EQUALWEIGHTS = 'equalWeights',
	CALIBRATIONWEIGHTS = 'calibrationWeights',
	CUSTOM = 'custom'
}

const CATEGORYPERCENTAGE = 0.9;
const BARPERCENTAGE = 0.6;
const MINBARLENGTH = 1;

const listModelIds = computed<string[]>(() => props.node.state.modelConfigIds);
const listModelLabels = ref<string[]>([]);
const ensembleCalibrationMode = ref<string>(EnsembleCalibrationMode.EQUALWEIGHTS);
const allModelConfigurations = ref<ModelConfiguration[]>([]);
// List of each observible + state for each model.
const allModelOptions = ref<string[][]>([]);
const ensembleConfigs = ref<EnsembleModelConfigs[]>(props.node.state.mapping);

const timeSpan = ref<TimeSpan>(props.node.state.timeSpan);
const numSamples = ref<number>(props.node.state.numSamples);
const completedRunId = computed<string>(
	() => props?.node?.outputs?.[0]?.value?.[0].runId as string
);

// const hasValidDatasetName = computed<boolean>(() => saveAsName.value !== '');
// const showSaveInput = ref(<boolean>false);
// const saveAsName = ref(<string | null>'');

const customWeights = ref<boolean>(false);
const newSolutionMappingKey = ref<string>('');
const runResults = ref<RunResults>({});

// Tom TODO: Make this generic... its copy paste from node.
// const chartConfigurationChange = (index: number, config: ChartConfig) => {
// 	const state = _.cloneDeep(props.node.state);
// 	state.chartConfigs[index] = config;
//
// 	emit('update-state', state);
// };

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

// const addChart = () => {
// 	const state = _.cloneDeep(props.node.state);
// 	state.chartConfigs.push({ selectedVariable: [], selectedRun: '' } as ChartConfig);
//
// 	emit('update-state', state);
// };
//
// async function saveDatasetToProject() {
// 	const { activeProject, refresh } = useProjects();
// 	if (activeProject.value?.id) {
// 		if (await saveDataset(activeProject.value.id, completedRunId.value, saveAsName.value)) {
// 			refresh();
// 		}
// 		showSaveInput.value = false;
// 	}
// }

// assume only one run for now
const watchCompletedRunList = async () => {
	if (!completedRunId.value) return;

	const output = await getRunResultCiemss(completedRunId.value, 'result.csv');
	runResults.value = output.runResults;
};

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
	() => timeSpan.value,
	async () => {
		const state = _.cloneDeep(props.node.state);
		state.timeSpan = timeSpan.value;

		emit('update-state', state);
	},
	{ immediate: true }
);

watch(
	() => numSamples.value,
	async () => {
		const state = _.cloneDeep(props.node.state);
		state.numSamples = numSamples.value;

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
	height: 100px;
	/* width: 80%; */
}

.model-weights {
	display: flex;
	align-items: start;
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
