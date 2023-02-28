<template>
	<Teleport to="body">
		<Modal v-if="modalVisible" @modal-mask-clicked="modalVisible = false">
			<template #default>
				<InputText
					ref="input"
					id="new-node-name"
					type="text"
					v-model="newNodeName"
					@keyup.enter="insertNode()"
				/>
			</template>
		</Modal>
	</Teleport>
	<div class="container" @click.stop="clickBackground()">
		<div
			class="node"
			:style="calcNodeStyle(node)"
			v-for="node in nodes"
			@click.stop="dragNode(node)"
		>
			<div class="port-left" @click.stop="createNodePath(node, -1)"></div>
			{{ node.name }}
			<div class="port-right" @click.stop="createNodePath(node, 1)"></div>
		</div>
		<svg stroke="black" stroke-width="1" fill="none">
			<path v-if="isCreatingNodePath" :d="drawNewPath()" stroke-dasharray="4"></path>
			<path v-for="path in paths" :d="drawPath(path)"></path>
		</svg>
	</div>
</template>

<style scoped>
.container {
	position: relative;
	height: 100%;
	width: 100%;
}

.node {
	position: absolute;
	height: 100px;
	width: 100px;
	background-color: var(--surface-section);
	border: 1px solid var(--gray-500);
	border-radius: 4px;
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.port-left,
.port-right {
	height: 8px;
	width: 8px;
	border-radius: 4px;
	position: relative;
	border: 1px solid black;
	background-color: var(--surface-section);
}

.port-left:hover,
.port-right:hover {
	height: 16px;
	width: 16px;
	border-radius: 8px;
}

.port-left {
	left: -4px;
}

.port-left:hover {
	left: -8px;
}

.port-right {
	left: 4px;
}

.port-right:hover {
	left: 8px;
}

svg {
	position: absolute;
	height: 100%;
	width: 100%;
	z-index: -1;
}
</style>

<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted, computed } from 'vue';
import Modal from '@/components/Modal.vue';
import InputText from 'primevue/inputtext';

interface Position {
	x: number;
	y: number;
}

interface Node {
	id: number;
	name: string;
	inputs?: Node[];
	outputs?: Node[];
	style: {
		top: string;
		left: string;
	};
	isDragging: boolean;
	position: Position;
}

interface Path {
	startPosition: Position;
	endPosition?: Position;
	startNode?: Node;
	endNode?: Node;
	direction: number;
}

interface Edge {
	origin?: Node;
	destination?: Node;
}

const modalVisible = ref(false);
const newNodeName = ref<string>('');
const input = ref<HTMLInputElement | null>(null);
const nodes = ref<Node[]>([]);
const paths = ref<Path[]>([]);
const isCreatingNodePath = ref(false);
const newEdge = ref<Edge>();
const newPathPosition = ref<Position | null>();
const newPath = ref<Path | null>();

const mouseX = ref(0);
const mouseY = ref(0);

function cancelNewPath() {
	newPathPosition.value = null;
	isCreatingNodePath.value = false;
	newPath.value = null;
}

function clickBackground() {
	if (isCreatingNodePath.value) {
		cancelNewPath();
	} else {
		createNode();
	}
}

async function createNode() {
	modalVisible.value = true;
	await nextTick();
	input.value?.$el.focus();
}

function insertNode() {
	modalVisible.value = false;
	nodes.value.push({
		id: nodes.value.length,
		name: newNodeName.value,
		style: {
			top: `0px`,
			left: `0px`
		},
		position: {
			x: 50,
			y: 50
		}
	} as Node);
	newNodeName.value = '';
}

function dragNode(node: Node) {
	node.style.top = `${mouseY.value - 57 - 50}px`;
	node.style.left = `${mouseX.value - 0 - 50}px`;
	node.position.x = mouseX.value - 0;
	node.position.y = mouseY.value - 57;
	node.isDragging = !node.isDragging;
}

function calcNodeStyle(node: Node) {
	if (!node.isDragging) {
		return node.style;
	}
	const top = mouseY.value - 57 - 50;
	const left = mouseX.value - 0 - 50;
	node.position.x = mouseX.value - 0;
	node.position.y = mouseY.value - 57;
	return {
		top: `${top}px`,
		left: `${left}px`,
		cursor: 'move'
	};
}

function createNodePath(node: Node, direction: number) {
	if (!isCreatingNodePath.value) {
		newPathPosition.value = {
			x: node.position.x + 50 * direction,
			y: node.position.y
		} as Position;
		newPath.value = { startPosition: newPathPosition.value, startNode: node, direction };
		isCreatingNodePath.value = true;
		if (direction > 0) {
			newEdge.value = {
				origin: node
			} as Edge;
		} else {
			newEdge.value = {
				destination: node
			} as Edge;
		}
	} else {
		if ((newEdge.value?.origin && direction > 0) || (newEdge.value?.destination && direction < 0)) {
			cancelNewPath();
		} else {
			if (newPathPosition.value && newPath.value) {
				newEdge.value = {};
				const path: Path = {
					startPosition: newPathPosition.value,
					endPosition: {
						x: node.position.x + 50 * direction,
						y: node.position.y
					},
					startNode: newPath.value.startNode,
					endNode: node,
					direction: newPath.value.direction
				};
				paths.value.push(path);
				isCreatingNodePath.value = false;
			}
		}
	}
}

function drawNewPath() {
	const path = newPathPosition.value
		? `M${newPathPosition.value?.x},${newPathPosition.value?.y}
    h10
    C${newPathPosition.value?.x + 50 + 10},${newPathPosition.value?.y}
    ${mouseX.value - 0 - 50 - 10},${mouseY.value - 57}
    ${mouseX.value - 0 + -10},${mouseY.value - 57}
    h10`
		: 'M0,0';
	return path ?? 'M0,0';
}

function drawPath(path: Path) {
	const bezierOffsetX = 50;
	const runwayX = 10;
	const nodeOffsetX = 50 * path.direction;
	const newPath =
		path.endNode && path.startNode
			? `M ${path.startNode.position.x + nodeOffsetX},${path.startNode.position.y}
    h10
    C${path.startNode.position.x + nodeOffsetX + bezierOffsetX},${path.startNode.position.y}
    ${path.endNode.position.x - nodeOffsetX - bezierOffsetX - runwayX} ${path.endNode.position.y}
    ${path.endNode.position.x - nodeOffsetX - runwayX} ${path.endNode.position.y}
    h10`
			: `M0,0`;
	return newPath;
}

function mouseUpdate(event) {
	mouseX.value = event.pageX;
	mouseY.value = event.pageY;
}

onMounted(() => window.addEventListener('mousemove', mouseUpdate));
onUnmounted(() => window.removeEventListener('mousemove', mouseUpdate));
</script>
