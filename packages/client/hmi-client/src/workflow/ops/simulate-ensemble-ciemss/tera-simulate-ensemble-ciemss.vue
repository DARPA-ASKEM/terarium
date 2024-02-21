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
								v-if="ensembleCalibrationMode === EnsembleCalibrationMode.EQUALWEIGHTS"
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

							<tr v-for="key in Object.keys(ensembleConfigs[0].solutionMappings)" :key="key">
								<td>{{ key }}</td>
								<td v-for="config in ensembleConfigs" :key="config.id">
									<Dropdown
										:options="allModelOptions[config.id]"
										v-model="config.solutionMappings[key]"
									/>
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
		<template #preview>
			<tera-drilldown-preview
				title="Simulation output"
				:options="outputs"
				v-model:output="selectedOutputId"
				is-selectable
				:is-loading="showSpinner"
				@update:output="onUpdateOutput"
				@update:selection="onUpdateSelection"
			>
				<tera-simulate-chart
					v-for="(cfg, index) of node.state.chartConfigs"
					:key="index"
					:run-results="runResults"
					:chartConfig="cfg"
					has-mean-line
					@configuration-change="chartConfigurationChange(index, $event)"
				/>
			</tera-drilldown-preview>
		</template>
		<template #footer>
			<Button
				outlined
				:style="{ marginRight: 'auto' }"
				label="Run"
				icon="pi pi-play"
				@click="runEnsemble"
				:disabled="false"
			/>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, computed, watch, onMounted } from 'vue';
import Button from 'primevue/button';
import AccordionTab from 'primevue/accordiontab';
import Accordion from 'primevue/accordion';
import InputNumber from 'primevue/inputnumber';
import InputText from 'primevue/inputtext';
import Dropdown from 'primevue/dropdown';
import Chart from 'primevue/chart';
import ChartDataLabels from 'chartjs-plugin-datalabels';

import { Poller, PollerState } from '@/api/api';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';

import {
	getRunResultCiemss,
	makeEnsembleCiemssSimulation,
	simulationPollAction
} from '@/services/models/simulation-service';
import { getModelConfigurationById } from '@/services/model-configurations';
import { logger } from '@/utils/logger';

import type { WorkflowNode } from '@/types/workflow';
import type {
	TimeSpan,
	EnsembleModelConfigs,
	EnsembleSimulationCiemssRequest
} from '@/types/Types';
import { ProgressState } from '@/types/Types';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { SimulateEnsembleCiemssOperationState } from './simulate-ensemble-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateEnsembleCiemssOperationState>;
}>();
const emit = defineEmits([
	'append-output',
	'select-output',
	'update-output-port',
	'update-state',
	'close'
]);

const dataLabelPlugin = [ChartDataLabels];

enum Tabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}

enum EnsembleCalibrationMode {
	EQUALWEIGHTS = 'equalWeights',
	CUSTOM = 'custom'
}

const CATEGORYPERCENTAGE = 0.9;
const BARPERCENTAGE = 0.6;
const MINBARLENGTH = 1;

const poller = new Poller();
const showSpinner = ref(false);

const listModelIds = computed<string[]>(() => props.node.state.modelConfigIds);
const listModelLabels = ref<string[]>([]);
const ensembleCalibrationMode = ref<string>(EnsembleCalibrationMode.EQUALWEIGHTS);

// List of each observible + state for each model.
const allModelOptions = ref<{ [key: string]: string[] }>({});
const ensembleConfigs = ref<EnsembleModelConfigs[]>(props.node.state.mapping);

const timeSpan = ref<TimeSpan>(props.node.state.timeSpan);
const numSamples = ref<number>(props.node.state.numSamples);

// const showSaveInput = ref(<boolean>false);
// const saveAsName = ref(<string | null>'');

const customWeights = ref<boolean>(false);
const newSolutionMappingKey = ref<string>('');
const runResults = ref<RunResults>({});
const progress = ref({ status: ProgressState.Retrieving, value: 0 });

// Preview selection
const outputs = computed(() => {
	if (!_.isEmpty(props.node.outputs)) {
		return [
			{
				label: 'Select outputs',
				items: props.node.outputs
			}
		];
	}
	return [];
});
const selectedOutputId = ref<string>();

const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	emit('update-state', state);
};

const onUpdateOutput = (id) => {
	emit('select-output', id);
};

const onUpdateSelection = (id) => {
	const outputPort = _.cloneDeep(props.node.outputs?.find((port) => port.id === id));
	if (!outputPort) return;
	outputPort.isSelected = !outputPort?.isSelected;
	emit('update-output-port', outputPort);
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
	}
};

const addMapping = () => {
	for (let i = 0; i < ensembleConfigs.value.length; i++) {
		ensembleConfigs.value[i].solutionMappings[newSolutionMappingKey.value] = '';
	}

	const state = _.cloneDeep(props.node.state);
	state.mapping = ensembleConfigs.value;

	emit('update-state', state);
};

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

const runEnsemble = async () => {
	const params: EnsembleSimulationCiemssRequest = {
		modelConfigs: ensembleConfigs.value,
		timespan: timeSpan.value,
		engine: 'ciemss',
		extra: { num_samples: numSamples.value }
	};
	const response = await makeEnsembleCiemssSimulation(params);

	// Start polling
	if (response?.simulationId) {
		getStatus(response.simulationId);
	}
};

const getStatus = async (simulationId: string) => {
	showSpinner.value = true;
	if (!simulationId) return;

	const runIds = [simulationId];
	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => simulationPollAction(runIds, props.node, progress, emit));
	const pollerResults = await poller.start();

	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		showSpinner.value = false;
		logger.error(`Simulate Ensemble: ${simulationId} has failed`, {
			toastTitle: 'Error - Pyciemss'
		});
		throw Error('Failed Runs');
	}
	showSpinner.value = false;
	updateOutputPorts(simulationId);
};

const updateOutputPorts = (simulationId: string) => {
	const portLabel = props.node.inputs[0].label;
	const state = props.node.state;
	emit('append-output', {
		type: 'simulationId',
		label: `${portLabel} Result`,
		value: [simulationId],
		state: {
			mapping: _.cloneDeep(state.mapping),
			timeSpan: _.cloneDeep(state.timeSpan),
			numSamples: state.numSamples
		},
		isSelected: false
	});
};

onMounted(async () => {
	// FIXME: probably switch to multiport instead of multivalue
	const modelConfigurationIds = props.node.inputs[0]?.value;
	if (!modelConfigurationIds) return;

	const allModelConfigurations = await Promise.all(
		modelConfigurationIds.map((id) => getModelConfigurationById(id))
	);

	allModelOptions.value = {};
	for (let i = 0; i < allModelConfigurations.length; i++) {
		const tempList: string[] = [];
		const amr = allModelConfigurations[i].configuration;
		amr.model.states?.forEach((element) => {
			tempList.push(element.id);
		});
		amr.semantics.ode.observables?.forEach((element) => tempList.push(element.id));
		allModelOptions.value[allModelConfigurations[i].id as string] = tempList;
	}
	calculateWeights();
	listModelLabels.value = allModelConfigurations.map((ele) => ele.name);

	const state = _.cloneDeep(props.node.state);
	state.modelConfigIds = modelConfigurationIds;

	if (state.mapping && state.mapping.length === 0) {
		for (let i = 0; i < allModelConfigurations.length; i++) {
			ensembleConfigs.value.push({
				id: allModelConfigurations[i].id as string,
				solutionMappings: {},
				weight: 0
			});
		}
	}

	if (state.chartConfigs.length === 0) {
		state.chartConfigs.push({ selectedVariable: [], selectedRun: '' });
	}

	emit('update-state', state);
});

watch(
	() => props.node.active,
	async () => {
		const output = props.node.outputs.find((d) => d.id === props.node.active);
		if (!output || !output.value) return;

		selectedOutputId.value = output.id;

		const response = await getRunResultCiemss(output.value[0], 'result.csv');
		runResults.value = response.runResults;
	},
	{ immediate: true }
);

watch(
	[() => ensembleCalibrationMode.value, listModelIds.value],
	async () => {
		calculateWeights();
	},
	{ immediate: true }
);

watch(
	() => [timeSpan.value, numSamples.value],
	async () => {
		const state = _.cloneDeep(props.node.state);
		state.timeSpan = timeSpan.value;
		state.numSamples = numSamples.value;

		emit('update-state', state);
	},
	{ immediate: true }
);
</script>

<style scoped>
.row-header {
	display: flex;
	flex-direction: column;
}

.row-header td {
	margin: 1rem 0;
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
	height: 100px;
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

:deep(.p-inputnumber-input, .p-inputwrapper) {
	width: 100%;
}
</style>
