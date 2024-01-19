import { WorkflowOperationTypes } from '@/types/Types';
import { Operation } from '@/types/workflow';

export interface ModelCouplingState {}

export const ModelCouplingOperation: Operation = {
	name: WorkflowOperationTypes.ModelCoupling,
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
