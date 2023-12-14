import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface ModelCouplingState {}

export const ModelCouplingOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_COUPLING,
	description: 'Couple models',
	displayName: 'Couple models',
	isRunnable: true,
	inputs: [{ type: 'modelId', label: 'Model' }],
	outputs: [],
	action: () => {},

	initState: () => {
		const init: ModelCouplingState = {};
		return init;
	}
};
