<template>
	<main>
		<template v-if="!inProgressCalibrationId && runResult && csvAsset">
			<vega-chart
				v-for="(_config, index) of props.node.state.chartConfigs"
				:key="index"
				:are-embed-actions-visible="false"
				:visualization-spec="preparedCharts[index]"
			/>
		</template>

		<tera-progress-spinner v-if="inProgressCalibrationId" :font-size="2" is-centered style="height: 100%" />

		<Button v-if="areInputsFilled" label="Edit" @click="emit('open-drilldown')" severity="secondary" outlined />
		<tera-operator-placeholder v-else :operation-type="node.operationType">
			Connect a model configuration and dataset
		</tera-operator-placeholder>
	</main>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { csvParse, autoType } from 'd3';
import { computed, watch, ref, shallowRef } from 'vue';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import {
	getRunResultCSV,
	pollAction,
	getCalibrateBlobURL,
	makeForecastJobCiemss,
	getSimulation,
	parsePyCiemssMap,
	DataArray
} from '@/services/models/simulation-service';
import { setupDatasetInput } from '@/services/calibrate-workflow';
import { nodeMetadata, nodeOutputLabel } from '@/components/workflow/util';
import { logger } from '@/utils/logger';
import { Poller, PollerState } from '@/api/api';
import type { WorkflowNode } from '@/types/workflow';
import type { CsvAsset } from '@/types/Types';
import { createLLMSummary } from '@/services/summary-service';
import { createForecastChart } from '@/services/charts';
import VegaChart from '@/components/widgets/VegaChart.vue';
import type { CalibrationOperationStateCiemss } from './calibrate-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateCiemss>;
}>();
const emit = defineEmits(['open-drilldown', 'update-state', 'append-output']);

const modelConfigId = computed<string | undefined>(() => props.node.inputs[0].value?.[0]);

const runResult = ref<DataArray>([]);
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

const areInputsFilled = computed(() => props.node.inputs[0].value && props.node.inputs[1].value);
const inProgressCalibrationId = computed(() => props.node.state.inProgressCalibrationId);

let pyciemssMap: Record<string, string> = {};
const preparedCharts = computed(() => {
	const state = props.node.state;

	if (!runResult.value || !csvAsset.value) return [];

	const result = runResult.value;

	const reverseMap: Record<string, string> = {};
	Object.keys(pyciemssMap).forEach((key) => {
		reverseMap[`${pyciemssMap[key]}`] = key;
	});

	// FIXME: Hacky re-parse CSV with correct data types
	let groundTruth: DataArray = [];
	const csv = csvAsset.value.csv;
	const csvRaw = csv.map((d) => d.join(',')).join('\n');
	groundTruth = csvParse(csvRaw, autoType);

	// Need to get the dataset's time field
	const datasetTimeField = state.mapping.find((d) => d.modelVariable === 'timestamp')?.datasetVariable;

	return state.chartConfigs.map((config) => {
		const datasetVariables: string[] = [];
		config.forEach((variableName) => {
			const mapObj = state.mapping.find((d) => d.modelVariable === variableName);
			if (mapObj) {
				datasetVariables.push(mapObj.datasetVariable);
			}
		});

		return createForecastChart(
			{
				dataset: result,
				variables: config.map((d) => pyciemssMap[d]),
				timeField: 'timepoint_id',
				groupField: 'sample_id'
			},
			null,
			{
				dataset: groundTruth,
				variables: datasetVariables,
				timeField: datasetTimeField as string,
				groupField: 'sample_id'
			},
			{
				width: 180,
				height: 120,
				legend: false,
				translationMap: reverseMap,
				xAxisTitle: '',
				yAxisTitle: ''
			}
		);
	});
});

const poller = new Poller();
const pollResult = async (runId: string) => {
	poller
		.setInterval(4000)
		.setThreshold(350)
		.setPollAction(async () => pollAction(runId));
	const pollerResults = await poller.start();
	let state = _.cloneDeep(props.node.state);
	state.errorMessage = { name: '', value: '', traceback: '' };

	if (pollerResults.state === PollerState.Cancelled) {
		state.inProgressForecastId = '';
		state.inProgressCalibrationId = '';
		poller.stop();
	} else if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		logger.error(`Calibration: ${runId} has failed`, {
			toastTitle: 'Error - Pyciemss'
		});
		const simulation = await getSimulation(runId);
		if (simulation?.status && simulation?.statusMessage) {
			state = _.cloneDeep(props.node.state);
			state.inProgressCalibrationId = '';
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

watch(
	() => props.node.state.inProgressCalibrationId,
	async (id) => {
		if (!id || id === '') return;

		const response = await pollResult(id);
		if (response.state === PollerState.Done) {
			// Start 2nd simulation to get sample simulation from dill
			const dillURL = await getCalibrateBlobURL(id);
			console.log('dill URL is', dillURL);

			const forecastResponse = await makeForecastJobCiemss(
				{
					modelConfigId: modelConfigId.value as string,
					timespan: {
						start: 0,
						end: props.node.state.endTime
					},
					extra: {
						num_samples: props.node.state.numSamples,
						method: 'dopri5',
						inferred_parameters: id
					},
					engine: 'ciemss'
				},
				nodeMetadata(props.node)
			);
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

			// Get the calibrate losses to generate a run summary
			const calibrateResponse = await pollAction(state.calibrationId);
			const calibreateUpdates = calibrateResponse.data?.updates;
			const errorStart = _.first(calibreateUpdates)?.data;
			const errorEnd = _.last(calibreateUpdates)?.data;

			const prompt = `
		The following are the key attributes of a calibration/fitting process for a ODE epidemilogy model.

		- Fitting: ${JSON.stringify(state.mapping)}
		- Loss at start is: ${JSON.stringify(errorStart)}
		- Loss at end is: ${JSON.stringify(errorEnd)}

		Provide a summary in 100 words or less.
			`;
			const summaryResponse = await createLLMSummary(prompt);

			const portLabel = props.node.inputs[0].label;
			emit('append-output', {
				type: 'calibrateSimulationId',
				label: nodeOutputLabel(props.node, `${portLabel} Result`),
				value: [state.calibrationId],
				state: {
					calibrationId: state.calibrationId,
					forecastId: state.forecastId,
					numIterations: state.numIterations,
					numSamples: state.numSamples,
					summaryId: summaryResponse?.id
				}
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

		// Simulate
		const result = await getRunResultCSV(state.forecastId, 'result.csv');
		pyciemssMap = parsePyCiemssMap(result[0]);
		runResult.value = result;

		// Dataset used to calibrate
		const datasetId = props.node.inputs[1]?.value?.[0];
		const { csv } = await setupDatasetInput(datasetId);
		csvAsset.value = csv;
	},
	{ immediate: true }
);
</script>

<style scoped></style>
