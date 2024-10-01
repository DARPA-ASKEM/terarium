import { Operation, WorkflowOperationTypes, BaseState } from '@/types/workflow';
import model from '@assets/svg/operator-images/model.svg';

export interface ModelOperationState extends BaseState {
	modelId: string | null;
}

export const ModelOperation: Operation = {
	name: WorkflowOperationTypes.MODEL,
	displayName: 'Model',
	description: 'Select a model and configure its initial and parameter values.',
	imageUrl: model,
	isRunnable: true,
	inputs: [],
	outputs: [{ type: 'modelId', label: 'Model' }],
	action: async () => ({}),

	initState: () => {
		const init: ModelOperationState = {
			modelId: null
		};
		return init;
	}
};
