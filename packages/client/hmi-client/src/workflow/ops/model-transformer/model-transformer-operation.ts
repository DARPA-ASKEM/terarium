import { Operation, WorkflowOperationTypes } from '@/types/workflow';

interface ModelOperationState {
	modelId: string | null;
	modelConfigurationIds: any[];
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
		const init: ModelOperationState = {
			modelId: null,
			modelConfigurationIds: []
		};
		return init;
	}
};
