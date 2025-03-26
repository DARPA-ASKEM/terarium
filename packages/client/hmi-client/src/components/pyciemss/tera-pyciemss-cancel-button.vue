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
	simulationRunIds?: string[];
}>();

const isCancelling = ref(false);
const disabled = computed(() => _.isEmpty(props.simulationRunIds) || isCancelling.value);

const cancelSimulation = async () => {
	if (!props.simulationRunIds) return;
	isCancelling.value = true;
	props.simulationRunIds.forEach(async (id) => {
		if (!id) return;
		await cancelCiemssJob(id);
		logger.success(`Simulation ${id} has been cancelled.`);
	});
	isCancelling.value = false;
};
</script>

<style scoped>
.show-save-input {
	padding-left: 1em;
	padding-right: 2em;
}
</style>
