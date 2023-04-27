// Defines the type of data an operation can consume and output
export interface OperationData {
	type: string;
	label?: string;
}

// Defines a function: eg: model, simulate, calibrate
export interface Operation {
	name: string;
	description: string;
	action: Function;
	validation?: Function;

	inputs: OperationData[];
	outputs: OperationData[];
}

// Defines the data-exchange between WorkflowNode
// In most cases the value here will be an assetId
export interface WorkflowPort {
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
	statusCode?: string; // not-run, completed, in-progress, error, invalid
	intermediateIds?: WorkflowPort[];
}

export interface WorkflowEdge {
	id: string;
	workflowId: string;
	points: { x: number; y: number }[];

	source: string;
	sourcePort: number;

	target: string;
	targetPort: number;
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
