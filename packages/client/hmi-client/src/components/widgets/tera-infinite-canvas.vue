<template>
	<main id="canvas">
		<slot name="toolbar" />
		<slot name="nodes" />
		<slot name="background" />
		<svg class="parent"></svg>
	</main>
</template>

<script setup lang="ts"></script>

<style scoped>
#canvas {
	position: relative;
	border: 1px solid #000;
	width: 100%;
	height: 100%;
}

.parent {
	width: 100%;
	height: 100%;
	position: absolute;
	user-select: none;
}

svg {
	border: 1px solid blue;
	/* pointer-events: none; */
	width: 100%;
	height: 100%;
	cursor: grab;
}

svg:active {
	cursor: grabbing;
}

svg > * {
	/* pointer-events: all; */
}
</style>
