import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export const DatasetOperation: Operation = {
	name: WorkflowOperationTypes.DATASET,
	description: 'Select a dataset',
	isRunnable: true,
	inputs: [],
	outputs: [{ type: 'dataset' }],
	action: () => {}
};
