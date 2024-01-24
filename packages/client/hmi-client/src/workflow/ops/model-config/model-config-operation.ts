import { Operation, WorkflowOperationTypes } from '@/types/workflow';
import type { Initial, ModelParameter } from '@/types/Types';

export const name = 'ModelConfigOperation';

export interface ModelConfigOperationState {
	modelId: string | null;
	name: string;
	description: string;
	initials?: Initial[];
	parameters?: ModelParameter[];
}

export const ModelConfigOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_CONFIG,
	displayName: 'Configure model',
	description: 'Create model configurations.',
	isRunnable: true,
	inputs: [{ type: 'modelId' }],
	outputs: [{ type: 'modelConfigId' }],
	action: async () => ({}),

	initState: () => {
		const init: ModelConfigOperationState = {
			modelId: null,
			name: '',
			description: ''
		};
		return init;
	}
};
