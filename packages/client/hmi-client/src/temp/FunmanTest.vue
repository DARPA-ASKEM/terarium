<template>
	<div class="test-container">
		<h2>FUNMAN Test</h2>
		<svg></svg>
	</div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import funModel from '@/examples/fun-example.json';
import { processFunman, getTrajectories, createBoundaryChart } from '@/services/funman';

onMounted(() => {
	console.log(processFunman(funModel));
	console.log(getTrajectories(funModel));

	const width = 800;
	const height = 500;
	const xAxis = 1;
	const yAxis = 1;

	createBoundaryChart('svg', funModel, { width, height, xAxis, yAxis });
});
</script>

<style scoped>
.test-container {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}
</style>
