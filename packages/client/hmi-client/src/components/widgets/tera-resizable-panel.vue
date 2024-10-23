<template>
	<section class="container" :style="{ height: containerHeight + 'px' }">
		<main class="content">
			<slot />
		</main>
		<div v-if="resizeFromTop === true" class="resize-handle-top" @mousedown="startResize"></div>
		<div v-else class="resize-handle-bottom" @mousedown="startResize"></div>
	</section>
</template>

<script setup lang="ts">
/*

TeraResizablePanel: A Vertically resizable panel with a handle along the bottom
*/

import { ref, onUnmounted, onMounted } from 'vue';

const containerHeight = ref(320);
const draggedY = ref(0);

const resize = (event: MouseEvent) => {
	if (props?.resizeFromTop) {
		containerHeight.value += -event.clientY + draggedY.value;
	} else {
		containerHeight.value += event.clientY - draggedY.value;
	}
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
const props = defineProps<{
	startHeight?: number;
	resizeFromTop?: boolean;
}>();

onMounted(() => {
	if (props.startHeight !== undefined) {
		containerHeight.value = props.startHeight;
	}
});

onUnmounted(() => {
	window.removeEventListener('mousemove', resize);
	window.removeEventListener('mouseup', stopResize);
});
</script>

<style scoped>
.container {
	position: relative;
	width: 100%;
	border: none;
	overflow: hidden;
	height: 100%;
	isolation: isolate;
}

section {
	min-height: 55px;
}

main {
	height: 100%;
}

.content {
	height: 100%;
}

.resize-handle-bottom {
	position: absolute;
	bottom: 0;
	left: 0;
	width: 100%;
	height: 6px;
	cursor: ns-resize;
	background: var(--surface-border-light);
	z-index: 1;
	border-radius: 0 0 var(--border-radius-big) var(--border-radius-big);
	mix-blend-mode: darken;
}

.resize-handle-top {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 6px;
	cursor: ns-resize;
	background: var(--surface-border-light);
	z-index: 1;
	border-radius: 0 0 var(--border-radius-big) var(--border-radius-big);
	mix-blend-mode: darken;
}

.resize-handle:hover {
	background: var(--primary-color-light);
}
</style>
