<template>
	<div class="test-container">
		<h2>FUNMAN Test</h2>
		<svg></svg>

		<div ref="traj"></div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import * as d3 from 'd3';
import funModel from '@/examples/fun-example.json';
import {
	processFunman,
	getBoxes,
	getTrajectories,
	renderFumanTrajectories
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

	const svg = d3.select('svg').attr('width', width).attr('height', height);
	const g = svg.append('g');

	const drawRects = (data, fill) => {
		g.selectAll('.rect')
			.data(data)
			.enter()
			.append('rect')
			.attr('x', (d: any) => d.x1 * 1000)
			.attr('y', (d: any) => d.y1 * 1000)
			.attr('width', (d: any) => d.x2 * 1000 - d.x1 * 1000)
			.attr('height', (d: any) => d.y2 * 1000 - d.y1 * 1000)
			.attr('stroke', 'black')
			.attr('fill-opacity', 0.5)
			.attr('fill', fill);
	};

	const trueBoxes = getBoxes(funModel, 'beta', 'gamma', 7, 'true_boxes');
	const falseBoxes = getBoxes(funModel, 'beta', 'gamma', 7, 'false_boxes');

	console.log(trueBoxes);
	console.log(falseBoxes);

	drawRects(trueBoxes, 'teal');
	drawRects(falseBoxes, 'orange');
});
</script>

<style scoped>
.test-container {
	display: flex;
	flex-direction: column;
	gap: 1rem;
}
</style>
