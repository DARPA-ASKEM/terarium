<template>
	<main id="canvas" ref="canvasRef">
		<slot name="toolbar" />
		<slot name="nodes" />
		<svg :width="width" :height="height"></svg>
	</main>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import * as d3 from 'd3';

let width = 0,
	height = 0;
let x: d3.ScaleLinear<number, number, never>, y: d3.ScaleLinear<number, number, never>;
let xAxis: d3.Axis<d3.NumberValue>, yAxis: d3.Axis<d3.NumberValue>;
let gX: d3.Selection<SVGGElement, unknown, HTMLElement, any>,
	gY: d3.Selection<SVGGElement, unknown, HTMLElement, any>;
let currentTransform: d3.ZoomTransform;

const canvasRef = ref<HTMLElement>();
// const debugMode = ref(true);

const handleZoom = (evt: any, container: d3.Selection<SVGGElement, unknown, HTMLElement, any>) => {
	container.attr('transform', evt.transform).style('transform-origin', '0 0');

	gX.call(xAxis.scale(evt.transform.rescaleX(x)));
	gY.call(yAxis.scale(evt.transform.rescaleY(y)));

	currentTransform = evt.transform;
};

function updateDimensions() {
	// Update dimensions
	width = canvasRef.value?.clientWidth ?? window.innerWidth;
	height = canvasRef.value?.clientHeight ?? window.innerHeight;
	// Update debug values
	x = d3
		.scaleLinear()
		.domain([-1, width + 1])
		.range([-1, width + 1]);
	y = d3
		.scaleLinear()
		.domain([-1, height + 1])
		.range([-1, height + 1]);
	xAxis = d3
		.axisBottom(x)
		.ticks(((width + 2) / (height + 2)) * 10)
		.tickSize(height)
		.tickPadding(8 - height);
	yAxis = d3
		.axisRight(y)
		.ticks(10)
		.tickSize(width)
		.tickPadding(8 - width);

	if (currentTransform) {
		gX.call(xAxis.scale(currentTransform.rescaleX(x)));
		gY.call(yAxis.scale(currentTransform.rescaleY(y)));
	}
}

onMounted(() => {
	const svg = d3.select('svg');

	const container = svg.append('g').classed('container', true);

	const zoom = d3
		.zoom()
		.scaleExtent([0.2, 20])
		.on('zoom', (e) => handleZoom(e, container));

	svg.call(zoom as any).on('dblclick.zoom', null);

	container.append('circle').attr('cx', 400).attr('cy', 300).attr('r', 20).attr('fill', 'red');

	updateDimensions();
	window.addEventListener('resize', () => updateDimensions());

	gX = svg.append('g').attr('class', 'axis axis--x').call(xAxis);
	gY = svg.append('g').attr('class', 'axis axis--y').call(yAxis);
});
</script>

<style scoped>
#canvas {
	width: 100%;
	height: 100%;
}

svg {
	border: 1px solid blue;
	cursor: grab;
	width: 100%;
	height: 100%;
}

svg:active {
	cursor: grabbing;
}
</style>
