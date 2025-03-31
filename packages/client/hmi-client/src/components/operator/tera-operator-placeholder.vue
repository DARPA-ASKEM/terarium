<template>
	<aside>
		<img :src="placeholderGraphic" alt="Plant icon" />
		<slot />
	</aside>
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
aside {
	color: var(--text-color-subdued);
	display: flex;
	flex-direction: column;
	gap: var(--gap-6);
	justify-content: center;
	margin-top: var(--gap-3);
	margin-bottom: var(--gap-3);
	text-align: center;
	font-size: var(--font-caption);
}

img {
	height: 5.5rem;
	margin: 0 auto;
	max-height: 8rem;
	pointer-events: none;
}
</style>
