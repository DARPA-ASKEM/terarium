<template>
	<!-- add 'debug-mode' to debug this -->
	<tera-infinite-canvas
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
		<!-- toolbar -->
		<template #foreground>
			<div class="toolbar glass">
				<h5>{{ wf.name }}</h5>
				<div class="button-group">
					<Button label="Show all" class="secondary-button" text @click="resetZoom" />
					<Button label="Clean up layout" class="secondary-button" text @click="cleanUpLayout" />
					<Button icon="pi pi-plus" label="Add component" @click="showAddComponentMenu" />
					<Menu ref="addComponentMenu" :model="contextMenuItems" :popup="true" />
				</div>
			</div>
		</template>
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
				@remove-node="(event) => removeNode(event)"
				@drilldown="(event) => drilldown(event)"
				:canDrag="isMouseOverCanvas"
			>
				<template #body>
					<tera-model-node
						v-if="node.operationType === 'ModelOperation' && models"
						:models="models"
						:node="node"
						@select-model="(event) => selectModel(node, event)"
					/>
					<tera-dataset-node
						v-else-if="node.operationType === 'Dataset' && datasets"
						:datasets="datasets"
						:node="node"
						@select-dataset="(event) => selectDataset(node, event)"
					/>
					<tera-simulate-node
						v-else-if="node.operationType === 'SimulateOperation'"
						:node="node"
						@append-output-port="(event) => appendOutputPort(node, event)"
					/>
					<tera-calibration-node
						v-else-if="node.operationType === 'CalibrationOperation'"
						:node="node"
						@append-output-port="(event) => appendOutputPort(node, event)"
					/>
					<tera-stratify-node v-else-if="node.operationType === WorkflowOperationTypes.STRATIFY" />
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
				v-for="i in wf.edges.length"
				:key="i"
				:id="`circle${i - 1}`"
				markerWidth="8"
				markerHeight="8"
				refX="5"
				refY="5"
			>
				<circle cx="5" cy="5" r="3" :style="`fill: ${getVariableColorByRunIdx(i - 1)}`" />
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
			<marker
				id="smallArrow"
				viewBox="0 0 16 16"
				refX="8"
				refY="8"
				orient="auto"
				markerWidth="12"
				markerHeight="12"
				markerUnits="userSpaceOnUse"
				xoverflow="visible"
			>
				<path d="M 0 0 L 8 8 L 0 16 z" style="fill: var(--primary-color); fill-opacity: 1"></path>
			</marker>
			<marker
				v-for="i in wf.edges.length"
				:key="i"
				:id="`smallArrow${i - 1}`"
				viewBox="0 0 16 16"
				refX="8"
				refY="8"
				orient="auto"
				markerWidth="12"
				markerHeight="12"
				markerUnits="userSpaceOnUse"
				xoverflow="visible"
			>
				<path
					d="M 0 0 L 8 8 L 0 16 z"
					:style="`fill: ${getVariableColorByRunIdx(i - 1)}; fill-opacity: 1`"
				></path>
			</marker>
		</template>
		<template #background>
			<path
				v-if="newEdge?.points"
				:d="drawPath(interpolatePointsForCurve(newEdge.points[0], newEdge.points[1]))"
				stroke="#1B8073"
				stroke-width="2"
				marker-start="url(#circle)"
				marker-end="url(#arrow)"
				fill="none"
			/>
			<path
				v-for="(edge, index) of wf.edges"
				:d="drawPath(interpolatePointsForCurve(edge.points[0], edge.points[1]))"
				:stroke="isEdgeTargetSim(edge) ? getVariableColorByRunIdx(index) : '#1B8073'"
				stroke-width="2"
				:marker-start="`url(#circle${isEdgeTargetSim(edge) ? index : ''})`"
				:marker-mid="`url(#smallArrow${isEdgeTargetSim(edge) ? index : ''})`"
				:key="index"
				fill="none"
			/>
		</template>
	</tera-infinite-canvas>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { v4 as uuidv4 } from 'uuid';
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { getModelConfigurations } from '@/services/model';
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
import {
	SimulateOperation,
	SimulateOperationState
} from '@/components/workflow/simulate-operation';
import { StratifyOperation } from '@/components/workflow/stratify-operation';
import ContextMenu from '@/components/widgets/tera-context-menu.vue';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import * as workflowService from '@/services/workflow';
import * as d3 from 'd3';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import { Dataset, Model } from '@/types/Types';
import { useDragEvent } from '@/services/drag-drop';
import { DatasetOperation } from './dataset-operation';
import TeraDatasetNode from './tera-dataset-node.vue';
import TeraStratifyNode from './tera-stratify-node.vue';

const workflowEventBus = workflowService.workflowEventBus;

// Will probably be used later to save the workflow in the project
const props = defineProps<{
	project: IProject;
	assetId: string;
}>();

const newNodePosition = { x: 0, y: 0 };
let canvasTransform = { x: 0, y: 0, k: 1 };
let currentPortPosition: Position = { x: 0, y: 0 };
let isMouseOverPort: boolean = false;
let saveTimer: any = null;
let workflowDirty: boolean = false;

const newEdge = ref<WorkflowEdge | undefined>();
const newAssetId = ref<string | null>(null);
const isMouseOverCanvas = ref<boolean>(false);

const wf = ref<Workflow>(workflowService.emptyWorkflow());
const contextMenu = ref();

// FIXME: temporary function to color edges with simulate
const VIRIDIS_14 = [
	'#440154',
	'#481c6e',
	'#453581',
	'#3d4d8a',
	'#34618d',
	'#2b748e',
	'#24878e',
	'#1f998a',
	'#25ac82',
	'#40bd72',
	'#67cc5c',
	'#98d83e',
	'#cde11d',
	'#fde725'
];
const getVariableColorByRunIdx = (edgeIdx: number) =>
	wf.value.edges.length > 1
		? VIRIDIS_14[Math.floor((edgeIdx / wf.value.edges.length) * VIRIDIS_14.length)]
		: '#1B8073';
const isEdgeTargetSim = (edge) =>
	wf.value.nodes.find((node) => node.id === edge.target)?.operationType ===
	WorkflowOperationTypes.SIMULATE;

const testOperation: Operation = {
	name: WorkflowOperationTypes.TEST,
	description: 'A test operation',
	inputs: [
		{ type: 'number', label: 'Number input', acceptMultiple: false },
		{ type: 'number', label: 'Multi number input', acceptMultiple: true },
		{ type: 'string', label: 'String input' }
	],
	outputs: [{ type: 'number', label: 'Number output' }],
	action: () => {},
	isRunnable: true
};

const models = computed<Model[]>(() => props.project.assets?.models ?? []);
const datasets = computed<Dataset[]>(() => props.project.assets?.datasets ?? []);

async function selectModel(node: WorkflowNode, data: { id: string }) {
	node.state.modelId = data.id;

	// FIXME: Need additional design to work out exactly what to show. June 2023
	// FIXME: Need to merge with any existing output-port results (e.g. new configs are added)
	const configurationList = await getModelConfigurations(data.id);
	node.outputs = [];
	configurationList.forEach((configuration) => {
		node.outputs.push({
			id: uuidv4(),
			type: 'modelConfigId',
			label: configuration.name,
			value: [configuration.id],
			status: WorkflowPortStatus.NOT_CONNECTED
		});
	});
}

async function selectDataset(node: WorkflowNode, data: { id: string; name: string }) {
	node.state.datasetId = data.id;
	node.outputs = [
		{
			id: uuidv4(),
			type: 'datasetId',
			label: data.name,
			value: [data.id],
			status: WorkflowPortStatus.NOT_CONNECTED
		}
	];
}

function appendOutputPort(node: WorkflowNode, port: { type: string; label?: string; value: any }) {
	node.outputs.push({
		id: uuidv4(),
		type: port.type,
		label: port.label,
		value: _.isArray(port.value) ? port.value : [port.value],
		status: WorkflowPortStatus.NOT_CONNECTED
	});

	// FIXME: This is a bit hacky, we should split this out into separate events, or the action
	// should be built into the Operation directly. What we are doing is to update the internal state
	// and this feels it is leaking too much low-level information
	if (node.operationType === 'SimulateOperation') {
		const state = node.state as SimulateOperationState;
		if (state.chartConfigs.length === 0) {
			state.chartConfigs.push({
				selectedRun: port.value[0],
				selectedVariable: ['S']
			});
		}
	}
	workflowDirty = true;
}

// Run testOperation
const testNode = (node: WorkflowNode) => {
	const value = (node.inputs[0].value?.[0] ?? 0) + Math.round(Math.random() * 10);
	appendOutputPort(node, { type: 'number', label: value.toString(), value });
};

const drilldown = (event: WorkflowNode) => {
	workflowEventBus.emit('drilldown', event);
};

workflowEventBus.on('node-state-change', (payload: any) => {
	if (wf.value.id !== payload.workflowId) return;
	workflowService.updateNodeState(wf.value, payload.nodeId, payload.state);
});

const removeNode = (event) => {
	workflowService.removeNode(wf.value, event);
};

const contextMenuItems = ref([
	{
		label: 'Test operation',
		command: () => {
			workflowService.addNode(wf.value, testOperation, newNodePosition);
			workflowDirty = true;
		}
	},
	{
		label: 'Model',
		command: () => {
			newAssetId.value = null;
			workflowService.addNode(wf.value, ModelOperation, newNodePosition);
			workflowDirty = true;
		}
	},
	{
		label: 'Dataset',
		command: () => {
			newAssetId.value = null;
			workflowService.addNode(wf.value, DatasetOperation, newNodePosition);
			workflowDirty = true;
		}
	},
	{
		label: 'Stratify',
		command: () => {
			workflowService.addNode(wf.value, StratifyOperation, newNodePosition);
		}
	},
	{
		label: 'Deterministic',
		items: [
			{
				label: 'Simulate',
				command: () => {
					workflowService.addNode(wf.value, SimulateOperation, newNodePosition, {
						width: 420,
						height: 220
					});
					workflowDirty = true;
				}
			},
			{
				label: 'Calibrate',
				command: () => {
					workflowService.addNode(wf.value, CalibrationOperation, newNodePosition);
					workflowDirty = true;
				}
			}
		]
	},
	{
		label: 'Probabilistic',
		items: [
			{
				label: 'Simulate',
				disabled: true,
				command: () => {}
			},
			{
				label: 'Calibrate & Simulate',
				disabled: true,
				command: () => {}
			}
		]
	}
]);
const addComponentMenu = ref();
const showAddComponentMenu = (event) => addComponentMenu.value.toggle(event);

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
	workflowDirty = true;
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
	workflowDirty = true;
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

watch(
	() => [props.assetId],
	async () => {
		if (wf.value && workflowDirty) {
			workflowService.updateWorkflow(wf.value);
		}
		const workflowId = props.assetId;
		if (!workflowId) return;
		wf.value = await workflowService.getWorkflow(workflowId);
	},
	{ immediate: true }
);

onMounted(() => {
	document.addEventListener('mousemove', mouseUpdate);
	saveTimer = setInterval(() => {
		if (workflowDirty) {
			workflowService.updateWorkflow(wf.value);
			workflowDirty = false;
		}
	}, 8000);
});
onUnmounted(() => {
	if (workflowDirty) {
		workflowService.updateWorkflow(wf.value);
	}
	if (saveTimer) {
		clearInterval(saveTimer);
	}
	document.removeEventListener('mousemove', mouseUpdate);
});

function cleanUpLayout() {
	// TODO: clean up layout of nodes
	console.log('clean up layout');
}
function resetZoom() {
	// TODO: reset zoom level and position
	console.log('clean up layout');
}
</script>

<style scoped>
.toolbar {
	display: flex;
	flex-direction: row;
	justify-content: space-between;
	align-items: center;
	padding: 0.5rem 1rem;
	border-top: 1px solid var(--surface-border-light);
	border-bottom: 1px solid var(--surface-border-light);
	z-index: 900;
}

.glass {
	background-color: rgba(255, 255, 255, 0.8);
	backdrop-filter: blur(10px);
}

.button-group {
	display: flex;
	flex-direction: row;
	gap: 1rem;
}

/* We should make a proper secondary outline button. Until then this works. */
.toolbar .button-group .secondary-button {
	color: var(--text-color-secondary);
	background-color: var(--surface-0);
	border: 1px solid var(--surface-border-light);
	padding-top: 0px;
	padding-bottom: 0px;
}

.toolbar .button-group .secondary-button:hover {
	color: var(--text-color-secondary) !important;
	background-color: var(--surface-highlight) !important;
}

.toolbar .button-group .primary-dropdown {
	background-color: var(--primary-color);
	border: 1px solid var(--primary-color);
}

.toolbar .button-group .primary-dropdown:deep(.p-dropdown-label),
.toolbar .button-group .primary-dropdown:deep(.p-dropdown-trigger) {
	color: var(--surface-0);
	padding-top: 0.5rem;
	padding-bottom: 0.5rem;
}
</style>
