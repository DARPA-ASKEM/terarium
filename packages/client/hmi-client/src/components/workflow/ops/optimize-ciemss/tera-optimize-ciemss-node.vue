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
				<tera-simulate-chart
					v-for="(cfg, idx) in node.state.chartConfigs"
					:key="idx"
					:run-results="runResults"
					:chartConfig="{ selectedRun: props.node.state.postForecastRunId, selectedVariable: cfg }"
					has-mean-line
					:size="{ width: 190, height: 120 }"
					@configuration-change="chartProxy.configurationChange(idx, $event)"
				/>
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
import { computed, watch, ref, onUnmounted } from 'vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraSimulateChart from '@/components/workflow/tera-simulate-chart.vue';
import { WorkflowNode } from '@/types/workflow';
import Button from 'primevue/button';
import { Poller, PollerResult, PollerState } from '@/api/api';
import {
	pollAction,
	getRunResultCiemss,
	makeForecastJobCiemss,
	getRunResult
} from '@/services/models/simulation-service';
import { chartActionsProxy, nodeMetadata } from '@/components/workflow/util';
import { SimulationRequest } from '@/types/Types';
import type { RunResults } from '@/types/SimulateConfig';
import { createLLMSummary } from '@/services/summary-service';
import {
	OptimizeCiemssOperationState,
	OptimizeCiemssOperation,
	getOptimizedInterventions
} from './optimize-ciemss-operation';

const emit = defineEmits(['open-drilldown', 'append-output', 'update-state']);

const props = defineProps<{
	node: WorkflowNode<OptimizeCiemssOperationState>;
}>();

const runResults = ref<RunResults>({});
const modelConfigId = computed<string | undefined>(() => props.node.inputs[0]?.value?.[0]);
const inferredParameters = computed(() => props.node.inputs[1].value);
const showSpinner = computed<boolean>(
	() =>
		props.node.state.inProgressOptimizeId !== '' || props.node.state.inProgressPostForecastId !== ''
);
const chartProxy = chartActionsProxy(props.node, (state: OptimizeCiemssOperationState) => {
	emit('update-state', state);
});

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

			state.chartConfigs = [[]];
			state.inProgressPreForecastId = '';
			state.preForecastRunId = preSimId;
			state.inProgressPostForecastId = '';
			state.postForecastRunId = postSimId;
			emit('update-state', state);

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
		if (!state.postForecastRunId) return;

		const postForecastRunId = state.postForecastRunId;

		// Simulate
		const result = await getRunResultCiemss(postForecastRunId, 'result.csv');
		runResults.value = result.runResults;
	},
	{ immediate: true }
);

onUnmounted(() => {
	poller.stop();
});
</script>

<style scoped></style>
