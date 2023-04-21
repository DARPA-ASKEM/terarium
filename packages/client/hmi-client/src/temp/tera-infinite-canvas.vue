<template>
	<main id="canvas">
		<slot name="toolbar" />
		<slot name="nodes" />
		<svg :width="width" :height="height"></svg>
	</main>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import * as d3 from 'd3';

const width = 800;
const height = 600;

const x = d3
	.scaleLinear()
	.domain([-1, width + 1])
	.range([-1, width + 1]);

const y = d3
	.scaleLinear()
	.domain([-1, height + 1])
	.range([-1, height + 1]);

const xAxis = d3
	.axisBottom(x)
	.ticks(((width + 2) / (height + 2)) * 10)
	.tickSize(height)
	.tickPadding(8 - height);

const yAxis = d3
	.axisRight(y)
	.ticks(10)
	.tickSize(width)
	.tickPadding(8 - width);

const gX = ref();
const gY = ref();

let zoom: d3.ZoomBehavior<Element, unknown> | null = null;
// const zoomObj: { x: number; y: number; k: number } = { x: 0, y: 0, k: 1 };
// const debugMode = ref(true);

const handleZoom = (evt: any) => {
	d3.select('svg')
		.attr('transform', evt.transform)
		// .style(
		// 	'transform',
		// 	`translate(${evt.transform.x}px, ${evt.transform.y}px) scale(${evt.transform.k})`
		// )
		.style('transform-origin', '0 0');

	gX.value.call(xAxis.scale(evt.transform.rescaleX(x)));
	gY.value.call(yAxis.scale(evt.transform.rescaleY(y)));
};

zoom = d3
	.zoom()
	.scaleExtent([1, 5])
	.translateExtent([
		[-100, -100],
		[width + 90, height + 100]
	])
	.on('zoom', handleZoom);

onMounted(() => {
	const svg = d3.select('svg');

	svg.call(zoom as any).on('dblclick.zoom', null);

	svg.append('circle').attr('cx', 400).attr('cy', 300).attr('r', 10).attr('fill', 'red');

	gX.value = svg.append('g').attr('class', 'axis axis--x').call(xAxis);
	gY.value = svg.append('g').attr('class', 'axis axis--y').call(yAxis);
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
