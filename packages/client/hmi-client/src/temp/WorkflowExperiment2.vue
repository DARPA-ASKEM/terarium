<template>
	<div id="app">
		<div class="parent">
			<Vue3DraggableResizable
				:initW="110"
				:initH="120"
				v-model:x="itemState.x"
				v-model:y="itemState.y"
				v-model:w="itemState.w"
				v-model:h="itemState.h"
				v-model:active="itemState.active"
				:draggable="true"
				:resizable="true"
				@activated="print('activated')"
				@deactivated="print('deactivated')"
				@dragging="test"
				@drag-start="test"
				@drag-end="test"
				@resize-start="print('resize-start')"
				@resizing="print('resizing')"
				@resize-end="print('resize-end')"
			>
				<div class="test">
					This is a test example
					<ModelPicker />
				</div>
			</Vue3DraggableResizable>
		</div>
		<svg class="parent">
			<circle cx="200" cy="200" r="80" fill="red" />
			<circle cx="260" cy="200" r="60" fill="red" />
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
// let zoomTransformObject: d3.ZoomTransform | null = null;

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
		if (x.target.tagName === 'DIV' && x.type === 'mousedown') {
			return false;
		}
		return true;
	})
	.scaleExtent([0.2, 10])
	.on('zoom', zoomed)
	.on('end', zoomEnd);

onMounted(() => {
	d3.select('#app')
		.call(zoom as any)
		.on('dblclick.zoom', null);
});

const itemState: any = ref({
	x: 100,
	y: 100,
	h: 100,
	w: 100,
	active: false
});

const print = (v: any) => {};

const test = (v: any) => {
	console.log('!!!', v);
};

// let scale = 100;
// const testTransform = () => {
// 	scale -= 20;
// 	if (scale <= 20) scale = 100;
// 	const k = scale / 100;
// 	d3.selectAll('.parent').style('transform', `scale(${k})`);
// }
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
	background: #fff;
}

#app {
	border: 1px solid #000;
	width: 100%;
	height: 100%;
}

svg {
	pointer-events: none;
}

svg > * {
	pointer-events: all;
}
</style>
