import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface ModelFromCodeState {
	codeLanguage: string;
	codeContent: string;
	modelFramework: string;
	modelId: string;
	isSaved?: boolean;
}

export const ModelFromCodeOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_FROM_CODE,
	description: 'Create model',
	displayName: 'Create model from code',
	isRunnable: true,
	inputs: [{ type: 'codeAssetId', label: 'Code' }],
	outputs: [],
	action: () => {},

	initState: () => {
		const init: ModelFromCodeState = {
			codeLanguage: 'python',
			codeContent: '',
			modelFramework: 'Petrinet',
			modelId: ''
		};
		return init;
	}
};
