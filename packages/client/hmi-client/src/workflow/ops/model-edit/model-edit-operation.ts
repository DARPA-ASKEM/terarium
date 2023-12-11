import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface ModelEditCode {
	code: string;
	timestamp: number;
}

export interface ModelEditOperationState {
	modelEditCodeHistory: ModelEditCode[];
	hasCodeBeenRun: boolean;
}

export const ModelEditOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_EDIT,
	displayName: 'Model Edit',
	description: 'Edit a model',
	isRunnable: false,
	inputs: [{ type: 'modelId', label: 'Model', acceptMultiple: false }],
	outputs: [{ type: 'modelId' }],
	action: async () => ({}),
	initState: () => {
		const init: ModelEditOperationState = {
			modelEditCodeHistory: [],
			hasCodeBeenRun: false
		};
		return init;
	}
};
