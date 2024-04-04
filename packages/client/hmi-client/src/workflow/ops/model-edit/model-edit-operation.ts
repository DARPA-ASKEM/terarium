import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';

export interface ModelEditCode {
	code: string;
	timestamp: number;
}

export interface ModelEditOperationState extends BaseState {
	modelEditCodeHistory: ModelEditCode[];
	hasCodeBeenRun: boolean;
}

export const ModelEditOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_EDIT,
	displayName: 'Edit model',
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
