<script setup lang="ts">
import { Project } from '@/types/Project';
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import ArtifactList from './artifact-list.vue';

const props = defineProps<{
	project: Project;
}>();

const router = useRouter();
const openSimulationPlan = (runId: string | number) => {
	router.push({ params: { simulationId: runId } });
};
const simulationPlansAsArtifactList = computed(() =>
	props.project.assets.plans.map((planId) => ({
		id: planId,
		name: planId.toString()
	}))
);
const route = useRoute();
const selectedArtifactId = computed(() => {
	// route.params values can be string or string[], safe to assume there is
	//	only one value for this param.
	const planId = route.params.simulationId as string;
	// Convert that string to a (decimal) number so it's comparable with the plan
	//	IDs we display in the list.
	return Number.parseInt(planId, 10);
});
</script>

<template>
	<!-- FIXME: currently no way to delete simulation plans -->
	<!-- @remove-artifact="deleteSimulationPlan" -->
	<ArtifactList
		:artifacts="simulationPlansAsArtifactList"
		:selected-artifact-id="selectedArtifactId"
		@artifact-clicked="openSimulationPlan"
	/>
</template>
