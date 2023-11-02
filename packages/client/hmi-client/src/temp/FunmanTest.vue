<template>
	<div class="test-container">
		<h2>FUNMAN Test</h2>

		<div ref="boxRef"></div>
		<div ref="trajRef"></div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import funModel from '@/examples/fun-example.json';
import { processFunman, renderFumanTrajectories, createBoundaryChart } from '@/services/funman';

const boxRef = ref();
const trajRef = ref();

const timestep = 7;
const boxId = 'box2';

onMounted(() => {
	const processedData = processFunman(funModel);

	const width = 300;
	const height = 200;
	const param1 = 'beta';
	const param2 = 'gamma';
	createBoundaryChart(boxRef.value, processedData, param1, param2, timestep, { width, height });
	renderFumanTrajectories(trajRef.value as HTMLElement, processedData, boxId, {
		width: 300,
		height: 100
	});
});
</script>

<style scoped>
.test-container {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}
</style>
