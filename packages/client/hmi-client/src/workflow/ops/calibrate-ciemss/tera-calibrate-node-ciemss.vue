<template>
	<main>
		<template v-if="!inProgressCalibrationId && runResults && csvAsset">
			<tera-simulate-chart
				v-for="(config, index) of props.node.state.chartConfigs"
				:key="index"
				:run-results="runResults"
				:chartConfig="{
					selectedRun: props.node.state.forecastId,
					selectedVariable: config
				}"
				:mapping="props.node.state.mapping as any"
				:initial-data="csvAsset"
				:size="{ width: 190, height: 120 }"
				has-mean-line
				@configuration-change="chartProxy.configurationChange(index, $event)"
			/>
		</template>

		<tera-progress-spinner
			v-if="inProgressCalibrationId"
			:font-size="2"
			is-centered
			style="height: 100%"
		/>

		<Button
			v-if="areInputsFilled"
			label="Edit"
			@click="emit('open-drilldown')"
			severity="secondary"
			outlined
		/>
		<tera-operator-placeholder v-else :operation-type="node.operationType">
			Connect a model configuration and dataset
		</tera-operator-placeholder>
	</main>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, watch, ref, shallowRef } from 'vue';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import {
	getRunResultCiemss,
	pollAction,
	getCalibrateBlobURL,
	makeForecastJobCiemss,
	getSimulation
} from '@/services/models/simulation-service';
import { setupDatasetInput } from '@/services/calibrate-workflow';
import { chartActionsProxy } from '@/workflow/util';
import { logger } from '@/utils/logger';

import { Poller, PollerState } from '@/api/api';
import type { WorkflowNode } from '@/types/workflow';
import type { CsvAsset } from '@/types/Types';
import type { RunResults } from '@/types/SimulateConfig';
import type { CalibrationOperationStateCiemss } from './calibrate-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateCiemss>;
}>();
const emit = defineEmits(['open-drilldown', 'update-state', 'append-output']);

const modelConfigId = computed<string | undefined>(() => props.node.inputs[0]?.value?.[0]);

const runResults = ref<RunResults>({});
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

const areInputsFilled = computed(() => props.node.inputs[0].value && props.node.inputs[1].value);
const inProgressCalibrationId = computed(() => props.node.state.inProgressCalibrationId);

const chartProxy = chartActionsProxy(props.node, (state: CalibrationOperationStateCiemss) => {
	emit('update-state', state);
});

const poller = new Poller();
const pollResult = async (runId: string) => {
	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => pollAction(runId));
	const pollerResults = await poller.start();
	const state = _.cloneDeep(props.node.state);
	state.errorMessage = { name: '', value: '', traceback: '' };
	emit('update-state', state);

	if (pollerResults.state === PollerState.Cancelled) {
		return pollerResults;
	}
	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		const simulation = await getSimulation(runId);
		if (simulation?.status && simulation?.statusMessage) {
			const errorMessage = {
				name: runId,
				value: simulation.status,
				traceback: simulation.statusMessage
			};
			state.inProgressCalibrationId = '';
			state.errorMessage = errorMessage;
			logger.error(errorMessage.traceback, {
				toastTitle: 'Error - Pyciemss'
			});
			emit('update-state', state);
		}
		throw Error('Failed Runs');
	}
	return pollerResults;
};

watch(
	() => props.node.state.inProgressCalibrationId,
	async (id) => {
		if (!id || id === '') return;

		const response = await pollResult(id);
		if (response.state === PollerState.Done) {
			// Start 2nd simulation to get sample simulation from dill
			const dillURL = await getCalibrateBlobURL(id);
			console.log('dill URL is', dillURL);

			// FIXME: should proably align with time-span in dataset
			const forecastResponse = await makeForecastJobCiemss({
				projectId: '',
				modelConfigId: modelConfigId.value as string,
				timespan: {
					start: 0,
					end: 100
					// start: state.currentTimespan.start,
					// end: state.currentTimespan.end
				},
				extra: {
					num_samples: 5,
					method: 'dopri5',
					inferred_parameters: id
				},
				engine: 'ciemss'
			});
			const forecastId = forecastResponse.id;

			const state = _.cloneDeep(props.node.state);
			state.inProgressCalibrationId = '';
			state.calibrationId = id;
			state.inProgressForecastId = forecastId;
			emit('update-state', state);
		}
	},
	{ immediate: true }
);

watch(
	() => props.node.state.inProgressForecastId,
	async (id) => {
		if (!id || id === '') return;

		const response = await pollResult(id);
		if (response.state === PollerState.Done) {
			const state = _.cloneDeep(props.node.state);

			state.chartConfigs = [[]];
			state.inProgressForecastId = '';
			state.forecastId = id;
			emit('update-state', state);

			const portLabel = props.node.inputs[0].label;
			emit('append-output', {
				type: 'calibrateSimulationId',
				label: `${portLabel} Result`,
				value: [state.calibrationId]
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
		if (!state.forecastId) return;

		const forecastId = state.forecastId;

		// Simulate
		const result = await getRunResultCiemss(forecastId, 'result.csv');
		runResults.value = result.runResults;

		// Dataset used to calibrate
		const datasetId = props.node.inputs[1]?.value?.[0];
		const { csv } = await setupDatasetInput(datasetId);
		csvAsset.value = csv;
	},
	{ immediate: true }
);
</script>

<style scoped></style>
