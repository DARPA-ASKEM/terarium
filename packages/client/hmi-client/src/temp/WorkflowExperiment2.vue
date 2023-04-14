<template>
	<div id="app">
		<div class="parent" id="data">
			<Vue3DraggableResizable
				v-for="(item, key) of itemState"
				:key="key"
				v-model:x="item.x"
				v-model:y="item.y"
				v-model:w="item.w"
				v-model:h="item.h"
				v-model:active="item.active"
				:draggable="item.draggable"
				:resizable="true"
				@activated="print('activated')"
				@deactivated="print('deactivated')"
				@dragging="print('dragging')"
				@drag-start="print('drag-start')"
				@drag-end="print('drag-end')"
				@resize-start="print('resize-start')"
				@resizing="print('resizing')"
				@resize-end="print('resize-end')"
			>
				<div class="test">
					<h4>Model picker</h4>
					<ModelPicker />
					<div
						draggable="true"
						class="handle-r"
						@drag.stop="dragHandler($event, item)"
						@dragstart.stop="dragstartHandler($event, item)"
						@dragend.stop="dragendHandler($even, item)"
						@mousedown="mousedown(item)"
						@mouseup="mouseup(item)"
					/>
					<div
						draggable="true"
						class="handle-l"
						@drag.stop="dragHandler($event, item)"
						@dragstart.stop="dragstartHandler($event, itemt)"
						@dragend.stop="dragendHandler($event, item)"
						@mousedown="mousedown(item)"
						@mouseup="mouseup(item)"
					/>
				</div>
			</Vue3DraggableResizable>
		</div>
		<svg class="parent">
			<!--
			<circle cx="200" cy="200" r="80" fill="red" />
			<circle cx="260" cy="200" r="60" fill="red" />
			-->
		</svg>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import * as d3 from 'd3';
import Vue3DraggableResizable from 'vue3-draggable-resizable';
import 'vue3-draggable-resizable/dist/Vue3DraggableResizable.css';
import ModelPicker from './workflow-exp2/ModelPicker.vue';

let zoom: d3.ZoomBehavior<Element, unknown> | null = null;
const zoomObj: { x: number; y: number; k: number } = { x: 0, y: 0, k: 1 };

const zoomed = (evt: any) => {
	d3.selectAll('.parent')
		.style(
			'transform',
			`translate(${evt.transform.x}px, ${evt.transform.y}px) scale(${evt.transform.k})`
		)
		.style('transform-origin', '0 0');
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
	.scaleExtent([0.2, 10])
	.on('zoom', zoomed)
	.on('end', zoomEnd);

onMounted(() => {
	d3.select('#app')
		.call(zoom as any)
		.on('dblclick.zoom', null);
	d3.select('svg')
		.append('circle')
		.attr('cx', 613)
		.attr('cy', 250)
		.attr('r', 5)
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

const print = (v: any) => {
	/* console.log(v) */
};

interface Point {
	x: number;
	y: number;
}

const startPoint: Point = { x: 0, y: 0 };

const mousedown = (item: any) => {
	console.log('[down]', item);
	item.draggable = false;
};

const mouseup = (item: any) => {
	console.log('[up]', item);
	item.draggable = true;
};

const dragstartHandler = (event: any, item: any) => {
	console.log('[dragstart]', event, item);
	console.log('[dragstart]', item.y, event.x, event.y);
	startPoint.x = item.x;
	startPoint.y = item.y;
};

const dragendHandler = (event: any, item: any) => {
	console.log('[dragsend]');
	const svg = d3.select('svg');
	svg.selectAll('.temp').remove();
	item.draggable = true;
};

const dragHandler = (event: any, item: any) => {
	const svg = d3.select('svg');
	svg.selectAll('.temp').remove();
	const p: [number, number] = d3.pointer(event, svg.node());

	svg
		.append('line')
		.classed('temp', true)
		.attr('x1', startPoint.x)
		.attr('y1', startPoint.y)
		.attr('x2', p[0])
		.attr('y2', p[1])
		.attr('stroke-width', 4)
		.attr('stroke', '#f80');
};
</script>

<style scoped>
.parent {
	width: 100%;
	height: 100%;
	position: absolute;
	user-select: none;
}

.test {
	height: 100%;
	width: 100%;
	background: #fff;
	border: 1px solid #bbb;
	box-shadow: 5px 2px 2px #ddd;
	border-radius: 5px;
	padding: 5px;
}

#app {
	position: relative;
	border: 1px solid #000;
	width: 100%;
	height: 100%;
}

.handle-r {
	position: absolute;
	left: 100%;
	border-radius: 50%;
	width: 25px;
	height: 25px;
	background: #fff;
	border: 2px solid #ddd;
}

.handle-l {
	position: absolute;
	left: -25px;
	border-radius: 50%;
	width: 25px;
	height: 25px;
	background: #fff;
	border: 2px solid #ddd;
}

svg {
	pointer-events: none;
}

svg > * {
	pointer-events: all;
}
</style>
