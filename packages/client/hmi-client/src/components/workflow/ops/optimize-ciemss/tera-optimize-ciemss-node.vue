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
					:chartConfig="{ selectedRun: props.node.state.forecastRunId, selectedVariable: cfg }"
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
import { computed, watch, ref } from 'vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraSimulateChart from '@/components/workflow/tera-simulate-chart.vue';
import { WorkflowNode } from '@/types/workflow';
import Button from 'primevue/button';
import { Poller, PollerState } from '@/api/api';
import {
	pollAction,
	getRunResultCiemss,
	makeForecastJobCiemss,
	getSimulation
} from '@/services/models/simulation-service';
import { logger } from '@/utils/logger';
import { chartActionsProxy } from '@/components/workflow/util';
import { SimulationRequest } from '@/types/Types';
import type { RunResults } from '@/types/SimulateConfig';
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
	() => props.node.state.inProgressOptimizeId !== '' || props.node.state.inProgressForecastId !== ''
);
const chartProxy = chartActionsProxy(props.node, (state: OptimizeCiemssOperationState) => {
	emit('update-state', state);
});

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
		state.inProgressForecastId = '';
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
		projectId: '',
		modelConfigId: modelConfigId.value as string,
		timespan: {
			start: 0,
			end: props.node.state.endTime
		},
		interventions: simulationIntervetions,
		extra: {
			num_samples: props.node.state.numSamples,
			method: props.node.state.solverMethod
		},
		engine: 'ciemss'
	};
	if (inferredParameters.value) {
		simulationPayload.extra.inferred_parameters = inferredParameters.value[0];
	}
	return makeForecastJobCiemss(simulationPayload);
};

watch(
	() => props.node.state.inProgressOptimizeId,
	async (optId) => {
		if (!optId || optId === '') return;

		const response = await pollResult(optId);
		if (response.state === PollerState.Done) {
			// Start 2nd simulation to get sample simulation from dill
			const simulationIntervetions = await getOptimizedInterventions(optId);
			const forecastResponse = await startForecast(simulationIntervetions);
			const forecastId = forecastResponse.id;

			const state = _.cloneDeep(props.node.state);
			state.inProgressOptimizeId = '';
			state.optimizationRunId = optId;
			state.inProgressForecastId = forecastId;
			emit('update-state', state);
		}
	},
	{ immediate: true }
);

watch(
	() => props.node.state.inProgressForecastId,
	async (simId) => {
		if (!simId || simId === '') return;

		const response = await pollResult(simId);
		if (response.state === PollerState.Done) {
			const state = _.cloneDeep(props.node.state);
			state.chartConfigs = [[]];
			state.inProgressForecastId = '';
			state.forecastRunId = simId;
			emit('update-state', state);

			emit('append-output', {
				type: OptimizeCiemssOperation.outputs[0].type,
				label: `Simulation output - ${props.node.outputs.length + 1}`,
				value: [simId],
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
		if (!state.forecastRunId) return;

		const forecastRunId = state.forecastRunId;

		// Simulate
		const result = await getRunResultCiemss(forecastRunId, 'result.csv');
		runResults.value = result.runResults;
	},
	{ immediate: true }
);
</script>

<style scoped></style>
