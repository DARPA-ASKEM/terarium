<template>
	<!-- add 'debug-mode' to debug this -->
	<tera-infinite-canvas
		v-if="!isWorkflowLoading"
		@click="onCanvasClick()"
		@contextmenu="toggleContextMenu"
		@save-transform="saveTransform"
		@focus="() => {}"
		@blur="() => {}"
		@drop="onDrop"
		@dragover.prevent
		@dragenter.prevent
		:lastTransform="canvasTransform"
	>
		<!-- toolbar -->
		<template #foreground>
			<div class="toolbar glass">
				<div class="button-group w-full">
					<div v-if="isRenamingWorkflow" class="rename-workflow w-full">
						<InputText
							class="p-inputtext w-full"
							v-model.lazy="newWorkflowName"
							placeholder="Workflow name"
							@keyup.enter="updateWorkflowName"
							@keyup.esc="updateWorkflowName"
							v-focus
						/>
						<div class="flex flex-nowrap ml-1 mr-3">
							<Button icon="pi pi-check" rounded text @click="updateWorkflowName" />
						</div>
					</div>
					<h4 v-else>{{ wf.getName() }}</h4>
					<Button
						v-if="!isRenamingWorkflow"
						icon="pi pi-ellipsis-v"
						class="p-button-icon-only p-button-text p-button-rounded"
						@click="toggleOptionsMenu"
					/>
				</div>
				<Menu ref="optionsMenu" :model="optionsMenuItems" :popup="true" />
				<div class="button-group">
					<Button
						id="add-component-btn"
						icon="pi pi-plus"
						label="Add component"
						@click="showAddComponentMenu"
						size="small"
						class="white-space-nowrap"
					/>
					<!--ContextMenu is used instead of TieredMenu for the submenus to appear on the left (not get cut off on the right)-->
					<ContextMenu ref="addComponentMenu" :model="contextMenuItems" style="white-space: nowrap; width: auto" />
				</div>
			</div>
		</template>
		<!-- data -->
		<template #data>
			<ContextMenu ref="contextMenu" :model="contextMenuItems" style="white-space: nowrap; width: auto" />
			<tera-canvas-item
				v-for="node in wf.getNodes()"
				:key="node.id"
				:style="{
					width: `${node.width}px`,
					top: `${node.y}px`,
					left: `${node.x}px`
				}"
				@dragging="(event) => updatePosition(node, event)"
			>
				<tera-operator
					ref="teraOperatorRefs"
					:node="node"
					:nodeMenu="outputPortMenu"
					@resize="resizeHandler"
					@port-selected="(port: WorkflowPort, direction: WorkflowDirection) => createNewEdge(node, port, direction)"
					@port-mouseover="onPortMouseover"
					@port-mouseleave="onPortMouseleave"
					@remove-operator="(event) => removeNode(event)"
					@duplicate-branch="duplicateBranch(node.id)"
					@remove-edges="removeEdges"
					@update-state="(event: any) => updateWorkflowNodeState(node, event)"
					@menu-selection="(operatorType) => onMenuSelection(operatorType, node)"
				>
					<template #body>
						<component
							:is="registry.getNode(node.operationType)"
							:node="node"
							@append-output="(event: any) => appendOutput(node, event)"
							@append-input-port="(event: any) => appendInputPort(node, event)"
							@update-state="(event: any) => updateWorkflowNodeState(node, event)"
							@open-drilldown="addOperatorToRoute(node.id)"
						/>
					</template>
				</tera-operator>
			</tera-canvas-item>
		</template>
		<!-- background -->
		<template #backgroundDefs>
			<marker id="circle" markerWidth="8" markerHeight="8" refX="5" refY="5">
				<circle cx="5" cy="5" r="3" style="fill: var(--text-color-secondary)" />
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
				<path d="M 0 0 L 8 8 L 0 16 z" style="fill: var(--text-color-secondary); fill-opacity: 1"></path>
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
				<path d="M 0 0 L 8 8 L 0 16 z" style="fill: var(--text-color-secondary)"></path>
			</marker>
		</template>
		<template #background>
			<path
				v-if="newEdge?.points"
				:d="drawPath(interpolatePointsForCurve(newEdge.points[0], newEdge.points[1]))"
				stroke="#667085"
				stroke-width="2"
				marker-start="url(#circle)"
				marker-end="url(#arrow)"
				fill="none"
			/>
			<path
				v-for="edge of wf.getEdges()"
				:key="edge.id"
				:d="drawPath(interpolatePointsForCurve(edge.points[0], edge.points[1]))"
				stroke="#667085"
				stroke-width="2"
				marker-start="url(#circle)"
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
			:upstream-operators-nav="upstreamOperatorsNav"
			:downstream-operators-nav="downstreamOperatorsNav"
			:spawn-animation="drilldownSpawnAnimation"
			@append-output="(event: any) => appendOutput(currentActiveNode, event)"
			@update-state="(event: any) => updateWorkflowNodeState(currentActiveNode, event)"
			@update-status="(event: any) => updateWorkflowNodeStatus(currentActiveNode, event)"
			@select-output="(event: any) => selectOutput(currentActiveNode, event)"
			@close="addOperatorToRoute(null)"
			@update-output-port="(event: any) => updateOutputPort(currentActiveNode, event)"
		/>
	</Teleport>
</template>

<script setup lang="ts">
import { cloneDeep, isArray, isEmpty } from 'lodash';
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import TeraInfiniteCanvas from '@/components/widgets/tera-infinite-canvas.vue';
import TeraCanvasItem from '@/components/widgets/tera-canvas-item.vue';
import type { Position } from '@/types/common';
import type { Operation, WorkflowEdge, WorkflowNode, WorkflowOutput, WorkflowPort } from '@/types/workflow';
import { WorkflowDirection, WorkflowPortStatus, OperatorStatus } from '@/types/workflow';
// Operation imports
import TeraOperator from '@/components/operator/tera-operator.vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Menu from 'primevue/menu';
import ContextMenu from 'primevue/contextmenu';
import * as workflowService from '@/services/workflow';
import { OperatorImport, OperatorNodeSize, getNodeMenu } from '@/services/workflow';
import * as d3 from 'd3';
import { AssetType, EventType } from '@/types/Types';
import { useDragEvent } from '@/services/drag-drop';
import { v4 as uuidv4 } from 'uuid';
import { getLocalStorageTransform, setLocalStorageTransform } from '@/utils/localStorage';

import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';

import { logger } from '@/utils/logger';
import { useRouter, useRoute } from 'vue-router';
import { MenuItem } from 'primevue/menuitem';
import * as EventService from '@/services/event';
import { useProjects } from '@/composables/project';
import { cloneNoteBookSession } from '@/services/notebook-session';
import * as SimulateCiemssOp from '@/components/workflow/ops/simulate-ciemss/mod';
import * as StratifyMiraOp from '@/components/workflow/ops/stratify-mira/mod';
import * as DatasetOp from '@/components/workflow/ops/dataset/mod';
import * as FunmanOp from '@/components/workflow/ops/funman/mod';
import * as SimulateEnsembleCiemssOp from '@/components/workflow/ops/simulate-ensemble-ciemss/mod';
import * as ModelOp from '@/components/workflow/ops/model/mod';
import * as ModelEditOp from '@/components/workflow/ops/model-edit/mod';
import * as ModelConfigOp from '@/components/workflow/ops/model-config/mod';
import * as CalibrateCiemssOp from '@/components/workflow/ops/calibrate-ciemss/mod';
import * as CalibrateEnsembleCiemssOp from '@/components/workflow/ops/calibrate-ensemble-ciemss/mod';
import * as DatasetTransformerOp from '@/components/workflow/ops/dataset-transformer/mod';
import * as SubsetDataOp from '@/components/workflow/ops/subset-data/mod';
import * as CodeAssetOp from '@/components/workflow/ops/code-asset/mod';
import * as OptimizeCiemssOp from '@/components/workflow/ops/optimize-ciemss/mod';
import * as DocumentOp from '@/components/workflow/ops/document/mod';
import * as ModelFromDocumentOp from '@/components/workflow/ops/model-from-equations/mod';
import * as ModelComparisonOp from '@/components/workflow/ops/model-comparison/mod';
import * as RegriddingOp from '@/components/workflow/ops/regridding/mod';
import * as InterventionPolicyOp from '@/components/workflow/ops/intervention-policy/mod';

const WORKFLOW_SAVE_INTERVAL = 8000;

const registry = new workflowService.WorkflowRegistry();
registry.registerOp(SimulateCiemssOp);
registry.registerOp(StratifyMiraOp);
registry.registerOp(SimulateEnsembleCiemssOp);
registry.registerOp(DatasetOp);
registry.registerOp(FunmanOp);
registry.registerOp(ModelOp);
registry.registerOp(ModelEditOp);
registry.registerOp(CalibrateEnsembleCiemssOp);
registry.registerOp(ModelConfigOp);
registry.registerOp(CalibrateCiemssOp);
registry.registerOp(DatasetTransformerOp);
registry.registerOp(CodeAssetOp);
registry.registerOp(SubsetDataOp);
registry.registerOp(OptimizeCiemssOp);
registry.registerOp(DocumentOp);
registry.registerOp(ModelFromDocumentOp);
registry.registerOp(ModelComparisonOp);
registry.registerOp(RegriddingOp);
registry.registerOp(InterventionPolicyOp);

// Will probably be used later to save the workflow in the project
const props = defineProps<{
	assetId: string;
}>();

const outputPortMenu = ref(getNodeMenu(registry.operationMap));
const upstreamOperatorsNav = ref<MenuItem[]>([]);
const downstreamOperatorsNav = ref<MenuItem[]>([]);
const drilldownSpawnAnimation = ref<'left' | 'right' | 'scale'>('scale');

const route = useRoute();
const router = useRouter();

const newNodePosition = { x: 0, y: 0 };
let canvasTransform = { x: 0, y: 0, k: 1 };
let currentPortPosition: Position = { x: 0, y: 0 };
let isMouseOverPort: boolean = false;
let saveTimer: any = null;
let workflowDirty: boolean = false;
let startTime: number = 0;

const isWorkflowLoading = ref(false);

const currentActiveNode = ref<WorkflowNode<any> | null>(null);
const newEdge = ref<WorkflowEdge | undefined>();
const dialogIsOpened = ref(false);

const wf = ref<workflowService.WorkflowWrapper>(new workflowService.WorkflowWrapper());
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
			newWorkflowName.value = wf.value?.getName() ?? '';
		}
	}
]);

const toggleOptionsMenu = (event) => {
	optionsMenu.value.toggle(event);
};
const teraOperatorRefs = ref();

async function updateWorkflowName() {
	const workflowClone = cloneDeep(wf.value.dump());
	workflowClone.name = newWorkflowName.value;
	await workflowService.updateWorkflow(workflowClone);
	await useProjects().refresh();
	isRenamingWorkflow.value = false;
	wf.value.load(await workflowService.getWorkflow(props.assetId));
}

function appendInputPort(node: WorkflowNode<any>, port: { type: string; label?: string; value: any }) {
	node.inputs.push({
		id: uuidv4(),
		type: port.type,
		label: port.label,
		isOptional: false,
		status: WorkflowPortStatus.NOT_CONNECTED
	});
}

/**
 * The operator creates a new output, this will mark the
 * output as selected, and revert the selection status of
 * existing outputs
 * */
function appendOutput(
	node: WorkflowNode<any> | null,
	port: {
		type: string;
		label?: string;
		value: any;
		state?: any;
		isSelected?: boolean;
	}
) {
	if (!node) return;

	// We assume that if we can produce an output, the status is okay
	node.status = OperatorStatus.SUCCESS;

	const uuid = uuidv4();
	const outputPort: WorkflowOutput<any> = {
		id: uuid,
		type: port.type,
		label: port.label,
		value: isArray(port.value) ? port.value : [port.value],
		isOptional: false,
		status: WorkflowPortStatus.NOT_CONNECTED,
		state: port.state,
		isSelected: true,
		timestamp: new Date(),
		operatorStatus: node.status
	};

	// Append and set active
	node.outputs.push(outputPort);
	node.active = uuid;

	// Filter out temporary outputs where value is null
	node.outputs = node.outputs.filter((d) => d.value);

	selectOutput(node, uuid);
	workflowDirty = true;
}

function updateWorkflowNodeState(node: WorkflowNode<any> | null, state: any) {
	if (!node) return;
	wf.value.updateNodeState(node.id, state);
	workflowDirty = true;
}

function updateWorkflowNodeStatus(node: WorkflowNode<any> | null, status: OperatorStatus) {
	if (!node) return;
	wf.value.updateNodeStatus(node.id, status);
	workflowDirty = true;
}

function selectOutput(node: WorkflowNode<any> | null, selectedOutputId: string) {
	if (!node) return;
	wf.value.selectOutput(node, selectedOutputId);
	workflowDirty = true;
}

function updateOutputPort(node: WorkflowNode<any> | null, workflowOutput: WorkflowOutput<any>) {
	if (!node) return;
	workflowService.updateOutputPort(node, workflowOutput);
	workflowDirty = true;
}

// Route is mutated then watcher is triggered to open or close the drilldown
function addOperatorToRoute(
	nodeId: string | null,
	animation: 'left' | 'right' | 'scale' = 'scale' // drilldownSpawnAnimation is set here, left/right animations are for drilldown navigation
) {
	drilldownSpawnAnimation.value = animation;
	if (nodeId !== null) {
		router.push({ query: { operator: nodeId } });
	} else {
		router.push({ query: {} });
	}
}

const openDrilldown = (node: WorkflowNode<any>) => {
	currentActiveNode.value = node;
	startTime = Date.now();
	dialogIsOpened.value = true;
};

const closeDrilldown = async () => {
	dialogIsOpened.value = false;
	const timeSpent: number = Date.now() - startTime;
	await EventService.create(
		EventType.OperatorDrilldownTiming,
		useProjects().activeProject.value?.id,
		JSON.stringify({
			node: currentActiveNode.value?.displayName,
			timeSpent
		})
	);
};

const removeNode = (nodeId: string) => {
	wf.value.removeNode(nodeId);
	workflowDirty = true;
};

const duplicateBranch = (nodeId: string) => {
	wf.value.branchWorkflow(nodeId);

	cloneNoteBookSessions();
	workflowDirty = true;
};

// We need to clone data-transform sessions, unlike other operators that are
// append-only, data-transform updates so we need to create distinct copies.
const cloneNoteBookSessions = async () => {
	const sessionIdSet = new Set<string>();

	const operationList = [DatasetTransformerOp.operation.name, RegriddingOp.operation.name];

	for (let i = 0; i < wf.value.getNodes().length; i++) {
		const node = wf.value.getNodes()[i];
		if (operationList.includes(node.operationType)) {
			const state = node.state;
			const sessionId = state.notebookSessionId as string;
			if (!sessionId) continue;
			if (!sessionIdSet.has(sessionId)) {
				sessionIdSet.add(sessionId);
			} else {
				// eslint-disable-next-line
				const session = await cloneNoteBookSession(sessionId);
				state.notebookSessionId = session.id;
				sessionIdSet.add(session.id);
			}
		}
	}
};

const addOperatorToWorkflow: Function =
	(operator: OperatorImport, nodeSize: OperatorNodeSize = OperatorNodeSize.medium) =>
	() => {
		wf.value.addNode(operator.operation, newNodePosition, {
			size: nodeSize
		});
		workflowDirty = true;
	};

function onMenuSelection(operatorType: string, menuNode: WorkflowNode<any>) {
	const name = operatorType;
	const operation = registry.getOperation(operatorType);
	const node = registry.getNode(operatorType);
	const drilldown = registry.getDrilldown(operatorType);

	const width = workflowService.getOperatorNodeSize(OperatorNodeSize.medium).width;
	newNodePosition.x = menuNode.x + 80 + width;
	newNodePosition.y = menuNode.y;

	if (name && operation && node && drilldown) {
		addOperatorToWorkflow({ name, operation, node, drilldown })();
	}
}

// Menu categories and list items are in order of appearance for separators to work
const contextMenuItems: MenuItem[] = [
	{
		label: 'Modeling',
		items: [
			{
				label: ModelFromDocumentOp.operation.displayName,
				command: addOperatorToWorkflow(ModelFromDocumentOp)
			},
			{
				label: ModelEditOp.operation.displayName,
				command: addOperatorToWorkflow(ModelEditOp)
			},
			{
				label: StratifyMiraOp.operation.displayName,
				command: addOperatorToWorkflow(StratifyMiraOp)
			},
			{
				label: ModelComparisonOp.operation.displayName,
				command: addOperatorToWorkflow(ModelComparisonOp)
			}
		]
	},
	{
		label: 'Config & Intervention',
		items: [
			{
				label: ModelConfigOp.operation.displayName,
				command: addOperatorToWorkflow(ModelConfigOp)
			},
			{
				label: FunmanOp.operation.displayName,
				command: addOperatorToWorkflow(FunmanOp)
			},
			{
				label: InterventionPolicyOp.operation.displayName,
				command: addOperatorToWorkflow(InterventionPolicyOp)
			}
		]
	},
	{
		label: 'Simulation',
		items: [
			{
				label: SimulateCiemssOp.operation.displayName,
				command: addOperatorToWorkflow(SimulateCiemssOp)
			},
			{
				label: CalibrateCiemssOp.operation.displayName,
				command: addOperatorToWorkflow(CalibrateCiemssOp)
			},
			{
				label: OptimizeCiemssOp.operation.displayName,
				command: addOperatorToWorkflow(OptimizeCiemssOp)
			},
			{
				label: SimulateEnsembleCiemssOp.operation.displayName,
				command: addOperatorToWorkflow(SimulateEnsembleCiemssOp)
			},
			{
				label: CalibrateEnsembleCiemssOp.operation.displayName,
				command: addOperatorToWorkflow(CalibrateEnsembleCiemssOp)
			}
		]
	},
	{
		label: 'Data',
		items: [
			{
				label: DatasetTransformerOp.operation.displayName,
				command: addOperatorToWorkflow(DatasetTransformerOp)
			},
			{
				label: SubsetDataOp.operation.displayName,
				command: addOperatorToWorkflow(SubsetDataOp)
			},
			{
				label: RegriddingOp.operation.displayName,
				command: addOperatorToWorkflow(RegriddingOp)
			}
		]
	}
];
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
			case AssetType.Model:
				operation = ModelOp.operation;
				state = { modelId: assetId };
				break;
			case AssetType.Dataset:
				operation = DatasetOp.operation;
				state = { datasetId: assetId };
				break;
			case AssetType.Code:
				operation = CodeAssetOp.operation;
				state = { codeAssetId: assetId };
				break;
			case AssetType.Document:
				operation = DocumentOp.operation;
				state = { documentId: assetId };
				break;
			default:
				return;
		}
		wf.value.addNode(operation, newNodePosition, { state });
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

	const t = wf.value.getTransform();
	t.x = newTransform.x;
	t.y = newTransform.y;
	t.k = newTransform.k;
}

const isCreatingNewEdge = computed(() => newEdge.value && newEdge.value.points && newEdge.value.points.length === 2);

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
		wf.value.addEdge(
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
	const edges = wf.value
		.getEdges()
		.filter(({ targetPortId, sourcePortId }) => targetPortId === portId || sourcePortId === portId);

	// Build a traversal map before we do actual removal
	const nodeMap = new Map<WorkflowNode<any>['id'], WorkflowNode<any>>(
		wf.value.getNodes().map((node) => [node.id, node])
	);
	const nodeCache = new Map<WorkflowOutput<any>['id'], WorkflowNode<any>[]>();
	wf.value.getEdges().forEach((edge) => {
		if (!edge.source || !edge.target) return;
		if (!nodeCache.has(edge.source)) nodeCache.set(edge.source, []);
		nodeCache.get(edge.source)?.push(nodeMap.get(edge.target) as WorkflowNode<any>);
	});
	const startingNodeId = edges.length > 0 ? (edges[0].source as string) : '';

	// Remove edge
	if (!isEmpty(edges)) {
		edges.forEach((edge) => {
			wf.value.removeEdge(edge.id);
		});
		workflowDirty = true;
	} else {
		logger.error(`Edges with port id:${portId} not found.`);
		return;
	}

	// cascade invalid status to downstream operators
	if (startingNodeId !== '') {
		workflowService.cascadeInvalidateDownstream(nodeMap.get(startingNodeId) as WorkflowNode<any>, nodeCache);
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

function resizeHandler(node: WorkflowNode<any>) {
	relinkEdges(node);
}

// For relinking
const dist2 = (a: Position, b: Position) => (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
const threshold2 = 5.0 * 5.0;

/*
 * Relink edges that have become detatched
 *
 * [output-port](edge source => edge target)[input-port]
 *
 * FIXME: not efficient, need cache/map for larger workflows
 */
function relinkEdges(node: WorkflowNode<any> | null) {
	const nodes = node ? [node] : wf.value.getNodes();
	const allEdges = wf.value.getEdges();

	// Note id can start with numerals, so we need [id=...]
	const getPortElement = (id: string) => d3.select(`[id='${id}']`).select('.port').node() as HTMLElement;

	// Relink heuristic, this will modify source
	const relink = (source: Position, target: Position) => {
		if (dist2(source, target) > threshold2) {
			source.x = target.x;
			source.y = target.y;
		}
	};

	for (let i = 0; i < nodes.length; i++) {
		const n = nodes[i];
		const nodePosition: Position = { x: n.x, y: n.y };

		// The input ports connects to the edge's target
		const inputs = n.inputs;
		inputs.forEach((port) => {
			const edges = allEdges.filter((e) => e.targetPortId === port.id);
			if (!edges || edges.length === 0) return;

			edges.forEach((edge) => {
				const portElem = getPortElement(edge.targetPortId as string);
				const totalOffsetY = portElem.offsetTop + portElem.offsetHeight / 2;

				const portPos = {
					x: nodePosition.x,
					y: nodePosition.y + totalOffsetY
				};
				relink(edge.points[1], portPos);
			});
		});

		// The output ports connects to the edge's source
		const outputs = n.outputs;
		outputs.forEach((port) => {
			const edges = allEdges.filter((e) => e.sourcePortId === port.id);
			if (!edges || edges.length === 0) return;

			edges.forEach((edge) => {
				const portElem = getPortElement(edge.sourcePortId as string);
				const totalOffsetY = portElem.offsetTop + portElem.offsetHeight / 2;
				const portPos = {
					x: nodePosition.x + n.width + portElem.offsetWidth * 0.5,
					y: nodePosition.y + totalOffsetY
				};
				relink(edge.points[0], portPos);
			});
		});
	}
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

function updateEdgePositions(node: WorkflowNode<any>, { x, y }) {
	wf.value.getEdges().forEach((edge) => {
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
	const teraNode = teraOperatorRefs.value.find((operatorNode) => operatorNode.id === node.id);
	if (teraNode.isEditing ?? false) {
		return;
	}
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

const unloadCheck = () => {
	if (workflowDirty) {
		workflowService.updateWorkflow(wf.value.dump());
	}
};

const handleDrilldown = () => {
	const operatorId = route.query?.operator?.toString();
	if (operatorId) {
		const operator = wf.value.getNodes().find((n) => n.id === operatorId);
		if (!operator) return;
		// Prepare drilldown navigation menus
		const { upstreamNodes, downstreamNodes } = wf.value.getNeighborNodes(operatorId);
		upstreamOperatorsNav.value = [
			{
				label: 'Upstream operators',
				items: upstreamNodes.map((upstreamNode) => ({
					label: workflowService.isAssetOperator(upstreamNode.operationType)
						? upstreamNode.outputs[0].label // Asset name
						: upstreamNode.displayName, // Operator name
					icon: workflowService.iconToOperatorMap.get(upstreamNode.operationType) ?? 'pi pi-cog',
					command: () => addOperatorToRoute(upstreamNode.id, 'right')
				}))
			}
		];
		downstreamOperatorsNav.value = [
			{
				label: 'Downstream operators',
				items: downstreamNodes.map((downstreamNode) => ({
					label: workflowService.isAssetOperator(downstreamNode.operationType)
						? downstreamNode.outputs[0].label // Asset name
						: downstreamNode.displayName, // Operator name
					icon: workflowService.iconToOperatorMap.get(downstreamNode.operationType) ?? 'pi pi-cog',
					command: () => addOperatorToRoute(downstreamNode.id, 'left')
				}))
			}
		];
		// Open drilldown
		openDrilldown(operator);
	} else {
		closeDrilldown();
	}
};

watch(
	() => [props.assetId],
	async () => {
		isRenamingWorkflow.value = false; // Closes rename input if opened in previous workflow
		if (wf.value && workflowDirty) {
			workflowService.updateWorkflow(wf.value.dump());
		}
		const workflowId = props.assetId;
		if (!workflowId) return;
		isWorkflowLoading.value = true;

		const transform = getLocalStorageTransform(workflowId);
		if (transform) {
			canvasTransform = transform;
		}
		wf.value.load(await workflowService.getWorkflow(workflowId));
		isWorkflowLoading.value = false;

		handleDrilldown();
	},
	{ immediate: true }
);

watch(
	() => [route.query],
	() => {
		if (isWorkflowLoading.value) return;
		handleDrilldown();
	},
	{ deep: true }
);

onMounted(() => {
	document.addEventListener('mousemove', mouseUpdate);
	window.addEventListener('beforeunload', unloadCheck);
	saveTimer = setInterval(() => {
		if (workflowDirty && useProjects().hasEditPermission()) {
			workflowService.updateWorkflow(wf.value.dump());
			workflowDirty = false;
		}
	}, WORKFLOW_SAVE_INTERVAL);
});

onUnmounted(() => {
	if (workflowDirty) {
		workflowService.updateWorkflow(wf.value.dump());
	}
	if (saveTimer) {
		clearInterval(saveTimer);
	}
	if (canvasTransform) {
		setLocalStorageTransform(wf.value.getId(), canvasTransform);
	}
	document.removeEventListener('mousemove', mouseUpdate);
	window.removeEventListener('beforeunload', unloadCheck);
});
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
	background-color: var(--surface-transparent);
}

.glass {
	backdrop-filter: blur(10px);
}

.button-group {
	display: flex;
	align-items: center;
	flex-direction: row;
	gap: var(--gap-small);
}

.rename-workflow {
	display: flex;
	align-items: center;
	flex-wrap: nowrap;
}
</style>
