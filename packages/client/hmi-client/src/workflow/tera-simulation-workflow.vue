<template>
	<infinite-canvas debug-mode @contextmenu="toggleContextMenu" @save-transform="saveTransform">
		<template #foreground></template>
		<template #data>
			<ContextMenu ref="contextMenu" :model="contextMenuItems" />

			<ul v-for="(node, index) in nodes" :key="index">
				<tera-workflow-node :node="node" v-if="node.operationType === 'testOpteration'" />
				<tera-calibration-node :node="node" v-if="node.operationType === 'CalibrationOperation'" />
			</ul>
		</template>
	</infinite-canvas>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import InfiniteCanvas from '@/components/widgets/tera-infinite-canvas.vue';
import { Operation, WorkflowNode, WorkflowStatus } from '@/types/workflow';
import ContextMenu from 'primevue/contextmenu';
import TeraWorkflowNode from '@/components/workflow/tera-workflow-node.vue';
import teraCalibrationNode from '@/components/workflow/tera-calibration-node.vue';
import { CalibrationOperation } from '@/types/workflow/CalibrationOperation';

const nodes = ref<WorkflowNode[]>([]);
const contextMenu = ref();
const newNodePosition = ref<{ x: number; y: number }>({ x: 0, y: 0 });
let canvasTransform = { x: 0, y: 0, k: 1 };

// const testOperation: Operation = {
// 	name: 'Test operation',
// 	description: 'A test operation',
// 	inputs: [
// 		{ type: 'number', label: 'Input one' },
// 		{ type: 'number', label: 'Input two' },
// 		{ type: 'number', label: 'Input three' }
// 	],
// 	outputs: [
// 		{ type: 'number', label: 'Output one' },
// 		{ type: 'number', label: 'Output two' }
// 	],
// 	action: () => {},
// 	isRunnable: true
// };

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
			insertNode(CalibrationOperation);
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
</script>
