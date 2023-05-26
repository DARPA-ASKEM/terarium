<template>
	<tera-infinite-canvas
		debug-mode
		@click="onCanvasClick()"
		@contextmenu="toggleContextMenu"
		@save-transform="saveTransform"
		@mouseleave="isMouseOverCanvas = false"
		@mouseenter="isMouseOverCanvas = true"
		@focus="() => {}"
		@blur="() => {}"
		@drop="onDrop"
		@dragover.prevent
		@dragenter.prevent
	>
		<!-- data -->
		<template #data>
			<ContextMenu ref="contextMenu" :model="contextMenuItems" />
			<tera-workflow-node
				v-for="(node, index) in wf.nodes"
				:key="index"
				:node="node"
				@port-selected="(port: WorkflowPort, direction: WorkflowDirection) => createNewEdge(node, port, direction)"
				@port-mouseover="onPortMouseover"
				@port-mouseleave="onPortMouseleave"
				@dragging="(event) => updatePosition(node, event)"
				:canDrag="isMouseOverCanvas"
			>
				<template #body>
					<tera-model-node
						v-if="node.operationType === 'ModelOperation' && models"
						:models="models"
						:model-id="node.outputs?.[0]?.value.model.id.toString() ?? newAssetId"
						:outputAmount="node.outputs.length + 1"
						@append-output-port="(event) => appendOutputPort(node, event)"
					/>
					<tera-calibration-node
						v-else-if="node.operationType === 'CalibrationOperation'"
						:node="node"
						@append-output-port="(event) => appendOutputPort(node, event)"
					/>
					<tera-dataset-node
						v-else-if="node.operationType === 'Dataset' && datasets"
						:datasets="datasets"
						:datasetId="node.outputs?.[0]?.value.toString() ?? newAssetId"
						@append-output-port="(event) => appendOutputPort(node, event)"
					/>
					<tera-simulate-node v-else-if="node.operationType === 'SimulateOperation'" :node="node" />
					<div v-else>
						<Button @click="testNode(node)">Test run</Button
						><span v-if="node.outputs[0]">{{ node.outputs[0].value }}</span>
					</div>
				</template>
			</tera-workflow-node>
		</template>
		<!-- background -->
		<template #backgroundDefs>
			<marker id="circle" markerWidth="8" markerHeight="8" refX="5" refY="5">
				<circle cx="5" cy="5" r="3" style="fill: var(--primary-color)" />
			</marker>
			<marker
				id="arrow"
				viewBox="0 0 16 16"
				refX="8"
				refY="8"
				orient="auto"
				markerWidth="16"
				markerHeight="16"
				markerUnits="userSpaceOnUse"
				xoverflow="visible"
			>
				<path d="M 0 0 L 8 8 L 0 16 z" style="fill: var(--primary-color); fill-opacity: 1"></path>
			</marker>
		</template>
		<template #background>
			<path
				v-if="newEdge?.points"
				:d="drawPath(interpolatePointsForCurve(newEdge.points[0], newEdge.points[1]))"
				stroke="#1B8073"
				stroke-dasharray="2"
				stroke-width="2"
				marker-start="url(#circle)"
				marker-end="url(#arrow)"
				fill="none"
			/>
			<path
				v-for="(edge, index) of wf.edges"
				:d="drawPath(interpolatePointsForCurve(edge.points[0], edge.points[1]))"
				stroke="#1B8073"
				stroke-width="2"
				marker-start="url(#circle)"
				marker-mid="url(#arrow)"
				:key="index"
				fill="none"
			/>
		</template>
	</tera-infinite-canvas>
</template>

<script setup lang="ts">
import { v4 as uuidv4 } from 'uuid';
import { ref, onMounted, onUnmounted, computed } from 'vue';
import TeraInfiniteCanvas from '@/components/widgets/tera-infinite-canvas.vue';
import {
	Operation,
	Position,
	Workflow,
	WorkflowEdge,
	WorkflowNode,
	WorkflowPort,
	WorkflowPortStatus,
	WorkflowDirection,
	WorkflowOperationTypes
} from '@/types/workflow';
import TeraWorkflowNode from '@/components/workflow/tera-workflow-node.vue';
import TeraModelNode from '@/components/workflow/tera-model-node.vue';
import TeraCalibrationNode from '@/components/workflow/tera-calibration-node.vue';
import TeraSimulateNode from '@/components/workflow/tera-simulate-node.vue';
import { ModelOperation } from '@/components/workflow/model-operation';
import { CalibrationOperation } from '@/components/workflow/calibrate-operation';
import { SimulateOperation } from '@/components/workflow/simulate-operation';
import ContextMenu from 'primevue/contextmenu';
import Button from 'primevue/button';
import * as workflowService from '@/services/workflow';
import * as d3 from 'd3';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import { Dataset } from '@/types/Types';
import { Model } from '@/types/Model';
import { useDragEvent } from '@/services/drag-drop';
import { DatasetOperation } from './dataset-operation';
import TeraDatasetNode from './tera-dataset-node.vue';

// Will probably be used later to save the workflow in the project
const props = defineProps<{
	project: IProject;
}>();

const newNodePosition = { x: 0, y: 0 };
let canvasTransform = { x: 0, y: 0, k: 1 };
let currentPortPosition: Position = { x: 0, y: 0 };
let isMouseOverPort: boolean = false;

const newEdge = ref<WorkflowEdge | undefined>();
const newAssetId = ref<string | null>(null);
const isMouseOverCanvas = ref<boolean>(false);

const wf = ref<Workflow>(workflowService.create());
const contextMenu = ref();

const testOperation: Operation = {
	name: WorkflowOperationTypes.TEST,
	description: 'A test operation',
	inputs: [
		{ type: 'number', label: 'Number input' },
		{ type: 'string', label: 'String input' }
	],
	outputs: [{ type: 'number', label: 'Number output' }],
	action: () => {},
	isRunnable: true
};

const models = computed<Model[]>(() => props.project.assets?.models ?? []);
const datasets = computed<Dataset[]>(() => props.project.assets?.datasets ?? []);

function appendOutputPort(node: WorkflowNode, port: { type: string; label?: string; value: any }) {
	node.outputs.push({
		id: uuidv4(),
		type: port.type,
		label: port.label,
		value: port.value,
		status: WorkflowPortStatus.NOT_CONNECTED
	});
}

// Run testOperation
const testNode = (node: WorkflowNode) => {
	if (node.outputs.length === 0) {
		node.outputs.push({
			id: uuidv4(),
			label: 'test',
			value: null,
			type: 'number',
			status: WorkflowPortStatus.NOT_CONNECTED
		});
	}

	if (node.inputs[0].value !== null) {
		node.outputs[0].value = node.inputs[0].value + Math.round(Math.random() * 10);
	} else {
		node.outputs[0].value = Math.round(Math.random() * 10);
	}
};

const contextMenuItems = ref([
	{
		label: 'New operation',
		command: () => {
			workflowService.addNode(wf.value, testOperation, newNodePosition);
		}
	},
	{
		label: 'New model',
		command: () => {
			newAssetId.value = null;
			workflowService.addNode(wf.value, ModelOperation, newNodePosition);
		}
	},
	{
		label: 'New dataset',
		command: () => {
			newAssetId.value = null;
			workflowService.addNode(wf.value, DatasetOperation, newNodePosition);
		}
	},
	{
		label: 'New calibration',
		command: () => {
			workflowService.addNode(wf.value, CalibrationOperation, newNodePosition);
		}
	},
	{
		label: 'New simulation',
		command: () => {
			workflowService.addNode(wf.value, SimulateOperation, newNodePosition, {
				width: 420,
				height: 220
			});
		}
	}
]);

const { getDragData } = useDragEvent();

function onDrop(event) {
	const { assetId, assetType } = getDragData('initAssetNode') as {
		assetId: string;
		assetType: ProjectAssetTypes;
	};

	if (assetId && assetType) {
		updateNewNodePosition(event);

		let operation: Operation;

		switch (assetType) {
			case ProjectAssetTypes.MODELS:
				operation = ModelOperation;
				break;
			case ProjectAssetTypes.DATASETS:
				operation = DatasetOperation;
				break;
			default:
				return;
		}

		workflowService.addNode(wf.value, operation, newNodePosition);
		newAssetId.value = assetId;
	}
}

function toggleContextMenu(event) {
	contextMenu.value.show(event);
	updateNewNodePosition(event);
}

function updateNewNodePosition(event) {
	newNodePosition.x = (event.offsetX - canvasTransform.x) / canvasTransform.k;
	newNodePosition.y = (event.offsetY - canvasTransform.y) / canvasTransform.k;
}

function saveTransform(newTransform: { k: number; x: number; y: number }) {
	canvasTransform = newTransform;

	const t = wf.value.transform;
	t.x = newTransform.x;
	t.y = newTransform.y;
	t.k = newTransform.k;
}

const isCreatingNewEdge = computed(
	() => newEdge.value && newEdge.value.points && newEdge.value.points.length === 2
);

function createNewEdge(node: WorkflowNode, port: WorkflowPort, direction: WorkflowDirection) {
	if (!isCreatingNewEdge.value) {
		newEdge.value = {
			id: 'new edge',
			workflowId: '0',
			points: [
				{ x: currentPortPosition.x, y: currentPortPosition.y },
				{ x: currentPortPosition.x, y: currentPortPosition.y }
			],
			source: direction === WorkflowDirection.FROM_OUTPUT ? node.id : undefined,
			sourcePortId: direction === WorkflowDirection.FROM_OUTPUT ? port.id : undefined,
			target: direction === WorkflowDirection.FROM_OUTPUT ? undefined : node.id,
			targetPortId: direction === WorkflowDirection.FROM_OUTPUT ? undefined : port.id,
			direction
		};
	} else {
		workflowService.addEdge(
			wf.value,
			newEdge.value!.source ?? node.id,
			newEdge.value!.sourcePortId ?? port.id,
			newEdge.value!.target ?? node.id,
			newEdge.value!.targetPortId ?? port.id,
			newEdge.value!.points
		);
		cancelNewEdge();
	}
}

function onCanvasClick() {
	if (isCreatingNewEdge.value) {
		cancelNewEdge();
	}
}

function cancelNewEdge() {
	newEdge.value = undefined;
}

function onPortMouseover(position: Position) {
	currentPortPosition = position;
	isMouseOverPort = true;
}

function onPortMouseleave() {
	isMouseOverPort = false;
}

let prevX = 0;
let prevY = 0;
function mouseUpdate(event: MouseEvent) {
	if (isCreatingNewEdge.value) {
		const pointIndex = newEdge.value?.direction === WorkflowDirection.FROM_OUTPUT ? 1 : 0;
		if (isMouseOverPort) {
			newEdge.value!.points[pointIndex].x = currentPortPosition.x;
			newEdge.value!.points[pointIndex].y = currentPortPosition.y;
		} else {
			const dx = event.x - prevX;
			const dy = event.y - prevY;
			newEdge.value!.points[pointIndex].x += dx / canvasTransform.k;
			newEdge.value!.points[pointIndex].y += dy / canvasTransform.k;
		}
	}
	prevX = event.x;
	prevY = event.y;
}

// TODO: rename/refactor
function updateEdgePositions(node: WorkflowNode, { x, y }) {
	wf.value.edges.forEach((edge) => {
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

function interpolatePointsForCurve(a: Position, b: Position): Position[] {
	const controlXOffset = 50;
	return [a, { x: a.x + controlXOffset, y: a.y }, { x: b.x - controlXOffset, y: b.y }, b];
}

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
