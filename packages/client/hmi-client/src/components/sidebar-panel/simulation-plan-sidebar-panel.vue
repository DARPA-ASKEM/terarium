<script setup lang="ts">
import { Project } from '@/types/Project';
import { useRouter } from 'vue-router';

defineProps<{
	project: Project;
}>();

const router = useRouter();
const openSimulationPlan = (runId: string | number) => {
	router.push({ params: { simulationId: runId } });
};
</script>

<template>
	<ul>
		<li
			v-for="simulationId of project.assets.plans"
			:key="simulationId"
			class="selectable-item"
			@click="openSimulationPlan(simulationId)"
		>
			{{ simulationId }}
		</li>
	</ul>
</template>

<style>
.selectable-item:hover {
	background-color: var(--un-color-black-20);
	cursor: pointer;
}
</style>
