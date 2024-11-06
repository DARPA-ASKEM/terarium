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
		<vega-chart v-if="lossChartSpec" :are-embed-actions-visible="false" :visualization-spec="lossChartSpec" />

		<tera-progress-spinner
			v-if="inProgressCalibrationId || inProgressForecastId"
			:font-size="2"
			is-centered
			style="height: 100%"
		>
			{{ node.state.currentProgress }}%
		</tera-progress-spinner>

		<Button v-if="areInputsFilled" label="Edit" @click="emit('open-drilldown')" severity="secondary" outlined />
		<tera-operator-placeholder v-else :node="node">
			Connect a model configuration and dataset
		</tera-operator-placeholder>
	</main>
</template>

<script setup lang="ts">
import { computed, ref, shallowRef, watch, onMounted } from 'vue';
import _ from 'lodash';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraSimulateChart from '@/components/workflow/tera-simulate-chart.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import {
	getRunResultCiemss,
	pollAction,
	getCalibrateBlobURL,
	makeEnsembleCiemssSimulation,
	getSimulation
} from '@/services/models/simulation-service';
import { setupCsvAsset } from '@/services/calibrate-workflow';
import { chartActionsProxy, nodeMetadata, nodeOutputLabel } from '@/components/workflow/util';
import { logger } from '@/utils/logger';
import { Poller, PollerState } from '@/api/api';
import type { WorkflowNode } from '@/types/workflow';
import { WorkflowPortStatus } from '@/types/workflow';
import type { CsvAsset, EnsembleSimulationCiemssRequest, Dataset, Simulation } from '@/types/Types';
import type { RunResults } from '@/types/SimulateConfig';
import { getDataset } from '@/services/dataset';
import { createForecastChart, AUTOSIZE } from '@/services/charts';
import VegaChart from '@/components/widgets/VegaChart.vue';
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
let lossValues: { [key: string]: number }[] = [];
const lossChartSpec = ref();
const chartSize = { width: 180, height: 120 };

const chartProxy = chartActionsProxy(props.node, (state: CalibrateEnsembleCiemssOperationState) => {
	emit('update-state', state);
});

const poller = new Poller();
const pollResult = async (runId: string) => {
	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => pollAction(runId))
		.setProgressAction((data: Simulation) => {
			if (data?.updates?.length) {
				lossValues = data?.updates
					.sort((a, b) => a.data.progress - b.data.progress)
					.map((d, i) => ({
						iter: i,
						loss: d.data.loss
					}));
				updateLossChartSpec(lossValues);
			}
			if (runId === props.node.state.inProgressCalibrationId && data.updates.length > 0) {
				const checkpoint = _.first(data.updates);
				if (checkpoint) {
					const state = _.cloneDeep(props.node.state);
					state.currentProgress = +((100 * checkpoint.data.progress) / state.extra.numIterations).toFixed(2);
					emit('update-state', state);
				}
			}
		});
	const pollerResults = await poller.start();

	if (pollerResults.state === PollerState.Cancelled) {
		const state = _.cloneDeep(props.node.state);
		state.currentProgress = 0;
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

const updateLossChartSpec = (data: Record<string, any>[]) => {
	lossChartSpec.value = createForecastChart(
		null,
		{
			data,
			variables: ['loss'],
			timeField: 'iter'
		},
		null,
		{
			title: '',
			xAxisTitle: 'Solver iterations',
			yAxisTitle: 'Loss',
			autosize: AUTOSIZE.FIT,
			...chartSize,
			fitYDomain: true
		}
	);
};

async function updateLossChartWithSimulation() {
	if (props.node.active) {
		const simulationObj = await getSimulation(props.node.state.calibrationId);
		if (simulationObj?.updates) {
			lossValues = simulationObj?.updates
				.sort((a, b) => a.data.progress - b.data.progress)
				.map((d, i) => ({
					iter: i,
					loss: d.data.loss
				}));
			updateLossChartSpec(lossValues);
		}
	}
}

// Init loss chart
onMounted(async () => updateLossChartWithSimulation());

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
			state.currentProgress = 0;
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
		// Get dataset:
		const dataset: Dataset | null = await getDataset(datasetId);
		if (dataset) {
			setupCsvAsset(dataset).then((csv) => {
				csvAsset.value = csv;
			});
		}
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
