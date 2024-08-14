<template>
	<main>
		<tera-operator-placeholder v-if="!showSpinner && !runResults[node.state.postForecastRunId]" :node="node">
			<template v-if="!node.inputs[0].value"> Attach a model configuration </template>
		</tera-operator-placeholder>
		<template v-if="node.inputs[0].value">
			<tera-progress-spinner v-if="showSpinner" :font-size="2" is-centered style="height: 100%" />
			<div v-if="!showSpinner && runResults">
				<template v-for="(_, index) of node.state.selectedSimulationVariables" :key="index">
					<vega-chart :visualization-spec="preparedCharts[index]" :are-embed-actions-visible="false" />
				</template>
			</div>
			<div class="flex gap-2">
				<Button @click="emit('open-drilldown')" label="Edit" severity="secondary" outlined class="w-full" />
			</div>
		</template>
	</main>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, watch, ref, onUnmounted } from 'vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import { WorkflowNode } from '@/types/workflow';
import Button from 'primevue/button';
import { Poller, PollerResult, PollerState } from '@/api/api';
import {
	pollAction,
	makeForecastJobCiemss,
	getRunResult,
	getRunResultCSV,
	parsePyCiemssMap
} from '@/services/models/simulation-service';
import { nodeMetadata, nodeOutputLabel } from '@/components/workflow/util';
import { SimulationRequest, InterventionPolicy } from '@/types/Types';
import { createLLMSummary } from '@/services/summary-service';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { createForecastChart } from '@/services/charts';
import { mergeResults, renameFnGenerator } from '@/components/workflow/ops/calibrate-ciemss/calibrate-utils';
import { getModelByModelConfigurationId, getUnitsFromModelParts } from '@/services/model';
import { getModelConfigurationById } from '@/services/model-configurations';
import { createDatasetFromSimulationResult } from '@/services/dataset';
import { useProjects } from '@/composables/project';
import {
	OptimizeCiemssOperationState,
	OptimizeCiemssOperation,
	createInterventionPolicyFromOptimize
} from './optimize-ciemss-operation';

const emit = defineEmits(['open-drilldown', 'append-output', 'update-state']);

const props = defineProps<{
	node: WorkflowNode<OptimizeCiemssOperationState>;
}>();

const runResults = ref<any>({});
const runResultsSummary = ref<any>({});
const modelConfigId = computed<string | undefined>(() => props.node.inputs[0]?.value?.[0]);

const modelVarUnits = ref<{ [key: string]: string }>({});

let pyciemssMap: Record<string, string> = {};

const showSpinner = computed<boolean>(
	() =>
		props.node.state.inProgressOptimizeId !== '' ||
		props.node.state.inProgressPostForecastId !== '' ||
		props.node.state.inProgressPreForecastId !== ''
);

const poller = new Poller();
const pollResult = async (runId: string) => {
	poller
		.setInterval(5000)
		.setThreshold(100)
		.setPollAction(async () => pollAction(runId));

	const pollerResults = await poller.start();

	if (pollerResults.state === PollerState.Cancelled) {
		return pollerResults;
	}
	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		console.error(`Optimize: ${runId} has failed`, pollerResults);
	}
	return pollerResults;
};

const startForecast = async (optimizedInterventions?: InterventionPolicy) => {
	const simulationPayload: SimulationRequest = {
		modelConfigId: modelConfigId.value as string,
		timespan: {
			start: 0,
			end: props.node.state.endTime
		},
		extra: {
			num_samples: props.node.state.numSamples,
			method: props.node.state.solverMethod
		},
		engine: 'ciemss'
	};

	if (optimizedInterventions) {
		// Use the intervention policy ID provided.
		simulationPayload.policyInterventionId = optimizedInterventions.id;
	} else {
		// Use the input interventions provided
		const inputIntervention = props.node.inputs[1].value?.[0];
		simulationPayload.policyInterventionId = inputIntervention;
	}

	const modelConfig = await getModelConfigurationById(modelConfigId.value as string);
	if (modelConfig.simulationId) {
		simulationPayload.extra.inferred_parameters = modelConfig.simulationId;
	}

	return makeForecastJobCiemss(simulationPayload, nodeMetadata(props.node));
};

const preparedCharts = computed(() => {
	const { preForecastRunId, postForecastRunId, selectedSimulationVariables } = props.node.state;
	if (!postForecastRunId || !preForecastRunId) return [];
	const preResult = runResults.value[preForecastRunId];
	const preResultSummary = runResultsSummary.value[preForecastRunId];
	const postResult = runResults.value[postForecastRunId];
	const postResultSummary = runResultsSummary.value[postForecastRunId];

	if (!postResult || !postResultSummary || !preResultSummary || !preResult) return [];
	// Merge before/after for chart
	const { result, resultSummary } = mergeResults(preResult, postResult, preResultSummary, postResultSummary);
	return selectedSimulationVariables.map((variable) =>
		createForecastChart(
			{
				dataset: result,
				variables: [`${pyciemssMap[variable]}:pre`, pyciemssMap[variable]],
				timeField: 'timepoint_id',
				groupField: 'sample_id'
			},
			{
				dataset: resultSummary,
				variables: [`${pyciemssMap[variable]}_mean:pre`, `${pyciemssMap[variable]}_mean`],
				timeField: 'timepoint_id'
			},
			null,
			{
				width: 180,
				height: 120,
				legend: true,
				xAxisTitle: modelVarUnits.value._time || 'Time',
				yAxisTitle: modelVarUnits.value[variable] || '',
				translationMap: {
					[`${pyciemssMap[variable]}_mean:pre`]: `${variable} before optimization`,
					[`${pyciemssMap[variable]}_mean`]: `${variable} after optimization`
				},
				title: '',
				colorscheme: ['#AAB3C6', '#1B8073']
			}
		)
	);
});

watch(
	() => props.node.state.inProgressOptimizeId,
	async (optId) => {
		if (!optId || optId === '') return;

		const response = await pollResult(optId);
		if (response.state === PollerState.Done) {
			// Start 2nd simulation to get sample simulation from dill
			const newInterventionResponse = await createInterventionPolicyFromOptimize(modelConfigId.value as string, optId);

			const preForecastResponse = startForecast();
			const postForecastResponse = startForecast(newInterventionResponse);
			const forecastResults = await Promise.all([preForecastResponse, postForecastResponse]);
			const [{ id: preForecastId }, { id: postForecastId }] = forecastResults;

			const state = _.cloneDeep(props.node.state);
			state.inProgressOptimizeId = '';
			state.optimizationRunId = optId;
			state.inProgressPreForecastId = preForecastId;
			state.inProgressPostForecastId = postForecastId;
			state.optimizedInterventionPolicyId = newInterventionResponse.id ?? '';
			emit('update-state', state);
		} else {
			// Simulation Failed:
			const state = _.cloneDeep(props.node.state);
			if (response?.state && response?.error) {
				state.optimizeErrorMessage = {
					name: optId,
					value: response.state,
					traceback: response.error
				};
			}
			state.inProgressOptimizeId = '';
			state.inProgressPreForecastId = '';
			state.inProgressPostForecastId = '';
			emit('update-state', state);
		}
	},
	{ immediate: true }
);

watch(
	() => `${props.node.state.inProgressPreForecastId},${props.node.state.inProgressPostForecastId}`,
	async () => {
		const preSimId = props.node.state.inProgressPreForecastId;
		const postSimId = props.node.state.inProgressPostForecastId;
		if (!preSimId || preSimId === '' || !postSimId || postSimId === '') return;
		const responseList: Promise<PollerResult<any>>[] = [];
		responseList.push(pollResult(preSimId));
		responseList.push(pollResult(postSimId));
		const [preResponse, postResponse] = await Promise.all(responseList);
		if (preResponse.state === PollerState.Done && postResponse.state === PollerState.Done) {
			const state = _.cloneDeep(props.node.state);

			// Generate output summary, collect key facts and get agent to summarize
			const optimizationResult = await getRunResult(state.optimizationRunId, 'optimize_results.json');
			const prompt = `
The following are the key attributes and findings of an optimization process for a ODE epidemilogy model, the goal is to find the best values or time points that satisfy a set of constraints.

- The succss constraints are, in JSON: ${JSON.stringify(state.constraintGroups)}
- We want to optimize: ${JSON.stringify(state.interventionPolicyGroups.filter((d) => d.isActive === true))}
- The fixed/static intervenations: ${JSON.stringify(state.interventionPolicyGroups.filter((d) => d.isActive === false))}
- The best guesses are: ${optimizationResult.x}
- The result is ${optimizationResult.success}

Provide a consis summary in 100 words or less.
			`;
			const summaryResponse = await createLLMSummary(prompt);
			state.summaryId = summaryResponse?.id;

			state.inProgressPreForecastId = '';
			state.preForecastRunId = preSimId;
			state.inProgressPostForecastId = '';
			state.postForecastRunId = postSimId;
			emit('update-state', state);

			const datasetName = `Forecast run ${state.postForecastRunId}`;
			const projectId = useProjects().activeProjectId.value;
			const datasetResult = await createDatasetFromSimulationResult(
				projectId,
				state.postForecastRunId,
				datasetName,
				false
			);
			if (!datasetResult) {
				return;
			}

			emit('append-output', {
				type: OptimizeCiemssOperation.outputs[0].type,
				label: nodeOutputLabel(props.node, `Optimize output`),
				value: [
					{
						policyInterventionId: state.optimizedInterventionPolicyId,
						datasetId: datasetResult.id
					}
				],
				isSelected: false,
				state
			});
		} else {
			// Simulation Failed:
			const state = _.cloneDeep(props.node.state);
			if (preResponse.error) {
				state.simulateErrorMessage = {
					name: preSimId,
					value: preResponse.state,
					traceback: preResponse.error
				};
			}
			// Probably no need to capture both simulation error messages as theyre very similar simulation calls.
			else if (postResponse.error) {
				state.simulateErrorMessage = {
					name: postSimId,
					value: postResponse.state,
					traceback: postResponse.error
				};
			}
			state.inProgressOptimizeId = '';
			state.inProgressPreForecastId = '';
			state.inProgressPostForecastId = '';
			emit('update-state', state);
		}
	},
	{ immediate: true }
);

watch(
	() => props.node.active,
	async () => {
		const active = props.node.active;
		const state = _.cloneDeep(props.node.state);
		if (!active) return;
		if (!state.postForecastRunId || !state.preForecastRunId) return;

		const model = await getModelByModelConfigurationId(modelConfigId.value as string);
		if (model) {
			modelVarUnits.value = getUnitsFromModelParts(model);
		}

		const preForecastRunId = state.preForecastRunId;
		const postForecastRunId = state.postForecastRunId;

		const preResult = await getRunResultCSV(preForecastRunId, 'result.csv', renameFnGenerator('pre'));
		const postResult = await getRunResultCSV(postForecastRunId, 'result.csv');
		pyciemssMap = parsePyCiemssMap(postResult[0]);

		runResults.value[preForecastRunId] = preResult;
		runResults.value[postForecastRunId] = postResult;

		const preResultSummary = await getRunResultCSV(preForecastRunId, 'result_summary.csv', renameFnGenerator('pre'));
		const postResultSummary = await getRunResultCSV(postForecastRunId, 'result_summary.csv');

		runResultsSummary.value[preForecastRunId] = preResultSummary;
		runResultsSummary.value[postForecastRunId] = postResultSummary;
	},
	{ immediate: true }
);

onUnmounted(() => {
	poller.stop();
});
</script>

<style scoped></style>
