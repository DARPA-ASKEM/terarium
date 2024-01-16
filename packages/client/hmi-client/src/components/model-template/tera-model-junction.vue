<template>
	<section>
		<i class="pi pi-info-circle" />
	</section>
</template>

<script setup lang="ts">
import { Position } from '@/types/workflow'; // temp

interface ModelTemplateJunction {
	id: number;
	x: number;
	y: number;
	edges: {
		target: number;
		points: Position[];
	}[];
}

defineProps<{
	junction: ModelTemplateJunction;
}>();
</script>

<style scoped>
section {
	background-color: var(--text-color-subdued);
	color: var(--gray-0);
	width: 1.5rem;
	height: 1.5rem;
	border-radius: var(--border-radius);
	display: flex;
	justify-content: center;
	align-items: center;
}
</style>
