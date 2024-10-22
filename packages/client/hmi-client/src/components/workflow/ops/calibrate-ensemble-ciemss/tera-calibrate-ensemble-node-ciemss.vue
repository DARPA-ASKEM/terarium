<template>
	<main>
		<template v-if="!inProgressCalibrationId && !inProgressForecastId && runResults && csvAsset">
			<tera-simulate-chart
				v-for="(config, index) of props.node.state.chartConfigs"
				:key="index"
				:run-results="runResults"
				:chartConfig="{
					selectedRun: props.node.state.forecastRunId,
					selectedVariable: config
				}"
				:mapping="props.node.state.ensembleConfigs as any"
				:initial-data="csvAsset"
				:size="{ width: 190, height: 120 }"
				has-mean-line
				@configuration-change="chartProxy.configurationChange(index, $event)"
			/>
		</template>

		<tera-progress-spinner
			v-if="inProgressCalibrationId || inProgressForecastId"
			:font-size="2"
			is-centered
			style="height: 100%"
		/>

		<Button v-if="areInputsFilled" label="Edit" @click="emit('open-drilldown')" severity="secondary" outlined />
		<tera-operator-placeholder v-else :node="node">
			Connect a model configuration and dataset
		</tera-operator-placeholder>
	</main>
</template>

<script setup lang="ts">
import { computed, ref, shallowRef, watch } from 'vue';
import _ from 'lodash';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraSimulateChart from '@/components/workflow/tera-simulate-chart.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import {
	getRunResultCiemss,
	pollAction,
	getCalibrateBlobURL,
	makeEnsembleCiemssSimulation
} from '@/services/models/simulation-service';
import { setupCsvAsset } from '@/services/calibrate-workflow';
import { chartActionsProxy, nodeMetadata, nodeOutputLabel } from '@/components/workflow/util';
import { logger } from '@/utils/logger';

import { Poller, PollerState } from '@/api/api';
import type { WorkflowNode } from '@/types/workflow';
import { WorkflowPortStatus } from '@/types/workflow';
import type { CsvAsset, EnsembleSimulationCiemssRequest } from '@/types/Types';
import type { RunResults } from '@/types/SimulateConfig';
import type { CalibrateEnsembleCiemssOperationState } from './calibrate-ensemble-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrateEnsembleCiemssOperationState>;
}>();
const emit = defineEmits(['open-drilldown', 'update-state', 'append-output', 'append-input-port']);

const runResults = ref<RunResults>({});
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

const areInputsFilled = computed(() => props.node.inputs[0].value && props.node.inputs[1].value);
const inProgressCalibrationId = computed(() => props.node.state.inProgressCalibrationId);
const inProgressForecastId = computed(() => props.node.state.inProgressForecastId);

const chartProxy = chartActionsProxy(props.node, (state: CalibrateEnsembleCiemssOperationState) => {
	emit('update-state', state);
});

const poller = new Poller();
const pollResult = async (runId: string) => {
	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => pollAction(runId));
	const pollerResults = await poller.start();

	if (pollerResults.state === PollerState.Cancelled) {
		const state = _.cloneDeep(props.node.state);
		state.inProgressForecastId = '';
		state.inProgressCalibrationId = '';
		emit('update-state', state);
		return pollerResults;
	}
	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		logger.error(`Calibration: ${runId} has failed`, {
			toastTitle: 'Error - Pyciemss'
		});
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

			const params: EnsembleSimulationCiemssRequest = {
				modelConfigs: props.node.state.ensembleConfigs,
				timespan: {
					// Should probably grab this from csvasset
					start: 0,
					end: 100
				},
				engine: 'ciemss',
				extra: {
					num_samples: props.node.state.extra.numIterations,
					inferred_parameters: id
				}
			};
			const simulationResponse = await makeEnsembleCiemssSimulation(params, nodeMetadata(props.node));
			const forecastId = simulationResponse.simulationId;

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
			state.forecastRunId = id;
			emit('update-state', state);

			const portLabel = props.node.inputs[0].label;
			emit('append-output', {
				type: 'calibrateSimulationId',
				label: nodeOutputLabel(props.node, `${portLabel} Result`),
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
		if (!state.forecastRunId) return;

		const forecastId = state.forecastRunId;

		// Simulate
		const result = await getRunResultCiemss(forecastId, 'result.csv');
		runResults.value = result.runResults;

		// Dataset used to calibrate
		const datasetId = props.node.inputs[0]?.value?.[0];
		setupCsvAsset(datasetId.value).then((csv) => {
			csvAsset.value = csv;
		});
	},
	{ immediate: true }
);

watch(
	() => props.node.inputs,
	() => {
		if (
			props.node.inputs.every(
				(input) => input.status === WorkflowPortStatus.CONNECTED || input.type !== 'modelConfigId'
			)
		) {
			emit('append-input-port', { type: 'modelConfigId', label: 'Model configuration' });
		}
	},
	{ deep: true }
);
</script>

<style scoped></style>
