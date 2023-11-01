<template>
	<div class="test-container">
		<h2>FUNMAN Test</h2>
		<svg></svg>

		<div ref="traj"></div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import funModel from '@/examples/fun-example.json';
import {
	processFunman,
	getTrajectories,
	renderFumanTrajectories,
	createBoundaryChart
} from '@/services/funman';

const traj = ref();

const handleFunmanTrajectories = () => {
	console.log('hihi');

	if (traj) {
		renderFumanTrajectories(traj.value as HTMLElement, funModel, {});
	}
};

onMounted(() => {
	handleFunmanTrajectories();

	console.log(processFunman(funModel));
	console.log(getTrajectories(funModel));

	const width = 800;
	const height = 500;
	const param1 = 'beta';
	const param2 = 'gamma';

	createBoundaryChart('svg', funModel, param1, param2, { width, height });
});
</script>

<style scoped>
.test-container {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}
</style>
