<template>
	<main>
		<div class="test-container">
			<h2>FUNMAN Test</h2>
			<svg></svg>
		</div>
	</main>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import * as d3 from 'd3';
import funModel from '@/examples/fun-example.json';
import { processFunman, getBoxes, getTrajectories } from '@/services/funman';

onMounted(() => {
	console.log(processFunman(funModel));

	console.log(getTrajectories(funModel));

	const width = 800;
	const height = 500;

	const svg = d3.select('svg').attr('width', width).attr('height', height);
	const g = svg.append('g');

	const trueBoxes = getBoxes(funModel, 'beta', 'gamma', 7, 'true_boxes');
	console.log(trueBoxes);

	const falseBoxes = getBoxes(funModel, 'beta', 'gamma', 7, 'false_boxes');
	console.log(falseBoxes);

	g.selectAll('foo')
		.data(trueBoxes)
		.enter()
		.append('rect')
		.attr('x', (d) => d.x1 * 1000)
		.attr('y', (d) => d.y1 * 1000)
		.attr('width', (d) => d.x2 * 1000 - d.x1 * 1000)
		.attr('height', (d) => d.y2 * 1000 - d.y1 * 1000)
		.attr('stroke', 'black')
		.attr('fill-opacity', '.5')
		.attr('fill', 'teal');

	g.selectAll('foo')
		.data(falseBoxes)
		.enter()
		.append('rect')
		.attr('x', (d) => d.x1 * 1000)
		.attr('y', (d) => d.y1 * 1000)
		.attr('width', (d) => d.x2 * 1000 - d.x1 * 1000)
		.attr('height', (d) => d.y2 * 1000 - d.y1 * 1000)
		.attr('stroke', 'black')
		.attr('fill-opacity', '.5')
		.attr('fill', 'orange');
});
</script>

<style scoped>
.test-cotainer {
	display: flex;
	flex-direction: column;
	gap: 5rem;
}
</style>
