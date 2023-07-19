<template>
	<div>
		<Button
			size="small"
			label="Run"
			@click="runEnsemble"
			:disabled="disableRunButton"
			icon="pi pi-play"
		></Button>
	</div>
	<section v-if="!showSpinner" class="result-container">
		<section v-if="simulationIds">
			<tera-simulate-chart
				v-for="(cfg, index) of node.state.chartConfigs"
				:key="index"
				:run-results="renderedRuns"
				:chartConfig="cfg"
				:line-color-array="lineColorArray"
				:line-width-array="lineWidthArray"
				@configuration-change="chartConfigurationChange(index, $event)"
			/>
			<Button
				class="add-chart"
				text
				:outlined="true"
				@click="addChart"
				label="Add Chart"
				icon="pi pi-plus"
			/>
		</section>
		<section v-else class="result-container">
			<div class="invalid-block" v-if="node.statusCode === WorkflowStatus.INVALID">
				<img class="image" src="@assets/svg/plants.svg" alt="" />
				<p>Configure in side panel</p>
			</div>
		</section>
	</section>
	<section v-else>
		<div>loading...</div>
	</section>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, watch, computed, ComputedRef } from 'vue';
// import { csvParse } from 'd3';
// import { ModelConfiguration } from '@/types/Types';
// import { getRunResult } from '@/services/models/simulation-service';
import { WorkflowNode, WorkflowStatus } from '@/types/workflow';
// import { getModelConfigurationById } from '@/services/model-configurations';
import { workflowEventBus } from '@/services/workflow';
import {
	EnsembleSimulationCiemssRequest,
	Simulation,
	TimeSpan,
	EnsembleModelConfigs
} from '@/types/Types';
import {
	getSimulation,
	makeEnsembleCiemssSimulation,
	getRunResultCiemss
} from '@/services/models/simulation-service';
import Button from 'primevue/button';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import {
	CalibrateEnsembleCiemssOperationState,
	CalibrateEnsembleCiemssOperation
} from './calibrate-ensemble-ciemss-operation';
import TeraSimulateChart from './tera-simulate-chart.vue';

const props = defineProps<{
	node: WorkflowNode;
}>();
const emit = defineEmits(['append-output-port']);

const showSpinner = ref(false);
const modelConfigIds = computed<string[]>(() => props.node.inputs[0].value as string[]);
const startedRunId = ref<string>();
const completedRunId = ref<string>();
const disableRunButton = computed(() => !ensembleConfigs?.value[0]?.weight);
const ensembleConfigs = computed<EnsembleModelConfigs[]>(() => props.node.state.mapping);
const timeSpan = computed<TimeSpan>(() => props.node.state.timeSpan);
const numSamples = ref<number>(props.node.state.numSamples);
const runResults = ref<RunResults>({});
const simulationIds: ComputedRef<any | undefined> = computed(
	<any | undefined>(() => props.node.outputs[0]?.value)
);
// TODO Post hackathon, this entire thing should be computed or even just thrown into run result.
const renderedRuns = ref<RunResults>({});

const runEnsemble = async () => {
	const params: EnsembleSimulationCiemssRequest = {
		modelConfigs: ensembleConfigs.value,
		timespan: timeSpan.value,
		engine: 'ciemss',
		extra: { num_samples: numSamples.value }
	};
	const response = await makeEnsembleCiemssSimulation(params);
	startedRunId.value = response.simulationId;

	showSpinner.value = true;
	getStatus();
};

// Tom TODO: Make this generic, its copy paste from drilldown
const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state: CalibrateEnsembleCiemssOperationState = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

// TODO: This is repeated every single node that uses a chart. Hope to refactor if the state manip allows for it easily
const addChart = () => {
	const state: CalibrateEnsembleCiemssOperationState = _.cloneDeep(props.node.state);
	state.chartConfigs.push({ selectedRun: '', selectedVariable: [] } as ChartConfig);

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const getStatus = async () => {
	if (!startedRunId.value) return;

	const currentSimulation: Simulation | null = await getSimulation(startedRunId.value); // get TDS's simulation object
	const ongoingStatusList = ['running', 'queued'];

	if (currentSimulation && currentSimulation.status === 'complete') {
		completedRunId.value = startedRunId.value;
		updateOutputPorts(completedRunId);
		addChart();
		showSpinner.value = false;
	} else if (currentSimulation && ongoingStatusList.includes(currentSimulation.status)) {
		// recursively call until all runs retrieved
		setTimeout(getStatus, 3000);
	} else {
		// throw if there are any failed runs for now
		console.error('Failed', startedRunId.value);
		throw Error('Failed Runs');
	}
};

const updateOutputPorts = async (runId) => {
	const portLabel = props.node.inputs[0].label;
	emit('append-output-port', {
		type: CalibrateEnsembleCiemssOperation.outputs[0].type,
		label: `${portLabel} Result`,
		value: { runId }
	});
};

const lineColorArray = computed(() => {
	const output = Array(Math.max(Object.keys(runResults.value).length ?? 0 - 1, 0)).fill(
		'#00000020'
	);
	output.push('#1b8073');
	return output;
});

const lineWidthArray = computed(() => {
	const output = Array(Math.max(Object.keys(runResults.value).length ?? 0 - 1, 0)).fill(1);
	output.push(2);
	return output;
});

watch(
	() => modelConfigIds.value,
	async () => {
		if (modelConfigIds.value) {
			const mapping: EnsembleModelConfigs[] = [];
			// Init ensemble Configs:
			for (let i = 0; i < modelConfigIds.value.length; i++) {
				mapping[i] = {
					id: modelConfigIds.value[i],
					solutionMappings: {},
					weight: 0
				};
			}

			const state: CalibrateEnsembleCiemssOperationState = _.cloneDeep(props.node.state);
			state.modelConfigIds = modelConfigIds.value;
			state.mapping = mapping;
			workflowEventBus.emitNodeStateChange({
				workflowId: props.node.workflowId,
				nodeId: props.node.id,
				state
			});
		}
	},
	{ immediate: true }
);

watch(
	() => simulationIds.value,
	async () => {
		if (!simulationIds.value) return;

		const output = await getRunResultCiemss(simulationIds.value[0].runId, 'simulation.csv');
		runResults.value = output.runResults;
	},
	{ immediate: true }
);

// TODO Post hackathon, this entire thing should be computed or even just thrown into run result.
watch(
	() => runResults.value,
	(input) => {
		const runResult: RunResults = JSON.parse(JSON.stringify(input));

		// convert to array from array-like object
		const parsedSimProbData = Object.values(runResult);

		const numRuns = parsedSimProbData.length;
		if (!numRuns) {
			renderedRuns.value = runResult;
			return;
		}

		const numTimestamps = (parsedSimProbData as { [key: string]: number }[][])[0].length;
		const aggregateRun: { [key: string]: number }[] = [];

		for (let timestamp = 0; timestamp < numTimestamps; timestamp++) {
			for (let run = 0; run < numRuns; run++) {
				if (!aggregateRun[timestamp]) {
					aggregateRun[timestamp] = parsedSimProbData[run][timestamp];
					Object.keys(aggregateRun[timestamp]).forEach((key) => {
						aggregateRun[timestamp][key] = Number(aggregateRun[timestamp][key]) / numRuns;
					});
				} else {
					const datum = parsedSimProbData[run][timestamp];
					Object.keys(datum).forEach((key) => {
						aggregateRun[timestamp][key] += datum[key] / numRuns;
					});
				}
			}
		}

		renderedRuns.value = { ...runResult, [numRuns]: aggregateRun };
	},
	{ immediate: true, deep: true }
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

.result-container {
	align-items: center;
}
.image {
	height: 8.75rem;
	margin-bottom: 0.5rem;
	background-color: var(--surface-ground);
	border-radius: 1rem;
	background-color: rgb(0, 0, 0, 0);
}
.invalid-block {
	display: contents;
}
.simulate-chart {
	margin: 1em 0em;
}

.add-chart {
	width: 9em;
}
</style>
