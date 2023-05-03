import { Operation } from '@/types/workflow';
import { Model } from '@/types/Model';

interface StringValueMap {
	[key: string]: string;
}

export const modelOperation: Operation = {
	name: 'ModelOperation',
	description: 'Select a model and configure its initial and parameter values.',
	isRunnable: true,
	inputs: [],
	outputs: [{ type: 'modelConfig' }],
	action: async (modelConfig: {
		model: Model;
		intialValues: StringValueMap;
		parameterValues: StringValueMap;
	}) => [{ type: 'modelConfig', value: modelConfig }]
};
