import { Operation } from '@/types/workflow';
import { getModel } from '@/services/model';
import { Model } from '@/types/Model';

export const ModelOperation: Operation = {
	name: 'ModelOperation',
	description: '',
	isRunnable: true,
	inputs: [],
	outputs: [{ type: 'Model' }],
	action: async (selectedModel: Model) => [
		{ type: 'Model', value: await getModel(selectedModel.id.toString()) }
	]
};
