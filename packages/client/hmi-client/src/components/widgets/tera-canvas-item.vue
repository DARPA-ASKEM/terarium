<!--
Notes for how to use this component:
1. Give this component a style attribute with the following properties:
	- top: the top position of the component
	- left: the left position of the component
	- width: the width of the component
2. By default the entire area of tera-canvas-item works as a drag handle.
To specify a drag handle area add the "drag-handle" class within the component that would be placed in this slot.
-->

<template>
	<section ref="canvasItem">
		<slot />
	</section>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';

const emit = defineEmits(['dragging', 'dragstart', 'dragend']);

const canvasItem = ref();

let isDragging = false;
let tempX = 0;
let tempY = 0;

const startDrag = (evt: MouseEvent) => {
	tempX = evt.x;
	tempY = evt.y;
	isDragging = true;
	emit('dragstart');
};

const drag = (evt: MouseEvent) => {
	if (!isDragging) return;

	const dx = evt.x - tempX;
	const dy = evt.y - tempY;

	emit('dragging', { x: dx, y: dy });

	tempX = evt.x;
	tempY = evt.y;
};

const stopDrag = (/* evt: MouseEvent */) => {
	if (!isDragging) return;
	tempX = 0;
	tempY = 0;
	isDragging = false;
	emit('dragend');
};

onMounted(() => {
	if (canvasItem.value) {
		const dragHandle = canvasItem.value.querySelector('.card') ?? canvasItem.value;

		dragHandle.addEventListener('mousedown', startDrag);
		document.addEventListener('mousemove', drag);
		document.addEventListener('click', stopDrag);
		dragHandle.addEventListener('mouseup', stopDrag);
	}
});

onBeforeUnmount(() => {
	if (canvasItem.value) {
		const dragHandle = canvasItem.value.querySelector('.card') ?? canvasItem.value;

		dragHandle.removeEventListener('mousedown', startDrag);
		document.removeEventListener('mousemove', drag);
		document.removeEventListener('click', stopDrag);
		dragHandle.removeEventListener('mouseup', stopDrag);
	}
});
</script>

<style scoped>
section {
	position: absolute;
	user-select: none;
}
</style>
