import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface ModelEditState {
	code: string;
}

export const ModelEdit: Operation = {
	name: WorkflowOperationTypes.MODEL,
	displayName: 'Model',
	description: '',
	isRunnable: true,
	inputs: [{ type: 'modelId' }],
	outputs: [{ type: 'modelId' }],
	action: async () => ({}),

	initState: () => {
		const init: ModelEditState = {
			code: ''
		};
		return init;
	}
};
