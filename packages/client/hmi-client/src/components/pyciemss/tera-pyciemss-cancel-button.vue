<template>
	<Button :disabled="disabled" outlined severity="secondary" @click="cancelSimulation" label="Stop" />
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ref } from 'vue';
import Button from 'primevue/button';
import { cancelCiemssJob } from '@/services/models/simulation-service';
import { logger } from '@/utils/logger';

const props = defineProps<{
	simulationRunId?: string | string[];
}>();

const isCancelling = ref(false);
const disabled = computed(() => _.isEmpty(props.simulationRunId) || isCancelling.value);

const cancelSimulation = async () => {
	if (!props.simulationRunId) return;
	isCancelling.value = true;
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
