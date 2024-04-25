import type { Operation, BaseState } from '@/types/workflow';
import { WorkflowOperationTypes } from '@/types/workflow';

export const DOCUMENTATION_URL =
	'https://algebraicjulia.github.io/Decapodes.jl/dev/overview/#Merging-Multiple-Physics';

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
