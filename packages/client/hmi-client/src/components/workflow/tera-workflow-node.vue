<template>
	<section class="container" :style="nodeStyle">
		<header>
			<h5>{{ node.operationType }}</h5>
		</header>
		<section class="inputs">
			<li v-for="(input, index) in node.inputs" :key="index" ref="inputs">
				<div class="port" @click.stop="selectPort" @mouseover="mouseoverPort(index, true)"></div>
				{{ input.label }}
			</li>
		</section>
		<slot name="body" />
		<section class="outputs">
			<li v-for="(output, index) in node.outputs" :key="index" ref="outputs">
				{{ output.label }}
				<div class="port" @click.stop="selectPort" @mouseover="mouseoverPort(index, false)"></div>
			</li>
		</section>
	</section>
</template>

<script setup lang="ts">
import { Position, WorkflowNode } from '@/types/workflow';
import { ref } from 'vue';

const props = defineProps<{
	node: WorkflowNode;
}>();

const emit = defineEmits(['port-selected', 'port-mouseover']);

const inputs = ref<HTMLElement>();
const outputs = ref<HTMLElement>();

const nodeStyle = ref({
	minWidth: `${props.node.width}px`,
	minHeight: `${props.node.height}px`,
	top: `${props.node.y}px`,
	left: `${props.node.x}px`
});

function selectPort() {
	emit('port-selected');
}

function mouseoverPort(index: number, isInput: boolean) {
	const ports = isInput ? inputs.value : outputs.value;
	if (ports) {
		const el = ports[index] as HTMLElement;
		const nodePosition: Position = { x: props.node.x, y: props.node.y };
		const totalOffsetX = el.offsetLeft + (isInput ? 0 : el.offsetWidth);
		const totalOffsetY = el.offsetTop + el.offsetHeight / 2 + 1;
		const portPosition = { x: nodePosition.x + totalOffsetX, y: nodePosition.y + totalOffsetY };
		emit('port-mouseover', portPosition);
	}
}
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
