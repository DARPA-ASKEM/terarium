<template>
	<main id="canvas">
		<slot name="toolbar" />
		<slot name="nodes" />
		<svg :width="width" :height="height"></svg>
	</main>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import * as d3 from 'd3';

let gX;
let gY;
const canvasRef = ref<HTMLElement>();

const width = computed(() => canvasRef.value?.clientWidth ?? 600);
const height = computed(() => canvasRef.value?.clientHeight ?? 500);

let x = d3
	.scaleLinear()
	.domain([-1, width.value + 1])
	.range([-1, width.value + 1]);

let y = d3
	.scaleLinear()
	.domain([-1, height.value + 1])
	.range([-1, height.value + 1]);

let xAxis = d3
	.axisBottom(x)
	.ticks(((width.value + 2) / (height.value + 2)) * 10)
	.tickSize(height.value)
	.tickPadding(8 - height.value);

let yAxis = d3
	.axisRight(y)
	.ticks(10)
	.tickSize(width.value)
	.tickPadding(8 - width.value);

// const zoomObj: { x: number; y: number; k: number } = { x: 0, y: 0, k: 1 };
// const debugMode = ref(true);

const handleZoom = (evt: any, container) => {
	container.attr('transform', evt.transform).style('transform-origin', '0 0');

	gX.call(xAxis.scale(evt.transform.rescaleX(x)));
	gY.call(yAxis.scale(evt.transform.rescaleY(y)));
};
onMounted(() => {
	const svg = d3.select('svg');

	const container = svg.append('g').classed('container', true);

	const zoom = d3
		.zoom()
		.scaleExtent([1, 5])
		.on('zoom', (e) => handleZoom(e, container));

	svg.call(zoom as any).on('dblclick.zoom', null);

	svg.append('circle').attr('cx', 400).attr('cy', 300).attr('r', 10).attr('fill', 'red');

	gX = svg.append('g').attr('class', 'axis axis--x').call(xAxis);
	gY = svg.append('g').attr('class', 'axis axis--y').call(yAxis);
});

// const itemState: any = ref([
// 	{
// 		x: 100,
// 		y: 100,
// 		h: 200,
// 		w: 200,
// 		active: false,
// 		draggable: true
// 	},
// 	{
// 		x: 400,
// 		y: 100,
// 		h: 200,
// 		w: 200,
// 		active: false,
// 		draggable: true
// 	}
// ]);
</script>

<style scoped>
#canvas {
	position: relative;
	border: 1px solid #000;
	width: 100%;
	height: 100%;
}

/* .parent {
	width: 100%;
	height: 100%;
	position: absolute;
	user-select: none;
} */

svg {
	border: 1px solid blue;
	cursor: grab;
	/* pointer-events: none; */
	/* width: 100%;
	height: 100%; */
}

svg:active {
	cursor: grabbing;
}

svg > * {
	/* pointer-events: all; */
}
</style>
