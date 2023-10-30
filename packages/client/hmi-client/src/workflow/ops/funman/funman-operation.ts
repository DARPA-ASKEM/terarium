import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export const FunmanOperation: Operation = {
	name: WorkflowOperationTypes.FUNMAN,
	displayName: 'Validate model configuration',
	description: 'Validate model configuration',
	inputs: [{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: false }],
	outputs: [],
	isRunnable: true,
	action: () => {}
};
