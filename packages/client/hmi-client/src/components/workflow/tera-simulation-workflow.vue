<template>
	<infinite-canvas
		debug-mode
		@click.stop="onCanvasClick()"
		@contextmenu="toggleContextMenu"
		@save-transform="saveTransform"
		:new-edge="newEdge"
		:edges="edges"
	>
		<template #foreground></template>
		<template #data>
			<ContextMenu ref="contextMenu" :model="contextMenuItems" />
			<tera-workflow-node
				v-for="(node, index) in nodes"
				:key="index"
				:node="node"
				@port-selected="(portId) => createNewEdge(node, portId)"
				@port-mouseover="onPortMouseover"
			>
				<template #body>
					<tera-model-node
						v-if="node.operationType === 'ModelOperation' && models"
						:models="models"
					/>
					<tera-calibration-node
						v-else-if="node.operationType === 'CalibrationOperation'"
						:node="node"
					/>
					<div v-else>Test node</div>
				</template>
			</tera-workflow-node>
		</template>
	</infinite-canvas>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import InfiniteCanvas from '@/components/widgets/tera-infinite-canvas.vue';
import { Operation, Position, WorkflowEdge, WorkflowNode, WorkflowStatus } from '@/types/workflow';
import TeraWorkflowNode from '@/components/workflow/tera-workflow-node.vue';
import TeraModelNode from '@/components/workflow/tera-model-node.vue';
import TeraCalibrationNode from '@/components/workflow/tera-calibration-node.vue';
import { CalibrationOperation } from '@/types/workflow/CalibrationOperation';
import { ModelOperation } from '@/components/workflow/model-operation';
import ContextMenu from 'primevue/contextmenu';
import { Model } from '@/types/Model';

defineProps<{
	models?: Model[];
}>();

const nodes = ref<WorkflowNode[]>([]);
const contextMenu = ref();
const newNodePosition = ref<{ x: number; y: number }>({ x: 0, y: 0 });
let canvasTransform = { x: 0, y: 0, k: 1 };
const mousePosition = ref<Position>({ x: 0, y: 0 });
const isCreatingNewEdge = ref<boolean>(false);
let currentPortPosition: Position = { x: 0, y: 0 };
const newEdge = ref<WorkflowEdge | undefined>();
const edges = ref<WorkflowEdge[]>([]);

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
	},
	{
		label: 'New model',
		command: () => {
			insertNode(ModelOperation);
		}
	},
	{
		label: 'New calibration',
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

function saveTransform(newTransform: { k: number; x: number; y: number }) {
	canvasTransform = newTransform;
}

function createNewEdge(node: WorkflowNode, portId: string) {
	if (isCreatingNewEdge.value === false) {
		newEdge.value = {
			id: edges.value.length.toString(),
			workflowId: '0',
			points: [currentPortPosition, mousePosition.value],
			source: node.id,
			sourcePortId: portId,
			target: undefined,
			targetPortId: undefined
		};
		isCreatingNewEdge.value = true;
	} else if (newEdge.value) {
		newEdge.value.target = node.id;
		newEdge.value.targetPortId = portId;
		edges.value.push(newEdge.value);
		cancelNewEdge();
	}
}

function onCanvasClick() {
	if (isCreatingNewEdge.value === true) {
		cancelNewEdge();
	}
}

function cancelNewEdge() {
	isCreatingNewEdge.value = false;
	newEdge.value = undefined;
}

function onPortMouseover(position: Position) {
	currentPortPosition = position;
}

function mouseUpdate(event) {
	if (newEdge.value && newEdge.value.points && newEdge.value.points.length === 2) {
		mousePosition.value = {
			x: (event.pageX - canvasTransform.x) / canvasTransform.k,
			y: (event.pageY - 57 - canvasTransform.y) / canvasTransform.k
		};
		if (event.target.className === 'port') {
			newEdge.value.points[1] = currentPortPosition;
		} else {
			newEdge.value.points[1] = mousePosition.value;
		}
	}
}

onMounted(() => window.addEventListener('mousemove', mouseUpdate));
onUnmounted(() => window.removeEventListener('mousemove', mouseUpdate));
</script>
