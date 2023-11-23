<template>
	<!-- add 'debug-mode' to debug this -->
	<tera-infinite-canvas
		v-if="!isWorkflowLoading"
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
					<Button icon="pi pi-plus" label="Add component" @click="showAddComponentMenu" />
					<Menu
						ref="addComponentMenu"
						:model="contextMenuItems"
						:popup="true"
						style="white-space: nowrap; width: auto"
					/>
				</div>
			</div>
		</template>
		<!-- data -->
		<template #data>
			<ContextMenu ref="contextMenu" :model="contextMenuItems" />
			<tera-operator
				v-for="(node, index) in wf.nodes"
				:key="index"
				:node="node"
				@port-selected="(port: WorkflowPort, direction: WorkflowDirection) => createNewEdge(node, port, direction)"
				@port-mouseover="onPortMouseover"
				@port-mouseleave="onPortMouseleave"
				@dragging="(event) => updatePosition(node, event)"
				@remove-operator="(event) => removeNode(event)"
				@remove-edges="removeEdges"
				@drilldown="(event) => drilldown(event)"
				:canDrag="isMouseOverCanvas"
				:isActive="currentActiveNode?.id === node.id"
			>
				<template #body>
					<tera-model-node
						v-if="node.operationType === WorkflowOperationTypes.MODEL && models"
						:models="models"
						:node="node"
						@select-model="(event) => selectModel(node, event)"
					/>
					<tera-dataset-node
						v-else-if="node.operationType === WorkflowOperationTypes.DATASET && datasets"
						:datasets="datasets"
						:node="node"
						@select-dataset="(event) => selectDataset(node, event)"
					/>
					<tera-code-asset-node
						v-else-if="node.operationType === WorkflowOperationTypes.CODE && codeAssets"
						:code-assets="codeAssets"
						:node="node"
						@select-code-asset="(event) => selectCodeAsset(node, event)"
					/>
					<tera-dataset-transformer-node
						v-else-if="
							node.operationType === WorkflowOperationTypes.DATASET_TRANSFORMER && datasets
						"
						:node="node"
						@append-input-port="(event) => appendInputPort(node, event)"
					/>
					<tera-model-transformer-node
						v-else-if="node.operationType === WorkflowOperationTypes.MODEL_TRANSFORMER && models"
						:node="node"
						@append-input-port="(event) => appendInputPort(node, event)"
					/>
					<tera-simulate-node-julia
						v-else-if="node.operationType === WorkflowOperationTypes.SIMULATE_JULIA"
						:node="node"
						@append-output-port="(event) => appendOutputPort(node, event)"
						@update-state="(event) => updateWorkflowNodeState(node, event)"
					/>
					<tera-simulate-node-ciemss
						v-else-if="node.operationType === WorkflowOperationTypes.SIMULATE_CIEMSS"
						:node="node"
						@append-output-port="(event) => appendOutputPort(node, event)"
						@update-state="(event) => updateWorkflowNodeState(node, event)"
					/>
					<tera-calibrate-node-julia
						v-else-if="node.operationType === WorkflowOperationTypes.CALIBRATION_JULIA"
						:node="node"
						@append-output-port="(event) => appendOutputPort(node, event)"
						@update-state="(event) => updateWorkflowNodeState(node, event)"
					/>
					<tera-calibrate-node-ciemss
						v-else-if="node.operationType === WorkflowOperationTypes.CALIBRATION_CIEMSS"
						:node="node"
						@append-output-port="(event) => appendOutputPort(node, event)"
						@update-state="(event) => updateWorkflowNodeState(node, event)"
					/>
					<tera-stratify-node-mira
						v-else-if="node.operationType === WorkflowOperationTypes.STRATIFY_MIRA"
					/>
					<tera-simulate-ensemble-node-ciemss
						v-else-if="node.operationType === WorkflowOperationTypes.SIMULATE_ENSEMBLE_CIEMSS"
						:node="node"
						@append-output-port="(event) => appendOutputPort(node, event)"
						@update-state="(event) => updateWorkflowNodeState(node, event)"
					/>
					<tera-calibrate-ensemble-node-ciemss
						v-else-if="node.operationType === WorkflowOperationTypes.CALIBRATE_ENSEMBLE_CIEMSS"
						:node="node"
						@append-output-port="(event) => appendOutputPort(node, event)"
						@update-state="(event) => updateWorkflowNodeState(node, event)"
					/>
					<tera-model-from-code-node
						v-else-if="node.operationType === WorkflowOperationTypes.MODEL_FROM_CODE"
						:node="node"
						@append-output-port="(event) => appendOutputPort(node, event)"
						@update-state="(event) => updateWorkflowNodeState(node, event)"
					/>
					<tera-funman-node
						v-else-if="node.operationType === WorkflowOperationTypes.FUNMAN"
						:node="node"
						@append-output-port="(event) => appendOutputPort(node, event)"
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
				v-for="i in wf.edges.length"
				:key="i"
				:id="`circle${i - 1}`"
				markerWidth="8"
				markerHeight="8"
				refX="5"
				refY="5"
			>
				<circle cx="5" cy="5" r="3" :style="`fill: #1B8073`" />
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
				<path d="M 0 0 L 8 8 L 0 16 z" :style="`fill: #1B8073; fill-opacity: 1`"></path>
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
				:marker-start="`url(#circle${isEdgeTargetSim(edge) ? index : ''})`"
				:key="index"
				fill="none"
			/>
		</template>
	</tera-infinite-canvas>
	<tera-progress-spinner v-else :font-size="2" is-centered />
	<Teleport to="body">
		<tera-drilldown
			v-if="dialogIsOpened && currentActiveNode"
			@on-close-clicked="dialogIsOpened = false"
			:title="currentActiveNode.displayName"
			:tooltip="'A brief description of the operator.'"
		>
			<component
				:is="drilldownRegistry.get(currentActiveNode.operationType)"
				:node="currentActiveNode"
				@append-output-port="(event: any) => appendOutputPort(currentActiveNode, event)"
				@update-state="(event: any) => updateWorkflowNodeState(currentActiveNode, event)"
			>
			</component>
		</tera-drilldown>
	</Teleport>
</template>

<script setup lang="ts">
import { isArray, cloneDeep, isEqual, isEmpty } from 'lodash';
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { getModelConfigurations } from '@/services/model';
import TeraInfiniteCanvas from '@/components/widgets/tera-infinite-canvas.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
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
// Operation imports
import TeraOperator from '@/workflow/tera-operator.vue';
import ContextMenu from '@/components/widgets/tera-context-menu.vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Menu from 'primevue/menu';
import * as workflowService from '@/services/workflow';
import * as d3 from 'd3';
import { AssetType, Code, Dataset, Model } from '@/types/Types';
import { useDragEvent } from '@/services/drag-drop';
import { v4 as uuidv4 } from 'uuid';

import { useProjects } from '@/composables/project';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';

import { logger } from '@/utils/logger';
import {
	ModelOperation,
	TeraModelWorkflowWrapper,
	TeraModelNode,
	ModelOperationState
} from './ops/model/mod';
import {
	SimulateCiemssOperation,
	TeraSimulateCiemss,
	TeraSimulateNodeCiemss
} from './ops/simulate-ciemss/mod';
import {
	StratifyMiraOperation,
	TeraStratifyMira,
	TeraStratifyNodeMira
} from './ops/stratify-mira/mod';
import {
	DatasetOperation,
	TeraDatasetWorkflowWrapper,
	TeraDatasetNode,
	DatasetOperationState
} from './ops/dataset/mod';
import { FunmanOperation, TeraFunman, TeraFunmanNode } from './ops/funman/mod';

import {
	CalibrateEnsembleCiemssOperation,
	TeraCalibrateEnsembleCiemss,
	TeraCalibrateEnsembleNodeCiemss
} from './ops/calibrate-ensemble-ciemss/mod';
import {
	DatasetTransformerOperation,
	TeraDatasetTransformer,
	TeraDatasetTransformerNode
} from './ops/dataset-transformer/mod';
import {
	CalibrationOperationJulia,
	TeraCalibrateNodeJulia,
	TeraCalibrateJulia,
	CalibrationOperationStateJulia
} from './ops/calibrate-julia/mod';
import {
	CalibrationOperationCiemss,
	TeraCalibrateCiemss,
	TeraCalibrateNodeCiemss
} from './ops/calibrate-ciemss/mod';
import {
	SimulateEnsembleCiemssOperation,
	TeraSimulateEnsembleCiemss,
	TeraSimulateEnsembleNodeCiemss
} from './ops/simulate-ensemble-ciemss/mod';

import {
	ModelFromCodeOperation,
	TeraModelFromCode,
	TeraModelFromCodeNode
} from './ops/model-from-code/mod';

import {
	SimulateJuliaOperation,
	TeraSimulateJulia,
	TeraSimulateNodeJulia
} from './ops/simulate-julia/mod';

import {
	ModelTransformerOperation,
	TeraModelTransformer,
	TeraModelTransformerNode
} from './ops/model-transformer/mod';

import {
	TeraCodeAssetNode,
	CodeAssetOperation,
	CodeAssetState,
	TeraCodeAssetWrapper
} from './ops/code-asset/mod';

const workflowEventBus = workflowService.workflowEventBus;
const WORKFLOW_SAVE_INTERVAL = 8000;

// FIXME: check if there is a component typing instead of any
const drilldownRegistry = new Map<string, any>();
drilldownRegistry.set(CalibrationOperationJulia.name, TeraCalibrateJulia);
drilldownRegistry.set(CalibrationOperationCiemss.name, TeraCalibrateCiemss);
drilldownRegistry.set(SimulateJuliaOperation.name, TeraSimulateJulia);
drilldownRegistry.set(SimulateCiemssOperation.name, TeraSimulateCiemss);
drilldownRegistry.set(StratifyMiraOperation.name, TeraStratifyMira);
drilldownRegistry.set(ModelFromCodeOperation.name, TeraModelFromCode);
drilldownRegistry.set(SimulateEnsembleCiemssOperation.name, TeraSimulateEnsembleCiemss);
drilldownRegistry.set(CalibrateEnsembleCiemssOperation.name, TeraCalibrateEnsembleCiemss);
drilldownRegistry.set(ModelOperation.name, TeraModelWorkflowWrapper);
drilldownRegistry.set(DatasetOperation.name, TeraDatasetWorkflowWrapper);
drilldownRegistry.set(CodeAssetOperation.name, TeraCodeAssetWrapper);
drilldownRegistry.set(DatasetTransformerOperation.name, TeraDatasetTransformer);
drilldownRegistry.set(ModelTransformerOperation.name, TeraModelTransformer);
drilldownRegistry.set(FunmanOperation.name, TeraFunman);

// Will probably be used later to save the workflow in the project
const props = defineProps<{
	assetId: string;
}>();

const newNodePosition = { x: 0, y: 0 };
let canvasTransform = { x: 0, y: 0, k: 1 };
let currentPortPosition: Position = { x: 0, y: 0 };
let isMouseOverPort: boolean = false;
let saveTimer: any = null;
let workflowDirty: boolean = false;

const isWorkflowLoading = ref(false);

const currentActiveNode = ref<WorkflowNode<any> | null>(null);
const newEdge = ref<WorkflowEdge | undefined>();
const isMouseOverCanvas = ref<boolean>(false);
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

const toggleOptionsMenu = (event) => {
	optionsMenu.value.toggle(event);
};

const isEdgeTargetSim = (edge) =>
	wf.value.nodes.find((node) => node.id === edge.target)?.operationType ===
	WorkflowOperationTypes.SIMULATE_JULIA;

const models = computed<Model[]>(() => useProjects().activeProject.value?.assets?.models ?? []);
const datasets = computed<Dataset[]>(
	() => useProjects().activeProject.value?.assets?.datasets ?? []
);

const codeAssets = computed<Code[]>(() => useProjects().activeProject.value?.assets?.code ?? []);

const refreshModelNode = async (node: WorkflowNode<ModelOperationState>) => {
	// FIXME: Need additional design to work out exactly what to show. June 2023
	const configurationList = await getModelConfigurations(node.state.modelId as string);
	configurationList.forEach((configuration) => {
		// Only add new configurations
		const existingConfig = node.outputs.find((port) => isEqual(port.value, [configuration.id]));
		if (existingConfig) {
			existingConfig.label = configuration.name;
			return;
		}

		node.outputs.push({
			id: uuidv4(),
			type: 'modelConfigId',
			label: configuration.name,
			value: [configuration.id],
			isOptional: false,
			status: WorkflowPortStatus.NOT_CONNECTED
		});
	});
};

async function selectModel(node: WorkflowNode<ModelOperationState>, data: { id: string }) {
	node.state.modelId = data.id;
	await refreshModelNode(node);
}

async function selectCodeAsset(node: WorkflowNode<CodeAssetState>, data: { id: string }) {
	node.state.codeAssetId = data.id;
}

async function updateWorkflowName() {
	const workflowClone = cloneDeep(wf.value);
	workflowClone.name = newWorkflowName.value;
	workflowService.updateWorkflow(workflowClone);
	isRenamingWorkflow.value = false;
	wf.value = await workflowService.getWorkflow(props.assetId);
}

async function selectDataset(
	node: WorkflowNode<DatasetOperationState>,
	data: { id: string; name: string }
) {
	node.state.datasetId = data.id;
	node.outputs = [
		{
			id: uuidv4(),
			type: 'datasetId',
			label: data.name,
			value: [data.id],
			isOptional: false,
			status: WorkflowPortStatus.NOT_CONNECTED
		}
	];
	workflowDirty = true;
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
	port: { type: string; label?: string; value: any }
) {
	if (!node) return;

	node.outputs.push({
		id: uuidv4(),
		type: port.type,
		label: port.label,
		value: isArray(port.value) ? port.value : [port.value],
		isOptional: false,
		status: WorkflowPortStatus.NOT_CONNECTED
	});

	// FIXME: This is a bit hacky, we should split this out into separate events, or the action
	// should be built into the Operation directly. What we are doing is to update the internal state
	// and this feels it is leaking too much low-level information
	if (node.operationType === WorkflowOperationTypes.CALIBRATION_CIEMSS) {
		const state = node.state as CalibrationOperationStateJulia;
		if (state.chartConfigs.length === 0) {
			// This only ends up showing the output of the first run, perhaps we should consider showing
			// the output of the last run, or all runs?
			state.chartConfigs.push({
				selectedRun: port.value[0],
				selectedVariable: []
			});
		}
	}
	workflowDirty = true;
}

function updateWorkflowNodeState(node: WorkflowNode<any> | null, state: any) {
	if (!node) return;
	workflowService.updateNodeState(wf.value, node.id, state);
	workflowDirty = true;
}

const drilldown = (event: WorkflowNode<any>) => {
	currentActiveNode.value = event;
	dialogIsOpened.value = true;
};

workflowEventBus.on('node-refresh', (payload: { workflowId: string; nodeId: string }) => {
	if (wf.value?.id !== payload.workflowId) return;
	const node = wf.value.nodes.find((n) => n.id === payload.nodeId);
	if (!node) return;

	if (node.operationType === WorkflowOperationTypes.MODEL) {
		// This part is a bit hacky and slow. Because we allow multiple instances of the
		// same model across many nodes in a workflow, they ALL need to be updated. However
		// this multi-models setup is also somewhat uncommon so I don't want to go out of the way
		// to communicate "model change" instead of "node change", the former seemingly out of
		// place when using the WorkflowEventBus mechanism. DC - Aug 2023
		const nodesToRefresh = wf.value.nodes.filter((n) => n.state.modelId === node.state.modelId);
		nodesToRefresh.forEach(refreshModelNode);
	}
});

// TODO: Remove
workflowEventBus.on('node-state-change', (/* payload: any */) => {
	throw new Error('bus event no longer available');
});

workflowEventBus.on('append-output-port', () => {
	throw new Error('bus event no longer available');
});

workflowEventBus.on('update-state', () => {
	throw new Error('bus event no longer available');
});

const removeNode = (event) => {
	workflowService.removeNode(wf.value, event);
};

const contextMenuItems = ref([
	{
		label: 'Model',
		command: () => {
			workflowService.addNode(wf.value, ModelOperation, newNodePosition);
			workflowDirty = true;
		}
	},
	{
		label: 'Dataset',
		command: () => {
			workflowService.addNode(wf.value, DatasetOperation, newNodePosition);
			workflowDirty = true;
		}
	},
	{
		label: 'Dataset Transformer',
		command: () => {
			workflowService.addNode(wf.value, DatasetTransformerOperation, newNodePosition);
			workflowDirty = true;
		}
	},
	{
		label: 'Model Transformer',
		command: () => {
			workflowService.addNode(wf.value, ModelTransformerOperation, newNodePosition);
			workflowDirty = true;
		}
	},
	{
		label: 'Stratify Mira',
		command: () => {
			workflowService.addNode(wf.value, StratifyMiraOperation, newNodePosition, { state: null });
			workflowDirty = true;
		}
	},
	{
		label: 'Create model',
		disabled: false,
		command: () => {
			workflowService.addNode(wf.value, ModelFromCodeOperation, newNodePosition);
			workflowDirty = true;
		}
	},
	{
		label: 'Validate model configuration',
		command: () => {
			workflowService.addNode(wf.value, FunmanOperation, newNodePosition, { state: null });
			workflowDirty = true;
		}
	},
	{
		label: 'DETERMINISTIC',
		items: [
			{
				label: 'Simulate',
				command: () => {
					workflowService.addNode(wf.value, SimulateJuliaOperation, newNodePosition, {
						size: {
							width: 420,
							height: 220
						}
					});
					workflowDirty = true;
				}
			},
			{
				label: 'Simulate ensemble',
				disabled: true,
				command: () => {}
			},
			{
				label: 'Calibrate',
				command: () => {
					workflowService.addNode(wf.value, CalibrationOperationJulia, newNodePosition);
					workflowDirty = true;
				}
			}
		]
	},
	{
		label: 'PROBABILISTIC',
		items: [
			{
				label: 'Simulate',
				command: () => {
					workflowService.addNode(wf.value, SimulateCiemssOperation, newNodePosition, {
						size: {
							width: 420,
							height: 220
						}
					});
					workflowDirty = true;
				}
			},
			{
				label: 'Calibrate & Simulate',
				disabled: false,
				command: () => {
					workflowService.addNode(wf.value, CalibrationOperationCiemss, newNodePosition, {
						size: {
							width: 420,
							height: 220
						}
					});
					workflowDirty = true;
				}
			},
			{
				label: 'Simulate ensemble',
				disabled: false,
				command: () => {
					workflowService.addNode(wf.value, SimulateEnsembleCiemssOperation, newNodePosition, {
						size: {
							width: 420,
							height: 220
						}
					});
					workflowDirty = true;
				}
			},
			{
				label: 'Calibrate ensemble',
				disabled: false,
				command: () => {
					workflowService.addNode(wf.value, CalibrateEnsembleCiemssOperation, newNodePosition, {
						size: {
							width: 420,
							height: 220
						}
					});
					workflowDirty = true;
				}
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
		assetType: AssetType;
	};

	if (assetId && assetType) {
		updateNewNodePosition(event);

		let operation: Operation;
		let state: any = null;

		switch (assetType) {
			case AssetType.Models:
				operation = ModelOperation;
				state = { modelId: assetId };
				break;
			case AssetType.Datasets:
				operation = DatasetOperation;
				state = { datasetId: assetId };
				break;
			case AssetType.Code:
				operation = CodeAssetOperation;
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
