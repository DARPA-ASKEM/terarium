<template>
	<infinite-canvas debug-mode :last-transform="lastTransform" @save-transform="saveTransform">
		<template #foreground></template>
		<template #data>
			<div style="font-size: 24px; padding: 10px; background: #9ef">This is a DataLayer DIV</div>
		</template>
	</infinite-canvas>
</template>

<script setup lang="ts">
import * as d3 from 'd3';
import InfiniteCanvas from '@/temp/tera-infinite-canvas.vue';

let lastTransform = undefined;

// {
// 	k: 0.7120250977985356,
// 	x: 246.1420085893451,
// 	y: 199.416543275727
// }

function saveTransform(transform: d3.ZoomTransform) {
	console.log(transform);
}
</script>
