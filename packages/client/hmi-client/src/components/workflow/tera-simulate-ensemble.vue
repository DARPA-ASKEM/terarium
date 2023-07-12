<template>
	<section class="tera-ensemble">
		<div class="ensemble-header p-buttonset">
			<span class="ensemble-header-label">Ensemble</span>
			<Button
				label="Input"
				severity="secondary"
				icon="pi pi-sign-in"
				size="small"
				:active="activeTab === EnsembleTabs.input"
				@click="activeTab = EnsembleTabs.input"
			/>
			<Button
				label="Ouput"
				severity="secondary"
				icon="pi pi-sign-out"
				size="small"
				:active="activeTab === EnsembleTabs.output"
				@click="activeTab = EnsembleTabs.output"
			/>
			<Button class="p-button-sm" label="Run" @click="runEnsemble" :disabled="disableRunButton" />
		</div>
		<div
			v-if="activeTab === EnsembleTabs.output && node?.outputs.length"
			class="simulate-container"
		>
			<p>Ensemble Output here</p>
		</div>

		<div v-else-if="activeTab === EnsembleTabs.input && node" class="simulate-container">
			<Accordion :multiple="true" :active-index="[0]">
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
											{{ weights[i] }}
										</td>
										<td v-else>
											<InputNumber v-model="weights[i]" />
										</td>
									</tr>
								</tbody>
							</table>
						</section>
					</div>
				</AccordionTab>
				<AccordionTab header="Mapping"> </AccordionTab>
				<AccordionTab header="Time Span">
					<table>
						<thead class="p-datatable-thead">
							<th>Units</th>
							<th>Start Step</th>
							<th>End Step</th>
						</thead>
						<tbody class="p-datatable-tbody">
							<td>Steps</td>
							<td><InputNumber v-model="timeSpan.start" /></td>
							<td><InputNumber v-model="timeSpan.end" /></td>
						</tbody>
					</table>
				</AccordionTab>
			</Accordion>
		</div>
	</section>
</template>

<script setup lang="ts">
// import _ from 'lodash';
import { ref, computed, watch, onMounted } from 'vue';
import { getSimulation, makeEnsembleSimulation } from '@/services/models/simulation-service';
import { getModelConfigurationById } from '@/services/model-configurations';
import { WorkflowNode } from '@/types/workflow';
import Button from 'primevue/button';
import AccordionTab from 'primevue/accordiontab';
import Accordion from 'primevue/accordion';
import InputNumber from 'primevue/inputnumber';
import {
	ModelConfiguration,
	EnsembleSimulationRequest,
	Simulation,
	TimeSpan,
	EnsembleModelConfigs
} from '@/types/Types';
// import Dropdown from 'primevue/dropdown';
import Chart from 'primevue/chart';
import { EnsembleOperation } from './simulate-ensemble-operation';
// import { workflowEventBus } from '@/services/workflow';
// import DataTable from 'primevue/datatable';
// import Column from 'primevue/column';
// import InputText from 'primevue/inputtext';

const props = defineProps<{
	node: WorkflowNode;
}>();

const emit = defineEmits(['append-output-port']);

enum EnsembleTabs {
	input,
	output
}
enum EnsembleCalibrationMode {
	EQUALWEIGHTS = 'equalWeights',
	CALIBRATIONWEIGHTS = 'calibrationWeights',
	CUSTOM = 'custom'
}
export interface Mapping {
	modelId: string;
	compartmentName: string;
	value: string;
}

const CATEGORYPERCENTAGE = 0.9;
const BARPERCENTAGE = 1.0;
const MINBARLENGTH = 1;

const activeTab = ref(EnsembleTabs.input);
const listModelIds = computed<string[]>(() => props.node.state.modelConfigIds);
// const listModels = ref<Model>();
const ensembleCalibrationMode = ref<string>(EnsembleCalibrationMode.EQUALWEIGHTS);
const allModelConfigurations = ref<ModelConfiguration[]>([]);
const allModelOptions = ref<string[][]>([]);
// List of each observible + state for each model.
const weights = ref<number[]>([]);
// const mapping = ref<Mapping[]>(props.node.state.mapping);

const timeSpan = ref<TimeSpan>({ start: 0, end: 90 });
const startedRunId = ref<string>();
const completedRunId = ref<string>();

const disableRunButton = computed(() => !weights.value);
const customWeights = ref<boolean>(false);
// TODO: Does AMR contain weights? Can i check all inputs have the weights parameter filled in or the calibration boolean checked off?
const disabledCalibrationWeights = computed(() => false);

function calculateWeights() {
	if (!listModelIds.value) return [];
	if (ensembleCalibrationMode.value === EnsembleCalibrationMode.EQUALWEIGHTS) {
		customWeights.value = false;
		const percent = 1 / listModelIds.value.length;
		const outputList: number[] = [];
		for (let i = 0; i < listModelIds.value.length; i++) {
			outputList.push(percent);
		}
		return outputList;
	}
	if (ensembleCalibrationMode.value === EnsembleCalibrationMode.CUSTOM) {
		customWeights.value = true;
	} else if (ensembleCalibrationMode.value === EnsembleCalibrationMode.CALIBRATIONWEIGHTS) {
		customWeights.value = false;
		console.log('TODO: Get weights from AMRs');
	}
	return [];
}

const runEnsemble = async () => {
	const ensembleConfigs: EnsembleModelConfigs[] = [];
	for (let i = 0; i < listModelIds.value.length; i++) {
		const id = listModelIds.value[i];
		const obs = {};
		const weight = weights.value[i];
		ensembleConfigs.push({
			id,
			observables: obs,
			weight
		});
	}
	const params: EnsembleSimulationRequest = {
		modelConfigs: ensembleConfigs,
		timespan: timeSpan.value,
		engine: 'sciml',
		extra: { num_samples: 100 }
	};
	const response = await makeEnsembleSimulation(params);
	startedRunId.value = response.id;
	getStatus();
};

const getStatus = async () => {
	if (!startedRunId.value) return;

	const currentSimulation: Simulation | null = await getSimulation(startedRunId.value); // get TDS's simulation object
	const ongoingStatusList = ['running', 'queued'];

	if (currentSimulation && currentSimulation.status === 'complete') {
		completedRunId.value = startedRunId.value;
		updateOutputPorts(completedRunId);
		// showSpinner.value = false;
	} else if (currentSimulation && ongoingStatusList.includes(currentSimulation.status)) {
		// recursively call until all runs retrieved
		setTimeout(getStatus, 3000);
	} else {
		// throw if there are any failed runs for now
		console.error('Failed', startedRunId.value);
		throw Error('Failed Runs');
	}
};

const updateOutputPorts = async (runId) => {
	const portLabel = props.node.inputs[0].label;
	emit('append-output-port', {
		type: EnsembleOperation.outputs[0].type,
		label: `${portLabel} Result`,
		value: {
			runId
		}
	});
};

// function addMapping(modelId: string) {
// 	console.log(modelId);
// 	mapping.value.push({
// 		modelId: modelId,
// 		compartmentName: "",
// 		value: ""
// 	});

// 	const state: EnsembleOperationState = _.cloneDeep(props.node.state);
// 	state.mapping = mapping.value;

// 	workflowEventBus.emitNodeStateChange({
// 		workflowId: props.node.workflowId,
// 		nodeId: props.node.id,
// 		state
// 	});
// };

const setBarChartData = () => {
	const documentStyle = getComputedStyle(document.documentElement);
	const datasetLabel: string[] = [];
	for (let i = 0; i < listModelIds.value.length; i++) {
		datasetLabel.push(listModelIds.value[i]);
	}
	return {
		labels: datasetLabel,
		datasets: [
			{
				backgroundColor: documentStyle.getPropertyValue('--primary-color'),
				borderColor: documentStyle.getPropertyValue('--primary-color'),
				data: weights.value,
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
				labels: {
					fontColor: documentStyle.getPropertyValue('--primary-color')
				},
				display: false
			}
		},
		scales: {
			x: {
				ticks: {
					color: documentStyle.getPropertyValue('--primary-color'),
					font: {
						weight: 500
					}
				},
				grid: {
					display: false,
					drawBorder: false
				}
			},
			y: {
				ticks: {
					color: documentStyle.getPropertyValue('--primary-color')
				},
				grid: {
					color: documentStyle.getPropertyValue('--surface-border-light'),
					drawBorder: false
				}
			}
		}
	};
};

watch(
	[() => ensembleCalibrationMode.value, listModelIds.value],
	async () => {
		weights.value = calculateWeights();
	},
	{ immediate: true }
);

onMounted(() => {
	watch(
		() => props.node.state.modelConfigIds,
		async () => {
			allModelConfigurations.value = [];
			// Fetch Model Configurations
			await Promise.all(
				listModelIds.value.map(async (id) => {
					const result = await getModelConfigurationById(id);
					allModelConfigurations.value.push(result);
				})
			);
			console.log(allModelConfigurations.value);
			allModelOptions.value = [];
			for (let i = 0; i < allModelConfigurations.value.length; i++) {
				const tempList = allModelConfigurations.value[i].configuration.model.states.map(
					(element) => element.id
				);
				tempList.push(
					allModelConfigurations.value[i].configuration.semantics.ode.observables.map(
						(element) => element.id
					)
				);
				allModelOptions.value.push(tempList);
			}
			weights.value = calculateWeights();
		},
		{ immediate: true }
	);
});
</script>

<style scoped>
.add-chart {
	width: 9em;
	margin: 0em 1em;
	margin-bottom: 1em;
}

.tera-ensemble {
	background: white;
	z-index: 1;
}

.ensemble-calibration-mode {
	display: grid;
	padding-left: 0.5rem;
}

.ensemble-calibration-graph {
	padding-left: 0.5rem;
	height: 200px;
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
