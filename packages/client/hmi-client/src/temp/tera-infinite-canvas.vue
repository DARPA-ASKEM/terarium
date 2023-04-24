<template>
	<main id="canvas" ref="canvasRef">
		<slot name="toolbar" />
		<slot name="nodes" />
		<svg :width="width" :height="height"></svg>
	</main>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import * as d3 from 'd3';

let width: number, height: number;
let x: d3.ScaleLinear<number, number, never>, y: d3.ScaleLinear<number, number, never>;
let xAxis: d3.Axis<d3.NumberValue>, yAxis: d3.Axis<d3.NumberValue>;
let gX: d3.Selection<SVGGElement, unknown, HTMLElement, any>,
	gY: d3.Selection<SVGGElement, unknown, HTMLElement, any>;

const canvasRef = ref<HTMLElement>();

// const zoomObj: { x: number; y: number; k: number } = { x: 0, y: 0, k: 1 };
// const debugMode = ref(true);

const handleZoom = (evt: any, container) => {
	container.attr('transform', evt.transform).style('transform-origin', '0 0');

	gX.call(xAxis.scale(evt.transform.rescaleX(x)));
	gY.call(yAxis.scale(evt.transform.rescaleY(y)));
};

function updateDimensions() {
	width = window.innerWidth;
	height = window.innerHeight;

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

	console.log(1);
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

	console.log(0);

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
