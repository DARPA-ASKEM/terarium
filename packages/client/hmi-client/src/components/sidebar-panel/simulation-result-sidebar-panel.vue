<script setup lang="ts">
import { Project } from '@/types/Project';
import { useRouter } from 'vue-router';

defineProps<{
	project: Project;
}>();

const router = useRouter();
const openSimulationResult = (runId: string | number) => {
	router.push({ params: { simulationRunId: runId } });
};
</script>

<template>
	<ul>
		<li
			v-for="runId of project.assets.simulation_runs"
			:key="runId"
			class="selectable-item"
			@click="openSimulationResult(runId)"
		>
			{{ runId }}
		</li>
	</ul>
</template>

<style>
.selectable-item:hover {
	background-color: var(--un-color-black-20);
	cursor: pointer;
}
</style>
