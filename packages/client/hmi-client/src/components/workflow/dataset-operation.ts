import { Operation } from '@/types/workflow';

export const DatasetOperation: Operation = {
	name: 'Dataset',
	description: 'Select a dataset',
	isRunnable: true,
	inputs: [],
	outputs: [{ type: 'dataset' }],
	action: () => { }
};
