<template>
	<main>
		<template v-if="selectedRunId && runResults[selectedRunId]">
			<section v-for="setting of chartSettings" :key="setting.id">
				<vega-chart
					v-if="preparedCharts[setting.id]"
					expandable
					are-embed-actions-visible
					:visualization-spec="preparedCharts[setting.id]"
				/>
				<div v-else class="empty-chart">
					<img src="@assets/svg/seed.svg" alt="" draggable="false" class="empty-image" />
					<p class="helpMessage">No variables selected</p>
				</div>
			</section>
		</template>
		<tera-progress-spinner v-if="inProgressForecastRun" :font-size="2" is-centered style="height: 100%" />
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
import { getRunResultCSV, getSimulation, parsePyCiemssMap, DataArray } from '@/services/models/simulation-service';
import { getModelByModelConfigurationId, getUnitsFromModelParts, getVegaDateOptions } from '@/services/model';
import { logger } from '@/utils/logger';
import { nodeOutputLabel } from '@/components/workflow/util';

import type { WorkflowNode } from '@/types/workflow';
import { createLLMSummary } from '@/services/summary-service';
import { useProjects } from '@/composables/project';
import { createForecastChart, createInterventionChartMarkers, ForecastChartOptions } from '@/services/charts';
import { createDatasetFromSimulationResult } from '@/services/dataset';
import VegaChart from '@/components/widgets/VegaChart.vue';
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
import { flattenInterventionData, getInterventionPolicyById } from '@/services/intervention-policy';
import { addMultiVariableChartSetting } from '@/services/chart-settings';
import { ChartSettingType } from '@/types/common';
import { useClientEvent } from '@/composables/useClientEvent';
import { getModelConfigurationById } from '@/services/model-configurations';
import { SimulateCiemssOperationState, SimulateCiemssOperation } from './simulate-ciemss-operation';
import { mergeResults, renameFnGenerator } from '../calibrate-ciemss/calibrate-utils';

const props = defineProps<{
	node: WorkflowNode<SimulateCiemssOperationState>;
}>();

const emit = defineEmits(['open-drilldown', 'update-state', 'append-output']);
const model = ref<Model | null>(null);
const modelConfiguration = ref<ModelConfiguration | null>(null);
const modelVarUnits = ref<{ [key: string]: string }>({});

const runResults = ref<{ [runId: string]: DataArray }>({});
const runResultsSummary = ref<{ [runId: string]: DataArray }>({});

const selectedRunId = ref<string>();
const inProgressForecastId = computed(() => props.node.state.inProgressForecastId);
const inProgressBaseForecastId = computed(() => props.node.state.inProgressBaseForecastId);
const inProgressForecastRun = computed(() => inProgressForecastId.value || inProgressBaseForecastId.value);
const areInputsFilled = computed(() => props.node.inputs[0].value);
const interventionPolicyId = computed(() => props.node.inputs[1].value?.[0]);
const interventionPolicy = ref<InterventionPolicy | null>(null);

const chartSettings = computed(() => props.node.state.chartSettings ?? []);

let pyciemssMap: Record<string, string> = {};

const processResult = async (runId: string) => {
	const state = _.cloneDeep(props.node.state);
	if (interventionPolicyId.value && _.isEmpty(state.chartSettings)) {
		_.keys(groupedInterventionOutputs.value).forEach((key) => {
			state.chartSettings = addMultiVariableChartSetting(
				state.chartSettings ?? [],
				ChartSettingType.VARIABLE_COMPARISON,
				[key]
			);
		});
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

const preparedCharts = computed(() => {
	const charts: Record<string, any> = {};
	if (!selectedRunId.value) return charts;
	const result = runResults.value[selectedRunId.value];
	const resultSummary = runResultsSummary.value[selectedRunId.value];
	const reverseMap: Record<string, string> = {};
	Object.keys(pyciemssMap).forEach((key) => {
		reverseMap[`${pyciemssMap[key]}_mean`] = key;
	});

	const dateOptions = getVegaDateOptions(model.value, modelConfiguration.value);
	chartSettings.value.forEach((setting) => {
		// If only one variable is selected, show the baseline forecast
		const selectedVars = setting.selectedVariables;
		const showBaseLine = selectedVars.length === 1 && Boolean(props.node.state.baseForecastId);

		const options: ForecastChartOptions = {
			title: '',
			width: 180,
			height: 120,
			legend: true,
			translationMap: reverseMap,
			xAxisTitle: modelVarUnits.value._time || 'Time',
			yAxisTitle: _.uniq(selectedVars.map((v) => modelVarUnits.value[v]).filter((v) => !!v)).join(',') || '',
			dateOptions
		};

		let statLayerVariables = selectedVars.map((d) => `${pyciemssMap[d]}_mean`);

		if (showBaseLine) {
			statLayerVariables = [`${pyciemssMap[selectedVars[0]]}_mean:base`, `${pyciemssMap[selectedVars[0]]}_mean`];
			options.translationMap = {
				...options.translationMap,
				[`${pyciemssMap[selectedVars[0]]}_mean:base`]: `${selectedVars[0]} (baseline)`
			};
			options.colorscheme = ['#AAB3C6', '#1B8073'];
		}

		const chart = createForecastChart(
			{
				data: result,
				variables: setting.selectedVariables.map((d) => pyciemssMap[d]),
				timeField: 'timepoint_id',
				groupField: 'sample_id'
			},
			{
				data: resultSummary,
				variables: statLayerVariables,
				timeField: 'timepoint_id'
			},
			null,
			options
		);
		if (interventionPolicy.value) {
			_.keys(groupedInterventionOutputs.value).forEach((key) => {
				if (selectedVars.includes(key)) {
					chart.layer.push(
						...createInterventionChartMarkers(groupedInterventionOutputs.value[key], {
							labelXOffset: -115
						})
					);
				}
			});
		}
		charts[setting.id] = chart;
	});
	return charts;
});

const isFinished = (state: ProgressState) =>
	[ProgressState.Cancelled, ProgressState.Failed, ProgressState.Complete].includes(state);

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
		// Check simulation status and update the state
		const simResponse: Simulation | null = await getSimulation(id);
		if (simResponse?.status !== ProgressState.Complete) return;
		const state = _.cloneDeep(props.node.state);
		state.inProgressForecastId = '';
		state.forecastId = id;
		emit('update-state', state);
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
		pyciemssMap = parsePyCiemssMap(result[0]);

		const baseForecastId = props.node.state.baseForecastId;
		if (baseForecastId) {
			// If forecast run before intervention (base run) is available, merge the results
			const [baseResult, baseResultSummary] = await Promise.all([
				getRunResultCSV(baseForecastId, 'result.csv', renameFnGenerator('base')),
				getRunResultCSV(baseForecastId, 'result_summary.csv', renameFnGenerator('base'))
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
