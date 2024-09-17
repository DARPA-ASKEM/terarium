<template>
	<Button :disabled="disabled" outlined severity="secondary" @click="cancelSimulation" label="Stop" />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Button from 'primevue/button';
import { cancelCiemssJob } from '@/services/models/simulation-service';
import { logger } from '@/utils/logger';

const props = defineProps<{
	simulationRunId?: string;
}>();

const disabled = computed(() => props.simulationRunId === '');

const cancelSimulation = async () => {
	if (!props.simulationRunId) return;
	await cancelCiemssJob(props.simulationRunId);
	logger.success(`Simulation ${props.simulationRunId} has been cancelled.`);
};
</script>

<style scoped>
.show-save-input {
	padding-left: 1em;
	padding-right: 2em;
}
</style>
