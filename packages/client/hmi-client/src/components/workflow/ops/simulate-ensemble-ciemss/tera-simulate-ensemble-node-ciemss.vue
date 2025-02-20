<template>
	<main>
		<section>
			<tera-node-preview
				:node="node"
				:is-loading="!!inProgressForecastId"
				:prepared-charts="Object.assign({}, useEnsembleVariableCharts)"
				:chart-settings="[...ensembleVariableCharts]"
				:are-embed-actions-visible="true"
				:placeholder="placeholderText"
			/>
		</section>
		<Button v-if="areInputsFilled" label="Open" @click="emit('open-drilldown')" severity="secondary" outlined />
	</main>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, computed, watch } from 'vue';
import { pollAction, getSimulation, getRunResultCSV, parsePyCiemssMap } from '@/services/models/simulation-service';
import Button from 'primevue/button';
import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import { RunResults } from '@/types/SimulateConfig';
import { Poller, PollerState } from '@/api/api';
import { logger } from '@/utils/logger';
import { nodeOutputLabel } from '@/components/workflow/util';
import { useProjects } from '@/composables/project';
import { createDatasetFromSimulationResult } from '@/services/dataset';
import { useCharts } from '@/composables/useCharts';
import { ModelConfiguration } from '@/types/Types';
import { DataArray } from '@/utils/stats';
import { useChartSettings } from '@/composables/useChartSettings';
import { getChartEnsembleMapping, usePreparedChartInputs } from './simulate-ensemble-util';
import {
	SimulateEnsembleCiemssOperation,
	SimulateEnsembleCiemssOperationState
} from './simulate-ensemble-ciemss-operation';
import TeraNodePreview from '../tera-node-preview.vue';

const props = defineProps<{
	node: WorkflowNode<SimulateEnsembleCiemssOperationState>;
}>();
const emit = defineEmits(['append-output', 'update-state', 'open-drilldown', 'append-input-port']);

// const runResults = ref<RunResults>({});
const runResults = ref<{ [runId: string]: DataArray }>({});
const runResultsSummary = ref<{ [runId: string]: DataArray }>({});
const pyciemssMap = ref<Record<string, string>>({});
const inProgressForecastId = computed(() => props.node.state.inProgressForecastId);
const selectedRunId = ref<string>('');
const areInputsFilled = computed(() => props.node.inputs.filter((ele) => ele.value).length > 1);
// -------------- Charts && chart settings ----------------
const chartSize = ref<{ width: number; height: number }>({ width: 180, height: 120 });
const allModelConfigurations = ref<ModelConfiguration[]>([]);
const preparedChartInputs = usePreparedChartInputs(props, runResults, runResultsSummary, pyciemssMap);
const stateToModelConfigMap = ref<{ [key: string]: string[] }>({});
const selectedOutputMapping = computed(() => getChartEnsembleMapping(props.node, stateToModelConfigMap.value));

const { useEnsembleVariableCharts } = useCharts(
	props.node.id,
	null,
	allModelConfigurations,
	preparedChartInputs,
	chartSize,
	null,
	selectedOutputMapping
);

const { selectedEnsembleVariableSettings } = useChartSettings(props, emit);

const ensembleVariableCharts = computed(() => {
	const charts = useEnsembleVariableCharts(selectedEnsembleVariableSettings, null);
	const ensembleCharts = selectedEnsembleVariableSettings.value.map((setting) => {
		// Grab the first chart only since the rest of the charts are for model configurations charts
		const spec = charts.value[setting.id][0];
		// Make sure the chart since width of the chart can be too small if charts were small multiple charts.
		spec.width = chartSize.value.width;
		spec.height = chartSize.value.height + 100;
		return spec;
	});
	return ensembleCharts;
});

const isChartsEmpty = computed(() => _.isEmpty(ensembleVariableCharts.value));

const placeholderText = computed(() => {
	if (!areInputsFilled.value) {
		return 'Connect a model configuration';
	}
	if (isChartsEmpty.value) {
		return 'No variables selected';
	}
	return undefined;
});

const poller = new Poller();
const pollResult = async (runId: string) => {
	poller.setPollAction(async () => pollAction(runId));
	const pollerResults = await poller.start();

	if (pollerResults.state === PollerState.Cancelled) {
		poller.stop();
	} else if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		const simulation = await getSimulation(runId);
		if (simulation?.status && simulation?.statusMessage) {
			const state = _.cloneDeep(props.node.state);
			state.errorMessage = {
				name: runId,
				value: simulation.status,
				traceback: simulation.statusMessage
			};
			emit('update-state', state);
		}
		// throw if there are any failed runs for now
		logger.error(`Simulate: ${runId} has failed`, {
			toastTitle: 'Error - Pyciemss'
		});
	}
	return pollerResults;
};

const processResult = async () => {
	const portLabel = props.node.inputs[0].label;
	const state = _.cloneDeep(props.node.state);
	state.errorMessage = { name: '', value: '', traceback: '' };
	state.forecastId = state.inProgressForecastId;
	state.inProgressForecastId = '';
	const simulationId = state.forecastId;
	const datasetName = `Forecast run ${simulationId}`;
	const projectId = useProjects().activeProjectId.value;
	const datasetResult = await createDatasetFromSimulationResult(projectId, simulationId, datasetName, false);
	if (!datasetResult) {
		state.errorMessage = {
			name: 'Failed to create dataset',
			value: '',
			traceback: `Failed to create dataset from simulation result: ${simulationId}`
		};
		emit('update-state', state);
		return;
	}

	emit(
		'append-output',
		{
			type: SimulateEnsembleCiemssOperation.outputs[0].type,
			label: nodeOutputLabel(props.node, `${portLabel} Result`),
			value: [datasetResult.id],
			state: _.omit(state, ['chartSettings']),
			isSelected: false
		},
		state
	);
};

watch(
	() => props.node.inputs,
	() => {
		if (props.node.inputs.every((input) => input.status === WorkflowPortStatus.CONNECTED)) {
			emit('append-input-port', { type: 'modelConfigId', label: 'Model configuration' });
		}
	},
	{ deep: true }
);

watch(
	() => props.node.state.inProgressForecastId,
	async (id) => {
		console.log('hitting in progress watcher - todo check why 2 outputs on 1 ');
		if (!id || id === '') return;

		const response = await pollResult(id);
		if (response?.state === PollerState.Done) {
			processResult();
		}
	},
	{ immediate: true }
);

watch(
	() => props.node.active,
	async () => {
		const active = props.node.active;
		if (!active) return;

		selectedRunId.value = props.node.outputs.find((o) => o.id === active)?.value?.[0];
		if (!selectedRunId.value) return;
		const forecastId = props.node.state.forecastId;
		if (!forecastId) return;

		const [result, resultSummary] = await Promise.all([
			getRunResultCSV(forecastId, 'result.csv'),
			getRunResultCSV(forecastId, 'result_summary.csv')
		]);
		pyciemssMap.value = parsePyCiemssMap(result[0]);
		runResults.value[forecastId] = result;
		runResultsSummary.value[forecastId] = resultSummary;
	},
	{ immediate: true }
);
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	width: 100%;
	padding: 10px;
	background: var(--surface-overlay);
}

.simulate-chart {
	margin: 1em 0em;
}
</style>
