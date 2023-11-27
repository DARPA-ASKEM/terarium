import { Component } from 'vue';
import { v4 as uuidv4 } from 'uuid';
import _, { cloneDeep } from 'lodash';
import API from '@/api/api';
import { logger } from '@/utils/logger';
import { EventEmitter } from '@/utils/emitter';
import {
	Operation,
	Position,
	Size,
	Workflow,
	WorkflowEdge,
	WorkflowNode,
	WorkflowPortStatus,
	OperatorStatus,
	WorkflowPort,
	WorkflowOutput
} from '@/types/workflow';

/**
 * Captures common actions performed on workflow nodes/edges. The functions here are
 * not optimized, on the account that we don't expect most workflow graphs to
 * exceed say ... 10-12 nodes with 30-40 edges.
 *
 */
export const emptyWorkflow = (name: string = 'test', description: string = '') => {
	const workflow: Workflow = {
		id: uuidv4(),
		name,
		description,

		transform: { x: 0, y: 0, k: 1 },
		nodes: [],
		edges: []
	};
	return workflow;
};

const defaultNodeSize: Size = { width: 180, height: 220 };
export const addNode = (
	wf: Workflow,
	op: Operation,
	pos: Position,
	options: { size?: Size; state?: any } = { size: defaultNodeSize, state: {} }
) => {
	const node: WorkflowNode<any> = {
		id: uuidv4(),
		workflowId: wf.id,
		operationType: op.name,
		displayName: op.displayName,
		x: pos.x,
		y: pos.y,
		state: options.state,

		inputs: op.inputs.map((port) => ({
			id: uuidv4(),
			type: port.type,
			label: port.label,
			status: WorkflowPortStatus.NOT_CONNECTED,
			value: null,
			isOptional: false,
			acceptMultiple: port.acceptMultiple
		})),
		outputs: [],
		/*
		outputs: op.outputs.map((port) => ({
			id: uuidv4(),
			type: port.type,
			label: port.label,
			status: WorkflowPortStatus.NOT_CONNECTED,
			value: null
		})),
	  */
		status: OperatorStatus.INVALID,

		width: options?.size?.width ?? defaultNodeSize.width,
		height: options?.size?.height ?? defaultNodeSize.height
	};
	if (op.initState && _.isEmpty(node.state)) {
		node.state = op.initState();
	}

	wf.nodes.push(node);
};

export const addEdge = (
	wf: Workflow,
	sourceId: string,
	sourcePortId: string,
	targetId: string,
	targetPortId: string,
	points: Position[]
) => {
	const sourceNode = wf.nodes.find((d) => d.id === sourceId);
	const targetNode = wf.nodes.find((d) => d.id === targetId);
	if (!sourceNode) return;
	if (!targetNode) return;

	const sourceOutputPort = sourceNode.outputs.find((d) => d.id === sourcePortId);
	const targetInputPort = targetNode.inputs.find((d) => d.id === targetPortId);

	if (!sourceOutputPort) return;
	if (!targetInputPort) return;

	// Check if edge already exist
	const existingEdge = wf.edges.find(
		(d) =>
			d.source === sourceId &&
			d.sourcePortId === sourcePortId &&
			d.target === targetId &&
			d.targetPortId === targetPortId
	);

	if (existingEdge) return;

	// Check if type is compatible
	if (sourceOutputPort.value === null) return;
	if (sourceOutputPort.type !== targetInputPort.type) return;
	if (!targetInputPort.acceptMultiple && targetInputPort.status === WorkflowPortStatus.CONNECTED) {
		return;
	}

	// Transfer data value/reference
	if (targetInputPort.acceptMultiple && targetInputPort.value && sourceOutputPort.value) {
		targetInputPort.label = `${sourceOutputPort.label},${targetInputPort.label}`;
		targetInputPort.value = [...targetInputPort.value, ...sourceOutputPort.value];
	} else {
		targetInputPort.label = sourceOutputPort.label;
		targetInputPort.value = sourceOutputPort.value;
	}

	const edge: WorkflowEdge = {
		id: uuidv4(),
		workflowId: wf.id,
		source: sourceId,
		sourcePortId,
		target: targetId,
		targetPortId,
		points: _.cloneDeep(points)
	};

	wf.edges.push(edge);
	sourceOutputPort.status = WorkflowPortStatus.CONNECTED;
	targetInputPort.status = WorkflowPortStatus.CONNECTED;
};

export const removeEdge = (wf: Workflow, id: string) => {
	const edgeToRemove = wf.edges.find((d) => d.id === id);
	if (!edgeToRemove) return;

	// Remove the data reference at the targetPort
	const targetNode = wf.nodes.find((d) => d.id === edgeToRemove.target);
	if (!targetNode) return;
	const targetPort = targetNode.inputs.find((d) => d.id === edgeToRemove.targetPortId);
	if (!targetPort) return;
	targetPort.value = null;
	targetPort.status = WorkflowPortStatus.NOT_CONNECTED;
	delete targetPort.label;

	// Edge re-assignment
	wf.edges = wf.edges.filter((edge) => edge.id !== id);

	// If there are no more references reset the connected status of the source node
	if (_.isEmpty(wf.edges.filter((e) => e.source === edgeToRemove.source))) {
		const sourceNode = wf.nodes.find((d) => d.id === edgeToRemove.source);
		if (!sourceNode) return;
		const sourcePort = sourceNode.outputs.find((d) => d.id === edgeToRemove.sourcePortId);
		if (!sourcePort) return;
		sourcePort.status = WorkflowPortStatus.NOT_CONNECTED;
	}
};

export const removeNode = (wf: Workflow, id: string) => {
	// Remove all the edges first
	const edgesToRemove = wf.edges.filter((d) => d.source === id || d.target === id);
	const edgeIds = edgesToRemove.map((d) => d.id);
	edgeIds.forEach((edgeId) => {
		removeEdge(wf, edgeId);
	});

	// Remove the node
	wf.nodes = wf.nodes.filter((node) => node.id !== id);
};

export const updateNodeState = (wf: Workflow, nodeId: string, state: any) => {
	const node = wf.nodes.find((d) => d.id === nodeId);
	if (!node) return;
	node.state = state;
};

// Get port label for frontend
const defaultPortLabels = {
	modelId: 'Model',
	modelConfigId: 'Model configuration',
	datasetId: 'Dataset'
};

export function getPortLabel({ label, type, isOptional }: WorkflowPort) {
	let portLabel = type; // Initialize to port type (fallback)

	// Assign to name of port value
	if (label) portLabel = label;
	// Assign to default label using port type
	else if (defaultPortLabels[type]) {
		portLabel = defaultPortLabels[type];
	}

	if (isOptional) portLabel = portLabel.concat(' (optional)');

	return portLabel;
}

/**
 * API hooks: Handles reading and writing back to the store
 * */

// Create
export const createWorkflow = async (workflow: Workflow) => {
	const response = await API.post('/workflows', workflow);
	return response?.data ?? null;
};

// Update
export const updateWorkflow = async (workflow: Workflow) => {
	const id = workflow.id;
	const response = await API.put(`/workflows/${id}`, workflow);
	return response?.data ?? null;
};

// Get
export const getWorkflow = async (id: string) => {
	const response = await API.get(`/workflows/${id}`);
	return response?.data ?? null;
};

/// /////////////////////////////////////////////////////////////////////////////
// Events bus for workflow
/// /////////////////////////////////////////////////////////////////////////////
class WorkflowEventEmitter extends EventEmitter {
	emitNodeStateChange(payload: { workflowId: string; nodeId: string; state: any }) {
		this.emit('node-state-change', payload);
	}

	emitNodeRefresh(payload: { workflowId: string; nodeId: string }) {
		this.emit('node-refresh', payload);
	}
}

export const workflowEventBus = new WorkflowEventEmitter();

/// /////////////////////////////////////////////////////////////////////////////
// Workflow component registry, this is used to
// dynamically determine which component should be rendered
/// /////////////////////////////////////////////////////////////////////////////
export class WorkflowRegistry {
	nodeMap: Map<string, Component>;

	drilldownMap: Map<string, Component>;

	constructor() {
		this.nodeMap = new Map();
		this.drilldownMap = new Map();
	}

	set(name: string, node: Component, drilldown: Component) {
		this.nodeMap.set(name, node);
		this.drilldownMap.set(name, drilldown);
	}

	getNode(name: string) {
		return this.nodeMap.get(name);
	}

	getDrilldown(name: string) {
		return this.drilldownMap.get(name);
	}

	remove(name: string) {
		this.nodeMap.delete(name);
		this.drilldownMap.delete(name);
	}
}

///
// Operator
///

/**
 * Updates the operator's state using the data from a specified WorkflowOutput. If the operator's
 * current state was not previously stored as a WorkflowOutput, this function first saves the current state
 * as a new WorkflowOutput. It then replaces the operator's existing state with the data from the specified WorkflowOutput.
 *
 * @param operator - The operator whose state is to be updated.
 * @param selectedWorkflowOutputId - The ID of the WorkflowOutput whose data will be used to update the operator's state.
 */

export function selectOutput(
	operator: WorkflowNode<any>,
	selectedWorkflowOutputId: WorkflowOutput<any>['id']
) {
	// Check if the current state existed previously in the outputs
	let current = operator.outputs.find((output) => output.id === operator.active);
	if (!current) {
		// the current state was never saved in the outputs prior
		current = {
			id: uuidv4(),
			type: '',
			status: WorkflowPortStatus.NOT_CONNECTED,
			isOptional: false,
			isSelected: false,
			operatorStatus: OperatorStatus.DEFAULT,
			state: null,
			timestamp: new Date()
		} as WorkflowOutput<any>;
		operator.outputs.push(current);
	}

	// Update the current state within the outputs
	current.state = cloneDeep(operator.state);
	current.operatorStatus = operator.status;
	current.timestamp = new Date();

	// Update the Operator state with the selected one
	const selected = operator.outputs.find((output) => output.id === selectedWorkflowOutputId);
	if (selected) {
		operator.state = selected.state;
		operator.status = selected.operatorStatus;
		operator.active = selected.id;
	} else {
		logger.warn(
			`Operator Output Id ${selectedWorkflowOutputId} does not exist within ${operator.displayName} Operator ${operator.id}.`
		);
	}
}
