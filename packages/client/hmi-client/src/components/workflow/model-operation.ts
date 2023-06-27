import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export const ModelOperation: Operation = {
	name: WorkflowOperationTypes.MODEL,
	description: 'Select a model and configure its initial and parameter values.',
	isRunnable: true,
	inputs: [],
	outputs: [{ type: 'modelConfigId' }],
	action: async (modelConfigId: string) => [{ type: 'modelConfigId', value: modelConfigId }]
};
