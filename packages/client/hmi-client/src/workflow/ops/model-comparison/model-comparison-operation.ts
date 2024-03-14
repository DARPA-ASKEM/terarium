import { Operation, WorkflowOperationTypes } from '@/types/workflow';
import { NotebookHistory } from '@/services/notebook';

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
			hasCodeRun: false,
			structuralComparisons: []
		};
		return init;
	}
};
