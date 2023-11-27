import { Operation } from '@/types/workflow';

export const name = 'ModelConfigOperation';

export interface ModelConfigOperationState {
	modelId: string | null;
}

export const ModelConfigOperation: Operation = {
	name,
	displayName: 'Model Configuration',
	description: 'Create model configurations.',
	isRunnable: true,
	inputs: [{ type: 'modelId' }],
	outputs: [{ type: 'modelConfigId' }],
	action: async () => ({}),

	initState: () => {
		const init: ModelConfigOperationState = {
			modelId: null
		};
		return init;
	}
};
