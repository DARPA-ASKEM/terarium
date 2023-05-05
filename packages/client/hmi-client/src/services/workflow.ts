// Captures common actions performed on workflow nodes/edges
import { v4 as uuidv4 } from 'uuid';
import {
	Workflow,
	Operation,
	WorkflowNode,
	WorkflowStatus,
	Position,
	WorkflowEdge
} from '@/types/workflow';

/// /////////////////////////////////////////////////////////////////////////////
// TODO:
// - Should update workflow node status on modification???
// - Remove node...this also remove edges
/// /////////////////////////////////////////////////////////////////////////////

export const addNode = (wf: Workflow, op: Operation, pos: Position) => {
	const node: WorkflowNode = {
		id: uuidv4(),
		workflowId: wf.id,
		operationType: op.name,
		x: pos.x,
		y: pos.y,

		inputs: op.inputs.map((o) => ({
			id: uuidv4(),
			type: o.type,
			value: null
		})),
		outputs: op.outputs.map((o) => ({
			id: uuidv4(),
			type: o.type,
			value: null
		})),
		statusCode: WorkflowStatus.INVALID,

		// Not currently in use. May 2023
		width: 100,
		height: 100
	};

	wf.nodes.push(node);
};

export const addEdge = (
	wf: Workflow,
	sourceId: string,
	sourceOutputPortId: string,
	targetId: string,
	targetInputPortId: string,
	points: Position[]
) => {
	// Find the nodes and ports instances, not particulary efficient however the workflow is not expected to be large
	const sourceNode = wf.nodes.find((d) => d.id === sourceId);
	const targetNode = wf.nodes.find((d) => d.id === targetId);
	if (!sourceNode) return;
	if (!targetNode) return;

	const sourceOutputPort = sourceNode.outputs.find((d) => d.id === sourceOutputPortId);
	const targetInputPort = targetNode.inputs.find((d) => d.id === targetInputPortId);

	if (!sourceOutputPort) return;
	if (!targetInputPort) return;

	// Check if edge already exist
	const existingEdge = wf.edges.find(
		(d) =>
			d.source === sourceId &&
			d.sourcePortId === sourceOutputPortId &&
			d.target === targetId &&
			d.targetPortId === targetInputPortId
	);

	if (existingEdge) return;

	// Check if type is compatible
	if (sourceOutputPort.value === null) return;
	if (sourceOutputPort.type !== targetInputPort.type) return;

	// Transfer data value/reference
	targetInputPort.value = sourceOutputPort.value;

	const edge: WorkflowEdge = {
		id: uuidv4(),
		workflowId: wf.id,
		source: sourceId,
		sourcePortId: sourceOutputPortId,
		target: targetId,
		targetPortId: targetInputPortId,
		points
	};

	wf.edges.push(edge);
};

export const removeEdge = (wf: Workflow, id: string) => {
	const edgeToRemove = wf.edges.find((d) => d.id === id);
	if (!edgeToRemove) return;

	// Remove the data refernece at the targetPort
	const targetNode = wf.nodes.find((d) => d.id === edgeToRemove.target);
	if (!targetNode) return;
	const targetPort = targetNode.inputs.find((d) => d.id === edgeToRemove.targetPortId);
	if (!targetPort) return;

	targetPort.value = null;
};
