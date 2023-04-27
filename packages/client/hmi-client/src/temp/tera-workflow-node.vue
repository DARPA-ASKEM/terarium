<template>
	<div class="container" :style="nodeStyle"></div>
</template>

<script setup lang="ts">
import { WorkflowNode } from '@/workflow/workflow';
import { ref, onMounted } from 'vue';

const props = defineProps<{
	node: WorkflowNode;
}>();

const nodeStyle = ref({
	width: `${props.node.width}px`,
	height: `${props.node.height}px`,
	top: `${props.node.y}px`,
	left: `${props.node.x}px`
});

onMounted(() => {});
</script>

<style scoped>
.container {
	background-color: var(--surface-section);
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	position: absolute;
}
</style>
