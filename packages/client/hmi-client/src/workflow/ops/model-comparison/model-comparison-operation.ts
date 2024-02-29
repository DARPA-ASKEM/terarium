import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface ModelComparisonOperationState {}

export const ModelComparisonOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_COMPARISON,
	displayName: 'Compare models',
	description: '',
	inputs: [
		{ type: 'modelId', label: 'Model' },
		{ type: 'modelId', label: 'Model' }
	],
	outputs: [],
	isRunnable: true,
	action: () => {},
	initState: () => {
		const init: ModelComparisonOperationState = {};
		return init;
	}
};
