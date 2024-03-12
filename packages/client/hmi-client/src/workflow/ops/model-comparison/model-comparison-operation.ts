import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface NotebookHistory {
	code: string;
	timestamp: number;
}

export interface ModelComparisonOperationState {
	notebookHistory: NotebookHistory[];
	hasCodeRun: boolean;
	structuralComparisons: string[];
}

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
		const init: ModelComparisonOperationState = {
			notebookHistory: [],
			hasCodeBeenRun: false,
			structuralComparisons: []
		};
		return init;
	}
};
