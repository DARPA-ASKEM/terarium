<template>
	<Button :disabled="disabled" outlined severity="secondary" @click="cancelSimulation" label="Stop" />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Button from 'primevue/button';
import { cancelCiemssJob } from '@/services/models/simulation-service';
import { logger } from '@/utils/logger';

const props = defineProps<{
	simulationRunId?: string | string[];
}>();

const disabled = computed(() => props.simulationRunId === '' || props.simulationRunId?.length === 0);

const cancelSimulation = async () => {
	if (!props.simulationRunId) return;
	const cancelIds = Array.isArray(props.simulationRunId) ? props.simulationRunId : [props.simulationRunId];
	cancelIds.forEach(async (id) => {
		if (!id) return;
		await cancelCiemssJob(id);
		logger.success(`Simulation ${id} has been cancelled.`);
	});
};
</script>

<style scoped>
.show-save-input {
	padding-left: 1em;
	padding-right: 2em;
}
</style>
