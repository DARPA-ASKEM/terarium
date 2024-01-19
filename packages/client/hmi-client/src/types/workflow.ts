import { WorkflowOperationTypes } from './Types';

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
	displayName: string; // Human-readable name for each node.

	// The operation is self-runnable, that is, given just the inputs we can derive the outputs
	isRunnable: boolean;

	initState?: Function;

	action?: Function;
	validation?: Function;

	inputs: OperationData[];
	outputs: OperationData[];
}

export interface Size {
	width: number;
	height: number;
}

export interface AssetBlock<T> {
	name: string;
	includeInProcess: boolean;
	asset: T;
}
