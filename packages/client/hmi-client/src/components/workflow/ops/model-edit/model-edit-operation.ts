import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';
import { NotebookHistory } from '@/services/notebook';

const DOCUMENTATION_URL =
	'https://github.com/DARPA-ASKEM/beaker-kernel/blob/main/docs/contexts_mira_model_edit.md';

export interface ModelEditCode {
	code: string;
	timestamp: number;
}

export interface ModelEditOperationState extends BaseState {
	notebookHistory: NotebookHistory[];
	hasCodeRun: boolean;
}

export const ModelEditOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_EDIT,
	displayName: 'Edit model',
	description: 'Edit a model',
	documentationUrl: DOCUMENTATION_URL,
	isRunnable: false,
	inputs: [
		{ type: 'modelId|modelConfigId', label: 'Model or Model configuration', acceptMultiple: false }
	],
	outputs: [{ type: 'modelId' }],
	action: async () => ({}),
	initState: () => {
		const init: ModelEditOperationState = {
			notebookHistory: [],
			hasCodeRun: false
		};
		return init;
	}
};
