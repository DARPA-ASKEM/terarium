<template>
	<section class="container" :style="{ height: containerHeight + 'px' }">
		<main class="content">
			<slot />
		</main>
		<div class="resize-handle" @mousedown="startResize"></div>
	</section>
</template>

<script setup lang="ts">
/*

TeraResizablePanel: A Vertically resizable panel with a handle along the bottom
*/

import { ref, onUnmounted } from 'vue';

// const minHeight = ref(300);
const containerHeight = ref(400);
const draggedY = ref(0);

const resize = (event: MouseEvent) => {
	console.log('resizing');
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

section {
	min-height: 300px;
}

main {
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
