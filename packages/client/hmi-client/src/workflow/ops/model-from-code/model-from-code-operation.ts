import { Operation, WorkflowOperationTypes } from '@/types/operator';

export interface ModelFromCodeState {
	codeLanguage: string;
	codeContent: string;
	modelFramework: string;
}

export const ModelFromCodeOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_FROM_CODE,
	description: 'Create model',
	displayName: 'Create model from code',
	isRunnable: true,
	inputs: [],
	outputs: [],
	action: () => {},

	initState: () => {
		const init: ModelFromCodeState = {
			codeLanguage: 'python',
			codeContent: '',
			modelFramework: ''
		};
		return init;
	}
};
