<template>
	<main>
		<template v-if="selectedRunId && runResults[selectedRunId]">
			<vega-chart
				v-for="(_config, index) of props.node.state.chartConfigs"
				:key="index"
				:are-embed-actions-visible="false"
				:visualization-spec="preparedCharts[index]"
			/>
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
	pollAction,
	getSimulation,
	parsePyCiemssMap,
	DataArray
} from '@/services/models/simulation-service';
import { getModelByModelConfigurationId, getUnitsFromModelParts } from '@/services/model';
import { Poller, PollerState } from '@/api/api';
import { logger } from '@/utils/logger';
import { chartActionsProxy, nodeOutputLabel } from '@/components/workflow/util';

import type { WorkflowNode } from '@/types/workflow';
import { createLLMSummary } from '@/services/summary-service';
import { useProjects } from '@/composables/project';
import { createForecastChart } from '@/services/charts';
import { createDatasetFromSimulationResult } from '@/services/dataset';
import VegaChart from '@/components/widgets/VegaChart.vue';
import type { Model } from '@/types/Types';
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
const areInputsFilled = computed(() => props.node.inputs[0].value);

let pyciemssMap: Record<string, string> = {};

const poller = new Poller();
const pollResult = async (runId: string) => {
	selectedRunId.value = undefined;

	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => pollAction(runId));
	const pollerResults = await poller.start();
	let state = _.cloneDeep(props.node.state);
	state.errorMessage = { name: '', value: '', traceback: '' };

	if (pollerResults.state === PollerState.Cancelled) {
		state.inProgressForecastId = '';
		poller.stop();
	} else if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		logger.error(`Simulation: ${runId} has failed`, {
			toastTitle: 'Error - Pyciemss'
		});
		const simulation = await getSimulation(runId);
		if (simulation?.status && simulation?.statusMessage) {
			state = _.cloneDeep(props.node.state);
			state.inProgressForecastId = '';
			state.errorMessage = {
				name: runId,
				value: simulation.status,
				traceback: simulation.statusMessage
			};
			emit('update-state', state);
		}
		throw Error('Failed Runs');
	}
	emit('update-state', state);
	return pollerResults;
};

const chartProxy = chartActionsProxy(props.node, (state: SimulateCiemssOperationState) => {
	emit('update-state', state);
});

const processResult = async (runId: string) => {
	const state = _.cloneDeep(props.node.state);
	if (state.chartConfigs.length === 0) {
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

const preparedCharts = computed(() => {
	if (!selectedRunId.value) return [];

	const result = runResults.value[selectedRunId.value];
	const resultSummary = runResultsSummary.value[selectedRunId.value];
	const reverseMap: Record<string, string> = {};
	Object.keys(pyciemssMap).forEach((key) => {
		reverseMap[`${pyciemssMap[key]}_mean`] = key;
	});

	return props.node.state.chartConfigs.map((config) =>
		createForecastChart(
			{
				dataset: result,
				variables: config.map((d) => pyciemssMap[d]),
				timeField: 'timepoint_id',
				groupField: 'sample_id'
			},
			{
				dataset: resultSummary,
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
		)
	);
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

watch(
	() => props.node.state.inProgressForecastId,
	async (id) => {
		if (!id || id === '') return;

		const response = await pollResult(id);
		if (response.state === PollerState.Done) {
			await processResult(id);
		}
		const state = _.cloneDeep(props.node.state);
		state.inProgressForecastId = '';
		state.forecastId = id;
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
</script>

<style scoped></style>
