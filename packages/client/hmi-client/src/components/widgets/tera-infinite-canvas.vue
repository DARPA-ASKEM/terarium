<template>
	<main ref="canvasRef">
		<svg
			class="canvas-layer background-layer"
			ref="backgroundLayerRef"
			:width="width"
			:height="height"
		>
			<slot name="background">
				<g ref="edgesRef" class="edges" stroke-width="1" fill="none">
					<path v-if="newEdge?.points" :d="drawNewEdge()" stroke="green" />
					<path v-for="(edge, index) in edges" :d="drawEdge(edge)" stroke="black" :key="index" />
				</g>
			</slot>
		</svg>
		<div class="canvas-layer data-layer" ref="dataLayerRef">
			<slot name="data" />
		</div>
		<div class="canvas-layer">
			<slot name="foreground" />
		</div>
	</main>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import * as d3 from 'd3';
import { WorkflowEdge } from '@/types/workflow';

const props = withDefaults(
	defineProps<{
		debugMode?: boolean;
		scaleExtent?: [number, number];
		lastTransform?: { k: number; x: number; y: number };
		newEdge?: WorkflowEdge;
		edges: WorkflowEdge[];
	}>(),
	{
		debugMode: false,
		scaleExtent: () => [0.1, 10],
		lastTransform: undefined,
		newEdge: undefined,
		edges: () => []
	}
);
//
const emit = defineEmits(['save-transform']);

let x: d3.ScaleLinear<number, number, never>;
let y: d3.ScaleLinear<number, number, never>;
let xAxis: d3.Axis<d3.NumberValue>;
let yAxis: d3.Axis<d3.NumberValue>;
let gX: d3.Selection<SVGGElement, any, null, any>;
let gY: d3.Selection<SVGGElement, any, null, any>;
let currentTransform: d3.ZoomTransform;

const width = ref(0);
const height = ref(0);
const canvasRef = ref<HTMLElement>();
const dataLayerRef = ref<HTMLDivElement>();
const backgroundLayerRef = ref<SVGElement>();
const edgesRef = ref<SVGElement>();

const newEdge = computed(() => props.newEdge);

function handleZoom(
	e: any,
	container: d3.Selection<SVGGElement, any, null, any>,
	edges: d3.Selection<SVGGElement, any, null, any>
) {
	container.attr('transform', e.transform);
	edges.attr('transform', e.transform);

	d3.select(dataLayerRef.value as HTMLDivElement)
		.style('transform', `translate(${e.transform.x}px, ${e.transform.y}px) scale(${e.transform.k})`)
		.style('transform-origin', '0 0');

	if (props.debugMode) {
		gX.call(xAxis.scale(e.transform.rescaleX(x)));
		gY.call(yAxis.scale(e.transform.rescaleY(y)));
	}

	currentTransform = e.transform;
}

function handleZoomEnd() {
	emit('save-transform', { x: currentTransform.x, y: currentTransform.y, k: currentTransform.k });
}

function updateDimensions() {
	// Update dimensions
	width.value = canvasRef.value?.clientWidth ?? window.innerWidth;
	height.value = canvasRef.value?.clientHeight ?? window.innerHeight;

	if (props.debugMode) {
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
}

const resizeObserver = new ResizeObserver(() => updateDimensions());

onMounted(() => {
	const svg = d3.select(backgroundLayerRef.value as SVGGElement); // Parent SVG
	const container = svg.append('g'); // Pan/zoom area
	const edges = d3.select(edgesRef.value as SVGGElement);

	// Zoom config is applied and event handler
	const zoom = d3
		.zoom()
		.filter((evt: any) => {
			const classStr = d3.select(evt.target).attr('class');
			if (classStr.includes('canvas-layer') || evt.type === 'wheel') {
				return true;
			}
			return false;
		})
		.scaleExtent(props.scaleExtent)
		.on('zoom', (e) => {
			handleZoom(e, container, edges);
		})
		.on('end', handleZoomEnd);
	svg.call(zoom as any).on('dblclick.zoom', null);

	// Initializes/watches resize of component so SVG layer fills it
	updateDimensions();
	if (canvasRef.value) resizeObserver.observe(canvasRef.value);

	// Assign debug grid
	if (props.debugMode) {
		gX = svg.append('g').attr('class', 'axis axis--x').call(xAxis);
		gY = svg.append('g').attr('class', 'axis axis--y').call(yAxis);
	}

	// Initialize starting position
	if (props.lastTransform) {
		zoom.scaleTo(svg as any, props.lastTransform.k);
		zoom.translateTo(svg as any, props.lastTransform.x, props.lastTransform.y);
	} else {
		// Default position - triggers handleZoom which in turn sets currentTransform
		svg.transition().call(zoom.transform as any, d3.zoomIdentity);
	}
});

function drawNewEdge(): string {
	// if (newEdge.value?.points && newEdge.value?.points.length === 2) {
	// 	const sourcePoint = newEdge.value.points[0];
	// 	const targetPoint = newEdge.value.points[1];
	// 	const path = d3.path();
	// 	path.moveTo(sourcePoint.x, sourcePoint.y);
	// 	path.lineTo(targetPoint.x, targetPoint.y);
	// 	path.closePath();
	// 	return path.toString();
	// }
	// return 'M0,0';
	if (newEdge.value) {
		return drawEdge(newEdge.value);
	}
	return 'M0,0';
}

function drawEdge(edge: WorkflowEdge): string {
	if (edge.points && edge.points.length === 2) {
		const sourcePoint = edge.points[0];
		const targetPoint = edge.points[1];
		const path = d3.path();
		path.moveTo(sourcePoint.x, sourcePoint.y);
		path.lineTo(targetPoint.x, targetPoint.y);
		path.closePath();
		return path.toString();
	}
	return 'M0,0';
}
</script>

<style scoped>
main {
	width: 100%;
	height: 100%;
}

main > * {
	position: absolute;
}

.background-layer {
	cursor: grab;
	width: 100%;
	height: 100%;
}

.background-layer:deep(.tick line) {
	color: var(--surface-border);
}

.background-layer:deep(.tick text) {
	color: var(--surface-border);
}

svg:active {
	cursor: grabbing;
}
</style>
