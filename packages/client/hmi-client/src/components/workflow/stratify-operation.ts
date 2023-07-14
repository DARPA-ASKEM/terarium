import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export const StratifyOperation: Operation = {
	name: WorkflowOperationTypes.STRATIFY,
	displayName: 'Stratification',
	description: 'Stratify a model',
	inputs: [{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: false }],
	outputs: [{ type: 'model' }],
	isRunnable: false,
	action: () => {}
};
