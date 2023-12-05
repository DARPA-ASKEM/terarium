import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface ModelCouplingState {
	modelIds: string[];
}

export const ModelCouplingOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_COUPLING,
	description: 'Couple models',
	displayName: 'Couple models',
	isRunnable: true,
	inputs: [{ type: 'modelId', label: 'Model' }],
	outputs: [],
	action: () => {},

	initState: () => {
		const init: ModelCouplingState = {
			modelIds: []
		};
		return init;
	}
};
