<template>
	<main>
		<template
			v-if="
				!inProgressCalibrationId && runResult && csvAsset && runResultPre && props.node.state.chartConfigs[0]?.length
			"
		>
			<vega-chart
				v-for="(_config, index) of props.node.state.chartConfigs"
				:key="index"
				:are-embed-actions-visible="false"
				:visualization-spec="preparedCharts[index]"
			/>
		</template>
		<div v-else ref="drilldownLossPlot" class="loss-chart" />

		<tera-progress-spinner v-if="inProgressCalibrationId" :font-size="2" is-centered style="height: 100%">
			<div>{{ props.node.state.currentProgress }}%</div>
		</tera-progress-spinner>

		<Button v-if="areInputsFilled" label="Edit" @click="emit('open-drilldown')" severity="secondary" outlined />
		<tera-operator-placeholder v-else :node="node">
			Connect a model configuration and dataset
		</tera-operator-placeholder>
	</main>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { csvParse, autoType } from 'd3';
import { computed, watch, ref, shallowRef, onMounted } from 'vue';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import {
	getRunResultCSV,
	pollAction,
	makeForecastJobCiemss,
	getSimulation,
	parsePyCiemssMap,
	DataArray
} from '@/services/models/simulation-service';
import { getModelConfigurationById, createModelConfiguration } from '@/services/model-configurations';
import { getModelByModelConfigurationId, getUnitsFromModelParts } from '@/services/model';
import { renderLossGraph, setupDatasetInput } from '@/services/calibrate-workflow';
import { nodeMetadata, nodeOutputLabel } from '@/components/workflow/util';
import { logger } from '@/utils/logger';
import { Poller, PollerState } from '@/api/api';
import type { WorkflowNode } from '@/types/workflow';
import {
	CsvAsset,
	Simulation,
	SimulationRequest,
	Model,
	ModelConfiguration,
	SemanticType,
	InferredParameterSemantic
} from '@/types/Types';
import { createLLMSummary } from '@/services/summary-service';
import { createForecastChart } from '@/services/charts';
import VegaChart from '@/components/widgets/VegaChart.vue';
import * as stats from '@/utils/stats';
import type { CalibrationOperationStateCiemss } from './calibrate-operation';
import { renameFnGenerator, mergeResults } from './calibrate-utils';

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateCiemss>;
}>();
const emit = defineEmits(['open-drilldown', 'update-state', 'append-output']);

const modelConfigId = computed<string | undefined>(() => props.node.inputs[0].value?.[0]);

const model = ref<Model | null>(null);
const modelVarUnits = ref<{ [key: string]: string }>({});

const runResult = ref<DataArray>([]);
const runResultPre = ref<DataArray>([]);
const runResultSummary = ref<DataArray>([]);
const runResultSummaryPre = ref<DataArray>([]);
const drilldownLossPlot = ref<HTMLElement>();

const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

const areInputsFilled = computed(() => props.node.inputs[0].value && props.node.inputs[1].value);
const inProgressCalibrationId = computed(() => props.node.state.inProgressCalibrationId);

let lossValues: { [key: string]: number }[] = [];

function drawLossGraph() {
	if (drilldownLossPlot.value) {
		renderLossGraph(drilldownLossPlot.value, lossValues, {
			width: 200,
			height: 120
		});
	}
}

async function updateLossChartWithSimulation() {
	if (props.node.active) {
		const simulationObj = await getSimulation(props.node.state.calibrationId);
		if (simulationObj?.updates) {
			lossValues = simulationObj?.updates.map((d, i) => ({
				iter: i,
				loss: d.data.loss
			}));
			drawLossGraph();
		}
	}
}

onMounted(async () => updateLossChartWithSimulation());

let pyciemssMap: Record<string, string> = {};

const preparedCharts = computed(() => {
	const state = props.node.state;

	if (!runResult.value || !csvAsset.value || !runResultPre.value) return [];

	// Merge before/after for chart
	const { result, resultSummary } = mergeResults(
		runResult.value,
		runResultPre.value,
		runResultSummary.value,
		runResultSummaryPre.value
	);

	// Build lookup map for calibration, include before/afer and dataset (observations)
	const reverseMap: Record<string, string> = {};
	Object.keys(pyciemssMap).forEach((key) => {
		reverseMap[`${pyciemssMap[key]}_mean`] = `${key} after calibration`;
		reverseMap[`${pyciemssMap[key]}_mean:pre`] = `${key} before calibration`;
	});
	state.mapping.forEach((mapObj) => {
		reverseMap[mapObj.datasetVariable] = 'Observations';
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
				data: result,
				variables: [...config.map((d) => `${pyciemssMap[d]}:pre`), ...config.map((d) => pyciemssMap[d])],
				timeField: 'timepoint_id',
				groupField: 'sample_id'
			},
			{
				data: resultSummary,
				variables: [...config.map((d) => `${pyciemssMap[d]}_mean:pre`), ...config.map((d) => `${pyciemssMap[d]}_mean`)],
				timeField: 'timepoint_id'
			},
			{
				data: groundTruth,
				variables: datasetVariables,
				timeField: datasetTimeField as string
			},
			{
				title: '',
				width: 180,
				height: 120,
				legend: true,
				translationMap: reverseMap,
				xAxisTitle: modelVarUnits.value._time || 'Time',
				yAxisTitle: _.uniq(config.map((v) => modelVarUnits.value[v]).filter((v) => !!v)).join(',') || '',
				colorscheme: ['#AAB3C6', '#1B8073']
			}
		);
	});
});

const poller = new Poller();
const pollResult = async (runId: string) => {
	poller
		.setInterval(3000)
		.setThreshold(350)
		.setPollAction(async () => pollAction(runId))
		.setProgressAction((data: Simulation) => {
			if (data?.updates?.length) {
				lossValues = data?.updates.map((d, i) => ({
					iter: i,
					loss: d.data.loss
				}));
				drawLossGraph();
			}
			if (runId === props.node.state.inProgressCalibrationId && data.updates.length > 0) {
				const checkpoint = _.first(data.updates);
				if (checkpoint) {
					const state = _.cloneDeep(props.node.state);
					state.currentProgress = +((100 * checkpoint.data.progress) / state.numIterations).toFixed(2);
					emit('update-state', state);
				}
			}
		});

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
	() => props.node.state.inProgressCalibrationId,
	async (id) => {
		if (!id || id === '') return;

		const response = await pollResult(id);
		const state = _.cloneDeep(props.node.state);

		const baseRequestPayload: SimulationRequest = {
			modelConfigId: modelConfigId.value as string,
			timespan: {
				start: 0,
				end: state.endTime
			},
			extra: {
				num_samples: state.numSamples,
				method: 'dopri5'
			},
			engine: 'ciemss'
		};

		// Calibration has finished, now kick of two forecast jobs to do comparison
		// - using the default configuration ()
		// - using the calibfrated configuration
		if (response.state === PollerState.Done) {
			// Default (Pre)
			let forecastResponse = await makeForecastJobCiemss(baseRequestPayload, nodeMetadata(props.node));
			state.inProgressPreForecastId = forecastResponse.id;

			// With calibrated result
			baseRequestPayload.extra.inferred_parameters = id;
			forecastResponse = await makeForecastJobCiemss(baseRequestPayload, nodeMetadata(props.node));
			state.inProgressForecastId = forecastResponse.id;

			state.inProgressCalibrationId = '';
			state.calibrationId = id;
			emit('update-state', state);
		}
	},
	{ immediate: true }
);

watch(
	() => props.node.state.inProgressForecastId + props.node.state.inProgressPreForecastId,
	async (id) => {
		if (!id || id === '') return;

		let doneProcess = true;
		let response = await pollResult(props.node.state.inProgressPreForecastId);
		if (response.state !== PollerState.Done) {
			doneProcess = false;
		}

		response = await pollResult(props.node.state.inProgressForecastId);
		if (response.state !== PollerState.Done) {
			doneProcess = false;
		}

		if (doneProcess) {
			const state = _.cloneDeep(props.node.state);
			if (state.chartConfigs.length === 0) {
				state.chartConfigs = [[]];
			}
			state.forecastId = state.inProgressForecastId;
			state.preForecastId = state.inProgressPreForecastId;

			state.inProgressForecastId = '';
			state.inProgressPreForecastId = '';
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
			const baseConfig = await getModelConfigurationById(modelConfigId.value as string);

			// Computed sampled model parameters from the forecast
			// FIXME: this is highly dependent on the number of samples, we may want to compute
			// the mean/stddev metrics independently of the forecsts directly querying the dill file
			const sampledData = await getRunResultCSV(state.forecastId, 'result.csv');
			const translationMap = parsePyCiemssMap(sampledData[0]);

			const timeId = sampledData[0].timepoint_id;
			const ode = model.value?.semantics?.ode;
			const parameterIds = ode?.parameters?.map((d) => d.id) as string[];
			const parameterTable: { [key: string]: number[] } = {};
			parameterIds.forEach((parameterId) => {
				parameterTable[parameterId] = [];
			});

			sampledData.forEach((point) => {
				if (point.timepoint_id === timeId) {
					parameterIds.forEach((parameterId) => {
						let dataKey = translationMap[parameterId];

						// propbably initials, need a proxy-translation map
						if (!dataKey) {
							const surrogateKey = ode?.initials?.find((d) => d.expression === parameterId)?.target;
							if (!surrogateKey) return;

							dataKey = translationMap[surrogateKey];
							if (!dataKey) return;
						}
						parameterTable[parameterId].push(point[dataKey]);
					});
				}
			});

			const inferredParameters: InferredParameterSemantic[] = [];
			Object.keys(parameterTable).forEach((parameterId) => {
				const mean = stats.mean(parameterTable[parameterId]);
				const stddev = stats.stddev(parameterTable[parameterId]);

				inferredParameters.push({
					source: 'calibration',
					type: SemanticType.Inferred,
					referenceId: parameterId,
					default: false,
					distribution: {
						type: 'inferred',
						parameters: {
							mean,
							stddev
						}
					}
				});
			});

			const calibratedModelConfig: ModelConfiguration = {
				name: `Calibrated: ${baseConfig.name}`,
				description: `Calibrated: ${baseConfig.description}`,
				simulationId: state.calibrationId,
				modelId: baseConfig.modelId,
				observableSemanticList: _.cloneDeep(baseConfig.observableSemanticList),
				parameterSemanticList: [],
				initialSemanticList: _.cloneDeep(baseConfig.initialSemanticList),
				inferredParameterList: inferredParameters
			};
			const modelConfigResponse = await createModelConfiguration(calibratedModelConfig);

			// const portLabel = props.node.inputs[0].label;
			emit('append-output', {
				type: 'modelConfigId',
				label: nodeOutputLabel(props.node, `Calibration Result`),
				value: [modelConfigResponse.id],
				state: {
					calibrationId: state.calibrationId,
					forecastId: state.forecastId,
					preForecastId: state.preForecastId,
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

		// Simulates
		runResult.value = await getRunResultCSV(state.forecastId, 'result.csv');
		runResultSummary.value = await getRunResultCSV(state.forecastId, 'result_summary.csv');

		runResultPre.value = await getRunResultCSV(state.preForecastId, 'result.csv', renameFnGenerator('pre'));
		runResultSummaryPre.value = await getRunResultCSV(
			state.preForecastId,
			'result_summary.csv',
			renameFnGenerator('pre')
		);

		pyciemssMap = parsePyCiemssMap(runResult.value[0]);

		// Dataset used to calibrate
		const datasetId = props.node.inputs[1]?.value?.[0];
		const { csv } = await setupDatasetInput(datasetId);
		csvAsset.value = csv;
	},
	{ immediate: true }
);
</script>

<style scoped></style>
