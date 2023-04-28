<template>
	<infinite-canvas debug-mode :last-transform="lastTransform" @save-transform="saveTransform">
		<template #foreground></template>
		<template #data>
			<div style="font-size: 24px; padding: 10px; background: #9ef">This is a DataLayer DIV</div>
			<tera-model-node />
		</template>
	</infinite-canvas>
</template>

<script setup lang="ts">
import InfiniteCanvas from '@/components/widgets/tera-infinite-canvas.vue';
import TeraModelNode from '@/components/workflow/tera-model-node.vue';

const lastTransform = {
	k: 0.75,
	x: 0,
	y: 0
};

function saveTransform(transform: d3.ZoomTransform) {
	console.log(transform);
}
</script>
