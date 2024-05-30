import { WorkflowOperationTypes } from '@/types/workflow';
import type { Operation, BaseState } from '@/types/workflow';

export interface ModelTransformerState extends BaseState {
	modelId: string | null;
	modelConfigurationIds: string[];
	notebookSessionId?: string;
}

export const ModelTransformerOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_TRANSFORMER,
	description: 'Select a model',
	displayName: 'Transform model',
	isRunnable: true,
	inputs: [{ type: 'modelId', label: 'Model' }],
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
