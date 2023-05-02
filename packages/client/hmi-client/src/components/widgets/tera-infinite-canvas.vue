<template>
	<main ref="canvasRef">
		<svg class="background-layer" ref="backgroundLayerRef" :width="width" :height="height">
			<slot name="background">
				<g ref="edgesRef" class="edges" stroke-width="1" fill="none">
					<path v-if="newPath?.start && newPath.end" :d="drawNewEdge()" stroke="green" />
					<path v-for="(path, index) in paths" :d="drawEdge(path)" stroke="black" :key="index" />
				</g>
			</slot>
		</svg>
		<div class="data-layer" ref="dataLayerRef">
			<slot name="data" />
		</div>
		<div>
			<slot name="foreground" />
		</div>
	</main>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import * as d3 from 'd3';
import { Path } from '@/types/workflow';

const props = withDefaults(
	defineProps<{
		debugMode?: boolean;
		scaleExtent?: [number, number];
		lastTransform?: { k: number; x: number; y: number };
		newPath?: Path;
		paths: Path[];
	}>(),
	{
		debugMode: false,
		scaleExtent: () => [0.1, 10],
		lastTransform: undefined,
		newPath: undefined,
		paths: () => []
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

const newPath = computed(() => props.newPath);

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

	container.append('circle').attr('cx', 400).attr('cy', 300).attr('r', 20).attr('fill', 'red');
});

function drawNewEdge(): string {
	if (newPath.value?.start && newPath.value?.end) {
		const path = d3.path();
		path.moveTo(newPath.value.start.x, newPath.value.start.y);
		path.lineTo(newPath.value.end.x, newPath.value.end.y);
		path.closePath();
		return path.toString();
	}
	return `M0,0`;
}

function drawEdge(path: Path): string {
	if (path.start && path.end) {
		const d3Path = d3.path();
		d3Path.moveTo(path.start.x, path.start.y);
		d3Path.lineTo(path.end.x, path.end.y);
		d3Path.closePath();
		return d3Path.toString();
	}
	return `M0,0`;
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

svg:active {
	cursor: grabbing;
}
</style>
