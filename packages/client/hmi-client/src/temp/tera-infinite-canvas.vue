<template>
	<main ref="canvasRef">
		<div>
			<slot name="foreground" />
		</div>
		<div class="data-layer" ref="dataLayerRef">
			<slot name="data" />
		</div>
		<svg class="background-layer" ref="backgroundLayerRef" :width="width" :height="height">
			<slot name="background" />
		</svg>
	</main>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import * as d3 from 'd3';

const props = withDefaults(
	defineProps<{
		debugMode?: boolean;
		scaleExtent?: [number, number];
		lastTransform?: d3.ZoomTransform;
	}>(),
	{
		debugMode: false,
		scaleExtent: () => [0.1, 10],
		lastTransform: undefined
	}
);

const emit = defineEmits(['save-transform']);

let x: d3.ScaleLinear<number, number, never>, y: d3.ScaleLinear<number, number, never>;
let xAxis: d3.Axis<d3.NumberValue>, yAxis: d3.Axis<d3.NumberValue>;
let gX: d3.Selection<SVGGElement, any, null, any>, gY: d3.Selection<SVGGElement, any, null, any>;

let currentTransform = props.lastTransform;

const width = ref(0);
const height = ref(0);
const canvasRef = ref<HTMLElement>();
const dataLayerRef = ref<HTMLDivElement>();
const backgroundLayerRef = ref<SVGElement>();

const handleZoom = (evt: any, container: d3.Selection<SVGGElement, any, null, any>) => {
	container.attr('transform', evt.transform);

	d3.select(dataLayerRef.value as HTMLDivElement)
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
	const svg = d3.select(backgroundLayerRef.value as SVGGElement);

	const container = svg.append('g');

	const zoom = d3
		.zoom()
		.scaleExtent(props.scaleExtent)
		.on('zoom', (e) => handleZoom(e, container));

	svg.call(zoom as any).on('dblclick.zoom', null);
	svg.transition().call(zoom.transform as any, currentTransform);

	container.append('circle').attr('cx', 400).attr('cy', 300).attr('r', 20).attr('fill', 'red');

	updateDimensions();
	if (canvasRef.value) resizeObserver.observe(canvasRef.value);

	if (props.debugMode) {
		gX = svg.append('g').attr('class', 'axis axis--x').call(xAxis);
		gY = svg.append('g').attr('class', 'axis axis--y').call(yAxis);
	}
});

onUnmounted(() => {
	emit('save-transform', currentTransform);
});
</script>

<style scoped>
main {
	width: 100%;
	height: 100%;
}

.data-layer {
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
