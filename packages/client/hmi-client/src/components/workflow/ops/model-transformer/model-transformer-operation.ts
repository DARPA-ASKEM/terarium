import { WorkflowOperationTypes } from '@/types/workflow';
import type { Operation, BaseState } from '@/types/workflow';
import transformModel from '@assets/svg/operator-images/transform-model.svg';

export interface ModelTransformerState extends BaseState {
	modelId: string | null;
	modelConfigurationIds: string[];
	notebookSessionId?: string;
}

export const ModelTransformerOperation: Operation = {
	name: WorkflowOperationTypes.MODEL_TRANSFORMER,
	description: 'Select a model',
	imageUrl: transformModel,
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
