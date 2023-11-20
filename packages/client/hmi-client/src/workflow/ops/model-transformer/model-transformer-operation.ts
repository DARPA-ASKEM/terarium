import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface ModelTransformerState {
	modelId: string | null;
	modelConfigurationIds: string[];
	notebookSessionId?: string;
}

export const ModelTransformerOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_TRANSFORMER,
	description: 'Select a model',
	displayName: 'Model Transformer',
	isRunnable: true,
	inputs: [{ type: 'modelConfigId', label: 'Model Configuration' }],
	outputs: [],
	action: () => {},

	initState: () => {
		const init: ModelTransformerState = {
			modelId: null,
			modelConfigurationIds: []
		};
		return init;
	}
};
