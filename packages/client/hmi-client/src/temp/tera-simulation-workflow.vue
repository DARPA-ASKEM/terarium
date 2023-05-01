<template>
	<infinite-canvas
		debug-mode
		@click.stop="handleCanvasClick()"
		@contextmenu="toggleContextMenu"
		@save-transform="saveTransform"
		:new-path="newPath"
	>
		<template #foreground></template>
		<template #data>
			<ContextMenu ref="contextMenu" :model="contextMenuItems" />
			<ul v-for="node in nodes">
				<tera-workflow-node :node="node" @port-selected="createNewEdge"></tera-workflow-node>
			</ul>
		</template>
	</infinite-canvas>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import InfiniteCanvas from '@/components/widgets/tera-infinite-canvas.vue';
import { Operation, Position, WorkflowNode, WorkflowStatus, Path } from '@/types/workflow';
import TeraWorkflowNode from './tera-workflow-node.vue';
import ContextMenu from 'primevue/contextmenu';

const nodes = ref<WorkflowNode[]>([]);
const contextMenu = ref();
const newNodePosition = ref<Position>({ x: 0, y: 0 });
let canvasTransform = { x: 0, y: 0, k: 1 };
const mousePosition = ref<Position>();
const newPath = ref<Path>({ start: undefined, end: undefined });
const isCreatingNewEdge = ref<boolean>(false);
const paths = ref<Path[]>([]);

const testOperation: Operation = {
	name: 'Test operation',
	description: 'A test operation',
	inputs: [
		{ type: 'number', label: 'Input one' },
		{ type: 'number', label: 'Input two' },
		{ type: 'number', label: 'Input three' }
	],
	outputs: [
		{ type: 'number', label: 'Output one' },
		{ type: 'number', label: 'Output two' }
	],
	action: () => {},
	isRunnable: true
};

function insertNode(operation: Operation) {
	const newNode: WorkflowNode = {
		id: nodes.value.length.toString(),
		workflowId: '0',
		operationType: operation.name,
		x: newNodePosition.value.x,
		y: newNodePosition.value.y,
		width: 100,
		height: 100,
		inputs: operation.inputs.map((o, i) => ({ id: i.toString(), ...o })),
		outputs: operation.outputs.map((o, i) => ({ id: i.toString(), ...o })),
		statusCode: WorkflowStatus.INVALID
	};
	nodes.value.push(newNode);
}

const contextMenuItems = ref([
	{
		label: 'New operation',
		command: () => {
			insertNode(testOperation);
		}
	}
]);

function toggleContextMenu(event) {
	contextMenu.value.show(event);
	newNodePosition.value = {
		x: (event.offsetX - canvasTransform.x) / canvasTransform.k,
		y: (event.offsetY - canvasTransform.y) / canvasTransform.k
	};
}

function saveTransform(newTransform) {
	canvasTransform = newTransform;
}

// function applyCanvasTransform(position: Position): Position {
// 	return {
// 		x: (position.x - canvasTransform.x) / canvasTransform.k,
// 		y: (position.y - canvasTransform.y) / canvasTransform.k
// 	}
// }

function createNewEdge(nodePosition: Position, portElement: HTMLElement) {
	if (isCreatingNewEdge.value === false) {
		const totalOffsetX = portElement.offsetLeft + portElement.offsetWidth;
		const totalOffsetY = portElement.offsetTop + portElement.offsetHeight / 2 + 1;
		newPath.value.start = { x: nodePosition.x + totalOffsetX, y: nodePosition.y + totalOffsetY };
		isCreatingNewEdge.value = true;
	}
}

function handleCanvasClick() {
	if (isCreatingNewEdge.value === true) {
		newPath.value.start = undefined;
		isCreatingNewEdge.value = false;
	}
}

function mouseUpdate(event) {
	mousePosition.value = {
		x: (event.offsetX - canvasTransform.x) / canvasTransform.k,
		y: (event.offsetY - canvasTransform.y) / canvasTransform.k
	};
	// mousePosition.value = applyCanvasTransform({ x: event.offsetX, y: event.offsetY });
	if (isCreatingNewEdge.value === true) {
		// console.log(event);
	}
	newPath.value.end = mousePosition.value;
}

onMounted(() => window.addEventListener('mousemove', mouseUpdate));
onUnmounted(() => window.removeEventListener('mousemove', mouseUpdate));
</script>
