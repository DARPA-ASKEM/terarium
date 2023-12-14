<template>
	<!-- add 'debug-mode' to debug this -->
	<tera-infinite-canvas
		v-if="!isWorkflowLoading"
		@click="onCanvasClick()"
		@contextmenu="toggleContextMenu"
		@save-transform="saveTransform"
		@mouseleave="setMouseOverCanvas(false)"
		@mouseenter="setMouseOverCanvas(true)"
		@focus="() => {}"
		@blur="() => {}"
		@drop="onDrop"
		@dragover.prevent
		@dragenter.prevent
	>
		<!-- toolbar -->
		<template #foreground>
			<div class="toolbar glass">
				<div class="button-group">
					<InputText
						v-if="isRenamingWorkflow"
						class="p-inputtext-sm"
						v-model.lazy="newWorkflowName"
						placeholder="Workflow name"
						@keyup.enter="updateWorkflowName"
					/>
					<h5 v-else>{{ wf.name }}</h5>
					<Button
						icon="pi pi-ellipsis-v"
						class="p-button-icon-only p-button-text p-button-rounded"
						@click="toggleOptionsMenu"
					/>
				</div>
				<Menu ref="optionsMenu" :model="optionsMenuItems" :popup="true" />
				<div class="button-group">
					<Button label="Show all" severity="secondary" outlined @click="resetZoom" />
					<Button label="Clean up layout" severity="secondary" outlined @click="cleanUpLayout" />
					<Button
						id="add-component-btn"
						icon="pi pi-plus"
						label="Add component"
						@click="showAddComponentMenu"
					/>
					<!--ContextMenu is used instead of TieredMenu for the submenus to appear on the left (not get cut off on the right)-->
					<ContextMenu
						ref="addComponentMenu"
						:model="contextMenuItems"
						style="white-space: nowrap; width: auto"
					/>
				</div>
			</div>
		</template>
		<!-- data -->
		<template #data>
			<ContextMenu
				ref="contextMenu"
				:model="contextMenuItems"
				style="white-space: nowrap; width: auto"
			/>
			<tera-operator
				v-for="(node, index) in wf.nodes"
				:key="index"
				:node="node"
				@port-selected="
					(port: WorkflowPort, direction: WorkflowDirection) => createNewEdge(node, port, direction)
				"
				@port-mouseover="onPortMouseover"
				@port-mouseleave="onPortMouseleave"
				@dragging="(event) => updatePosition(node, event)"
				@remove-operator="(event) => removeNode(event)"
				@remove-edges="removeEdges"
			>
				<template #body>
					<component
						:is="registry.getNode(node.operationType)"
						:node="node"
						@append-output-port="(event: any) => appendOutputPort(node, event)"
						@append-input-port="(event: any) => appendInputPort(node, event)"
						@update-state="(event: any) => updateWorkflowNodeState(node, event)"
						@open-drilldown="openDrilldown(node)"
					/>
				</template>
			</tera-operator>
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
				stroke="#1B8073"
				stroke-width="2"
				marker-start="url(#circle)"
				:key="index"
				fill="none"
			/>
		</template>
	</tera-infinite-canvas>
	<tera-progress-spinner v-else :font-size="2" is-centered />
	<Teleport to="body">
		<component
			v-if="dialogIsOpened && currentActiveNode"
			:is="registry.getDrilldown(currentActiveNode.operationType)"
			:node="currentActiveNode"
			@append-output-port="(event: any) => appendOutputPort(currentActiveNode, event)"
			@update-state="(event: any) => updateWorkflowNodeState(currentActiveNode, event)"
			@select-output="(event: any) => selectOutput(currentActiveNode, event)"
			@update-output-port="(event: any) => updateOutputPort(currentActiveNode, event)"
			@close="dialogIsOpened = false"
		>
		</component>
	</Teleport>
</template>

<script setup lang="ts">
import { isArray, cloneDeep, isEmpty } from 'lodash';
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
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
	WorkflowOutput
} from '@/types/workflow';
// Operation imports
import TeraOperator from '@/components/operator/tera-operator.vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Menu from 'primevue/menu';
import ContextMenu from 'primevue/contextmenu';
import * as workflowService from '@/services/workflow';
import * as d3 from 'd3';
import { AssetType } from '@/types/Types';
import { useDragEvent } from '@/services/drag-drop';
import { v4 as uuidv4 } from 'uuid';

import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';

import { logger } from '@/utils/logger';
import * as SimulateCiemssOp from './ops/simulate-ciemss/mod';
import * as StratifyMiraOp from './ops/stratify-mira/mod';
import * as DatasetOp from './ops/dataset/mod';
import * as FunmanOp from './ops/funman/mod';
import * as SimulateEnsembleCiemssOp from './ops/simulate-ensemble-ciemss/mod';
import * as ModelFromCodeOp from './ops/model-from-code/mod';
import * as SimulateJuliaOp from './ops/simulate-julia/mod';
import * as ModelTransformerOp from './ops/model-transformer/mod';
import * as ModelOp from './ops/model/mod';
import * as ModelConfigOp from './ops/model-config/mod';
import * as CalibrateCiemssOp from './ops/calibrate-ciemss/mod';
import * as CalibrateEnsembleCiemssOp from './ops/calibrate-ensemble-ciemss/mod';
import * as DatasetTransformerOp from './ops/dataset-transformer/mod';
import * as CalibrateJuliaOp from './ops/calibrate-julia/mod';
import * as CodeAssetOp from './ops/code-asset/mod';
import * as ModelOptimizeOp from './ops/model-optimize/mod';
import * as ModelCouplingOp from './ops/model-coupling/mod';

const WORKFLOW_SAVE_INTERVAL = 8000;

const registry = new workflowService.WorkflowRegistry();
registry.registerOp(SimulateJuliaOp);
registry.registerOp(SimulateCiemssOp);
registry.registerOp(StratifyMiraOp);
registry.registerOp(ModelFromCodeOp);
registry.registerOp(SimulateEnsembleCiemssOp);
registry.registerOp(DatasetOp);
registry.registerOp(ModelTransformerOp);
registry.registerOp(FunmanOp);
registry.registerOp(ModelOp);
registry.registerOp(CalibrateEnsembleCiemssOp);
registry.registerOp(ModelConfigOp);
registry.registerOp(CalibrateCiemssOp);
registry.registerOp(DatasetTransformerOp);
registry.registerOp(CodeAssetOp);
registry.registerOp(CalibrateJuliaOp);
registry.registerOp(ModelOptimizeOp);
registry.registerOp(ModelCouplingOp);

// Will probably be used later to save the workflow in the project
const props = defineProps<{
	assetId: string;
}>();

const newNodePosition = { x: 0, y: 0 };
let canvasTransform = { x: 0, y: 0, k: 1 };
let currentPortPosition: Position = { x: 0, y: 0 };
let isMouseOverPort: boolean = false;
let isMouseOverCanvas: boolean = false;
let saveTimer: any = null;
let workflowDirty: boolean = false;

const isWorkflowLoading = ref(false);

const currentActiveNode = ref<WorkflowNode<any> | null>(null);
const newEdge = ref<WorkflowEdge | undefined>();
const dialogIsOpened = ref(false);

const wf = ref<Workflow>(workflowService.emptyWorkflow());
const contextMenu = ref();

const isRenamingWorkflow = ref(false);
const newWorkflowName = ref('');

const optionsMenu = ref();
const optionsMenuItems = ref([
	{
		icon: 'pi pi-pencil',
		label: 'Rename',
		command() {
			isRenamingWorkflow.value = true;
			newWorkflowName.value = wf.value?.name ?? '';
		}
	}
]);

const setMouseOverCanvas = (val: boolean) => {
	isMouseOverCanvas = val;
};

const toggleOptionsMenu = (event) => {
	optionsMenu.value.toggle(event);
};

async function updateWorkflowName() {
	const workflowClone = cloneDeep(wf.value);
	workflowClone.name = newWorkflowName.value;
	workflowService.updateWorkflow(workflowClone);
	isRenamingWorkflow.value = false;
	wf.value = await workflowService.getWorkflow(props.assetId);
}

function appendInputPort(
	node: WorkflowNode<any>,
	port: { type: string; label?: string; value: any }
) {
	node.inputs.push({
		id: uuidv4(),
		type: port.type,
		label: port.label,
		isOptional: false,
		status: WorkflowPortStatus.NOT_CONNECTED
	});
}

function appendOutputPort(
	node: WorkflowNode<any> | null,
	port: { type: string; label?: string; value: any; state?: any; isSelected?: boolean }
) {
	if (!node) return;

	const uuid = uuidv4();

	const outputPort: WorkflowOutput<any> = {
		id: uuid,
		type: port.type,
		label: port.label,
		value: isArray(port.value) ? port.value : [port.value],
		isOptional: false,
		status: WorkflowPortStatus.NOT_CONNECTED,
		state: port.state,
		timestamp: new Date()
	};

	if ('isSelected' in port) outputPort.isSelected = port.isSelected;

	node.outputs.push(outputPort);
	node.active = uuid;

	workflowDirty = true;
}

function updateWorkflowNodeState(node: WorkflowNode<any> | null, state: any) {
	if (!node) return;
	workflowService.updateNodeState(wf.value, node.id, state);
	workflowDirty = true;
}

function selectOutput(node: WorkflowNode<any> | null, selectedOutputId: string) {
	if (!node) return;
	workflowService.selectOutput(node, selectedOutputId);
	workflowDirty = true;
}

function updateOutputPort(node: WorkflowNode<any> | null, workflowOutput: WorkflowOutput<any>) {
	if (!node) return;
	workflowService.updateOutputPort(node, workflowOutput);
	workflowDirty = true;
}

const openDrilldown = (node: WorkflowNode<any>) => {
	currentActiveNode.value = node;
	dialogIsOpened.value = true;
};

const removeNode = (event) => {
	workflowService.removeNode(wf.value, event);
};

const largeNode = { width: 420, height: 220 };

// Menu categories and list items are in order of appearance for separators to work
const categories: Record<string, { label: string; icon: string; separator?: boolean }> = {
	model: {
		label: 'Model operators',
		icon: 'pi pi-share-alt'
	},
	code: {
		label: 'Code operators',
		icon: 'pi pi-code'
	},
	document: {
		label: 'Document operators',
		icon: 'pi pi-file'
	},
	dataset: {
		label: 'Dataset operators',
		icon: 'pi pi-database'
	},
	simulate: {
		separator: true,
		label: 'Simulate',
		icon: 'pi pi-chart-bar'
	},
	llm: {
		label: "Ask 'em LLM tool",
		icon: 'pi pi-comment'
	}
};
const operationContextMenuList = [
	// Model
	{ name: ModelOp.name, category: categories.model },
	{ name: ModelConfigOp.name, category: categories.model },
	{ name: StratifyMiraOp.name, category: categories.model },
	{ name: ModelTransformerOp.name, category: categories.model },
	{ name: FunmanOp.name, category: categories.model, separator: true },
	{ name: ModelOptimizeOp.name, category: categories.model },
	{ name: ModelCouplingOp.name, category: categories.model },
	// Code
	{ name: CodeAssetOp.name, category: categories.code },
	{ name: ModelFromCodeOp.name, category: categories.code },
	// Dataset
	{ name: DatasetOp.name, category: categories.dataset },
	{ name: DatasetTransformerOp.name, category: categories.dataset },
	// Simulate
	{ name: CalibrateJuliaOp.name, category: categories.simulate, options: { size: largeNode } },
	{ name: SimulateJuliaOp.name, category: categories.simulate, options: { size: largeNode } },
	{
		name: SimulateCiemssOp.name,
		category: categories.simulate,
		options: { size: largeNode },
		separator: true
	},
	{
		name: CalibrateCiemssOp.name,
		category: categories.simulate,
		options: { size: largeNode }
	},
	{
		name: CalibrateEnsembleCiemssOp.name,
		category: categories.simulate,
		options: { size: largeNode },
		separator: true
	},
	{
		name: SimulateEnsembleCiemssOp.name,
		category: categories.simulate,
		options: { size: largeNode }
	}
];

const contextMenuItems = ref<any[]>([]);

// Add operator categories to the context menu
Object.values(categories).forEach(({ label, icon, separator }) => {
	if (separator) {
		contextMenuItems.value.push({ separator });
	}
	contextMenuItems.value.push({
		label,
		icon,
		items: []
	});
});

// Add operators within the proper categories
operationContextMenuList.forEach((item) => {
	const op = registry.getOperation(item.name);
	if (!op) return;

	const categoryIndex = contextMenuItems.value.findIndex(
		({ label }) => label === item.category.label
	);

	if (item.separator) {
		contextMenuItems.value[categoryIndex].items.push({ separator: item.separator });
	}

	contextMenuItems.value[categoryIndex].items.push({
		label: op.displayName,
		command: () => {
			workflowService.addNode(wf.value, op, newNodePosition, item.options);
			workflowDirty = true;
		}
	});
});

const addComponentMenu = ref();
const showAddComponentMenu = () => {
	const el = document.querySelector('#add-component-btn');
	const coords = el?.getBoundingClientRect();

	if (coords) {
		const event = new PointerEvent('click', {
			clientX: coords.x + coords.width,
			clientY: coords.y + coords.height
		});
		addComponentMenu.value.toggle(event);
	}
};

const { getDragData } = useDragEvent();

function onDrop(event) {
	const { assetId, assetType } = getDragData('initAssetNode') as {
		assetId: string;
		assetType: AssetType;
	};

	if (assetId && assetType) {
		updateNewNodePosition(event);

		let operation: Operation;
		let state: any = null;

		switch (assetType) {
			case AssetType.Models:
				operation = ModelOp.operation;
				state = { modelId: assetId };
				break;
			case AssetType.Datasets:
				operation = DatasetOp.operation;
				state = { datasetId: assetId };
				break;
			case AssetType.Code:
				operation = CodeAssetOp.operation;
				state = { codeAssetId: assetId };
				break;
			default:
				return;
		}
		workflowService.addNode(wf.value, operation, newNodePosition, { state });
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

function createNewEdge(node: WorkflowNode<any>, port: WorkflowPort, direction: WorkflowDirection) {
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

function removeEdges(portId: string) {
	const edges = wf.value.edges.filter(
		({ targetPortId, sourcePortId }) => targetPortId === portId || sourcePortId === portId
	);
	if (!isEmpty(edges)) {
		edges.forEach((edge) => {
			workflowService.removeEdge(wf.value, edge.id);
		});
		workflowDirty = true;
	} else logger.error(`Edges with port id:${portId} not found.`);
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
function updateEdgePositions(node: WorkflowNode<any>, { x, y }) {
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

const updatePosition = (node: WorkflowNode<any>, { x, y }) => {
	if (!isMouseOverCanvas) return;
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
		isRenamingWorkflow.value = false; // Closes rename input if opened in previous workflow
		if (wf.value && workflowDirty) {
			workflowService.updateWorkflow(wf.value);
		}
		const workflowId = props.assetId;
		if (!workflowId) return;
		isWorkflowLoading.value = true;
		wf.value = await workflowService.getWorkflow(workflowId);
		isWorkflowLoading.value = false;
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
	}, WORKFLOW_SAVE_INTERVAL);
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
	border-bottom: 1px solid var(--surface-border-light);
	z-index: 900;
}

.glass {
	background-color: rgba(255, 255, 255, 0.8);
	backdrop-filter: blur(10px);
}

.button-group {
	display: flex;
	align-items: center;
	flex-direction: row;
	gap: 1rem;
}
</style>
