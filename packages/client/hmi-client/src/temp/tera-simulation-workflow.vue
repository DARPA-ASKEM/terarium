<template>
	<infinite-canvas debug-mode @save-transform="saveTransform">
		<template #foreground></template>
		<template #data>
			<div style="font-size: 24px; padding: 10px; background: #9ef">This is a DataLayer DIV</div>
		</template>
	</infinite-canvas>
</template>

<script setup lang="ts">
import InfiniteCanvas from '@/temp/tera-infinite-canvas.vue';

function saveTransform(transform: d3.ZoomTransform) {
	console.log(transform);
}
</script>
