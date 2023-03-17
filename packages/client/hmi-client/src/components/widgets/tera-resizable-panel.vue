<!--
TeraResizablePanel: AVertically resizable panel with a handle along the bottom
-->

<template>
	<div class="container" :style="containerStyle">
		<div class="content">
			<slot></slot>
		</div>
		<div class="resize-handle" @mousedown="startResize"></div>
	</div>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue';

const minHeight = ref(300);
const containerHeight = ref(400);
const draggedY = ref(0);

const containerStyle = computed(() => ({
	height:
		containerHeight.value <= minHeight.value ? `${minHeight.value}px` : `${containerHeight.value}px`
}));

const resize = (event: MouseEvent) => {
	containerHeight.value += event.clientY - draggedY.value;
	draggedY.value = event.clientY;
};

const stopResize = () => {
	document.removeEventListener('mousemove', resize);
	document.removeEventListener('mouseup', stopResize);
};

const startResize = (event: MouseEvent) => {
	draggedY.value = event.clientY;
	document.addEventListener('mousemove', resize);
	document.addEventListener('mouseup', stopResize);
};

onUnmounted(() => {
	window.removeEventListener('mousemove', resize);
	window.removeEventListener('mouseup', stopResize);
});
</script>

<style scoped>
.container {
	position: relative;
	width: 100%;
	border: 1px solid #ccc;
	overflow: hidden;
	height: 100%;
}

.content {
	height: 100%;
}

.resize-handle {
	position: absolute;
	bottom: 0;
	left: 0;
	width: 100%;
	height: 5px;
	cursor: ns-resize;
	background: linear-gradient(to bottom, white, var(--primary-color));
	z-index: 1;
}
</style>
