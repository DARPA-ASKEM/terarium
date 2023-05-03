<template>
	<section class="container" :style="nodeStyle" ref="workflowNode">
		<header>
			<h5>{{ node.operationType }}</h5>
		</header>
		<section class="inputs">
			<li v-for="(input, index) in node.inputs" :key="index" ref="inputs">
				<div
					class="input port"
					@click.stop="selectPort(input)"
					@mouseover="(event) => mouseoverPort(event)"
					@focus="() => {}"
				></div>
				{{ input.label }}
			</li>
		</section>
		<slot name="body" />
		<section class="outputs">
			<li v-for="(output, index) in node.outputs" :key="index" ref="outputs">
				{{ output.label }}
				<div
					class="output port"
					@click.stop="selectPort(output)"
					@mouseover="(event) => mouseoverPort(event)"
					@focus="() => {}"
				></div>
			</li>
		</section>
	</section>
</template>

<script setup lang="ts">
import { Position, WorkflowNode, WorkflowPort } from '@/types/workflow';
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';

const props = defineProps<{
	node: WorkflowNode;
}>();

const emit = defineEmits(['dragging', 'port-selected', 'port-mouseover']);

const inputs = ref<HTMLElement>();
const outputs = ref<HTMLElement>();

const nodeStyle = computed(() => ({
	minWidth: `${props.node.width}px`,
	minHeight: `${props.node.height}px`,
	top: `${props.node.y}px`,
	left: `${props.node.x}px`
}));

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
	document.addEventListener('mousemove', drag);
	workflowNode.value.addEventListener('mouseup', stopDrag);
});

function selectPort(port: WorkflowPort) {
	emit('port-selected', port);
}

function mouseoverPort(event) {
	const el = event.target as HTMLElement;
	const nodePosition: Position = { x: props.node.x, y: props.node.y };
	const isInput = el.className === 'input port';
	console.log(isInput);
	const totalOffsetX = el.offsetLeft + (isInput ? 0 : el.offsetWidth);
	const totalOffsetY = el.offsetTop + el.offsetHeight / 2 + 1;
	const portPosition = { x: nodePosition.x + totalOffsetX, y: nodePosition.y + totalOffsetY };
	emit('port-mouseover', portPosition);
}
onBeforeUnmount(() => {
	if (workflowNode.value) {
		workflowNode.value.removeEventListener('mousedown', startDrag);
		document.removeEventListener('mousemove', drag);
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
	width: 8px;
	border: 1px solid var(--surface-border);

	position: relative;
	background: var(--surface-100);
}

.port:hover {
	background: var(--surface-border);
}

.inputs .port {
	left: -8px;
	border-radius: 0 8px 8px 0;
}

.outputs .port {
	left: 8px;
	border-radius: 8px 0 0 8px;
}

header {
	padding: 4px;
	white-space: nowrap;
}
</style>
