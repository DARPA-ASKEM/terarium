<template>
	<main>
		<template v-if="selectedRunId && runResults[selectedRunId]">
			<section v-for="(_config, index) of props.node.state.chartConfigs" :key="index">
				<vega-chart
					v-if="preparedCharts[index].layer.length > 0"
					:visualization-spec="preparedCharts[index]"
					:are-embed-actions-visible="false"
				/>
				<div v-else class="empty-chart">
					<img src="@assets/svg/seed.svg" alt="" draggable="false" class="empty-image" />
					<p class="helpMessage">No variables selected</p>
				</div>
			</section>
		</template>
		<tera-progress-spinner v-if="inProgressForecastId" :font-size="2" is-centered style="height: 100%" />
		<Button v-if="areInputsFilled" label="Edit" @click="emit('open-drilldown')" severity="secondary" outlined />
		<tera-operator-placeholder v-else :node="node"> Connect a model configuration </tera-operator-placeholder>
	</main>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ref, watch } from 'vue';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import {
	getRunResultCSV,
	// pollAction,
	getSimulation,
	parsePyCiemssMap,
	DataArray
} from '@/services/models/simulation-service';
import { getModelByModelConfigurationId, getUnitsFromModelParts } from '@/services/model';
// import { Poller, PollerState } from '@/api/api';
import { logger } from '@/utils/logger';
import { chartActionsProxy, nodeOutputLabel } from '@/components/workflow/util';

import type { WorkflowNode } from '@/types/workflow';
import { createLLMSummary } from '@/services/summary-service';
import { useProjects } from '@/composables/project';
import { createForecastChart, createInterventionChartMarkers } from '@/services/charts';
import { createDatasetFromSimulationResult } from '@/services/dataset';
import VegaChart from '@/components/widgets/VegaChart.vue';
import {
	ClientEvent,
	ClientEventType,
	ProgressState,
	Simulation,
	SimulationNotificationData,
	StatusUpdate,
	type InterventionPolicy,
	type Model
} from '@/types/Types';
import { flattenInterventionData, getInterventionPolicyById } from '@/services/intervention-policy';
import { useClientEvent } from '@/composables/useClientEvent';
import { SimulateCiemssOperationState, SimulateCiemssOperation } from './simulate-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateCiemssOperationState>;
}>();

const emit = defineEmits(['open-drilldown', 'update-state', 'append-output']);
const model = ref<Model | null>(null);
const modelVarUnits = ref<{ [key: string]: string }>({});

const runResults = ref<{ [runId: string]: DataArray }>({});
const runResultsSummary = ref<{ [runId: string]: DataArray }>({});

const selectedRunId = ref<string>();
const inProgressForecastId = computed(() => props.node.state.inProgressForecastId);
const inProgressBaseForecastId = computed(() => props.node.state.inProgressBaseForecastId);
const areInputsFilled = computed(() => props.node.inputs[0].value);
const interventionPolicyId = computed(() => props.node.inputs[1].value?.[0]);
const interventionPolicy = ref<InterventionPolicy | null>(null);

let pyciemssMap: Record<string, string> = {};

useClientEvent(
	ClientEventType.SimulationNotification,
	async (event: ClientEvent<StatusUpdate<SimulationNotificationData>>) => {
		const simulationNotificationData = event.data.data;
		if (
			simulationNotificationData.simulationId !== inProgressForecastId.value ||
			![ProgressState.Cancelled, ProgressState.Failed, ProgressState.Complete].includes(event.data.state)
		)
			return;

		const simId = simulationNotificationData.simulationId;
		const state = _.cloneDeep(props.node.state);
		state.errorMessage = { name: '', value: '', traceback: '' };
		state.inProgressForecastId = '';

		if (event.data.state === ProgressState.Failed) {
			const simulation = await getSimulation(simId);
			if (simulation?.status && simulation?.statusMessage) {
				state.errorMessage = {
					name: simId,
					value: simulation.status,
					traceback: simulation.statusMessage
				};
			}
		} else if (event.data.state === ProgressState.Complete) {
			await processResult(simId);
			state.forecastId = simId;
		}
		emit('update-state', state);
	}
);

useClientEvent(
	ClientEventType.SimulationNotification,
	async (event: ClientEvent<StatusUpdate<SimulationNotificationData>>) => {
		const simulationNotificationData = event.data.data;
		if (
			simulationNotificationData.simulationId !== inProgressBaseForecastId.value ||
			![ProgressState.Cancelled, ProgressState.Failed, ProgressState.Complete].includes(event.data.state)
		)
			return;

		const simId = simulationNotificationData.simulationId;
		const state = _.cloneDeep(props.node.state);
		state.errorMessage = { name: '', value: '', traceback: '' };
		state.inProgressBaseForecastId = '';
		if (event.data.state === ProgressState.Complete) {
			state.baseForecastId = simId;
		}
		emit('update-state', state);
	}
);

// const poller = new Poller();
// // TODO: replace poller with useClientEvent and check both base simulation and simulation with intervention as well.
// const pollResult = async (runId: string) => {
// 	selectedRunId.value = undefined;

// 	poller
// 		.setInterval(3000)
// 		.setThreshold(300)
// 		.setPollAction(async () => pollAction(runId));
// 	const pollerResults = await poller.start();
// 	let state = _.cloneDeep(props.node.state);
// 	state.errorMessage = { name: '', value: '', traceback: '' };

// 	if (pollerResults.state === PollerState.Cancelled) {
// 		state.inProgressForecastId = '';
// 		poller.stop();
// 	} else if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
// 		// throw if there are any failed runs for now
// 		logger.error(`Simulation: ${runId} has failed`, {
// 			toastTitle: 'Error - Pyciemss'
// 		});
// 		const simulation = await getSimulation(runId);
// 		if (simulation?.status && simulation?.statusMessage) {
// 			state = _.cloneDeep(props.node.state);
// 			state.inProgressForecastId = '';
// 			state.errorMessage = {
// 				name: runId,
// 				value: simulation.status,
// 				traceback: simulation.statusMessage
// 			};
// 			emit('update-state', state);
// 		}
// 		throw Error('Failed Runs');
// 	}
// 	emit('update-state', state);
// 	return pollerResults;
// };

const chartProxy = chartActionsProxy(props.node, (state: SimulateCiemssOperationState) => {
	emit('update-state', state);
});

const processResult = async (runId: string) => {
	const state = _.cloneDeep(props.node.state);
	if (interventionPolicyId.value && _.isEmpty(state.chartConfigs)) {
		_.keys(groupedInterventionOutputs.value).forEach((key) => {
			chartProxy.addChart([key]);
		});
	} else if (_.isEmpty(state.chartConfigs)) {
		chartProxy.addChart();
	}

	const summaryData = await getRunResultCSV(runId, 'result_summary.csv');
	const start = _.first(summaryData);
	const end = _.last(summaryData);

	const prompt = `
The following are the key attributes of a simulation/forecasting process for a ODE epidemilogy model.

The input parameters are as follows:
- samples: ${state.numSamples}
- method: ${state.method}
- timespan: ${JSON.stringify(state.currentTimespan)}
- interventions: ${JSON.stringify(interventionPolicy.value?.interventions)};


The output has these metrics at the start:
- ${JSON.stringify(start)}

The output has these metrics at the end:
- ${JSON.stringify(end)}

Provide a summary in 100 words or less.
	`;

	const summaryResponse = await createLLMSummary(prompt);

	const datasetName = `Forecast run ${runId}`;
	const projectId = useProjects().activeProject.value?.id ?? '';
	const datasetResult = await createDatasetFromSimulationResult(projectId, runId, datasetName, false);
	if (!datasetResult) {
		logger.error('Error creating dataset from simulation result.');
		return;
	}
	emit('append-output', {
		type: SimulateCiemssOperation.outputs[0].type,
		label: nodeOutputLabel(props.node, 'Dataset'),
		value: [datasetResult.id],
		state: {
			currentTimespan: state.currentTimespan,
			numSamples: state.numSamples,
			method: state.method,
			summaryId: summaryResponse?.id,
			forecastId: runId
		},
		isSelected: false
	});
};
const groupedInterventionOutputs = computed(() =>
	_.groupBy(flattenInterventionData(interventionPolicy.value?.interventions ?? []), 'appliedTo')
);

const preparedCharts = computed(() => {
	if (!selectedRunId.value) return [];
	const result = runResults.value[selectedRunId.value];
	const resultSummary = runResultsSummary.value[selectedRunId.value];
	const reverseMap: Record<string, string> = {};
	Object.keys(pyciemssMap).forEach((key) => {
		reverseMap[`${pyciemssMap[key]}_mean`] = key;
	});

	return props.node.state.chartConfigs.map((config) => {
		const chart = createForecastChart(
			{
				data: result,
				variables: config.map((d) => pyciemssMap[d]),
				timeField: 'timepoint_id',
				groupField: 'sample_id'
			},
			{
				data: resultSummary,
				variables: config.map((d) => `${pyciemssMap[d]}_mean`),
				timeField: 'timepoint_id'
			},
			null,
			// options
			{
				title: '',
				width: 180,
				height: 120,
				legend: true,
				translationMap: reverseMap,
				xAxisTitle: modelVarUnits.value._time || 'Time',
				yAxisTitle: _.uniq(config.map((v) => modelVarUnits.value[v]).filter((v) => !!v)).join(',') || ''
			}
		);
		if (interventionPolicy.value) {
			_.keys(groupedInterventionOutputs.value).forEach((key) => {
				if (config.includes(key)) {
					chart.layer.push(...createInterventionChartMarkers(groupedInterventionOutputs.value[key]));
				}
			});
		}
		return chart;
	});
});

watch(
	() => props.node.inputs[0].value,
	async () => {
		const input = props.node.inputs[0];
		if (!input.value) return;

		const id = input.value[0];
		model.value = await getModelByModelConfigurationId(id);
		modelVarUnits.value = getUnitsFromModelParts(model.value as Model);
	},
	{ immediate: true }
);

// watch(
// 	() => props.node.state.inProgressForecastId,
// 	async (id) => {
// 		if (!id || id === '') return;

// 		const response = await pollResult(id);
// 		if (response.state === PollerState.Done) {
// 			await processResult(id);
// 		}
// 		const state = _.cloneDeep(props.node.state);
// 		state.inProgressForecastId = '';
// 		state.forecastId = id;
// 		emit('update-state', state);
// 	},
// 	{ immediate: true }
// );

watch(
	() => props.node.state.inProgressForecastId,
	async (id) => {
		if (!id || id === '') return;
		// Check simulation status and update the state
		const simResponse: Simulation | null = await getSimulation(id);
		if (simResponse?.status !== ProgressState.Complete) return;
		await processResult(id);
		const state = _.cloneDeep(props.node.state);
		state.inProgressForecastId = '';
		state.forecastId = id;
		emit('update-state', state);
	},
	{ immediate: true }
);

watch(
	() => props.node.state.inProgressBaseForecastId,
	async (id) => {
		if (!id || id === '') return;
		// Check base simulation (without intervention) status and update the state
		const simResponse: Simulation | null = await getSimulation(id);
		if (simResponse?.status !== ProgressState.Complete) return;
		const state = _.cloneDeep(props.node.state);
		state.inProgressBaseForecastId = '';
		state.baseForecastId = id;
		emit('update-state', state);
	},
	{ immediate: true }
);

watch(
	() => props.node.active,
	async () => {
		const active = props.node.active;
		if (!active) return;

		selectedRunId.value = props.node.outputs.find((o) => o.id === active)?.value?.[0];
		const forecastId = props.node.state.forecastId;
		if (!forecastId || !selectedRunId.value) return;

		const result = await getRunResultCSV(forecastId, 'result.csv');
		pyciemssMap = parsePyCiemssMap(result[0]);
		runResults.value[selectedRunId.value] = result;

		const resultSummary = await getRunResultCSV(forecastId, 'result_summary.csv');
		runResultsSummary.value[selectedRunId.value] = resultSummary;
	},
	{ immediate: true }
);

watch(
	() => interventionPolicyId.value,
	() => {
		if (interventionPolicyId.value) {
			getInterventionPolicyById(interventionPolicyId.value).then((policy) => {
				interventionPolicy.value = policy;
			});
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
:deep(.vega-chart-container) {
	margin-bottom: 0;
}

.empty-chart {
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	height: 9rem;
	gap: var(--gap);
	background: var(--surface-50);
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	margin-bottom: var(--gap);
	color: var(--text-color-secondary);
	font-size: var(--font-caption);
}
.empty-image {
	width: 5rem;
	height: 5rem;
}
</style>
