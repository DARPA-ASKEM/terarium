import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';
import { NotebookHistory } from '@/services/notebook';
import editModel from '@assets/svg/operator-images/edit-model.svg';

export interface ModelEditOperationState extends BaseState {
	notebookHistory: NotebookHistory[];
	hasCodeRun: boolean;
}

export const ModelEditOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_EDIT,
	displayName: 'Edit model',
	description: 'Edit a model',
	imageUrl: editModel,
	isRunnable: false,
	inputs: [{ type: 'modelId|modelConfigId', label: 'Model or Model configuration' }],
	outputs: [{ type: 'modelId', label: 'Model' }],
	action: async () => ({}),
	initState: () => {
		const init: ModelEditOperationState = {
			notebookHistory: [],
			hasCodeRun: false
		};
		return init;
	}
};
