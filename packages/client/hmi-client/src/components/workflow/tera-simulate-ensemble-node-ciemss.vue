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
		<div class="invalid-block" v-if="props.node.statusCode === WorkflowStatus.INVALID">
			<img class="image" src="@assets/svg/plants.svg" alt="" />
			<p>Configure in side panel</p>
		</div>
	</section>
	<section v-else>
		<div>loading...</div>
	</section>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, watch, computed } from 'vue';
// import { csvParse } from 'd3';
// import { ModelConfiguration } from '@/types/Types';
// import { getRunResult } from '@/services/models/simulation-service';
import { WorkflowNode, WorkflowStatus } from '@/types/workflow';
// import { RunResults } from '@/types/SimulateConfig';
// import { getModelConfigurationById } from '@/services/model-configurations';
import { workflowEventBus } from '@/services/workflow';
import {
	EnsembleSimulationCiemssRequest,
	Simulation,
	TimeSpan,
	EnsembleModelConfigs
} from '@/types/Types';
import { getSimulation, makeEnsembleCiemssSimulation } from '@/services/models/simulation-service';
import Button from 'primevue/button';
import {
	EnsembleCiemssOperationState,
	EnsembleCiemssOperation
} from './simulate-ensemble-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode;
}>();
const emit = defineEmits(['append-output-port']);

const showSpinner = ref(false);
const modelConfigIds = computed<string[]>(() => props.node.inputs[0].value as string[]);
const startedRunId = ref<string>();
const completedRunId = ref<string>();
const disableRunButton = computed(() => !ensembleConfigs?.value[0]?.weight);
const ensembleConfigs = ref<EnsembleModelConfigs[]>(props.node.state.mapping);
const timeSpan = ref<TimeSpan>(props.node.state.timespan);
const numSamples = ref<number>(props.node.state.numSamples);

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

const getStatus = async () => {
	if (!startedRunId.value) return;

	const currentSimulation: Simulation | null = await getSimulation(startedRunId.value); // get TDS's simulation object
	const ongoingStatusList = ['running', 'queued'];

	if (currentSimulation && currentSimulation.status === 'complete') {
		completedRunId.value = startedRunId.value;
		updateOutputPorts(completedRunId);
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
		type: EnsembleCiemssOperation.outputs[0].type,
		label: `${portLabel} Result`,
		value: {
			runId
		}
	});
};

watch(
	() => modelConfigIds.value,
	async () => {
		if (modelConfigIds.value) {
			console.log(modelConfigIds.value);
			const mapping: EnsembleModelConfigs[] = [];
			// Init ensemble Configs:
			for (let i = 0; i < modelConfigIds.value.length; i++) {
				mapping[i] = {
					id: modelConfigIds.value[i],
					solutionMappings: {},
					weight: 0
				};
			}

			const state: EnsembleCiemssOperationState = _.cloneDeep(props.node.state);
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
