<template>
	<infinite-canvas>
		<template #toolbar></template>
		<template #nodes></template>
		<template #edges></template>
	</infinite-canvas>
</template>

<script setup lang="ts">
import InfiniteCanvas from '@/components/widgets/tera-infinite-canvas.vue';

import { ref, onMounted } from 'vue';
import * as d3 from 'd3';

let zoom: d3.ZoomBehavior<Element, unknown> | null = null;
const zoomObj: { x: number; y: number; k: number } = { x: 0, y: 0, k: 1 };

const debugMode = ref(true);

// Debugging grids
// const x = d3.scaleLinear().domain([-1, width + 1]).range([-1, width + 1]);
// const y = d3.scaleLinear().domain([-1, height + 1]).range([-1, height + 1]);

// const xAxis = d3.axisBottom(x)
//     .ticks(((width + 2) / (height + 2)) * 10)
//     .tickSize(height)
//     .tickPadding(8 - height)

// const yAxis = d3.axisRight(y)
//     .ticks(10)
//     .tickSize(width)
//     .tickPadding(8 - width)

// const gX = this.svg.append("g").attr("class", "axis axis--x");
// const gY = this.svg.append("g").attr("class", "axis axis--y");

const zoomed = (evt: any) => {
	d3.selectAll('.parent')
		.style(
			'transform',
			`translate(${evt.transform.x}px, ${evt.transform.y}px) scale(${evt.transform.k})`
		)
		.style('transform-origin', '0 0');

	// this.surface.attr('transform', transform);
	zoomObj.x = evt.transform.x;
	zoomObj.y = evt.transform.y;
	zoomObj.k = evt.transform.k;

	if (debugMode) {
		//     gX.call(xAxis.scale(transform.rescaleX(x)));
		//     gY.call(yAxis.scale(transform.rescaleY(y)));
		//     this.svg.selectAll('.axis').selectAll('line').style('opacity', 0.1).style('pointer-events', 'none');
		//     this.svg.selectAll('.axis').selectAll('text').style('opacity', 0.5).style('pointer-events', 'none');
	}
};

const zoomEnd = () => {
	// const node = d3.select('.parent').node();
	// zoomTransformObject = d3.zoomTransform(node as Element);
};

zoom = d3
	.zoom()
	.filter((x) => {
		if (x.target.className === 'parent' || x.type === 'wheel') {
			itemState.value.forEach((e) => (e.active = false));
			return true;
		}
		return false;
	})
	.scaleExtent([1, 5])
	.on('zoom', zoomed)
	.on('end', zoomEnd);

onMounted(() => {
	d3.select('#canvas')
		.call(zoom as any)
		.on('dblclick.zoom', null);

	d3.select('svg')
		.append('circle')
		.attr('cx', 613)
		.attr('cy', 250)
		.attr('r', 10)
		.attr('fill', 'red');
});

const itemState: any = ref([
	{
		x: 100,
		y: 100,
		h: 200,
		w: 200,
		active: false,
		draggable: true
	},
	{
		x: 400,
		y: 100,
		h: 200,
		w: 200,
		active: false,
		draggable: true
	}
]);
</script>
