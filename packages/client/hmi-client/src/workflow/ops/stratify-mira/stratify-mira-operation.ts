import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export const StratifyMiraOperation: Operation = {
	name: WorkflowOperationTypes.STRATIFY_MIRA,
	displayName: 'Stratify MIRA',
	description: 'Stratify a model',
	inputs: [{ type: 'modelConfigId', label: 'Model configuration', acceptMultiple: false }],
	outputs: [{ type: 'model' }],
	isRunnable: false,
	action: () => {}
};
