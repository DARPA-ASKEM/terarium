<template>
	<section class="container" :style="nodeStyle">
		<header>{{ node.operationType }}</header>
		<section class="inputs">
			<li v-for="(input, index) in node.inputs" ref="inputs">
				<div class="port" @click.stop="selectPort(index)"></div>
				{{ input.label }}
			</li>
		</section>
		<section class="outputs">
			<li v-for="(output, index) in node.outputs" ref="outputs">
				{{ output.label }}
				<div class="port" @click.stop="selectPort(index)"></div>
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

const emit = defineEmits(['port-selected']);

const inputs = ref<HTMLElement>();
const outputs = ref<HTMLElement>();

const nodeStyle = ref({
	minWidth: `${props.node.width}px`,
	minHeight: `${props.node.height}px`,
	top: `${props.node.y}px`,
	left: `${props.node.x}px`
});

function selectPort(index: number) {
	if (outputs.value && inputs.value) {
		const el = outputs.value[index] as HTMLElement;
		const nodePosition: Position = { x: props.node.x, y: props.node.y };
		emit('port-selected', nodePosition, el);
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

.port:hover {
	background: var(--surface-border);
}

.inputs .port {
	left: -8px;
}

.outputs .port {
	left: 8px;
}

.outputs {
	padding-bottom: 4px;
}

header {
	padding: 4px;
	white-space: nowrap;
}
</style>
