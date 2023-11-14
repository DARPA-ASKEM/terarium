import { Operation, WorkflowOperationTypes } from '@/types/operator';

export const StratifyOperation: Operation = {
	name: WorkflowOperationTypes.STRATIFY_JULIA,
	displayName: 'Stratify',
	description: 'Stratify a model',
	inputs: [{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: false }],
	outputs: [{ type: 'model' }],
	isRunnable: false,
	action: () => {}
};
