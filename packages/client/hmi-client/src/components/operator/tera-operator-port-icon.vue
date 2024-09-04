<template>
	<i class="p-chip-icon" :class="`pi ${icons.get(portType) ?? 'pi-cog'}`" />
</template>

<script setup lang="ts">
defineProps<{ portType: string }>();
const icons = new Map<string, string>([
	['documentId', 'pi-file'],
	['modelId', 'pi-share-alt'],
	['datasetId', 'pi-table'],
	['datasetId|simulationId', 'pi-table'],
	['calibrateSimulationId', 'pi-chart-line'],
	['simulationId', 'pi-chart-line'],
	['modelConfigId', 'pi-cog']
]);
</script>
