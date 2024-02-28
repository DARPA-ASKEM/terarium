import type { Position } from '@/types/common';

export enum WorkflowOperationTypes {
	ADD = 'add', // temp for test to work
	TEST = 'TestOperation',
	CALIBRATION_JULIA = 'CalibrationOperationJulia',
	CALIBRATION_CIEMSS = 'CalibrationOperationCiemss',
	DATASET = 'Dataset',
	MODEL = 'ModelOperation',
	SIMULATE_JULIA = 'SimulateJuliaOperation',
	SIMULATE_CIEMSS = 'SimulateCiemssOperation',
	STRATIFY_JULIA = 'StratifyJulia',
	STRATIFY_MIRA = 'StratifyMira',
	SIMULATE_ENSEMBLE_CIEMSS = 'SimulateEnsembleCiemms',
	CALIBRATE_ENSEMBLE_CIEMSS = 'CalibrateEnsembleCiemms',
	DATASET_TRANSFORMER = 'DatasetTransformer',
	MODEL_TRANSFORMER = 'ModelTransformer',
	MODEL_FROM_CODE = 'ModelFromCode',
	FUNMAN = 'Funman',
	CODE = 'Code',
	MODEL_COMPARISON = 'ModelComparison',
	MODEL_CONFIG = 'ModelConfiguraiton',
	MODEL_OPTIMIZE = 'ModelOptimize',
	MODEL_COUPLING = 'ModelCoupling',
	MODEL_EDIT = 'ModelEdit',
	DOCUMENT = 'Document',
	MODEL_FROM_DOCUMENT = 'ModelFromDocument'
}

export enum OperatorStatus {
	DEFAULT = 'default',
	IN_PROGRESS = 'in progress',
	SUCCESS = 'success',
	INVALID = 'invalid',
	WARNING = 'warning', // Probably won't be used - would there be potential crossover with INVALID?
	FAILED = 'failed',
	ERROR = 'error',
	DISABLED = 'disabled'
}

export enum WorkflowPortStatus {
	CONNECTED = 'connected',
	NOT_CONNECTED = 'not connected'
}

// Defines the type of data an operation can consume and output
export interface OperationData {
	type: string;
	label?: string;
	isOptional?: boolean;
	acceptMultiple?: boolean;
}

// Defines a function: eg: model, simulate, calibrate
export interface Operation {
	name: WorkflowOperationTypes;
	description: string;
	displayName: string; // Human-readable name for each node.

	// The operation is self-runnable, that is, given just the inputs we can derive the outputs
	isRunnable: boolean;

	initState?: Function;

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
	isOptional: boolean;
	acceptMultiple?: boolean;
}

// Operator Output needs more information than a standard operator port.
export interface WorkflowOutput<S> extends WorkflowPort {
	isSelected?: boolean;
	operatorStatus?: OperatorStatus;
	state?: Partial<S>;
	timestamp?: Date;
}

// Node definition in the workflow
// This is the graphical operation of the operation defined in operationType
export interface WorkflowNode<S> {
	// Information
	id: string;
	displayName: string;
	workflowId: string;
	operationType: WorkflowOperationTypes;

	// Position on canvas
	x: number;
	y: number;
	width: number;
	height: number;

	// Current operator state
	state: S; // Internal state. For example chosen model, display color ... etc
	active?: WorkflowOutput<S>['id'] | null;

	// I/O
	inputs: WorkflowPort[];
	outputs: WorkflowOutput<S>[];

	// Behaviour
	status: OperatorStatus;
}

export interface WorkflowEdge {
	id: string;
	workflowId: string;
	points: Position[];

	source?: WorkflowNode<any>['id'];
	sourcePortId?: string;

	target?: WorkflowNode<any>['id'];
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
	nodes: WorkflowNode<any>[];
	edges: WorkflowEdge[];
}

export interface Size {
	width: number;
	height: number;
}

export enum ProgressState {
	RETRIEVING = 'retrieving',
	QUEUED = 'queued',
	RUNNING = 'running',
	COMPLETE = 'complete'
}

export interface AssetBlock<T> {
	name: string;
	includeInProcess: boolean;
	asset: T;
}
