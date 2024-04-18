import type { Operation, BaseState } from '@/types/workflow';
import { WorkflowOperationTypes } from '@/types/workflow';

export interface ModelCouplingState extends BaseState {}

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
