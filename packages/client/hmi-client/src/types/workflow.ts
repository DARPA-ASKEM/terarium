export enum WorkflowStatus {
	INVALID = 'invalid',
	FAILED = 'failed',
	COMPLETED = 'completed',
	IN_PROGRESS = 'in progress',
	ERROR = 'error'
}

// Defines the type of data an operation can consume and output
export interface OperationData {
	type: string;
	label?: string;
}

// Defines a function: eg: model, simulate, calibrate
export interface Operation {
	name: string;
	description: string;

	// The operation is self-runnable, that is, given just the inputs we can derive the outputs
	isRunnable: boolean;

	action?: Function;
	validation?: Function;

	inputs: OperationData[];
	outputs: OperationData[];
}

// Defines the data-exchange between WorkflowNode
// In most cases the value here will be an assetId
export interface WorkflowPort {
	id: string;
	type: string;
	label?: string;
	value?: any;
}

// Node definition in the workflow
// This is the graphical operation of the operation defined in operationType
export interface WorkflowNode {
	id: string;
	workflowId: string;
	operationType: string;

	// Position on canvas
	x: number;
	y: number;
	width: number;
	height: number;

	inputs: WorkflowPort[];
	outputs: WorkflowPort[];

	// FIXME: The section below is slated to be further spec'ed out later.
	// State and progress, tracking of intermediate results
	statusCode: WorkflowStatus;
	intermediateIds?: WorkflowPort[];
}

export interface WorkflowEdge {
	id: string;
	workflowId: string;
	points: { x: number; y: number }[];

	source: WorkflowNode['id'];
	sourcePortId: string;

	target: WorkflowNode['id'];
	targetPortId: string;
}

export interface Workflow {
	id: string;
	name: string;
	description: string;

	// zoom x-y translate and zoom
	transform: {
		x: number;
		y: number;
		k: number;
	};
	nodes: WorkflowNode[];
	edges: WorkflowEdge[];
}
