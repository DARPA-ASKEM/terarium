<template>
	<section class="container" :style="nodeStyle" ref="workflowNode">
		<header>
			<h5>{{ node.operationType }}</h5>
		</header>
		<section class="inputs">
			<li v-for="(input, index) in node.inputs" :key="index">
				<div class="port"></div>
				{{ input.label }}
			</li>
		</section>
		<slot name="body" />
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
import { ref, onMounted, onBeforeUnmount } from 'vue';

const emit = defineEmits(['dragging']);

const props = defineProps<{
	node: WorkflowNode;
}>();

const nodeStyle = ref({
	minWidth: `${props.node.width}px`,
	minHeight: `${props.node.height}px`,
	top: `${props.node.y}px`,
	left: `${props.node.x}px`
});

const workflowNode = ref<HTMLElement>();

let tempX = 0;
let tempY = 0;
let dragStart = false;

const startDrag = (evt: MouseEvent) => {
	console.log('start', evt.x, evt.y);
	tempX = evt.x;
	tempY = evt.y;
	dragStart = true;
};

const drag = (evt: MouseEvent) => {
	if (dragStart === false) return;

	const dx = evt.x - tempX;
	const dy = evt.y - tempY;

	console.log('move', dx, dy);
	emit('dragging', { x: dx, y: dy });

	tempX = evt.x;
	tempY = evt.y;
};

const stopDrag = (evt: MouseEvent) => {
	console.log('end', evt.x, evt.y);
	tempX = 0;
	tempY = 0;
	dragStart = false;
};

onMounted(() => {
	if (!workflowNode.value) return;

	workflowNode.value.addEventListener('mousedown', startDrag);
	workflowNode.value.addEventListener('mousemove', drag);
	workflowNode.value.addEventListener('mouseup', stopDrag);
});

onBeforeUnmount(() => {
	if (workflowNode.value) {
		workflowNode.value.removeEventListener('mousedown', startDrag);
		workflowNode.value.removeEventListener('mousemove', drag);
		workflowNode.value.removeEventListener('mouseup', stopDrag);
	}
});
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
	padding: 0.5rem;
	user-select: none;
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
