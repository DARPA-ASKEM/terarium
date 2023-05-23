import { Operation } from '@/types/workflow';

interface StringValueMap {
	[key: string]: number;
}

export const ModelOperation: Operation = {
	name: 'ModelOperation',
	description: 'Select a model and configure its initial and parameter values.',
	isRunnable: true,
	inputs: [],
	outputs: [{ type: 'modelConfig' }],
	action: async (modelConfig: {
		id: string;
		intialValues: StringValueMap;
		parameterValues: StringValueMap;
	}) => [{ type: 'modelConfig', value: modelConfig }]
};
