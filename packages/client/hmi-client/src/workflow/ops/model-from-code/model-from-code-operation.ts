import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface ModelFromCodeState {
	codeLanguage: string;
	codeContent: string;
	modelFramework: string;
}

export const ModelFromCodeOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_FROM_CODE,
	description: 'Create model',
	displayName: 'Create Model from Code',
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
