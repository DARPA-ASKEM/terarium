<template>
	<tera-infinite-canvas
		debug-mode
		@click.stop="onCanvasClick()"
		@contextmenu="toggleContextMenu"
		@save-transform="saveTransform"
	>
		<!-- data -->
		<template #data>
			<ContextMenu ref="contextMenu" :model="contextMenuItems" />
			<tera-workflow-node
				v-for="(node, index) in nodes"
				:key="index"
				:node="node"
				@port-selected="(port: WorkflowPort) => createNewEdge(node, port)"
				@port-mouseover="onPortMouseover"
				@dragging="(event) => updatePosition(node, event)"
			>
				<template #body>
					<tera-model-node
						v-if="node.operationType === 'ModelOperation' && models"
						:models="models"
						:node="node"
						@append-output-port="appendOutputPort"
					/>
					<tera-calibration-node
						v-else-if="node.operationType === 'CalibrationOperation'"
						:node="node"
					/>
					<div v-else>Test node</div>
				</template>
			</tera-workflow-node>
		</template>

		<!-- background -->
		<template #background>
			<path v-if="newEdge?.points" :d="drawPath(newEdge.points)" stroke="green" />
			<path v-for="(edge, index) of edges" :d="drawPath(edge.points)" stroke="black" :key="index" />
		</template>
	</tera-infinite-canvas>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import TeraInfiniteCanvas from '@/components/widgets/tera-infinite-canvas.vue';
import {
	Operation,
	Position,
	WorkflowEdge,
	WorkflowNode,
	WorkflowPort,
	WorkflowStatus
} from '@/types/workflow';
import TeraWorkflowNode from '@/components/workflow/tera-workflow-node.vue';
import TeraModelNode from '@/components/workflow/tera-model-node.vue';
import TeraCalibrationNode from '@/components/workflow/tera-calibration-node.vue';
import { ModelOperation } from '@/components/workflow/model-operation';
import { CalibrationOperation } from '@/components/workflow/calibrate-operation';
import ContextMenu from 'primevue/contextmenu';
import { Model } from '@/types/Model';
import * as d3 from 'd3';

defineProps<{
	models?: Model[];
}>();

const nodes = ref<WorkflowNode[]>([]);
const contextMenu = ref();

const newNodePosition = ref<{ x: number; y: number }>({ x: 0, y: 0 });
let canvasTransform = { x: 0, y: 0, k: 1 };
let isCreatingNewEdge = false;
let currentPortPosition: Position = { x: 0, y: 0 };
const newEdge = ref<WorkflowEdge | undefined>();
const edges = ref<WorkflowEdge[]>([]);

const testOperation: Operation = {
	name: 'Test operation',
	description: 'A test operation',
	inputs: [
		{ type: 'number', label: 'Number input' },
		{ type: 'number', label: 'Number input' },
		{ type: 'string', label: 'String input' }
	],
	outputs: [
		{ type: 'number', label: 'Number output' },
		{ type: 'string', label: 'String output' }
	],
	action: () => {},
	isRunnable: true
};

function appendOutputPort(nodeId: string, outputPortData: WorkflowPort) {
	// Find node and assign outport data to its output port
	const node = nodes.value[nodes.value.findIndex(({ id }) => id === nodeId)];
	node.outputs[node.outputs.length - 1] = outputPortData;

	// Create new output port
	node.outputs.push({
		id: node.outputs.length.toString(),
		type: outputPortData.type
	});
}

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

function createNewEdge(node: WorkflowNode, port: WorkflowPort) {
	if (isCreatingNewEdge === false) {
		newEdge.value = {
			id: edges.value.length.toString(),
			workflowId: '0',
			points: [
				{ x: currentPortPosition.x, y: currentPortPosition.y },
				{ x: currentPortPosition.x, y: currentPortPosition.y }
			],
			source: node.id,
			sourcePortId: port.id,
			target: '',
			targetPortId: ''
		};
		isCreatingNewEdge = true;
	} else if (newEdge.value) {
		// FIXME: move to service
		const sourceNode = nodes.value.find((d) => d.id === newEdge.value?.source);
		const sourcePort = sourceNode?.outputs.find((d) => d.id === newEdge.value?.sourcePortId);

		if (port.type === sourcePort?.type) {
			newEdge.value.target = node.id;
			newEdge.value.targetPortId = port.id;
			edges.value.push(newEdge.value);
			cancelNewEdge();
		} else {
			cancelNewEdge();
		}
	}
}

function onCanvasClick() {
	if (isCreatingNewEdge === true) {
		cancelNewEdge();
	}
}

function cancelNewEdge() {
	isCreatingNewEdge = false;
	newEdge.value = undefined;
}

function onPortMouseover(position: Position) {
	currentPortPosition = position;
}

let prevX = 0;
let prevY = 0;
function mouseUpdate(event: MouseEvent) {
	if (newEdge.value && newEdge.value.points && newEdge.value.points.length === 2) {
		const dx = event.x - prevX;
		const dy = event.y - prevY;
		newEdge.value.points[1].x += dx / canvasTransform.k;
		newEdge.value.points[1].y += dy / canvasTransform.k;
	}
	prevX = event.x;
	prevY = event.y;
}

// TODO: rename/refactor
function updateEdgePositions(node: WorkflowNode, { x, y }) {
	edges.value.forEach((edge) => {
		if (edge.source === node.id) {
			edge.points[0].x += x / canvasTransform.k;
			edge.points[0].y += y / canvasTransform.k;
		}
		if (edge.target === node.id) {
			edge.points[edge.points.length - 1].x += x / canvasTransform.k;
			edge.points[edge.points.length - 1].y += y / canvasTransform.k;
		}
	});
}

const updatePosition = (node: WorkflowNode, { x, y }) => {
	node.x += x / canvasTransform.k;
	node.y += y / canvasTransform.k;
	updateEdgePositions(node, { x, y });
};

const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y)
	.curve(d3.curveBasis);

// Get around typescript complaints
const drawPath = (v: any) => pathFn(v) as string;

onMounted(() => {
	document.addEventListener('mousemove', mouseUpdate);
});
onUnmounted(() => {
	document.removeEventListener('mousemove', mouseUpdate);
});
</script>
