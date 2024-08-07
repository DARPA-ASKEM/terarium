<!--
By default the entire area of tera-canvas-item works as a drag handle.
To specify a drag handle area add the "drag-handle" class within the component that would be placed in this slot.
-->

<template>
	<section ref="canvasItem" :style="style">
		<slot />
	</section>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';

defineProps<{
	style: { width: string; top: string; left: string };
}>();

const emit = defineEmits(['dragging']);

const canvasItem = ref();

let isDragging = false;
let tempX = 0;
let tempY = 0;

const startDrag = (evt: MouseEvent) => {
	tempX = evt.x;
	tempY = evt.y;
	isDragging = true;
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
	tempX = 0;
	tempY = 0;
	isDragging = false;
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
