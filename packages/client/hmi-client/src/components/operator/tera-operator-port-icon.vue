<template>
	<i v-if="typeof portIcon === 'string'" class="p-chip-icon" :class="`pi ${portIcon}`" />
	<!-- <component v-else :is="portIcon" class="p-button-icon-left icon" /> -->
</template>

<script setup lang="ts">
import { computed } from 'vue';
// import DatasetIcon from '@/assets/svg/icons/dataset.svg?component';

const props = defineProps<{
	portType: string;
}>();

const icons = new Map<string, string>([
	// FIXME: should be <string, string | Component>, for some reason importing Component here breaks everything
	['documentId', 'pi-file'],
	['modelId', 'pi-share-alt'],
	['datasetId', 'pi-table'],
	['datasetId|simulationId', 'pi-table'],
	// Use these once FIXME is solved
	// ['datasetId', DatasetIcon],
	// ['datasetId|simulationId', DatasetIcon],
	['calibrateSimulationId', 'pi-chart-line'],
	['simulationId', 'pi-chart-line'],
	['modelConfigId', 'pi-cog']
]);

const portIcon = computed(() => {
	if (icons.has(props.portType)) {
		return icons.get(props.portType) ?? 'circle';
	}
	return 'circle';
});
</script>

<style scoped>
svg {
	scale: 0.8;
}
</style>
