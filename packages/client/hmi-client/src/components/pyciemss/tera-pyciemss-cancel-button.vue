<template>
	<Button
		:disabled="disabled"
		outlined
		severity="secondary"
		title="Saves the current version of the model as a new Terarium asset"
		@click="cancelSimulation"
		label="Cancel"
		icon="pi pi-times"
	/>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import { cancelCiemssJob } from '@/services/models/simulation-service';
import { logger } from '@/utils/logger';

const props = defineProps<{
	simulationRunId?: string;
	disabled: boolean;
}>();

const cancelSimulation = async () => {
	if (!props.simulationRunId) return;

	const result = await cancelCiemssJob(props.simulationRunId);
	console.log(result);
	logger.success(`Simulation ${props.simulationRunId} has been cancelled.`);
};
</script>

<style scoped>
.show-save-input {
	padding-left: 1em;
	padding-right: 2em;
}
</style>
