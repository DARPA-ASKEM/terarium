<template>
	<section v-if="!showSpinner" class="emptyState">
		<img src="@assets/svg/seed.svg" alt="" draggable="false" />
		<p class="helpMessage">
			Connect a model configuration and dataset, then configure in the side panel
		</p>
	</section>
	<section v-else>
		<tera-progress-bar :value="progress.value" :status="progress.status" />
	</section>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { ProgressState, WorkflowNode } from '@/types/workflow';
import {
	simulationPollAction,
	querySimulationInProgress
} from '@/services/models/simulation-service';
import { Poller, PollerState } from '@/api/api';
import TeraProgressBar from '@/workflow/tera-progress-bar.vue';
import { logger } from '@/utils/logger';
import { CalibrationOperationStateJulia } from './calibrate-operation';

// TODO: FIGURE OUT WHAT TO SHOW IN THIS WORKFLOW NODE WHILE CALIBRATION IS RUNNING FROM THE DRILLDOWN

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateJulia>;
}>();

const emit = defineEmits(['append-output-port', 'update-state']);

const showSpinner = ref(false);
const progress = ref({ status: ProgressState.RETRIEVING, value: 0 });

const poller = new Poller();

onMounted(() => {
	const runIds = querySimulationInProgress(props.node);
	if (runIds.length > 0) {
		getStatus(runIds);
	}
});

onUnmounted(() => {
	poller.stop();
});

const getStatus = async (simulationIds: string[]) => {
	showSpinner.value = true;

	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => simulationPollAction(simulationIds, props.node, progress, emit));
	const pollerResults = await poller.start();

	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		showSpinner.value = false;
		logger.error(`Calibrate: ${simulationIds} has failed`, {
			toastTitle: 'Error - Julia'
		});
		throw Error('Failed Runs');
	}

	showSpinner.value = false;
};
</script>

<style scoped>
.emptyState {
	align-self: center;
	display: flex;
	flex-direction: column;
	align-items: center;
	text-align: center;
	margin-bottom: 1rem;
	gap: 0.5rem;
}

.helpMessage {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
	width: 90%;
}

img {
	width: 20%;
}
</style>
