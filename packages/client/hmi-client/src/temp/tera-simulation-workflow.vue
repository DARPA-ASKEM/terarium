<template>
	<infinite-canvas @contextmenu="toggleContextMenu" @zoom="updateTransform">
		<template #foreground></template>
		<template #data>
			<ContextMenu ref="contextMenu" :model="contextMenuItems" />

			<ul v-for="node in nodes">
				<tera-workflow-node :node="node" :initialTransform="transform"></tera-workflow-node>
			</ul>
		</template>
	</infinite-canvas>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import InfiniteCanvas from '@/temp/tera-infinite-canvas.vue';
import { Operation, WorkflowNode, WorkflowStatus } from '@/types/workflow';
import TeraWorkflowNode from './tera-workflow-node.vue';
import ContextMenu from 'primevue/contextmenu';

const nodes = ref<WorkflowNode[]>([]);
const contextMenu = ref();
const newNodePosition = ref<{ x: number; y: number }>({ x: 0, y: 0 });
const transform = ref<d3.ZoomTransform>();

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
	const transformX = transform.value ? transform.value.x : 0;
	const transformY = transform.value ? transform.value.y : 0;
	const transformK = transform.value ? transform.value.k : 1;
	newNodePosition.value = {
		x: (event.offsetX - transformX) / transformK,
		y: (event.offsetY - transformY) / transformK
	};
}

function updateTransform(newTransform: d3.ZoomTransform) {
	transform.value = newTransform;
}
</script>
