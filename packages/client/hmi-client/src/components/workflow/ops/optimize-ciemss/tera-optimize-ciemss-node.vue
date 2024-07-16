<template>
	<main>
		<tera-operator-placeholder
			v-if="!showSpinner && !runResults"
			:operation-type="node.operationType"
		>
			<template v-if="!node.inputs[0].value"> Attach a model configuration </template>
		</tera-operator-placeholder>
		<template v-if="node.inputs[0].value">
			<tera-progress-spinner v-if="showSpinner" :font-size="2" is-centered style="height: 100%" />
			<div v-if="!showSpinner && runResults">
				<template v-for="(_, index) of node.state.selectedSimulationVariables" :key="index">
					<vega-chart
						:visualization-spec="preparedCharts[index]"
						:are-embed-actions-visible="false"
					/>
				</template>
			</div>
			<div class="flex gap-2">
				<Button
					@click="emit('open-drilldown')"
					label="Edit"
					severity="secondary"
					outlined
					class="w-full"
				/>
			</div>
		</template>
	</main>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, watch, ref } from 'vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import { WorkflowNode } from '@/types/workflow';
import Button from 'primevue/button';
import { Poller, PollerResult, PollerState } from '@/api/api';
import {
	pollAction,
	makeForecastJobCiemss,
	getSimulation,
	getRunResult,
	getRunResultCSV,
	parsePyCiemssMap
} from '@/services/models/simulation-service';
import { logger } from '@/utils/logger';
import { nodeMetadata } from '@/components/workflow/util';
import { SimulationRequest } from '@/types/Types';
import { createLLMSummary } from '@/services/summary-service';
import VegaChart from '@/components/widgets/VegaChart.vue';
import { createOptimizeForecastChart } from '@/utils/optimize';
import {
	OptimizeCiemssOperationState,
	OptimizeCiemssOperation,
	getOptimizedInterventions
} from './optimize-ciemss-operation';

const emit = defineEmits(['open-drilldown', 'append-output', 'update-state']);

const props = defineProps<{
	node: WorkflowNode<OptimizeCiemssOperationState>;
}>();

const runResults = ref<any>({});
const runResultsSummary = ref<any>({});
const modelConfigId = computed<string | undefined>(() => props.node.inputs[0]?.value?.[0]);

let pyciemssMap: Record<string, string> = {};

const inferredParameters = computed(() => props.node.inputs[1].value);
const showSpinner = computed<boolean>(
	() =>
		props.node.state.inProgressOptimizeId !== '' ||
		props.node.state.inProgressPostForecastId !== '' ||
		props.node.state.inProgressPreForecastId !== ''
);

const poller = new Poller();
const pollResult = async (runId: string) => {
	poller
		.setInterval(4000)
		.setThreshold(350)
		.setPollAction(async () => pollAction(runId));
	const pollerResults = await poller.start();
	let state = _.cloneDeep(props.node.state);
	state.optimizeErrorMessage = { name: '', value: '', traceback: '' };

	if (pollerResults.state === PollerState.Cancelled) {
		state.inProgressPostForecastId = '';
		state.inProgressOptimizeId = '';
		poller.stop();
	} else if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		logger.error(`Optimization: ${runId} has failed`, {
			toastTitle: 'Error - Pyciemss'
		});
		const simulation = await getSimulation(runId);
		if (simulation?.status && simulation?.statusMessage) {
			state = _.cloneDeep(props.node.state);
			state.inProgressOptimizeId = '';
			state.optimizeErrorMessage = {
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

const startForecast = async (simulationIntervetions) => {
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
	// Explicitly add interventions provided. Interventions within the model config will still be utilized either way
	// TODO: https://github.com/DARPA-ASKEM/terarium/issues/4025
	console.log(
		`We now need to concat this with the policy intervention provided and make an object in TDS ${simulationIntervetions}`
	);
	// if (simulationIntervetions) {
	// 	simulationPayload.interventions = simulationIntervetions;
	// }
	if (inferredParameters.value) {
		simulationPayload.extra.inferred_parameters = inferredParameters.value[0];
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
	return selectedSimulationVariables.map((variable) =>
		createOptimizeForecastChart(preResult, preResultSummary, postResult, postResultSummary, [], {
			width: 180,
			height: 120,
			variables: [pyciemssMap[variable]],
			statisticalVariables: [`${pyciemssMap[variable]}_mean`],
			legend: false,
			groupField: 'sample_id',
			timeField: 'timepoint_id',
			xAxisTitle: '',
			yAxisTitle: '',
			title: variable
		})
	);
});

watch(
	() => props.node.state.inProgressOptimizeId,
	async (optId) => {
		if (!optId || optId === '') return;

		const response = await pollResult(optId);
		if (response.state === PollerState.Done) {
			// Start 2nd simulation to get sample simulation from dill
			const simulationIntervetions = await getOptimizedInterventions(optId);
			const preForecastResponce = await startForecast(undefined);
			const preForecastId = preForecastResponce.id;
			const postForecastResponce = await startForecast(simulationIntervetions);
			const postForecastId = postForecastResponce.id;

			const state = _.cloneDeep(props.node.state);
			state.inProgressOptimizeId = '';
			state.optimizationRunId = optId;
			state.inProgressPreForecastId = preForecastId;
			state.inProgressPostForecastId = postForecastId;
			emit('update-state', state);
		}
	},
	{ immediate: true }
);

watch(
	() => [props.node.state.inProgressPreForecastId, props.node.state.inProgressPostForecastId],
	async ([preSimId, postSimId]) => {
		if (!preSimId || preSimId === '' || !postSimId || postSimId === '') return;
		const responseList: Promise<PollerResult<any>>[] = [];
		responseList.push(pollResult(preSimId));
		responseList.push(pollResult(postSimId));
		const [preResponse, postResponse] = await Promise.all(responseList);
		if (preResponse.state === PollerState.Done && postResponse.state === PollerState.Done) {
			const state = _.cloneDeep(props.node.state);
			state.inProgressPreForecastId = '';
			state.preForecastRunId = preSimId;
			state.inProgressPostForecastId = '';
			state.postForecastRunId = postSimId;
			emit('update-state', state);

			// Generate output summary, collect key facts and get agent to summarize
			const optimizationResult = await getRunResult(
				state.optimizationRunId,
				'optimize_results.json'
			);
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

			emit('append-output', {
				type: OptimizeCiemssOperation.outputs[0].type,
				label: `Simulation output - ${props.node.outputs.length + 1}`,
				value: [postSimId],
				isSelected: false,
				state
			});
		}
	},
	{ immediate: true }
);

watch(
	() => props.node.active,
	async () => {
		const active = props.node.active;
		const state = props.node.state;
		if (!active) return;
		if (!state.postForecastRunId || !state.preForecastRunId) return;

		const preForecastRunId = state.preForecastRunId;
		const postForecastRunId = state.postForecastRunId;

		const preResult = await getRunResultCSV(preForecastRunId, 'result.csv');
		const postResult = await getRunResultCSV(postForecastRunId, 'result.csv');
		pyciemssMap = parsePyCiemssMap(postResult[0]);

		runResults.value[preForecastRunId] = preResult;
		runResults.value[postForecastRunId] = postResult;

		const preResultSummary = await getRunResultCSV(preForecastRunId, 'result_summary.csv');
		const postResultSummary = await getRunResultCSV(postForecastRunId, 'result_summary.csv');

		runResultsSummary.value[preForecastRunId] = preResultSummary;
		runResultsSummary.value[postForecastRunId] = postResultSummary;
	},
	{ immediate: true }
);
</script>

<style scoped></style>
