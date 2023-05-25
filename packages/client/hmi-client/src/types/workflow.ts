export enum WorkflowOperationTypes {
	ADD = 'add', // temp for test to work
	TEST = 'TestOperation',
	CALIBRATION = 'CalibrationOperation',
	DATASET = 'Dataset',
	MODEL = 'ModelOperation',
	SIMULATE = 'SimulateOperation'
}

export enum WorkflowStatus {
	INVALID = 'invalid',
	FAILED = 'failed',
	COMPLETED = 'completed',
	IN_PROGRESS = 'in progress',
	ERROR = 'error'
}

export enum WorkflowPortStatus {
	CONNECTED = 'connected',
	NOT_CONNECTED = 'not connected'
}

// Defines the type of data an operation can consume and output
export interface OperationData {
	type: string;
	label?: string;
	acceptMultiple?: boolean;
}

// Defines a function: eg: model, simulate, calibrate
export interface Operation {
	name: WorkflowOperationTypes;
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
	status: WorkflowPortStatus;
	label?: string;
	value?: any[] | null;
	acceptMultiple?: boolean;
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
	points: Position[];

	source?: WorkflowNode['id'];
	sourcePortId?: string;

	target?: WorkflowNode['id'];
	targetPortId?: string;

	// is this edge being started from an input or output?
	// not persisted; only used during edge creation
	direction?: WorkflowDirection;
}

export enum WorkflowDirection {
	FROM_INPUT,
	FROM_OUTPUT
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

export interface Position {
	x: number;
	y: number;
}

export interface Size {
	width: number;
	height: number;
}
