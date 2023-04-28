<template>
	<section class="container" :style="nodeStyle">
		<header>{{ node.operationType }}</header>
		<section class="inputs">
			<li v-for="(input, index) in node.inputs" :key="index">
				<div class="port"></div>
				{{ input.label }}
			</li>
		</section>
		<slot name="body"></slot>
		<section class="outputs">
			<li v-for="(output, index) in node.outputs" :key="index">
				{{ output.label }}
				<div class="port"></div>
			</li>
		</section>
	</section>
</template>

<script setup lang="ts">
import { WorkflowNode } from '@/types/workflow';
import { ref, onMounted } from 'vue';

const props = defineProps<{
	node: WorkflowNode;
}>();

const nodeStyle = ref({
	minWidth: `${props.node.width}px`,
	minHeight: `${props.node.height}px`,
	top: `${props.node.y}px`,
	left: `${props.node.x}px`
});

onMounted(() => {});
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	justify-content: space-evenly;
	gap: 4px;
}

.container {
	background-color: var(--surface-section);
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	position: absolute;
}

.outputs {
	align-items: end;
}

li {
	list-style: none;
	font-size: var(--font-caption);
	display: flex;
}

.port {
	display: inline-block;
	height: 16px;
	width: 16px;
	border: 1px solid var(--surface-border);
	border-radius: 8px;
	position: relative;
	background: var(--surface-100);
}

.inputs .port {
	left: -8px;
}

.outputs .port {
	left: 8px;
}

header {
	padding: 4px;
	white-space: nowrap;
}
</style>
