<template>
	<div class="placeholder-container">
		<img :src="placeholderGraphic" alt="Plant icon" />
		<slot />
	</div>
</template>

<script setup lang="ts">
import { WorkflowNode } from '@/types/workflow';
import plants from '@/assets/svg/plants.svg';

const props = defineProps<{
	node: WorkflowNode<any>;
}>();

const placeholderGraphic = props.node.imageUrl ?? plants;
</script>

<style scoped>
.placeholder-container {
	display: flex;
	flex-direction: column;
	justify-content: center;
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	text-align: center;
	gap: 0.5rem;
	padding: 0.5rem;
}

img {
	height: 5.5rem;
	max-height: 8rem;
	margin: 0 auto;
	pointer-events: none;
}
</style>
