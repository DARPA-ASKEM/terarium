<template>
	<main ref="canvasRef" @contextmenu="toggleContextMenu">
		<ContextMenu ref="contextMenu" :model="contextMenuItems" />
		<div>
			<slot name="foreground" />
		</div>
		<div class="data-layer" ref="dataLayerRef">
			<!-- <tera-workflow-node /> -->
			<slot name="data" />
		</div>
		<svg ref="backgroundLayerRef" :width="width" :height="height"></svg>
	</main>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import * as d3 from 'd3';
import TeraWorkflowNode from './tera-workflow-node.vue';
import { Node } from '@/types/workflow';
import ContextMenu from 'primevue/contextmenu';

const nodes = ref<Node[]>([]);
const contextMenu = ref();
const contextMenuItems = ref([{ label: 'New operation' }]);
function toggleContextMenu(event) {
	contextMenu.value.show(event);
}

let x: d3.ScaleLinear<number, number, never>, y: d3.ScaleLinear<number, number, never>;
let xAxis: d3.Axis<d3.NumberValue>, yAxis: d3.Axis<d3.NumberValue>;
let gX: d3.Selection<SVGGElement, unknown, HTMLElement, any>,
	gY: d3.Selection<SVGGElement, unknown, HTMLElement, any>;
let currentTransform: d3.ZoomTransform;

const width = ref(0);
const height = ref(0);
const canvasRef = ref<HTMLElement>();
const dataLayerRef = ref();
const backgroundLayerRef = ref();

const handleZoom = (evt: any, container: d3.Selection<SVGGElement, unknown, HTMLElement, any>) => {
	container.attr('transform', evt.transform);

	d3.select(dataLayerRef.value)
		.style(
			'transform',
			`translate(${evt.transform.x}px, ${evt.transform.y}px) scale(${evt.transform.k})`
		)
		.style('transform-origin', '0 0');

	gX.call(xAxis.scale(evt.transform.rescaleX(x)));
	gY.call(yAxis.scale(evt.transform.rescaleY(y)));

	currentTransform = evt.transform;
};

function updateDimensions() {
	// Update dimensions
	width.value = canvasRef.value?.clientWidth ?? window.innerWidth;
	height.value = canvasRef.value?.clientHeight ?? window.innerHeight;
	console.log(0);
	// Update debug values
	x = d3
		.scaleLinear()
		.domain([-1, width.value + 1])
		.range([-1, width.value + 1]);
	y = d3
		.scaleLinear()
		.domain([-1, height.value + 1])
		.range([-1, height.value + 1]);
	xAxis = d3
		.axisBottom(x)
		.ticks(((width.value + 2) / (height.value + 2)) * 10)
		.tickSize(height.value)
		.tickPadding(8 - height.value);
	yAxis = d3
		.axisRight(y)
		.ticks(10)
		.tickSize(width.value)
		.tickPadding(8 - width.value);

	if (currentTransform) {
		gX.call(xAxis.scale(currentTransform.rescaleX(x)));
		gY.call(yAxis.scale(currentTransform.rescaleY(y)));
	}
}

onMounted(() => {
	const svg = d3.select(backgroundLayerRef.value);

	const container = svg.append('g');

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
main {
	width: 100%;
	height: 100%;
}

svg {
	border: 1px solid blue;
	cursor: grab;
	width: 100%;
	height: 100%;
}

.data-layer {
	position: absolute;
}

svg:active {
	cursor: grabbing;
}
</style>
