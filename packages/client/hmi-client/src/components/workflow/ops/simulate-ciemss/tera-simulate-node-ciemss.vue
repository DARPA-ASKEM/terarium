<template>
	<main>
		<tera-progress-spinner v-if="inProgressForecastRun" :font-size="2" is-centered style="height: 100%">
			Processing...
		</tera-progress-spinner>
		<template v-else-if="selectedRunId && runResults[selectedRunId]">
			<section>
				<div v-if="isChartsEmpty" class="empty-chart">
					<img src="@assets/svg/seed.svg" alt="" draggable="false" class="empty-image" />
					<p class="helpMessage">No variables selected</p>
				</div>
				<vega-chart
					v-for="setting of selectedInterventionSettings"
					:key="setting.id"
					expandable
					are-embed-actions-visible
					:visualization-spec="interventionCharts[setting.id]"
					:interactive="false"
				/>
				<vega-chart
					v-for="setting of selectedVariableSettings"
					:key="setting.id"
					expandable
					are-embed-actions-visible
					:visualization-spec="variableCharts[setting.id]"
					:interactive="false"
				/>
				<vega-chart
					v-for="setting of selectedComparisonChartSettings"
					:key="setting.id"
					expandable
					are-embed-actions-visible
					:visualization-spec="comparisonCharts[setting.id]"
					:interactive="false"
				/>
			</section>
		</template>
		<Button v-if="areInputsFilled" label="Edit" @click="emit('open-drilldown')" severity="secondary" outlined />
		<tera-operator-placeholder v-else :node="node"> Connect a model configuration </tera-operator-placeholder>
	</main>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ref, toRef, watch } from 'vue';
import Button from 'primevue/button';

import { logger } from '@/utils/logger';

import { updateChartSettingsBySelectedVariables } from '@/services/chart-settings';
import { createDatasetFromSimulationResult } from '@/services/dataset';
import { flattenInterventionData, getInterventionPolicyById } from '@/services/intervention-policy';
import { getModelByModelConfigurationId, getTypesFromModelParts, getUnitsFromModelParts } from '@/services/model';
import { getModelConfigurationById } from '@/services/model-configurations';
import {
	getRunResultCSV,
	getSimulation,
	parsePyCiemssMap,
	pollAction,
	DataArray
} from '@/services/models/simulation-service';
import { createLLMSummary } from '@/services/summary-service';

import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { nodeOutputLabel } from '@/components/workflow/util';

import {
	ClientEvent,
	ClientEventType,
	ModelConfiguration,
	ProgressState,
	Simulation,
	SimulationNotificationData,
	StatusUpdate,
	type InterventionPolicy,
	type Model
} from '@/types/Types';
import { ChartSettingType } from '@/types/common';
import type { WorkflowNode } from '@/types/workflow';

import { useClientEvent } from '@/composables/useClientEvent';
import { useChartSettings } from '@/composables/useChartSettings';
import { useCharts } from '@/composables/useCharts';
import { useProjects } from '@/composables/project';

import { Poller, PollerState } from '@/api/api';
import { SimulateCiemssOperationState, SimulateCiemssOperation } from './simulate-ciemss-operation';
import { mergeResults, renameFnGenerator } from '../calibrate-ciemss/calibrate-utils';
import { usePreparedChartInputs } from './simulate-utils';

const props = defineProps<{
	node: WorkflowNode<SimulateCiemssOperationState>;
}>();

const emit = defineEmits(['open-drilldown', 'update-state', 'append-output']);
const model = ref<Model | null>(null);
const modelConfiguration = ref<ModelConfiguration | null>(null);
const modelVarUnits = ref<{ [key: string]: string }>({});
const modelPartTypesMap = computed(() => (!model.value ? {} : getTypesFromModelParts(model.value)));

const runResults = ref<{ [runId: string]: DataArray }>({});
const runResultsSummary = ref<{ [runId: string]: DataArray }>({});

const selectedRunId = ref<string>();
const inProgressForecastId = computed(() => props.node.state.inProgressForecastId);
const inProgressBaseForecastId = computed(() => props.node.state.inProgressBaseForecastId);
const inProgressForecastRun = computed(() => inProgressForecastId.value || inProgressBaseForecastId.value);
const areInputsFilled = computed(() => props.node.inputs[0].value);
const interventionPolicyId = computed(() => props.node.inputs[1].value?.[0]);
const interventionPolicy = ref<InterventionPolicy | null>(null);

const pyciemssMap = ref<Record<string, string>>({});

const processResult = async (runId: string) => {
	const result = await getRunResultCSV(runId, 'result.csv');
	pyciemssMap.value = parsePyCiemssMap(result[0]);
	const state = _.cloneDeep(props.node.state);
	if (_.isEmpty(state.chartSettings)) {
		state.chartSettings = updateChartSettingsBySelectedVariables(
			state.chartSettings ?? [],
			ChartSettingType.INTERVENTION,
			Object.keys(groupedInterventionOutputs.value)
		);
		state.chartSettings = updateChartSettingsBySelectedVariables(
			state.chartSettings,
			ChartSettingType.VARIABLE,
			Object.keys(pyciemssMap)
				.filter((c) => ['state', 'observable'].includes(modelPartTypesMap.value[c]))
				.slice(0, 5) // Limit the number of initial variables to first 5 to prevent too many charts
		);
		emit('update-state', state);
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

const preparedChartInputs = usePreparedChartInputs(props, runResults, runResultsSummary, pyciemssMap);

const { selectedVariableSettings, selectedInterventionSettings, selectedComparisonChartSettings } = useChartSettings(
	props,
	emit
);

const { useInterventionCharts, useVariableCharts, useComparisonCharts } = useCharts(
	props.node.id,
	model,
	modelConfiguration,
	preparedChartInputs,
	toRef({ width: 180, height: 120 }),
	computed(() => interventionPolicy.value?.interventions ?? []),
	null
);
const interventionCharts = useInterventionCharts(selectedInterventionSettings, true);
const variableCharts = useVariableCharts(selectedVariableSettings, null);
const comparisonCharts = useComparisonCharts(selectedComparisonChartSettings);
const isChartsEmpty = computed(
	() => _.isEmpty(interventionCharts.value) && _.isEmpty(variableCharts.value) && _.isEmpty(comparisonCharts.value)
);

const isFinished = (state: ProgressState) =>
	[ProgressState.Cancelled, ProgressState.Failed, ProgressState.Complete].includes(state);

const poller = new Poller();
const pollResult = async (runId: string) => {
	poller.setPollAction(async () => pollAction(runId));
	const pollerResults = await poller.start();
	const state = _.cloneDeep(props.node.state);
	state.errorMessage = { name: '', value: '', traceback: '' };

	if (pollerResults.state === PollerState.Cancelled) {
		state.inProgressForecastId = '';
		state.inProgressBaseForecastId = '';
		poller.stop();
	} else if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		logger.error(`Simulate: ${runId} has failed`, {
			toastTitle: 'Error - Pyciemss'
		});
	}
	return pollerResults;
};

// Handle simulation status update event for the forecast run
useClientEvent(
	ClientEventType.SimulationNotification,
	async (event: ClientEvent<StatusUpdate<SimulationNotificationData>>) => {
		const simulationNotificationData = event.data.data;
		if (simulationNotificationData.simulationId !== inProgressForecastId.value || !isFinished(event.data.state)) return;

		const simId = simulationNotificationData.simulationId;
		let errorMessage = { name: '', value: '', traceback: '' };
		let forecastId = '';
		if (event.data.state === ProgressState.Failed) {
			const simulation = await getSimulation(simId);
			if (simulation?.status && simulation?.statusMessage) {
				errorMessage = {
					name: simId,
					value: simulation.status,
					traceback: simulation.statusMessage
				};
			}
		} else if (event.data.state === ProgressState.Complete) {
			forecastId = simId;
		}
		const state = _.cloneDeep(props.node.state);
		state.inProgressForecastId = '';
		state.forecastId = forecastId;
		state.errorMessage = errorMessage;
		emit('update-state', state);
		if (event.data.state === ProgressState.Complete) await processResult(simId);
	}
);

// Handle simulation status update event for the base forecast run
useClientEvent(
	ClientEventType.SimulationNotification,
	async (event: ClientEvent<StatusUpdate<SimulationNotificationData>>) => {
		const simulationNotificationData = event.data.data;
		if (simulationNotificationData.simulationId !== inProgressBaseForecastId.value || !isFinished(event.data.state))
			return;

		const simId = simulationNotificationData.simulationId;
		const state = _.cloneDeep(props.node.state);
		state.errorMessage = { name: '', value: '', traceback: '' };
		state.inProgressBaseForecastId = '';
		if (event.data.state === ProgressState.Complete) state.baseForecastId = simId;
		emit('update-state', state);
	}
);

watch(
	() => props.node.state.inProgressForecastId,
	async (id) => {
		if (!id || id === '') return;
		const response = await pollResult(id);
		if (response.state === PollerState.Done) {
			const state = _.cloneDeep(props.node.state);
			state.inProgressForecastId = '';
			state.forecastId = id;
			emit('update-state', state);
		}
		await processResult(id);
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
	() => props.node.inputs[0].value,
	async () => {
		const input = props.node.inputs[0];
		if (!input.value) return;

		const id = input.value[0];
		modelConfiguration.value = await getModelConfigurationById(id);
		model.value = await getModelByModelConfigurationId(id);
		modelVarUnits.value = getUnitsFromModelParts(model.value as Model);
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
		if (!forecastId || !selectedRunId.value || inProgressForecastRun.value) return;

		let [result, resultSummary] = await Promise.all([
			getRunResultCSV(forecastId, 'result.csv'),
			getRunResultCSV(forecastId, 'result_summary.csv')
		]);
		pyciemssMap.value = parsePyCiemssMap(result[0]);

		const baseForecastId = props.node.state.baseForecastId;
		if (baseForecastId) {
			// If forecast run before intervention (base run) is available, merge the results
			const [baseResult, baseResultSummary] = await Promise.all([
				getRunResultCSV(baseForecastId, 'result.csv', renameFnGenerator('pre')),
				getRunResultCSV(baseForecastId, 'result_summary.csv', renameFnGenerator('pre'))
			]);
			const merged = mergeResults(baseResult, result, baseResultSummary, resultSummary);
			result = merged.result;
			resultSummary = merged.resultSummary;
		}
		runResults.value[selectedRunId.value] = result;
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
	gap: var(--gap-4);
	background: var(--surface-50);
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	margin-bottom: var(--gap-4);
	color: var(--text-color-secondary);
	font-size: var(--font-caption);
}
.empty-image {
	width: 5rem;
	height: 5rem;
}
</style>
