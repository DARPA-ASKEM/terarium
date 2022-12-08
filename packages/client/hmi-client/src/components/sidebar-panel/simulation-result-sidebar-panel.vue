<script setup lang="ts">
import { Project } from '@/types/Project';
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import ArtifactList from './artifact-list.vue';

const props = defineProps<{
	project: Project;
}>();

const router = useRouter();
const openSimulationResult = (runId: string | number) => {
	router.push({ params: { simulationRunId: runId } });
};
const simulationRunsAsArtifactList = computed(() =>
	props.project.assets.simulation_runs.map((runId) => ({
		id: runId,
		name: runId.toString()
	}))
);
const route = useRoute();
const selectedArtifactId = computed(() => {
	// route.params values can be string or string[], safe to assume there is
	//	only one value for this param.
	const runId = route.params.simulationRunId as string;
	// Convert that string to a (decimal) number so it's comparable with the run
	//	IDs we display in the list.
	return Number.parseInt(runId, 10);
});
</script>

<template>
	<!-- FIXME: currently no way to delete simulation results -->
	<!-- @remove-artifact="deleteSimulationResult" -->
	<ArtifactList
		:artifacts="simulationRunsAsArtifactList"
		:selected-artifact-id="selectedArtifactId"
		@artifact-clicked="openSimulationResult"
	/>
</template>
