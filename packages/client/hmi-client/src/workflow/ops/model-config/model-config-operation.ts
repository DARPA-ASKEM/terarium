import { Operation, WorkflowOperationTypes } from '@/types/workflow';
import { Initial, ModelParameter } from '@/types/Types';

export const name = 'ModelConfigOperation';

export interface ModelConfigOperationState {
	modelId: string | null;
	configName: string;
	configDescription: string;
	configInitials?: Initial[];
	configParams?: ModelParameter[];
}

export const ModelConfigOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_CONFIG,
	displayName: 'Model Configuration',
	description: 'Create model configurations.',
	isRunnable: true,
	inputs: [{ type: 'modelId' }],
	outputs: [{ type: 'modelConfigId' }],
	action: async () => ({}),

	initState: () => {
		const init: ModelConfigOperationState = {
			modelId: null,
			configName: '',
			configDescription: ''
		};
		return init;
	}
};
